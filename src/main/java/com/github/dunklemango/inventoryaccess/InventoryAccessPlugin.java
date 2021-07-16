package com.github.dunklemango.inventoryaccess;

import com.github.dunklemango.inventoryaccess.command.InventoryAccessCommand;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Optional;

@Plugin(id = InventoryAccessPlugin.PLUGIN_ID, name = InventoryAccessPlugin.PLUGIN_NAME)
public class InventoryAccessPlugin {
    public static final String PLUGIN_ID = "inventoryaccess";
    public static final String PLUGIN_NAME = "Inventory Access";

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
