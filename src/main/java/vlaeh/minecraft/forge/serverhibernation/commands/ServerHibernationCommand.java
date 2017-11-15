package vlaeh.minecraft.forge.serverhibernation.commands;

import java.util.Collections;

import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;

import vlaeh.minecraft.forge.serverhibernation.ServerHibernation;

public class ServerHibernationCommand extends CommandBase {
	public static final String COMMAND = "serverHibernation";
	public static final String USAGE = "/" + COMMAND + " forcestart | forcestop | enabled [new_value]";
	public static final String NOT_OPPED = "You must be opped to use this command";

	@Override
	public String getName() {
		return "ServerHibernation";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return USAGE;
	}

	public void usage(final ICommandSender sender) {
        sender.sendMessage(new TextComponentString("Usage: " + USAGE).setStyle(new Style().setColor(TextFormatting.RED)));
	}
	
	public final boolean playerIsAllowed(final ICommandSender sender) {
        if ((! sender.getEntityWorld().isRemote) && (FMLCommonHandler.instance().getSide() == Side.CLIENT)) 
        	return true;
        if (! (sender instanceof EntityPlayer))
        	return true;
        for (String player : sender.getServer().getPlayerList().getOppedPlayerNames())
        	if (player.equals(sender.getName()))
                    return true;
        return false;
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender,
			String[] args) throws CommandException {
        if (args.length == 0 || args.length >= 3)
        {
        	usage(sender);
            return;
        }
        if (args.length == 1) {
        	if (args[0].equalsIgnoreCase("enabled"))
                sender.sendMessage(new TextComponentString("" + COMMAND + " " + args[0] + " " + TextFormatting.GREEN + ServerHibernation.hibernationEnabled));
        	else if (args[0].equalsIgnoreCase("forcestart")) { 
                ServerHibernation.proxy.startHibernation(true);
                ServerHibernation.syncConfig();
                sender.sendMessage(new TextComponentString("ServerHibernation " + ServerHibernation.proxy.getStatus()));
            } else if (args[0].equalsIgnoreCase("forcestop")) { 
                ServerHibernation.proxy.stopHibernation(true);
                ServerHibernation.syncConfig();
                sender.sendMessage(new TextComponentString("ServerHibernation " + ServerHibernation.proxy.getStatus()));
            } else
        		usage(sender);
        	return;
        }
        // args.length == 2
        if (! playerIsAllowed(sender)) {
        	sender.sendMessage(new TextComponentString(NOT_OPPED).setStyle(new Style().setColor(TextFormatting.RED)));
        	return;
        }
        if (args[0].equalsIgnoreCase("enabled")) { 
        	ServerHibernation.config.get(Configuration.CATEGORY_GENERAL, "1.enabled", ServerHibernation.hibernationEnabled).set(Boolean.parseBoolean(args[1]));
        	ServerHibernation.syncConfig();
        	sender.sendMessage(new TextComponentString("ServerHibernation enabled set to " + TextFormatting.GREEN + ServerHibernation.hibernationEnabled));
        } else
            sender.sendMessage(new TextComponentString("Invalid argument " + args[0] + " " + args[1]));
	}

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        if (args.length == 1)
        {
            return getListOfStringsMatchingLastWord(args, "enabled", "forcestart", "forcestop");
        }
        if (args[0].equalsIgnoreCase("enabled"))
        {
            return getListOfStringsMatchingLastWord(args, "true", "false");
        }
        return Collections.emptyList();
    }
}
