package com.Da_Technomancer.crossroads.API.effects;

import com.Da_Technomancer.crossroads.blocks.ModBlocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LightEffect implements IEffect{

	@Override
	public void doEffect(World worldIn, BlockPos pos, double mult){
		IBlockState state = worldIn.getBlockState(pos);
		if(state.getMaterial() == Material.ROCK){
			worldIn.setBlockState(pos, Blocks.GLOWSTONE.getDefaultState());
		}else if(state.getMaterial() == Material.GLASS && state.getBlock() != Blocks.GLOWSTONE){
			worldIn.setBlockState(pos, ModBlocks.glowGlass.getDefaultState());
		}
	}

	public static class VoidLightEffect implements IEffect{

		@Override
		public void doEffect(World worldIn, BlockPos pos, double mult){
			IBlockState state = worldIn.getBlockState(pos);
			if(state.getBlock() == Blocks.GLOWSTONE){
				worldIn.setBlockState(pos, Blocks.COBBLESTONE.getDefaultState());
			}else if(state.getBlock() == ModBlocks.glowGlass){
				worldIn.setBlockState(pos, Blocks.GLASS.getDefaultState());
			}
		}
	}
}