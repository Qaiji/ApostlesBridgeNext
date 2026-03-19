package com.medua.apostlesbridgenext.config;

import com.google.gson.annotations.Expose;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.Social;
import io.github.notenoughupdates.moulconfig.annotations.*;
import io.github.notenoughupdates.moulconfig.common.MyResourceLocation;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText;
import io.github.notenoughupdates.moulconfig.processor.ConfigProcessorDriver;
import io.github.notenoughupdates.moulconfig.processor.MoulConfigProcessor;
import java.util.List;

import static com.medua.apostlesbridgenext.handler.MessageHandler.sendMessage;
import static com.medua.apostlesbridgenext.handler.MessageHandler.sendSpacerMessage;

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
        @Accordion
        @ConfigOption(name = "Colors", desc = "Colors used in the chat message")
        public Colors colors = new Colors();

        @Expose
        @Accordion
        @ConfigOption(name = "Prefixes", desc = "Names used as message origins")
        public Prefixes prefixes = new Prefixes();

        @Expose
        @ConfigOption(
                name = "Send Preview Messages",
                desc = "Send all preview messages in chat"
        )
        @ConfigEditorButton(buttonText = "Send")
        public Runnable sendPreview = this::sendPreviewMessages;


        private void sendPreviewMessages() {
            Formatting cfg = MoulBridgeConfig.CONFIG.formatting;
            sendSpacerMessage();

            String origin = color(cfg.colors.originColor);
            String user = color(cfg.colors.userColor);
            String msg = color(cfg.colors.messageColor);

            String chatMessage = origin + cfg.prefixes.bridge + " > "
                    + user + "IcyRetro" + ": " + msg + "this is a preview of a command";

            sendMessage(chatMessage, false);

            chatMessage = origin + cfg.prefixes.discord + " > "
                    + user + "IcyRetro" + ": " + msg + "this is a preview of a message from bridge";

            sendMessage(chatMessage, false);

            chatMessage = origin + cfg.prefixes.g1 + " > "
                    + user + "IcyRetro" + ": " + msg + "this is a preview of a message from guild 1";

            sendMessage(chatMessage, false);

            chatMessage = origin + cfg.prefixes.g2 + " > "
                    + user + "IcyRetro" + ": " + msg + "this is a preview of a message from guild 2";

            sendMessage(chatMessage, false);

            chatMessage = origin + cfg.prefixes.g3 + " > "
                    + user + "IcyRetro" + ": " + msg + "this is a preview of a message from guild 3";

            sendMessage(chatMessage, false);
            sendSpacerMessage();

        }

        private static String color(int i) {
            return "§" + "0123456789abcdef".charAt(i);
        }
    }

    public static class Colors {
        @Expose
        @ConfigOption(name = "Origin Color", desc = "Color for origin messages")
        @ConfigEditorDropdown(values = {
                "§0Black","§1Dark Blue","§2Dark Green","§3Dark Aqua",
                "§4Dark Red","§5Dark Purple","§6Gold","§7Gray",
                "§8Dark Gray","§9Blue","§aGreen","§bAqua",
                "§cRed","§dLight Purple","§eYellow","§fWhite"
        })
        public Integer originColor = 2;

        @Expose
        @ConfigOption(name = "User Color", desc = "Color for usernames")
        @ConfigEditorDropdown(values = {
                "§0Black","§1Dark Blue","§2Dark Green","§3Dark Aqua",
                "§4Dark Red","§5Dark Purple","§6Gold","§7Gray",
                "§8Dark Gray","§9Blue","§aGreen","§bAqua",
                "§cRed","§dLight Purple","§eYellow","§fWhite"
        })
        public Integer userColor = 11;

        @Expose
        @ConfigOption(name = "Message Color", desc = "Color for messages")
        @ConfigEditorDropdown(values = {
                "§0Black","§1Dark Blue","§2Dark Green","§3Dark Aqua",
                "§4Dark Red","§5Dark Purple","§6Gold","§7Gray",
                "§8Dark Gray","§9Blue","§aGreen","§bAqua",
                "§cRed","§dLight Purple","§eYellow","§fWhite"
        })
        public Integer messageColor = 15;
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
        return StructuredText.of("ApostlesBridgeNext Config");
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