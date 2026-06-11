package com.medua.apostlesbridgenext.events;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.config.ConfigSync;
import com.medua.apostlesbridgenext.handler.RespectGuildToggleMessages;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.network.chat.Component;

public class GuildChatToggleEvent {
    private static final String DISABLED_GUILD_CHAT = "Disabled guild chat!";
    private static final String ENABLED_GUILD_CHAT = "Enabled guild chat!";

    public static void register(ApostlesBridgeNextClient apostlesBridge) {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            Boolean enabled = matchGuildChatToggle(message);
            if (enabled == null) {
                return;
            }

            Config.setGuildChatEnabled(enabled);
            Config.saveConfig();
            ConfigSync.syncFromJson();

            if (Config.getGeneralMode() == 0 || !Config.isRespectGuildChatToggleEnabled()) {
                return;
            }

            if (enabled) {
                if (apostlesBridge.getWebSocketHandler().canReconnectAfterGuildChatEnabled()) {
                    RespectGuildToggleMessages.sendSettingMessage("Reconnecting to WebSocket because ", " is enabled...");
                    apostlesBridge.getWebSocketHandler().reconnectAfterGuildChatEnabled();
                } else {
                    apostlesBridge.getWebSocketHandler().restartWebSocket();
                }
                return;
            }

            RespectGuildToggleMessages.sendSettingMessage(apostlesBridge.getWebSocketHandler().isConnected() ? "WebSocket disconnected because " : "WebSocket remains paused because ", " is enabled.");
            apostlesBridge.getWebSocketHandler().restartWebSocket();
        });
    }

    private static Boolean matchGuildChatToggle(Component message) {
        String text = message.getString();
        if (text.equals(DISABLED_GUILD_CHAT)) {
            return false;
        }
        if (text.equals(ENABLED_GUILD_CHAT)) {
            return true;
        }
        return null;
    }
}
