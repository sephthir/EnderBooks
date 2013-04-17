/*
 *  Copyright 2013 Sephthir
 *
 *  This file is part of the Ender Books mod for Minecraft.
 *
 *  Consult the global LICENSE.txt file for additional licensing information.
 *
 */

package mods.sephthir.client.renderer;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.SideOnly;
import cpw.mods.fml.relauncher.Side;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderEngine;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;

@SideOnly(Side.CLIENT)
public class ItemRendererEnderBooks implements IItemRenderer {

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return type == ItemRenderType.INVENTORY;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {
		return false;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		RenderEngine engine = Minecraft.getMinecraft().renderEngine;
		Icon icon = item.getItem().getIcon(item, 0);
		
		GL11.glDisable(GL11.GL_LIGHTING);

        if (item.getItemSpriteNumber() == 0)
        {
            engine.bindTexture("/terrain.png");
        }
        else
        {
            engine.bindTexture("/gui/items.png");
        }

        if (icon == null)
        {
            icon = engine.getMissingIcon(item.getItemSpriteNumber());
        }

        this.renderIcon(0, 0, icon, 16, 16);
        GL11.glEnable(GL11.GL_LIGHTING);
	}
	
	public void renderIcon(int par1, int par2, Icon par3Icon, int par4, int par5)
    {
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + par5), 0, (double)par3Icon.getMinU(), (double)par3Icon.getMaxV());
        tessellator.addVertexWithUV((double)(par1 + par4), (double)(par2 + par5), 0, (double)par3Icon.getMaxU(), (double)par3Icon.getMaxV());
        tessellator.addVertexWithUV((double)(par1 + par4), (double)(par2 + 0), 0, (double)par3Icon.getMaxU(), (double)par3Icon.getMinV());
        tessellator.addVertexWithUV((double)(par1 + 0), (double)(par2 + 0), 0, (double)par3Icon.getMinU(), (double)par3Icon.getMinV());
        tessellator.draw();
    }

}
