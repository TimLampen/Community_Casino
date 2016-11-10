package me.timlampen.cc.roulette;

import me.timlampen.cc.CC;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Created by Primary on 9/3/2016.
 */
public class RouletteInv {
    ArrayList<UUID> betters = new ArrayList<>();
    ArrayList<RouletteType> pastRounds = new ArrayList<RouletteType>();
    HashMap<String, Double> cache = new HashMap<String, Double>();
    int fills[] = new int[]{10, 11, 12, 14, 15, 16, 19, 20, 21, 23, 24, 25, 36, 37, 38, 42, 43, 44};
    int[] slots = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 26, 35, 34, 33, 32, 31, 30, 29, 28, 27, 18, 9};
    short[] order = new short[]{15, 14, 15, 14, 5, 15, 14, 15, 14, 15, 14, 15, 14, 15, 14, 5, 15, 14 , 15, 14, 15, 14};
    String title = "";
    CC p;
    ItemStack red = null;
    ItemStack black = null;
    ItemStack green = null;
    ItemStack hopper = null;
    ItemStack filler = null;
    ItemStack cd = null;
    ItemStack history = null;
    Inventory inv = null;
    boolean isSpinning = false;
    int seconds;
    public RouletteInv(CC p){
        this.p = p;
        seconds = p.getCasinoConfig().getRouletteRefreshTime();
        int counter = 0;
        for(char c : "Community Roulette".toCharArray()){
            counter++;
            title += (counter%2==0 ? ChatColor.RED : ChatColor.BLACK) + "" + c;
        }
        red = new ItemStack(Material.STAINED_CLAY, 1, (short)14);
        ItemMeta im = red.getItemMeta();
        im.setDisplayName(ChatColor.RED + "Current Red Bets " + ChatColor.GOLD + "(Click To Place A Bet)");
        red.setItemMeta(im);

        black = new ItemStack(Material.STAINED_CLAY, 1, (short)15);
        im = black.getItemMeta();
        im.setDisplayName(ChatColor.BLACK + "Current Black Bets " + ChatColor.GOLD + "(Click To Place A Bet)");
        black.setItemMeta(im);

        green = new ItemStack(Material.STAINED_CLAY, 1, (short)5);
        im = green.getItemMeta();
        im.setDisplayName(ChatColor.GREEN + "Current Green Bets " + ChatColor.GOLD + "(Click To Place A Bet)");
        green.setItemMeta(im);

        hopper = new ItemStack(Material.HOPPER, 1);
        im = hopper.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + "↓ Winning Color ↓");
        hopper.setItemMeta(im);

        filler = new ItemStack(Material.STAINED_GLASS_PANE, 1, (short)7);
        im = filler.getItemMeta();
        im.setDisplayName(" ");
        filler.setItemMeta(im);

        cd = new ItemStack(Material.WATCH, 1);
        im = cd.getItemMeta();
        im.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "60s" + ChatColor.GOLD + " until the game begins");
        cd.setItemMeta(im);

        history = new ItemStack(Material.STAINED_CLAY);
        im = history.getItemMeta();
        im.setDisplayName(ChatColor.GOLD + "Previous Rounds (Recent -> Old)");
        history.setItemMeta(im);
        generateInv(false);
    }

    public void openInv(Player p){
        p.openInventory(inv);
    }
    public void countdown(){
        new BukkitRunnable(){
            @Override
            public void run() {
                seconds--;
                if(seconds<0){
                    seconds = p.getCasinoConfig().getRouletteRefreshTime();
                    start();
                    this.cancel();
                }
                ItemStack clock = inv.getItem(13);
                ItemMeta im = clock.getItemMeta();
                im.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + seconds + "s" + ChatColor.GOLD + " until the game begins");
                clock.setItemMeta(im);

            }
        }.runTaskTimer(p, 0, 20);
    }

    Random ran = new Random();
    long cDelay = 2;
    int cTick = 0;
    int fast = 0;
    public void start(){
        final long delay = ran.nextInt(12)+23;
        final long fDelay = ran.nextInt(23)+22;
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
                                rotateWool();
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
                rotateWool();
            }
        }.runTaskTimer(p, 0, 2);
    }
    int flashes = 0;
    boolean flash = false;
    public void end(){
       final ArrayList<UUID> winners = new ArrayList<>();
        final short winNum = inv.getItem(31).getDurability();
        ItemStack is = winNum==14 ? inv.getItem(getBetSlot(RouletteType.RED)) : (winNum==5 ? inv.getItem(getBetSlot(RouletteType.GREEN)) : inv.getItem(getBetSlot(RouletteType.BLACK)));
        pastRounds.add(winNum==14 ? RouletteType.RED : (winNum==15 ? RouletteType.BLACK : RouletteType.GREEN));
        if(is.getItemMeta().hasLore()) {
            List<String> lore = is.getItemMeta().getLore();
            for (String s : lore) {
                s = ChatColor.stripColor(s);
                String[] split = s.split(":");
                String name = split[0];
                Double amt = Double.parseDouble(split[1].replace(" $", "")) * (winNum == 5 ? 10 : 2);
                if (Bukkit.getPlayer(name) != null) {
                    Player target = Bukkit.getPlayer(name);
                    p.eco.depositPlayer(target, amt);
                    winners.add(target.getUniqueId());
                    target.sendMessage(p.getCasinoConfig().getCasinoWin(amt + ""));
                } else {
                    cache.put(name, amt);
                }
            }
        }
        new BukkitRunnable(){
            @Override
            public void run() {
                flash = !flash;
                flashes++;
                for(HumanEntity e : inv.getViewers()) {
                    if (winners.contains(e.getUniqueId()) || !betters.contains(e.getUniqueId())) {
                        ((Player) e).playSound(e.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 10);
                    }
                    else{
                        ((Player)e).playSound(e.getLocation(), Sound.BLOCK_FIRE_EXTINGUISH, 1, 10);
                    }
                }
                if (flash) {
                    for (int i : fills) {
                        if(!(inv.getItem(i).getType()==Material.STAINED_GLASS)) {
                            inv.getItem(i).setDurability(winNum);
                        }
                    }
                } else {
                    for (int i : fills) {
                        if(!(inv.getItem(i).getType()==Material.STAINED_GLASS)) {
                            inv.getItem(i).setDurability((short) 7);
                        }
                    }
                }
                if (flashes >= 11) {
                    flashes = 0;
                    flash = false;
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            betters.clear();
                            generateInv(true);
                            isSpinning = false;
                            countdown();
                        }
                    }.runTaskLater(p, 100);

                    this.cancel();
                }
            }
        }.runTaskTimer(p, 0, 6);
    }

    public void rotateWool(){
        for (int i = 0; i < slots.length; i++) {
            if (i == (slots.length - 1)) {
                inv.setItem(slots[i], (inv.getItem(slots[10]).getDurability() == 5 ? getRouletteItem((short) 5) : (inv.getItem(slots[i]).getDurability() == 14 ? getRouletteItem((short) 15) : getRouletteItem((short) 14))));
            } else {
                inv.setItem(slots[i], inv.getItem(slots[i + 1]));
            }
        }

        for(HumanEntity e : inv.getViewers()){
            Player player = (Player)e;
            player.playSound(player.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 1, 10);
        }
    }

    public void generateInv(boolean regen){

        if(!regen) {
            Inventory inv = Bukkit.createInventory(null, 54, title);
            for (int i = 0; i < slots.length; i++) {
                int slot = slots[i];
                inv.setItem(slot, getRouletteItem(order[i]));
            }
            this.inv = inv;

            for(int i = 46; i < 54; i++){
                inv.setItem(i, new ItemStack(Material.STAINED_GLASS));
            }
        }

        inv.setItem(39, red);
        inv.setItem(40, green);
        inv.setItem(41, black);
        inv.setItem(22, hopper);
        inv.setItem(13, cd);
        inv.setItem(45, history);
        for(int i : fills){
            inv.setItem(i, filler);
        }
        int slt = 46;
        if(pastRounds.size()>=9){
            pastRounds.remove(0);
        }
        Collections.reverse(pastRounds);
        for(RouletteType type: pastRounds){
            inv.setItem(slt, constructHistoryItem(type));
            slt++;
        }
        Collections.reverse(pastRounds);
    }

    public ItemStack constructHistoryItem(RouletteType type){
        ItemStack is = new ItemStack(Material.STAINED_GLASS);
        switch (type){
            case RED:
                is.setDurability((short)14);
                break;
            case BLACK:
                is.setDurability((short)15);
                break;
            case GREEN:
                is.setDurability((short)5);
                break;
        }
        ItemMeta im = is.getItemMeta();
        im.setDisplayName(" ");
        is.setItemMeta(im);
        return is;
    }

    public ItemStack getRouletteItem(short data){
        ItemStack is = new ItemStack(Material.WOOL, 1, data);
        ItemMeta im = is.getItemMeta();
        switch (data){
            case 14:
                im.setDisplayName(ChatColor.RED + "Red " + ChatColor.GOLD + "(x2)");
                break;
            case 15:
                im.setDisplayName(ChatColor.BLACK + "Black"  + ChatColor.GOLD + "(x2)");
                break;
            case 5:
                im.setDisplayName(ChatColor.GREEN + "Green "  + ChatColor.GOLD + "" + ChatColor.BOLD + "(x10)");
                break;
        }
        is.setItemMeta(im);
        return is;
    }

    public int getBetSlot(RouletteType type){
        int slot = 0;
        switch (type){
            case RED:
                slot = 39;
                break;
            case GREEN:
                slot = 40;
                break;
            case BLACK:
                slot = 41;
                break;
        }
        return slot;
    }

    public RouletteType getBetType(int slot){
        switch (slot){
            case 39:
                return RouletteType.RED;
            case 40:
                return RouletteType.GREEN;
            case 41:
                return RouletteType.BLACK;
        }
        return null;
    }

    public boolean addBetName(RouletteType type, Player player, double bet){
        //38 39 40 43
        int slot = getBetSlot(type);
        ItemStack item = inv.getItem(slot);
        ItemMeta im = item.getItemMeta();
        List<String> oldLore = im.hasLore() ? im.getLore() : new ArrayList<String>();
        oldLore.add(ChatColor.GRAY + player.getName() + ": " + ChatColor.AQUA + "$" + bet);
        Map<String, Double> map = new HashMap<>();

        for(String s : oldLore){
            s = ChatColor.stripColor(s);
            Bukkit.getConsoleSender().sendMessage(s);
            String[] split = s.split(":");
            map.put(split[0], Double.parseDouble(split[1].replace(" $", "")));
        }
        map = p.sortByValue(map);
        oldLore.clear();
        for(String s  : map.keySet()){
            oldLore.add(ChatColor.GRAY + s + ": " + ChatColor.AQUA + "$" + map.get(s));
        }
        Collections.reverse(oldLore);
        im.setLore(oldLore);
        item.setItemMeta(im);
        return true;
       // player.updateInventory();
    }

    public boolean hasBetName(RouletteType type, String name){
        int slot = getBetSlot(type);
        ItemStack item = inv.getItem(slot);
        ItemMeta im = item.getItemMeta();
        if(im.hasLore()){
            for(String s : im.getLore()){
                if(ChatColor.stripColor(s).contains(name)){
                    return true;
                }
            }
        }
        return false;
    }
}
