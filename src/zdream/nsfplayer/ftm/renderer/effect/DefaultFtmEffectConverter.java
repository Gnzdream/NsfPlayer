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
		runtime.geffect.clear();
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
			putEffect(channelCode, effects, VolumeEffect.of(note.vol));
		}
		
		if (note.instrument != MAX_INSTRUMENTS) {
			handleInst(channelCode, note, effects);
		}
		
		// 其它效果
		handleEffect(channelCode, note, effects);
		
	}
	
	/**
	 * 放入效果
	 * @param channelCode
	 * @param effects
	 * @param effect
	 */
	private void putEffect(byte channelCode, Map<FtmEffectType, IFtmEffect> effects, IFtmEffect effect) {
		effects.put(effect.type(), effect);
	}
	
	/**
	 * 放入全局效果
	 * @param effect
	 */
	private void putGlobalEffect(IFtmEffect effect) {
		runtime.geffect.put(effect.type(), effect);
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
	
	/**
	 * 处理其它效果部分
	 * @param channelCode
	 * @param note
	 * @param effects
	 */
	private void handleEffect(byte channelCode, FtmNote note, Map<FtmEffectType, IFtmEffect> effects) {
		short delay = 0;
		
		for (int i = 0; i < MAX_EFFECT_COLUMNS; i++) {
			/*if (effNumber[i] != 0) {
				b.append(' ').append(EFF_CHAR[effNumber[i] - 1]).append('-');
				String paramStr = Integer.toHexString(effParam[i] & 0xFF);
				if (paramStr.length() == 1) {
					b.append('0');
				}
				b.append(paramStr);
			}*/
			
			switch (note.effNumber[i]) {
			case EF_NONE:
				continue;
				
				// 全局
				
			case EF_JUMP:
				putGlobalEffect(JumpEffect.of(note.effParam[i]));
				break;
				
			case EF_SKIP:
				putGlobalEffect(SkipEffect.of(note.effParam[i]));
				break;
				
			case EF_SPEED: {
				int speed = note.effParam[i];
				if (speed >= runtime.querier.audio.getSplit()) {
					putGlobalEffect(TempoEffect.of(speed));
				} else {
					putGlobalEffect(SpeedEffect.of(speed));
				}
			} break;
			
			case EF_HALT:
				// TODO
				break;
			
				// 轨道
				
			case EF_PITCH: // Pxx
				if (channelCode != CHANNEL_2A03_DPCM)
					putEffect(channelCode, effects, PitchEffect.of(note.effParam[i] - 0x80));
				break;
				
			case EF_DELAY: // Gxx
				delay = note.effParam[i]; // 延迟处理
				break;
				
			case EF_DUTY_CYCLE: // Vxx
				if (channelCode != CHANNEL_2A03_DPCM)
					putEffect(channelCode, effects, DutyEffect.of(note.effParam[i]));
				break;
				
			case EF_VOLUME_SLIDE: // Axx
				if (channelCode != CHANNEL_2A03_DPCM && channelCode != CHANNEL_2A03_TRIANGLE) {
					int param = note.effParam[i];
					if (param <= 15) { // up 或 0
						putEffect(channelCode, effects, VolumeSlideEffect.of(param));
					} else { // down
						putEffect(channelCode, effects, VolumeSlideEffect.of((param >> 4) * -1));
					}
				}
				break;
			
			// TODO 其它效果

			default:
				break;
			}
		}
		
		// 处理延迟部分 Gxx
		if (delay != 0) {
			DelayEffect d = DelayEffect.of(delay, effects.values());
			effects.clear();
			putEffect(channelCode, effects, d);
		}
	}

}
