/*
 *  Copyright 2013 Sephthir
 *
 *  This file is part of the Ender Books mod for Minecraft.
 *
 *  Consult the global LICENSE.txt file for additional licensing information.
 *
 */

package mods.sephthir.item;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.List;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import mods.sephthir.ModEnderBooks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.Icon;
import net.minecraft.world.World;

public class ItemEnderBook extends Item {
	
	public final int MAX_PAGES;
	
	public Icon iconNotAuthor;
	public Icon iconAuthor;
	
	public ItemEnderBook(int id)
	{
		this(id, 25);
	}

	public ItemEnderBook(int id, int maxPages)
	{
		super(id);
		this.setCreativeTab(CreativeTabs.tabMisc);
		this.setMaxStackSize(1);
		
		this.MAX_PAGES = maxPages;
	}
	
	@Override
	public boolean hasEffect(ItemStack stack)
	{
		NBTTagCompound tag = stack.getTagCompound();
		
		if ((tag != null) && (tag.hasKey("author")))
			return true;
		else
			return false;
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List information, boolean unknown)
	{
		NBTTagCompound tag = stack.getTagCompound();
		
		if ((tag != null) && (tag.hasKey("author"))) {
			information.add("By " + tag.getString("author"));
		}
	}
	
	@Override
	public String getItemDisplayName(ItemStack stack)
	{
		NBTTagCompound tag = stack.getTagCompound();
		
		if ((tag != null) && (tag.hasKey("title")) && (!tag.getString("title").equals(""))) {
			return tag.getString("title");
		} else {
			return "Ender Book";
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public Icon getIcon(ItemStack stack, int renderPass)
	{
		NBTTagCompound tag = stack.getTagCompound();
		
		if ((tag != null) && (tag.hasKey("author")) && (tag.getString("author").equals(Minecraft.getMinecraft().thePlayer.username)))
				return this.iconAuthor;
		
		return this.iconNotAuthor;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister register)
	{
		this.iconNotAuthor = register.registerIcon("sephthir:EnderBook");
		this.iconAuthor = register.registerIcon("sephthir:EnderBookEditable");
		this.itemIcon = this.iconNotAuthor;
	}
	
	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		if (!world.isRemote) {
			NBTTagCompound book = null;
			
			if (stack.hasTagCompound()) {
				long id = stack.getTagCompound().getLong("id");
				
				String username;
				if (stack.getTagCompound().hasKey("author")) {
					username = stack.getTagCompound().getString("author");
				} else {
					username = player.username;
				}
				
				if (ModEnderBooks.instance.bookHandler.hasBookData(username, id)) {
					book = ModEnderBooks.instance.bookHandler.getBookData(username, id);
				}
			}
			
			if (book == null) {
				book = ModEnderBooks.instance.bookHandler.getNewEmptyBook(player.username);
			}
			
			NBTTagCompound stackTag = new NBTTagCompound();
			stackTag.setLong("id", book.getLong("id"));
			stackTag.setString("author", book.getString("author"));
			stackTag.setString("title", book.getString("title"));
			stack.setTagCompound(stackTag);
			
			ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			DataOutputStream data = new DataOutputStream(bytes);
			
			try {
				NBTTagCompound.writeNamedTag(book, data);
			} catch (Exception ex) {
				FMLLog.log("sephthir.enderbooks", Level.WARNING, "Could not send book information to client.");
				return stack;
			}
	
			Packet250CustomPayload packet = new Packet250CustomPayload();
			packet.channel = "EndrBk|Data";
			packet.data = bytes.toByteArray();
			packet.length = bytes.size();
			
			PacketDispatcher.sendPacketToPlayer((Packet)packet, (Player)player);
		}
		
		return stack;
	}
	
	@Override
	public boolean getShareTag()
	{
		return true;
	}

}
