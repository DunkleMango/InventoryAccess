package io.github.dunklemango.inventoryaccess;

import com.google.inject.Inject;
import io.github.dunklemango.inventoryaccess.command.InventoryAccessCommand;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.Optional;

@Plugin(id = InventoryAccessPlugin.PLUGIN_ID, name = InventoryAccessPlugin.PLUGIN_NAME,
        version = InventoryAccessPlugin.PLUGIN_VERSION, description = InventoryAccessPlugin.PLUGIN_DESCRIPTION,
        authors = {"DunkleMango"})
public class InventoryAccessPlugin {
    public static final String PLUGIN_ID = "inventoryaccess";
    public static final String PLUGIN_NAME = "Inventory Access";
    public static final String PLUGIN_VERSION = "1.0.0";
    public static final String PLUGIN_DESCRIPTION = "Enables the access of other players inventories.";

    @Inject
    private Logger logger;

    @Inject
    private Game game;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.info("Started successfully!");
    }

    @Listener
    public void onGamePreInitialization(GamePreInitializationEvent event) {
        Sponge.getCommandManager().register(this, new InventoryAccessCommand(), "invacc");
    }

    public static Optional<InventoryAccessPlugin> getInstance() {
        Optional<PluginContainer> pluginContainerOptional = Sponge.getPluginManager()
                .getPlugin(InventoryAccessPlugin.PLUGIN_ID);
        if (!pluginContainerOptional.isPresent()) return Optional.empty();
        Optional<?> pluginMaybe = pluginContainerOptional.get().getInstance();
        if (!pluginMaybe.isPresent() || !(pluginMaybe.get() instanceof InventoryAccessPlugin)) return Optional.empty();
        return Optional.of((InventoryAccessPlugin) pluginMaybe.get());
    }
}
