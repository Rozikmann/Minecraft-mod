package com.example.indwave.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class PlotMenuScreen extends Screen {

    public PlotMenuScreen() {
        super(Component.literal("Управление измерением"));
    }

    @Override
    protected void init() {
        super.init();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Кнопка "Телепорт домой"
        this.addRenderableWidget(Button.builder(Component.literal("Вернуться на участок"), (btn) -> {
            // Отправляем команду на сервер (игрок как бы вводит её в чат)
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.connection.sendCommand("plot home");
            }
            this.onClose();
        }).bounds(centerX - 100, centerY - 40, 200, 20).build());

        // Кнопка "Пригласить игрока"
        this.addRenderableWidget(Button.builder(Component.literal("Пригласить в пати"), (btn) -> {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.connection.sendCommand("plot invite");
            }
            // Здесь мы просто закрываем меню, чтобы игрок ввёл ник в чате (самый простой путь для старта)
            this.onClose();
        }).bounds(centerX - 100, centerY - 10, 200, 20).build());

        // Кнопка "Настройки" (пока просто заглушка)
        this.addRenderableWidget(Button.builder(Component.literal("Настройки участка"), (btn) -> {
            // Позже добавим сюда логику
            this.onClose();
        }).bounds(centerX - 100, centerY + 20, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // 1. Рисуем тёмный полупрозрачный фон (в стиле LuminousUI)
        graphics.fill(0, 0, this.width, this.height, 0xCC000000); // 0xCC = прозрачность ~80%

        // 2. Рисуем заголовок по центру
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);

        // 3. Рисуем кнопки (вызываем super.render)
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false; // Меню не ставит игру на паузу (важно для мультиплеера)
    }
}