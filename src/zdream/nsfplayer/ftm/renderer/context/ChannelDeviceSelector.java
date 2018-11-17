package zdream.nsfplayer.ftm.renderer.context;

import zdream.nsfplayer.core.INsfChannelCode;
import zdream.nsfplayer.core.IResetable;
import zdream.nsfplayer.ftm.renderer.AbstractFtmChannel;
import zdream.nsfplayer.ftm.renderer.TestFtmChannel;
import zdream.nsfplayer.ftm.renderer.channel.Channel2A03Pulse;
import zdream.nsfplayer.ftm.renderer.channel.ChannelFDS;
import zdream.nsfplayer.ftm.renderer.channel.ChannelMMC5Pulse;
import zdream.nsfplayer.ftm.renderer.channel.ChannelN163;
import zdream.nsfplayer.ftm.renderer.channel.ChannelVRC6Pulse;
import zdream.nsfplayer.ftm.renderer.channel.ChannelVRC6Sawtooth;
import zdream.nsfplayer.ftm.renderer.channel.ChannelVRC7;
import zdream.nsfplayer.ftm.renderer.channel.DPCMChannel;
import zdream.nsfplayer.ftm.renderer.channel.NoiseChannel;
import zdream.nsfplayer.ftm.renderer.channel.TriangleChannel;
import zdream.nsfplayer.sound.vrc7.OPLL;

/**
 * <p>多轨道环境存储器, 兼轨道设备的选择、生成工具
 * <p>原来名称为 <code>zdream.nsfplayer.ftm.renderer.channel.ChannalFactory</code>
 * 后来发现对于每个不同的轨道, 不仅是 Ftm 轨道, 还有发声器、音频轨道等都不相同,
 * 因此将其功能加强, 对每个不同的轨道选择各种设备和类.
 * <p>对于像 VRC7 轨道这样, 多个轨道间共用同一个环境数据集 (OPLL) 的,
 * 需要单独设立一个地方存储公有的环境数据. 于是就选择了这里
 * </p>
 * 
 * @version v0.2.7
 *   将该类从单纯的轨道设备的选择工具, 转变为环境的存储器.
 *   里面使用的方法也从静态修改为非静态的
 * 
 * @author Zdream
 * @since 0.2.1
 */
public class ChannelDeviceSelector implements INsfChannelCode, IResetable {

	/**
	 * 建立各个轨道
	 * @param code
	 *   轨道号
	 * @return
	 */
	public AbstractFtmChannel selectFtmChannel(byte code) {
		switch (code) {
		// 2A03
		case CHANNEL_2A03_PULSE1: {
			Channel2A03Pulse s = new Channel2A03Pulse(true);
			return s;
		}
		case CHANNEL_2A03_PULSE2: {
			Channel2A03Pulse s = new Channel2A03Pulse(false);
			return s;
		}
		case CHANNEL_2A03_TRIANGLE: {
			TriangleChannel s = new TriangleChannel();
			return s;
		}
		case CHANNEL_2A03_NOISE: {
			NoiseChannel s = new NoiseChannel();
			return s;
		}
		case CHANNEL_2A03_DPCM:
			DPCMChannel s = new DPCMChannel();
			return s;
			
		// VRC6
		case CHANNEL_VRC6_PULSE1: {
			return new ChannelVRC6Pulse(true);
		}
		case CHANNEL_VRC6_PULSE2: {
			return new ChannelVRC6Pulse(false);
		}
		case CHANNEL_VRC6_SAWTOOTH: {
			return new ChannelVRC6Sawtooth();
		}
			
		// MMC5
		case CHANNEL_MMC5_PULSE1: {
			return new ChannelMMC5Pulse(true);
		}
		case CHANNEL_MMC5_PULSE2: {
			return new ChannelMMC5Pulse(false);
		}
		
		// FDS
		case CHANNEL_FDS: {
			return new ChannelFDS();
		}
		
		// N163
		case CHANNEL_N163_1: {
			return new ChannelN163(0);
		}
		case CHANNEL_N163_2: {
			return new ChannelN163(1);
		}
		case CHANNEL_N163_3: {
			return new ChannelN163(2);
		}
		case CHANNEL_N163_4: {
			return new ChannelN163(3);
		}
		case CHANNEL_N163_5: {
			return new ChannelN163(4);
		}
		case CHANNEL_N163_6: {
			return new ChannelN163(5);
		}
		case CHANNEL_N163_7: {
			return new ChannelN163(6);
		}
		case CHANNEL_N163_8: {
			return new ChannelN163(7);
		}

		// VRC7
		case CHANNEL_VRC7_FM1: {
			return createVRC7Channel(0);
		}
		case CHANNEL_VRC7_FM2: {
			return createVRC7Channel(1);
		}
		case CHANNEL_VRC7_FM3: {
			return createVRC7Channel(2);
		}
		case CHANNEL_VRC7_FM4: {
			return createVRC7Channel(3);
		}
		case CHANNEL_VRC7_FM5: {
			return createVRC7Channel(4);
		}
		case CHANNEL_VRC7_FM6: {
			return createVRC7Channel(5);
		}

		default:
			break;
		}
		
		return new TestFtmChannel(code);
	}
	
	/* **********
	 * 公共方法 *
	 ********** */

	@Override
	public void reset() {
		opll = null;
		
	}
	
	/* **********
	 *   环境   *
	 ********** */
	
	OPLL opll;
	
	public OPLL getOpll() {
		return opll;
	}
	
	private ChannelVRC7 createVRC7Channel(int index) {
		if (opll == null) {
			opll = new OPLL();
		}
		
		return new ChannelVRC7(index, opll);
	}

}
