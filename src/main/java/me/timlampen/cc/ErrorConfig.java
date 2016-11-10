package me.timlampen.cc;


import org.bukkit.ChatColor;

/**
 * Created by Primary on 9/4/2016.
 */
public class ErrorConfig {

    CC p;
    private String no_decimals, roulette_spinning, null_number, no_funds, already_bet_color;
    public ErrorConfig(CC p){
        this.p = p;
    }

    public void loadConfig(){
        no_decimals = p.getConfig().getString("error.no_decimals");
        roulette_spinning = p.getConfig().getString("error.roulette_spinning");
        null_number = p.getConfig().getString("error.null_number");
        no_funds = p.getConfig().getString("error.no_funds");
        already_bet_color = p.getConfig().getString("error.already_bet_color");
    }

    public String getNoDecimal(){
        return p.prefix +ChatColor.translateAlternateColorCodes('&', no_decimals);
    }

    public String getRouletteSpinning(){
        return p.prefix + ChatColor.translateAlternateColorCodes('&', roulette_spinning);
    }

    public String getNullNumber(String num){
        return p.prefix + ChatColor.translateAlternateColorCodes('&', null_number.replace("%num%", num));
    }

    public String getNoFunds(){
        return p.prefix + ChatColor.translateAlternateColorCodes('&', no_funds);
    }

    public String getAlreadyBetColor(){
        return p.prefix + ChatColor.translateAlternateColorCodes('&', already_bet_color);
    }
}
