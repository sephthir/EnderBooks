/*
 *  Copyright 2013 Sephthir
 *
 *  This file is part of the Ender Books mod for Minecraft.
 *  
 *  Consult the global LICENSE.txt file for additional licensing information.
 *
 */

package mods.sephthir;

import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import mods.sephthir.handler.EnderBookHandler;
import mods.sephthir.handler.PacketHandlerEnderBooks;
import mods.sephthir.item.ItemEnderBook;
import mods.sephthir.item.crafting.RecipesEnderBooks;
import mods.sephthir.proxy.ProxyCommonEnderBooks;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.Mod.ServerStopping;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import cpw.mods.fml.relauncher.Side;

@Mod(
	modid				= "sephthir.enderbooks",
	name				= "Ender Books",
	version				= "1.1-1")
@NetworkMod(
	clientSideRequired	= true,
	serverSideRequired	= true,
	channels			= {"EndrBk|Data", "EndrBk|Save", "EndrBk|Page", "EndrBk|Del"},
	packetHandler		= PacketHandlerEnderBooks.class)
public class ModEnderBooks {
	
	@Instance
	public static ModEnderBooks instance = new ModEnderBooks();
	
	@SidedProxy(
		clientSide		= "mods.sephthir.proxy.ProxyClientEnderBooks",
		serverSide		= "mods.sephthir.proxy.ProxyCommonEnderBooks")
	public static ProxyCommonEnderBooks proxy;
	
	public static Item itemEnderBook;
	public static int itemEnderBookID;
	public static int itemEnderBookMaxPages;
	
	public EnderBookHandler bookHandler;
	
	@PreInit
	public void preInit(FMLPreInitializationEvent evt)
	{
		Configuration config = new Configuration(evt.getSuggestedConfigurationFile());
		
		config.load();
		
		this.itemEnderBookID = config.getItem("ItemEnderBook", 5120).getInt();
		this.itemEnderBookMaxPages = config.get(Configuration.CATEGORY_GENERAL, "ItemEnderBookMaxPages", 25).getInt();
		
		config.save();
	}
	
	@Init
	public void init(FMLInitializationEvent evt)
	{
		this.itemEnderBook = new ItemEnderBook(this.itemEnderBookID, this.itemEnderBookMaxPages);
		GameRegistry.registerItem(this.itemEnderBook, "sephthir.enderBook");
		LanguageRegistry.addName(this.itemEnderBook, "Ender Book");
		
		GameRegistry.addRecipe(new RecipesEnderBooks());
		
		this.proxy.registerRenderers();
	}
	
	@ServerStarting
	public void serverStarting(FMLServerStartingEvent evt)
	{
		this.bookHandler = new EnderBookHandler(evt.getServer());
	}
	
	@ServerStopping
	public void serverStopping(FMLServerStoppingEvent evt)
	{
		this.bookHandler.saveEnderBooks();
		this.bookHandler.unload();
	}

}
