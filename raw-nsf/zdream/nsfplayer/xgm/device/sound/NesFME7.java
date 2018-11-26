package zdream.nsfplayer.xgm.device.sound;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.xgm.device.ISoundChip;
import zdream.nsfplayer.xgm.device.ITrackInfo;
import zdream.nsfplayer.xgm.device.TrackInfoBasic;
import zdream.nsfplayer.xgm.device.sound.legacy.PSG;

public class NesFME7 implements ISoundChip, IDeviceValue {
	
	protected int[][] sm = new int[2][3]; // stereo mix
	protected int[] buf = new int[2];
	protected PSG psg;
	protected int divider; // clock divider
	protected double clock, rate;
	protected TrackInfoBasic[] trkinfo = new TrackInfoBasic[5];
	
	{
		for (int i = 0; i < trkinfo.length; i++) {
			trkinfo[i] = new TrackInfoBasic();
		}
	}
	
	/** TODO this is not optimal, rewrite PSG output */
	protected static final int DIVIDER = 8;
	
	public NesFME7() {
		psg = new PSG((int) DEFAULT_CLOCK, DEFAULT_RATE);

		for (int c = 0; c < 2; ++c)
			for (int t = 0; t < 3; ++t)
				sm[c][t] = 128;
	}

	@Override
	public void setClock(double c) {
		clock = c * 2.0;
	}

	@Override
	public void setRate(double rate) {
		// rate = r ? r : DEFAULT_RATE;
		rate = DEFAULT_CLOCK / (double) DIVIDER; // TODO rewrite PSG to integrate with clock
		if (psg != null)
			psg.setRate((int) rate);
	}
	
	@Override
	public void reset() {
		for (int i = 0; i < 16; ++i) { // blank all registers
			write(0xC000, i, 0);
			write(0xE000, 0, 0);
		}
		write(0xC000, 0x07, 0); // disable all tones
		write(0xE000, 0x3F, 0);

		divider = 0;
		if (psg != null)
			psg.reset();
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0xC000) {
			if (psg != null)
				psg.writeIO(0, val);
			return true;
		}
		if (adr == 0xE000) {
			if (psg != null)
				psg.writeIO(1, val);
			return true;
		} else
			return false;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		// not sure why this was here - BS
		return false;
	}

	@Override
	public void tick(int clocks) {
		divider += clocks;
		while (divider >= DIVIDER) {
			divider -= DIVIDER;
			if (psg != null)
				psg.calc1();
		}
	}

	@Override
	public int render(int[] bs) {
		bs[0] = bs[1] = 0;

		for (int i = 0; i < 3; ++i) {
			// note negative polarity
			bs[0] -= psg.cout[i] * sm[0][i];
			bs[1] -= psg.cout[i] * sm[1][i];
		}
		bs[0] >>= (7 - 4);
		bs[1] >>= (7 - 4);

		// master volume adjustment
		final int MASTER = (int) (0.64 * 256.0);
		bs[0] = (bs[0] * MASTER) >> 8;
		bs[1] = (bs[1] * MASTER) >> 8;

		return 2;
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
		assert (trk < 5);

		if (psg != null) {
			if (trk < 3) {
				trkinfo[trk]._freq = psg.freq[trk];
				if (psg.freq[trk] != 0)
					trkinfo[trk].freq = psg.clk / 32.0 / psg.freq[trk];
				else
					trkinfo[trk].freq = 0;

				trkinfo[trk].output = psg.cout[trk];
				trkinfo[trk].maxVolume = 15;
				trkinfo[trk].volume = psg.volume[trk] >> 1;
				// trkinfo[trk].key = (psg.cout[trk]>0)?true:false;
				trkinfo[trk].key = psg.tmask[trk] == 0;
				trkinfo[trk].tone = (psg.tmask[trk] != 0 ? 2 : 0) + (psg.nmask[trk] != 0 ? 1 : 0);
			} else if (trk == 3) // envelope
			{
				trkinfo[trk]._freq = psg.env_freq;
				if (psg.env_freq != 0)
					trkinfo[trk].freq = psg.clk / 512.0 / psg.env_freq;
				else
					trkinfo[trk].freq = 0;

				if (psg.env_continue != 0 && psg.env_alternate != 0 && psg.env_hold == 0) // triangle wave
				{
					trkinfo[trk].freq *= 0.5f; // sounds an octave down
				}

				trkinfo[trk].output = psg.voltbl[psg.env_ptr];
				trkinfo[trk].maxVolume = 0;
				trkinfo[trk].volume = 0;
				trkinfo[trk].key = (((psg.volume[0] | psg.volume[1] | psg.volume[2]) & 32) != 0);
				trkinfo[trk].tone = (psg.env_continue != 0 ? 8 : 0) | (psg.env_attack != 0 ? 4 : 0)
						| (psg.env_alternate != 0 ? 2 : 0) | (psg.env_hold != 0 ? 1 : 0);
			} else if (trk == 4) // noise
			{
				trkinfo[trk]._freq = psg.noise_freq >> 1;
				if (trkinfo[trk]._freq > 0)
					trkinfo[trk].freq = psg.clk / 16.0 / psg.noise_freq;
				else
					trkinfo[trk].freq = 0;

				trkinfo[trk].output = psg.noise_seed & 1;
				trkinfo[trk].maxVolume = 0;
				trkinfo[trk].volume = 0;
				// trkinfo[trk].key = ((psg.nmask[0]&psg.nmask[1]&psg.nmask[2]) == 0);
				trkinfo[trk].key = false;
				trkinfo[trk].tone = 0;
			}
		}
		return trkinfo[trk];
	}

	@Override
	public void setOption(int id, int value) {}

	@Override
	public void setMask(int m) {
		if (psg != null)
			psg.setMask(m);
	}

}
