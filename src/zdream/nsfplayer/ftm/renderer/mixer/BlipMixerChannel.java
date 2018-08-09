package zdream.nsfplayer.ftm.renderer.mixer;

import zdream.nsfplayer.sound.buffer.BlipSynth;
import zdream.nsfplayer.sound.mixer.IMixerChannel;

/**
 * FTM 默认的音频管道
 * @author Zdream
 * @since 0.2.1
 */
public class BlipMixerChannel implements IMixerChannel {
	
	BlipSynth synth;
	FtmSoundMixer mixer;
	/**
	 * 音量大小的修正. 默认 1.0
	 */
	float level = 1;
	
	/**
	 * 设置的表达式
	 */
	IExpression expression;
	
	/**
	 * 上一个值
	 * <li>lastInValue: 从 sound 那里传入的
	 * <li>lastMixValue: 在表达式里面计算的
	 * </li>
	 */
	int lastInValue, lastMixValue;

	public BlipMixerChannel(FtmSoundMixer mixer) {
		this.mixer = mixer;
	}
	
	public void updateSetting(int quality, int range) {
		synth = new BlipSynth(quality, range);
	}
	
	/**
	 * 修正总体音量值
	 */
	public void setLevel(float level) {
		this.level = level;
	}
	
	/**
	 * 获得总体音量值
	 */
	public float getLevel() {
		return level;
	}
	
	/**
	 * 设置表达式
	 * @param expression
	 */
	public void setExpression(IExpression expression) {
		this.expression = expression;
	}

	@Override
	public void mix(int value, int time) {
		if (value == lastInValue) {
			return;
		}
		
		final int mv = expression.f(value);
		
		synth.offset(time, (int) ((mv - lastMixValue) * level), mixer.buffer);
		lastInValue = value;
		lastMixValue = mv;
	}
	
	@Override
	public void reset() {
		lastInValue = lastMixValue = 0;
	}

}
