package me.timlampen.cc.roulette;

import me.timlampen.cc.CC;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Primary on 9/3/2016.
 */
public class RouletteListener implements Listener {
    HashMap<UUID, RouletteType> betting = new HashMap<>();
    CC p;
    RouletteInv rInv;
    public RouletteListener(CC p, RouletteInv rInv){
        this.p = p;
        this.rInv = rInv;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player player = event.getPlayer();
        if(rInv.cache.containsKey(player.getName())){
            double amt = rInv.cache.get(player.getName());
            p.eco.depositPlayer(player, rInv.cache.get(player.getName()));
            rInv.cache.remove(player.getUniqueId());
            player.sendMessage(p.getCasinoConfig().getLoginWin(amt + ""));
        }
    }

    @EventHandler
    public void onInteract(InventoryClickEvent event){
        Player player = (Player)event.getWhoClicked();
        if(event.getClickedInventory()!=null && ChatColor.stripColor(event.getClickedInventory().getName()).contains("Roulette")){
            event.setCancelled(true);
            RouletteType type = rInv.getBetType(event.getSlot());
            if(type!=null){
                if(!rInv.isSpinning) {
                    player.closeInventory();
                    betting.put(player.getUniqueId(), type);
                    player.sendMessage(p.getCasinoConfig().getRoulettePlaceBet(ChatColor.valueOf(type.toString()) + type.toString().toLowerCase()));
                }
                else{
                    player.sendMessage(p.getErrorConfig().getRouletteSpinning());
                }
            }
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        Player player = event.getPlayer();
        if(betting.containsKey(player.getUniqueId())){
            event.setCancelled(true);
            String msg = event.getMessage().replace("$", "");
            if(msg.contains(".")){
                player.sendMessage(p.getErrorConfig().getNoDecimal());
                return;
            }
            if(rInv.isSpinning){
                player.sendMessage(p.getErrorConfig().getRouletteSpinning());
                betting.remove(player.getUniqueId());
            }
            try{
                Double.parseDouble(msg);
            }catch(NumberFormatException nfe){
                player.sendMessage(p.getErrorConfig().getNullNumber(msg));
                betting.remove(player.getUniqueId());
                return;
            }
            double amt = Math.round(Double.parseDouble(msg));
            RouletteType type = betting.get(player.getUniqueId());
            if(!rInv.hasBetName(type, player.getName())){
                if(p.eco.has(player, amt)){
                    p.eco.withdrawPlayer(player, amt);
                    rInv.addBetName(type, player, amt);
                    rInv.openInv(player);
                    player.sendMessage(p.getCasinoConfig().getRouletteCompleteBet(amt + "", ChatColor.valueOf(type.toString()) + type.toString()));
                    rInv.betters.add(player.getUniqueId());
                }
                else{
                    player.sendMessage(p.getErrorConfig().getNoFunds());
                }
            }
            else{
                player.sendMessage(p.getErrorConfig().getAlreadyBetColor());
            }
            betting.remove(player.getUniqueId());
        }
    }
}
