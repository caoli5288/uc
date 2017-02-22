package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.UltraPlayer;
import be.isach.ultracosmetics.command.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Sacha on 22/12/15.
 */
public class SubCommandClear extends SubCommand {

    public SubCommandClear() {
        super("Clears a Cosmetic.", "ultracosmetics.command.clear", "/uc clear <player> [type]", "clear");
    }

    @Override
    protected void onExePlayer(Player sender, String... args) {
        common(sender, args);
    }

    @Override
    protected void onExeConsole(ConsoleCommandSender sender, String... args) {
        common(sender, args);
    }

    private void common(CommandSender sender, String... args) {
        Player receiver;
        if (args.length < 2) {
            sender.sendMessage("§c§l  Incorrect Usage. " + getUsage());
            return;
        }

        if (!sender.hasPermission(getPermission() + ".others")) return;
        receiver = Bukkit.getPlayer(args[1]);

        if (receiver == null) {
            sender.sendMessage("  §c§lPlayer " + args[1] + " not found!");
            return;
        }
        if (args.length < 3) {
            Main.getPlayerManager().getUltraPlayer(receiver).clear();
            return;
        }

        UltraPlayer cp = Main.getPlayerManager().getUltraPlayer(receiver);
        String s = args[2].toLowerCase();

        if (s.startsWith("g")) cp.removeGadget();
        else if (s.startsWith("pa")) cp.removeParticleEffect();
        else if (s.startsWith("pe")) cp.removePet();
        else if (s.startsWith("h")) cp.removeHat();
        else if (s.startsWith("s")) cp.removeSuit();
        else if (s.startsWith("mor")) cp.removeMorph();
        else if (s.startsWith("mou")) cp.removeMount();
        else if (s.startsWith("e")) cp.removeEmote();
        else {
            sender.sendMessage("§c§l/uc menu <menu>\n§c§lInvalid Type.\n§c§lAvailable types: gadgets, particleeffects, pets, mounts, suits, hats, morphs");
        }
    }
}
