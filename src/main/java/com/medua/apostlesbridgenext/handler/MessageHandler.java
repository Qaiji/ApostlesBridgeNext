package com.medua.apostlesbridgenext.handler;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.types.LinkPreviewType;
import com.medua.apostlesbridgenext.util.EmojiUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;

import java.net.URI;
import java.util.List;

public class MessageHandler {
    public static final String MOD_PREFIX = "AB";

    ApostlesBridgeNextClient apostlesBridge;
    public MessageHandler(ApostlesBridgeNextClient apostlesBridge) {
        this.apostlesBridge = apostlesBridge;
    }

    public static String getPrefix() {
        return getPrefix(true, false);
    }

    public static String getPrefix(boolean colors, boolean brackets) {
        return (brackets ? "\u00A7r[" : "") + (colors ? "\u00A75" : "\u00A7r") + MOD_PREFIX + "\u00A7r" + (brackets ? "] " : " > ");
    }

    public static void sendMessage(Text message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread()) {
            client.execute(() -> sendMessage(message));
            return;
        }

        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(message);
    }

    public static void sendMessage(String message) {
        sendMessage(getTextForMessage(message));
    }

    public static void sendMessage(String message, boolean prefix) {
        sendMessage(getTextForMessage(message, prefix));
    }

    public static void sendMessageWithLinks(String message, boolean prefix, List<String> urls) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread()) {
            client.execute(() -> sendMessageWithLinks(message, prefix, urls));
            return;
        }
        Text messageComponent = getTextForMessage(message, prefix);

        if (!urls.isEmpty()) {
            messageComponent = messageComponent.copy().append(" ");

            int imageCount = 1;
            int youtubeCount = 1;
            int twitchCount = 1;
            int linkCount = 0;
            for (String url : urls) {
                if (linkCount++ > 0) {
                    messageComponent = messageComponent.copy().append(", ");
                }

                LinkPreviewType previewType = LinkPreviewType.fromUrl(url);
                String label;
                String hoverText;
                if (previewType == LinkPreviewType.YOUTUBE) {
                    label = "\u00A7r[\u00A7cYOUTUBE_" + youtubeCount++ + "\u00A7r]";
                    hoverText = "Click to open the YouTube video";
                } else if (previewType == LinkPreviewType.TWITCH) {
                    label = "\u00A7r[\u00A75TWITCH_" + twitchCount++ + "\u00A7r]";
                    hoverText = "Click to open the Twitch stream";
                } else {
                    ImagePreviewHandler.registerImageUrl(url);
                    label = "\u00A7r[\u00A7dIMAGE_" + imageCount++ + "\u00A7r]";
                    hoverText = "Click to open the image";
                }

                LinkPreviewType finalPreviewType = previewType;
                Text imageComponent = Text.literal(label)
                        .styled(style -> {
                            style = style
                                    .withClickEvent(new ClickEvent.OpenUrl(URI.create(url)))
                                    .withHoverEvent(new HoverEvent.ShowText(Text.literal(hoverText)));
                            if (finalPreviewType == LinkPreviewType.IMAGE) {
                                style = style.withInsertion(ImagePreviewHandler.IMAGE_PREVIEW_INSERTION + url);
                            }
                            return style;
                        });

                messageComponent = messageComponent.copy().append(imageComponent);
            }
        }

        sendMessage(messageComponent);
    }

    private static Text getTextForMessage(String message) {
        return getTextForMessage(message, true);
    }

    private static Text getTextForMessage(String message, boolean prefix) {
        Text baseMessage = Text.literal((prefix ? getPrefix() : "") + "\u00A7r");
        if (Config.isEmojiConversionEnabled()) {
            return baseMessage.copy().append(EmojiUtil.replaceShortcodesWithFont(message));
        }
        return baseMessage.copy().append(message);
    }

    public static void sendSpacerMessage() {
        sendMessage("=====================================================", false);
    }

}
