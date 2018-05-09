package zdream.nsfplayer.nsf.renderer;

import zdream.nsfplayer.nsf.audio.NsfAudio;
import zdream.nsfplayer.nsf.device.memory.NesBank;
import zdream.nsfplayer.nsf.device.memory.NesMem;

/**
 * NSF 渲染器
 * @author Zdream
 * @date 2018-05-09
 * @since v0.1
 */
public class NsfRenderer {
	
	NsfAudio audio;
	NsfRendererConfig config;
	
	// 存储部件
	NesMem mem;
	NesBank bank;
	
	public NsfRenderer() {
		config = new NsfRendererConfig();
		
		mem = new NesMem();
	}
	
	/**
	 * 读取 Nsf 音频
	 * @param audio
	 * @throws NullPointerException
	 *   当 audio 为 null 时
	 */
	public void load(NsfAudio audio) {
		if (audio == null) {
			throw new NullPointerException("audio = null");
		}
		
		this.audio = audio;
		reload();
	}
	
	/**
	 * 重读 Nsf 音频
	 */
	public void reload() {
		reloadMemory();
		
		// TODO 这里参照 NsfPlayer 类
	}
	
	/**
	 * 用 audio 的数据内容, 重设、覆盖 memory 里面的数据内容
	 */
	private void reloadMemory() {
		int i, bmax = 0;

		for (i = 0; i < 8; i++)
			if (bmax < audio.bankswitch[i])
				bmax = audio.bankswitch[i];

		mem.setImage(audio.body, audio.load_address, audio.body.length);

		if (bmax != 0) {
			bank.setImage(audio.body, audio.load_address, audio.body.length);
			for (i = 0; i < 8; i++)
				bank.setBankDefault(i + 8, audio.bankswitch[i]);
		}
	}

	/**
	 * 渲染 Nsf 音频, 将音频采样的数据放入到 b 数组中
	 * 需要之前 load 过.
	 * @param b
	 *   需要填充的 byte 数组
	 * @param offset
	 * @param size
	 * @return
	 *   实际填充的 byte 数据量
	 */
	public int render(byte[] b, int offset, int size) {
		return 0;
	}

}
