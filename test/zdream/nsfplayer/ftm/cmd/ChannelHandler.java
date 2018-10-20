package zdream.nsfplayer.ftm.cmd;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.ftm.FtmPlayerConsole;
import zdream.nsfplayer.ftm.renderer.FamiTrackerRenderer;

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
		FamiTrackerRenderer renderer = env.getRenderer();
		Set<Byte> bs = renderer.allChannelSet();
		
		bs.forEach(code -> renderer.setChannelEnable(code, true));
	}
	
	/**
	 * 打开单个轨道
	 */
	private void muteClear(FtmPlayerConsole env, byte channelCode) {
		FamiTrackerRenderer renderer = env.getRenderer();
		renderer.setChannelEnable(channelCode, true);
	}
	
	/**
	 * 关闭单个轨道
	 */
	private void mute(FtmPlayerConsole env, byte channelCode) {
		FamiTrackerRenderer renderer = env.getRenderer();
		renderer.setChannelEnable(channelCode, false);
	}

	/**
	 * 查看所有轨道的音量
	 */
	private void volumePrintAll(FtmPlayerConsole env) {
		env.printOut("[VOLUME] 下面罗列所有轨道的音量\n     ---------- ---- ---");
		
		FamiTrackerRenderer renderer = env.getRenderer();
		ArrayList<Byte> bs = new ArrayList<>(renderer.allChannelSet());
		bs.sort(null);
		
		for (Iterator<Byte> it = bs.iterator(); it.hasNext();) {
			byte channelCode = it.next();
			float vol = renderer.getLevel(channelCode);
			if (!renderer.isChannelEnable(channelCode)) {
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
		FamiTrackerRenderer renderer = env.getRenderer();
		Set<Byte> bs = renderer.allChannelSet();
		
		bs.forEach(code -> {
			renderer.setChannelEnable(code, true);
			renderer.setLevel(code, 1.0f);
		});
	}

	/**
	 * 设置全部轨道音量
	 */
	private void volumeSetAll(FtmPlayerConsole env, float vol) {
		FamiTrackerRenderer renderer = env.getRenderer();
		Set<Byte> bs = renderer.allChannelSet();
		
		bs.forEach(code -> {
			renderer.setChannelEnable(code, true);
			renderer.setLevel(code, vol);
		});
	}

	/**
	 * 查看单个轨道的音量
	 */
	private void volumePrint(FtmPlayerConsole env, byte channelCode) {
		FamiTrackerRenderer renderer = env.getRenderer();
		float vol = renderer.getLevel(channelCode);
		if (!renderer.isChannelEnable(channelCode)) {
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
		FamiTrackerRenderer renderer = env.getRenderer();
		
		renderer.setChannelEnable(channelCode, true);
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
		}
		
		return "";
	}
	
}
