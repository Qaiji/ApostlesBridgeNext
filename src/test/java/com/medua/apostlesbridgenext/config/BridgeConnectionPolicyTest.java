package com.medua.apostlesbridgenext.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BridgeConnectionPolicyTest {
    @Test
    void modeOffNeverConnects() {
        assertFalse(BridgeConnectionPolicy.shouldConnect(0, true, true, true));
        assertFalse(BridgeConnectionPolicy.shouldConnect(0, false, false, true));
    }

    @Test
    void respectDisabledIgnoresGuildChatState() {
        assertTrue(BridgeConnectionPolicy.shouldConnect(1, false, false, false));
        assertTrue(BridgeConnectionPolicy.shouldConnect(2, false, false, true));
    }

    @Test
    void respectEnabledBlocksWhenGuildChatIsDisabled() {
        assertFalse(BridgeConnectionPolicy.shouldConnect(1, true, false, false));
        assertFalse(BridgeConnectionPolicy.shouldConnect(2, true, false, true));
    }

    @Test
    void respectEnabledAllowsNormalModeRulesWhenGuildChatIsEnabled() {
        assertTrue(BridgeConnectionPolicy.shouldConnect(1, true, true, false));
        assertTrue(BridgeConnectionPolicy.shouldConnect(2, true, true, true));
        assertFalse(BridgeConnectionPolicy.shouldConnect(2, true, true, false));
    }

    @Test
    void detectsWhenRespectGuildToggleIsTheBlockingReason() {
        assertTrue(BridgeConnectionPolicy.isBlockedByGuildChatToggle(1, true, false));
        assertTrue(BridgeConnectionPolicy.isBlockedByGuildChatToggle(2, true, false));
        assertFalse(BridgeConnectionPolicy.isBlockedByGuildChatToggle(0, true, false));
        assertFalse(BridgeConnectionPolicy.isBlockedByGuildChatToggle(1, false, false));
        assertFalse(BridgeConnectionPolicy.isBlockedByGuildChatToggle(1, true, true));
    }
}
