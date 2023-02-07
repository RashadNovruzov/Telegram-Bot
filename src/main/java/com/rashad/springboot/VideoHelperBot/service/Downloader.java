package com.rashad.springboot.VideoHelperBot.service;


import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;


import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;

@Service
public class Downloader {

    public File youtubeDownloader(String url, Update update){
        String name = "video"+System.currentTimeMillis()+".mp4";
        File file = new File(name);

        CommandLine cmdLine = CommandLine.parse("youtube-dl -o "+name+" -f best " + url);
        DefaultExecutor executor = new DefaultExecutor();
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
        }finally {
            return file;
        }

    }

    public File mp3Downloader(String url, Update update){
        String name = "audio"+System.currentTimeMillis()+".mp3";
        File file = new File(name);
        CommandLine cmdLine = CommandLine.parse("youtube-dl -x --audio-format mp3 -o "+name+" "+url);
        DefaultExecutor executor = new DefaultExecutor();
        try {
            executor.execute(cmdLine);
        } catch (IOException e) {
        }finally {
            return file;
        }

    }

}
