package com.zdream.famitracker.document;

/**
 * DPCM sample class
 * @author Zdream
 */
public class DSample implements Cloneable {

	/**
	 * Max size of a sample as supported by the NES, in bytes
	 */
	public static final int MAX_SIZE = 0x0FF1;
	
	/**
	 * Size of sample name
	public static final int MAX_NAME_SIZE = 256;*/

	/**
	 * Empty constructor
	 */
	public DSample() {
		this(0, null);
	}
	
	public DSample(int size) {
		this(size, null);
	}
	
	/**
	 * Unnamed sample constructor
	 * @param size unsigned
	 */
	public DSample(int size, byte[] pData) {
		this.m_pSampleData = pData;
		
		if (pData == null) {
			m_pSampleData = new byte[size];
		}
		
		m_Name = "";
	}
	
	public DSample clone() {
		DSample sample = new DSample();
		clone(sample);
		return sample;
	}

	/**
	 * <p>从原先 C++ 文件中复制构造函数中翻译过来的
	 * <p>将自己作为模板, 将数据复制到 sample 中
	 */
	protected void clone(DSample sample) {
		sample.m_pSampleData = new byte[this.m_pSampleData.length];
		
		assert(sample.m_pSampleData.length != 0);
		System.arraycopy(m_pSampleData, 0, sample.m_pSampleData, 0, m_pSampleData.length);
		sample.m_Name = m_Name;
	}

	/**
	 * Copy from existing sample
	 * @param pDSample
	 */
	public void copy(final DSample pDSample) {
		assert(pDSample != null);
		
		m_pSampleData = new byte[pDSample.m_pSampleData.length];
		System.arraycopy(pDSample.m_pSampleData, 0, m_pSampleData, 0, pDSample.m_pSampleData.length);
	}
	
	/**
	 * Allocate memory, optionally copy data
	 * @param pData 不能为空
	 */
	public void allocate(final byte[] pData) {
		m_pSampleData = new byte[pData.length];
		
		if (pData != null) {
			System.arraycopy(pData, 0, m_pSampleData, 0, pData.length);
		}
	}
	
	/**
	 * Allocate memory, 相当于重新分配 sample 数组的空间
	 * @param pData default null
	 */
	public void allocate(final int size) {
		m_pSampleData = new byte[size];
	}

	/**
	 * Clear sample data
	 */
	public void clear() {
		m_pSampleData = null;
	}

	/**
	 * Set sample data and size, the object will own the memory area assigned
	 * @param pData
	 *   数据源. 因为不会做拷贝, 只是将引用赋值给该类
	 */
	public void setData(byte[] pData) {
		assert(pData != null);
		m_pSampleData = pData;
	}

	/**
	 * Get sample size
	 * @return
	 */
	public final int getSize() {
		return m_pSampleData.length;
	}
	
	/**
	 * Get sample data
	 * @return
	 */
	public final byte[] getData() {
		return m_pSampleData;
	}
	
	/**
	 * Set sample name
	 */
	public void setName(final String pName) {
		this.m_Name = pName;
	}
	
	/**
	 * Get sample name
	 */
	public final String getName() {
		return m_Name;
	}

	/**
	 * 数据存储在这里
	 */
	private byte[] m_pSampleData;
	
	private String m_Name;
}
