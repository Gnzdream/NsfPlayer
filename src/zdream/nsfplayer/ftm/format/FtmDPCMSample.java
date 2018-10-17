package zdream.nsfplayer.ftm.format;

/**
 * DPCM 采样
 * @author Zdream
 * @date 2018-04-26
 */
public class FtmDPCMSample {
	
	public String name;
	
	public byte[] data;
	
	@Override
	public String toString() {
		if (name != null) {
			return "DPCMSample:" + name;
		}
		return "Empty DPCMSample";
	}
	
}
