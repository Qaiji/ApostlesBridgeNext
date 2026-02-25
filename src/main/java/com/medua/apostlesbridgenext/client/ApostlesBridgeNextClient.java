package com.medua.apostlesbridgenext.client;

import com.medua.apostlesbridgenext.generated.gen.BuildConfig;
import com.medua.apostlesbridgenext.commands.ApostlesCommand;
import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.events.PlayerJoinEvent;
import com.medua.apostlesbridgenext.handler.LogHandler;
import com.medua.apostlesbridgenext.handler.WebSocketHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;

public class ApostlesBridgeNextClient implements ClientModInitializer {

    public static final String MODID = BuildConfig.MODID;
    public static final String VERSION = BuildConfig.VERSION;

    private static final LogHandler LOGGER = new LogHandler(ApostlesBridgeNextClient.class);
    private WebSocketHandler webSocketHandler;

    @Override
    public void onInitializeClient() {
        LOGGER.info(MODID + " v" + VERSION + " initializing..");

        webSocketHandler = new WebSocketHandler(this);

        // REGISTER COMMANDS
        ApostlesCommand.register(this);

        //REGISTER EVENTS
        ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            PlayerJoinEvent.onPlayerJoin();
            getWebSocketHandler().restartWebSocket();
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> getWebSocketHandler().restartWebSocket(false));

        // LOAD CONFIG
        Config.loadConfig();
    }

    public WebSocketHandler getWebSocketHandler() {
        return this.webSocketHandler;
    }
}
