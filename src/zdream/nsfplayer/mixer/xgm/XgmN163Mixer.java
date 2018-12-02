package zdream.nsfplayer.mixer.xgm;

/**
 * N163 的 (合并) 轨道
 * 
 * @author Zdream
 * @since v0.2.6
 */
public class XgmN163Mixer extends AbstractXgmMultiMixer {
	
	private final XgmAudioChannel[] n163s = new XgmAudioChannel[8];
	private final boolean[] enables = new boolean[8];
	
	public XgmN163Mixer() {
		for (int i = 0; i < n163s.length; i++) {
			n163s[i] = new XgmAudioChannel();
		}
	}

	@Override
	public void reset() {
		super.reset();
		for (int i = 0; i < n163s.length; i++) {
			AbstractXgmAudioChannel ch = n163s[i];
			ch.reset();
		}
	}

	@Override
	public AbstractXgmAudioChannel getRemainAudioChannel(byte type) {
		if (type != CHANNEL_TYPE_N163) {
			return null;
		}
		
		for (int i = 0; i < enables.length; i++) {
			if (!enables[i]) {
				return n163s[i];
			}
		}
		
		return null;
	}
	
	@Override
	public void setEnable(AbstractXgmAudioChannel channel, boolean enable) {
		for (int i = 0; i < n163s.length; i++) {
			if (channel == n163s[i]) {
				enables[i] = enable;
				break;
			}
		}
	}
	
	@Override
	public boolean isEnable(AbstractXgmAudioChannel channel) {
		for (int i = 0; i < n163s.length; i++) {
			if (channel == n163s[i]) {
				return enables[i];
			}
		}
		return false;
	}
	
	@Override
	public void beforeRender() {
		super.beforeRender();
		for (int i = 0; i < enables.length; i++) {
			if (!enables[i]) {
				continue;
			}
			AbstractXgmAudioChannel ch = n163s[i];
			ch.beforeSubmit();
		}
	}

	@Override
	public int render(int index) {
		float sum = 0;
		int count = 0;
		for (int i = 0; i < n163s.length; i++) {
			XgmAudioChannel ch = n163s[i];
			if (ch == null || !enables[i]) {
				continue;
			}
			sum += (ch.read(index) * ch.getLevel());
			count ++;
		}
		
		if (count == 0) {
			return 0;
		}
		
		// 这里原本乘的值分别是:
		// 256 / 1
		// 256 / 2
		// 256 / 3
		// 256 / 4
		// 256 / 5
		// 256 / 6
		// 由于只有一个轨道时声音比较爆炸, 所以这里也进行轨道声音的修正
		switch (count) {
		case 1:
			sum *= 100;
			break;
		case 2:
			sum *= 85;
			break;
		case 3:
			sum *= 72;
			break;
		case 4:
			sum *= 64;
			break;
		case 5:
			sum *= 56;
			break;
		default:
			sum *= 50;
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
		// and lower volumes on others. Using 5.2x as a rough "one size fits all".
		// 根据测量结果, N163 音量在不同的游戏中表现得很不稳定,
		// 范围在 APU 矩形脉冲波声音的 [3.4, 8.5] 倍. 这里决定采用 4.5 倍.
		
		// 另外提一句, 源程序用的是 6 倍, 但是我是要兼容 FTM 的.
		// 我用 6 倍来播放 FTM 后发现, 声音还是太响了, 因此把音量继续往下降至 4.5
		
		final double MASTER_VOL = 5503.5; // 4.5 * 1223.0
		final double MAX_OUT = 57600; // max digital value: = 15.0 * 15.0 * 256.0
		
		int v = (int) ((MASTER_VOL / MAX_OUT) * sum);
		v = intercept(v, 1);
		return v;
	}

}
