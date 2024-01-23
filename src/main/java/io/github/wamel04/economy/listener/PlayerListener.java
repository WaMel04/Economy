package io.github.wamel04.economy.listener;

import io.github.wamel04.economy.currency.Currency;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        for (Currency currency : Currency.currencyNameMap.values()) {
            currency.loadBalance(player.getUniqueId().toString());
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        for (Currency currency : Currency.currencyNameMap.values()) {
            currency.saveBalance(player.getUniqueId().toString());
        }
    }

}
