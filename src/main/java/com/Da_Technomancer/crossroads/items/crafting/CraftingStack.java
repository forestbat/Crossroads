package com.Da_Technomancer.crossroads.items.crafting;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CraftingStack implements ICraftingStack{

	private final Item item;
	private final int count;
	private final int meta;

	/** A metadata of -1 means ignore metadata
	 * 
	 */
	public CraftingStack(Block block, int count, int meta){
		this(Item.getItemFromBlock(block), count, meta);
	}

	/** A metadata of -1 means ignore metadata
	 * 
	 */
	public CraftingStack(Item item, int count, int meta){
		this.item = item;
		this.count = count;
		this.meta = meta;
	}

	@Override
	public boolean match(ItemStack stack){
		if(stack == null){
			return false;
		}

		if(stack.getItem() == item && stack.stackSize == count && (meta == -1 || stack.getMetadata() == meta)){
			return true;
		}

		return false;
	}

	@Override
	public boolean softMatch(ItemStack stack){
		if(stack == null){
			return false;
		}

		if(stack.getItem() == item && (meta == -1 || stack.getMetadata() == meta)){
			return true;
		}

		return false;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public List<ItemStack> getMatchingList(){
		if(meta != -1 || !item.getHasSubtypes()){
			return ImmutableList.of(new ItemStack(item, count, meta));
		}
		ArrayList<ItemStack> list = new ArrayList<ItemStack>();
		item.getSubItems(item, null, list);
		return list;
	}
	
	protected Item getItem(){
		return item;
	}
	
	protected int getCount(){
		return count;
	}
	
	protected int getMeta(){
		return meta;
	}
	
	@Override
	public boolean equals(Object other){
		if(other == this){
			return true;
		}
		if(other instanceof CraftingStack){
			CraftingStack otherStack = (CraftingStack) other;
			return item == otherStack.getItem() && meta == otherStack.getMeta() && count == otherStack.getCount();
		}
		
		return false;
	}
}
