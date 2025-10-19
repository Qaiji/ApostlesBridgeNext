package com.medua.apostlesbridgenext.config;

import com.medua.apostlesbridgenext.client.ApostlesBridgeNextClient;
import com.medua.apostlesbridgenext.handler.MessageHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {
    private final ApostlesBridgeNextClient apostlesBridge;

    private TextFieldWidget urlField;
    private TextFieldWidget tokenField;
    private TextFieldWidget guildField;
    private ButtonWidget generalToggleButton;

    private int totalHeight = 0;
    private final int headerHeight = 10;
    private final int headerMarginBottom = 10;

    private int fieldTotalHeight = 0;
    private final int fieldSpacingHeight = 10;
    private final int fieldTextHeight = 15;
    private final int fieldTextboxHeight = 20;

    private final int generalToggleButtonHeight = 20;
    private final int formattingButtonHeight = 20;

    public ConfigScreen(ApostlesBridgeNextClient apostlesBridge) {
        super(Text.literal("Apostles Settings"));
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
        totalHeight += generalToggleButtonHeight; // button
    }

    @Override
    public void init() {
        int centerX = width / 2;
        int centerY = height / 2;

        int fieldPosition = centerY - totalHeight / 2 + headerMarginBottom + fieldSpacingHeight + fieldTextHeight;
        urlField = addDrawableChild(new TextFieldWidget(textRenderer, centerX - 100, fieldPosition, 200, fieldTextboxHeight, Text.literal("")));
        fieldPosition += fieldTotalHeight;
        tokenField = addDrawableChild(new TextFieldWidget(textRenderer, centerX - 100, fieldPosition, 200, fieldTextboxHeight, Text.literal("")));
        fieldPosition += fieldTotalHeight;
        guildField = addDrawableChild(new TextFieldWidget(textRenderer, centerX - 100, fieldPosition, 200, fieldTextboxHeight, Text.literal("")));
        fieldPosition += fieldTextboxHeight;

        urlField.setMaxLength(256);
        tokenField.setMaxLength(128);
        guildField.setMaxLength(64);

        urlField.setText(Config.getURL());
        tokenField.setText(Config.getToken());
        guildField.setText(Config.getGuild());

        generalToggleButton = addDrawableChild(ButtonWidget.builder(
                Text.literal(getModeButtonText()),
                button -> {
                    Config.nextGeneralMode();
                    generalToggleButton.setMessage(Text.literal(getModeButtonText()));
                }
        ).dimensions(centerX - 100, fieldPosition + fieldSpacingHeight, 200, generalToggleButtonHeight).build());

        // FORMATTING BUTTON
        addDrawableChild(ButtonWidget.builder(
                Text.literal("Formatting"),
                button -> {
                    saveSettings();
                    MinecraftClient.getInstance().setScreen(new ConfigFormattingScreen(this.apostlesBridge));
                }
        ).dimensions(10, height - formattingButtonHeight - 10, 80, formattingButtonHeight).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // this.renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);

        String title = "Apostles Settings";
        int titleWidth = textRenderer.getWidth(title);
        int titleX = (width - titleWidth) / 2;
        int titleY = height / 2 - totalHeight / 2;
        context.drawText(textRenderer, title, titleX, titleY, 0xFFFFFF, false);

        int textPosition = height / 2 - totalHeight / 2 + headerMarginBottom + fieldSpacingHeight;
        context.drawText(textRenderer, "WebSocket-URL:", width / 2 - 110, textPosition, 0xA0A0A0, false);
        textPosition += fieldTotalHeight;
        context.drawText(textRenderer, "WebSocket-Token:", width / 2 - 110, textPosition, 0xA0A0A0, false);
        textPosition += fieldTotalHeight;
        context.drawText(textRenderer, "Your Guild:", width / 2 - 110, textPosition, 0xA0A0A0, false);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public void removed() {
        saveAndExit(false);

        super.removed();
    }

    private String getModeButtonText() {
        return "Mode: " + Config.getGeneralModeText();
    }

    private void saveSettings() {
        Config.setURL(urlField.getText());
        Config.setGuild(guildField.getText());
        Config.setToken(tokenField.getText());
        Config.saveConfig();

        MessageHandler.sendMessage("Config has been saved");
    }

    private void saveAndExit(boolean removeScreen) {
        saveSettings();

        if (removeScreen) {
            MinecraftClient.getInstance().setScreen(null);
        }

        this.apostlesBridge.getWebSocketHandler().restartWebSocket(true);
    }
}
