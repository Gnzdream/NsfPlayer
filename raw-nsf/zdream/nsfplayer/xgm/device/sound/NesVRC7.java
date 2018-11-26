package zdream.nsfplayer.xgm.device.sound;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.xgm.device.ISoundChip;
import zdream.nsfplayer.xgm.device.ITrackInfo;
import zdream.nsfplayer.xgm.device.TrackInfoBasic;
import zdream.nsfplayer.xgm.device.sound.legacy.OPLL_env;
import zdream.nsfplayer.xgm.device.sound.legacy.OPLL_env.OPLL;

public class NesVRC7 implements ISoundChip, IDeviceValue {
	
	protected int mask;
	/** unsigned */
	protected int patch_set;
	/** stereo mix */
	protected int[][] sm = new int[2][6];
	protected int[] buf = new int[2];
	protected OPLL opll;
	/** clock divider, unsigned */
	protected int divider;
	protected double clock, rate;
	protected TrackInfoBasic[] trkinfo = new TrackInfoBasic[6];
	
	final OPLL_env env = OPLL_env.ins;
	
	{
		for (int i = 0; i < trkinfo.length; i++) {
			trkinfo[i] = new TrackInfoBasic();
		}
	}
	
	public NesVRC7() {
		patch_set = OPLL_env.OPLL_VRC7_RW_TONE;

		opll = env.createOPLL(3579545, DEFAULT_RATE);
		env.reset_patch(opll, patch_set);
		setClock(DEFAULT_CLOCK);

		for (int c = 0; c < 2; ++c)
			for (int t = 0; t < 6; ++t)
				sm[c][t] = 128;
	}
	
	/**
	 * @param p
	 *   unsigned
	 */
	public void setPatchSet(int p) {
		patch_set = p;
	}

	@Override
	public void setClock(double c) {
		clock = c / 36;
	}

	@Override
	public void setRate(double r) {
		// rate = r ? r : DEFAULT_RATE;
		// r is ignored
		rate = 49716;
		env.setQuality(opll, 1); // quality always on (not really a CPU hog)
		env.setRate(opll, (int) rate);
	}

	@Override
	public void reset() {
		for (int i = 0; i < 0x40; ++i) {
			write(0x9010, i, 0);
			write(0x9030, 0, 0);
		}

		divider = 0;
		env.reset_patch(opll, patch_set);
		env.reset(opll);
	}

	@Override
	public void setStereoMix(int trk, int mixl, int mixr) {
		if (trk < 0)
			return;
		if (trk > 5)
			return;
		sm[0][trk] = mixl;
		sm[1][trk] = mixr;
	}

	@Override
	public ITrackInfo getTrackInfo(int trk) {
		if (opll != null && trk < 6) {
			trkinfo[trk].maxVolume = 15;
			trkinfo[trk].volume = 15 - ((opll.reg[0x30 + trk]) & 15);
			trkinfo[trk]._freq = opll.reg[0x10 + trk] + ((opll.reg[0x20 + trk] & 1) << 8);
			int blk = (opll.reg[0x20 + trk] >> 1) & 7;
			trkinfo[trk].freq = clock * trkinfo[trk]._freq / (double) (0x80000 >> blk);
			trkinfo[trk].tone = (opll.reg[0x30 + trk] >> 4) & 15;
			trkinfo[trk].key = (opll.reg[0x20 + trk] & 0x10) != 0 ? true : false;
			return trkinfo[trk];
		} else
			return null;
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0x9010) {
			env.writeIO(opll, 0, val);
			return true;
		}
		if (adr == 0x9030) {
			env.writeIO(opll, 1, val);
			return true;
		} else
			return false;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		return false;
	}

	@Override
	public void tick(int clocks) {
		divider += clocks;
		while (divider >= 36) {
			divider -= 36;
			env.calc0(opll);
		}
	}

	@Override
	public int render(int[] bs) {
		bs[0] = bs[1] = 0;
		for (int i = 0; i < 6; ++i) {
			int val = (mask & (1 << i)) != 0 ? 0 : opll.slot[(i << 1) | 1].output[1];
			bs[0] += val * sm[0][i];
			bs[1] += val * sm[1][i];
		}
		bs[0] >>= (7 - 4);
		bs[1] >>= (7 - 4);

		// master volume adjustment
		final int MASTER = (int) (0.8 * 256.0);
		bs[0] = (bs[0] * MASTER) >> 8;
		bs[1] = (bs[1] * MASTER) >> 8;

		return 2;
	}

	@Override
	public void setOption(int id, int value) { }

	@Override
	public void setMask(int m) {
		mask = m;
		if (opll != null)
			env.setMask(opll, m);
	}

}
