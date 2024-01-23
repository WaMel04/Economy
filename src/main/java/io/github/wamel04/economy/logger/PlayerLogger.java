package io.github.wamel04.economy.logger;

import io.github.wamel04.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
import java.util.HashMap;

public class PlayerLogger {

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

    public static void log(String uuid, String context) {
        File playerLogFile = new File(Economy.getInstance().getDataFolder() + "/player_logs/" + uuid, "latest.txt");

        if (!playerLogFile.exists()) {
            playerLogFile.getParentFile().mkdirs();

            try {
                playerLogFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter fw = new FileWriter(playerLogFile, true);
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

}
