package zdream.nsfplayer.mixer.xgm;

/**
 * <p>Xgm 混音器的线性读写轨道
 * </p>
 * 
 * @author Zdream
 * @since v0.2.10
 */
public final class XgmLinearChannel extends AbstractXgmAudioChannel {
	
	/**
	 * 位置.
	 * 有效位置为 [0, nextWritePtr)
	 */
	int[] pos;
	/**
	 * 值.
	 * 索引等同与 {@link #pos}
	 */
	short[] values;
	
	/**
	 * 总时间跨度, 单位: 时钟
	 */
	int capacity;
	
	/*
	 * 线性写部分
	 */
	
	/**
	 * 指向 pos 填充的下一个索引
	 */
	int nextWritePtr;
	/**
	 * 上一个写入的数据
	 */
	short lastWriteValue;
	
	/*
	 * 线性读部分
	 */
	
	/**
	 * 上一个读数据的索引.
	 */
	int lastReadPtr;
	
	/**
	 * 当前帧的输入采样率 / 输出采样数
	 */
	float param;
	
	/* **********
	 * 公共方法 *
	 ********** */
	
	public XgmLinearChannel() {
		
	}

	@Override
	public void reset() {
		nextWritePtr = 0;
		lastWriteValue = 0;
		lastReadPtr = 0;
	}

	@Override
	public void mix(int value, int time) {
		if (value == lastWriteValue) {
			return;
		}
		if (time < pos[lastReadPtr]) {
			return;
		}
		
		writeNext(time, (short) value);
		checkArraySize();
	}
	
	/* **********
	 * XGM混音器 *
	 ********** */

	@Override
	protected void beforeSubmit() {
		writeNext(Integer.MAX_VALUE, lastWriteValue);
	}

	@Override
	protected void checkCapacity(int size, int frame) {
		this.capacity = size;
		
		int len = size / 32 + 8;
		if (this.pos == null) {
			this.pos = new int[len];
			this.values = new short[len];
		} else {
			int delta = this.pos.length - len;
			if (delta > 8 || delta <= -8) {
				this.pos = new int[len];
				this.values = new short[len];
			}
		}
		
		nextWritePtr = 0;
		lastReadPtr = 0;
		writeNext(0, lastWriteValue);
		this.param = (float) size / frame;
	}
	
	@Override
	protected float read(int index) {
		float time = index * param + param / 2;
		return readValue((int) time);
	}
	
	int readValue(final int time) {
		int beginTime = pos[lastReadPtr];
		if (time >= beginTime) {
			// 线性读
			
			while (true) {
				int endTime = pos[lastReadPtr + 1];
				if (endTime > time) {
					return values[lastReadPtr];
				}
				lastReadPtr ++;
			}
		}
		
		lastReadPtr = 0;
		return readValue(time);
	}
	
	private void writeNext(int p, short v) {
		pos[nextWritePtr] = p;
		values[nextWritePtr] = v;
		nextWritePtr++;
		lastWriteValue = v;
	}
	
	private void checkArraySize() {
		if (nextWritePtr == pos.length) {
			int nlen = this.pos.length * 2 + 8;
			int[] npos = new int[nlen];
			short[] nvalues = new short[nlen];
			
			System.arraycopy(pos, 0, npos, 0, nextWritePtr);
			System.arraycopy(values, 0, nvalues, 0, nextWritePtr);
			pos = npos;
			values = nvalues;
		}
	}

}
