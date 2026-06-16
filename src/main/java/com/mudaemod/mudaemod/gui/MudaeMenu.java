package com.mudaemod.mudaemod.gui;

import com.mudaemod.mudaemod.MudaeMod;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class MudaeMenu extends AbstractContainerMenu {

    public MudaeMenu(int id, Inventory inv) {
        super(MudaeMod.MUDAE_MENU.get(), id);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
