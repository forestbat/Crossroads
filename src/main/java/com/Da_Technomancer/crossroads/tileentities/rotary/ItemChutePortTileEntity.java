package com.Da_Technomancer.crossroads.tileentities.rotary;

import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.blocks.ModBlocks;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ItemChutePortTileEntity extends TileEntity implements ITickable{

	private ItemStack inventory;

	@Override
	public void update(){
		if(worldObj.isRemote){
			return;
		}

		if(isSpotInvalid() && inventory != null){
			worldObj.spawnEntityInWorld(new EntityItem(worldObj, pos.offset(worldObj.getBlockState(pos).getValue(Properties.FACING)).getX(), pos.getY(), pos.offset(worldObj.getBlockState(pos).getValue(Properties.FACING)).getZ(), inventory.copy()));
			inventory = null;
		}else{
			EnumFacing side = worldObj.getBlockState(pos).getValue(Properties.FACING).rotateAround(Axis.Y);
			if(inventory != null && getOutput() != null && worldObj.getTileEntity(pos.offset(side)) != null && worldObj.getTileEntity(pos.offset(side)).hasCapability(Capabilities.AXLE_HANDLER_CAPABILITY, side.getOpposite())){
				EnumFacing facing = worldObj.getBlockState(getOutput()).getValue(Properties.FACING);
				if(Math.abs(worldObj.getTileEntity(pos.offset(side)).getCapability(Capabilities.AXLE_HANDLER_CAPABILITY, side.getOpposite()).getMotionData()[0]) > .1D && Math.abs(worldObj.getTileEntity(pos.offset(side)).getCapability(Capabilities.AXLE_HANDLER_CAPABILITY, side.getOpposite()).getMotionData()[1]) > .5D){
					worldObj.getTileEntity(pos.offset(side)).getCapability(Capabilities.AXLE_HANDLER_CAPABILITY, side.getOpposite()).addEnergy(-.5D, false, false);
					EntityItem ent = new EntityItem(worldObj, getOutput().getX() + (facing == EnumFacing.EAST ? 1.5D : facing == EnumFacing.WEST ? -.5D : facing == EnumFacing.NORTH || facing == EnumFacing.SOUTH ? .5D : 0), getOutput().getY(), getOutput().getZ() + (facing == EnumFacing.SOUTH ? 1.5D : facing == EnumFacing.EAST || facing == EnumFacing.WEST ? .5D : facing == EnumFacing.NORTH ? -.5D : 0), inventory.copy());
					ent.motionX = 0;
					ent.motionZ = 0;
					worldObj.spawnEntityInWorld(ent);
					inventory = null;
					markDirty();
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt){
		super.writeToNBT(nbt);

		if(inventory != null){
			nbt.setTag("inv", inventory.writeToNBT(new NBTTagCompound()));
		}
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt){
		super.readFromNBT(nbt);

		if(nbt.hasKey("inv")){
			inventory = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("inv"));
		}
	}

	private boolean isSpotInvalid(){
		if(worldObj.getTileEntity(pos.offset(EnumFacing.DOWN)) instanceof ItemChutePortTileEntity || worldObj.getBlockState(pos.offset(EnumFacing.DOWN)).getBlock() == ModBlocks.itemChute){
			return true;
		}
		return false;
	}

	private BlockPos getOutput(){

		boolean contin = true;
		int height = 1;

		while(contin){

			if(worldObj.getTileEntity(pos.offset(EnumFacing.UP, height)) instanceof ItemChutePortTileEntity){
				return pos.offset(EnumFacing.UP, height);
			}

			if(worldObj.getBlockState(pos.offset(EnumFacing.UP, height)).getBlock() != ModBlocks.itemChute){
				return null;
			}
			if(++height > 255){
				break;
			}
		}
		return null;
	}

	@Override
	public boolean hasCapability(Capability<?> cap, EnumFacing facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getOutput() != null && (facing == null || facing == worldObj.getBlockState(pos).getValue(Properties.FACING))){
			return true;
		}

		return super.hasCapability(cap, facing);
	}

	private final InventoryHandler handler = new InventoryHandler();

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> cap, EnumFacing facing){
		if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && getOutput() != null && (facing == null || facing == worldObj.getBlockState(pos).getValue(Properties.FACING))){
			return (T) handler;
		}

		return super.getCapability(cap, facing);
	}

	private class InventoryHandler implements IItemHandler{

		@Override
		public int getSlots(){
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot){
			return slot == 0 ? inventory : null;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate){
			if(slot != 0 || stack == null || stack.stackSize <= 0 || inventory != null){
				return stack;
			}

			if(!simulate){
				inventory = stack.copy();
				inventory.stackSize = 1;
				markDirty();
			}

			ItemStack holder = stack.copy();
			--holder.stackSize;
			return holder;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate){
			return null;
		}
	}
}
