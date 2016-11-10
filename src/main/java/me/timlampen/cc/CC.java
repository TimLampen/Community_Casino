package me.timlampen.cc;

import me.timlampen.cc.jackpot.JackpotInv;
import me.timlampen.cc.roulette.RouletteInv;
import me.timlampen.cc.roulette.RouletteListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

/**
 * Created by Primary on 9/3/2016.
 */
public class CC extends JavaPlugin implements Listener{
    public String prefix = ChatColor.GRAY + "[" + ChatColor.GOLD + "CC" + ChatColor.GRAY + "] " + ChatColor.RESET;
   public RouletteInv rInv = null;
    JackpotInv jInv = null;
    RouletteListener rInvListener;
    CasinoConfig cConfig;
    ErrorConfig eConfig;
    public Economy eco;
    @Override
    public void onEnable(){
        File file = new File(getDataFolder(), "config.yml");
        if(!file.exists()){
            saveDefaultConfig();
        }
        Bukkit.getPluginManager().registerEvents(this, this);
        cConfig = new CasinoConfig(this);
        eConfig = new ErrorConfig(this);
        cConfig.loadConfig();
        eConfig.loadConfig();
        rInv = new RouletteInv(this);
        jInv = new JackpotInv(this);
        rInvListener = new RouletteListener(this, rInv);
        if(!setupEconomy()){
            Bukkit.getConsoleSender().sendMessage(prefix + ChatColor.RED + "Error: You do not have a economy plugin installed, disabling this plugin");
            getServer().getPluginManager().disablePlugin(this);
        }
        Bukkit.getPluginManager().registerEvents(rInvListener, this);
        getCommand("cc").setExecutor(new CCCommand(this));
        rInv.countdown();
        jInv.countdown();
    }

    @Override
    public void onDisable(){
        saveDefaultConfig();
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        eco = rsp.getProvider();
        return eco != null;
    }

    public ErrorConfig getErrorConfig(){
        return eConfig;
    }

    public CasinoConfig getCasinoConfig(){
        return cConfig;
    }

    public static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, new Comparator<Object>() {
            @SuppressWarnings("unchecked")
            public int compare(Object o1, Object o2) {
                return ((Comparable<V>) ((Map.Entry<K, V>) (o1)).getValue()).compareTo(((Map.Entry<K, V>) (o2)).getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<>();
        for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext();) {
            Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
