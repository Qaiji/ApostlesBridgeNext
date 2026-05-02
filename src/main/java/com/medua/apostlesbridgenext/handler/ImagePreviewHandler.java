package com.medua.apostlesbridgenext.handler;

import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.util.ImagePreview;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
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
        Style legacyStyle = getLegacyHoveredStyle(client, mouseX, mouseY);
        if (legacyStyle != null) {
            return legacyStyle;
        }

        return getDrawnTextHoveredStyle(client, mouseX, mouseY);
    }

    private static Style getLegacyHoveredStyle(MinecraftClient client, int mouseX, int mouseY) {
        try {
            Object chatHud = client.inGameHud.getChatHud();
            Method method = chatHud.getClass().getMethod(mapMethod(
                    "net.minecraft.client.gui.hud.ChatHud",
                    "getTextStyleAt",
                    "(DD)Lnet/minecraft/text/Style;"
            ), double.class, double.class);
            return (Style) method.invoke(chatHud, (double) mouseX, (double) mouseY);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static Style getDrawnTextHoveredStyle(MinecraftClient client, int mouseX, int mouseY) {
        try {
            Class<?> consumerClass = Class.forName(mapClass("net.minecraft.client.font.DrawnTextConsumer"));
            Class<?> clickHandlerClass = Class.forName(mapClass("net.minecraft.client.font.DrawnTextConsumer$ClickHandler"));
            Class<?> textRendererClass = Class.forName(mapClass("net.minecraft.client.font.TextRenderer"));
            Constructor<?> constructor = clickHandlerClass.getConstructor(textRendererClass, int.class, int.class);
            Object clickHandler = constructor.newInstance(client.textRenderer, mouseX, mouseY);
            Method insertMethod = clickHandlerClass.getMethod(mapMethod(
                    "net.minecraft.client.font.DrawnTextConsumer$ClickHandler",
                    "insert",
                    "(Z)Lnet/minecraft/client/font/DrawnTextConsumer$ClickHandler;"
            ), boolean.class);
            clickHandler = insertMethod.invoke(clickHandler, client.isShiftPressed());

            Method renderMethod = client.inGameHud.getChatHud().getClass().getMethod(mapMethod(
                    "net.minecraft.client.gui.hud.ChatHud",
                    "render",
                    "(Lnet/minecraft/client/font/DrawnTextConsumer;IIZ)V"
            ), consumerClass, int.class, int.class, boolean.class);
            renderMethod.invoke(client.inGameHud.getChatHud(), clickHandler, client.getWindow().getScaledHeight(), client.inGameHud.getTicks(), true);

            Method getStyleMethod = clickHandlerClass.getMethod(mapMethod(
                    "net.minecraft.client.font.DrawnTextConsumer$ClickHandler",
                    "getStyle",
                    "()Lnet/minecraft/text/Style;"
            ));
            return (Style) getStyleMethod.invoke(clickHandler);
        } catch (ReflectiveOperationException | RuntimeException exception) {
            return null;
        }
    }

    private static String mapClass(String namedClass) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        return resolver.mapClassName("named", namedClass);
    }

    private static String mapMethod(String owner, String name, String descriptor) {
        MappingResolver resolver = FabricLoader.getInstance().getMappingResolver();
        return resolver.mapMethodName("named", owner, name, descriptor);
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
