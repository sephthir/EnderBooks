/*
 *  Copyright 2013 Sephthir
 *
 *  This file is part of the Ender Books mod for Minecraft.
 *
 *  Consult the global LICENSE.txt file for additional licensing information.
 *
 */

package mods.sephthir.item.crafting;

import mods.sephthir.ModEnderBooks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipesEnderBooks implements IRecipe {

	@Override
	public boolean matches(InventoryCrafting inventory, World world) {
		int countBooks = 0;
		int countEyeOfEnder = 0;
		int countEnderBooks = 0;
		int countWrittenEnderBooks = 0;
		
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null) {
				if (stack.itemID == Item.writableBook.itemID) {
					countBooks++;
				} else if (stack.itemID == Item.eyeOfEnder.itemID) {
					countEyeOfEnder++;
				} else if (stack.itemID == ModEnderBooks.itemEnderBook.itemID) {
					if ((stack.hasTagCompound()) && (stack.getTagCompound().hasKey("id"))) {
						countWrittenEnderBooks++;
					} else {
						countEnderBooks++;
					}
				}
			}
		}
		
		return ((countBooks == 1) && (countEyeOfEnder == 1) && (countWrittenEnderBooks <= 1)) ||
				((countEnderBooks == 1) && (countWrittenEnderBooks == 1));
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting inventory) {
		int countBooks = 0;
		int countEyeOfEnder = 0;
		int countEnderBooks = 0;
		int countWrittenEnderBooks = 0;
		
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack != null) {
				if (stack.itemID == Item.writableBook.itemID) {
					countBooks++;
				} else if (stack.itemID == Item.eyeOfEnder.itemID) {
					countEyeOfEnder++;
				} else if (stack.itemID == ModEnderBooks.itemEnderBook.itemID) {
					if ((stack.hasTagCompound()) && (stack.getTagCompound().hasKey("id"))) {
						countWrittenEnderBooks++;
					} else {
						countEnderBooks++;
					}
				}
			}
		}
		
		if ((countBooks == 1) && (countEyeOfEnder == 1) && (countWrittenEnderBooks == 0)) {
			return new ItemStack(ModEnderBooks.itemEnderBook, 1);
		} else {
			for (int i = 0; i < inventory.getSizeInventory(); ++i) {
				ItemStack stack = inventory.getStackInSlot(i);
				if ((stack != null) && (stack.itemID == ModEnderBooks.itemEnderBook.itemID)) {
					if ((stack.hasTagCompound()) && (stack.getTagCompound().hasKey("id"))) {
						ItemStack returnStack = new ItemStack(ModEnderBooks.itemEnderBook, 2);
						returnStack.setTagCompound(stack.getTagCompound());
						return returnStack;
					}
				}
			}
		}
		
		return null;
	}
	
	private void clearInventory(InventoryCrafting inventory, int except) {
		for (int i = 0; i < inventory.getSizeInventory(); ++i) {
			if (i != except) {
				inventory.setInventorySlotContents(i, null);
			}
		}
	}

	@Override
	public int getRecipeSize() {
		return 4;
	}

	@Override
	public ItemStack getRecipeOutput() {
		return null;
	}

}
