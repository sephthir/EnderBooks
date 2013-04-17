/*
 *  Copyright 2013 Sephthir
 *
 *  This file is part of the Ender Books mod for Minecraft.
 *
 *  Consult the global LICENSE.txt file for additional licensing information.
 *
 */

package mods.sephthir.gui;

import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.logging.Level;

import mods.sephthir.ModEnderBooks;
import mods.sephthir.item.ItemEnderBook;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.Packet250CustomPayload;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiScreenEnderBook extends GuiScreen
{
    private EntityPlayer currentPlayer;
    private ItemStack bookStack;
    private NBTTagCompound bookInfo;
    
    private NBTTagList bookPages;
    private String bookTitle;
    private String bookAuthor;

    private boolean bookIsOwnedByPlayer;
    private boolean bookEditingTitle;
    
    private boolean bookTitleConfirmed;

    private int updateCount;
    private int bookImageWidth = 192;
    private int bookImageHeight = 192;
    private int bookTotalPages = 1;
    private int currPage;
    
    private GuiButtonEnderNextPage buttonNextPage;
    private GuiButtonEnderNextPage buttonPreviousPage;
    private GuiButton buttonDone;

    public GuiScreenEnderBook(EntityPlayer player, ItemStack stack, NBTTagCompound book)
    {
        this.currentPlayer = player;
        this.bookStack = stack;
        this.bookInfo = book;
        
        this.bookTitle = this.bookInfo.getString("title");
        this.bookAuthor = this.bookInfo.getString("author");
        this.bookIsOwnedByPlayer = this.bookAuthor.equals(this.currentPlayer.username);
        
        this.bookEditingTitle = (this.bookIsOwnedByPlayer && this.bookTitle.equals(""));
        this.bookTitleConfirmed = !this.bookEditingTitle;
        
        this.bookPages = this.bookInfo.getTagList("pages");
        this.bookTotalPages = this.bookPages.tagCount();
        
        if (this.bookStack.hasTagCompound()) {
        	this.currPage = this.bookStack.getTagCompound().getInteger("currpage");
        } else {
        	this.currPage = 0;
        }
        
        NBTTagCompound itemTag = new NBTTagCompound();
        itemTag.setString("title", this.bookTitle);
        itemTag.setString("author", this.bookAuthor);
        itemTag.setLong("id", this.bookInfo.getLong("id"));
        
        this.bookStack.setTagCompound(itemTag);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    @Override
    public void updateScreen()
    {
        super.updateScreen();
        ++this.updateCount;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    @Override
    public void initGui()
    {
        this.buttonList.clear();
        Keyboard.enableRepeatEvents(true);

    	this.buttonList.add(this.buttonDone = new GuiButton(0, this.width / 2 - 100, 4 + this.bookImageHeight, 200, 20, "Done"));

        int i = (this.width - this.bookImageWidth) / 2;
        
        this.buttonList.add(this.buttonNextPage = new GuiButtonEnderNextPage(1, i + 120, 156, true));
        this.buttonList.add(this.buttonPreviousPage = new GuiButtonEnderNextPage(2, i + 38, 156, false));
        
        this.updateButtons();
    }

    /**
     * Called when the screen is unloaded. Used to disable keyboard repeat events
     */
    @Override
    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        
        if (!this.bookTitleConfirmed) {
        	this.removeBookFromServer();
        	this.bookStack.setTagCompound(null);
        }
    }
    
    private void updateButtons()
    {
    	if (this.bookEditingTitle) {
    		this.buttonNextPage.drawButton = false;
    		this.buttonPreviousPage.drawButton = false;
    		
    		this.buttonDone.enabled = (this.bookTitle.length() > 0);
    	} else {
	        this.buttonNextPage.drawButton = (this.currPage < this.bookTotalPages - 1 || this.bookIsOwnedByPlayer);
	        this.buttonPreviousPage.drawButton = this.currPage > 0;
	        this.buttonDone.enabled = true;
    	}
    }

    private void sendBookToServer()
    {   
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		
		this.bookInfo.setInteger("currpage", this.currPage);
		this.bookInfo.setString("title", this.bookTitle);
		
		try {
			NBTTagCompound.writeNamedTag(this.bookInfo, data);
		} catch (Exception ex) {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Could not send book information to server.");
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "EndrBk|Save";
		packet.data = bytes.toByteArray();
		packet.length = bytes.size();
		
		PacketDispatcher.sendPacketToServer(packet);
    }
    
    private void removeBookFromServer()
    {
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		
		try {
			Packet.writeString(this.bookAuthor, data);
			data.writeLong(this.bookInfo.getLong("id"));
		} catch (Exception ex) {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Could not remove book information from server.");
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "EndrBk|Del";
		packet.data = bytes.toByteArray();
		packet.length = bytes.size();
		
		PacketDispatcher.sendPacketToServer(packet);
    }
    
    private void sendPageToServer()
    {   
    	ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream data = new DataOutputStream(bytes);
		
		try {
			data.writeInt(this.currPage);
		} catch (Exception ex) {
			FMLLog.log("sephthir.enderbooks", Level.WARNING, "Could not send page information to server.");
		}

		Packet250CustomPayload packet = new Packet250CustomPayload();
		packet.channel = "EndrBk|Page";
		packet.data = bytes.toByteArray();
		packet.length = bytes.size();
		
		PacketDispatcher.sendPacketToServer(packet);
    }

    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton button)
    {
        if (button.enabled)
        {
            if (button.id == 0)
            {
            	if (this.bookEditingTitle) {
            		this.bookEditingTitle = false;
            		this.bookTitleConfirmed = true;
            		
            		this.bookStack.getTagCompound().setString("title", this.bookTitle);
            		
            		this.sendBookToServer();
            	} else {
	                this.mc.displayGuiScreen((GuiScreen)null);
	                
	        		this.bookStack.getTagCompound().setInteger("currpage", this.currPage);
	        		this.bookStack.getTagCompound().setString("title", this.bookTitle);
	                
	                if (this.bookIsOwnedByPlayer)
	                	this.sendBookToServer();
	                else
	                	this.sendPageToServer();
            	}
            }
            else if (button.id == 1)
            {
                if (this.currPage < this.bookTotalPages - 1)
                {
                    ++this.currPage;
                }
                else if (this.bookIsOwnedByPlayer)
                {
                    this.addNewPage();

                    if (this.currPage < this.bookTotalPages - 1)
                    {
                        ++this.currPage;
                    }
                }
            }
            else if (button.id == 2)
            {
                if (this.currPage > 0)
                {
                    --this.currPage;
                }
            }
            
            this.updateButtons();
        }
    }

    private void addNewPage()
    {
        if (this.bookPages != null && this.bookPages.tagCount() < ((ItemEnderBook)ModEnderBooks.itemEnderBook).MAX_PAGES)
        {
            this.bookPages.appendTag(new NBTTagString("" + (this.bookTotalPages + 1), ""));
            ++this.bookTotalPages;
        }
    }

    /**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
     */
    @Override
    protected void keyTyped(char par1, int par2)
    {
        super.keyTyped(par1, par2);
        
        if (this.bookIsOwnedByPlayer)
        {
        	if (this.bookEditingTitle)
        		this.keyTypedInTitleEditing(par1, par2);
        	else
        		this.keyTypedInBook(par1, par2);
        }
    }

    /**
     * Processes keystrokes when editing the text of a book
     */
    private void keyTypedInBook(char par1, int par2)
    {
        switch (par1)
        {
        case 22:
            this.addToCurrentPageContents(GuiScreen.getClipboardString());
            return;
        default:
            switch (par2)
            {
            case 14:
                String s = this.getCurrentPageContents();

                if (s.length() > 0)
                {
                    this.setCurrentPageContents(s.substring(0, s.length() - 1));
                }

                return;
            case 28:
                this.addToCurrentPageContents("\n");
                return;
            default:
                if (ChatAllowedCharacters.isAllowedCharacter(par1))
                {
                    this.addToCurrentPageContents(Character.toString(par1));
                }
            }
        }
    }
    
    private void keyTypedInTitleEditing(char par1, int par2)
    {
        switch (par2)
        {
            case 14:
                if (this.bookTitle.length() > 0)
                {
                    this.bookTitle = this.bookTitle.substring(0, this.bookTitle.length() - 1);
                    this.updateButtons();
                }

                return;
            case 28:
            	return;
            default:
                if (this.bookTitle.length() < 16 && ChatAllowedCharacters.isAllowedCharacter(par1))
                {
                    this.bookTitle = this.bookTitle + Character.toString(par1);
                    this.updateButtons();
                }
        }
    }

    private String getCurrentPageContents()
    {
        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
        {
            NBTTagString tag = (NBTTagString)this.bookPages.tagAt(this.currPage);
            return tag.toString();
        }
        else
        {
            return "";
        }
    }

    private void setCurrentPageContents(String contents)
    {
        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
        {
            NBTTagString tag = (NBTTagString)this.bookPages.tagAt(this.currPage);
            tag.data = contents;
        }
    }

    private void addToCurrentPageContents(String addition)
    {
        String currentPageContents = this.getCurrentPageContents();
        String newPageContents = currentPageContents + addition;
        
        int i = this.fontRenderer.splitStringWidth(newPageContents + "" + EnumChatFormatting.BLACK + "_", 118);

        if (i <= 118 && newPageContents.length() < 256)
        {
            this.setCurrentPageContents(newPageContents);
        }
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.renderEngine.bindTexture("/mods/sephthir/textures/gui/EnderBook.png");
        
        int x = (this.width - this.bookImageWidth) / 2;
        
        this.drawTexturedModalRect(x, 2, 0, 0, this.bookImageWidth, this.bookImageHeight);
        
        String s;
        String s1;
        int l;
        
    	if (this.bookEditingTitle) {
    		s = this.bookTitle;

            if (this.updateCount / 6 % 2 == 0)
            {
                s = s + "" + EnumChatFormatting.BLACK + "_";
            }
            else
            {
                s = s + "" + EnumChatFormatting.GRAY + "_";
            }

            s1 = StatCollector.translateToLocal("book.editTitle");
            l = this.fontRenderer.getStringWidth(s1);
            this.fontRenderer.drawString(s1, x + 36 + (116 - l) / 2, 2 + 16 + 16, 0);
            int i1 = this.fontRenderer.getStringWidth(s);
            this.fontRenderer.drawString(s, x + 36 + (116 - i1) / 2, 2 + 48, 0);
            String s2 = String.format("by %s", new Object[] {this.bookAuthor});
            int j1 = this.fontRenderer.getStringWidth(s2);
            this.fontRenderer.drawString(EnumChatFormatting.DARK_GRAY + s2, x + 36 + (116 - j1) / 2, 2 + 48 + 10, 0);
    	} else {
	        s = String.format(StatCollector.translateToLocal("book.pageIndicator"), new Object[] {Integer.valueOf(this.currPage + 1), Integer.valueOf(this.bookTotalPages)});
	        s1 = "";
	
	        if (this.bookPages != null && this.currPage >= 0 && this.currPage < this.bookPages.tagCount())
	        {
	            NBTTagString nbttagstring = (NBTTagString)this.bookPages.tagAt(this.currPage);
	            s1 = nbttagstring.toString();
	        }
	
	        if (this.bookIsOwnedByPlayer)
	        {
	            if (this.fontRenderer.getBidiFlag())
	            {
	                s1 = s1 + "_";
	            }
	            else if (this.updateCount / 6 % 2 == 0)
	            {
	                s1 = s1 + "" + EnumChatFormatting.BLACK + "_";
	            }
	            else
	            {
	                s1 = s1 + "" + EnumChatFormatting.GRAY + "_";
	            }
	        }
	
	        l = this.fontRenderer.getStringWidth(s);
	        this.fontRenderer.drawString(s, x - l + this.bookImageWidth - 44, 2 + 16, 0);
	        this.fontRenderer.drawSplitString(s1, x + 36, 2 + 16 + 16, 116, 0);
    	}
	    
        super.drawScreen(par1, par2, par3);
    }
}

