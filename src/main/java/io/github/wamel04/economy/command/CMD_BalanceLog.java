package io.github.wamel04.economy.command;

import io.github.wamel04.economy.currency.Currency;
import io.github.wamel04.economy.logger.PlayerLogger;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CMD_BalanceLog implements TabExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.isOp())) {
            sender.sendMessage("§c권한이 부족합니다.");
            return false;
        }

        if (args.length < 2) {
            sender.sendMessage("§a/balancelog [닉네임] [화폐] [페이지] §7- 플레이어의 재화 변동 로그를 확인합니다.");
            return false;
        } else {
            String nickname = args[0];
            String currencyName = args[1].toLowerCase();
            int page = 1;

            if (args.length > 2) {
                try {
                    page = Integer.parseInt(args[2]);

                    if (page <= 0) throw new NumberFormatException();
                } catch (NumberFormatException e) {
                    sender.sendMessage("§c페이지의 포맷은 자연수입니다.");
                    return false;
                }
            }
            if (!Currency.currencyNameMap.containsKey(currencyName)) {
                sender.sendMessage("§e" + currencyName + " §a재화는 존재하지 않습니다.");
                return false;
            }

            Player target = Bukkit.getPlayer(nickname);

            if (target != null) {
                int finalPage = page;
                CompletableFuture.runAsync(() -> {
                    String uuid = target.getUniqueId().toString();
                    LinkedList<String> logs = PlayerLogger.getLogs();

                    sender.sendMessage("§2<===== " + target.getName() + "의 " + currencyName + " 로그 §a(" + finalPage + " 페이지) §2=====>");

                    for (String log : logs) {
                        sender.spigot().sendMessage(new MineDown(log).toComponent());
                    }

                    sender.sendMessage("");

                    BigDecimal balance = MCUEconomyAPI.getBalance(target.getUniqueId().toString(), currencyName);

                    if (balance == null)
                        balance = BigDecimal.ZERO;

                    DecimalFormat df = new DecimalFormat("#.###");
                    String formattedBalance = df.format(balance.doubleValue());

                    sender.sendMessage("§6" + target.getName() + "§a님의 §e" + currencyName + " 재화 §a보유량은 §e" + formattedBalance + "§a입니다.");

                    if (finalPage == 1) {
                        String tail = "[§a\\[다음 페이지\\]](run_command=/balancelog " + target.getName() + " " + currencyName + " " + (finalPage + 1) +
                                " show_text=§a클릭 시 다음 페이지로 넘어갑니다)";
                        sender.spigot().sendMessage(new MineDown(tail).toComponent());
                    } else {
                        String tail = "[§c[이전 페이지]](run_command=/balancelog " + target.getName() + " " + currencyName + " " + (finalPage - 1) +
                                " show_text=§c클릭 시 이전 페이지로 돌아갑니다) §7| " +
                                "[§a\\[다음 페이지\\]](run_command=/balancelog " + target.getName() + " " + currencyName + " " + (finalPage + 1) +
                                " show_text=§a클릭 시 다음 페이지로 넘어갑니다)";
                        sender.spigot().sendMessage(new MineDown(tail).toComponent());
                    }
                });
            } else {
                int finalPage = page;
                CompletableFuture.runAsync(() -> {
                    OfflinePlayer newTarget = Bukkit.getOfflinePlayer(nickname);

                    String uuid = newTarget.getUniqueId().toString();
                    LinkedList<String> logs = DatabaseUtil.getLog(newTarget.getName(), uuid, currencyName, finalPage);

                    sender.sendMessage("§2<===== " + newTarget.getName() + "의 " + currencyName + " 로그 §a(" + finalPage + " 페이지) §2=====>");

                    for (String log : logs) {
                        sender.spigot().sendMessage(new MineDown(log).toComponent());
                    }

                    sender.sendMessage("");

                    BigDecimal balance = MCUEconomyAPI.getBalance(newTarget.getUniqueId().toString(), currencyName);

                    if (balance == null)
                        balance = BigDecimal.ZERO;

                    DecimalFormat df = new DecimalFormat("#.###");
                    String formattedBalance = df.format(balance.doubleValue());

                    sender.sendMessage("§6" + newTarget.getName() + "§a님의 §e" + currencyName + " 재화 §a보유량은 §e" + formattedBalance + "§a입니다.");

                    if (finalPage == 1) {
                        String tail = "[§a\\[다음 페이지\\]](run_command=/balancelog " + newTarget.getName() + " " + currencyName + " " + (finalPage + 1) +
                                " show_text=§a클릭 시 다음 페이지로 넘어갑니다)";
                        sender.spigot().sendMessage(new MineDown(tail).toComponent());
                    } else {
                        String tail = "[§c[이전 페이지]](run_command=/balancelog " + newTarget.getName() + " " + currencyName + " " + (finalPage - 1) +
                                " show_text=§c클릭 시 이전 페이지로 돌아갑니다) §7| " +
                                "[§a\\[다음 페이지\\]](run_command=/balancelog " + newTarget.getName() + " " + currencyName + " " + (finalPage + 1) +
                                " show_text=§a클릭 시 다음 페이지로 넘어갑니다)";
                        sender.spigot().sendMessage(new MineDown(tail).toComponent());
                    }
                });
            }
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            List<String> matchedPlayers = new ArrayList<>();

            if (args[0].isEmpty()) {
                // 아무 입력이 없을 경우 모든 플레이어 반환
                matchedPlayers.addAll(DayCronizeAPI.getPlayerList());
            } else {
                // 입력된 문자열로 시작하는 플레이어 반환
                String searchInput = args[0].toLowerCase(); // 대소문자 구분 없이 검색하기 위해 소문자로 변환

                for (String player : DayCronizeAPI.getPlayerList()) {
                    String lowercasePlayer = player.toLowerCase(); // 대소문자 구분 없이 비교하기 위해 소문자로 변환
                    if (lowercasePlayer.startsWith(searchInput)) {
                        matchedPlayers.add(player);
                    }
                }
            }

            return matchedPlayers;
        } else if (args.length == 2){
            LinkedList<String> list = new LinkedList<>();

            for (String currencyName : Currency.currencyNameMap.keySet()) {
                list.add(currencyName);
            }

            return list;
        }
        return null;
    }

}
