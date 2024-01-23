package io.github.wamel04.economy;

import io.github.wamel04.economy.command.CMD_AddBalance;
import io.github.wamel04.economy.command.CMD_GetBalance;
import io.github.wamel04.economy.command.CMD_RemoveBalance;
import io.github.wamel04.economy.command.CMD_SetBalance;
import io.github.wamel04.economy.currency.Currency;
import io.github.wamel04.economy.currency.CurrencyConfig;
import io.github.wamel04.economy.listener.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public final class Economy extends JavaPlugin {

    private static Economy instance;

    @Override
    public void onEnable() {
        instance = this;

        CurrencyConfig.init();
        registerCommands();
        registerListeners();
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            for (Currency currency : Currency.currencyNameMap.values()) {
                currency.saveAll();
            }
        }, 20 * 60 * 60, 20 * 60 * 60);
    }

    @Override
    public void onDisable() {
        for (Currency currency : Currency.currencyNameMap.values()) {
            currency.saveAll();
        }
    }

    public static Economy getInstance() {
        return instance;
    }

    private void registerCommands() {
        getCommand("addbalance").setExecutor(new CMD_AddBalance());
        getCommand("removebalance").setExecutor(new CMD_RemoveBalance());
        getCommand("setbalance").setExecutor(new CMD_SetBalance());
        getCommand("getbalance").setExecutor(new CMD_GetBalance());
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(), instance);
    }

}
