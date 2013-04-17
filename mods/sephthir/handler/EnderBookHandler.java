/*
 *  Copyright 2013 Sephthir
 *
 *  This file is part of the Ender Books mod for Minecraft.
 *
 *  Consult the global LICENSE.txt file for additional licensing information.
 *
 */

package mods.sephthir.handler;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;

import mods.sephthir.ModEnderBooks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;

public class EnderBookHandler {
	
	private MinecraftServer server;
	
	private File saveFile;
	
	private NBTTagCompound books;
	
	private long lastUsedID;
	
	public EnderBookHandler(MinecraftServer server)
	{
		this.server = server;
		
		this.saveFile = new File(this.getWorldFolder(this.server), "sephthir.enderbooks.dat");
		
		if (!this.saveFile.exists()) {
			try {
				if (!this.saveFile.createNewFile())
					this.logSevere("It doesn't seem like we can write out to our enderbook save file...");
			} catch (IOException e) {
				this.logSevere("It doesn't seem like we can write out to our enderbook save file...");
			}
		}
		
		this.books = null;
		this.lastUsedID = -1;
		
		this.loadEnderBooks();
	}
	
	private static void logInfo(String format, Object... data)
	{
		FMLLog.log("sephthir.enderbooks", Level.INFO, format, data);
	}
	
	private static void logWarning(String format, Object... data)
	{
		FMLLog.log("sephthir.enderbooks", Level.WARNING, format, data);
	}
	
	private static void logSevere(String format, Object... data)
	{
		FMLLog.log("sephthir.enderbooks", Level.SEVERE, format, data);
	}
	
	public static File getWorldFolder(MinecraftServer server)
	{
		if (server.isDedicatedServer()) {
			return server.getFile(server.getFolderName());
		} else {
			return new File(server.getFile("saves"), server.getFolderName());
		}
	}
	
	public int getCountEnderBooks()
	{
		int count = 0;
		for (NBTTagCompound tag : (Collection<NBTTagCompound>)this.books.getTags()) {
			count += tag.getTags().size();
		}
		return count;
	}

	public void loadEnderBooks()
	{
		NBTTagCompound wrapper = null;
		this.books = null;
		
		try {
			DataInput saveFileStream = new DataInputStream(new FileInputStream(this.saveFile));
			
			try {
				wrapper = (NBTTagCompound)NBTTagCompound.readNamedTag(saveFileStream);
			} catch (IOException e) {
				this.logWarning("Could not read enderbooks in from file: error reading.");
			}
		} catch (FileNotFoundException e) {
			this.logInfo("Could not read enderbooks in from file: no savefile.");
		}
		
		if (wrapper != null) {
			this.lastUsedID = wrapper.getLong("lastUsedID");
			this.books = wrapper.getCompoundTag("books");
			this.logInfo("Loaded %d enderbooks.", this.getCountEnderBooks());
		} else {
			this.books = new NBTTagCompound();
		}
	}
	
	public void saveEnderBooks()
	{
		NBTTagCompound wrapper = new NBTTagCompound();
		
		wrapper.setLong("lastUsedID", this.lastUsedID);
		wrapper.setCompoundTag("books", this.books);
		
		try {
			DataOutput saveFileStream = new DataOutputStream(new FileOutputStream(this.saveFile));
			
			try {
				NBTTagCompound.writeNamedTag(wrapper, saveFileStream);
			} catch (IOException e) {
				this.logSevere("Could not write enderbooks in from file: error writing.");
			}
		} catch (FileNotFoundException e) {
			this.logSevere("Could not write enderbooks in from file: file not writable.");
		}
		
		this.logInfo("Saved %d enderbooks.", this.getCountEnderBooks());
	}
	
	public void unload()
	{
		this.server = null;
	}
	
	public NBTTagCompound getBookData(String player, long id)
	{
		if (!this.books.hasKey(player))
			return null;
		
		NBTTagCompound userTag = this.books.getCompoundTag(player);
		
		if (!userTag.hasKey(Long.toString(id)))
			return null;
		
		return userTag.getCompoundTag(Long.toString(id));
	}
	
	public void setBookData(String player, long id, NBTTagCompound book)
	{
		NBTTagCompound userTag;
		
		if (!this.books.hasKey(player)) {
			userTag = new NBTTagCompound();
			this.books.setCompoundTag(player, userTag);
		} else {
			userTag = this.books.getCompoundTag(player);
		}
		
		userTag.setCompoundTag(Long.toString(id), book);
		
		this.saveEnderBooks();
	}
	
	public void delBookData(String player, long id)
	{
		NBTTagCompound userTag;
		
		if (!this.books.hasKey(player)) {
			userTag = new NBTTagCompound();
			this.books.setCompoundTag(player, userTag);
		} else {
			userTag = this.books.getCompoundTag(player);
		}
		
		if (userTag.hasKey(Long.toString(id))) {
			userTag.removeTag(Long.toString(id));
			this.saveEnderBooks();
		}
	}
	
	public NBTTagCompound getNewEmptyBook(String player)
	{
		NBTTagCompound book = new NBTTagCompound();
		NBTTagList pages = new NBTTagList();
		pages.appendTag(new NBTTagString("1", ""));
		
		long id = this.getNewBookID();
		
		book.setLong("id", id);
		book.setString("title", "");
		book.setString("author", player);
		book.setTag("pages", pages);
		
		this.setBookData(player, id, book);
		
		return book;
	}
	
	public boolean hasBookData(String player, long id)
	{
		if (!this.books.hasKey(player))
			return false;
		
		if (!this.books.getCompoundTag(player).hasKey(Long.toString(id)))
			return false;
		
		return true;
	}
	
	public long getNewBookID()
	{
		return ++this.lastUsedID;
	}
	
}
