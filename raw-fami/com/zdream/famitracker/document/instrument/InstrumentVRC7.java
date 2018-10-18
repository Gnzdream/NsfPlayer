package com.zdream.famitracker.document.instrument;

import com.zdream.famitracker.components.DocumentFile;

public class InstrumentVRC7 extends Instrument {

	public InstrumentVRC7() {
		super(INST_VRC7);
		
		
	}
	
	@Override
	public InstrumentVRC7 clone() {
		InstrumentVRC7 pNew = new InstrumentVRC7();

		pNew.setPatch(getPatch());

		System.arraycopy(m_iRegs, 0, pNew.m_iRegs, 0, m_iRegs.length);

		pNew.setName(getName());

		return pNew;
	}

	@Override
	public void setup() {}

	@Override
	public boolean load(DocumentFile pDocFile) {
		m_iPatch = pDocFile.getBlockInt();

		for (int i = 0; i < 8; ++i)
			setCustomReg(i, pDocFile.getBlockChar() & 0xFF);

		return true;
	}

	@Override
	public boolean loadFile() {
		throw new RuntimeException("我不允许你调用");
	}

	@Override
	public boolean canRelease() {
		return false; // This can use release but disable it when previewing notes
	}
	
	public void setPatch(int patch) {
		m_iPatch = patch;
		InstrumentChanged();
	}
	public final int getPatch() {
		return m_iPatch;
	}
	public void setCustomReg(int Reg, int value) {
		m_iRegs[Reg] = value;
		InstrumentChanged();
	}
	public final int getCustomReg(int Reg) {
		return m_iRegs[Reg];
	}

	private int m_iPatch;
	/**
	 * Custom patch settings, length = 8
	 */
	private int[] m_iRegs = {0x01, 0x21, 0x00, 0x00, 0x00, 0xF0, 0x00, 0x0F};

}
