package com.github.dunklemango.inventoryaccess;

import com.github.dunklemango.inventoryaccess.command.InventoryAccessCommand;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;

@Plugin(id = "inventoryaccess", name = "Inventory Access")
public class InventoryAccessPlugin {
    public static final String PLUGIN_ID = "inventoryaccess";

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
}
