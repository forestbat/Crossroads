package com.Da_Technomancer.crossroads.blocks.fluid;

import java.util.List;

import javax.annotation.Nullable;

import com.Da_Technomancer.crossroads.API.IBlockCompare;
import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.items.ModItems;
import com.Da_Technomancer.crossroads.tileentities.fluid.FluidTankTileEntity;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FluidTank extends BlockContainer implements IBlockCompare{
	
	public FluidTank(){
		super(Material.IRON);
		String name = "fluidTank";
		setUnlocalizedName(name);
		setRegistryName(name);
		setSoundType(SoundType.METAL);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this).setRegistryName(name));
		this.setCreativeTab(ModItems.tabCrossroads);
		this.setHardness(3);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new FluidTankTileEntity();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced){
		if(stack.hasTagCompound() && stack.getTagCompound().hasKey("FluidName")){
			tooltip.add("Contains: " + FluidStack.loadFluidStackFromNBT(stack.getTagCompound()).amount + "mB of " + FluidStack.loadFluidStackFromNBT(stack.getTagCompound()).getLocalizedName());
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stackIn){
		if(!(te instanceof FluidTankTileEntity) || te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).getTankProperties()[0].getContents() == null){
			super.harvestBlock(worldIn, player, pos, state, te, stackIn);
		}else{
			player.addExhaustion(0.025F);
			ItemStack stack = new ItemStack(Item.getItemFromBlock(this), 1);
			stack.setTagCompound(te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).getTankProperties()[0].getContents().writeToNBT(new NBTTagCompound()));
			spawnAsEntity(worldIn, pos, stack);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
		if(stack.hasTagCompound()){
			FluidTankTileEntity te = (FluidTankTileEntity) world.getTileEntity(pos);
			te.setContent(FluidStack.loadFluidStackFromNBT(stack.getTagCompound()));
		}
	}

	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ){
		if(!worldIn.isRemote){
			return FluidUtil.interactWithFluidHandler(heldItem, worldIn.getTileEntity(pos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null), playerIn);
		}

		return FluidUtil.getFluidHandler(heldItem) != null;
	}
	
	@Override
	public int damageDropped(IBlockState state){
		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, new IProperty[] {Properties.REDSTONE});
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		return this.getDefaultState().withProperty(Properties.REDSTONE, meta);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(Properties.REDSTONE);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state){
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos){
		return blockState.getValue(Properties.REDSTONE);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public double getOutput(World worldIn, BlockPos pos){
		IFluidTankProperties fluid = worldIn.getTileEntity(pos).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null).getTankProperties()[0];
		return fluid.getContents() == null ? 0 : 15D * (double) fluid.getContents().amount / (double) fluid.getCapacity();
	}
}
