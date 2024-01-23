package io.github.wamel04.economy.api;

import io.github.wamel04.economy.currency.Currency;

import java.math.BigDecimal;

public class EconomyAPI {

    public static void addBalance(String uuid, String currencyName, double balance) throws Exception {
        if (!Currency.currencyNameMap.containsKey(currencyName))
            throw new Exception(currencyName + "은 존재하지 않는 화폐입니다.");

        Currency currency = Currency.currencyNameMap.get(currencyName);
        currency.addBalance(uuid, balance, true);
    }

    public static boolean removeBalance(String uuid, String currencyName, double balance) throws Exception {
        if (!Currency.currencyNameMap.containsKey(currencyName))
            throw new Exception(currencyName + "은 존재하지 않는 화폐입니다.");

        Currency currency = Currency.currencyNameMap.get(currencyName);
        return currency.removeBalance(uuid, balance, true);
    }

    public static void setBalance(String uuid, String currencyName, double balance) throws Exception {
        if (!Currency.currencyNameMap.containsKey(currencyName))
            throw new Exception(currencyName + "은 존재하지 않는 화폐입니다.");

        Currency currency = Currency.currencyNameMap.get(currencyName);
        currency.setBalance(uuid, balance, true);
    }

    public static BigDecimal getBalance(String uuid, String currencyName) throws Exception {
        if (!Currency.currencyNameMap.containsKey(currencyName))
            throw new Exception(currencyName + "은 존재하지 않는 화폐입니다.");

        Currency currency = Currency.currencyNameMap.get(currencyName);
        return currency.getBalance(uuid);
    }

}
