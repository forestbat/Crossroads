package com.Da_Technomancer.crossroads.blocks.rotary;

import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.items.ModItems;
import com.Da_Technomancer.crossroads.tileentities.rotary.CrystalMasterAxisTileEntity;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class CrystalMasterAxis extends BlockContainer{
	
	public CrystalMasterAxis(){
		super(Material.ROCK);
		setUnlocalizedName("masterAxisCrystal");
		setRegistryName("masterAxisCrystal");
		GameRegistry.register(this);
		GameRegistry.register(new ItemBlock(this).setRegistryName("masterAxisCrystal"));
		this.setCreativeTab(ModItems.tabCrossroads);
		this.setHardness(3);
		setSoundType(SoundType.STONE);
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing blockFaceClickedOn, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer){
		EnumFacing enumfacing = (placer == null) ? EnumFacing.NORTH : BlockPistonBase.getFacingFromEntity(pos, placer);
		return this.getDefaultState().withProperty(Properties.FACING, enumfacing);
	}

	@Override
	public int damageDropped(IBlockState state){
		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState(){
		return new BlockStateContainer(this, new IProperty[] {Properties.FACING});
	}

	@Override
	public IBlockState getStateFromMeta(int meta){
		EnumFacing facing = EnumFacing.getFront(meta);
		return this.getDefaultState().withProperty(Properties.FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state){
		return state.getValue(Properties.FACING).getIndex();
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta){
		return new CrystalMasterAxisTileEntity(EnumFacing.getFront(meta));

	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state){
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot){
		return state.withProperty(Properties.FACING, rot.rotate(state.getValue(Properties.FACING)));
	}

	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn){
		return state.withRotation(mirrorIn.toRotation(state.getValue(Properties.FACING)));
	}

}
