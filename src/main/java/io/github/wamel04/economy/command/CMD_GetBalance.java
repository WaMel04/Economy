package io.github.wamel04.economy.command;

import io.github.wamel04.economy.api.EconomyAPI;
import io.github.wamel04.economy.currency.Currency;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;

import javax.swing.plaf.ViewportUI;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class CMD_GetBalance implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§c권한이 부족합니다.");
            return false;
        }
        if (args.length < 2) {
            sender.sendMessage("§a/getbalance [닉네임] [화폐] §7- 재화 보유량을 확인합니다.");
            return false;
        }
        if (args.length == 2) {
            String nickname = args[0];
            String currencyName = args[1].toLowerCase();

            if (!Currency.currencyNameMap.containsKey(currencyName)) {
                sender.sendMessage("§e" + currencyName + " §a화폐는 존재하지 않습니다.");
                return false;
            }

            Player target = Bukkit.getPlayer(nickname);
            DecimalFormat df = new DecimalFormat("###,###");

            if (target != null) {
                try {
                    Double balance = EconomyAPI.getBalance(target.getUniqueId().toString(), currencyName).doubleValue();

                    sender.sendMessage("§6" + target.getName() + "§a님의 §e" + currencyName + " §a보유량은 §e" + df.format(balance) + "§a입니다.");
                } catch (Exception e) {
                    sender.sendMessage("§c오류 발생: " + e.getLocalizedMessage());
                }
            } else {
                CompletableFuture.runAsync(() -> {
                    OfflinePlayer newTarget = Bukkit.getOfflinePlayer(nickname);

                    try {
                        Double balance = EconomyAPI.getBalance(newTarget.getUniqueId().toString(), currencyName).doubleValue();

                        sender.sendMessage("§6" + newTarget.getName() + "§a님의 §e" + currencyName + " §a보유량은 §e" + df.format(balance) + "§a입니다.");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1) {
            List<String> result = new ArrayList<>();

            if (args[0].isEmpty()) {
                result.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(HumanEntity::getName)
                        .collect(Collectors.toList()));
            } else {
                String searchInput = args[0].toLowerCase();

                result.addAll(Bukkit.getOnlinePlayers().stream()
                        .map(HumanEntity::getName)
                        .filter(name -> name.toLowerCase().startsWith(searchInput))
                        .collect(Collectors.toList()));
            }

            return result;
        } else if (args.length == 2){
            List<String> result = new ArrayList<>();

            if (args[1].isEmpty()) {
                result.addAll(Currency.currencyNameMap.keySet().stream()
                        .map(String::toLowerCase)
                        .collect(Collectors.toList()));
            } else {
                String searchInput = args[1].toLowerCase();

                result.addAll(Currency.currencyNameMap.keySet().stream()
                        .filter(name -> name.toLowerCase().startsWith(searchInput))
                        .collect(Collectors.toList()));
            }

            return result;
        }

        return null;
    }

}
