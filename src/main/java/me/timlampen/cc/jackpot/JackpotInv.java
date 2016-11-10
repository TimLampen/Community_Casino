package me.timlampen.cc.jackpot;

import me.timlampen.cc.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Skull;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigInteger;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Created by Primary on 9/4/2016.
 */
public class JackpotInv {
    CC p;
    HashMap<UUID, Double> cache = new HashMap<>();
    LinkedHashMap<UUID, JackpotRange> tickets = new LinkedHashMap<>();
    ArrayList<ItemStack> headItems = new ArrayList<>();
    int[] fills = new int[]{0, 1, 2, 3, 5, 6, 7, 8, 9, 17, 18, 19, 20, 22, 24, 25, 26};
    int[] heads = new int[]{10, 11, 12, 13, 14, 15, 16};
    public boolean isSpinning = false;
    double jackpot = 0;
    ItemStack deposit = null;
    ItemStack hopper = null;
    ItemStack cd = null;
    ItemStack filler = null;
    String title = ChatColor.DARK_GREEN + "Community Jackpot";
    Inventory inv;
    public JackpotInv(CC p){
        this.p = p;
        ItemMeta im;
        hopper = new ItemStack(Material.HOPPER, 1);
        im = hopper.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "↓ Winning Player ↓");
        hopper.setItemMeta(im);

        deposit = new ItemStack(Material.GOLD_INGOT);
        im = deposit.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "Current Jackpot: " + ChatColor.AQUA + "$0");
        im.setLore(Arrays.asList(new String[]{ChatColor.GOLD + "Click here to enter the jackpot"}));
        deposit.setItemMeta(im);

        cd = new ItemStack(Material.WATCH, 1);
        im = cd.getItemMeta();
        im.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "60s" + ChatColor.GOLD + " until the game begins");
        cd.setItemMeta(im);

        filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
        im = filler.getItemMeta();
        im.setDisplayName(" ");
        filler.setItemMeta(im);
        generateInv(false);

        ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (short)3);
        SkullMeta i = (SkullMeta)is.getItemMeta();
        i.setOwner("Server_Dev");
        is.setItemMeta(i);
        headItems.add(is);

        i.setOwner("JackedPenguin");
        is.setItemMeta(i);
        headItems.add(is);
    }
    Random ran = new Random();
    int seconds = 10;
    public void countdown(){
        new BukkitRunnable(){
            @Override
            public void run() {
                seconds--;
                if(seconds<0){
                    seconds = 10;
                    start();
                    this.cancel();
                }
                ItemStack clock = inv.getItem(23);
                ItemMeta im = clock.getItemMeta();
                im.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + seconds + "s" + ChatColor.GOLD + " until the game begins");
                clock.setItemMeta(im);

            }
        }.runTaskTimer(p, 0, 20);
    }

    long cDelay = 2;
    int cTick = 0;
    int fast = 0;
    public void start(){
        for(JackpotRange r : tickets.values()){
            ItemStack is = new ItemStack(Material.SKULL);
            SkullMeta meta = (SkullMeta)is.getItemMeta();
            meta.setOwner(r.name);
            meta.setDisplayName(ChatColor.GRAY + r.name);
            is.setItemMeta(meta);
            headItems.add(is);
        }

        final int delay = ran.nextInt(12)+22;
        final int fDelay = ran.nextInt(22)+30;
        int winningSlot = fDelay + 1 + (delay>=26 ? 9 : 8);
        setWinningHead(winningSlot%headItems.size());
        isSpinning = true;
        new BukkitRunnable() {
            @Override
            public void run() {
                fast++;
                if(fast>=fDelay){
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            cTick++;
                            if(cTick>=cDelay){
                                cTick=0;
                                cDelay = Math.round(Math.ceil(cDelay*1.3));
                                rotateHeads();
                            }
                            if(cDelay>=delay){
                                end();
                                fast = 0;
                                cDelay = 2;
                                cTick = 0;
                                this.cancel();
                            }
                        }
                    }.runTaskTimer(p, 1, 1);
                    this.cancel();
                }
                rotateHeads();
            }
        }.runTaskTimer(p, 0, 2);
    }

    public void openInv(Player player){
        player.openInventory(inv);
    }
    //somehow make the winTicket change the skull
    public void end(){
        if(tickets.size()>0) {
            double winTicket = (tickets.get(tickets.values().toArray()[tickets.size()-1]).max) * ran.nextDouble();
            UUID uuid = null;
            for(JackpotRange r : tickets.values()) {
                if (winTicket >= r.min && winTicket <= r.max) {
                    uuid = r.uuid;
                    break;
                }
            }
            if(Bukkit.getPlayer(uuid)!=null){
                Player player = Bukkit.getPlayer(uuid);
                p.eco.depositPlayer(player, jackpot);
                player.sendMessage(p.getCasinoConfig().getCasinoWin(jackpot + ""));
            }
            else{
                cache.put(uuid, jackpot);
            }
            jackpot = 0;
            isSpinning = false;
        }
        generateInv(true);
    }
    int cindex = 0;
    public void rotateHeads(){
        if(headItems.size()>0) {
            for (int i = 0; i < heads.length; i++) {
                int slot = heads[i];
                if (i == 0) {
                    if (cindex >= headItems.size()) {
                        cindex = 0;
                    }
                    inv.setItem(slot, headItems.get(cindex));
                } else if (!(i == heads.length - 1)) {
                    inv.setItem(slot, inv.getItem(slot-1));
                }
            }
        }
        for(HumanEntity e : inv.getViewers()){
            Player player = (Player)e;
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 10);
        }
    }

    public void setWinningHead(int slot){
        if(tickets.size()>0) {
            double winTicket = (tickets.get(tickets.values().toArray()[tickets.size()-1]).max) * ran.nextDouble();
            String s = "";
            for(JackpotRange r : tickets.values()) {
                if (winTicket >= r.min && winTicket <= r.max) {
                    s = r.name;
                }
            }
            ItemStack is = new ItemStack(Material.SKULL);
            SkullMeta meta = (SkullMeta)is.getItemMeta();
            meta.setOwner(s);
            meta.setDisplayName(ChatColor.GRAY + s);
            is.setItemMeta(meta);
            headItems.set(slot-1, is);
        }
    }
    public void addBet(Player player, double amt){
        if(tickets.size()>0){
            JackpotRange prevRange = (JackpotRange)tickets.values().toArray()[tickets.size()-1];
            tickets.put(player.getUniqueId(), new JackpotRange(player.getUniqueId(), player.getName(), prevRange.max+1, prevRange.max+amt));
        }
        else{
            tickets.put(player.getUniqueId(), new JackpotRange(player.getUniqueId(), player.getName(), 0, amt));
        }
        jackpot += amt;

        ItemStack is = inv.getItem(21);
        ItemMeta im = is.getItemMeta();
        List<String> lore = im.hasLore() ? im.getLore() : new ArrayList<String>();
        lore.clear();
        Map<String, Double> temp = new HashMap<>();
        temp.put(player.getName(), amt);
        for(JackpotRange r : tickets.values()){
            temp.put(r.name, r.max-r.min);
        }
        temp = p.sortByValue(temp);
        DecimalFormat df = new DecimalFormat("#.##");
        lore.add(ChatColor.GOLD + "Click here to enter the jackpot");
        for(String name : temp.keySet()){
            lore.add(ChatColor.GRAY + name + ": " + ChatColor.AQUA + df.format(temp.get(name)));
        }
        im.setLore(lore);
        is.setItemMeta(im);
    }

    public boolean hasBet(Player player){
        return tickets.containsKey(player.getUniqueId());
    }

    public void generateInv(boolean regen){
        if(!regen){
            Inventory inv = Bukkit.createInventory(null, 27, title);
            this.inv = inv;
        }
        for(int slot : fills){
            inv.setItem(slot, filler);
        }
        inv.setItem(21, deposit);
        inv.setItem(23, cd);
        inv.setItem(4, hopper);
        for(int slot : heads){
            inv.setItem(slot, new ItemStack(Material.SKULL_ITEM, 1, (short)2));
        }
    }
}
