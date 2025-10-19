package com.medua.apostlesbridgenext.commands;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.config.Config;
import com.medua.apostlesbridgenext.config.ConfigScreen;
import com.medua.apostlesbridgenext.config.Ignored;
import com.medua.apostlesbridgenext.config.IgnoredType;
import com.medua.apostlesbridgenext.handler.MessageHandler;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ApostlesCommand {

    static final String COMMAND_NAME = "apostles";
    static final List<String> COMMAND_ALIASES = new ArrayList<>(List.of(COMMAND_NAME, "apostlesbridge", "bridge"));

    ApostlesBridgeNextClient apostlesBridge;
    public ApostlesCommand(ApostlesBridgeNextClient apostlesBridge) {
        this.apostlesBridge = apostlesBridge;
    }

    public static void register(ApostlesBridgeNextClient apostlesBridge) {
        List<String> allCommands = new ArrayList<>(COMMAND_ALIASES);
        allCommands.add(COMMAND_NAME);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            for (String alias : allCommands) {
                dispatcher.register(
                    ClientCommandManager.literal(alias)
                        // /bridge reconnect
                        .then(ClientCommandManager.literal("reconnect")
                            .executes(context -> {
                                proceedCommand(apostlesBridge, alias, new String[]{"reconnect"});
                                return 1;
                            }))
                        // /bridge status
                        .then(ClientCommandManager.literal("status")
                            .executes(context -> {
                                proceedCommand(apostlesBridge, alias, new String[]{"status"});
                                return 1;
                            }))
                        // /bridge disconnect
                        .then(ClientCommandManager.literal("disconnect")
                                .executes(context -> {
                                    proceedCommand(apostlesBridge, alias, new String[]{"disconnect"});
                                    return 1;
                                }))
                        // /bridge ignore <add|remove|list> [player|origin] [name]
                        .then(ClientCommandManager.literal("ignore")
                            .then(ClientCommandManager.literal("add")
                                .then(ClientCommandManager.literal("player")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            // TODO: add player suggestion
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            proceedCommand(apostlesBridge, alias, new String[]{"ignore", "add", "player", name});
                                            return 1;
                                        })
                                    )
                                )
                                .then(ClientCommandManager.literal("origin")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            // TODO: add player suggestion
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            proceedCommand(apostlesBridge, alias, new String[]{"ignore", "add", "origin", name});
                                            return 1;
                                        })
                                    )
                                )
                            )
                            .then(ClientCommandManager.literal("remove")
                                .then(ClientCommandManager.literal("player")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            // TODO: add player suggestion
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            proceedCommand(apostlesBridge, alias, new String[]{"ignore", "remove", "player", name});
                                            return 1;
                                        })
                                    )
                                )
                                .then(ClientCommandManager.literal("origin")
                                    .then(ClientCommandManager.argument("name", StringArgumentType.word())
                                        .suggests((context, builder) -> {
                                            // TODO: add origin suggestion
                                            return builder.buildFuture();
                                        })
                                        .executes(context -> {
                                            String name = StringArgumentType.getString(context, "name");
                                            proceedCommand(apostlesBridge, alias, new String[]{"ignore", "remove", "origin", name});
                                            return 1;
                                        })
                                    )
                                )
                            )
                            .then(ClientCommandManager.literal("list")
                                .executes(context -> {
                                    proceedCommand(apostlesBridge, alias, new String[]{"ignore", "list"});
                                    return 1;
                                })
                            )
                        )
                        // /bridge help
                        .then(ClientCommandManager.literal("help")
                            .executes(context -> {
                                proceedCommand(apostlesBridge, alias, new String[]{"help"});
                                return 1;
                            }))
                        // /bridge
                        .executes(context -> {
                            proceedCommand(apostlesBridge, alias, new String[0]);
                            return 1;
                        })
                );
            }
        });
    }

    public static void openScreenNextTick(Screen screen) {
        AtomicBoolean opened = new AtomicBoolean(false);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!opened.get()) {
                MinecraftClient.getInstance().setScreen(screen);
                opened.set(true);
            }
        });
    }

    public static boolean proceedCommand(ApostlesBridgeNextClient apostlesBridge, String command, String[] args) {
        if (args.length == 0) {
            openScreenNextTick(new ConfigScreen(apostlesBridge));
            return true;
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reconnect")) {
                MessageHandler.sendMessage("Restarting WebSocket and clearing session");
                apostlesBridge.getWebSocketHandler().restartWebSocket();
                return true;
            } else if (args[0].equalsIgnoreCase("disconnect")) {
                MessageHandler.sendMessage("Disconnecting WebSocket and clearing session");
                apostlesBridge.getWebSocketHandler().disconnectWebSocket();
                return true;
            } else if (args[0].equalsIgnoreCase("status")) {
                MessageHandler.sendMessage("WebSocket connection: "+apostlesBridge.getWebSocketHandler().getStatus());
                return true;
            } else if (args[0].equalsIgnoreCase("ignore")) {
                MessageHandler.sendMessage("Command usage: §§d/bridge ignore <add/remove/list> [player/origin] [name]");
                return true;
            } else if (args[0].equalsIgnoreCase("help")) {
                MessageHandler.sendMessage("§lApostles Command Usages", false);
                MessageHandler.sendMessage("§d/bridge reconnect §7- Clears the session and restarts the WebSocket-connection", false);
                MessageHandler.sendMessage("§d/bridge status §7- Returns the current status of the WebSocket-connection", false);
                MessageHandler.sendMessage("§d/bridge ignore list §7- Lists all ignored players and origins", false);
                MessageHandler.sendMessage("§d/bridge ignore add <player/origin> [name] §7- Adds the selected player or origin to the ignore list", false);
                MessageHandler.sendMessage("§d/bridge ignore remove <player/origin> [name] §7- Removes the selected player or origin from the ignore list", false);
                return true;
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("ignore")) {
                if (args[1].equalsIgnoreCase("list")) {
                    List<String> playerNames = Config.getIgnoredListNames(IgnoredType.PLAYER);
                    List<String> originNames = Config.getIgnoredListNames(IgnoredType.ORIGIN);
                    if (playerNames.isEmpty() && originNames.isEmpty()) {
                        MessageHandler.sendMessage("The ignore list is empty! §a^-^");
                    } else {
                        if (!playerNames.isEmpty()) {
                            String players = String.join(", ", playerNames);
                            MessageHandler.sendMessage("§dIgnored Players§r: " + players, false);
                        }

                        if (!originNames.isEmpty()) {
                            String origins = String.join(", ", originNames);
                            MessageHandler.sendMessage("§dIgnored Origins§r: " + origins, false);
                        }
                    }
                    return true;
                }
            }
        } else if (args.length >= 4) {
            if (args[0].equalsIgnoreCase("ignore")) {
                try {
                    IgnoredType ignoredType = IgnoredType.valueOf(args[2].toUpperCase());
                    String name = ignoredType == IgnoredType.PLAYER ? args[3] : String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                    Ignored ignored = new Ignored(name, ignoredType);
                    if (args[1].equalsIgnoreCase("add")) {
                        if (!Config.isIgnored(ignored)) {
                            Config.addIgnored(new Ignored(name, ignoredType));
                            Config.saveConfig();
                            MessageHandler.sendMessage("The §7" + ignoredType + " §d" + name + " §rwas added to the ignore list");
                        } else {
                            MessageHandler.sendMessage("The §7" + ignoredType + " §d" + name + " §ris already on the ignore list");
                        }
                        return true;
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        if (Config.isIgnored(ignored)) {
                            Config.removeIgnored(new Ignored(name, ignoredType));
                            Config.saveConfig();
                            MessageHandler.sendMessage("The §7" + ignoredType + " §d" + name + " §rwas removed from the ignore list");
                        } else {
                            MessageHandler.sendMessage("The §7" + ignoredType + " §d" + name + " §ris not on the ignore list");
                        }
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    MessageHandler.sendMessage("Command usage: §d/bridge ignore <add/remove/list> [player/origin] [name]");
                    return true;
                }
            }
        }
        MessageHandler.sendMessage("Incorrect usage. Please use §d/bridge help §rto get a list of all commands and their usage");
        return false;
    }
}
