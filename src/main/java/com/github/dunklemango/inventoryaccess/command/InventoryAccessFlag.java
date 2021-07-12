package com.github.dunklemango.inventoryaccess.command;

public enum InventoryAccessFlag {
    FLAG_OPEN_PLAYER_INVENTORY("-i"),
    FLAG_OPEN_ENDER_CHEST("-e"),
    FLAG_OPEN_ARMOR_INVENTORY("-a");

    private final String flag;

    private InventoryAccessFlag(String flag) {
        this.flag = flag;
    }

    @Override
    public String toString() {
        return this.flag;
    }

    public String getUsage() {
        return this.flag + " <player>";
    }
}
