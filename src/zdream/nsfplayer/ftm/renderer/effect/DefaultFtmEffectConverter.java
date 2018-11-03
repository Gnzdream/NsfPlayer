package zdream.nsfplayer.ftm.renderer.effect;

import static zdream.nsfplayer.ftm.format.FtmNote.EF_ARPEGGIO;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_DAC;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_DELAY;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_DPCM_PITCH;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_DUTY_CYCLE;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_FDS_MOD_DEPTH;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_FDS_MOD_SPEED_HI;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_FDS_MOD_SPEED_LO;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_HALT;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_JUMP;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_NONE;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_NOTE_CUT;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_PITCH;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_PORTAMENTO;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_PORTA_DOWN;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_PORTA_UP;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_RETRIGGER;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_SAMPLE_OFFSET;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_SKIP;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_SLIDE_DOWN;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_SLIDE_UP;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_SPEED;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_TREMOLO;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_VIBRATO;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_VOLUME_SLIDE;
import static zdream.nsfplayer.ftm.format.FtmNote.MAX_EFFECT_COLUMNS;
import static zdream.nsfplayer.ftm.format.FtmNote.MAX_VOLUME;
import static zdream.nsfplayer.ftm.format.FtmNote.NOTE_HALT;
import static zdream.nsfplayer.ftm.format.FtmNote.NOTE_NONE;
import static zdream.nsfplayer.ftm.format.FtmNote.NOTE_RELEASE;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_INSTRUMENTS;
import static zdream.nsfplayer.core.NsfChannelCode.chipOfChannel;
import static zdream.nsfplayer.core.FtmChipType.*;

import java.util.Map;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRuntime;

/**
 * 默认的 Ftm 效果转换器接口.
 * <p>在默认的播放环境中, 一个 {@link FamiTrackerRenderer} 只有一个该转换器.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class DefaultFtmEffectConverter implements IFtmEffectConverter, INsfChannelCode {
	
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
	public void convert(final byte channelCode, FtmNote note) {
		if (note == null) {
			return;
		}
		
		Map<FtmEffectType, IFtmEffect> effects = runtime.effects.get(channelCode);
		
		if (note.note != NOTE_NONE) {
			if (channelCode == CHANNEL_2A03_NOISE) {
				handleNoise(note, effects);
			} else {
				handleNote(channelCode, note, effects);
			}
		}
		
		if (note.vol != MAX_VOLUME && channelCode != CHANNEL_2A03_TRIANGLE) {
			// 三角波轨道忽略音量这一栏
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
		if (effect == null) {
			return;
		}
		
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
	 * 处理音符部分 (噪音轨道)
	 * @param note
	 * @param effects
	 */
	private void handleNoise(FtmNote note, Map<FtmEffectType, IFtmEffect> effects) {
		if (note.note == NOTE_RELEASE) {
			putEffect(CHANNEL_2A03_NOISE, effects, NoteReleaseEffect.of());
		} else if (note.note == NOTE_HALT) {
			putEffect(CHANNEL_2A03_NOISE, effects, NoteHaltEffect.of());
		} else {
			int noise = (note.octave * 12 + note.note) & 0xF;
			if (noise == 0) {
				noise = 16;
			}
			putEffect(CHANNEL_2A03_NOISE, effects, NoiseEffect.of(noise));
		}
	}
	
	/**
	 * 处理音符部分 (非噪音轨道)
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
		int inst = note.instrument;
		if (inst < 0) {
			return;
		}
		
		// 检查该轨道所属芯片能否应用该乐器
		boolean valid = false;
		FtmChipType type = runtime.querier.getInstrumentType(inst);
		switch (chipOfChannel(channelCode)) {
		case CHIP_2A03: case CHIP_MMC5:
			valid = type == _2A03; break;
		case CHIP_VRC6:
			valid = type == VRC6; break;
		case CHIP_FDS:
			valid = type == FDS; break;
		case CHIP_N163:
			valid = type == N163; break;
		case CHIP_VRC7:
			valid = type == VRC7; break;
		case CHIP_S5B:
			valid = type == S5B; break;
		}
		
		if (valid) {
			putEffect(channelCode, effects, InstrumentEffect.of(inst));
		} else {
			putEffect(channelCode, effects, StopEffect.of());
		}
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
				putGlobalEffect(StopEffect.of());
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
					if ((param & 0xF) != 0) { // down 或 0
						putEffect(channelCode, effects, VolumeSlideEffect.of((param & 0xF) * -2));
					} else { // up
						putEffect(channelCode, effects, VolumeSlideEffect.of((param >> 4) * 2));
					}
				}
				break;
				
			case EF_SLIDE_UP: case EF_SLIDE_DOWN: // Qxy Rxy
			{
				if (channelCode == CHANNEL_2A03_DPCM) {
					break;
				}
				
				short param = note.effParam[i];
				int delta = param & 0xF;
				
				param >>= 4;
				boolean up = note.effNumber[i] == EF_SLIDE_UP;
				int speed;
				if (channelCode == CHANNEL_2A03_NOISE) {
					speed = (param == 0) ? 1 : param;
				} else {
					speed = (param << 1) + 1;
				}
				
				if (!up) {
					delta *= -1;
				}
				
				putEffect(channelCode, effects, NoteSlideEffect.of(delta, speed));
			} break;
			
			case EF_VIBRATO: // 4xy
				if (channelCode != CHANNEL_2A03_DPCM) {
					int param = note.effParam[i];
					putEffect(channelCode, effects, VibratoEffect.of(param >> 4, param & 0xF));
				}
				break;
				
			case EF_TREMOLO: // 7xy
				if (channelCode != CHANNEL_2A03_DPCM && channelCode != CHANNEL_2A03_TRIANGLE) {
					int param = note.effParam[i];
					putEffect(channelCode, effects, TremoloEffect.of(param >> 4, param & 0xF));
				}
				break;
				
			case EF_PORTA_UP: // 1xx
				if (channelCode != CHANNEL_2A03_DPCM) {
					putEffect(channelCode, effects, PortamentoEffect.of(-note.effParam[i]));
				}
				break;
				
			case EF_PORTA_DOWN: // 2xx
				if (channelCode != CHANNEL_2A03_DPCM) {
					putEffect(channelCode, effects, PortamentoEffect.of(note.effParam[i]));
				}
				break;
				
			case EF_NOTE_CUT: // Sxx
				putEffect(channelCode, effects, CutEffect.of(note.effParam[i]));
				break;
				
			case EF_ARPEGGIO: // 0xy
				if (channelCode != CHANNEL_2A03_DPCM) {
					int param = note.effParam[i];
					putEffect(channelCode, effects, ArpeggioEffect.of(param >> 4, param & 0xF));
				}
				break;
				
			case EF_PORTAMENTO: // 3xx
				if (channelCode != CHANNEL_2A03_DPCM) {
					putEffect(channelCode, effects, PortamentoOnEffect.of(note.effParam[i]));
				}
				break;
				
				// DPCM 轨道
				
			case EF_DAC: // Zxx
				if (channelCode == CHANNEL_2A03_DPCM) {
					putEffect(channelCode, effects, DPCM_DACSettingEffect.of(note.effParam[i]));
				}
				break;
				
			case EF_SAMPLE_OFFSET: // Yxx
				if (channelCode == CHANNEL_2A03_DPCM) {
					putEffect(channelCode, effects, DPCMSampleOffsetEffect.of(note.effParam[i] * 64));
				}
				break;
				
			case EF_RETRIGGER: // Xxx
				if (channelCode == CHANNEL_2A03_DPCM) {
					putEffect(channelCode, effects, DPCMRetriggerEffect.of(note.effParam[i]));
				}
				break;
				
			case EF_DPCM_PITCH:
				if (channelCode == CHANNEL_2A03_DPCM) {
					putEffect(channelCode, effects, DPCMPitchEffect.of(note.effParam[i] & 0xF));
				}
				break;
				
				// FDS 轨道
				
			case EF_FDS_MOD_DEPTH: // Hxx
				if (channelCode == CHANNEL_FDS) {
					putEffect(channelCode, effects, FDSModDepthEffect.of(note.effParam[i] & 0x3F));
				}
				break;
				
			case EF_FDS_MOD_SPEED_HI: // Ixx
				if (channelCode == CHANNEL_FDS) {
					putEffect(channelCode, effects, FDSModSpeedHighEffect.of(note.effParam[i] & 15));
				}
				break;
				
			case EF_FDS_MOD_SPEED_LO: // Jxx
				if (channelCode == CHANNEL_FDS) {
					putEffect(channelCode, effects, FDSModSpeedLowEffect.of(note.effParam[i] & 0xFF));
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
