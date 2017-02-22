package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.UltraPlayer;
import be.isach.ultracosmetics.command.SubCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Sacha on 20/12/15.
 */
public class SubCommandGadgets extends SubCommand {


    public SubCommandGadgets() {
        super("Toggle Gadgets", "ultracosmetics.command.gadgets", "/uc gadgets", "gadgets");
    }

    @Override
    protected void onExePlayer(Player sender, String... args) {
        UltraPlayer customPlayer = Main.getPlayerManager().getUltraPlayer(sender);
        customPlayer.setGadgetsEnabled(!customPlayer.hasGadgetsEnabled());
    }

    @Override
    protected void onExeConsole(ConsoleCommandSender sender, String... args) {
        notAllowed(sender);
    }
}
