package zdream.nsfplayer.xgm.device.sound;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.xgm.device.ISoundChip;
import zdream.nsfplayer.xgm.device.ITrackInfo;
import zdream.nsfplayer.xgm.device.TrackInfoBasic;

public class NesVRC6 implements ISoundChip, IDeviceValue {

	/** frequency divider, unsigned */
	protected int[] counter = new int[3];
	/** phase counter, unsigned */
	protected int[] phase = new int[3];
	/** adjusted frequency, unsigned */
	protected int[] freq2 = new int[3];
	/** saw 14-stage counter */
	protected int count14;

	protected int mask;
	/** stereo mix */
	protected int[][] sm = new int[2][3];
	protected int[] duty = new int[2];
	protected int[] volume = new int[3];
	protected int[] enable = new int[3];
	protected int[] gate = new int[3];
	/** unsigned */
	protected int[] freq = new int[3];
//	protected int calc_sqr (int i, UINT32 clocks);
//	protected int calc_saw (UINT32 clocks);
	protected boolean halt;
	protected int freq_shift;
	protected double clock, rate;
	protected int[] out = new int[3];
	protected TrackInfoBasic[] trkinfo = new TrackInfoBasic[3];
	
	{
		for (int i = 0; i < trkinfo.length; i++) {
			trkinfo[i] = new TrackInfoBasic();
		}
	}
	
	private static final boolean[][] SQRT_BL = {
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, true},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, false, true, true},
			{false, false, false, false, false, false, false, false, false, false, false, false, false, true, true, true},
			{false, false, false, false, false, false, false, false, false, false, false, false, true, true, true, true},
			{false, false, false, false, false, false, false, false, false, false, false, true, true, true, true, true},
			{false, false, false, false, false, false, false, false, false, false, true, true, true, true, true, true},
			{false, false, false, false, false, false, false, false, false, true, true, true, true, true, true, true},
			{false, false, false, false, false, false, false, false, true, true, true, true, true, true, true, true}
	};
	
	public NesVRC6() {
		setClock(DEFAULT_CLOCK);
		setRate(DEFAULT_RATE);

		halt = false;
		freq_shift = 0;

		for (int c = 0; c < 2; ++c)
			for (int t = 0; t < 3; ++t)
				sm[c][t] = 128;
	}

	@Override
	public void setStereoMix(int trk, int mixl, int mixr) {
		if (trk < 0)
			return;
		if (trk > 2)
			return;
		sm[0][trk] = mixl;
		sm[1][trk] = mixr;
	}

	@Override
	public ITrackInfo getTrackInfo(int trk) {
		if (trk < 2) {
			trkinfo[trk].maxVolume = 15;
			trkinfo[trk].volume = volume[trk];
			trkinfo[trk]._freq = freq2[trk];
			trkinfo[trk].freq = freq2[trk] != 0 ? clock / 16 / (freq2[trk] + 1) : 0;
			trkinfo[trk].tone = duty[trk];
			trkinfo[trk].key = (volume[trk] > 0) && enable[trk] != 0 && gate[trk] == 0;
			return trkinfo[trk];
		} else if (trk == 2) {
			trkinfo[2].maxVolume = 255;
			trkinfo[2].volume = volume[2];
			trkinfo[2]._freq = freq2[2];
			trkinfo[2].freq = freq2[2] != 0 ? clock / 14 / (freq2[2] + 1) : 0;
			trkinfo[2].tone = -1;
			trkinfo[2].key = (enable[2] > 0);
			return trkinfo[2];
		} else
			return null;
	}

	@Override
	public void setClock(double c) {
		clock = c;
	}

	@Override
	public void setRate(double r) {
		rate = r != 0 ? r : DEFAULT_RATE;
	}

	@Override
	public void reset() {
		write(0x9003, 0, 0);
		for (int i = 0; i < 3; i++) {
			write(0x9000 + i, 0, 0);
			write(0xa000 + i, 0, 0);
			write(0xb000 + i, 0, 0);
		}
		count14 = 0;
		mask = 0;
		counter[0] = 0;
		counter[1] = 0;
		counter[2] = 0;
		phase[0] = 0;
		phase[0] = 1;
		phase[0] = 2;
	}
	
	public int calc_sqr(int i, int clocks) {
		if (enable[i] == 0)
			return 0;

		if (!halt) {
			counter[i] += clocks;
			while (counter[i] > freq2[i]) {
				phase[i] = (phase[i] + 1) & 15;
				counter[i] -= (freq2[i] + 1);
			}
		}

		return (gate[i] != 0 || SQRT_BL[duty[i]][phase[i]]) ? volume[i] : 0;
	}
	
	public final int calc_saw(int clocks) {
		if (enable[2] == 0)
			return 0;

		if (!halt) {
			counter[2] += clocks;
			while (counter[2] > freq2[2]) {
				counter[2] -= (freq2[2] + 1);

				// accumulate saw
				++count14;
				if (count14 >= 14) {
					count14 = 0;
					phase[2] = 0;
				} else if (0 == (count14 & 1)) // only accumulate on even ticks
				{
					phase[2] = (phase[2] + volume[2]) & 0xFF; // note 8-bit wrapping behaviour
				}
			}
		}

		// only top 5 bits of saw are output
		return phase[2] >> 3;
	}

	@Override
	public void tick(int clocks) {
		out[0] = calc_sqr(0, clocks);
		out[1] = calc_sqr(1, clocks);
		out[2] = calc_saw(clocks);
	}

	@Override
	public int render(int[] bs) {
		int[] m = new int[3];
		m[0] = out[0];
		m[1] = out[1];
		m[2] = out[2];

		// note: signal is inverted compared to 2A03

		m[0] = (mask & 1) != 0 ? 0 : -m[0];
		m[1] = (mask & 2) != 0 ? 0 : -m[1];
		m[2] = (mask & 4) != 0 ? 0 : -m[2];

		bs[0] = m[0] * sm[0][0];
		bs[0] += m[1] * sm[0][1];
		bs[0] += m[2] * sm[0][2];
		// b[0] >>= (7 - 7);

		bs[1] = m[0] * sm[1][0];
		bs[1] += m[1] * sm[1][1];
		bs[1] += m[2] * sm[1][2];
		// b[1] >>= (7 - 7);

		// master volume adjustment
		final int MASTER = (int) (256.0 * 1223.0 / 1920.0);
		bs[0] = (bs[0] * MASTER) >> 8;
		bs[1] = (bs[1] * MASTER) >> 8;

		return 2;
	}

	@Override
	public boolean write(int adr, int val, int id) {
		int ch, cmap[] = { 0, 0, 1, 2 };

		switch (adr) {
		case 0x9000:
		case 0xa000:
			ch = cmap[(adr >> 12) & 3];
			volume[ch] = val & 15;
			duty[ch] = (val >> 4) & 7;
			gate[ch] = (val >> 7) & 1;
			break;
		case 0xb000:
			volume[2] = val & 63;
			break;

		case 0x9001:
		case 0xa001:
		case 0xb001:
			ch = cmap[(adr >> 12) & 3];
			freq[ch] = (freq[ch] & 0xf00) | val;
			freq2[ch] = (freq[ch] >> freq_shift);
			if (counter[ch] > freq2[ch])
				counter[ch] = freq2[ch];
			break;

		case 0x9002:
		case 0xa002:
		case 0xb002:
			ch = cmap[(adr >> 12) & 3];
			freq[ch] = ((val & 0xf) << 8) + (freq[ch] & 0xff);
			freq2[ch] = (freq[ch] >> freq_shift);
			if (counter[ch] > freq2[ch])
				counter[ch] = freq2[ch];
			if (enable[ch] == 0) // if enable is being turned on, phase should be reset
			{
				if (ch == 2) {
					count14 = 0; // reset saw
				}
				phase[ch] = 0;
			}
			enable[ch] = (val >> 7) & 1;
			break;

		case 0x9003:
			halt = ((val & 1) != 0);
			freq_shift = (val & 4) != 0 ? 8 : (val & 2) != 0 ? 4 : 0;
			freq2[0] = (freq[0] >> freq_shift);
			freq2[1] = (freq[1] >> freq_shift);
			freq2[2] = (freq[2] >> freq_shift);
			if (counter[0] > freq2[0])
				counter[0] = freq2[0];
			if (counter[1] > freq2[1])
				counter[1] = freq2[1];
			if (counter[2] > freq2[2])
				counter[2] = freq2[2];
			break;

		default:
			return false;
		}
		return true;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		return false;
	}

	@Override
	public void setOption(int id, int value) {}

	@Override
	public void setMask(int m) {
		mask = m;
	}

}
