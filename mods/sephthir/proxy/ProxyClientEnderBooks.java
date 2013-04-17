/*
 *  Copyright 2013 Sephthir
 *
 *  This file is part of the Ender Books mod for Minecraft.
 *
 *  Consult the global LICENSE.txt file for additional licensing information.
 *
 */

package mods.sephthir.proxy;

import mods.sephthir.ModEnderBooks;
import mods.sephthir.client.renderer.ItemRendererEnderBooks;
import net.minecraftforge.client.MinecraftForgeClient;

public class ProxyClientEnderBooks extends ProxyCommonEnderBooks {
	
	@Override
	public void registerRenderers()
	{
		MinecraftForgeClient.registerItemRenderer(ModEnderBooks.itemEnderBook.itemID, new ItemRendererEnderBooks());
	}
	
}
