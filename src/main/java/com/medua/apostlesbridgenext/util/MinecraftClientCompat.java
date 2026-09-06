package com.medua.apostlesbridgenext.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class MinecraftClientCompat {
    private MinecraftClientCompat() { }

    public static Object getChat(Minecraft client) {
        //? if >=26.2 {
        /*return client.gui.hud.getChat();
        *///? } else {
        return client.gui.getChat();
        //? }
    }

    public static int getGuiTicks(Minecraft client) {
        //? if >=26.2 {
        /*return client.gui.hud.getGuiTicks();
        *///? } else {
        return client.gui.getGuiTicks();
        //? }
    }

    public static void setScreen(Minecraft client, Screen screen) {
        //? if >=26.2 {
        /*client.gui.setScreen(screen);
        *///? } else {
        client.setScreen(screen);
        //? }
    }
}
