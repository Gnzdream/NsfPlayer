package zdream.nsfplayer.ftm.renderer.sequence;

import static zdream.nsfplayer.ftm.format.FtmSequence.SEQUENCE_COUNT;

import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;
import zdream.nsfplayer.sound.IResetable;

/**
 * <p>默认的序列处理器
 * <p>适用于 2A03 的 Pulse, Triangle, Noise 轨道
 * </p>
 * @author Zdream
 * @since 0.2.1
 */
public class DefaultSequenceHandler implements IResetable {
	
	/**
	 * <li>STATE_DISABLED: 未播放
	 * <li>STATE_RUNNING: 正在播放
	 * <li>STATE_END: 播放至序列的末尾,指向序列外
	 */
	public static final byte
			STATE_DISABLED = 0,
			STATE_RUNNING = 1,
			STATE_END = 2;
	
	private FtmSequence[] sequence = new FtmSequence[SEQUENCE_COUNT];
	
	/**
	 * 记录当前序列的播放状态
	 */
	private byte[] seqState = new byte[SEQUENCE_COUNT];
	
	/**
	 * 记录当前序列跑到的位置. 大于零
	 */
	private int[] seqPtr = new int[SEQUENCE_COUNT];
	
	/**
	 * 序列指示的音量大小. 有效值 [0, 15]
	 */
	public int volume = 15;
	/**
	 * 序列指示的波长增量. 允许正数、负数和 0.
	 * 值越高, 让最后获得的音高越低, 波长越长.
	 */
	public int period;
	/**
	 * 序列指示的音键的增量. 允许正数、负数和 0
	 */
	public int deltaNote;
	/**
	 * 音色. 2A03 有效值 [0, 3]. 无效值 / 默认值 -1
	 */
	public int duty = -1;
	/**
	 * 序列 {@link FtmSequenceType#ARPEGGIO} 的配置.
	 * @see FtmSequence#ARP_SETTING_ABSOLUTE
	 * @see FtmSequence#ARP_SETTING_FIXED
	 * @see FtmSequence#ARP_SETTING_RELATIVE
	 */
	public byte arpSetting;
	/**
	 * 序列 {@link FtmSequenceType#ARPEGGIO} 的值
	 */
	public int arp;
	/**
	 * 是否该序列在释放状态
	 */
	private boolean release;

	public DefaultSequenceHandler() {}
	
	/**
	 * 安装序列, 并且立即设置该序列为运行状态
	 * @param seq
	 */
	public void setupSequence(FtmSequence seq) {
		int index = seq.type.ordinal();
		
		seqState[index] = STATE_RUNNING;
		seqPtr[index] = 0;
		sequence[index] = seq;

		resetValue();
	}
	
	/**
	 * 清空序列
	 * @param type
	 */
	public void clearSequence(FtmSequenceType type) {
		int index = type.ordinal();
		
		seqState[index] = STATE_DISABLED;
		seqPtr[index] = 0;
		sequence[index] = null;
		
		resetValue();
	}
	
	/**
	 * 清空全部序列, 并重置成原始状态
	 */
	@Override
	public void reset() {
		for (int i = 0; i < sequence.length; i++) {
			seqState[i] = STATE_DISABLED;
			seqPtr[i] = 0;
			sequence[i] = null;
		}
		volume = 15;
		period = deltaNote = arp = 0;
		arpSetting = 0;
		duty = -1;
	}
	
	/**
	 * 仅重置数值, 不重置序列
	 */
	public void resetValue() {
		volume = 15;
		period = deltaNote = arp = 0;
		arpSetting = 0;
		duty = -1;
	}
	
	/**
	 * 获得指定序列的状态. 默认为 {@link #STATE_DISABLED}
	 * @param type
	 * @return
	 */
	public byte getSequenceState(FtmSequenceType type) {
		int index = type.ordinal();
		return this.seqState[index];
	}
	
	/**
	 * 获取序列
	 * @param type
	 * @return
	 */
	public FtmSequence getSequence(FtmSequenceType type) {
		int index = type.ordinal();
		return this.sequence[index];
	}
	
	/**
	 * 获取指定序列当前播放的位置
	 * @param type
	 * @return
	 */
	public int getSequencePointer(FtmSequenceType type) {
		int index = type.ordinal();
		return this.seqPtr[index];
	}
	
	/**
	 * @param release
	 *   {@link #release}
	 */
	public void setRelease(boolean release) {
		this.release = release;
	}
	
	/**
	 * @return
	 *   {@link #release}
	 */
	public boolean isReleasing() {
		return release;
	}
	
	/* **********
	 *   运行   *
	 ********** */
	
	/**
	 * <p>运行序列
	 * <p>每帧运行该方法一次, 它将重置序列影响的数据, 包括 {@link #volume} 和 {@link #deltaNote},
	 * 但是不包含 {@link #period}, 该值能够在每个循环中累加.
	 * 它会重写这些数据, 然后让序列的指针向前移动一格
	 * </p>
	 */
	public void update() {
		volume = 15;
		/*period = */deltaNote = arp = 0;
		arpSetting = 0;
		duty = -1;
		
		for (int i = 0; i < sequence.length; i++) {
			update(i);
		}
	}
	
	/**
	 * @param index
	 *   第几个序列
	 */
	private void update(int index) {
		FtmSequence seq = this.sequence[index];
		
		if (seq == null || seq.length() == 0) {
			return;
		}
		
		switch (seqState[index]) {
		case STATE_RUNNING:
			updateRunning(index, seq);
			break;
		case STATE_END:
			updateEnd(index, seq);
			break;
		default :
			// Do nothing
			break;
		}
	}

	private void updateRunning(int index, FtmSequence seq) {
		int ptr = this.seqPtr[index]++;
		final int value = seq.data[ptr];
		
		updateValue(seq, value, false);
		
		int release = seq.releasePoint;
		int loop = seq.loopPoint;
		int length = seq.length(); // length 保证大于 0
		ptr ++;

		if (ptr == release || ptr + 1 >= length) {
			// End point reached
			if (loop != -1 && !(isReleasing() && release != -1)) {
				// 循环中, 没释放
				seqPtr[index] = loop;
			} else {
				if (ptr + 1 >= length) {
					// 到了序列的末尾
					seqState[index] = STATE_END;
				} else if (!isReleasing()) {
					// 等待释放
					--seqPtr[index];
				}
			}
		}
		
	}

	private void updateEnd(int index, FtmSequence seq) {
		final int length = seq.data.length;
		final int value = seq.data[length - 1];
		
		updateValue(seq, value, true);
	}
	
	private void updateValue(FtmSequence seq, int value, boolean isEnd) {
		switch (seq.type) {
		case VOLUME:
			this.volume = value;
			break;
		case ARPEGGIO:
			if (!isEnd || seq.settings != FtmSequence.ARP_SETTING_FIXED) {
				this.arpSetting = seq.settings;
				this.arp = value;
			}
			break;
		case PITCH:
			this.period += value;
			break;
		case HI_PITCH:
			this.period += (value << 4);
			break;
		case DUTY:
			this.duty = value;
			break;
		}
	}

}
