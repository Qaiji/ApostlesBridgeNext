package com.medua.apostlesbridgenext.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;

public final class RespectGuildToggleMessages {
    private RespectGuildToggleMessages() { }

    public static void sendSettingMessage(String beforeSetting, String afterSetting) {
        Component message = Component.literal(MessageHandler.getPrefix(true, true))
            .append(Component.literal(beforeSetting))
            .append(settingText())
            .append(Component.literal(afterSetting));
        MessageHandler.sendMessage(message);
    }

    private static Component settingText() {
        return Component.literal("Respect /g toggle").withStyle(style -> style.withColor(0xAA00AA)
            .withHoverEvent(new HoverEvent.ShowText(Component.literal("Pauses the bridge when Hypixel guild chat is disabled via /g toggle.\nYou can change this in /bridge settings."))));
    }
}
