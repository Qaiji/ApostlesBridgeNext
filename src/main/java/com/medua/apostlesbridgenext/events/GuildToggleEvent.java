package com.medua.apostlesbridgenext.events;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.config.ConfigSync;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.text.Text;

public class GuildToggleEvent {

    private static final String DISABLED_GUILD_CHAT = "Disabled guild chat!";
    private static final String ENABLED_GUILD_CHAT = "Enabled guild chat!";

    public static void register(ApostlesBridgeNextClient apostlesBridge) {
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            Boolean enabled = matchGuildChatToggle(message);
            if (enabled == null) {
                return;
            }

            Config.setBridgeChatEnabled(enabled);
            Config.saveConfig();
            ConfigSync.syncFromJson();
            apostlesBridge.getWebSocketHandler().restartWebSocket();
        });
    }

    private static Boolean matchGuildChatToggle(Text message) {
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
