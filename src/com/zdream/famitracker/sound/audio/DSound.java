package com.zdream.famitracker.sound.audio;

/**
 * 原工程用的是 Direct Sound, 这里需要改成用 javax
 * @author Zdream
 */
public class DSound {
	
	public static final byte
		BUFFER_NONE = 0,
		BUFFER_CUSTOM_EVENT = 1, 
		BUFFER_TIMEOUT = 2, 
		BUFFER_IN_SYNC = 3, 
		BUFFER_OUT_OF_SYNC = 4;

	public DSound() {
		// do nothing
	}
	
	int m_iDevices;
	
	/**
	 * return false 的话, 请抛异常
	 * @param iDevice
	 */
	public void setupDevice(int iDevice) throws RuntimeException {
		if (iDevice > (int) m_iDevices)
			iDevice = 0;
		
		/*if (m_lpDirectSound) {
			m_lpDirectSound->Release();
			m_lpDirectSound = NULL;
		}

		if (FAILED(DirectSoundCreate((LPCGUID)m_pGUIDs[iDevice], &m_lpDirectSound, NULL))) {
			m_lpDirectSound = NULL;
			return false;
		}

		if (FAILED(m_lpDirectSound->SetCooperativeLevel(m_hWndTarget, DSSCL_PRIORITY))) {
			m_lpDirectSound = NULL;
			return false;
		}

		return true;*/
	}

	public void closeChannel(DSoundChannel m_pDSoundChannel) {
		System.out.println("调用了还没有完成的方法: ");
		System.out.println(Thread.currentThread().getStackTrace()[1]);
	}

	public int getDeviceCount() {
		return m_iDevices;
	}

	public DSoundChannel openChannel(int sampleRate, int sampleSize, int i, int bufferLen, int iBlocks) {
		System.out.println("调用了还没有完成的方法: ");
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		return null;
	}

}
