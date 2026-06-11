package com.medua.apostlesbridgenext.config;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import net.minecraft.network.chat.Component;

public class ConfigGuiManager {
    private static MoulConfigEditor<MoulBridgeConfig> editor = null;

    public static MoulConfigEditor<MoulBridgeConfig> getEditorInstance() {
        if (editor == null) {
            editor = new MoulConfigEditor<>(MoulBridgeConfig.processor);
        }
        return editor;
    }

    public static MoulConfigScreenComponent openConfigGui(ApostlesBridgeNextClient apostlesBridge) {
        ConfigSync.syncFromJson();
        int previousGeneralMode = Config.getGeneralMode();
        boolean wasBlockedByGuildChatToggle = Config.isBlockedByGuildChatToggle();
        boolean previousRespectGuildChatToggle = Config.isRespectGuildChatToggleEnabled();
        String previousUrl = Config.getURL();
        String previousToken = Config.getToken();

        MoulConfigEditor<MoulBridgeConfig> editor = getEditorInstance();

        MoulConfigScreenComponent screen = new MoulConfigScreenComponent(
            Component.literal("ApostlesBridgeNext Config"),
            new GuiContext(new GuiElementComponent(editor)),
            null
        ) {
            @Override
            public void removed() {
                super.removed();
                MoulBridgeConfig.CONFIG.saveNow();
                boolean connectionSettingsChanged = previousGeneralMode != Config.getGeneralMode()
                    || previousRespectGuildChatToggle != Config.isRespectGuildChatToggleEnabled()
                    || !previousUrl.equals(Config.getURL())
                    || !previousToken.equals(Config.getToken());
                apostlesBridge.getWebSocketHandler().handleConfigSaved(
                    previousGeneralMode,
                    wasBlockedByGuildChatToggle,
                    connectionSettingsChanged
                );
            }
        };

        return screen;
    }
}
