package io.github.dunklemango.inventoryaccess.command;

import io.github.dunklemango.inventoryaccess.InventoryAccessFlags;
import io.github.dunklemango.inventoryaccess.InventoryAccessPlugin;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.equipment.EquipmentType;
import org.spongepowered.api.item.inventory.equipment.EquipmentTypes;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.SlotIndex;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.query.QueryOperationTypes;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.plugin.meta.util.NonnullByDefault;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class InventoryAccessCommand implements CommandCallable {
    private final Logger logger = LoggerFactory.getLogger(InventoryAccessCommand.class);
    private boolean loggingEnabled = false;
    private static final Map<EquipmentType, SlotIndex> equipmentSlotMap
            = ImmutableMap.<EquipmentType, SlotIndex>builder()
            .put(EquipmentTypes.HEADWEAR, SlotIndex.of(0))
            .put(EquipmentTypes.CHESTPLATE, SlotIndex.of(1))
            .put(EquipmentTypes.LEGGINGS, SlotIndex.of(2))
            .put(EquipmentTypes.BOOTS, SlotIndex.of(3))
            .put(EquipmentTypes.OFF_HAND, SlotIndex.of(4))
            .build();

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

        // Check flags: -i -e -a
        String flag = args.get(0);
        boolean isOpenPlayerInventory = flag.equals(InventoryAccessFlags.FLAG_OPEN_PLAYER_INVENTORY.toString());
        boolean isOpenEnderChest = flag.equals(InventoryAccessFlags.FLAG_OPEN_ENDER_CHEST.toString());
        boolean isOpenArmorInventory = flag.equals(InventoryAccessFlags.FLAG_OPEN_ARMOR_INVENTORY.toString());
        if (isOpenPlayerInventory || isOpenEnderChest || isOpenArmorInventory) {
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

            // Open specified inventory of target player
            Inventory customInventory;
            if (isOpenPlayerInventory) {
                customInventory = targetPlayer.getInventory();
                player.openInventory(customInventory, Text.of(targetPlayerName + "'s inventory"));
            } else if (isOpenEnderChest) {
                customInventory = targetPlayer.getEnderChestInventory();
                player.openInventory(customInventory, Text.of(targetPlayerName + "'s ender chest"));

            } else {
                Optional<InventoryAccessPlugin> pluginOptional = InventoryAccessPlugin.getInstance();
                if (!pluginOptional.isPresent()) return CommandResult.empty();

                customInventory = Inventory.builder().of(InventoryArchetypes.CHEST)
                        .property(InventoryDimension.of(9,1))
                        .listener(ClickInventoryEvent.class, event -> {
                            // Get Slot and SlotIndex
                            Optional<Slot> slotOptional = event.getSlot();
                            if (!slotOptional.isPresent()) return;
                            Slot slotSelected = slotOptional.get();
                            Optional<SlotIndex> slotIndexOptional = slotSelected.getInventoryProperty(SlotIndex.class);
                            if (!slotIndexOptional.isPresent()) return;
                            SlotIndex slotIndex = slotIndexOptional.get();
                            if (slotIndex.getValue() == null) return;

                            // Perform actions based on the index of the clicked slot
                            Inventory inventory = slotSelected.parent();
                            if (slotIndex.getValue() == 7) {
                                updateArmorSlots(inventory, targetPlayer);
                                event.setCancelled(true);
                            } else if (slotIndex.getValue() == 8) {
                                equipArmorFromInventory(inventory, targetPlayer);
                                event.setCancelled(true);
                            }
                        })
                        .build(pluginOptional.get());

                Inventory buttonUpdateSlot  = getSlot(customInventory, SlotIndex.of(7));
                Inventory buttonSaveSlot    = getSlot(customInventory, SlotIndex.of(8));

                buttonUpdateSlot.set(ItemStack.builder().itemType(ItemTypes.CLOCK).quantity(1)
                        .add(Keys.DISPLAY_NAME, Text.of("update")).build());
                buttonSaveSlot.set(ItemStack.builder().itemType(ItemTypes.WRITABLE_BOOK).quantity(1)
                        .add(Keys.DISPLAY_NAME, Text.of("save")).build());

                updateArmorSlots(customInventory, targetPlayer);

                player.openInventory(customInventory, Text.of(targetPlayerName + "'s armor and off-hand"));
                sendMessage(source, Text.of(String.format(
                        "Successfully opened the armor and off-hand inventory of player \"%s\".", targetPlayerName)));
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
            InventoryAccessFlags[] flags = InventoryAccessFlags.class.getEnumConstants();
            for (InventoryAccessFlags flag : flags) {
                suggestions.add(flag.toString());
            }
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
            filteredPlayers.forEach(player -> suggestions.add(player.getName()));
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
        StringBuilder builder = new StringBuilder();
        builder.append("usage: invacc ");
        InventoryAccessFlags[] flags = InventoryAccessFlags.class.getEnumConstants();
        for (int i = 0; i < flags.length - 1; ++i) {
            builder.append(flags[i].getUsage());
            builder.append(" | ");
        }
        builder.append(flags[flags.length - 1].getUsage());
        return Text.of(builder.toString());
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
        Pattern pattern = Pattern.compile(CommandRegexes.REGEX_SPLIT_QUOTES_OR_WHITESPACE.regex);
        Matcher matcher = pattern.matcher(arguments);
        List<String> args = new ArrayList<>();
        while (matcher.find()) {
            args.add(matcher.group());
        }
        return args;
    }

    public Inventory getSlot(Inventory parentInventory, SlotIndex slotIndex) {
        Inventory slot = parentInventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(slotIndex));
        return slot.slots().iterator().next();
    }

    public Inventory getSlot(Inventory parentInventory, SlotPos slotPos) {
        Inventory slot = parentInventory.query(QueryOperationTypes.INVENTORY_PROPERTY.of(slotPos));
        return slot.slots().iterator().next();
    }

    public void equipItemStackFromSlot(Inventory slot, Player target, EquipmentType equipmentType) {
        Optional<ItemStack> itemStackOptional = slot.peek();
        itemStackOptional.ifPresent(itemStack -> target.equip(equipmentType, itemStack));
    }

    public void updateArmorSlots(Inventory inventory, Player targetPlayer) {
        // Get items from player
        Optional<ItemStack> headwear    = targetPlayer.getEquipped(EquipmentTypes.HEADWEAR);
        Optional<ItemStack> chestplate  = targetPlayer.getEquipped(EquipmentTypes.CHESTPLATE);
        Optional<ItemStack> leggings    = targetPlayer.getEquipped(EquipmentTypes.LEGGINGS);
        Optional<ItemStack> boots       = targetPlayer.getEquipped(EquipmentTypes.BOOTS);
        Optional<ItemStack> offHand     = targetPlayer.getEquipped(EquipmentTypes.OFF_HAND);
        // Get slots from inventory
        Inventory slotHeadwear      = getSlot(inventory, SlotIndex.of(0));
        Inventory slotChestplate    = getSlot(inventory, SlotIndex.of(1));
        Inventory slotLeggings      = getSlot(inventory, SlotIndex.of(2));
        Inventory slotBoots         = getSlot(inventory, SlotIndex.of(3));
        Inventory slotOffHand       = getSlot(inventory, SlotIndex.of(4));
        // Put copies of the items into the slots
        headwear.ifPresent(     itemStack -> slotHeadwear.set(itemStack.copy()));
        chestplate.ifPresent(   itemStack -> slotChestplate.set(itemStack.copy()));
        leggings.ifPresent(     itemStack -> slotLeggings.set(itemStack.copy()));
        boots.ifPresent(        itemStack -> slotBoots.set(itemStack.copy()));
        offHand.ifPresent(      itemStack -> slotOffHand.set(itemStack.copy()));
    }

    public void equipArmorFromInventory(Inventory inventory, Player targetPlayer) {
        Inventory slotHeadwear      = getSlot(inventory, equipmentSlotMap.get(EquipmentTypes.HEADWEAR));
        Inventory slotChestplate    = getSlot(inventory, equipmentSlotMap.get(EquipmentTypes.CHESTPLATE));
        Inventory slotLeggings      = getSlot(inventory, equipmentSlotMap.get(EquipmentTypes.LEGGINGS));
        Inventory slotBoots         = getSlot(inventory, equipmentSlotMap.get(EquipmentTypes.BOOTS));
        Inventory slotOffHand       = getSlot(inventory, equipmentSlotMap.get(EquipmentTypes.OFF_HAND));
        equipItemStackFromSlot(slotHeadwear,    targetPlayer, EquipmentTypes.HEADWEAR);
        equipItemStackFromSlot(slotChestplate,  targetPlayer, EquipmentTypes.CHESTPLATE);
        equipItemStackFromSlot(slotLeggings,    targetPlayer, EquipmentTypes.LEGGINGS);
        equipItemStackFromSlot(slotBoots,       targetPlayer, EquipmentTypes.BOOTS);
        equipItemStackFromSlot(slotOffHand,     targetPlayer, EquipmentTypes.OFF_HAND);
    }
}
