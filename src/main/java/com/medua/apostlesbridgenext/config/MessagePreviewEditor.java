package com.medua.apostlesbridgenext.config;

import com.medua.apostlesbridgenext.util.ColorUtil;
import com.medua.apostlesbridgenext.util.EmojiUtil;
import io.github.notenoughupdates.moulconfig.common.IFontRenderer;
import io.github.notenoughupdates.moulconfig.common.RenderContext;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.gui.GuiOptionEditor;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigRenderContext;
import io.github.notenoughupdates.moulconfig.processor.ProcessedOption;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class MessagePreviewEditor extends GuiOptionEditor {
    private static final int HEIGHT = 126;

    public MessagePreviewEditor(ProcessedOption option) {
        super(option);
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void render(RenderContext context, int x, int y, int width) {
        IFontRenderer font = context.getMinecraft().getDefaultFontRenderer();
        MoulBridgeConfig.Formatting formatting = MoulBridgeConfig.CONFIG.formatting;

        context.pushMatrix();
        context.translate(x, y);

        context.drawDarkRect(0, 0, width, HEIGHT, true);
        context.drawStringCenteredScaledMaxWidth(option.getName(), font, width / 2F, 12, true, width - 10, ColorUtil.CONFIG_TITLE);

        int panelX = 10;
        int panelY = 28;
        int panelWidth = width - 20;
        int panelHeight = HEIGHT - 38;
        int panelRight = panelX + panelWidth;
        int panelBottom = panelY + panelHeight;
        context.drawColoredRect(panelX, panelY, panelRight, panelBottom, ColorUtil.PREVIEW_BACKGROUND);
        context.drawColoredRect(panelX, panelY, panelRight, panelY + 1, ColorUtil.DARK_PURPLE_BORDER);
        context.drawColoredRect(panelX, panelBottom - 1, panelRight, panelBottom, ColorUtil.DARK_PURPLE_BORDER);
        context.drawColoredRect(panelX, panelY, panelX + 1, panelBottom, ColorUtil.DARK_PURPLE_BORDER);
        context.drawColoredRect(panelRight - 1, panelY, panelRight, panelBottom, ColorUtil.DARK_PURPLE_BORDER);

        int lineX = panelX + 8;
        int lineY = panelY + 8;
        renderPreviewLine(context, font, lineX, lineY, formatting.prefixes.bridge, "IcyRetro", "preview for a command");
        renderPreviewLine(context, font, lineX, lineY + 13, formatting.prefixes.discord, "IcyRetro", "preview message from bridge");
        renderPreviewLine(context, font, lineX, lineY + 26, formatting.prefixes.g1, "IcyRetro", "preview message from guild 1");
        renderPreviewLine(context, font, lineX, lineY + 39, formatting.prefixes.g2, "IcyRetro", "preview message from guild 2");
        renderPreviewLine(context, font, lineX, lineY + 52, formatting.prefixes.g3, "IcyRetro", "preview message from guild 3");
        renderEmojiPreviewLine(context, lineX, lineY + 65, formatting.prefixes.discord, "Jaminul");

        context.popMatrix();
    }

    private static void renderPreviewLine(RenderContext context, IFontRenderer font, int x, int y, String origin, String user, String message) {
        MoulBridgeConfig.Formatting formatting = MoulBridgeConfig.CONFIG.formatting;
        int nextX = draw(context, font, x, y, origin, color(formatting.colors.originColor));
        nextX = draw(context, font, nextX, y, " > ", ColorUtil.TEXT_WHITE);
        nextX = draw(context, font, nextX, y, user, color(formatting.colors.userColor));
        nextX = draw(context, font, nextX, y, ": ", ColorUtil.TEXT_WHITE);
        draw(context, font, nextX, y, message, color(formatting.colors.messageColor));
    }

    private static int draw(RenderContext context, IFontRenderer font, int x, int y, String text, int color) {
        context.drawString(font, StructuredText.of(text), x, y, color, false);
        return x + font.getStringWidth(text);
    }

    private static void renderEmojiPreviewLine(RenderContext context, int x, int y, String origin, String user) {
        if (!(context instanceof MoulConfigRenderContext moulContext)) {
            renderPreviewLine(
                    context,
                    context.getMinecraft().getDefaultFontRenderer(),
                    x,
                    y,
                    origin,
                    user,
                    emojiPreviewMessage()
            );
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        moulContext.getDrawContext().drawText(
                client.textRenderer,
                emojiPreviewText(origin, user),
                x,
                y,
                ColorUtil.TEXT_WHITE,
                false
        );
    }

    private static MutableText emojiPreviewText(String origin, String user) {
        MoulBridgeConfig.Formatting formatting = MoulBridgeConfig.CONFIG.formatting;
        MutableText text = Text.literal(origin).styled(style -> style.withColor(rgb(formatting.colors.originColor)));
        text.append(Text.literal(" > ").styled(style -> style.withColor(rgb(ColorUtil.TEXT_WHITE))));
        text.append(Text.literal(user).styled(style -> style.withColor(rgb(formatting.colors.userColor))));
        text.append(Text.literal(": ").styled(style -> style.withColor(rgb(ColorUtil.TEXT_WHITE))));
        text.append(emojiPreviewMessageText().styled(style -> style.withColor(rgb(formatting.colors.messageColor))));
        return text;
    }

    private static MutableText emojiPreviewMessageText() {
        String message = emojiPreviewMessage();
        if (MoulBridgeConfig.CONFIG.formatting.emojiConversionEnabled) {
            return EmojiUtil.replaceShortcodesWithFont(message);
        }
        return Text.literal(message);
    }

    private static String emojiPreviewMessage() {
        return "emoji test :fire: :rocket:";
    }

    private static int rgb(ColorUtil.MinecraftColor color) {
        return color(color) & 0xFFFFFF;
    }

    private static int rgb(int color) {
        return color & 0xFFFFFF;
    }

    private static int color(ColorUtil.MinecraftColor color) {
        return color == null ? ColorUtil.MinecraftColor.WHITE.argb() : color.argb();
    }
}
