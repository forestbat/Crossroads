package com.Da_Technomancer.crossroads.client.TESR;

import com.Da_Technomancer.crossroads.Main;
import com.Da_Technomancer.crossroads.API.Capabilities;
import com.Da_Technomancer.crossroads.API.Properties;
import com.Da_Technomancer.crossroads.client.TESR.models.ModelAxle;
import com.Da_Technomancer.crossroads.client.TESR.models.ModelGearOctagon;
import com.Da_Technomancer.crossroads.tileentities.rotary.ToggleGearTileEntity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class ToggleGearRenderer extends TileEntitySpecialRenderer<ToggleGearTileEntity>{

	private final ModelGearOctagon modelOct = new ModelGearOctagon();
	private final ResourceLocation res = new ResourceLocation(Main.MODID + ":textures/model/gearOct.png");
	private final ResourceLocation textureAx = new ResourceLocation(Main.MODID + ":textures/model/axle.png");
	private final ModelAxle modelAx = new ModelAxle();
	
	@Override
	public void renderTileEntityAt(ToggleGearTileEntity gear, double x, double y, double z, float partialTicks, int destroyStage){
		
		if(!gear.getWorld().isBlockLoaded(gear.getPos(), false)){
			return;
		}

		if(gear.getMember() == null){
			return;
		}
		
		GlStateManager.pushMatrix();

		GlStateManager.pushAttrib();
		GlStateManager.disableLighting();
		GlStateManager.translate(x + .5D, y + .5D, z + .5D);
		if(!gear.getWorld().getBlockState(gear.getPos()).getValue(Properties.REDSTONE_BOOL)){
			GlStateManager.translate(0F, .5F, 0F);
		}
		GlStateManager.rotate(-(float) gear.getCapability(Capabilities.AXLE_HANDLER_CAPABILITY, EnumFacing.DOWN).getAngle(), 0F, 1F, 0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(res);
		modelOct.render(res, gear.getMember().getColor());
		GlStateManager.enableLighting();
		GlStateManager.popAttrib();
		GlStateManager.popMatrix();
		
		GlStateManager.pushMatrix();
		GlStateManager.color(1, 1, 1);
		GlStateManager.translate(x, y, z);
		GlStateManager.translate(.5F, .375F, .5F);
		GlStateManager.scale(1D, .75D, 1D);
		GlStateManager.translate(0, -.125F, 0);
		GlStateManager.rotate((float) -gear.getCapability(Capabilities.AXLE_HANDLER_CAPABILITY, EnumFacing.DOWN).getAngle(), 0F, 1F, 0F);
		Minecraft.getMinecraft().renderEngine.bindTexture(textureAx);
		modelAx.render();
		GlStateManager.popMatrix();
	}
}
