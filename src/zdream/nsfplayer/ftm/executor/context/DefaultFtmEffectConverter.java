package zdream.nsfplayer.ftm.executor.context;

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
import static zdream.nsfplayer.ftm.format.FtmNote.EF_SWEEPUP;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_SWEEPDOWN;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_TREMOLO;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_VIBRATO;
import static zdream.nsfplayer.ftm.format.FtmNote.EF_VOLUME_SLIDE;
import static zdream.nsfplayer.ftm.format.FtmNote.MAX_VOLUME;
import static zdream.nsfplayer.ftm.format.FtmNote.NOTE_HALT;
import static zdream.nsfplayer.ftm.format.FtmNote.NOTE_NONE;
import static zdream.nsfplayer.ftm.format.FtmNote.NOTE_RELEASE;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_EFFECT_COLUMNS;
import static zdream.nsfplayer.ftm.format.FtmStatic.MAX_INSTRUMENTS;
import static zdream.nsfplayer.core.NsfChannelCode.typeOfChannel;
import static zdream.nsfplayer.core.NsfChannelCode.chipOfChannel;
import static zdream.nsfplayer.core.FtmChipType.*;

import java.util.Map;

import zdream.nsfplayer.core.FtmChipType;
import zdream.nsfplayer.ftm.audio.FamiTrackerQuerier;
import zdream.nsfplayer.ftm.executor.effect.ArpeggioEffect;
import zdream.nsfplayer.ftm.executor.effect.CutEffect;
import zdream.nsfplayer.ftm.executor.effect.DPCMPitchEffect;
import zdream.nsfplayer.ftm.executor.effect.DPCMRetriggerEffect;
import zdream.nsfplayer.ftm.executor.effect.DPCMSampleOffsetEffect;
import zdream.nsfplayer.ftm.executor.effect.DPCM_DACSettingEffect;
import zdream.nsfplayer.ftm.executor.effect.DelayEffect;
import zdream.nsfplayer.ftm.executor.effect.DutyEffect;
import zdream.nsfplayer.ftm.executor.effect.FDSModDepthEffect;
import zdream.nsfplayer.ftm.executor.effect.FDSModSpeedHighEffect;
import zdream.nsfplayer.ftm.executor.effect.FDSModSpeedLowEffect;
import zdream.nsfplayer.ftm.executor.effect.FtmEffectType;
import zdream.nsfplayer.ftm.executor.effect.IFtmEffect;
import zdream.nsfplayer.ftm.executor.effect.InstrumentEffect;
import zdream.nsfplayer.ftm.executor.effect.JumpEffect;
import zdream.nsfplayer.ftm.executor.effect.NoiseEffect;
import zdream.nsfplayer.ftm.executor.effect.NoteEffect;
import zdream.nsfplayer.ftm.executor.effect.NoteHaltEffect;
import zdream.nsfplayer.ftm.executor.effect.NoteReleaseEffect;
import zdream.nsfplayer.ftm.executor.effect.NoteSlideEffect;
import zdream.nsfplayer.ftm.executor.effect.PitchEffect;
import zdream.nsfplayer.ftm.executor.effect.PortamentoEffect;
import zdream.nsfplayer.ftm.executor.effect.PortamentoOnEffect;
import zdream.nsfplayer.ftm.executor.effect.PulseSweepEffect;
import zdream.nsfplayer.ftm.executor.effect.SkipEffect;
import zdream.nsfplayer.ftm.executor.effect.SpeedEffect;
import zdream.nsfplayer.ftm.executor.effect.StopEffect;
import zdream.nsfplayer.ftm.executor.effect.TempoEffect;
import zdream.nsfplayer.ftm.executor.effect.TremoloEffect;
import zdream.nsfplayer.ftm.executor.effect.VRC7VolumeSlideEffect;
import zdream.nsfplayer.ftm.executor.effect.VibratoEffect;
import zdream.nsfplayer.ftm.executor.effect.VolumeEffect;
import zdream.nsfplayer.ftm.executor.effect.VolumeSlideEffect;
import zdream.nsfplayer.ftm.format.FtmNote;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;

/**
 * 默认的 Ftm 效果转换器接口.
 * <p>在默认的播放环境中, 一个 {@link FamiTrackerRenderer} 只有一个该转换器.
 * </p>
 * 
 * @author Zdream
 * @since v0.2.1
 */
public class DefaultFtmEffectConverter implements IFtmEffectConverter {
	
	/* **********
	 *   转换   *
	 ********** */
	
	@Override
	public void convert(
			FtmNote note,
			byte channelType,
			Map<FtmEffectType, IFtmEffect> effects,
			Map<FtmEffectType, IFtmEffect> geffects,
			FamiTrackerQuerier querier) {
		
		if (note == null) {
			return;
		}
		
		if (note.note != NOTE_NONE) {
			if (channelType == CHANNEL_TYPE_NOISE) {
				handleNoise(note, effects);
			} else {
				handleNote(channelType, note, effects);
			}
		}
		
		if (note.vol != MAX_VOLUME && channelType != CHANNEL_TYPE_TRIANGLE) {
			// 三角波轨道忽略音量这一栏
			putEffect(effects, VolumeEffect.of(note.vol));
		}
		
		if (note.instrument != MAX_INSTRUMENTS) {
			handleInst(channelType, note, effects, querier);
		}
		
		// 其它效果
		handleEffect(channelType, note, effects, geffects, querier);
		
	}
	
	
	/**
	 * 放入效果
	 * @param effects
	 * @param effect
	 */
	private void putEffect(Map<FtmEffectType, IFtmEffect> effects, IFtmEffect effect) {
		if (effect == null) {
			return;
		}
		
		effects.put(effect.type(), effect);
	}
	
	/**
	 * 放入全局效果
	 * @param effect
	 */
	private void putGlobalEffect(IFtmEffect effect, Map<FtmEffectType, IFtmEffect> geffects) {
		geffects.put(effect.type(), effect);
	}
	
	/**
	 * 处理音符部分 (噪音轨道)
	 * @param note
	 * @param effects
	 */
	private void handleNoise(FtmNote note, Map<FtmEffectType, IFtmEffect> effects) {
		if (note.note == NOTE_RELEASE) {
			putEffect(effects, NoteReleaseEffect.of());
		} else if (note.note == NOTE_HALT) {
			putEffect(effects, NoteHaltEffect.of());
		} else {
			int noise = (note.octave * 12 + note.note) & 0xF;
			if (noise == 0) {
				noise = 16;
			}
			putEffect(effects, NoiseEffect.of(noise));
		}
	}
	
	/**
	 * 处理音符部分 (非噪音轨道)
	 * @param channelType
	 *   轨道类型
	 * @param note
	 * @param effects
	 */
	private void handleNote(byte channelType, FtmNote note, Map<FtmEffectType, IFtmEffect> effects) {
		if (note.note == NOTE_RELEASE) {
			putEffect(effects, NoteReleaseEffect.of());
		} else if (note.note == NOTE_HALT) {
			putEffect(effects, NoteHaltEffect.of());
		} else {
			putEffect(effects, NoteEffect.of(note.octave, note.note));
		}
	}
	
	/**
	 * 处理乐器部分
	 * @param channelType
	 *   轨道类型
	 * @param note
	 * @param effects
	 * @param querier
	 */
	private void handleInst(
			byte channelType,
			FtmNote note,
			Map<FtmEffectType, IFtmEffect> effects,
			FamiTrackerQuerier querier) {
		int inst = note.instrument;
		if (inst < 0) {
			return;
		}
		
		// 检查该轨道所属芯片能否应用该乐器
		boolean valid = false;
		FtmChipType type = querier.getInstrumentType(inst);
		switch (chipOfChannel(channelType)) {
		case CHIP_2A03: case CHIP_2A07: case CHIP_MMC5:
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
			putEffect(effects, InstrumentEffect.of(inst));
		} else {
			putEffect(effects, NoteHaltEffect.of());
		}
	}
	
	/**
	 * 处理其它效果部分
	 * @param channelType
	 *   轨道类型
	 * @param note
	 * @param effects
	 * @param geffects
	 */
	private void handleEffect(
			byte channelType,
			FtmNote note,
			Map<FtmEffectType, IFtmEffect> effects,
			Map<FtmEffectType, IFtmEffect> geffects,
			FamiTrackerQuerier querier) {
		short delay = 0;
		
		for (int i = 0; i < MAX_EFFECT_COLUMNS; i++) {
			switch (note.effNumber[i]) {
			case EF_NONE:
				continue;
				
				// 全局
				
			case EF_JUMP:
				putGlobalEffect(JumpEffect.of(note.effParam[i]), geffects);
				break;
				
			case EF_SKIP:
				putGlobalEffect(SkipEffect.of(note.effParam[i]), geffects);
				break;
				
			case EF_SPEED: {
				int speed = note.effParam[i];
				if (speed >= querier.audio.getSplit()) {
					putGlobalEffect(TempoEffect.of(speed), geffects);
				} else {
					putGlobalEffect(SpeedEffect.of(speed), geffects);
				}
			} break;
			
			case EF_HALT:
				putGlobalEffect(StopEffect.of(), geffects);
				break;
			
				// 轨道
				
			case EF_PITCH: // Pxx
				if (channelType != CHANNEL_TYPE_DPCM)
					putEffect(effects, PitchEffect.of(note.effParam[i] - 0x80));
				break;
				
			case EF_DELAY: // Gxx
				delay = note.effParam[i]; // 延迟处理
				break;
				
			case EF_DUTY_CYCLE: // Vxx
				if (channelType != CHANNEL_TYPE_DPCM)
					putEffect(effects, DutyEffect.of(note.effParam[i]));
				break;
				
			case EF_VOLUME_SLIDE: // Axx
				if (channelType == CHANNEL_TYPE_DPCM || channelType == CHANNEL_TYPE_TRIANGLE) {
					break;
				}
				
				{
					int param = note.effParam[i];
					if (typeOfChannel(channelType) == CHANNEL_TYPE_VRC7) {
						if ((param & 0xF) != 0) { // up 或 0
							putEffect(effects, VRC7VolumeSlideEffect.of((param & 0xF)));
						} else { // down
							putEffect(effects, VRC7VolumeSlideEffect.of((param >> 4) * -1));
						}
					} else {
						if ((param & 0xF) != 0) { // down 或 0
							putEffect(effects, VolumeSlideEffect.of((param & 0xF) * -2));
						} else { // up
							putEffect(effects, VolumeSlideEffect.of((param >> 4) * 2));
						}
					}
				}
				break;
				
			case EF_SLIDE_UP: case EF_SLIDE_DOWN: // Qxy Rxy
			{
				if (channelType == CHANNEL_TYPE_DPCM) {
					break;
				}
				
				short param = note.effParam[i];
				int delta = param & 0xF;
				
				param >>= 4;
				boolean up = note.effNumber[i] == EF_SLIDE_UP;
				int speed;
				if (channelType == CHANNEL_TYPE_NOISE) {
					speed = (param == 0) ? 1 : param;
				} else if (typeOfChannel(channelType) == CHANNEL_TYPE_N163) {
					speed = (param << 3) + 1;
				} else {
					speed = (param << 1) + 1;
				}
				
				if (!up) {
					delta *= -1;
				}
				
				putEffect(effects, NoteSlideEffect.of(delta, speed));
			} break;
			
			case EF_VIBRATO: // 4xy
				if (channelType != CHANNEL_TYPE_DPCM) {
					int param = note.effParam[i];
					putEffect(effects, VibratoEffect.of(param >> 4, param & 0xF));
				}
				break;
				
			case EF_TREMOLO: // 7xy
				if (channelType != CHANNEL_TYPE_DPCM && channelType != CHANNEL_TYPE_TRIANGLE) {
					int param = note.effParam[i];
					putEffect(effects, TremoloEffect.of(param >> 4, param & 0xF));
				}
				break;
				
			case EF_PORTA_UP: // 1xx
				if (channelType != CHANNEL_TYPE_DPCM) {
					int speed;
					if (typeOfChannel(channelType) == CHANNEL_TYPE_N163) {
						// N163 修改的是波长的倒数, 是增加的, 且数据的绝对值高
						speed = (note.effParam[i] << 2);
					} else if (typeOfChannel(channelType) == CHANNEL_TYPE_VRC7) {
						// VRC7 修改的是波长的倒数, 是增加的
						speed = note.effParam[i];
					} else {
						// 其它轨道修改的是波长, 波长是减少的
						speed = -note.effParam[i];
					}
					putEffect(effects, PortamentoEffect.of(speed));
				}
				break;
				
			case EF_PORTA_DOWN: // 2xx
				if (channelType != CHANNEL_TYPE_DPCM) {
					int speed;
					if (typeOfChannel(channelType) == CHANNEL_TYPE_N163) {
						// N163 修改的是波长的倒数, 是减少的, 且数据的绝对值高
						speed = -(note.effParam[i] << 2);
					} else if (typeOfChannel(channelType) == CHANNEL_TYPE_VRC7) {
						// VRC7 修改的是波长的倒数, 是增加的
						speed = -note.effParam[i];
					} else {
						// 其它轨道修改的是波长, 波长是增加的
						speed = note.effParam[i];
					}
					putEffect(effects, PortamentoEffect.of(speed));
				}
				break;
				
			case EF_NOTE_CUT: // Sxx
				putEffect(effects, CutEffect.of(note.effParam[i]));
				break;
				
			case EF_ARPEGGIO: // 0xy
				if (channelType != CHANNEL_TYPE_DPCM) {
					int param = note.effParam[i];
					putEffect(effects, ArpeggioEffect.of(param >> 4, param & 0xF));
				}
				break;
				
			case EF_PORTAMENTO: // 3xx
				if (channelType != CHANNEL_TYPE_DPCM) {
					int speed;
					if (typeOfChannel(channelType) == CHANNEL_TYPE_N163) {
						speed = (note.effParam[i] << 2);
					} else {
						speed = note.effParam[i];
					}
					putEffect(effects, PortamentoOnEffect.of(speed));
				}
				break;
				
				// 2A03 Pulse 轨道
				
			case EF_SWEEPUP:
				if (channelType == CHANNEL_TYPE_PULSE) {
					short param = note.effParam[i];
					putEffect(effects, PulseSweepEffect.of((param >> 4) & 7, param & 7, true));
				}
				break;
				
			case EF_SWEEPDOWN:
				if (channelType == CHANNEL_TYPE_PULSE) {
					short param = note.effParam[i];
					putEffect(effects, PulseSweepEffect.of((param >> 4) & 7, param & 7, false));
				}
				break;
				
				// DPCM 轨道
				
			case EF_DAC: // Zxx
				if (channelType == CHANNEL_TYPE_DPCM) {
					putEffect(effects, DPCM_DACSettingEffect.of(note.effParam[i] & 0x7F));
				}
				break;
				
			case EF_SAMPLE_OFFSET: // Yxx
				if (channelType == CHANNEL_TYPE_DPCM) {
					putEffect(effects, DPCMSampleOffsetEffect.of(note.effParam[i] * 64));
				}
				break;
				
			case EF_RETRIGGER: // Xxx
				if (channelType == CHANNEL_TYPE_DPCM) {
					putEffect(effects, DPCMRetriggerEffect.of(note.effParam[i]));
				}
				break;
				
			case EF_DPCM_PITCH:
				if (channelType == CHANNEL_TYPE_DPCM) {
					putEffect(effects, DPCMPitchEffect.of(note.effParam[i] & 0xF));
				}
				break;
				
				// FDS 轨道
				
			case EF_FDS_MOD_DEPTH: // Hxx
				if (channelType == CHANNEL_TYPE_FDS) {
					putEffect(effects, FDSModDepthEffect.of(note.effParam[i] & 0x3F));
				}
				break;
				
			case EF_FDS_MOD_SPEED_HI: // Ixx
				if (channelType == CHANNEL_TYPE_FDS) {
					putEffect(effects, FDSModSpeedHighEffect.of(note.effParam[i] & 15));
				}
				break;
				
			case EF_FDS_MOD_SPEED_LO: // Jxx
				if (channelType == CHANNEL_TYPE_FDS) {
					putEffect(effects, FDSModSpeedLowEffect.of(note.effParam[i] & 0xFF));
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
			putEffect(effects, d);
		}
	}

}
