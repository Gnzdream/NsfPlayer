package zdream.nsfplayer.ftm.renderer.channel;

import zdream.nsfplayer.ftm.format.FtmInstrumentVRC7;
import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.sound.vrc7.OPLL;
import zdream.nsfplayer.sound.vrc7.OPLLPatch;
import zdream.nsfplayer.sound.vrc7.SoundVRC7;

/**
 * VRC7 轨道
 * 
 * @author Zdream
 * @since v0.2.7
 */
public class ChannelVRC7 extends AbstractFtmChannel {
	
	public final int index;

	/**
	 * @param index
	 *   第几号 VRC7 轨道. 范围 [0, 5]
	 * @param opll
	 *   OPLL 环境
	 */
	public ChannelVRC7(int index, OPLL opll) {
		super((byte) (CHANNEL_VRC7_FM1 + index));
		this.index = index;
		this.opll = opll;
		this.sound = opll.getSound(index);
	}

	@Override
	public void playNote() {
		super.playNote();
		
		if (instrumentUpdated) {
			handleInstrument();
			state = STATE_NOTE_TRIGGER;
		}
		calculateVolume();
		calculatePeriod();
		
		// 发声器
		writeToSound();
		processSound();
	}
	
	@Override
	public void reset() {
		hold = false;
		super.reset();
	}
	
	/* **********
	 * 乐器序列 *
	 ********** */
	
	/**
	 * 计算音量, 将序列所得出的音量合并计算, 最后将音量限定在 [0, 120] 范围内
	 */
	protected void calculateVolume() {
		int volume = masterVolume * 8 + curVolume; // 精度 120
 		
 		if (volume > 120) {
			curVolume = 120;
 		} else if (volume < 1) {
			curVolume = 0;
 		} else {
			curVolume = volume;
		}
	}
	
	/**
	 * <p>计算波长, 将其它效果得出的音高、音键值
	 * 最后综合出 VRC7 最终采用的音高、音键值
	 */
	protected void calculatePeriod() {
		if (masterNote == 0) {
			// 不播放
			curNote = 0;
			curPeriod = 0;
			return;
		}
		
		hold = true;
		int note = masterNote + curNote;
		lastOctave = (note - 1) / 12 - 2;
		int fnum = calcFnum(note);
		int period = fnum - masterPitch/* + curPeriod*/;
		
		// 下面的循环就相当于数学里的向下借位
		if (period < 0) {
			while (period < 0) {
				lastOctave --;
				fnum *= 2;
				period = fnum - masterPitch/* + curPeriod*/;
			}
		} else if (period >= 512) {
			while (period >= 512) {
				lastOctave ++;
				fnum /= 2;
				period = fnum - masterPitch/* + curPeriod*/;
			}
		}
		
		if (note <= 1) {
			note = 1;
		} else if (note > 96) {
			note = 96;
		}
		
		curNote = note;
		curPeriod = period;
	}
	
	public void doHalt() {
		super.doHalt();
		state = STATE_NOTE_HALT;
	}
	
	@Override
	public void doRelease() {
		state = STATE_NOTE_RELEASE;
	}
	
	/* **********
	 *   OPLL   *
	 ********** */
	
	/**
	 * 状态
	 */
	static final byte
			STATE_NONE = 0,
			STATE_NOTE_ON = 1,
			STATE_NOTE_TRIGGER = 2,
			STATE_NOTE_OFF = 3,
			STATE_NOTE_HALT = 4,
			STATE_NOTE_RELEASE = 5;
	
	final OPLL opll;
	
	int patchNum;
	final short[] regs = new short[8];
	boolean hold;
	byte state;
	int lastOctave;
	
	private void handleInstrument() {
		FtmInstrumentVRC7 inst = getRuntime().querier.getVRC7Instrument(instrument);
		if (inst == null) {
			return;
		}
		
		this.patchNum = inst.patchNum;
		if (patchNum == 0) {
			System.arraycopy(inst.regs, 0, regs, 0, 8);
		}
	}
	
	private int calcFnum(int note) {
		final int FREQ_TABLE[] = {172, 183, 194, 205, 217, 230, 244, 258, 274, 290, 307, 326};
		return FREQ_TABLE[(note - 1) % 12] << 2;
	}
	
	@Override
	public int periodTable(int note) {
		// TODO
		return 0;
	}
	
	/* **********
	 *  发声器  *
	 ********** */
	
	private final SoundVRC7 sound;

	@Override
	public SoundVRC7 getSound() {
		return sound;
	}
	
	/**
	 * <p>将轨道中的数据写到发声器中.
	 * </p>
	 */
	public void writeToSound() {
		int block = lastOctave; // bnum
		int fnum = curPeriod;

		if (!playing) {
			state = STATE_NOTE_HALT;
		}

		boolean noteOn = false;
		boolean sustainOn = false;

		switch (state) {
			case STATE_NOTE_TRIGGER:
				sound.keyOff();
				noteOn = true;
				sustainOn = true;
				break;
			case STATE_NOTE_ON:
				if (hold) {
					noteOn = true;
				} else {
					sustainOn = true;
				}
				break;
			case STATE_NOTE_HALT:
				break;
			case STATE_NOTE_RELEASE:
				sustainOn = true;
				break;
		}
		
		// Write frequency
		sound.modulatorSlot.fnum = fnum;
		sound.carriorSlot.fnum = fnum;

		// 自定义 pitch
		if (patchNum == 0 && (state == STATE_NOTE_TRIGGER)) {
			writeCustomPitch();
		}
		
		sound.modulatorSlot.block = block;
		sound.carriorSlot.block = block;
		sound.carriorSlot.sustine = sustainOn;
		
		if (noteOn) {
			sound.keyOn();
		} else {
			sound.keyOff();
		}
		
		if (state != STATE_NOTE_HALT) {
			// Select volume & patch
			sound.carriorSlot.volume = 63 - curVolume / 2;
			sound.setPatch(patchNum);
		}
		
		sound.rebuildAll();
		if (state == STATE_NOTE_TRIGGER) {
			state = STATE_NOTE_ON;
		}
	}
	
	private void writeCustomPitch() {
		OPLLPatch p = opll.getCustomModPatch();
		
		p.AM = (regs[0] & 0x80) != 0;
		p.PM = (regs[0] & 0x40) != 0;
		p.EG = (regs[0] & 0x20) != 0;
		p.KR = (regs[0] & 0x10) != 0;
		p.ML = (regs[0]) & 15;
		p.KL = (regs[2] >> 6) & 3;
		p.TL = (regs[2]) & 63;
		p.WF = (regs[3] >> 3) & 1;
		p.FB = (regs[3]) & 7;
		p.AR = (regs[4] >> 4) & 15;
		p.DR = (regs[4]) & 15;
		p.SL = (regs[6] >> 4) & 15;
		p.RR = (regs[6]) & 15;
		
		p = opll.getCustomCarPatch();

		p.AM = (regs[1] & 0x80) != 0;
		p.PM = (regs[1] & 0x40) != 0;
		p.EG = (regs[1] & 0x20) != 0;
		p.KR = (regs[1] & 0x10) != 0;
		p.ML = (regs[1]) & 15;
		p.KL = (regs[3] >> 6) & 3;
		p.WF = (regs[3] >> 4) & 1;
		p.AR = (regs[5] >> 4) & 15;
		p.DR = (regs[5]) & 15;
		p.SL = (regs[7] >> 4) & 15;
		p.RR = (regs[7]) & 15;
	}
	
	/**
	 * 指导发声器工作一帧
	 */
	public void processSound() {
		// 拿到一帧对应的时钟周期数
		int freq = getRuntime().param.freqPerFrame;
		
		sound.process(freq);
		
		// 结束
		sound.endFrame();
	}

}
