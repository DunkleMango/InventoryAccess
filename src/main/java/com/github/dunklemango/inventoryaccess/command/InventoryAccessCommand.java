package com.github.dunklemango.inventoryaccess.command;

import com.github.dunklemango.inventoryaccess.InventoryAccessPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class InventoryAccessCommand implements CommandCallable {
    private final Logger logger = LoggerFactory.getLogger(InventoryAccessCommand.class);
    private boolean loggingEnabled = false;

    @Override @NonnullByDefault @Nonnull
    public CommandResult process(CommandSource source, String arguments) {
        if (!(source instanceof Player)) {
            sendMessage(source, Text.of("Inventories can not be opened from a console."));
            return CommandResult.empty();
        }
        final Player player = (Player) source;
        final List<String> args = getParsedArguments(arguments);

        // (Player) /invacc
        if (arguments.isEmpty()) {
            sendMessage(source, Text.of("You need to provide the correct number of arguments."));
            sendMessage(source, getUsage(source));
            return CommandResult.empty();
        }
        if (loggingEnabled) {
            logger.debug("Processed arguments are: " + args);
        }

        // Check flags: -i -e
        String flag = args.get(0);
        boolean isOpenInventory = flag.equals(InventoryAccessFlag.FLAG_OPEN_PLAYER_INVENTORY.flag);
        boolean isOpenEnderChest = flag.equals(InventoryAccessFlag.FLAG_OPEN_ENDER_CHEST.flag);
        if (isOpenInventory || isOpenEnderChest) {
            // Common subroutine for finding the target player
            if (args.size() != 2) {
                sendMessage(source, Text.of("You need to provide the correct number of arguments."));
                sendMessage(source, getUsage(source));
                return CommandResult.empty();
            }
            String targetPlayerName = args.get(1).replaceAll("\"", "");
            Optional<Player> targetPlayerOptional = getOnlinePlayer(targetPlayerName);
            if (!targetPlayerOptional.isPresent()) {
                sendMessage(source, Text.of("The specified player-target is not online."));
                return CommandResult.empty();
            }
            Player targetPlayer = targetPlayerOptional.get();

            // Open inventory of target player
            Inventory targetInventory;
            if (isOpenInventory) {
                targetInventory = targetPlayer.getInventory();
                player.openInventory(targetInventory, Text.of(targetPlayerName + "'s inventory"));
                sendMessage(source, Text.of(String.format(
                        "Successfully opened the inventory of player \"%s\".", targetPlayerName)));
            } else {
                targetInventory = targetPlayer.getEnderChestInventory();
                player.openInventory(targetInventory, Text.of(targetPlayerName + "'s ender chest"));
                sendMessage(source, Text.of(String.format(
                        "Successfully opened the ender chest of player \"%s\".", targetPlayerName)));
            }
            return CommandResult.success();
        } else {
            sendMessage(source, Text.of("The provided flag is unknown."));
            sendMessage(source, getUsage(source));
            return CommandResult.empty();
        }
    }

    @Override @NonnullByDefault @Nonnull
    public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) {
        List<String> suggestions = new ArrayList<>();
        final List<String> args = getParsedArguments(arguments);
        if (args.isEmpty() || (args.size() == 1 && args.get(0).equals("-"))) {
            // Suggest flags
            suggestions.add(InventoryAccessFlag.FLAG_OPEN_PLAYER_INVENTORY.flag);
            suggestions.add(InventoryAccessFlag.FLAG_OPEN_ENDER_CHEST.flag);
            suggestions.add(InventoryAccessFlag.FLAG_OPEN_ARMOR_INVENTORY.flag);
        } else if (args.size() == 1) {
            // Suggest player-names
            Collection<Player> onlinePlayers = Sponge.getServer().getOnlinePlayers();
            for (Player player : onlinePlayers) {
                suggestions.add(player.getName());
            }
        } else if (args.size() == 2) {
            // Suggest player-names but filter based on characters already entered
            Collection<Player> onlinePlayers = Sponge.getServer().getOnlinePlayers();
            String incompletePlayerName = args.get(1).replaceAll("\"", "");
            Stream<Player> filteredPlayers = onlinePlayers.stream()
                    .filter(player -> player.getName().startsWith(incompletePlayerName));
            filteredPlayers.forEach(player ->
                    suggestions.add(player.getName().replaceFirst(incompletePlayerName, "")));
        }
        return suggestions;
    }

    @Override @NonnullByDefault
    public boolean testPermission(CommandSource source) {
        return source.hasPermission(InventoryAccessPlugin.PLUGIN_ID + ".command.invacc");
    }

    @Override @NonnullByDefault @Nonnull
    public Optional<Text> getShortDescription(CommandSource source) {
        return Optional.of(Text.of("Used to open personal inventories of other players" +
                "such as their inventory or their ender chest"));
    }

    @Override @NonnullByDefault @Nonnull
    public Optional<Text> getHelp(CommandSource source) {
        return Optional.of(getUsage(source));
    }

    @Override @NonnullByDefault @Nonnull
    public Text getUsage(CommandSource source) {
        return Text.of(String.format("usage: invacc %s <player> | %s <player>",
                InventoryAccessFlag.FLAG_OPEN_PLAYER_INVENTORY.flag,
                InventoryAccessFlag.FLAG_OPEN_ENDER_CHEST.flag));
    }

    private void sendMessage(CommandSource source, Text message) {
        if (loggingEnabled) {
            logger.debug(message.toString());
        }
        source.sendMessage(message);
    }

    public void enableLogging(boolean enable) {
        this.loggingEnabled = enable;
    }

    private Optional<Player> getOnlinePlayer(String targetPlayerName) {
        Optional<Player> targetPlayerOptional = Optional.empty();
        for (Player otherPlayer : Sponge.getServer().getOnlinePlayers()) {
            if (otherPlayer.getName().equals(targetPlayerName)) {
                targetPlayerOptional = Optional.of(otherPlayer);
                break;
            }
        }
        return targetPlayerOptional;
    }

    private List<String> getParsedArguments(String arguments) {
        Pattern pattern = Pattern.compile(CommandRegex.REGEX_SPLIT_QUOTES_OR_WHITESPACE.regex);
        Matcher matcher = pattern.matcher(arguments);
        List<String> args = new ArrayList<>();
        while (matcher.find()) {
            args.add(matcher.group());
        }
        return args;
    }
}
