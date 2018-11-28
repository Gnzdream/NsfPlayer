package zdream.nsfplayer.ftm.executor.effect;

import zdream.nsfplayer.ftm.executor.AbstractFtmChannel;
import zdream.nsfplayer.ftm.executor.FamiTrackerRuntime;

/**
 * 修改音调、音阶的效果
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class NoteEffect implements IFtmEffect {
	
	/**
	 * 音阶 * 12 + 音符
	 */
	public final int note;

	private NoteEffect(int note) {
		this.note = note;
	}

	@Override
	public FtmEffectType type() {
		return FtmEffectType.NOTE;
	}
	
	/**
	 * 形成一个修改音符的效果
	 * @param octave
	 *   音阶值. 必须在 [0, 7] 范围内
	 * @param note
	 *   不含音阶的音符值. 必须在 [1, 12] 范围内. 0 是非法值
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>octave</code> 或 <code>note</code> 不在指定范围内时
	 */
	public static NoteEffect of(int octave, int note) throws IllegalArgumentException {
		if (octave > 7 || octave < 0) {
			throw new IllegalArgumentException("音阶必须是 0 - 7 之间的整数数值");
		}
		if (note > 12 || note < 1) {
			throw new IllegalArgumentException("音符必须是 1 - 12 之间的整数数值");
		}
		return new NoteEffect(octave * 12 + note);
	}
	
	/**
	 * 形成一个修改音符的效果
	 * @param note
	 *   含音阶的音符值. 必须在 [1, 96] 范围内. 0 是非法值
	 * @return
	 *   效果实例
	 * @throws IllegalArgumentException
	 *   当 <code>octave</code> 或 <code>note</code> 不在指定范围内时
	 */
	public static NoteEffect of(int note) throws IllegalArgumentException {
		if (note > 96 || note < 1) {
			throw new IllegalArgumentException("音符必须是 1 - 96 之间的整数数值");
		}
		return new NoteEffect(note);
	}
	
	@Override
	public void execute(byte channelCode, FamiTrackerRuntime runtime) {
		AbstractFtmChannel ch = runtime.channels.get(channelCode);
		
		ch.setMasterNote(note);
		ch.turnOn();
	}
	
	@Override
	public String toString() {
		int n = note % 12;
		int octave = note / 12;
		
		StringBuilder b = new StringBuilder();
		b.append("Note:");
				
		switch (n) {
		case 1: b.append("C-"); break;
		case 2: b.append("C#"); break;
		case 3: b.append("D-"); break;
		case 4: b.append("D#"); break;
		case 5: b.append("E-"); break;
		case 6: b.append("F-"); break;
		case 7: b.append("F#"); break;
		case 8: b.append("G-"); break;
		case 9: b.append("G#"); break;
		case 10: b.append("A-"); break;
		case 11: b.append("A#"); break;
		case 0: b.append("B-"); octave--; break;
		}
		
		b.append(octave);
		return b.toString();
	}

}
