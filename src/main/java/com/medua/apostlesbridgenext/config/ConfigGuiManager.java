package com.medua.apostlesbridgenext.config;

import io.github.notenoughupdates.moulconfig.gui.GuiContext;
import io.github.notenoughupdates.moulconfig.gui.GuiElementComponent;
import io.github.notenoughupdates.moulconfig.gui.MoulConfigEditor;
import io.github.notenoughupdates.moulconfig.platform.MoulConfigScreenComponent;
import net.minecraft.text.Text;

public class ConfigGuiManager {

    private static MoulConfigEditor<MoulBridgeConfig> editor = null;

    public static MoulConfigEditor<MoulBridgeConfig> getEditorInstance() {
        if (editor == null) {
            editor = new MoulConfigEditor<>(MoulBridgeConfig.processor);
        }
        return editor;
    }

    public static MoulConfigScreenComponent openConfigGui() {

        // Sync JSON → MoulConfig before opening
        ConfigSync.syncFromJson();

        MoulConfigEditor<MoulBridgeConfig> editor = getEditorInstance();

        MoulConfigScreenComponent screen = new MoulConfigScreenComponent(
                Text.literal("ApostlesBridgeNext Config"),
                new GuiContext(new GuiElementComponent(editor)),
                null
        ) {
            @Override
            public void removed() {
                super.removed();
                MoulBridgeConfig.CONFIG.saveNow();
            }
        };

        return screen;
    }
}