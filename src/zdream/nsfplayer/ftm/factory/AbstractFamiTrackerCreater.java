package zdream.nsfplayer.ftm.factory;

import zdream.nsfplayer.ftm.audio.FamiTrackerHandler;
import zdream.nsfplayer.ftm.format.FtmChipType;
import zdream.nsfplayer.ftm.format.FtmSequence;
import zdream.nsfplayer.ftm.format.FtmSequenceType;

public abstract class AbstractFamiTrackerCreater {

	/**
	 * 创建 2A03 的序列
	 */
	protected FtmSequence createSequence(FamiTrackerHandler doc, int index, byte type) {
		// 将序列注册到 Ftm 中
		return doc.getOrCreateSequence(FtmChipType._2A03, FtmSequenceType.get(type), index);
	}
	
	/**
	 * 创建 2A03 的序列
	 */
	protected FtmSequence createSequence(FamiTrackerHandler doc, int index, FtmSequenceType type) {
		// 将序列注册到 Ftm 中
		return doc.getOrCreateSequence(FtmChipType._2A03, type, index);
	}
	
	/**
	 * 创建 VRC6 的序列
	 */
	protected FtmSequence createSeqVRC6(FamiTrackerHandler doc, int index, byte type) {
		// 将序列注册到 Ftm 中
		return doc.getOrCreateSequence(FtmChipType.VRC6, FtmSequenceType.get(type), index);
	}
	
	/**
	 * 创建 VRC6 的序列
	 */
	protected FtmSequence createSeqVRC6(FamiTrackerHandler doc, int index, FtmSequenceType type) {
		// 将序列注册到 Ftm 中
		return doc.getOrCreateSequence(FtmChipType.VRC6, type, index);
	}

}
