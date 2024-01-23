package io.github.wamel04.economy.currency;

import io.github.wamel04.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

public class CurrencyConfig {

    private static final Economy plugin = Economy.getInstance();

    private static final String CONFIG_NAME = "currency.yml";

    private static void create() {
        try {
            File dataFolder = plugin.getDataFolder();

            if (!dataFolder.exists()) {
                dataFolder.mkdir();
            }

            File configFile = new File(dataFolder, CONFIG_NAME);

            if (!configFile.exists()) {
                try (FileOutputStream outputStream = new FileOutputStream(configFile);
                     InputStream in = plugin.getResource(CONFIG_NAME)) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = in.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    plugin.getLogger().info(CONFIG_NAME + "을 생성했습니다.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void init() {
        create();

        Currency.currencyNameMap.clear();

        File file = new File(Economy.getInstance().getDataFolder(), "currency.yml");
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = yaml.getConfigurationSection("currencies");

        for (String currencyName : section.getKeys(false)) {
            ConfigurationSection currencySection = section.getConfigurationSection(currencyName);

            boolean isDefault = currencySection.getBoolean("default");
            boolean isTradable = currencySection.getBoolean("tradable");

            Material material;
            try {
                material = Material.getMaterial(currencySection.getString("material"));
            } catch (Exception e) {
                plugin.getLogger().info(currencySection.getString("material") + "은 존재하지 않는 아이템 타입입니다. " + currencyName + " 로드에 실패하였습니다.");
                continue;
            }

            int taxPercent = currencySection.getInt("tax-percent");
            List<String> description = currencySection.getStringList("description").stream()
                    .map(des -> des.replace("&", "§"))
                    .collect(Collectors.toList());

            Currency currency = new Currency(currencyName, isDefault, isTradable, material, taxPercent, description);
            Currency.currencyNameMap.put(currencyName, currency);
        }
    }
}
