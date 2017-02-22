package be.isach.ultracosmetics.command.subcommands;

import be.isach.ultracosmetics.Main;
import be.isach.ultracosmetics.UltraPlayer;
import be.isach.ultracosmetics.command.SubCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

/**
 * Created by Sacha on 20/12/15.
 */
public class SubCommandSelfView extends SubCommand {

    public SubCommandSelfView() {
        super("Toggle Morph Self View", "ultracosmetics.command.selfview", "/uc selfview", "selfview");
    }

    @Override
    protected void onExePlayer(Player sender, String... args) {
        UltraPlayer customPlayer = Main.getPlayerManager().getUltraPlayer(sender);
        customPlayer.setSeeSelfMorph(!customPlayer.canSeeSelfMorph());
    }

    @Override
    protected void onExeConsole(ConsoleCommandSender sender, String... args) {
        notAllowed(sender);
    }
}

