package com.zdream.famitracker.document.instrument;

import com.zdream.famitracker.components.DocumentFile;

public abstract class Instrument implements Cloneable {
	
	public static final byte
			INST_NONE = 0,
			INST_2A03 = 1,
			INST_VRC6 = 2,
			INST_VRC7 = 3,
			INST_FDS = 4,
			INST_N163 = 5,
			INST_S5B = 6;
	
	protected Instrument(byte type) {
		this.m_iType = type;
	}
	
	public void setName(final String name) {
		this.m_cName = name;
	}
	
	public String getName() {
		return m_cName;
	}
	
	/**
	 * Returns instrument type
	 */
	public final byte getType() {
		return m_iType;
	}
	
	/**
	 * Setup some initial values
	 */
	public abstract void setup();
	
	/**
	 * Saves the instrument to the module
	 */
	public void store(DocumentFile pDocFile) {
		throw new RuntimeException("我不允许你调用");
	}
	
	/**
	 * Loads the instrument from a module
	 * @return
	 */
	public abstract boolean load(DocumentFile pDocFile);
	
	/**
	 * Saves to an FTI file
	 */
	public void saveFile(/*InstrumentFile pFile, FamiTrackerDoc pDoc*/) {
		throw new RuntimeException("我不允许你调用");
	}
	
	/**
	 * Loads from an FTI file
	 * @return
	 */
	public abstract boolean loadFile(/*InstrumentFile pFile, int iVersion, FamiTrackerDoc pDoc*/);
	
	/**
	 * Compiles the instrument for NSF generation
	 * @return
	 */
	public int compile(/*FamiTrackerDoc pDoc, Chunk pChunk, int index*/) {
		throw new RuntimeException("我不允许你调用");
	}
	
	public abstract boolean canRelease();
	
	/**
	 * 该方法调用了 MFC
	 */
	protected void InstrumentChanged() {
		// Set modified flag
		
//		CFrameWnd *pFrameWnd = dynamic_cast<CFrameWnd*>(AfxGetMainWnd());
//		if (pFrameWnd != NULL) {
//			CDocument *pDoc = pFrameWnd->GetActiveDocument();
//			if (pDoc != NULL)
//				pDoc->SetModifiedFlag();
//		}
	}
	
	private String m_cName;
	
	protected byte m_iType;
	
}
