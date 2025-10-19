package com.medua.apostlesbridgenext.handler;

import com.google.gson.*;
import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.config.ConfigUtil;
import com.medua.apostlesbridgenext.config.Ignored;
import com.medua.apostlesbridgenext.config.IgnoredType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class WebSocketHandler {
    private static final LogHandler LOGGER = new LogHandler(WebSocketHandler.class);

    WebSocketClient webSocketClient;

    private Timer reconnectTimer;
    private static final int RECONNECT_DELAY = 30_000;
    private boolean reconnectScheduled = false;

    private boolean forceDisconnected = false;

    private String authKey = "";

    ApostlesBridgeNextClient apostlesBridge;

    public WebSocketHandler(ApostlesBridgeNextClient apostlesBridge) {
        this.apostlesBridge = apostlesBridge;

        waitForPlayerAndConnect();
    }

    private void waitForPlayerAndConnect() {
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                if (MinecraftClient.getInstance().player != null) {
                    LOGGER.debug("Player detected! Proceeding with WebSocket connection.");
                    if (shouldConnect()) {
                        connect();
                    }
                    cancel();
                } else {
                    LOGGER.debug("Waiting for player to initialize...");
                }
            }
        }, 0, 500);
    }

    public void connect() {
        if (!canConnect()) {
            LOGGER.warn("Canceled connecting to WebSocket, as the url or the token are unset.");
            return;
        }

        if (webSocketClient != null && !webSocketClient.isClosed()) {
            LOGGER.debug("Closing existing WebSocket connection before reconnecting...");
            webSocketClient.close();
        }

        LOGGER.debug("Trying to connect to WebSocket (" + getServerURL() + ")");
        try {
            webSocketClient = new WebSocketClient(new URI(getServerURL())) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    LOGGER.debug("Connected to WebSocket!");
                }

                @Override
                public void onMessage(String messageJson) {
                    try {
                        LOGGER.debug("WebSocket Recieved: " + messageJson);
                        JsonObject json = new Gson().fromJson(messageJson, JsonObject.class);

                        if (json.has("type")) {
                            String messageType = json.get("type").getAsString();

                            if (messageType.equals("authKey")) {
                                authKey = json.get("authKey").getAsString();
                                LOGGER.debug("Received new auth-key: " + authKey);
                                restartWebSocket();
                            } else if (messageType.equals("message") && json.has("messageData")) {
                                JsonObject messageData = json.getAsJsonObject("messageData");
                                String username = messageData.has("username") ? messageData.get("username").getAsString() : "";
                                String origin = messageData.has("origin") ? messageData.get("origin").getAsString() : "";
                                String originLongname = messageData.has("originLongname") ? messageData.get("originLongname").getAsString() : "";
                                String message = messageData.has("message") ? messageData.get("message").getAsString() : "";
                                String unformattedMessage = messageData.has("unformattedMessage") ? messageData.get("unformattedMessage").getAsString() : "";

                                Ignored ignoredPlayer = new Ignored(username, IgnoredType.PLAYER);
                                Ignored ignoredOrigin = new Ignored(originLongname, IgnoredType.ORIGIN);

                                if (Config.isIgnored(ignoredPlayer) || Config.isIgnored(ignoredOrigin)) {
                                    return;
                                }

                                JsonArray images = messageData.has("images") ? messageData.get("images").getAsJsonArray() : new JsonArray();
                                List<String> imageList = new ArrayList<>();

                                for (JsonElement element : images) {
                                    if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isString()) {
                                        imageList.add(element.getAsString());
                                    }
                                }

                                if (Config.getGuild().isEmpty() || (!origin.equalsIgnoreCase(Config.getGuild()) && !originLongname.equalsIgnoreCase(Config.getGuild()))) {
                                    String outputMessage = message;
                                    if (!unformattedMessage.isEmpty()) {
                                        outputMessage = unformattedMessage;
                                        outputMessage = outputMessage.replace("%originColor%", Config.getFormattingColors().getOriginColor());
                                        outputMessage = outputMessage.replace("%origin%", ConfigUtil.getOriginReplacement(origin));
                                        outputMessage = outputMessage.replaceAll("%userColor%", Config.getFormattingColors().getUserColor());
                                        outputMessage = outputMessage.replace("%messageColor%", Config.getFormattingColors().getMessageColor());
                                    }

                                    MinecraftClient client = MinecraftClient.getInstance();
                                    String finalOutputMessage = outputMessage;
                                    client.execute(() -> MessageHandler.sendMessageWithImages(finalOutputMessage, false, imageList));
                                }
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        LOGGER.error("WebSocket error parsing the response: " + e.getMessage());
                    }
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    LOGGER.debug("Disconnected from WebSocket: " + reason);
                    scheduleReconnect();
                }

                @Override
                public void onError(Exception e) {
                    LOGGER.error("WebSocket error: " + e.getMessage());
                    scheduleReconnect();
                }
            };
        } catch (URISyntaxException e) {
            LOGGER.error("An error occured trying to connect to the WebSocket (" + e.getMessage() + ")");
        }
        webSocketClient.connect();
    }

    private boolean canConnect() {
        return !Config.getURL().isEmpty() && !Config.getToken().isEmpty();
    }

    private boolean shouldConnect() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            LOGGER.debug("Reconnect skipped: WebSocket is already connected.");
            return false;
        }
        if (forceDisconnected) {
            LOGGER.debug("Reconnect skipped: WebSocket force disconnected.");
            return false;
        }

        int mode = Config.getGeneralMode();
        switch (mode) {
            case 0: // OFF
                LOGGER.debug("WebSocket connection canceled: Mode is OFF.");
                return false;
            case 1: // EVERYWHERE
                return true;
            case 2: // HYPIXEL_ONLY
                ServerInfo serverInfo = MinecraftClient.getInstance().getCurrentServerEntry();
                if (serverInfo != null && serverInfo.address != null && serverInfo.address.contains("hypixel.net")) {
                    LOGGER.debug("Player is on Hypixel. Connecting to WebSocket...");
                    return true;
                } else {
                    LOGGER.debug("WebSocket connection canceled: Not on Hypixel.");
                    return false;
                }
            default:
                LOGGER.debug("Unknown mode detected. WebSocket will NOT connect.");
                return false;
        }
    }

    private String getServerURL() {
        return getServerURL(Config.getToken());
    }

    private String getServerURL(String token) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        String username = player != null ? player.getName().getString() : "";
        String uuid = player != null ? player.getUuidAsString() : "";

        return "ws://" + Config.getURL() + "?token=" + token + "&authKey=" + authKey + "&username=" + username + "&uuid=" + uuid;
    }

    private synchronized void scheduleReconnect() {
        if (reconnectTimer != null) {
            reconnectTimer.cancel();
            reconnectTimer.purge();
        }

        reconnectTimer = new Timer();
        reconnectScheduled = true;
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                reconnectScheduled = false;
                if (shouldConnect()) {
                    LOGGER.info("Reconnecting to WebSocket...");
                    restartWebSocket();
                } else {
                    LOGGER.debug("Reconnect skipped due to mode restrictions or connection status.");
                }
            }
        }, RECONNECT_DELAY);
    }

    public String getStatus() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            return "§aCONNECTED§r";
        } else {
            return "§cDISCONNECTED§r" + (reconnectScheduled ? " §7(⟳ in a moment)§r" : "");
        }
    }

    public synchronized void restartWebSocket() {
        this.restartWebSocket(false);
    }

    public synchronized void restartWebSocket(boolean clearSession) {
        if (clearSession) {
            authKey = "";
        }

        if (reconnectTimer != null) {
            reconnectTimer.cancel();
        }

        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close();
        }

        if (this.forceDisconnected) {
            this.forceDisconnected = false;
        }

        if (shouldConnect()) {
            connect();
        } else {
            LOGGER.debug("Restart skipped due to mode restrictions.");
        }
    }

    public synchronized void disconnectWebSocket() {
        this.disconnectWebSocket(false);
    }

    public synchronized void disconnectWebSocket(boolean clearSession) {
        if (clearSession) {
            authKey = "";
        }

        if (reconnectTimer != null) {
            reconnectTimer.cancel();
        }

        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close();
        }

        this.forceDisconnected = true;
    }
}
