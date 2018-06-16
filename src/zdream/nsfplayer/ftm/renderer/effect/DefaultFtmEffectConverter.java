package zdream.nsfplayer.ftm.renderer.effect;

import java.util.Map;

import static zdream.nsfplayer.ftm.format.FtmNote.*;
import static zdream.nsfplayer.ftm.FamiTrackerSetting.MAX_INSTRUMENTS;

import zdream.nsfplayer.ftm.document.IFtmChannelCode;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * 默认的 Ftm 效果转换器接口.
 * <p>在默认的播放环境中, 一个 {@link FamiTrackerRenderer} 只有一个该转换器.
 * </p>
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class DefaultFtmEffectConverter implements IFtmEffectConverter, IFtmChannelCode {
	
	FamiTrackerRuntime runtime;

	public DefaultFtmEffectConverter(FamiTrackerRuntime runtime) {
		this.runtime = runtime;
		runtime.converter = this;
	}

	@Override
	public FamiTrackerRuntime getRuntime() {
		return runtime;
	}
	
	/* **********
	 *   转换   *
	 ********** */
	
	@Override
	public void clear() {
		for (Map<FtmEffectType, IFtmEffect> effect : runtime.effects.values()) {
			effect.clear();
		}
	}
	
	@Override
	public void convert(byte channelCode, FtmNote note) {
		if (note == null) {
			return;
		}
		
		Map<FtmEffectType, IFtmEffect> effects = runtime.effects.get(channelCode);
		
		if (note.note != NOTE_NONE) {
			handleNote(channelCode, note, effects);
		}
		
		if (note.vol != MAX_VOLUME) {
			putEffect(channelCode, effects, VolumnEffect.of(note.vol));
		}
		
		if (note.instrument != MAX_INSTRUMENTS) {
			handleInst(channelCode, note, effects);
		}
		
		// TODO 其它效果
		
	}
	
	/**
	 * 放入 effect
	 * @param channelCode
	 * @param effects
	 * @param effect
	 */
	private void putEffect(byte channelCode, Map<FtmEffectType, IFtmEffect> effects, IFtmEffect effect) {
		effects.put(effect.type(), effect);
	}
	
	/**
	 * 处理音符部分
	 * @param channelCode
	 * @param note
	 * @param effects
	 */
	private void handleNote(byte channelCode, FtmNote note, Map<FtmEffectType, IFtmEffect> effects) {
		if (note.note == NOTE_RELEASE) {
			putEffect(channelCode, effects, NoteReleaseEffect.of());
		} else if (note.note == NOTE_HALT) {
			putEffect(channelCode, effects, NoteHaltEffect.of());
		} else {
			putEffect(channelCode, effects, NoteEffect.of(note.octave, note.note));
		}
	}
	
	/**
	 * 处理乐器部分
	 * @param channelCode
	 * @param note
	 * @param effects
	 */
	private void handleInst(byte channelCode, FtmNote note, Map<FtmEffectType, IFtmEffect> effects) {
		// TODO 检查该轨道所属芯片能否应用该乐器
		
		putEffect(channelCode, effects, InstrumentEffect.of(note.instrument));
	}

}
