package com.zdream.famitracker.sound.emulation.expansion;

import com.zdream.famitracker.sound.emulation.Mixer;

public abstract class External {

	protected External(Mixer pMixer) {
		this.m_pMixer = pMixer;
	}
	
	public abstract void reset();
	public abstract void process(int time);
	public abstract void endFrame();

	public abstract void write(int address, int value);
	public abstract int read(int address);
	public abstract boolean isMapped();

	protected Mixer m_pMixer;

}
