package com.medua.apostlesbridgenext.config;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.handler.MessageHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ConfigFormattingScreen extends Screen {
    private final ApostlesBridgeNextClient apostlesBridge;

    // COLOR FIELDS
    private TextFieldWidget originColorField;
    private TextFieldWidget userColorField;
    private TextFieldWidget messageColorField;
    // NAMES FIELDS
    private TextFieldWidget bridgeField;
    private TextFieldWidget discordField;
    private TextFieldWidget g1Field;
    private TextFieldWidget g2Field;
    private TextFieldWidget g3Field;

    private int totalHeight = 0;
    private final int headerHeight = 10;
    private final int headerMarginBottom = 10;

    private int fieldTotalHeight;
    private final int fieldSpacingHeight = 10;
    private final int fieldTextHeight = 15;
    private final int fieldTextboxHeight = 20;

    private final int backButtonHeight = 20;

    public ConfigFormattingScreen(ApostlesBridgeNextClient apostlesBridge) {
        super(Text.literal("Apostles Formatting Settings"));
        this.apostlesBridge = apostlesBridge;

        fieldTotalHeight = 0;
        fieldTotalHeight += fieldSpacingHeight; // spacing
        fieldTotalHeight += fieldTextHeight; // text
        fieldTotalHeight += fieldTextboxHeight; // field

        totalHeight += headerHeight; // header
        totalHeight += headerMarginBottom;
        int totalFields = 3;
        for (int i = 0; i < totalFields; i++) {
            totalHeight += fieldTotalHeight;
        }
        totalHeight += fieldSpacingHeight; // spacing
    }

    @Override
    public void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        int fieldPosition = centerY - totalHeight / 2 + headerMarginBottom + fieldSpacingHeight + fieldTextHeight;
        originColorField = new TextFieldWidget(textRenderer, centerX - 100 + 40 - 5, fieldPosition, 20, fieldTextboxHeight, Text.literal(""));
        userColorField = new TextFieldWidget(textRenderer, centerX - 30 + 40 - 10 - 2, fieldPosition, 20, fieldTextboxHeight, Text.literal(""));
        messageColorField = new TextFieldWidget(textRenderer, centerX + 40 + 40, fieldPosition, 20, fieldTextboxHeight, Text.literal(""));
        fieldPosition += fieldTotalHeight + fieldSpacingHeight;
        bridgeField = new TextFieldWidget(textRenderer, centerX - 100, fieldPosition, 95, fieldTextboxHeight, Text.literal(""));
        discordField = new TextFieldWidget(textRenderer, centerX + 5, fieldPosition, 95, fieldTextboxHeight, Text.literal(""));
        fieldPosition += fieldSpacingHeight + fieldTextboxHeight + fieldSpacingHeight - 5;
        g1Field = new TextFieldWidget(textRenderer, centerX - 100, fieldPosition, 60, fieldTextboxHeight, Text.literal(""));
        g2Field = new TextFieldWidget(textRenderer, centerX - 30, fieldPosition, 60, fieldTextboxHeight, Text.literal(""));
        g3Field = new TextFieldWidget(textRenderer, centerX + 40, fieldPosition, 60, fieldTextboxHeight, Text.literal(""));

        originColorField.setMaxLength(2);
        userColorField.setMaxLength(2);
        messageColorField.setMaxLength(2);

        bridgeField.setMaxLength(64);
        discordField.setMaxLength(64);
        g1Field.setMaxLength(64);
        g2Field.setMaxLength(64);
        g3Field.setMaxLength(64);

        // BACK BUTTON
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Back"),
                button -> {
                    saveSettings();
                    MinecraftClient.getInstance().setScreen(new ConfigScreen(this.apostlesBridge));
                }
        ).dimensions(10, 10, 40, backButtonHeight).build());

        originColorField.setText(ConfigUtil.convertToColor(Config.getFormattingColors().getOriginColor()));
        userColorField.setText(ConfigUtil.convertToColor(Config.getFormattingColors().getUserColor()));
        messageColorField.setText(ConfigUtil.convertToColor(Config.getFormattingColors().getMessageColor()));

        bridgeField.setText(Config.getFormattingNames().getBridge());
        discordField.setText(Config.getFormattingNames().getDiscord());
        g1Field.setText(Config.getFormattingNames().getG1());
        g2Field.setText(Config.getFormattingNames().getG2());
        g3Field.setText(Config.getFormattingNames().getG3());

        addDrawableChild(originColorField);
        addDrawableChild(userColorField);
        addDrawableChild(messageColorField);
        addDrawableChild(bridgeField);
        addDrawableChild(discordField);
        addDrawableChild(g1Field);
        addDrawableChild(g2Field);
        addDrawableChild(g3Field);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderInGameBackground(context);

        String title = "Apostles Settings";
        int titleWidth = textRenderer.getWidth(title);
        int titleX = (width - titleWidth) / 2;
        int titleY = height / 2 - totalHeight / 2;
        context.drawText(textRenderer, title, titleX, titleY, 0xFFFFFF, false);

        String formattingHeader = "> Formatting";
        float tinyHeaderScale = 0.5f;
        int formatWidth = (int)(textRenderer.getWidth(formattingHeader) * tinyHeaderScale);
        int formatX = (int)((float)width / 2 - formatWidth / 2);
        int formatY = (int)(height / 2 - (float)totalHeight / 2 + 10);
        context.getMatrices().pushMatrix();
        context.getMatrices().scale(tinyHeaderScale, tinyHeaderScale);
        context.drawText(textRenderer, formattingHeader, (int)(formatX / tinyHeaderScale), (int)(formatY / tinyHeaderScale), 0xA0A0A0, false);
        context.getMatrices().popMatrix();

        int textPosition = height / 2 - totalHeight / 2 + headerMarginBottom + fieldSpacingHeight;
        context.drawText(textRenderer, "Colors:", width / 2 - 110, textPosition, 0xA0A0A0, false);
        textPosition += fieldSpacingHeight + fieldTextHeight - 5 + 2;
        context.drawText(textRenderer, "Origin:", width / 2 - 100, textPosition, 0xA0A0A0, false);
        context.drawText(textRenderer, "User:", width / 2 - 30 - 5 + 2, textPosition, 0xA0A0A0, false);
        context.drawText(textRenderer, "Message:", width / 2 + 40 - 10 + 1, textPosition, 0xA0A0A0, false);
        textPosition += fieldSpacingHeight + fieldTextHeight - 2;
        context.drawText(textRenderer, "Names:", width / 2 - 110, textPosition, 0xA0A0A0, false);
        textPosition += fieldTextHeight;
        context.drawText(textRenderer, "Bridge", width / 2 - 100, textPosition, 0xA0A0A0, false);
        context.drawText(textRenderer, "Discord", width / 2 + 5, textPosition, 0xA0A0A0, false);
        textPosition += fieldTotalHeight - fieldSpacingHeight;
        context.drawText(textRenderer, "Guild 1", width / 2 - 100, textPosition, 0xA0A0A0, false);
        context.drawText(textRenderer, "Guild 2", width / 2 - 30, textPosition, 0xA0A0A0, false);
        context.drawText(textRenderer, "Guild 3", width / 2 + 40, textPosition, 0xA0A0A0, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        saveAndExit(false);
        super.removed();
    }

    private void saveSettings() {
        Config.getFormattingColors().setOriginColor(ConfigUtil.convertToRawColor(originColorField.getText()));
        Config.getFormattingColors().setUserColor(ConfigUtil.convertToRawColor(userColorField.getText()));
        Config.getFormattingColors().setMessageColor(ConfigUtil.convertToRawColor(messageColorField.getText()));

        Config.getFormattingNames().setBridge(bridgeField.getText());
        Config.getFormattingNames().setDiscord(discordField.getText());
        Config.getFormattingNames().setG1(g1Field.getText());
        Config.getFormattingNames().setG2(g2Field.getText());
        Config.getFormattingNames().setG3(g3Field.getText());
        Config.saveConfig();

        MessageHandler.sendMessage("Config has been saved");
    }

    private void saveAndExit(boolean removeScreen) {
        saveSettings();
        if (removeScreen) {
            MinecraftClient.getInstance().setScreen(null);
        }
    }
}
