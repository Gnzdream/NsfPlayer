package zdream.nsfplayer.xgm.device.sound;

import java.util.Arrays;

import zdream.nsfplayer.xgm.device.ISoundChip;
import zdream.nsfplayer.xgm.device.ITrackInfo;
import zdream.nsfplayer.xgm.device.IntHolder;

public class NesN106 implements ISoundChip {
	
	public static final int
			OPT_SERIAL = 0,
	        OPT_END = 1;
	
	protected double rate, clock;
	protected int mask;
	/** stereo mix */
	protected int[][] sm = new int[2][8];
	/** current output */
	protected int[] fout = new int[8];
	protected TrackInfoN106[] trkinfo = new TrackInfoN106[8];
	protected int[] option = new int[OPT_END];

	protected boolean master_disable;
	/** all state is contained here, unsigned */
	protected int[] reg = new int[0x80];
	/** unsigned */
	protected int reg_select;
	protected boolean reg_advance;
	protected int tick_channel;
	protected int tick_clock;
	protected int render_channel;
	protected int render_clock;
	protected int render_subclock;
	
	{
		for (int i = 0; i < trkinfo.length; i++) {
			trkinfo[i] = new TrackInfoN106();
		}
	}
	
	public NesN106() {
		option[OPT_SERIAL] = 0;
	    setClock (DEFAULT_CLOCK);
	    setRate (DEFAULT_RATE);
	    for (int i=0; i < 8; ++i)
	    {
	        sm[0][i] = 128;
	        sm[1][i] = 128;
	    }
	    reset();
	}

	@Override
	public void setStereoMix(int trk, int mixl, int mixr) {
		if (trk < 0 || trk >= 8)
			return;
		trk = 7 - trk; // displayed channels are inverted
		sm[0][trk] = mixl;
		sm[1][trk] = mixr;
	}

	@Override
	public ITrackInfo getTrackInfo(int trk) {
		int channels = ((reg[0x7F] >> 4) & 0x07) + 1;
		int channel = 7 - trk; // invert the track display

		TrackInfoN106 t = trkinfo[channel];

		if (trk >= channels) {
			t.maxVolume = 15;
			t.volume = 0;
			t._freq = 0;
			t.wavelen = 0;
			t.tone = -1;
			t.output = 0;
			t.key = false;
			t.freq = 0;
		} else {
			t.maxVolume = 15;
			t.volume = reg[0x47 + channel] & 0x0F;
			int x = channel << 3;
			t._freq = (reg[0x40 + x]) + (reg[0x42 + x] << 8) + ((reg[0x44 + x] & 0x03) << 16);
			t.wavelen = 256 - (reg[0x44 + x] & 0xFC);
			t.tone = reg[0x46 + x];
			t.output = fout[channel];

			t.key = (t.volume > 0) && (t._freq > 0);
			t.freq = ((double) t._freq * clock) / (double) (15 * 65536 * channels * t.wavelen);

			for (int i = 0; i < t.wavelen; ++i) {
				int index = (i + t.tone) & 0xFF;

				t.wave[i] = (index & 1) != 0 ? ((reg[index >> 1] >> 4) & 0x0F) : (reg[index >> 1] & 0x0F);
			}
		}

		return t;
	}

	@Override
	public void setClock(double c) {
		clock = c;
	}

	@Override
	public void setRate(double r) {
		rate = r;
	}

	@Override
	public void setMask(int m) {
		// bit reverse the mask,
	    // N163 waves are displayed in reverse order
	    mask = 0
	        | ((m & (1<<0)) != 0 ? (1<<7) : 0)
	        | ((m & (1<<1)) != 0 ? (1<<6) : 0)
	        | ((m & (1<<2)) != 0 ? (1<<5) : 0)
	        | ((m & (1<<3)) != 0 ? (1<<4) : 0)
	        | ((m & (1<<4)) != 0 ? (1<<3) : 0)
	        | ((m & (1<<5)) != 0 ? (1<<2) : 0)
	        | ((m & (1<<6)) != 0 ? (1<<1) : 0)
	        | ((m & (1<<7)) != 0 ? (1<<0) : 0);
	}

	@Override
	public void setOption(int id, int val) {
		if (id < OPT_END)
			option[id] = val;
	}

	@Override
	public void reset() {
		master_disable = false;
		Arrays.fill(reg, 0);
	    reg_select = 0;
	    reg_advance = false;
	    tick_channel = 0;
	    tick_clock = 0;
	    render_channel = 0;
	    render_clock = 0;
	    render_subclock = 0;

	    for (int i=0; i<8; ++i) fout[i] = 0;

	    write(0xE000, 0x00, 0); // master disable off
	    write(0xF800, 0x80, 0); // select $00 with auto-increment
	    for (int i=0; i<0x80; ++i) // set all regs to 0
	    {
	        write(0x4800, 0x00, 0);
	    }
	    write(0xF800, 0x00, 0); // select $00 without auto-increment
	}

	@Override
	public void tick(int clocks) {
		if (master_disable)
			return;

		int channels = ((reg[0x7F] >> 4) & 0x07) + 1;

		tick_clock += clocks;
		render_clock += clocks; // keep render in sync
		while (tick_clock > 0) {
			int channel = 7 - tick_channel;

			int x = channel << 3;
			int phase = (reg[0x41 + x]) + (reg[0x43 + x] << 8) + (reg[0x45 + x] << 16);
			int freq = (reg[0x40 + x]) + (reg[0x42 + x] << 8) + ((reg[0x44 + x] & 0x03) << 16);
			int len = 256 - (reg[0x44 + x] & 0xFC);
			int off = reg[0x46 + x];
			int vol = reg[0x47 + x] & 0x0F;

			// accumulate 24-bit phase
			phase = (phase + freq) & 0x00FFFFFF;

			// wrap phase if wavelength exceeded
			int hilen = len << 16;
			while (phase >= hilen)
				phase -= hilen;

			// write back phase
			reg[0x41 + x] = phase & 0xFF;
			reg[0x43 + x] = (phase >> 8) & 0xFF;
			reg[0x45 + x] = (phase >> 16) & 0xFF;

			// fetch sample (note: N163 output is centred at 8, and inverted w.r.t 2A03)
			int index = ((phase >> 16) + off) & 0xFF;
			int sample = 8 - ((index & 1) != 0 ? ((reg[index >> 1] >> 4) & 0x0F) : (reg[index >> 1] & 0x0F));
			fout[channel] = sample * vol;

			// cycle to next channel every 15 clocks
			tick_clock -= 15;
			++tick_channel;
			if (tick_channel >= channels)
				tick_channel = 0;
		}
	}

	@Override
	public int render(int[] bs) {
		bs[0] = 0;
		bs[1] = 0;
		if (master_disable)
			return 2;

		int channels = ((reg[0x7F] >> 4) & 0x07) + 1;

		if (option[OPT_SERIAL] != 0) // hardware accurate serial multiplexing
		{
			// this could be made more efficient than going clock-by-clock
			// but this way is simpler
			int clocks = render_clock;
			while (clocks > 0) {
				int c = 7 - render_channel;
				if (0 == ((mask >> c) & 1)) {
					bs[0] += fout[c] * sm[0][c];
					bs[1] += fout[c] * sm[1][c];
				}

				++render_subclock;
				if (render_subclock >= 15) // each channel gets a 15-cycle slice
				{
					render_subclock = 0;
					++render_channel;
					if (render_channel >= channels)
						render_channel = 0;
				}
				--clocks;
			}

			// increase output level by 1 bits (7 bits already added from sm)
			bs[0] <<= 1;
			bs[1] <<= 1;

			// average the output
			if (render_clock > 0) {
				bs[0] /= render_clock;
				bs[1] /= render_clock;
			}
			render_clock = 0;
		} else // just mix all channels
		{
			for (int i = (8 - channels); i < 8; ++i) {
				if (0 == ((mask >> i) & 1)) {
					bs[0] += fout[i] * sm[0][i];
					bs[1] += fout[i] * sm[1][i];
				}
			}

			// mix together, increase output level by 8 bits, roll off 7 bits from sm
			int MIX[] = { 256 / 1, 256 / 1, 256 / 2, 256 / 3, 256 / 4, 256 / 5, 256 / 6, 256 / 6, 256 / 6 };
			bs[0] = (bs[0] * MIX[channels]) >> 7;
			bs[1] = (bs[1] * MIX[channels]) >> 7;
			// when approximating the serial multiplex as a straight mix, once the
			// multiplex frequency gets below the nyquist frequency an average mix
			// begins to sound too quiet. To approximate this effect, I don't attenuate
			// any further after 6 channels are active.
		}

		// 8 bit approximation of master volume
		// max N163 vol vs max APU square
		// unfortunately, games have been measured as low as 3.4x and as high as 8.5x
		// with higher volumes on Erika, King of Kings, and Rolling Thunder
		// and lower volumes on others. Using 6.0x as a rough "one size fits all".
		final double MASTER_VOL = 6.0 * 1223.0;
		final double MAX_OUT = 15.0 * 15.0 * 256.0; // max digital value
		final int GAIN = (int) ((MASTER_VOL / MAX_OUT) * 256.0f);
		bs[0] = (bs[0] * GAIN) >> 8;
		bs[1] = (bs[1] * GAIN) >> 8;

		return 2;
	}

	@Override
	public boolean write(int adr, int val, int id) {
		if (adr == 0xE000) { // master disable
			master_disable = ((val & 0x40) != 0);
			return true;
		} else if (adr == 0xF800) { // register select
			reg_select = (val & 0x7F);
			reg_advance = (val & 0x80) != 0;
			return true;
		} else if (adr == 0x4800) { // register write
			reg[reg_select] = val;
			if (reg_advance)
				reg_select = (reg_select + 1) & 0x7F;
			return true;
		}
		return false;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (adr == 0x4800) { // register read
			val.val = reg[reg_select];
			if (reg_advance)
				reg_select = (reg_select + 1) & 0x7F;
			return true;
		}
		return false;
	}

}
