package zdream.nsfplayer.xgm.device.sound;

import zdream.nsfplayer.nsf.device.cpu.IntHolder;
import zdream.nsfplayer.xgm.device.ISoundChip;
import zdream.nsfplayer.xgm.device.ITrackInfo;
import zdream.nsfplayer.xgm.device.TrackInfoBasic;

/**
 * <p>模拟生成矩形波的声音芯片. 共有两个矩形波通道.</p>
 * Upper half of APU
 * @author Zdream
 */
public class NesAPU implements ISoundChip, IFrameSequencer, IDeviceValue {
	
	public static final int
			OPT_UNMUTE_ON_RESET = 0,
			OPT_PHASE_REFRESH = 1,
			OPT_NONLINEAR_MIXER = 2,
			OPT_DUTY_SWAP = 3,
			OPT_END = 4;
	
	public static final int
			SQR0_MASK = 1,
			SQR1_MASK = 2;
	
	/**
	 * 各种选择
	 */
	protected boolean[] option = new boolean[OPT_END];
	protected int mask;
	protected int[][] sm = new int[2][2];
	
	protected int gclock; // unsigned
	protected int[] reg = new int[0x20]; // unsigned byte
	protected int[] out = new int[2];
	protected double rate, clock;
	
	/**
	 * nonlinear mixer
	 */
	protected int[] square_table = new int[32];

	/**
	 * frequency divider
	 */
	protected int[] scounter = new int[2];
	/**
	 * <p>相位计数器. 缓存矩形波渲染到一个周期的哪个位置.
	 * <p>该数值只有低 4 位有效.
	 * <p>phase counter
	 * </p>
	 */
	protected int[] sphase = new int[2];

	/**
	 * <p>音色
	 */
	protected int[] duty = new int[2];
	/**
	 * <p>音量
	 * <p>这个音量是指矩形轨道中每个音色的音量.
	 * 这个值类似于 FamiTracker 中对每个音符设置的音量大小,
	 * 与用户设置的轨道音量无关.
	 * <p>有效值在 0 到 15 之间.
	 * </p>
	 */
	protected int[] volume = new int[2];
	/**
	 * <p>频率
	 * <p>反映指定矩形波中音符音调的值.
	 * </p>
	 */
	protected int[] freq = new int[2];
	/**
	 * <p>扫描频率
	 * <p>暂时不知道作用. 大多数情况为 -1, 仅在歌曲切换时有大于零的有效值.
	 * </p>
	 */
	protected int[] sfreq = new int[2];

	protected boolean[] sweep_enable = new boolean[2];
	protected boolean[] sweep_mode = new boolean[2];
	protected boolean[] sweep_write = new boolean[2];
	protected int[] sweep_div_period = new int[2];
	protected int[] sweep_div = new int[2];
	protected int[] sweep_amount = new int[2];

	protected boolean[] envelope_disable = new boolean[2];
	protected boolean[] envelope_loop = new boolean[2];
	protected boolean[] envelope_write = new boolean[2];
	protected int[] envelope_div_period = new int[2];
	protected int[] envelope_div = new int[2];
	protected int[] envelope_counter = new int[2];

	protected int[] length_counter = new int[2];

	protected boolean[] enable = new boolean[2];
	
	protected TrackInfoBasic[] trkinfo = new TrackInfoBasic[2];
	
	static final boolean[][] SQRT_BL = {
			{ false, false,  true,  true, false, false, false, false, false, false, false, false, false, false, false, false },
			{ false, false,  true,  true,  true,  true, false, false, false, false, false, false, false, false, false, false },
			{ false, false,  true,  true,  true,  true,  true,  true,  true,  true, false, false, false, false, false, false },
			{  true,  true, false, false, false, false,  true,  true,  true,  true,  true,  true,  true,  true,  true,  true }
	};
	
	{
		for (int i = 0; i < trkinfo.length; i++) {
			trkinfo[i] = new TrackInfoBasic();
		}
	}
	
	public NesAPU() {
		setClock(DEFAULT_CLOCK);
		setRate(DEFAULT_RATE);
		option[OPT_UNMUTE_ON_RESET] = true;
		option[OPT_PHASE_REFRESH] = true;
		option[OPT_NONLINEAR_MIXER] = true;
		option[OPT_DUTY_SWAP] = false;

		square_table[0] = 0;
		for (int i = 1; i < 32; i++)
			square_table[i] = (int) ((8192.0 * 95.88) / (8128.0 / i + 100));

		for (int c = 0; c < 2; ++c)
			for (int t = 0; t < 2; ++t)
				sm[c][t] = 128;
	}
	
	/**
	 * <p>计算目标扫描频率. 扫描频率将会放在成员变量 <code>sfreq</code> 中.</p>
	 * calculates target sweep frequency
	 * @param ch
	 *   计算第几个矩形通道. 有效值是 0 或者 1.
	 */
	protected void sweepSqr (int ch) {
		 int shifted = freq[ch] >> sweep_amount[ch];
		 if (ch == 0 && sweep_mode[ch]) shifted += 1;
		 sfreq[ch] = freq[ch] + (sweep_mode[ch] ? -shifted : shifted);
		 /*if (sfreq[ch] > 0) {
			 System.out.println(String.format("sfreq[%d] = %d", ch, sfreq[ch]));
		 }*/
	}
	
	@Override
	public void frameSequence(int s) {
		//DEBUG_OUT("FrameSequence(%d)\n",s);

		if (s > 3) return; // no operation in step 4

		// 240hz clock
		for (int i = 0; i < 2; ++i) {
			boolean divider = false;
			if (envelope_write[i]) {
				envelope_write[i] = false;
				envelope_counter[i] = 15;
				envelope_div[i] = 0;
			} else {
				++envelope_div[i];
				if (envelope_div[i] > envelope_div_period[i]) {
					divider = true;
					envelope_div[i] = 0;
				}
			}
			if (divider) {
				if (envelope_loop[i] && envelope_counter[i] == 0)
					envelope_counter[i] = 15;
				else if (envelope_counter[i] > 0)
					--envelope_counter[i];
			}
		}

		// 120hz clock
		if ((s & 1) == 0) {
			for (int i = 0; i < 2; ++i) {
				if (!envelope_loop[i] && (length_counter[i] > 0))
					--length_counter[i];

				if (sweep_enable[i]) {
					// DEBUG_OUT("Clock sweep: %d\n", i);

					--sweep_div[i];
					if (sweep_div[i] <= 0) {
						sweepSqr(i); // calculate new sweep target

						// DEBUG_OUT("sweep_div[%d] (0/%d)\n",i,sweep_div_period[i]);
						// DEBUG_OUT("freq[%d]=%d > sfreq[%d]=%d\n",i,freq[i],i,sfreq[i]);

						if (freq[i] >= 8 && sfreq[i] < 0x800 && sweep_amount[i] > 0) { // update frequency if appropriate
							freq[i] = sfreq[i] < 0 ? 0 : sfreq[i];
							if (scounter[i] > freq[i])
								scounter[i] = freq[i];
						}
						sweep_div[i] = sweep_div_period[i] + 1;

						// DEBUG_OUT("freq[%d]=%d\n",i,freq[i]);
					}

					if (sweep_write[i]) {
						sweep_div[i] = sweep_div_period[i] + 1;
						sweep_write[i] = false;
					}
				}
			}
		}
	}
	
	/**
	 * 
	 * @param i
	 * @param clocks
	 *   unsigned
	 * @return
	 */
	protected int calcSqr(int i, int clocks) {
		scounter[i] += clocks;
		while (scounter[i] > freq[i]) {
			sphase[i] = (sphase[i] + 1) & 15; // 16 为一周期
			scounter[i] -= (freq[i] + 1);
		}

		int ret = 0;
		if (length_counter[i] > 0 && freq[i] >= 8 && sfreq[i] < 0x800) {
			int v = envelope_disable[i] ? volume[i] : envelope_counter[i];
			ret = (SQRT_BL[duty[i]][sphase[i]]) ? v : 0;
		}

		return ret;
	}

	@Override
	public boolean read(int adr, IntHolder val, int id) {
		if (0x4000 <= adr && adr < 0x4008) {
			val.val |= reg[adr & 0x7];
			return true;
		} else if (adr == 0x4015) {
			val.val |= (length_counter[1] != 0 ? 2 : 0) | (length_counter[0] != 0 ? 1 : 0);
			return true;
		} else
			return false;
	}
	
	//int ticks = 0;
	int count = 0;
	long time = -1;

	@Override
	public void tick(int clocks) {
//		if (i == 0)
//			System.out.println(freq[i]);
		out[0] = calcSqr(0, clocks);
		out[1] = calcSqr(1, clocks);
	}
	
	// debug
	int debug_reader_count = 0;

	/**
	 * 生成波形的振幅在 0 - 8191 之间
	 */
	public int render(int[] bs) {
		debug_reader_count++;
		
		out[0] = (mask & 1) != 0 ? 0 : out[0];
		out[1] = (mask & 2) != 0 ? 0 : out[1];

		int m0, m1;

		if (option[OPT_NONLINEAR_MIXER]) {
			int voltage = square_table[out[0] + out[1]];
			m0 = out[0] << 6;
			m1 = out[1] << 6;
			int ref = m0 + m1;
			if (ref > 0) {
				m0 = (m0 * voltage) / ref;
				m1 = (m1 * voltage) / ref;
			} else {
				m0 = voltage;
				m1 = voltage;
			}
		} else {
			m0 = out[0] << 6;
			m1 = out[1] << 6;
		}

		bs[0] = m0 * sm[0][0];
		bs[0] += m1 * sm[0][1];
		bs[0] >>= 7;

		bs[1] = m0 * sm[1][0];
		bs[1] += m1 * sm[1][1];
		bs[1] >>= 7;
		
		// System.out.println(String.format("APU<%d>: %d", debug_reader_count, bs[0]));
		if (debug_reader_count % 1000 == 0) {
			//System.out.println();
		}
		
		return 2;
	}

	@Override
	public void reset() {
		int i;
		gclock = 0;
		mask = 0;

		scounter[0] = 0;
		scounter[1] = 0;
		sphase[0] = 0;
		sphase[1] = 0;

		sweep_div[0] = 1;
		sweep_div[1] = 1;
		envelope_div[0] = 0;
		envelope_div[1] = 0;
		length_counter[0] = 0;
		length_counter[1] = 0;
		envelope_counter[0] = 0;
		envelope_counter[1] = 0;

		for (i = 0x4000; i < 0x4008; i++)
			write(i, 0, 0);

		write(0x4015, 0, 0);
		if (option[OPT_UNMUTE_ON_RESET])
			write(0x4015, 0x0f, 0);

		for (i = 0; i < 2; i++)
			out[i] = 0;

		setRate(rate);
		
		debug_reader_count = 0;
	}

	@Override
	public void setOption(int id, int val) {
		if (id < OPT_END)
			option[id] = val == 1;
	}

	@Override
	public void setClock(double c) {
		clock = c;
	}

	@Override
	public void setRate(double r) {
		rate = (r != 0) ? r : DEFAULT_RATE;
	}

	@Override
	public void setStereoMix(int trk, int mixl, int mixr) {
		if (trk < 0)
			return;
		if (trk > 1)
			return;
		sm[0][trk] = mixl;
		sm[1][trk] = mixr;
	}

	@Override
	public ITrackInfo getTrackInfo(int trk) {
		trkinfo[trk]._freq = freq[trk];
		if (freq[trk] != 0)
			trkinfo[trk].freq = clock / 16 / (freq[trk] + 1);
		else
			trkinfo[trk].freq = 0;

		trkinfo[trk].output = out[trk];
		trkinfo[trk].volume = volume[trk] + (envelope_disable[trk] ? 0 : 0x10) + (envelope_loop[trk] ? 0x20 : 0);
		trkinfo[trk].key = enable[trk] && length_counter[trk] > 0 && freq[trk] >= 8 && sfreq[trk] < 0x800
				&& (envelope_disable[trk] ? volume[trk] != 0 : (envelope_counter[trk] > 0));
		trkinfo[trk].tone = duty[trk];
		trkinfo[trk].maxVolume = 15;
		return trkinfo[trk];
	}

	@Override
	public boolean write(int adr, int val, int id) {
		/*if (adr != 0x4015) {
			count++;
			//ticks += clocks;
			if (time == -1) {
				time = System.currentTimeMillis();
			}
			if ((count & 0xFF) == 0)
				System.out.println(count + ":" + (System.currentTimeMillis() - time) + "ms");
		}*/

		if (0x4000 <= adr && adr < 0x4008) {
			// DEBUG_OUT("$%04X = %02X\n",adr,val);

			adr &= 0xf;
			// ch 只有两个值, 0 或者 1
			int ch = adr >> 2;
		
			// debug
//			if (adr < 4){
//				System.out.println(String.format("%d:%8s", adr, Integer.toString(val, 2)));
//			}
			// debug end
		
			switch (adr) {
			case 0x0:
			case 0x4:
				volume[ch] = val & 15;
				envelope_disable[ch] = ((val >> 4) & 1) != 0;
				envelope_loop[ch] = ((val >> 5) & 1) != 0;
				// 这里, envelope_div_period[ch] = volume[ch]
				envelope_div_period[ch] = (val & 15);
				duty[ch] = (val >> 6) & 3;
				if (option[OPT_DUTY_SWAP]) {
					if (duty[ch] == 1)
						duty[ch] = 2;
					else if (duty[ch] == 2)
						duty[ch] = 1;
				}
				break;

			case 0x1:
			case 0x5:
				sweep_enable[ch] = ((val >> 7) & 1) != 0;
				sweep_div_period[ch] = (((val >> 4) & 7));
				sweep_mode[ch] = ((val >> 3) & 1) != 0;
				sweep_amount[ch] = val & 7;
				sweep_write[ch] = true;
				sweepSqr(ch);
				break;

			case 0x2:
			case 0x6:
				freq[ch] = val | (freq[ch] & 0x700);
				sweepSqr(ch);
				if (scounter[ch] > freq[ch])
					scounter[ch] = freq[ch];
				break;

			case 0x3:
			case 0x7:

				final int length_table[] = { // len : 32
					0x0A, 0xFE,
					0x14, 0x02,
					0x28, 0x04,
					0x50, 0x06,
					0xA0, 0x08,
					0x3C, 0x0A,
					0x0E, 0x0C,
					0x1A, 0x0E,
					0x0C, 0x10,
					0x18, 0x12,
					0x30, 0x14,
					0x60, 0x16,
					0xC0, 0x18,
					0x48, 0x1A,
					0x10, 0x1C,
					0x20, 0x1E
				};
				
				freq[ch] = (freq[ch] & 0xFF) | ((val & 0x7) << 8);
				if (option[OPT_PHASE_REFRESH])
					sphase[ch] = 0;
				envelope_write[ch] = true;
				if (enable[ch]) {
					length_counter[ch] = length_table[(val >> 3) & 0x1f];
				}
				sweepSqr(ch);
				if (scounter[ch] > freq[ch])
					scounter[ch] = freq[ch];
				break;

			default:
				return false;
			}
			reg[adr] = val;
			return true;
		} else if (adr == 0x4015) {
			enable[0] = (val & 1) != 0 ? true : false;
			enable[1] = (val & 2) != 0 ? true : false;

			if (!enable[0])
				length_counter[0] = 0;
			if (!enable[1])
				length_counter[1] = 0;

			reg[adr - 0x4000] = val;
			return true;
		} else if (adr == 0x4017) {
			// TODO
			return true;
		}

		return false;
	}

	@Override
	public void setMask(int m) {
		mask = m;
	}

}
