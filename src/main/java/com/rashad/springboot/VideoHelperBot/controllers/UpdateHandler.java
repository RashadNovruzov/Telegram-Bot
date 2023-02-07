package com.rashad.springboot.VideoHelperBot.controllers;

import com.rashad.springboot.VideoHelperBot.service.Downloader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
public class UpdateHandler {
    private static int count;
    private static String func;
    private Downloader downloader;
    private TelegramBot telegramBot;

    private static final int THREAD_POOL_SIZE = 10;
    private static final int QUEUE_SIZE = 100;
    private static final ExecutorService EXECUTOR_SERVICE = new ThreadPoolExecutor(THREAD_POOL_SIZE, THREAD_POOL_SIZE, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(QUEUE_SIZE));


    @Autowired
    public UpdateHandler(Downloader downloader) {
        this.downloader = downloader;
    }

    public void handeUpdate(Update update){
        EXECUTOR_SERVICE.execute(() -> {
            Message message = update.getMessage();
            if(message!=null && message.hasText()){
                if(message.getText().equals("/start")){
                    startMessageHandler(update);
                } else if (message.getText().equals("Download video from youtube") || message.getText().equals("Download audio of youtube video")) {
                    count = 1;
                    func = message.getText();
                    downloadHandler(update);
                } else if ((message.getText().startsWith("http://www.youtube.com/watch?v=")||message.getText().startsWith("https://www.youtube.com/watch?v=")||message.getText().startsWith("https://youtu.be/")) && count==1) {
                    if(func.equals("Download video from youtube")){
                        waitMessage(update);
                        count = 0;
                        videoDownloader(update);
                        successMessage(update);
                    }else {
                        waitMessage(update);
                        count=0;
                        mp3Downloader(update);
                        successMessage(update);
                    }
                }else {
                    errorMessageHandler(update);
                }
            }else {
                errorMessageHandler(update);
            }
        });


    }

    private void waitMessage(Update update) {
        SendMessage sendMessage = messages("Please wait, we are handling your request...",update);
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
        }
    }

    private void successMessage(Update update) {
        SendMessage sendMessage = messages("Thank you for selecting us!!",update);
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
        }
    }

    private void errorMessageHandler(Update update) {
        SendMessage sendMessage = messages("Unregistered command",update);
        sendMessage.setReplyMarkup(getKeyboard());
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
        }
    }

    private SendMessage messages(String text,Update update){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText(text);
        return sendMessage;
    }
    private void mp3Downloader(Update update) {
        SendAudio sendAudio = new SendAudio();
        sendAudio.setChatId(update.getMessage().getChatId());
        File audioFile = downloader.mp3Downloader(update.getMessage().getText(),update);
        sendAudio.setAudio(new InputFile(audioFile));
        try {
            telegramBot.execute(sendAudio);
        } catch (TelegramApiException e) {
        }
        audioFile.delete();
    }

    private void videoDownloader(Update update) {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(update.getMessage().getChatId());
        File videoFile = downloader.youtubeDownloader(update.getMessage().getText(),update);
        sendVideo.setVideo(new InputFile(videoFile));
        try {
            telegramBot.execute(sendVideo);
        } catch (TelegramApiException e) {
        }
        videoFile.delete();
    }

    private void downloadHandler(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Send me link of youtube video :)");
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
        }
    }


    private void startMessageHandler(Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(update.getMessage().getChatId());
        sendMessage.setText("Select one of the options :-)");
        sendMessage.setReplyMarkup(getKeyboard());
        try {
            telegramBot.execute(sendMessage);
        } catch (TelegramApiException e) {
        }
    }

    private ReplyKeyboardMarkup getKeyboard() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(setButtons());
        return replyKeyboardMarkup;
    }

    private List<KeyboardRow> setButtons() {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow keyboardRow = new KeyboardRow();
        KeyboardButton keyboardButton = new KeyboardButton("Download video from youtube");
        keyboardRow.add(keyboardButton);
        rows.add(keyboardRow);
        keyboardRow = new KeyboardRow();
        KeyboardButton keyboardButton1 = new KeyboardButton("Download audio of youtube video");
        keyboardRow.add(keyboardButton1);
        rows.add(keyboardRow);
        return rows;
    }
    public void setTelegramBot(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public TelegramBot getTelegramBot(){
        return telegramBot;
    }
}
