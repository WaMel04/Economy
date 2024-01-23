package io.github.wamel04.economy.logger;

import io.github.wamel04.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class ServerLogger {

    public static String getDate() {
        LocalDate now = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        return formatter.format(now);
    }

    public static String getTime() {
        LocalTime now = LocalTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

        return formatter.format(now);
    }

    File serverLogFile;
    BukkitTask rotateTask;

    String currentDate = LocalDate.now().toString();

    public ServerLogger(File serverLogFile) {
        this.serverLogFile = serverLogFile;

        if (!serverLogFile.exists()) {
            serverLogFile.getParentFile().mkdirs();

            try {
                serverLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        this.rotateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Economy.getInstance(), () -> {
            if (!LocalDate.now().toString().equals(currentDate)) {
                checkAndRotateLogFile();
                currentDate = LocalDate.now().toString();
                Economy.getInstance().getLogger().info("날짜가 변경되어 서버 로그 파일을 새로 생성합니다...");
            }
        }, 20, 20);
    }

    public File getServerLogFile() {
        return serverLogFile;
    }

    public BukkitTask getRotateTask() {
        return rotateTask;
    }

    public void log(String context) {
        try {
            FileWriter fw = new FileWriter(serverLogFile, true);
            BufferedWriter bw = new BufferedWriter(fw);

            bw.write("[" + getDate() + "] [" + getTime() + "] " + context);
            bw.newLine();

            bw.flush();
            bw.close();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndRotateLogFile() {
        File logFolder = serverLogFile.getParentFile();
        File newLogFile = new File(logFolder, currentDate + ".txt");

        if (!newLogFile.exists()) {
            if (serverLogFile.exists()) {
                serverLogFile.renameTo(newLogFile);
            }

            resetLogFile(serverLogFile);
        }
    }

    private void resetLogFile(File logFile) {
        try {
            FileWriter writer = new FileWriter(logFile, false);
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
