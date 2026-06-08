package com.medua.apostlesbridgenext.events;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.config.ConfigSync;
import com.medua.apostlesbridgenext.handler.MessageHandler;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

public class GuildToggleEvent {

    public static void register(ApostlesBridgeNextClient apostlesBridge) {
        ClientSendMessageEvents.COMMAND.register(command -> {
            String normalized = command.trim().toLowerCase();
            if (!normalized.equals("g toggle") && !normalized.equals("guild toggle")) {
                return;
            }

            Config.toggleBridgeChat();
            Config.saveConfig();
            ConfigSync.syncFromJson();
            apostlesBridge.getWebSocketHandler().restartWebSocket();

            if (Config.getGeneralMode() != 0) {
                MessageHandler.sendMessage("Bridge chat toggled §aON §7(" + Config.getGeneralModeText() + ")");
            } else {
                MessageHandler.sendMessage("Bridge chat toggled §cOFF");
            }
        });
    }
}
