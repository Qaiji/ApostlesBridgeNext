package com.medua.apostlesbridgenext.config;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.medua.apostlesbridgenext.handler.LogHandler;
import com.medua.apostlesbridgenext.types.IgnoredType;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Config {
    private static final LogHandler LOGGER = new LogHandler(Config.class);

    public static final String CONFIG_FILE_URL = "config/apostles.json";
    private static final File CONFIG_FILE = new File(CONFIG_FILE_URL);
    private static final Gson GSON = new Gson();

    private static final String[] GENERAL_MODES = {"OFF", "EVERYWHERE", "HYPIXEL ONLY"};

    private static String url = "";
    private static String token = "";
    private static String guild = "";

    private static int generalMode = 2;
    private static int imagePreviewSize = ImagePreviewSize.MEDIUM.ordinal();

    private static FormattingColors formattingColors = new FormattingColors();
    private static FormattingNames formattingNames = new FormattingNames();

    private static List<Ignored> ignoredList = new ArrayList<>();

    public static void loadConfig() {
        if (!CONFIG_FILE.exists()) {
            saveConfig();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8))) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);

            url = json.has("url") ? json.get("url").getAsString() : url;
            guild = json.has("guild") ? json.get("guild").getAsString() : guild;
            token = json.has("token") ? json.get("token").getAsString() : token;
            generalMode = json.has("generalMode") ? json.get("generalMode").getAsInt() : generalMode;
            imagePreviewSize = json.has("imagePreviewSize") ? clampPreviewSize(json.get("imagePreviewSize").getAsInt()) : imagePreviewSize;

            if (json.has("formatting")) {
                JsonObject formatting = json.getAsJsonObject("formatting");
                if (formatting.has("colors")) {
                    JsonObject formattingColors = formatting.getAsJsonObject("colors");
                    getFormattingColors().setOriginColor(formattingColors.has("originColor") ? formattingColors.get("originColor").getAsString() : getFormattingColors().getOriginColor());
                    getFormattingColors().setUserColor(formattingColors.has("userColor") ? formattingColors.get("userColor").getAsString() : getFormattingColors().getUserColor());
                    getFormattingColors().setMessageColor(formattingColors.has("messageColor") ? formattingColors.get("messageColor").getAsString() : getFormattingColors().getMessageColor());
                }
                if (formatting.has("names")) {
                    JsonObject formattingNames = formatting.getAsJsonObject("names");
                    getFormattingNames().setBridge(formattingNames.has("bridge") ? formattingNames.get("bridge").getAsString() : getFormattingNames().getBridge());
                    getFormattingNames().setDiscord(formattingNames.has("discord") ? formattingNames.get("discord").getAsString() : getFormattingNames().getDiscord());
                    getFormattingNames().setG1(formattingNames.has("g1") ? formattingNames.get("g1").getAsString() : getFormattingNames().getG1());
                    getFormattingNames().setG2(formattingNames.has("g2") ? formattingNames.get("g2").getAsString() : getFormattingNames().getG2());
                    getFormattingNames().setG3(formattingNames.has("g3") ? formattingNames.get("g3").getAsString() : getFormattingNames().getG3());
                }
            }

            if (json.has("ignored") && json.get("ignored").isJsonArray()) {
                ignoredList.clear();
                JsonArray ignoredArray = json.getAsJsonArray("ignored");

                for (JsonElement ignoredArrayElement : ignoredArray) {
                    if (ignoredArrayElement.isJsonObject()) {
                        JsonObject ignoredJsonObject = ignoredArrayElement.getAsJsonObject();
                        String name = ignoredJsonObject.has("name") ? ignoredJsonObject.get("name").getAsString() : "";
                        IgnoredType type;
                        try {
                            type = ignoredJsonObject.has("type") ? IgnoredType.valueOf(ignoredJsonObject.get("type").getAsString()) : IgnoredType.PLAYER;
                        } catch (IllegalArgumentException e) {
                            type = IgnoredType.PLAYER;
                        }

                        ignoredList.add(new Ignored(name, type));
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("There was an issue reading from the config file!");
        }
    }

    public static void saveConfig() {
        JsonObject json = new JsonObject();
        json.addProperty("url", url);
        json.addProperty("guild", guild);
        json.addProperty("token", token);
        json.addProperty("generalMode", generalMode);
        json.addProperty("imagePreviewSize", imagePreviewSize);

        JsonObject formatting = new JsonObject();
        JsonObject formattingColors = new JsonObject();
        formattingColors.addProperty("originColor", getFormattingColors().getOriginColor());
        formattingColors.addProperty("userColor", getFormattingColors().getUserColor());
        formattingColors.addProperty("messageColor", getFormattingColors().getMessageColor());
        formatting.add("colors", formattingColors);

        JsonObject formattingNames = new JsonObject();
        formattingNames.addProperty("bridge", getFormattingNames().getBridge());
        formattingNames.addProperty("discord", getFormattingNames().getDiscord());
        formattingNames.addProperty("g1", getFormattingNames().getG1());
        formattingNames.addProperty("g2", getFormattingNames().getG2());
        formattingNames.addProperty("g3", getFormattingNames().getG3());
        formatting.add("names", formattingNames);

        json.add("formatting", formatting);

        JsonArray ignoredJsonArray = new JsonArray();
        for (Ignored ignored : ignoredList) {
            JsonObject ignoredJsonObject = new JsonObject();
            ignoredJsonObject.addProperty("name", ignored.getName());
            ignoredJsonObject.addProperty("type", ignored.getType().toString());
            ignoredJsonArray.add(ignoredJsonObject);
        }
        json.add("ignored", ignoredJsonArray);

        try (Writer writer = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            GSON.toJson(json, writer);
        } catch (IOException e) {
            LOGGER.error("There was an issue writing to the config file!");
        }
    }

    public static String getURL() {
        return url;
    }

    public static String getGuild() {
        return guild;
    }

    public static String getToken() {
        return token;
    }

    public static int getGeneralMode() {
        return generalMode;
    }

    public static ImagePreviewSize getImagePreviewSize() {
        return ImagePreviewSize.values()[clampPreviewSize(imagePreviewSize)];
    }

    public static String getGeneralModeText() {
        return GENERAL_MODES[generalMode];
    }

    public static void setURL(String newUrl) {
        url = newUrl;
    }

    public static void setGuild(String newGuild) {
        guild = newGuild;
    }

    public static void setToken(String newToken) {
        token = newToken;
    }

    public static void setGeneralMode(int newGeneralMode) {
        generalMode = newGeneralMode;
    }

    public static void setImagePreviewSize(int newImagePreviewSize) {
        imagePreviewSize = clampPreviewSize(newImagePreviewSize);
    }

    public static void nextGeneralMode() {
        generalMode = (generalMode + 1) % GENERAL_MODES.length;
    }

    public static FormattingNames getFormattingNames() {
        return formattingNames;
    }

    public static void setFormattingNames(FormattingNames formattingNames) {
        Config.formattingNames = formattingNames;
    }

    public static FormattingColors getFormattingColors() {
        return formattingColors;
    }

    public static void setFormattingColors(FormattingColors formattingColors) {
        Config.formattingColors = formattingColors;
    }

    public static List<Ignored> getIgnoredList() {
        return ignoredList;
    }

    public static List<Ignored> getIgnoredList(IgnoredType type) {
        return ignoredList.stream().filter(ignored -> ignored.getType() == type).collect(Collectors.toList());
    }

    public static List<String> getIgnoredListNames(IgnoredType type) {
        return getIgnoredList(type).stream().map(Ignored::getName).collect(Collectors.toList());
    }

    public static void setIgnoredList(List<Ignored> ignoredList) {
        Config.ignoredList = ignoredList;
    }

    public static boolean isIgnored(Ignored ignoredToFind) {
        return Config.ignoredList.stream().anyMatch(ignored -> ignored.getName().equalsIgnoreCase(ignoredToFind.getName()) && ignored.getType() == ignoredToFind.getType());
    }

    public static void removeIgnored(Ignored ignoredToRemove) {
        Config.ignoredList.removeIf(ignored -> ignored.getName().equalsIgnoreCase(ignoredToRemove.getName()) && ignored.getType() == ignoredToRemove.getType());
    }

    public static void addIgnored(Ignored ignored) {
        Config.ignoredList.add(ignored);
    }

    private static int clampPreviewSize(int size) {
        return Math.max(0, Math.min(size, ImagePreviewSize.values().length - 1));
    }

    public enum ImagePreviewSize {
        EXTRA_SMALL(120, 80),
        SMALL(160, 110),
        MEDIUM(220, 150),
        LARGE(320, 220);

        private final int maxWidth;
        private final int maxHeight;

        ImagePreviewSize(int maxWidth, int maxHeight) {
            this.maxWidth = maxWidth;
            this.maxHeight = maxHeight;
        }

        public int maxWidth() {
            return maxWidth;
        }

        public int maxHeight() {
            return maxHeight;
        }
    }
}


