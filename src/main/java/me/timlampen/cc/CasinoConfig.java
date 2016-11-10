package me.timlampen.cc;

import me.timlampen.cc.CC;
import org.bukkit.ChatColor;

/**
 * Created by Primary on 9/4/2016.
 */
public class CasinoConfig {
    private String login_win, roulette_place_bet, roulette_complete_bet, casino_win;
    private int roulette_refresh_time;
    CC p;
    public CasinoConfig(CC p){
        this.p = p;
    }

    public void loadConfig(){
        login_win = p.getConfig().getString("casino.login_win");
        casino_win = p.getConfig().getString("casino.casino_win");
        roulette_refresh_time = p.getConfig().getInt("casino.roulette.refresh_time");
        roulette_place_bet = p.getConfig().getString("casino.roulette.place_bet");
        roulette_complete_bet = p.getConfig().getString("casino.roulette.complete_bet");
    }

    public String getLoginWin(String amt){
        return p.prefix + ChatColor.translateAlternateColorCodes('&', login_win.replace("%amt%", amt));
    }

    public String getRoulettePlaceBet(String color){
        return p.prefix +ChatColor.translateAlternateColorCodes('&', roulette_place_bet.replace("%color%", color));
    }

    public String getRouletteCompleteBet(String amt, String color){
        return p.prefix +ChatColor.translateAlternateColorCodes('&', roulette_complete_bet.replace("%amt%", amt).replace("%color%", color));
    }

    public String getCasinoWin(String amt){
        return p.prefix +ChatColor.translateAlternateColorCodes('&', casino_win.replace("%amt%", amt));
    }

    public int getRouletteRefreshTime(){
        return roulette_refresh_time;
    }
}
