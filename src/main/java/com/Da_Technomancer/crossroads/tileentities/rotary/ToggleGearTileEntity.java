package com.Da_Technomancer.crossroads.tileentities.rotary;

import javax.annotation.Nullable;

import com.Da_Technomancer.crossroads.ModConfig;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.MiscOp;
import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.API.enums.GearTypes;
import com.Da_Technomancer.crossroads.API.packets.IDoubleReceiver;
import com.Da_Technomancer.crossroads.API.packets.ModPackets;
import com.Da_Technomancer.crossroads.API.packets.SendDoubleToClient;
import com.Da_Technomancer.crossroads.API.rotary.IAxleHandler;
import com.Da_Technomancer.crossroads.API.rotary.ICogHandler;
import com.Da_Technomancer.crossroads.API.rotary.ITileMasterAxis;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

public class ToggleGearTileEntity extends TileEntity implements ITickable, IDoubleReceiver{
	
	private GearTypes type;
	private double[] motionData = new double[4];
	private double[] physData = new double[2];
	private double angle;
	private double clientW;
	private double compOut = 0;
	
	public ToggleGearTileEntity(){
		
	}
	
	public ToggleGearTileEntity(GearTypes type){
		this.type = type;
		physData[0] = type.getDensity() / 8D;
		physData[1] = type.getDensity() / 64D;
	}
	
	public GearTypes getMember(){
		return type;
	}
	
	private final int tiers = ModConfig.speedTiers.getInt();
	
	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState){
		return (oldState.getBlock() != newState.getBlock());
	}
	
	@Override
	public void update(){
		if(worldObj.isRemote){
			if(clientW == Double.POSITIVE_INFINITY){
				angle = 0;
			}else if(clientW == Double.NEGATIVE_INFINITY){
				angle = 22.5;
			}else{
				// it's 9 / PI instead of 180 / PI because 20 ticks/second
				angle += clientW * 9D / Math.PI;
			}
		}

		if(!worldObj.isRemote){
			sendWPacket();
			if(compOut != (Math.abs(motionData[1] / physData[1])) * 15D){
				worldObj.updateComparatorOutputLevel(pos, this.blockType);
				compOut = Math.abs(motionData[1] / physData[1]) * 15D;
			}
		}
	}
	
	private void sendWPacket(){
		boolean flag = false;
		if(clientW == Double.POSITIVE_INFINITY || clientW == Double.NEGATIVE_INFINITY){
			flag = true;
		}else if(MiscOp.tiersRound(motionData[0], tiers) != clientW){
			flag = true;
			clientW = MiscOp.tiersRound(motionData[0], tiers);
		}

		if(flag){
			SendDoubleToClient msg = new SendDoubleToClient("w", clientW, pos);
			ModPackets.network.sendToAllAround(msg, new TargetPoint(worldObj.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 512));

			if(clientW == Double.POSITIVE_INFINITY || clientW == Double.NEGATIVE_INFINITY){
				clientW = 0;
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound){
		super.writeToNBT(compound);

		// motionData
		NBTTagCompound motionTags = new NBTTagCompound();
			for(int i = 0; i < 3; i++){
				if(motionData[i] != 0)
					motionTags.setDouble(i + "motion", motionData[i]);
		}
		compound.setTag("motionData", motionTags);

		// member
		if(type != null){
			compound.setString("type", type.name());
		}

		return compound;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound){
		super.readFromNBT(compound);

		// motionData
		NBTTagCompound innerMot = compound.getCompoundTag("motionData");
		for(int i = 0; i < 4; i++){
			this.motionData[i] = (innerMot.hasKey(i + "motion")) ? innerMot.getDouble(i + "motion") : 0;
		}

		//type
		this.type = compound.hasKey("type") ? GearTypes.valueOf(compound.getString("type")) : null;
		if(type != null){
			physData[0] = type.getDensity() / 8D;
			physData[1] = type.getDensity() / 64D;
		}
	}
	
	@Override
	public NBTTagCompound getUpdateTag(){
		NBTTagCompound nbt = super.getUpdateTag();
		if(type != null){
			nbt.setString("type", type.name());
		}
		return nbt;
	}
	
	@Override
	public void receiveDouble(String context, double message){
		if(context.equals("w")){
			clientW = message;
		}
	}
	
	private final IAxleHandler axleHandler = new AxleHandler();
	private final ICogHandler cogHandler = new CogHandler();
	
	@Override
	public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing){
		if(capability == Capabilities.AXLE_HANDLER_CAPABILITY && facing == EnumFacing.DOWN){
			return true;
		}
		if(capability == Capabilities.COG_HANDLER_CAPABILITY && facing == EnumFacing.DOWN && worldObj.getBlockState(pos).getValue(Properties.REDSTONE_BOOL)){
			return true;
		}
		return super.hasCapability(capability, facing);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing){
		if(capability == Capabilities.AXLE_HANDLER_CAPABILITY && facing == EnumFacing.DOWN){
			return (T) axleHandler;
		}
		if(capability == Capabilities.COG_HANDLER_CAPABILITY && facing == EnumFacing.DOWN && worldObj.getBlockState(pos).getValue(Properties.REDSTONE_BOOL)){
			return (T) cogHandler;
		}
		
		return super.getCapability(capability, facing);
	}
	
	private class CogHandler implements ICogHandler{

		@Override
		public void connect(ITileMasterAxis masterIn, byte key, double rotationRatioIn, double lastRadius){
			axleHandler.propogate(masterIn, key, rotationRatioIn, lastRadius);
		}

		@Override
		public IAxleHandler getAxle(){
			return axleHandler;
		}
	}
	
	private class AxleHandler implements IAxleHandler{

		private byte key;
		private double rotRatio;
		
		@Override
		public double[] getMotionData(){
			return motionData;
		}

		@Override
		public void propogate(ITileMasterAxis masterIn, byte keyIn, double rotRatioIn, double lastRadius){
			if(lastRadius != 0){
				rotRatioIn *= -lastRadius / .5D;
			}
			
			//If true, this has already been checked.
			if(key == keyIn){
				//If true, there is rotation conflict.
				if(rotRatio != rotRatioIn){
					masterIn.lock();
				}
				return;
			}
			
			if(masterIn.addToList(this)){
				return;
			}

			rotRatio = rotRatioIn;
			
			if(key == 0){
				resetAngle();
			}
			key = keyIn;
			
			if(worldObj.getTileEntity(pos.offset(EnumFacing.DOWN)) instanceof ITileMasterAxis){
				((ITileMasterAxis) worldObj.getTileEntity(pos.offset(EnumFacing.DOWN))).trigger(key, masterIn, EnumFacing.UP);
			}

			if(worldObj.getBlockState(pos).getValue(Properties.REDSTONE_BOOL)){
				for(int i = 2; i < 6; ++i){
					EnumFacing facing = EnumFacing.getFront(i);
					// Adjacent gears
					if(worldObj.getTileEntity(pos.offset(facing)) != null && worldObj.getTileEntity(pos.offset(facing)).hasCapability(Capabilities.COG_HANDLER_CAPABILITY, EnumFacing.DOWN)){
						worldObj.getTileEntity(pos.offset(facing)).getCapability(Capabilities.COG_HANDLER_CAPABILITY, EnumFacing.DOWN).connect(masterIn, key, rotRatio, .5D);
					}

					// Diagonal gears
					if(!worldObj.getBlockState(pos.offset(facing)).getBlock().isNormalCube(worldObj.getBlockState(pos.offset(facing)), worldObj, pos.offset(facing)) && worldObj.getTileEntity(pos.offset(facing).offset(EnumFacing.DOWN)) != null && worldObj.getTileEntity(pos.offset(facing).offset(EnumFacing.DOWN)).hasCapability(Capabilities.COG_HANDLER_CAPABILITY, facing.getOpposite())){
						worldObj.getTileEntity(pos.offset(facing).offset(EnumFacing.DOWN)).getCapability(Capabilities.COG_HANDLER_CAPABILITY, facing.getOpposite()).connect(masterIn, key, rotRatio, .5D);
					}
				}
			}

			if(worldObj.getTileEntity(pos.offset(EnumFacing.DOWN)) != null && worldObj.getTileEntity(pos.offset(EnumFacing.DOWN)).hasCapability(Capabilities.AXLE_HANDLER_CAPABILITY, EnumFacing.UP)){
				worldObj.getTileEntity(pos.offset(EnumFacing.DOWN)).getCapability(Capabilities.AXLE_HANDLER_CAPABILITY, EnumFacing.UP).propogate(masterIn, key, rotRatio, 0);
			}
		}
		
		@Override
		public double[] getPhysData(){
			return physData;
		}

		@Override
		public void resetAngle(){
			if(!worldObj.isRemote){
				clientW = (MiscOp.posOrNeg(rotRatio) == -1 ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
			}
		}

		@Override
		public double getAngle(){
			return angle;
		}

		@Override
		public void addEnergy(double energy, boolean allowInvert, boolean absolute){
			if(allowInvert && absolute){
				motionData[1] += energy;
			}else if(allowInvert){
				motionData[1] += energy * MiscOp.posOrNeg(motionData[1]);
			}else if(absolute){
				int sign = (int) MiscOp.posOrNeg(motionData[1]);
				motionData[1] += energy;
				if(sign != 0 && MiscOp.posOrNeg(motionData[1]) != sign){
					motionData[1] = 0;
				}
			}else{
				int sign = (int) MiscOp.posOrNeg(motionData[1]);
				motionData[1] += energy * ((double) sign);
				if(MiscOp.posOrNeg(motionData[1]) != sign){
					motionData[1] = 0;
				}
			}
		}

		@Override
		public double getRotationRatio(){
			return rotRatio;
		}
	}
}
