package zdream.nsfplayer.ftm.cmd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import zdream.nsfplayer.core.AbstractNsfRenderer;
import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.FtmPlayerConsole;

/**
 * <p>轨道相关命令处理器
 * 
 * <p>
 * mute 命令:
 * <li><code>mute [channel]</code>
 * <br>关闭某个轨道. channel 可以是轨道号 (16 进制) 或者名称
 * <li><code>mute -c [channel]</code>
 * <br>打开某个轨道. channel 可以是轨道号 (16 进制) 或者名称
 * <li><code>mute -c</code>
 * <br>打开全部轨道.
 * </li>
 * <p>
 * volume 命令:
 * <li><code>volume</code>
 * <br>查看所有轨道的音量.
 * <li><code>volume [channel]</code>
 * <br>查看某个轨道的音量. channel 可以是轨道号 (16 进制) 或者名称
 * <li><code>volume [channel] [level]</code>
 * <br>设置某个轨道的音量. channel 可以是轨道号 (16 进制) 或者名称
 * <br>level 是音量, 范围在 [0, 1]
 * <li><code>volume -reset</code>
 * <br>全部轨道音量重置为 1.
 * <li><code>volume -set [level]</code>
 * <br>全部轨道音量设置为 level.
 * <br>level 是音量, 范围在 [0, 1]
 * </li>
 * </p>
 * 
 * @author Zdream
 * @since v0.2.3-test
 */
public class ChannelHandler implements ICommandHandler, INsfChannelCode {
	
	public static final String
			CMD_MUTE = "mute",
			CMD_VOLUME = "volume";

	public ChannelHandler() {
		
	}

	@Override
	public String[] canHandle() {
		return new String[] {CMD_MUTE, CMD_VOLUME};
	}

	@Override
	public void handle(String[] args, FtmPlayerConsole env) {
		String cmd = args[0];
		if (CMD_MUTE.equals(cmd)) {
			handleMute(args, env);
		} else if (CMD_VOLUME.equals(cmd)) {
			handleVolume(args, env);
		}
	}

	private void handleMute(String[] args, FtmPlayerConsole env) {
		if ("-c".equals(args[1])) {
			if (args.length == 2) {
				muteClearAll(env);
			} else {
				byte channelCode = parseChannelCode(args[2].toLowerCase());
				muteClear(env, channelCode);
			}
		} else {
			byte channelCode = parseChannelCode(args[1].toLowerCase());
			mute(env, channelCode);
		}
	}

	private void handleVolume(String[] args, FtmPlayerConsole env) {
		if (args.length == 1) {
			volumePrintAll(env);
		} else if ("-reset".equals(args[1])) {
			volumeResetAll(env);
		} else if ("-set".equals(args[1])) {
			float vol = Float.parseFloat(args[2]);
			volumeSetAll(env, vol);
		} else {
			if (args.length == 2) {
				byte channelCode = parseChannelCode(args[1].toLowerCase());
				volumePrint(env, channelCode);
			} else {
				byte channelCode = parseChannelCode(args[1].toLowerCase());
				float vol = Float.parseFloat(args[2]);
				volumeSet(env, channelCode, vol);
			}
		}
	}

	/**
	 * 打开全部轨道
	 */
	private void muteClearAll(FtmPlayerConsole env) {
		AbstractNsfRenderer<?> renderer = env.getRenderer();
		Set<Byte> bs = renderer.allChannelSet();
		
		bs.forEach(code -> renderer.setChannelMuted(code, false));
	}
	
	/**
	 * 打开单个轨道
	 */
	private void muteClear(FtmPlayerConsole env, byte channelCode) {
		AbstractNsfRenderer<?> renderer = env.getRenderer();
		renderer.setChannelMuted(channelCode, false);
	}
	
	/**
	 * 关闭单个轨道
	 */
	private void mute(FtmPlayerConsole env, byte channelCode) {
		AbstractNsfRenderer<?> renderer = env.getRenderer();
		renderer.setChannelMuted(channelCode, true);
	}

	/**
	 * 查看所有轨道的音量
	 */
	private void volumePrintAll(FtmPlayerConsole env) {
		env.printOut("[VOLUME] 下面罗列所有轨道的音量\n     ---------- ---- ---");

		AbstractNsfRenderer<?> renderer = env.getRenderer();
		ArrayList<Byte> bs = new ArrayList<>(renderer.allChannelSet());
		bs.sort(null);
		
		for (Iterator<Byte> it = bs.iterator(); it.hasNext();) {
			byte channelCode = it.next();
			float vol = renderer.getLevel(channelCode);
			if (renderer.isChannelMuted(channelCode)) {
				vol = 0;
			}
			
			env.printOut("%15s [%2s] %.1f",
					toStringChannelCode(channelCode),
					Integer.toHexString(channelCode),
					vol);
			
		}
		env.printOut("     ---------- ---- ---\n使用 volume [channel] [level] 设置单个轨道的音量");
	}

	/**
	 * 重置全部轨道音量
	 */
	private void volumeResetAll(FtmPlayerConsole env) {
		AbstractNsfRenderer<?> renderer = env.getRenderer();
		Set<Byte> bs = renderer.allChannelSet();
		
		bs.forEach(code -> {
			renderer.setChannelMuted(code, false);
			renderer.setLevel(code, 1.0f);
		});
	}

	/**
	 * 设置全部轨道音量
	 */
	private void volumeSetAll(FtmPlayerConsole env, float vol) {
		AbstractNsfRenderer<?> renderer = env.getRenderer();
		Set<Byte> bs = renderer.allChannelSet();
		
		bs.forEach(code -> {
			renderer.setChannelMuted(code, false);
			renderer.setLevel(code, vol);
		});
	}

	/**
	 * 查看单个轨道的音量
	 */
	private void volumePrint(FtmPlayerConsole env, byte channelCode) {
		AbstractNsfRenderer<?> renderer = env.getRenderer();
		float vol = renderer.getLevel(channelCode);
		if (renderer.isChannelMuted(channelCode)) {
			vol = 0;
		}
		
		env.printOut("[VOLUME] 轨道 %s [%2s] 的音量为 %.1f\n使用 volume [channel] [level] 设置单个轨道的音量",
				toStringChannelCode(channelCode),
				Integer.toHexString(channelCode),
				vol);
	}

	/**
	 * 设置单个轨道音量
	 */
	private void volumeSet(FtmPlayerConsole env, byte channelCode, float vol) {
		AbstractNsfRenderer<?> renderer = env.getRenderer();
		
		renderer.setChannelMuted(channelCode, false);
		renderer.setLevel(channelCode, vol);
	}
	
	private byte parseChannelCode(String c) {
		switch (c) {
		// 2A03
		case "pulse1": case "p1": case "1": case "01":
			return CHANNEL_2A03_PULSE1;
		case "pulse2": case "p2": case "2": case "02":
			return CHANNEL_2A03_PULSE2;
		case "triangle": case "tri": case "3": case "03":
			return CHANNEL_2A03_TRIANGLE;
		case "noise": case "n": case "4": case "04":
			return CHANNEL_2A03_NOISE;
		case "dpcm": case "d": case "5": case "05":
			return CHANNEL_2A03_DPCM;
			
		// VRC6
		case "vrc6pulse1": case "vrc6p1": case "11":
			return CHANNEL_VRC6_PULSE1;
		case "vrc6pulse2": case "vrc6p2": case "12":
			return CHANNEL_VRC6_PULSE2;
		case "vrc6sawtooth": case "vrc6s": case "13":
			return CHANNEL_VRC6_SAWTOOTH;
			
		// MMC5
		case "mmc5pulse1": case "mmc5p1": case "41":
			return CHANNEL_MMC5_PULSE1;
		case "mmc5pulse2": case "mmc5p2": case "42":
			return CHANNEL_MMC5_PULSE2;
			
		// FDS
		case "fds": case "31":
			return CHANNEL_FDS;
			
		// N163
		case "namco1": case "n163_1": case "51":
			return CHANNEL_N163_1;
		case "namco2": case "n163_2": case "52":
			return CHANNEL_N163_2;
		case "namco3": case "n163_3": case "53":
			return CHANNEL_N163_3;
		case "namco4": case "n163_4": case "54":
			return CHANNEL_N163_4;
		case "namco5": case "n163_5": case "55":
			return CHANNEL_N163_5;
		case "namco6": case "n163_6": case "56":
			return CHANNEL_N163_6;
		case "namco7": case "n163_7": case "57":
			return CHANNEL_N163_7;
		case "namco8": case "n163_8": case "58":
			return CHANNEL_N163_8;
			
		// VRC7
		case "fmchannel1": case "fm1": case "vrc7_1": case "21":
			return CHANNEL_VRC7_FM1;
		case "fmchannel2": case "fm2": case "vrc7_2": case "22":
			return CHANNEL_VRC7_FM2;
		case "fmchannel3": case "fm3": case "vrc7_3": case "23":
			return CHANNEL_VRC7_FM3;
		case "fmchannel4": case "fm4": case "vrc7_4": case "24":
			return CHANNEL_VRC7_FM4;
		case "fmchannel5": case "fm5": case "vrc7_5": case "25":
			return CHANNEL_VRC7_FM5;
		case "fmchannel6": case "fm6": case "vrc7_6": case "26":
			return CHANNEL_VRC7_FM6;
			
		// S5B
		case "s5bsquare1": case "s5b1": case "61":
			return CHANNEL_S5B_SQUARE1;
		case "s5bsquare2": case "s5b2": case "62":
			return CHANNEL_S5B_SQUARE2;
		case "s5bsquare3": case "s5b3": case "63":
			return CHANNEL_S5B_SQUARE3;
		}
		
		
		throw new IllegalArgumentException("无法解析轨道号: " + c);
	}
	
	private String toStringChannelCode(byte channelCode) {
		switch (channelCode) {
		// 2A03
		case CHANNEL_2A03_PULSE1: return "Pulse1";
		case CHANNEL_2A03_PULSE2: return "Pulse2";
		case CHANNEL_2A03_TRIANGLE: return "Triangle";
		case CHANNEL_2A03_NOISE: return "Noise";
		case CHANNEL_2A03_DPCM: return "DPCM";
			
		// VRC6
		case CHANNEL_VRC6_PULSE1: return "VRC6Pulse1";
		case CHANNEL_VRC6_PULSE2: return "VRC6Pulse2";
		case CHANNEL_VRC6_SAWTOOTH: return "VRC6Sawtooth";
			
		// MMC5
		case CHANNEL_MMC5_PULSE1: return "MMC5Pulse1";
		case CHANNEL_MMC5_PULSE2: return "MMC5Pulse2";
		
		// FDS
		case CHANNEL_FDS: return "FDS";
		
		// N163
		case CHANNEL_N163_1: return "Namco1";
		case CHANNEL_N163_2: return "Namco2";
		case CHANNEL_N163_3: return "Namco3";
		case CHANNEL_N163_4: return "Namco4";
		case CHANNEL_N163_5: return "Namco5";
		case CHANNEL_N163_6: return "Namco6";
		case CHANNEL_N163_7: return "Namco7";
		case CHANNEL_N163_8: return "Namco8";
		
		// VRC7
		case CHANNEL_VRC7_FM1: return "FMChannel1";
		case CHANNEL_VRC7_FM2: return "FMChannel2";
		case CHANNEL_VRC7_FM3: return "FMChannel3";
		case CHANNEL_VRC7_FM4: return "FMChannel4";
		case CHANNEL_VRC7_FM5: return "FMChannel5";
		case CHANNEL_VRC7_FM6: return "FMChannel6";
		
		// S5B
		case CHANNEL_S5B_SQUARE1: return "S5BSquare1";
		case CHANNEL_S5B_SQUARE2: return "S5BSquare2";
		case CHANNEL_S5B_SQUARE3: return "S5BSquare3";
		}
		
		return "";
	}
	
}
