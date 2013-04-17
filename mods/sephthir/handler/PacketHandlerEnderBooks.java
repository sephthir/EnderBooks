/*
 *  Copyright 2013 Sephthir
 *
 *  This file is part of the Ender Books mod for Minecraft.
 *
 *  Consult the global LICENSE.txt file for additional licensing information.
 *
 */

package mods.sephthir.handler;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.logging.Level;

import mods.sephthir.ModEnderBooks;
import mods.sephthir.gui.GuiScreenEnderBook;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.INetworkManager;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.IPacketHandler;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class PacketHandlerEnderBooks implements IPacketHandler {

	@Override
	public void onPacketData(INetworkManager manager,
			Packet250CustomPayload packet, Player player) {
		
		if ((packet.channel.equals("EndrBk|Data")) && (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT))
			this.handleEnderBookDataPacket(manager, packet, player);
		else if (packet.channel.equals("EndrBk|Save"))
			this.handleEnderBookSavePacket(manager, packet, player);
		else if (packet.channel.equals("EndrBk|Page"))
			this.handleEnderBookPagePacket(manager, packet, player);
		else if (packet.channel.equals("EndrBk|Del"))
			this.handleEnderBookDeletePacket(manager, packet, player);
		else
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Received an unknown packet, or a packet on the wrong end of the pipe.");
	}
	
	@SideOnly(Side.CLIENT)
	private void handleEnderBookDataPacket(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		NBTTagCompound book = null;
		
		try {
			book = (NBTTagCompound)NBTTagCompound.readNamedTag(data);
		} catch (IOException e) {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Could not read the received enderbook packet.");
			return;
		}
		
		if (book != null) {
			Minecraft.getMinecraft().displayGuiScreen(new GuiScreenEnderBook((EntityPlayer)player, ((EntityPlayer)player).inventory.getCurrentItem(), book));
		} else {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Received a book packet, but no book was inside.");
			return;
		}
	}
	
	private void handleEnderBookSavePacket(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		NBTTagCompound book = null;
		
		try {
			book = (NBTTagCompound)NBTTagCompound.readNamedTag(data);
		} catch (IOException e) {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Could not read the received enderbook packet.");
			return;
		}
		
		if (book != null) {
			long id = book.getLong("id");
			int currPage = book.getInteger("currpage");
			
			book.removeTag("currpage");
			
			ModEnderBooks.instance.bookHandler.setBookData(((EntityPlayer)player).username, id, book);
			
			NBTTagCompound infoTag = new NBTTagCompound();
			infoTag.setString("author", book.getString("author"));
			infoTag.setString("title", book.getString("title"));
			infoTag.setLong("id", book.getLong("id"));
			infoTag.setInteger("currpage", currPage);
			
			((EntityPlayer)player).inventory.getCurrentItem().setTagCompound(infoTag);
		} else {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Received a book packet, but no book was inside.");
			return;
		}
	}
	
	private void handleEnderBookPagePacket(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));
		
		int currPage = 0;
		
		try {
			currPage = data.readInt();
		} catch (IOException e) {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Received a page packet, but no page number was inside.");
			return;
		}
		
		((EntityPlayer)player).inventory.getCurrentItem().getTagCompound().setInteger("currpage", currPage);
	}
	
	private void handleEnderBookDeletePacket(INetworkManager manager, Packet250CustomPayload packet, Player player)
	{
		DataInputStream data = new DataInputStream(new ByteArrayInputStream(packet.data));

		String author;
		long id;
		
		try {
			author = Packet.readString(data, 16);
			id = data.readLong();
		} catch (IOException e) {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Could not read the received enderbook deletion packet.");
			return;
		}
		
		((EntityPlayer)player).inventory.getCurrentItem().setTagCompound(null);
		
		if (ModEnderBooks.instance.bookHandler.hasBookData(author, id)) {
			ModEnderBooks.instance.bookHandler.delBookData(author, id);
		}
	}

}
