package com.medua.apostlesbridgenext.types;

import java.net.URI;
import java.util.Locale;

public enum LinkPreviewType {
    IMAGE,
    YOUTUBE,
    TWITCH;

    public static LinkPreviewType fromUrl(String url) {
        String host = getHost(url);
        if (host.equals("youtu.be") || host.equals("youtube.com") || host.endsWith(".youtube.com")) {
            return YOUTUBE;
        }
        if (host.equals("twitch.tv") || host.endsWith(".twitch.tv")) {
            return TWITCH;
        }
        return IMAGE;
    }

    private static String getHost(String url) {
        try {
            String host = URI.create(url).getHost();
            return host == null ? "" : host.toLowerCase(Locale.ROOT);
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }
}
