package com.Da_Technomancer.crossroads.API.rotary;

import javax.annotation.Nonnull;

/**
 * Gears and other rotary connectables use two capabilities, ICogHandler and IAxleHandler. 
 * The AxleHandler represents the core of the block, that can connect to machines and axles.
 * The CogHandler represents the part of the block able to connect to other blocks.
 *
 * In most cases, the AxleHandler and CogHandler are on the same side, though there are exceptions. Some blocks may only have one of them.
 */
public interface IAxleHandler{

	/**
	 * [0]=w, [1]=E, [2]=P, [3]=lastE
	 */
	public double[] getMotionData();

	/**
	 * If lastRadius equals 0, then the AxleHandler should not convert the rotationRationIn based on radius.
	 */
	public void propogate(@Nonnull ITileMasterAxis masterIn, byte key, double rotationRatioIn, double lastRadius);

	/**
	 * [0]=m, [1]=I
	 */
	public double[] getPhysData();
	
	public double getRotationRatio();

	public void resetAngle();

	public double getAngle();

	/**
	 * negative value decreases energy. For non-gears (or axises) affecting the
	 * network absolute controls whether the change is relative or absolute (to
	 * spin direction)
	 */
	public void addEnergy(double energy, boolean allowInvert, boolean absolute);
}
