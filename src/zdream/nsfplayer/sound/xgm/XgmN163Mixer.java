package zdream.nsfplayer.sound.xgm;

/**
 * N163 的 (合并) 轨道
 * 
 * @author Zdream
 * @since v0.2.6
 */
public class XgmN163Mixer extends AbstractXgmMultiMixer {
	
	private final XgmAudioChannel[] n163s = new XgmAudioChannel[8];

	@Override
	public void reset() {
		for (int i = 0; i < n163s.length; i++) {
			XgmAudioChannel ch = n163s[i];
			if (ch == null) {
				continue;
			}
			ch.reset();
		}
	}

	@Override
	public void setAudioChannel(byte channelCode, XgmAudioChannel ch) {
		switch (channelCode) {
		case CHANNEL_N163_1: this.n163s[0] = ch; break;
		case CHANNEL_N163_2: this.n163s[1] = ch; break;
		case CHANNEL_N163_3: this.n163s[2] = ch; break;
		case CHANNEL_N163_4: this.n163s[3] = ch; break;
		case CHANNEL_N163_5: this.n163s[4] = ch; break;
		case CHANNEL_N163_6: this.n163s[5] = ch; break;
		case CHANNEL_N163_7: this.n163s[6] = ch; break;
		case CHANNEL_N163_8: this.n163s[7] = ch; break;
		default: return;
		}
	}

	@Override
	public XgmAudioChannel getAudioChannel(byte channelCode) {
		switch (channelCode) {
		case CHANNEL_N163_1:
		case CHANNEL_N163_2:
		case CHANNEL_N163_3:
		case CHANNEL_N163_4:
		case CHANNEL_N163_5:
		case CHANNEL_N163_6:
		case CHANNEL_N163_7:
		case CHANNEL_N163_8:
			return n163s[channelCode - CHANNEL_N163_1];
		}
		return null;
	}

	@Override
	public void beforeRender() {
		for (int i = 0; i < n163s.length; i++) {
			XgmAudioChannel ch = n163s[i];
			if (ch == null) {
				continue;
			}
			ch.beforeSubmit();
		}
	}

	@Override
	public int render(int index, int fromIdx, int toIdx) {
		int time = toIdx - fromIdx;
		int idx = (fromIdx + toIdx) / 2;
		
		int sum = 0, count = 0;
		for (int i = 0; i < n163s.length; i++) {
			XgmAudioChannel ch = n163s[i];
			if (ch == null) {
				continue;
			}
			sum += (int) (ch.buffer[idx] * ch.getLevel());
			count ++;
		}
		
		if (count == 0) {
			return 0;
		}
		
		switch (count) {
		case 1:
			sum *= 256;
			break;
		case 2:
			sum *= 128;
			break;
		case 3:
			sum = sum * 256 / 3;
			break;
		case 4:
			sum *= 64;
			break;
		case 5:
			sum = sum * 256 / 5;
			break;
		default:
			sum = sum * 256 / 6;
			break;
		}
		
		// 以下注释来自源程序 NsfPlayer:
		// when approximating the serial multiplex as a straight mix, once the
		// multiplex frequency gets below the nyquist frequency an average mix
		// begins to sound too quiet. To approximate this effect, I don't attenuate
		// any further after 6 channels are active.
		// 如果轨道数量大于 6, 如果平分音量会导致各个轨道轻得离谱, 所以除以 6 以后不再更轻了
		
		// 8 bit approximation of master volume
		// max N163 vol vs max APU square
		// unfortunately, games have been measured as low as 3.4x and as high as 8.5x
		// with higher volumes on Erika, King of Kings, and Rolling Thunder
		// and lower volumes on others. Using 6.0x as a rough "one size fits all".
		// 根据测量结果, N163 音量在不同的游戏中表现得很不稳定,
		// 范围在 APU 矩形脉冲波声音的 [3.4, 8.5] 倍. 这里决定采用 6 倍.
		
		final double MASTER_VOL = 7338.0; // 6.0 * 1223.0
		final double MAX_OUT = 57600; // max digital value: = 15.0 * 15.0 * 256.0
		
		sum = (int) ((MASTER_VOL / MAX_OUT) * sum);
		sum = intercept(sum, time);
		return sum;
	}

}