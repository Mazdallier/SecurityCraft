package org.freeforums.geforce.securitycraft.tileentity;

import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;

import org.freeforums.geforce.securitycraft.blocks.BlockInventoryScannerBlock;
import org.freeforums.geforce.securitycraft.main.HelpfulMethods;
import org.freeforums.geforce.securitycraft.misc.EnumCustomModules;

public class TileEntityInventoryScanner extends CustomizableSCTE implements IInventory
{
	private ItemStack[] inventoryContents = new ItemStack[21]; 
	private String type = "check";
	private boolean isProvidingPower;
	private int cooldown;
	
	public void update(){ 						
    	if(cooldown > 0){
    		cooldown--;
    	}else{
    		if(isProvidingPower){
    			isProvidingPower = false;
    			HelpfulMethods.updateAndNotify(getWorld(), pos, getWorld().getBlockState(pos).getBlock(), 1, true);
    		}
    	}
    }
    
    public void readFromNBT(NBTTagCompound par1NBTTagCompound){ 	
    	super.readFromNBT(par1NBTTagCompound);
    	
        NBTTagList nbttaglist = par1NBTTagCompound.getTagList("Items", 10);
        this.inventoryContents = new ItemStack[this.getSizeInventory()];

        for (int i = 0; i < nbttaglist.tagCount(); ++i)
        {
            NBTTagCompound nbttagcompound1 = nbttaglist.getCompoundTagAt(i);
            int j = nbttagcompound1.getByte("Slot") & 255;

            if (j >= 0 && j < this.inventoryContents.length)
            {
                this.inventoryContents[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
            }
        }
        

    	if(par1NBTTagCompound.hasKey("cooldown")){
    	  	this.cooldown = par1NBTTagCompound.getInteger("cooldown");
      	}
    	
    	if(par1NBTTagCompound.hasKey("type")){
    	  	this.type = par1NBTTagCompound.getString("type");
      	}
    	
    }

    public void writeToNBT(NBTTagCompound par1NBTTagCompound){   
    	super.writeToNBT(par1NBTTagCompound);
    	
        NBTTagList nbttaglist = new NBTTagList();

        for (int i = 0; i < this.inventoryContents.length; ++i)
        {
            if (this.inventoryContents[i] != null)
            {
                NBTTagCompound nbttagcompound1 = new NBTTagCompound();
                nbttagcompound1.setByte("Slot", (byte)i);
                this.inventoryContents[i].writeToNBT(nbttagcompound1);
                nbttaglist.appendTag(nbttagcompound1);
            }
        }

        par1NBTTagCompound.setTag("Items", nbttaglist);
        
        par1NBTTagCompound.setInteger("cooldown", cooldown);
        
        par1NBTTagCompound.setString("type", type);
        
    }
    
	public int getSizeInventory() {
		return 19;
	}

	public ItemStack decrStackSize(int par1, int par2)
    {
        if (this.inventoryContents[par1] != null)
        {
            ItemStack itemstack;

            if (this.inventoryContents[par1].stackSize <= par2)
            {
                itemstack = this.inventoryContents[par1];
                this.inventoryContents[par1] = null;
                this.markDirty();
                return itemstack;
            }
            else
            {
                itemstack = this.inventoryContents[par1].splitStack(par2);

                if (this.inventoryContents[par1].stackSize == 0)
                {
                    this.inventoryContents[par1] = null;
                }

                this.markDirty();
                return itemstack;
            }
        }
        else
        {
            return null;
        }
    }

	/**
     * When some containers are closed they call this on each slot, then drop whatever it returns as an EntityItem -
     * like when you close a workbench GUI.
     */
    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (this.inventoryContents[par1] != null)
        {
            ItemStack itemstack = this.inventoryContents[par1];
            this.inventoryContents[par1] = null;
            return itemstack;
        }
        else
        {
            return null;
        }
    }

	public ItemStack getStackInSlot(int var1) {
		return this.inventoryContents[var1];
	}
	
	/**
	 * Copy of getStackInSlot which doesn't get overrided by CustomizableSCTE.
	 */
	
	public ItemStack getStackInSlotCopy(int var1) {
		return this.inventoryContents[var1];
	}

	public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
		this.inventoryContents[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
        {
            par2ItemStack.stackSize = this.getInventoryStackLimit();
        }

        this.markDirty();
	}

	public boolean hasCustomInventoryName() {
		return true;
	}
    
	public int getInventoryStackLimit() {
		return 1;
	}

	public boolean isUseableByPlayer(EntityPlayer var1) {
		return true;
	}

	public void openInventory() {}

	public void closeInventory() {}

	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}
	
	public String getType(){
		return type;
	}
	
	public void setType(String type){
		this.type = type;
	}

	public boolean shouldProvidePower() {
		return (this.type.matches("redstone") && this.isProvidingPower) ? true : false;
	}

	public void setShouldProvidePower(boolean isProvidingPower) {
		this.isProvidingPower = isProvidingPower;
	}

	public int getCooldown() {
		return cooldown;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}
	
	public ItemStack[] getContents(){
		return inventoryContents;
	}
	
	public void setContents(ItemStack[] contents){
		this.inventoryContents = contents;
	}
	
	public void onModuleInserted(ItemStack stack, EnumCustomModules module){
		if(!this.getWorld().isRemote){
			if(this.getWorld().getTileEntity(pos.east(2)) != null && this.getWorld().getTileEntity(pos.east(2)) instanceof TileEntityInventoryScanner){
				if(!((CustomizableSCTE) this.getWorld().getTileEntity(pos.east(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.east(2))).insertModule(stack);
				}
			}else if(this.getWorld().getTileEntity(pos.west(2)) != null && this.getWorld().getTileEntity(pos.west(2)) instanceof TileEntityInventoryScanner){
				if(!((CustomizableSCTE) this.getWorld().getTileEntity(pos.west(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.west(2))).insertModule(stack);
				}
			}else if(this.getWorld().getTileEntity(pos.south(2)) != null && this.getWorld().getTileEntity(pos.south(2)) instanceof TileEntityInventoryScanner){
				if(!((CustomizableSCTE) this.getWorld().getTileEntity(pos.south(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.south(2))).insertModule(stack);
				}
			}else if(this.getWorld().getTileEntity(pos.north(2)) != null && this.getWorld().getTileEntity(pos.north(2)) instanceof TileEntityInventoryScanner){
				if(!((CustomizableSCTE) this.getWorld().getTileEntity(pos.north(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.north(2))).insertModule(stack);
				}
			}
		}
	}
	
	public void onModuleRemoved(ItemStack stack, EnumCustomModules module){
		if(!this.getWorld().isRemote){
			if(this.getWorld().getTileEntity(pos.east(2)) != null && this.getWorld().getTileEntity(pos.east(2)) instanceof TileEntityInventoryScanner){
				if(((CustomizableSCTE) this.getWorld().getTileEntity(pos.east(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.east(2))).removeModule(module);
				}
			}else if(this.getWorld().getTileEntity(pos.west(2)) != null && this.getWorld().getTileEntity(pos.west(2)) instanceof TileEntityInventoryScanner){
				if(((CustomizableSCTE) this.getWorld().getTileEntity(pos.west(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.west(2))).removeModule(module);
				}
			}else if(this.getWorld().getTileEntity(pos.south(2)) != null && this.getWorld().getTileEntity(pos.south(2)) instanceof TileEntityInventoryScanner){
				if(((CustomizableSCTE) this.getWorld().getTileEntity(pos.south(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.south(2))).removeModule(module);
				}
			}else if(this.getWorld().getTileEntity(pos.north(2)) != null && this.getWorld().getTileEntity(pos.north(2)) instanceof TileEntityInventoryScanner){
				if(((CustomizableSCTE) this.getWorld().getTileEntity(pos.north(2))).hasModule(module)){
					((CustomizableSCTE) this.getWorld().getTileEntity(pos.north(2))).removeModule(module);
				}
			}
		}
	}

	protected EnumCustomModules[] getCustomizableOptions() {
		return new EnumCustomModules[]{EnumCustomModules.WHITELIST, EnumCustomModules.SMART};
	}
    
}
