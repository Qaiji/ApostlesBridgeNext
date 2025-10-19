package com.medua.apostlesbridgenext.handler;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
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
        return (brackets ? "§r[" : "") + (colors ? "§5" : "§r") + MOD_PREFIX + "§r" + (brackets ? "] " : " > ");
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

    public static void sendMessageWithImages(String message, boolean prefix, List<String> imageURLs) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.isOnThread()) {
            client.execute(() -> sendMessageWithImages(message, prefix, imageURLs));
            return;
        }
        Text messageComponent = getTextForMessage(message, prefix);

        if (!imageURLs.isEmpty()) {
            messageComponent = messageComponent.copy().append(" ");

            int imageCount = 1;
            for (String imageURL : imageURLs) {
                if (imageCount > 1) {
                    messageComponent = messageComponent.copy().append(", ");
                }
                Text imageComponent = Text.literal("§r[§dIMAGE_" + imageCount++ + "§r]")
                        .styled(style -> style
                                .withClickEvent(new ClickEvent.OpenUrl(URI.create(imageURL)))
                                .withHoverEvent(new HoverEvent.ShowText(Text.literal("Click to open the image")))
                        );

                messageComponent = messageComponent.copy().append(imageComponent);
            }
        }

        sendMessage(messageComponent);
    }

    private static Text getTextForMessage(String message) {
        return getTextForMessage(message, true);
    }

    private static Text getTextForMessage(String message, boolean prefix) {
        return Text.literal((prefix ? getPrefix() : "") + "§r" + message);
    }

    public static void sendSpacerMessage() {
        sendMessage("=====================================================", false);
    }
}
