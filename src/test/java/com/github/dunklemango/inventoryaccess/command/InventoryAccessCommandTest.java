package com.github.dunklemango.inventoryaccess.command;

import org.junit.jupiter.api.Test;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.Collection;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InventoryAccessCommandTest {

    @Test
    public void openPlayerInventoryWithCorrectArguments() {
        InventoryAccessCommand command = new InventoryAccessCommand();
        command.enableLogging(true);
        Player playerSource = mock(Player.class);
        Player playerTarget = mock(Player.class);
        World world = mock(World.class);
        Collection<Player> players = new ArrayList<>();
        players.add(playerSource);
        players.add(playerTarget);
        when(playerSource.getName()).thenReturn("Alex");
        when(playerSource.getName()).thenReturn("Steve");
        when(playerSource.getWorld()).thenReturn(world);
        when(world.getPlayers()).thenReturn(players);
        when(playerTarget.getInventory()).thenReturn(null);
        CommandResult result = command.process(playerSource, "-i Steve");
        assert result.equals(CommandResult.success());
    }
}
