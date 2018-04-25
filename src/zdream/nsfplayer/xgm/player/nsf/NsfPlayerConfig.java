package zdream.nsfplayer.xgm.player.nsf;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import zdream.nsfplayer.vcm.Value;
import zdream.nsfplayer.xgm.device.sound.NesAPU;
import zdream.nsfplayer.xgm.player.PlayerConfig;

public class NsfPlayerConfig extends PlayerConfig {
	
	public static final int NES_CHANNEL_MAX = 29;

	/** 设备名称前缀 */
	public static final String[] dname = {
		"APU1", "APU2", "5B", "MMC5", "N163", "VRC6", "VRC7", "FDS"
	};
	
	public static final String[] channel_name = {
		"SQR0", "SQR1", "TRI", "NOISE", "DMC",
		"FDS",
		"MMC5:S0", "MMC5:S1", "MMC5:PCM",
		"5B:0", "5B:1", "5B:2",
		"VRC6:S0", "VRC6:S1", "VRC6:SAW",
		"VRC7:0", "VRC7:1", "VRC7:2", "VRC7:3", "VRC7:4", "VRC7:5",
		"N163:0", "N163:1", "N163:2", "N163:3", "N163:4", "N163:5", "N163:6", "N163:7"
	};
	
	public static final int
			APU = 0,
			DMC = 1,
			FME7 = 2,
			MMC5 = 3,
			N106 = 4,
			VRC6 = 5,
			VRC7 = 6,
			FDS = 7,
			NES_DEVICE_MAX = 8;
	
	public static final int[] channel_device = {
		APU, APU, DMC, DMC, DMC,
		FDS,
		MMC5, MMC5, MMC5,
		FME7, FME7, FME7,
		VRC6, VRC6, VRC6,
		VRC7, VRC7, VRC7, VRC7, VRC7, VRC7,
		N106, N106, N106, N106, N106, N106, N106, N106
	};
	
	public static final int[] channel_device_index = {
			0, 1, 0, 1, 2,
			0,
			0, 1, 2,
			0, 1, 2,
			0, 1, 2,
			0, 1, 2, 3, 4, 5,
			0, 1, 2, 3, 4, 5, 6, 7
	};
	
	public static final int[] channel_track = {
			NsfPlayer.APU1_TRK0, NsfPlayer.APU1_TRK1, NsfPlayer.APU2_TRK0,
			NsfPlayer.APU2_TRK1, NsfPlayer.APU2_TRK2,
			NsfPlayer.FDS_TRK0,
			NsfPlayer.MMC5_TRK0, NsfPlayer.MMC5_TRK1, NsfPlayer.MMC5_TRK2,
			NsfPlayer.FME7_TRK0, NsfPlayer.FME7_TRK1, NsfPlayer.FME7_TRK2,
			NsfPlayer.VRC6_TRK0, NsfPlayer.VRC6_TRK1, NsfPlayer.VRC6_TRK2,
			NsfPlayer.VRC7_TRK0, NsfPlayer.VRC7_TRK1, NsfPlayer.VRC7_TRK2,
			NsfPlayer.VRC7_TRK3, NsfPlayer.VRC7_TRK4, NsfPlayer.VRC7_TRK5,
			NsfPlayer.N106_TRK0, NsfPlayer.N106_TRK1, NsfPlayer.N106_TRK2, NsfPlayer.N106_TRK3,
			NsfPlayer.N106_TRK4, NsfPlayer.N106_TRK5, NsfPlayer.N106_TRK6, NsfPlayer.N106_TRK7
	};
	
	public static final String[] DEFAULT_CHANNEL_COL = {
			"FF0000", "FF0000", //APU1
			"00FF00", "00FF00", "000000", //APU2
			"0080FF", //FDS
			"FFC000", "FFC000", "000000", //MMC5
			"0000FF", "0000FF", "0000FF", // 0000FF, 000000, //FME7
			"FF8000", "FF8000", "FF8000", //VRC6
			"8000FF", "8000FF", "8000FF", "8000FF", "8000FF", "8000FF",//VRC7
			"FF0080", "FF0080", "FF0080", "FF0080", "FF0080", "FF0080", "FF0080", "FF0080"  //N106
	};
	public static final String DEFAULT_5B_ENVELOPE_COL = "0000FF";
	public static final String DEFAULT_5B_NOISE_COL = "000000";
	
	public NsfPlayerConfig() {
		int i, j;

		createValue("RATE", 48000);
		createValue("NCH", 2);
		createValue("BPS", 16);
		createValue("MASK", 0);
		createValue("PLAY_TIME", 60 * 5 * 1000);
		createValue("FADE_TIME", 5 * 1000);
		createValue("STOP_SEC", 3);
		createValue("LOOP_NUM", 2);
		createValue("AUTO_STOP", 1);
		createValue("AUTO_DETECT", 1);
		createValue("DETECT_TIME", 30 * 1000);
		createValue("DETECT_INT", 5000);
		createValue("LPF", 112);
		createValue("HPF", 164);
		createValue("TITLE_FORMAT", "%L (%n/%e) %T - %A");
		createValue("DETECT_ALT", 0);
		createValue("VSYNC_ADJUST", 0);
		createValue("MULT_SPEED", 256); // clock speed multiplier
		createValue("VRC7_PATCH", 0); // VRC7 patch set
		createValue("NSFE_PLAYLIST", 1); // use NSFe playlist

		createValue("COMP_LIMIT", 100);
		createValue("COMP_THRESHOLD", 100);
		createValue("COMP_VELOCITY", 100);

		createValue("NTSC_BASECYCLES", 1789773);
		createValue("PAL_BASECYCLES", 1662607);
		createValue("DENDY_BASECYCLES", 1773448);
		createValue("REGION", 0);
		createValue("LOG_CPU", 0);
		createValue("LOG_CPU_FILE", "nsf_write.log");

		createValue("MASTER_VOLUME", 128);

		StringBuilder b = new StringBuilder(32);
		for (i = 0; i < NES_CHANNEL_MAX; ++i) {
			b.delete(0, b.length()).append("CHANNEL_");
			if (i < 10)
				b.append('0');
			b.append(i).append("_PAN");
			createValue(b.toString(), 128);

			b.delete(0, b.length()).append("CHANNEL_");
			if (i < 10)
				b.append('0');
			b.append(i).append("_VOL");
			createValue(b.toString(), 128);

			b.delete(0, b.length()).append("CHANNEL_");
			if (i < 10)
				b.append('0');
			b.append(i).append("_COL");
			createValue(b.toString(), DEFAULT_CHANNEL_COL[i]);
		}
		createValue("5B_ENVELOPE_COL", DEFAULT_5B_ENVELOPE_COL);
		createValue("5B_NOISE_COL", DEFAULT_5B_NOISE_COL);

		for (i = 0; i < NES_DEVICE_MAX; i++) {
			String str = dname[i];
			createValue(str + "_VOLUME", 128);
			createValue(str + "_QUALITY", 3);
			createValue(str + "_FILTER", 0);
			createValue(str + "_MUTE", 0);
			createValue(str + "_THRESHOLD", 100);

			final int[] DEVICE_OPTION_MAX = {
				NesAPU.OPT_END,
				NesAPU.OPT_END,
				0, // 5B
				NesAPU.OPT_END,
				NesAPU.OPT_END,
				0, // VRC6
				0, // VRC7
				NesAPU.OPT_END,
			};

			final int[][] DEFAULT_DEVICE_OPTION = {
				{ 1, 1, 1, 0, 0, 0, 0 },
				{ 1, 1, 1, 0, 1, 1, 1 },
				{ 0, 0, 0, 0, 0, 0, 0 },
				{ 1, 1, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0, 0 },
				{ 2000, 0, 0, 0, 0, 0, 0 }
			};

			for (j = 0; j < DEVICE_OPTION_MAX[i]; j++) {
				final String itoa = "0123456789ABCDEF";
				createValue(dname[i] + "_OPTION" + itoa.charAt(j), DEFAULT_DEVICE_OPTION[i][j]);
			}
		}
	}
	
	/**
	 * Load all
	 */
	public boolean load(String file, String sect) {
		for (Iterator<String> it = data.keySet().iterator(); it.hasNext(); ) {
			String name = it.next();
			load(file, sect, name);
		}
		return true;
	}
	
	/**
	 * Load one
	 */
	public boolean load(String file, String sect, String name) {
		// TODO 这里原本是读取配置文件的数据的, 现在直接设值
		data.put(name, new Value(255));
		return true;
	}
	
	public boolean save(String file, String sect) {
		// TODO 这里原本是写配置文件的, 这里什么都没做
		return true;
	}
	
	@Override
	public boolean save(String file, String sect, String name) {
		// TODO 这里原本是写配置文件的, 这里什么都没做
		return true;
	}
	
	public boolean loadProperties(Properties pro) {
		for (Iterator<Entry<Object, Object>> it = pro.entrySet().iterator(); it.hasNext();) {
			Entry<Object, Object> e = it.next();
			data.put(e.getKey().toString(), new Value(e.getValue().toString()));
		}
		
		return true;
	}

	public synchronized Value getDeviceConfig(int i, String key) {
		return data.get(dname[i] + "_" + key);
	}

	public synchronized Value getDeviceOption(int id, int opt) {
		final String itoa = "0123456789ABCDEF";
		return data.get(dname[id] + "_OPTION" + itoa.charAt(opt));
	}

	/**
	 * channel mix/pan config
	 */
	public synchronized Value getChannelConfig(int id, String key) {
		if (id < 0)
			id = 0;
		if (id >= NES_CHANNEL_MAX)
			id = NES_CHANNEL_MAX - 1;

		StringBuilder b = new StringBuilder(16);
		b.append("CHANNEL_");
		if (id < 10)
			b.append('0');
		b.append(id).append('_').append(key);
		return data.get(b.toString());
	}
    
}
