package com.zdream.famitracker.sound.audio;

public class DSoundChannel {

	public DSoundChannel() {
		
	}
	
	public final boolean play() {
		// Begin playback of buffer
		// TODO
		return false;
	}
	
	public final boolean stop() {
		System.out.println("调用了还没有完成的方法: ");
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		return false;
	}
	
	public final boolean isPlaying() {
		// TODO
		return false;
	}
	
	public boolean clearBuffer() {
		// TODO
		return false;
	}
	
	/**
	 * Fill sound buffer
	 * @param pBuffer
	 *   Pointer(Array) to a buffer with samples
	 * @param samples
	 *   Number of samples, in bytes
	 * @return
	 */
	public boolean writeBuffer(byte[] pBuffer, int samples) {
		
		// TODO
		return false;
	}

	// buffer_event_t WaitForSyncEvent(DWORD dwTimeout) const;

	public final int getBlockSize() {
		return m_iBlockSize;
	}
	
	public final int GetBlockSamples() {
		return m_iBlockSize >> ((m_iSampleSize >> 3) - 1);
	}

	public final int GetBlocks() {
		return m_iBlocks;
	}

	public final int GetBufferLength() {
		return m_iBufferLength;
	}

	public final int GetSampleSize() {
		return m_iSampleSize;
	}

	public final int GetSampleRate() {
		return m_iSampleRate;
	}

	public final int GetChannels() {
		return m_iChannels;
	}

//private:
//	int GetPlayBlock() const;
//	int GetWriteBlock() const;
//
//	void AdvanceWritePointer();

//private:
//	LPDIRECTSOUNDBUFFER	m_lpDirectSoundBuffer;
//	LPDIRECTSOUNDNOTIFY	m_lpDirectSoundNotify;
//
//	HANDLE			m_hEventList[2];
//	HWND			m_hWndTarget;

	// Configuration
	private int m_iSampleSize;
	private int m_iSampleRate;
	private int m_iChannels;
	private int m_iBufferLength;
//	private int m_iSoundBufferSize; // in bytes
	private int m_iBlocks;
	private int m_iBlockSize; // in bytes

	// State
//	private int m_iCurrentWriteBlock;

}
