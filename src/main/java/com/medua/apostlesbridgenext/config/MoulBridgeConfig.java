package com.medua.apostlesbridgenext.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver;
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor;

public class MoulBridgeConfig extends Config {

    public static final MoulBridgeConfig CONFIG = new MoulBridgeConfig();
    public static final MoulConfigProcessor<MoulBridgeConfig> processor;

    static {
        processor = MoulConfigProcessor.withDefaults(CONFIG);
        ConfigProcessorDriver driver = new ConfigProcessorDriver(processor);
        driver.processConfig(CONFIG);
    }

    @Expose
    @Category(name = "General", desc = "General Settings")
    public General general = new General();

    @Expose
    @Category(name = "Formatting", desc = "Formatting Settings")
    public Formatting formatting = new Formatting();

    public static class General {

        @Expose
        @ConfigOption(name = "WebSocket URL", desc = "URL used to connect to the websocket")
        @ConfigEditorText
        public String url = "";

        @Expose
        @ConfigOption(name = "WebSocket Token", desc = "Authentication token obtained from /token")
        @ConfigEditorText
        public String token = "";

        @Expose
        @ConfigOption(name = "Guild", desc = "Your guild")
        @ConfigEditorDropdown(values = {
                "Apostles",
                "Apostles Prime",
                "Apostles Lite"
        })
        public int guild = 0;

        @Expose
        @ConfigOption(name = "Mode", desc = "Where the bridge operates")
        @ConfigEditorDropdown(values = {
                "OFF",
                "EVERYWHERE",
                "HYPIXEL ONLY"
        })
        public int generalMode = 1;
    }

    public static class Formatting {

        @Expose
        @ConfigOption(name = "Origin Color", desc = "Color for origin messages")
        @ConfigEditorDropdown(values = {
                "§0Black","§1Dark Blue","§2Dark Green","§3Dark Aqua",
                "§4Dark Red","§5Dark Purple","§6Gold","§7Gray",
                "§8Dark Gray","§9Blue","§aGreen","§bAqua",
                "§cRed","§dLight Purple","§eYellow","§fWhite"
        })
        public int originColor = 2;

        @Expose
        @ConfigOption(name = "User Color", desc = "Color for usernames")
        @ConfigEditorDropdown(values = {
                "§0Black","§1Dark Blue","§2Dark Green","§3Dark Aqua",
                "§4Dark Red","§5Dark Purple","§6Gold","§7Gray",
                "§8Dark Gray","§9Blue","§aGreen","§bAqua",
                "§cRed","§dLight Purple","§eYellow","§fWhite"
        })
        public int userColor = 11;

        @Expose
        @ConfigOption(name = "Message Color", desc = "Color for messages")
        @ConfigEditorDropdown(values = {
                "§0Black","§1Dark Blue","§2Dark Green","§3Dark Aqua",
                "§4Dark Red","§5Dark Purple","§6Gold","§7Gray",
                "§8Dark Gray","§9Blue","§aGreen","§bAqua",
                "§cRed","§dLight Purple","§eYellow","§fWhite"
        })
        public int messageColor = 15;

        @Expose
        @ConfigOption(name = "Bridge Name", desc = "Bridge prefix in chat")
        @ConfigEditorText
        public String bridge = "Bridge";

        @Expose
        @ConfigOption(name = "Discord Name", desc = "Discord prefix in chat")
        @ConfigEditorText
        public String discord = "Discord";

        @Expose
        @ConfigOption(name = "Guild 1 Name", desc = "Guild 1 prefix")
        @ConfigEditorText
        public String g1 = "Guild 1";

        @Expose
        @ConfigOption(name = "Guild 2 Name", desc = "Guild 2 prefix")
        @ConfigEditorText
        public String g2 = "Guild 2";

        @Expose
        @ConfigOption(name = "Guild 3 Name", desc = "Guild 3 prefix")
        @ConfigEditorText
        public String g3 = "Guild 3";
    }

    @Override
    public StructuredText getTitle() {
        return StructuredText.of("ApostlesBridgeNext Config");
    }

    @Override
    public void saveNow() {
        ConfigSync.syncToJson();
    }
}