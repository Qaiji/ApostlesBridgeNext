package com.medua.apostlesbridgenext.handler;

import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.util.ImagePreview;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Style;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class ImagePreviewHandler {
    public static final int PADDING = 5;
    public static final String IMAGE_PREVIEW_INSERTION = "apostlesbridgenext:image-preview:";

    private static final Map<String, ImagePreview> PREVIEWS = new ConcurrentHashMap<>();
    private static final Set<String> PREVIEWABLE_URLS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private ImagePreviewHandler() {
    }

    public static void register() {
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof ChatScreen) {
                ScreenEvents.afterRender(screen).register(ImagePreviewHandler::render);
            }
        });
    }

    public static void registerImageUrl(String imageUrl) {
        PREVIEWABLE_URLS.add(URI.create(imageUrl).toString());
    }

    private static void render(Screen screen, DrawContext context, int mouseX, int mouseY, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!(screen instanceof ChatScreen) || client.world == null) {
            return;
        }

        Style hoveredStyle = getHoveredStyle(client, mouseX, mouseY);
        String imageUrl = getImageUrl(hoveredStyle);
        if (imageUrl == null) {
            return;
        }

        ImagePreview preview = PREVIEWS.computeIfAbsent(imageUrl, ImagePreview::new);
        preview.load(client);
        preview.render(context, client, getMaxWidth(client), getMaxHeight(client));
    }

    private static int getMaxWidth(MinecraftClient client) {
        if (client.isCtrlPressed()) {
            return Math.max(1, client.getWindow().getScaledWidth() - PADDING * 2 - 2);
        }

        return Config.getImagePreviewSize().maxWidth();
    }

    private static int getMaxHeight(MinecraftClient client) {
        if (client.isCtrlPressed()) {
            return Math.max(1, client.getWindow().getScaledHeight() - PADDING * 2 - 2);
        }

        return Config.getImagePreviewSize().maxHeight();
    }

    private static Style getHoveredStyle(MinecraftClient client, int mouseX, int mouseY) {
        Style hoveredStyle = getLegacyHoveredStyle(client, mouseX, mouseY);
        if (hoveredStyle != null) {
            return hoveredStyle;
        }

        return getDrawnTextHoveredStyle(client, mouseX, mouseY);
    }

    private static Style getLegacyHoveredStyle(MinecraftClient client, int mouseX, int mouseY) {
        try {
            Method method = getDeclaredMethod(client.inGameHud.getChatHud().getClass(), new String[]{"getTextStyleAt", "method_1816"}, double.class, double.class);
            method.setAccessible(true);
            return (Style) method.invoke(client.inGameHud.getChatHud(), (double) mouseX, (double) mouseY);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static Style getDrawnTextHoveredStyle(MinecraftClient client, int mouseX, int mouseY) {
        try {
            Class<?> consumerClass = forName("net.minecraft.client.font.DrawnTextConsumer", "net.minecraft.class_12225");
            Class<?> clickHandlerClass = forName("net.minecraft.client.font.DrawnTextConsumer$ClickHandler", "net.minecraft.class_12225$class_12226");
            Class<?> textRendererClass = forName("net.minecraft.client.font.TextRenderer", "net.minecraft.class_327");
            Constructor<?> constructor = clickHandlerClass.getDeclaredConstructor(textRendererClass, int.class, int.class);
            constructor.setAccessible(true);
            Object clickHandler = constructor.newInstance(client.textRenderer, mouseX, mouseY);

            Method insertMethod = getDeclaredMethod(clickHandlerClass, new String[]{"insert", "method_76756"}, boolean.class);
            insertMethod.setAccessible(true);
            clickHandler = insertMethod.invoke(clickHandler, client.isShiftPressed());

            Method renderMethod = getDeclaredMethod(client.inGameHud.getChatHud().getClass(), new String[]{"render", "method_75803"}, consumerClass, int.class, int.class, boolean.class);
            renderMethod.setAccessible(true);
            renderMethod.invoke(client.inGameHud.getChatHud(), clickHandler, client.getWindow().getScaledHeight(), client.inGameHud.getTicks(), true);

            Method getStyleMethod = getDeclaredMethod(clickHandlerClass, new String[]{"getStyle", "method_75777"});
            getStyleMethod.setAccessible(true);
            return (Style) getStyleMethod.invoke(clickHandler);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static Class<?> forName(String namedClass, String intermediaryClass) throws ClassNotFoundException {
        try {
            return Class.forName(namedClass);
        } catch (ClassNotFoundException exception) {
            return Class.forName(intermediaryClass);
        }
    }

    private static Method getDeclaredMethod(Class<?> owner, String[] names, Class<?>... parameterTypes) throws NoSuchMethodException {
        NoSuchMethodException lastException = null;
        for (String name : names) {
            try {
                return owner.getDeclaredMethod(name, parameterTypes);
            } catch (NoSuchMethodException exception) {
                lastException = exception;
            }
        }
        throw lastException == null ? new NoSuchMethodException(owner.getName()) : lastException;
    }

    private static String getImageUrl(Style hoveredStyle) {
        if (hoveredStyle != null && hoveredStyle.getInsertion() != null && hoveredStyle.getInsertion().startsWith(IMAGE_PREVIEW_INSERTION)) {
            return hoveredStyle.getInsertion().substring(IMAGE_PREVIEW_INSERTION.length());
        }

        if (hoveredStyle == null || !(hoveredStyle.getClickEvent() instanceof ClickEvent.OpenUrl openUrl)) {
            return null;
        }

        String imageUrl = openUrl.uri().toString();
        return PREVIEWABLE_URLS.contains(imageUrl) ? imageUrl : null;
    }
}
