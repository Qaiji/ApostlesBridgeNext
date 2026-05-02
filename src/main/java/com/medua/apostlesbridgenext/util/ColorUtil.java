package com.medua.apostlesbridgenext.util;

public final class ColorUtil {
    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int CONFIG_TITLE = 0xFFE0E0E0;
    public static final int PREVIEW_BACKGROUND = 0xDD101010;
    public static final int IMAGE_PREVIEW_BACKGROUND = 0xE0101010;
    public static final int DARK_PURPLE_BORDER = 0xFF4B1A78;
    public static final int DARK_PURPLE_BADGE = 0xD04B1A78;

    private ColorUtil() {
    }

    public static MinecraftColor minecraftColor(int index) {
        MinecraftColor[] colors = MinecraftColor.values();
        return colors[Math.max(0, Math.min(index, colors.length - 1))];
    }

    public static MinecraftColor minecraftColor(String colorCode) {
        String normalizedCode = colorCode == null ? "" : colorCode.replace("\u00C2\u00A7", "\u00A7");
        for (MinecraftColor color : MinecraftColor.values()) {
            if (color.code().equalsIgnoreCase(normalizedCode)) {
                return color;
            }
        }
        return MinecraftColor.WHITE;
    }

    public static String minecraftCode(MinecraftColor color) {
        return color == null ? MinecraftColor.WHITE.code() : color.code();
    }

    public enum MinecraftColor {
        BLACK("Black", "\u00A70", 0x000000, 0xFF000000),
        DARK_BLUE("Dark Blue", "\u00A71", 0x0000AA, 0xFF0000AA),
        DARK_GREEN("Dark Green", "\u00A72", 0x00AA00, 0xFF00AA00),
        DARK_AQUA("Dark Aqua", "\u00A73", 0x00AAAA, 0xFF00AAAA),
        DARK_RED("Dark Red", "\u00A74", 0xAA0000, 0xFFAA0000),
        DARK_PURPLE("Dark Purple", "\u00A75", 0xAA00AA, 0xFFAA00AA),
        GOLD("Gold", "\u00A76", 0xFFAA00, 0xFFFFAA00),
        GRAY("Gray", "\u00A77", 0xAAAAAA, 0xFFAAAAAA),
        DARK_GRAY("Dark Gray", "\u00A78", 0x555555, 0xFF555555),
        BLUE("Blue", "\u00A79", 0x5555FF, 0xFF5555FF),
        GREEN("Green", "\u00A7a", 0x55FF55, 0xFF55FF55),
        AQUA("Aqua", "\u00A7b", 0x55FFFF, 0xFF55FFFF),
        RED("Red", "\u00A7c", 0xFF5555, 0xFFFF5555),
        LIGHT_PURPLE("Light Purple", "\u00A7d", 0xFF55FF, 0xFFFF55FF),
        YELLOW("Yellow", "\u00A7e", 0xFFFF55, 0xFFFFFF55),
        WHITE("White", "\u00A7f", 0xFFFFFF, 0xFFFFFFFF);

        private final String displayName;
        private final String code;
        private final int rgb;
        private final int argb;

        MinecraftColor(String displayName, String code, int rgb, int argb) {
            this.displayName = displayName;
            this.code = code;
            this.rgb = rgb;
            this.argb = argb;
        }

        public String code() {
            return code;
        }

        public int rgb() {
            return rgb;
        }

        public int argb() {
            return argb;
        }

        @Override
        public String toString() {
            return code + displayName;
        }
    }
}
