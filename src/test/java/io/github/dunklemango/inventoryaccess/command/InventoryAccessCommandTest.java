package io.github.dunklemango.inventoryaccess.command;

import io.github.dunklemango.inventoryaccess.InventoryAccessFlags;
import io.github.dunklemango.inventoryaccess.InventoryAccessPlugin;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class InventoryAccessCommandTest {
    private static final String PLAYER_SOURCE_NAME = "Alex";
    private static final String PLAYER_TARGET_NAME = "Steve";
    private static MockedStatic<Sponge> spongeMockedStatic;
    private Player playerSource;

    @BeforeAll
    public static void beforeAll() {
        spongeMockedStatic = Mockito.mockStatic(Sponge.class);
        MockedStatic<InventoryAccessPlugin> inventoryAccessPluginMockedStatic
                = Mockito.mockStatic(InventoryAccessPlugin.class);
        inventoryAccessPluginMockedStatic.when(InventoryAccessPlugin::getInstance)
                .thenReturn(Optional.of(new InventoryAccessPlugin()));
    }

    @BeforeEach
    public void beforeEach() {
        playerSource = mock(Player.class);
        Player playerTarget = mock(Player.class);
        Collection<Player> players = new ArrayList<>();
        players.add(playerSource);
        players.add(playerTarget);
        Server server = mock(Server.class);
        when(playerSource.getName()).thenReturn(PLAYER_SOURCE_NAME);
        when(playerTarget.getName()).thenReturn(PLAYER_TARGET_NAME);
        spongeMockedStatic.when(Sponge::getServer).thenReturn(server);
        when(server.getOnlinePlayers()).thenReturn(players);
        when(playerTarget.getInventory()).thenReturn(null);
    }

    @Test
    public void openPlayerInventoryWithCorrectArguments() {
        InventoryAccessCommand command = new InventoryAccessCommand();
        command.enableLogging(true);
        CommandResult result = command.process(playerSource,
                InventoryAccessFlags.FLAG_OPEN_PLAYER_INVENTORY.toString() + " " + PLAYER_TARGET_NAME);
        assert result.equals(CommandResult.success());
    }

    @Test
    public void openEnderChestWithCorrectArguments() {
        InventoryAccessCommand command = new InventoryAccessCommand();
        command.enableLogging(true);
        CommandResult result = command.process(playerSource,
                InventoryAccessFlags.FLAG_OPEN_ENDER_CHEST.toString() + " " + PLAYER_TARGET_NAME);
        assert result.equals(CommandResult.success());
    }

    @Test
    public void openArmorInventoryWithCorrectArguments() {
        // TODO - fix the test
        /*
        InventoryAccessCommand command = new InventoryAccessCommand();
        command.enableLogging(true);
        CommandResult result = command.process(playerSource,
                InventoryAccessFlags.FLAG_OPEN_ARMOR_INVENTORY.toString() + " " + PLAYER_TARGET_NAME);
        assert result.equals(CommandResult.success());
        */
    }
}
