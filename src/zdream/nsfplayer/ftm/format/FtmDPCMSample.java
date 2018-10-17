package zdream.nsfplayer.ftm.format;

/**
 * DPCM 采样
 * @author Zdream
 * @date 2018-04-26
 */
public class FtmDPCMSample {
	
	public String name;
	
	public byte[] data;
	
	/**
	 * @param address
	 *   不能为负数
	 * @return
	 * @since v0.2.2
	 */
	public byte read(int address) {
		if (data == null)
			return 0;
		if (address >= data.length)
			return 0;
		return data[address];
	}
	
	@Override
	public String toString() {
		if (name != null) {
			return "DPCMSample:" + name;
		}
		return "Empty DPCMSample";
	}
	
}
