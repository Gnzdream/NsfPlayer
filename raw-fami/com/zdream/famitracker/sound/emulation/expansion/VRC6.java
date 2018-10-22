package com.zdream.famitracker.sound.emulation.expansion;

import com.zdream.famitracker.sound.emulation.Mixer;
import com.zdream.famitracker.test.FamitrackerLogger;

import static com.zdream.famitracker.sound.emulation.Types.*;

public class VRC6 implements External {
	
	private Mixer pMixer;
	
	VRC6Pulse m_pPulse1, m_pPulse2;
	VRC6Sawtooth m_pSawtooth;

	public VRC6(Mixer pMixer) {
		this.pMixer = pMixer;
		
		m_pPulse1 = new VRC6Pulse(pMixer, CHANID_VRC6_PULSE1);
		m_pPulse2 = new VRC6Pulse(pMixer, CHANID_VRC6_PULSE2);
		m_pSawtooth = new VRC6Sawtooth(pMixer, CHANID_VRC6_SAWTOOTH);
	}
	
	@Override
	public Mixer getMixer() {
		return this.pMixer;
	}

	@Override
	public void reset() {
		m_pPulse1.reset();
		m_pPulse2.reset();
		m_pSawtooth.reset();
	}

	@Override
	public void process(int time) {
		m_pPulse1.process(time);
		m_pPulse2.process(time);
		m_pSawtooth.process(time);
	}

	@Override
	public void endFrame() {
		m_pPulse1.endFrame();
		m_pPulse2.endFrame();
		m_pSawtooth.endFrame();
	}

	@Override
	public void write(int address, int value) {
		switch (address) {
		case 0x9000:
		case 0x9001:
		case 0x9002:
			m_pPulse1.write(address & 3, value);
			break;			
		case 0xA000:
		case 0xA001:
		case 0xA002:
			m_pPulse2.write(address & 3, value);
			break;
		case 0xB000:
		case 0xB001:
		case 0xB002:
			m_pSawtooth.write(address & 3, value);
			break;
		}
		
		// LOG
		int i = 0;
		switch (address) {
		case 0x9000: i = 0x10; break;
		case 0x9001: i = 0x11; break;
		case 0x9002: i = 0x12; break;
		case 0xA000: i = 0x20; break;
		case 0xA001: i = 0x21; break;
		case 0xA002: i = 0x22; break;
		case 0xB000: i = 0x30; break;
		case 0xB001: i = 0x31; break;
		case 0xB002: i = 0x32; break;
		}
		FamitrackerLogger.instance.logWriteAddress("VRC6", i, value);
	}

	@Override
	public int read(int address) {
		return 0;
	}

	@Override
	public boolean isMapped(int address) {
		return false;
	}

}
