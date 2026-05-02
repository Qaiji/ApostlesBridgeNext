package com.medua.apostlesbridgenext.config;

import com.google.gson.annotations.Expose;
import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.util.ColorUtil;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.Social;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver;
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor;

import java.util.List;

public class MoulBridgeConfig extends Config {
    private static final MyResourceLocation DISCORD =
            new MyResourceLocation("apostlesbridgenext", "discord.png");

    private static final MyResourceLocation GITHUB =
            new MyResourceLocation("apostlesbridgenext", "github.png");

    private static final MyResourceLocation FORUMS =
            new MyResourceLocation("apostlesbridgenext", "hypixel.png");

    public static final MoulBridgeConfig CONFIG = new MoulBridgeConfig();
    public static final MoulConfigProcessor<MoulBridgeConfig> processor;

    static {
        processor = MoulConfigProcessor.withDefaults(CONFIG);
        processor.registerConfigEditor(ConfigEditorMessagePreview.class, (option, ignored) -> new MessagePreviewEditor(option));
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

        @Expose
        @ConfigOption(name = "Image Preview Size", desc = "Default size for hovered image previews")
        @ConfigEditorDropdown(values = {
                "XS",
                "S",
                "M",
                "L"
        })
        public int imagePreviewSize = 2;
    }

    public static class Formatting {
        @Expose
        @ConfigOption(name = "Message Preview", desc = "Preview of the current chat formatting")
        @ConfigEditorMessagePreview
        public boolean messagePreview = true;

        @Expose
        @Accordion
        @ConfigOption(name = "Colors", desc = "Colors used in the chat message")
        public Colors colors = new Colors();

        @Expose
        @Accordion
        @ConfigOption(name = "Prefixes", desc = "Names used as message origins")
        public Prefixes prefixes = new Prefixes();

    }

    public static class Colors {
        @Expose
        @ConfigOption(name = "Origin Color", desc = "Color for origin messages")
        @ConfigEditorDropdown
        public ColorUtil.MinecraftColor originColor = ColorUtil.MinecraftColor.DARK_GREEN;

        @Expose
        @ConfigOption(name = "User Color", desc = "Color for usernames")
        @ConfigEditorDropdown
        public ColorUtil.MinecraftColor userColor = ColorUtil.MinecraftColor.AQUA;

        @Expose
        @ConfigOption(name = "Message Color", desc = "Color for messages")
        @ConfigEditorDropdown
        public ColorUtil.MinecraftColor messageColor = ColorUtil.MinecraftColor.WHITE;
    }

    public static class Prefixes {
        @Expose
        @ConfigOption(name = "Bridge Name", desc = "Bridge prefix in chat")
        @ConfigEditorText
        public String bridge = "Bridge";

        @Expose
        @ConfigOption(name = "Discord Name", desc = "Discord prefix in chat")
        @ConfigEditorText
        public String discord = "Discord";

        @Expose
        @ConfigOption(name = "Guild 1 Name", desc = "Guild 1 prefix in chat")
        @ConfigEditorText
        public String g1 = "Guild 1";

        @Expose
        @ConfigOption(name = "Guild 2 Name", desc = "Guild 2 prefix in chat")
        @ConfigEditorText
        public String g2 = "Guild 2";

        @Expose
        @ConfigOption(name = "Guild 3 Name", desc = "Guild 3 prefix in chat")
        @ConfigEditorText
        public String g3 = "Guild 3";
    }

    @Override
    public StructuredText getTitle() {
        return StructuredText.of("ApostlesBridgeNext v" + ApostlesBridgeNextClient.VERSION + " by ")
                .append(StructuredText.of("Medua").darkPurple())
                .append(" & ")
                .append(StructuredText.of("IcyRetro").darkPurple());
    }

    @Override
    public List<Social> getSocials() {
        return List.of(
                Social.forLink(
                        StructuredText.of("GitHub"),
                        GITHUB,
                        "https://github.com/Qaiji/ApostlesBridgeNext"
                ),
                Social.forLink(
                        StructuredText.of("Discord"),
                        DISCORD,
                        "https://discord.gg/76BwVqhK2H"
                ),
                Social.forLink(
                        StructuredText.of("Hypixel Forums"),
                        FORUMS,
                        "https://hypixel.net/threads/apostles-1-skyblock-guild-level-450-community-focused.5565942/"
                )
        );
    }

    @Override
    public void saveNow() {
        ConfigSync.syncToJson();
    }
}
