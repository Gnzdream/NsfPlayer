package zdream.nsfplayer.ftm.process.agreement;

import zdream.nsfplayer.ftm.process.base.FtmPosition;

/**
 * <p>单向等待同步协议的内容数据
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public class WaitingAgreementEntry extends AbstractAgreementEntry {

	public static WaitingAgreementEntry create(WaitingAgreement ref) {
		return new WaitingAgreementEntry(ref);
	}
	
	private WaitingAgreementEntry(WaitingAgreement ref) {
		super(ref);
		this.waitExeId = ref.waitExeId;
		this.waitPos = ref.waitPos;
		this.dependExeId = ref.dependExeId;
		this.dependPos = ref.dependPos;
	}

	public final int waitExeId;
	public final FtmPosition waitPos;
	public final int dependExeId;
	public final FtmPosition dependPos;

}
