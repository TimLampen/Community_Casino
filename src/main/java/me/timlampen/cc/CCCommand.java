package me.timlampen.cc;

import me.timlampen.cc.roulette.RouletteInv;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Primary on 9/3/2016.
 */
public class CCCommand implements CommandExecutor{
    CC p;
    public CCCommand(CC p){
        this.p = p;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (sender instanceof Player){
            Player player = (Player)sender;
            if(args.length>0){
                if(args[0].equalsIgnoreCase("roulette")){
                    p.rInv.openInv(player);
                }
                else if(args[0].equalsIgnoreCase("jackpot")){
                    p.jInv.openInv(player);
                }
            }
        }
        return false;
    }
}
