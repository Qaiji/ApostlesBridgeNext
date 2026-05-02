package com.medua.apostlesbridgenext.config;

import com.medua.apostlesbridgenext.util.ColorUtil;

public class ConfigSync {

    private static final String[] GUILDS = {
            "Apostles",
            "Apostles Prime",
            "Apostles Lite"
    };

    public static void syncFromJson() {
        MoulBridgeConfig moulConfig = MoulBridgeConfig.CONFIG;

        moulConfig.general.url = Config.getURL();
        moulConfig.general.token = Config.getToken();

        String guild = Config.getGuild();
        for (int i = 0; i < GUILDS.length; i++) {
            if (GUILDS[i].equalsIgnoreCase(guild)) {
                moulConfig.general.guild = i;
                break;
            }
        }

        moulConfig.general.generalMode = Config.getGeneralMode();
        moulConfig.general.imagePreviewSize = Config.getImagePreviewSize().ordinal();
        moulConfig.formatting.emojiConversionEnabled = Config.isEmojiConversionEnabled();

        moulConfig.formatting.colors.originColor = ColorUtil.minecraftColor(Config.getFormattingColors().getOriginColor());
        moulConfig.formatting.colors.userColor = ColorUtil.minecraftColor(Config.getFormattingColors().getUserColor());
        moulConfig.formatting.colors.messageColor = ColorUtil.minecraftColor(Config.getFormattingColors().getMessageColor());

        moulConfig.formatting.prefixes.bridge = Config.getFormattingNames().getBridge();
        moulConfig.formatting.prefixes.discord = Config.getFormattingNames().getDiscord();
        moulConfig.formatting.prefixes.g1 = Config.getFormattingNames().getG1();
        moulConfig.formatting.prefixes.g2 = Config.getFormattingNames().getG2();
        moulConfig.formatting.prefixes.g3 = Config.getFormattingNames().getG3();
    }

    public static void syncToJson() {
        MoulBridgeConfig moulConfig = MoulBridgeConfig.CONFIG;

        Config.setURL(moulConfig.general.url);
        Config.setToken(moulConfig.general.token);

        Config.setGuild(GUILDS[moulConfig.general.guild]);
        Config.setGeneralMode(moulConfig.general.generalMode);
        Config.setImagePreviewSize(moulConfig.general.imagePreviewSize);
        Config.setEmojiConversionEnabled(moulConfig.formatting.emojiConversionEnabled);

        Config.getFormattingColors().setOriginColor(ColorUtil.minecraftCode(moulConfig.formatting.colors.originColor));
        Config.getFormattingColors().setUserColor(ColorUtil.minecraftCode(moulConfig.formatting.colors.userColor));
        Config.getFormattingColors().setMessageColor(ColorUtil.minecraftCode(moulConfig.formatting.colors.messageColor));

        Config.getFormattingNames().setBridge(moulConfig.formatting.prefixes.bridge);
        Config.getFormattingNames().setDiscord(moulConfig.formatting.prefixes.discord);
        Config.getFormattingNames().setG1(moulConfig.formatting.prefixes.g1);
        Config.getFormattingNames().setG2(moulConfig.formatting.prefixes.g2);
        Config.getFormattingNames().setG3(moulConfig.formatting.prefixes.g3);

        Config.saveConfig();
    }
}
