package com.medua.apostlesbridgenext.commands;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.config.*;
import com.medua.apostlesbridgenext.handler.MessageHandler;
import com.medua.apostlesbridgenext.types.IgnoredType;
import com.medua.apostlesbridgenext.util.ConfigUtil;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

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
                    literal(alias)
                        // /bridge reconnect
                        .then(literal("reconnect")
                            .executes(context -> {
                                proceedCommand(apostlesBridge, alias, new String[]{"reconnect"});
                                return 1;
                            }))
                        // /bridge status
                        .then(literal("status")
                            .executes(context -> {
                                proceedCommand(apostlesBridge, alias, new String[]{"status"});
                                return 1;
                            }))
                        // /bridge debug [message and urls]
                        .then(literal("debug")
                            .executes(context -> {
                                proceedCommand(apostlesBridge, alias, new String[]{"debug"});
                                return 1;
                            })
                            .then(argument("urls", StringArgumentType.greedyString())
                                .executes(context -> {
                                    String urls = StringArgumentType.getString(context, "urls");
                                    proceedCommand(apostlesBridge, alias, new String[]{"debug", urls});
                                    return 1;
                                })))
                        // /bridge disconnect
                        .then(literal("disconnect")
                                .executes(context -> {
                                    proceedCommand(apostlesBridge, alias, new String[]{"disconnect"});
                                    return 1;
                                }))
                        // /bridge ignore <add|remove|list> [player|origin] [name]
                        .then(literal("ignore")
                            .then(literal("add")
                                .then(literal("player")
                                    .then(argument("name", StringArgumentType.word())
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
                                .then(literal("origin")
                                    .then(argument("name", StringArgumentType.word())
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
                            .then(literal("remove")
                                .then(literal("player")
                                    .then(argument("name", StringArgumentType.word())
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
                                .then(literal("origin")
                                    .then(argument("name", StringArgumentType.word())
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
                            .then(literal("list")
                                .executes(context -> {
                                    proceedCommand(apostlesBridge, alias, new String[]{"ignore", "list"});
                                    return 1;
                                })
                            )
                        )
                        // /bridge help
                        .then(literal("help")
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

    private static LiteralArgumentBuilder<FabricClientCommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private static <T> RequiredArgumentBuilder<FabricClientCommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

    public static void openScreenNextTick(Screen screen) {
        AtomicBoolean opened = new AtomicBoolean(false);
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!opened.get()) {
                Minecraft.getInstance().setScreen(screen);
                opened.set(true);
            }
        });
    }

    public static boolean proceedCommand(ApostlesBridgeNextClient apostlesBridge, String command, String[] args) {
        if (args.length == 0) {
            openScreenNextTick(ConfigGuiManager.openConfigGui(apostlesBridge));
            return true;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("debug")) {
            sendDebugMessage(parseDebugMessage(String.join(" ", Arrays.copyOfRange(args, 1, args.length))));
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
                MessageHandler.sendMessage("§d/bridge debug <message/urls> §7- Sends a local websocket-style debug message", false);
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

    private static void sendDebugMessage(DebugMessage debugMessage) {
        String origin = Config.getFormattingColors().getOriginColor() + ConfigUtil.getOriginReplacement("debug");
        String user = Config.getFormattingColors().getUserColor() + "DebugUser";
        String message = Config.getFormattingColors().getMessageColor() + debugMessage.message();
        MessageHandler.sendMessageWithLinks(origin + " > " + user + ": " + message, false, debugMessage.urls());
    }

    private static DebugMessage parseDebugMessage(String input) {
        List<String> urls = new ArrayList<>();
        List<String> messageParts = new ArrayList<>();
        for (String part : input.trim().split("\\s+")) {
            if (part.startsWith("http")) {
                urls.add(part);
            } else if (!part.isBlank()) {
                messageParts.add(part);
            }
        }

        String message = messageParts.isEmpty()
                ? "local websocket-style debug message"
                : String.join(" ", messageParts);
        return new DebugMessage(message, urls);
    }

    private record DebugMessage(String message, List<String> urls) {
    }
}
