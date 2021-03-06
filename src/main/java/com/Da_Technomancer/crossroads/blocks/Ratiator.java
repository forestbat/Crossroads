package com.Da_Technomancer.crossroads.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import com.Da_Technomancer.crossroads.API.IBlockCompare;
import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.items.ModItems;
import com.Da_Technomancer.crossroads.tileentities.RatiatorTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockRedstoneDiode;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class Ratiator extends BlockContainer{

	private static final AxisAlignedBB BB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.125D, 1.0D);

	protected Ratiator(){
		super(Material.CIRCUITS);
		String name = "ratiator";
		setUnlocalizedName(name);
		setRegistryName(name);
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this).setRegistryName(name));
		setHardness(0);
		setCreativeTab(ModItems.tabCrossroads);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new RatiatorTileEntity();
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos){
		return BB;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean isFullCube(IBlockState state){
		return false;
	}
	
	@Override
	public boolean canPlaceBlockAt(World worldIn, BlockPos pos){
		return worldIn.isSideSolid(pos.offset(EnumFacing.DOWN), EnumFacing.UP);
	}
	
	@Override
	public int getStrongPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side){
		return state.getWeakPower(blockAccess, pos, side);
	}

	@Override
	public int getWeakPower(IBlockState state, IBlockAccess blockAccess, BlockPos pos, EnumFacing side){
		if(side == state.getValue(Properties.FACING).getOpposite()){
			double d = getPowerOut(blockAccess, pos);
			if(d >= 15){
				return 15;
			}
			return (int) Math.round(d);
		}else{
			return 0;
		}
	}
	
	@Override
	public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn){
		if(!canPlaceBlockAt(worldIn, pos)){
			dropBlockAsItem(worldIn, pos, state, 0);
			worldIn.setBlockToAir(pos);
			return;
		}

		if (!worldIn.isBlockTickPending(pos, this)){
			int i = -1;

			if (BlockRedstoneDiode.isDiode(worldIn.getBlockState(pos.offset(state.getValue(Properties.FACING))))){
				i = -3;
			}
			
			worldIn.updateBlockTick(pos, this, 2, i);
		}
	}

	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack){
		neighborChanged(state, worldIn, pos, null);
	}
	
	public double getPowerOnSide(World worldIn, BlockPos pos, EnumFacing side, boolean allowAll){
		IBlockState state = worldIn.getBlockState(pos.offset(side));
		Block block = state.getBlock();
		if(allowAll){
			if(block instanceof IBlockCompare){
				return ((IBlockCompare) block).getOutput(worldIn, pos.offset(side));
			}
			if(state.hasComparatorInputOverride()){
				return state.getComparatorInputOverride(worldIn, pos.offset(side));
			}
		}
		return allowAll ? block == this ? getPowerOut(worldIn, pos.offset(side)) : Math.max(worldIn.getRedstonePower(pos.offset(side), side), block == Blocks.REDSTONE_WIRE ? state.getValue(BlockRedstoneWire.POWER) : 0) : block == this ? getPowerOut(worldIn, pos.offset(side)) : block == Blocks.REDSTONE_BLOCK ? 15 : (block == Blocks.REDSTONE_WIRE ? (int) state.getValue(BlockRedstoneWire.POWER) : worldIn.getStrongPower(pos.offset(side), side));
	}

	private double getPowerOut(IBlockAccess worldIn, BlockPos pos){
		return ((RatiatorTileEntity) worldIn.getTileEntity(pos)).getOutput();
	}
	
	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand){
		double lastOut = ((RatiatorTileEntity) worldIn.getTileEntity(pos)).getOutput();
		double sidePower = Math.max(getPowerOnSide(worldIn, pos, state.getValue(Properties.FACING).rotateY(), false), getPowerOnSide(worldIn, pos, state.getValue(Properties.FACING).getOpposite().rotateY(), false));
		((RatiatorTileEntity) worldIn.getTileEntity(pos)).setOutput(state.getValue(Properties.REDSTONE_BOOL) ? getPowerOnSide(worldIn, pos, state.getValue(Properties.FACING).getOpposite(), true) / (sidePower == 0 ? 1D : sidePower) : getPowerOnSide(worldIn, pos, state.getValue(Properties.FACING).getOpposite(), true) * sidePower);
		if(lastOut != ((RatiatorTileEntity) worldIn.getTileEntity(pos)).getOutput()){
			worldIn.notifyBlockOfStateChange(pos.offset(state.getValue(Properties.FACING)), this);
	        worldIn.notifyNeighborsOfStateExcept(pos.offset(state.getValue(Properties.FACING)), this, state.getValue(Properties.FACING).getOpposite());
		}
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ){
		if(!worldIn.isRemote){
			worldIn.setBlockState(pos, state.withProperty(Properties.REDSTONE_BOOL, !state.getValue(Properties.REDSTONE_BOOL)));
			neighborChanged(state.withProperty(Properties.REDSTONE_BOOL, !state.getValue(Properties.REDSTONE_BOOL)), worldIn, pos, null);
		}
		return true;
	}
	
	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side){
		return side != null && side.getAxis() != EnumFacing.Axis.Y;
	}
	
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer(){
		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, new IProperty[] {Properties.FACING, Properties.REDSTONE_BOOL});
	}
	
	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(Properties.FACING).getIndex()  + (state.getValue(Properties.REDSTONE_BOOL) ? 8 : 0);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta){
		return getDefaultState().withProperty(Properties.FACING, EnumFacing.getFront(meta & 7)).withProperty(Properties.REDSTONE_BOOL, meta >= 8);
	}
	
	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		EnumFacing enumfacing = (placer == null) ? EnumFacing.NORTH : placer.getHorizontalFacing();
		return getDefaultState().withProperty(Properties.FACING, enumfacing).withProperty(Properties.REDSTONE_BOOL, false);
	}
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}
	
	@Override
	public int damageDropped(IBlockState state){
		return 0;
	}
}
