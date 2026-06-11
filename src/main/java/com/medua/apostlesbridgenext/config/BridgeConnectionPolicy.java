package com.medua.apostlesbridgenext.config;

public final class BridgeConnectionPolicy {
    private BridgeConnectionPolicy() { }

    public static boolean shouldConnect(int generalMode, boolean respectGuildChatToggle, boolean guildChatEnabled, boolean onHypixel) {
        if (isBlockedByGuildChatToggle(generalMode, respectGuildChatToggle, guildChatEnabled)) {
            return false;
        }

        return switch (generalMode) {
            case 0 -> false;
            case 1 -> true;
            case 2 -> onHypixel;
            default -> false;
        };
    }

    public static boolean isBlockedByGuildChatToggle(int generalMode, boolean respectGuildChatToggle, boolean guildChatEnabled) {
        return generalMode != 0 && respectGuildChatToggle && !guildChatEnabled;
    }
}
