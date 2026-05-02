package com.medua.apostlesbridgenext.util;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.StyleSpriteSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class EmojiUtil {
    private static final Pattern SHORTCODE_PATTERN = Pattern.compile(":[a-zA-Z0-9_+\\-]+:");
    private static final Map<String, String> EMOJIS = new HashMap<>();
    private static final Map<String, String> FONT_GLYPHS = new HashMap<>();
    private static final StyleSpriteSource EMOJI_FONT = new StyleSpriteSource.Font(
            Identifier.of(ApostlesBridgeNextClient.MODID, "emoji")
    );

    static {
        // Emoji sprites are from Twemoji - 72x72 PNGs:
        // https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/{emoji-codepoint}.png
        add("thumbsup", codePoint(0x1F44D), 0xE000);
        add("+1", codePoint(0x1F44D), 0xE000);
        add("thumbsdown", codePoint(0x1F44E), 0xE001);
        add("-1", codePoint(0x1F44E), 0xE001);
        add("heart", codePoint(0x2764) + codePoint(0xFE0F), 0xE002);
        add("red_heart", codePoint(0x2764) + codePoint(0xFE0F), 0xE002);
        add("fire", codePoint(0x1F525), 0xE003);
        add("skull", codePoint(0x1F480), 0xE004);
        add("sob", codePoint(0x1F62D), 0xE005);
        add("joy", codePoint(0x1F602), 0xE006);
        add("eyes", codePoint(0x1F440), 0xE007);
        add("pray", codePoint(0x1F64F), 0xE008);
        add("warning", codePoint(0x26A0) + codePoint(0xFE0F), 0xE009);
        add("white_check_mark", codePoint(0x2705), 0xE00A);
        add("x", codePoint(0x274C), 0xE00B);
        add("clap", codePoint(0x1F44F), 0xE00C);
        add("ok_hand", codePoint(0x1F44C), 0xE00D);
        add("wave", codePoint(0x1F44B), 0xE00E);
        add("thinking", codePoint(0x1F914), 0xE00F);
        add("facepalm", codePoint(0x1F926), 0xE010);
        add("smile", codePoint(0x1F604), 0xE011);
        add("smiley", codePoint(0x1F603), 0xE012);
        add("grin", codePoint(0x1F601), 0xE013);
        add("laughing", codePoint(0x1F606), 0xE014);
        add("sweat_smile", codePoint(0x1F605), 0xE015);
        add("slight_smile", codePoint(0x1F642), 0xE016);
        add("wink", codePoint(0x1F609), 0xE017);
        add("blush", codePoint(0x1F60A), 0xE018);
        add("cry", codePoint(0x1F622), 0xE019);
        add("angry", codePoint(0x1F620), 0xE01A);
        add("rage", codePoint(0x1F621), 0xE01B);
        add("poop", codePoint(0x1F4A9), 0xE01C);
        add("shit", codePoint(0x1F4A9), 0xE01C);
        add("100", codePoint(0x1F4AF), 0xE01D);
        add("star", codePoint(0x2B50), 0xE01E);
        add("sparkles", codePoint(0x2728), 0xE01F);
        add("tada", codePoint(0x1F389), 0xE020);
        add("rocket", codePoint(0x1F680), 0xE021);
        add("moneybag", codePoint(0x1F4B0), 0xE022);
        add("heart_eyes", codePoint(0x1F60D), 0xE023);
    }

    private EmojiUtil() {
    }

    public static String replaceShortcodes(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        Matcher matcher = SHORTCODE_PATTERN.matcher(message);
        StringBuilder builder = new StringBuilder();
        while (matcher.find()) {
            String replacement = EMOJIS.get(matcher.group().toLowerCase());
            if (replacement != null) {
                matcher.appendReplacement(builder, Matcher.quoteReplacement(replacement));
            }
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    public static MutableText replaceShortcodesWithFont(String message) {
        if (message == null || message.isEmpty()) {
            return Text.literal(message == null ? "" : message);
        }

        Matcher matcher = SHORTCODE_PATTERN.matcher(message);
        MutableText text = Text.empty();
        int lastEnd = 0;
        while (matcher.find()) {
            String shortcode = matcher.group().toLowerCase();
            String glyph = FONT_GLYPHS.get(shortcode);
            if (glyph == null) {
                continue;
            }

            if (matcher.start() > lastEnd) {
                text.append(message.substring(lastEnd, matcher.start()));
            }
            text.append(Text.literal(glyph).styled(style -> style.withFont(EMOJI_FONT)));
            lastEnd = matcher.end();
        }

        if (lastEnd < message.length()) {
            text.append(message.substring(lastEnd));
        }
        return text;
    }

    private static void add(String shortcode, String emoji, int glyphCodePoint) {
        String key = ":" + shortcode + ":";
        EMOJIS.put(key, emoji);
        FONT_GLYPHS.put(key, String.valueOf((char) glyphCodePoint));
    }

    private static String codePoint(int codePoint) {
        return new String(Character.toChars(codePoint));
    }
}
