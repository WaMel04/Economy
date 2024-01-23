package io.github.wamel04.economy.currency;

import io.github.wamel04.economy.Economy;
import io.github.wamel04.economy.logger.PlayerLogger;
import io.github.wamel04.economy.logger.ServerLogger;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class Currency {

    public static HashMap<String, Currency> currencyNameMap = new HashMap<>();

    String name = "";
    boolean isDefault = false;
    boolean tradable = true;
    Material material;
    double taxPercent = 0;
    List<String> description;

    HashMap<String, BigDecimal> balanceMap = new HashMap<>();

    ServerLogger logger;

    public Currency(String name, boolean isDefault, boolean tradable, Material material, double taxPercent, List<String> description) {
        this.name = name;
        this.isDefault = isDefault;
        this.tradable = tradable;
        this.material = material;
        this.taxPercent = taxPercent;
        this.description = description;

        this.logger = new ServerLogger(new File(Economy.getInstance().getDataFolder() + "/server_logs/" + name, "latest.txt"));
    }

    public String getName() {
        return name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isTradable() { return tradable; }

    public Material getMaterial() {
        return material;
    }

    public double getTaxPercent() {
        return taxPercent;
    }

    public List<String> getDescription() { return description; }

    public ServerLogger getLogger() {
        return logger;
    }

    public boolean isLoaded(String uuid) {
        return balanceMap.containsKey(uuid);
    }

    public CompletableFuture<BigDecimal> loadBalance(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File(Economy.getInstance().getDataFolder() + "/data", uuid + ".yml");

            if (!file.exists()) {
                balanceMap.put(uuid, BigDecimal.ZERO);
                return BigDecimal.ZERO;
            }

            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            if (!yaml.contains(name)) {
                balanceMap.put(uuid, BigDecimal.ZERO);
                return BigDecimal.ZERO;
            }

            BigDecimal balance = new BigDecimal(yaml.getDouble(name));
            balanceMap.put(uuid, balance);
            return balance;
        });
    }

    public CompletableFuture<Void> saveBalance(String uuid) {
        return CompletableFuture.runAsync(() -> {
            File file = new File(Economy.getInstance().getDataFolder() + "/data", uuid + ".yml");
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

            yaml.set(name, balanceMap.get(uuid).doubleValue());
            balanceMap.remove(uuid);

            try {
                yaml.save(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> saveAll() {
        List<CompletableFuture<Void>> saveFutures = new ArrayList<>();

        for (String uuid : balanceMap.keySet()) {
            CompletableFuture<Void> saveFuture = saveBalance(uuid);
            saveFutures.add(saveFuture);
        }

        CompletableFuture<Void>[] saveArray = saveFutures.toArray(new CompletableFuture[0]);

        return CompletableFuture.allOf(saveArray);
    }

    public BigDecimal getBalance(String uuid) {
        if (balanceMap.containsKey(uuid)) {
            return balanceMap.get(uuid);
        } else {
            CompletableFuture<BigDecimal> future = loadBalance(uuid);

            try {
                return future.get(3, TimeUnit.SECONDS);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public void addBalance(String uuid, double balance, boolean doLog) {
        if (balanceMap.containsKey(uuid)) {
            BigDecimal currentBalance = balanceMap.get(uuid);
            balanceMap.put(uuid, currentBalance.add(BigDecimal.valueOf(balance)));

            if (doLog) {
                log(uuid, "ADD_BALANCE", balance);
            }
        } else {
            loadBalance(uuid).thenRun(() -> {
                BigDecimal currentBalance = balanceMap.get(uuid);

                balanceMap.put(uuid, currentBalance.add(BigDecimal.valueOf(balance)));

                if (doLog) {
                    log(uuid, "ADD_BALANCE", balance);
                }
            });
        }
    }

    public boolean removeBalance(String uuid, double balance, boolean doLog) {
        if (balanceMap.containsKey(uuid)) {
            BigDecimal currentBalance = balanceMap.get(uuid);

            if (currentBalance.doubleValue() - balance < 0)
                return false;

            balanceMap.put(uuid, currentBalance.subtract(BigDecimal.valueOf(balance)));

            if (doLog) {
                log(uuid, "REMOVE_BALANCE", balance);
            }
            return true;
        } else {
            CompletableFuture<BigDecimal> future = loadBalance(uuid);

            try {
                BigDecimal currentBalance = future.get(3, TimeUnit.SECONDS);

                if (currentBalance.doubleValue() - balance < 0)
                    return false;

                balanceMap.put(uuid, currentBalance.subtract(BigDecimal.valueOf(balance)));

                if (doLog) {
                    log(uuid, "REMOVE_BALANCE", balance);
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public void setBalance(String uuid, double balance, boolean doLog) {
        balanceMap.put(uuid, BigDecimal.valueOf(balance));

        if (doLog) {
            log(uuid, "SET_BALANCE", balance);
        }
    }

    public void log(String uuid, String eventName, double balance) {
        CompletableFuture.runAsync(() -> {
            OfflinePlayer player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));

            String context = player.getName() + "(" + uuid + ") | " + eventName + " " + balance + " " + name + " : " + balanceMap.get(uuid);
            logger.log(context);
            PlayerLogger.log(uuid, context);
        });
    }

}
