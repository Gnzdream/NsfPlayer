package zdream.nsfplayer.ftm.renderer.channel;

/**
 * 2A03 中的声音轨道, 包含 Pulse 和 Triangle
 * @author Zdream
 * @since 0.2.1
 */
public abstract class Channel2A03Tone extends Channel2A03 {

	public Channel2A03Tone(byte channelCode) {
		super(channelCode);
	}
	
	/* **********
	 *  sweep   *
	 ********** */
	
	/**
	 * 范围 [0, 0xFF]
	 */
	protected int sweep;

}
