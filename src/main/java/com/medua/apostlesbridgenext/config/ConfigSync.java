package com.medua.apostlesbridgenext.config;

public class ConfigSync {

    private static final String[] COLORS = {
            "§0","§1","§2","§3","§4","§5","§6","§7",
            "§8","§9","§a","§b","§c","§d","§e","§f"
    };

    private static final String[] GUILDS = {
            "Apostles",
            "Apostles Prime",
            "Apostles Lite"
    };

    public static void syncFromJson() {

        MoulBridgeConfig cfg = MoulBridgeConfig.CONFIG;

        cfg.general.url = Config.getURL();
        cfg.general.token = Config.getToken();

        String guild = Config.getGuild();
        for (int i = 0; i < GUILDS.length; i++) {
            if (GUILDS[i].equalsIgnoreCase(guild)) {
                cfg.general.guild = i;
                break;
            }
        }

        cfg.general.generalMode = Config.getGeneralMode();

        cfg.formatting.colors.originColor = colorIndex(Config.getFormattingColors().getOriginColor());
        cfg.formatting.colors.userColor = colorIndex(Config.getFormattingColors().getUserColor());
        cfg.formatting.colors.messageColor = colorIndex(Config.getFormattingColors().getMessageColor());

        cfg.formatting.prefixes.bridge = Config.getFormattingNames().getBridge();
        cfg.formatting.prefixes.discord = Config.getFormattingNames().getDiscord();
        cfg.formatting.prefixes.g1 = Config.getFormattingNames().getG1();
        cfg.formatting.prefixes.g2 = Config.getFormattingNames().getG2();
        cfg.formatting.prefixes.g3 = Config.getFormattingNames().getG3();
    }

    public static void syncToJson() {

        MoulBridgeConfig cfg = MoulBridgeConfig.CONFIG;

        Config.setURL(cfg.general.url);
        Config.setToken(cfg.general.token);

        Config.setGuild(GUILDS[cfg.general.guild]);
        Config.setGeneralMode(cfg.general.generalMode);

        Config.getFormattingColors().setOriginColor(COLORS[cfg.formatting.colors.originColor]);
        Config.getFormattingColors().setUserColor(COLORS[cfg.formatting.colors.userColor]);
        Config.getFormattingColors().setMessageColor(COLORS[cfg.formatting.colors.messageColor]);

        Config.getFormattingNames().setBridge(cfg.formatting.prefixes.bridge);
        Config.getFormattingNames().setDiscord(cfg.formatting.prefixes.discord);
        Config.getFormattingNames().setG1(cfg.formatting.prefixes.g1);
        Config.getFormattingNames().setG2(cfg.formatting.prefixes.g2);
        Config.getFormattingNames().setG3(cfg.formatting.prefixes.g3);

        Config.saveConfig();
    }

    private static int colorIndex(String color) {
        for (int i = 0; i < COLORS.length; i++) {
            if (COLORS[i].equalsIgnoreCase(color)) {
                return i;
            }
        }
        return 15;
    }
}