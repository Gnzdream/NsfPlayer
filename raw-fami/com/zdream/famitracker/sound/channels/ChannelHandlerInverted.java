package com.zdream.famitracker.sound.channels;

import static com.zdream.famitracker.FamitrackerTypes.*;

/**
 * <p>Channel handler for channels with frequency registers
 * <p>这个类在 ChannelHandler.h 中
 * @author Zdream
 */
public abstract class ChannelHandlerInverted extends ChannelHandler {

	public ChannelHandlerInverted(int maxPeriod, int maxVolume) {
		super(maxPeriod, maxVolume);
	}

	@Override
	protected void setupSlide(int type, int effParam) {
		super.setupSlide(type, effParam);

		// Invert slide effects
		if (m_iEffect == EF_SLIDE_DOWN)
			m_iEffect = EF_SLIDE_UP;
		else
			m_iEffect = EF_SLIDE_DOWN;
	}
	
	@Override
	protected int calculatePeriod() {
		return limitPeriod(getPeriod() + getVibrato() - getFinePitch() - getPitch());
	}

}
