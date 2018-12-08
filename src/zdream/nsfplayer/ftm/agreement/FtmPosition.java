package zdream.nsfplayer.ftm.agreement;

import static zdream.nsfplayer.ftm.format.FtmStatic.*;

import zdream.nsfplayer.core.NsfPlayerException;
import zdream.nsfplayer.ftm.audio.FtmAudio;
import zdream.nsfplayer.ftm.factory.FamiTrackerFormatException;
import zdream.nsfplayer.ftm.format.FtmStatic;

/**
 * <p>用于存储 FamiTracker 执行位置数据结构.
 * <p>事实不可变实例
 * </p>
 * 
 * @author Zdream
 * @since v0.3.1
 */
public class FtmPosition implements Cloneable, Comparable<FtmPosition> {
	
	/**
	 * {@link FtmAudio} 里面指示的段号, 从 0 开始.
	 */
	public final int section;
	/**
	 * {@link FtmAudio} 里面指示的行号, 从 0 开始.
	 */
	public final int row;
	
	/**
	 * 产生一个 Ftm 位置实例, 在指定段的开头.
	 * @param section
	 *   段号, 有效范围为 [0, MAX_SECTIONS).
	 * @throws FamiTrackerFormatException
	 *   当段号不在有效范围内时
	 * 
	 */
	public FtmPosition(int section) throws NsfPlayerException {
		this(section, 0);
	}
	
	/**
	 * 产生一个 Ftm 位置实例.
	 * @param section
	 *   段号, 有效范围为 [0, MAX_SECTIONS).
	 * @param row
	 *   行号, 有效范围为 [0, MAX_PATTERN_LENGTH).
	 * @throws FamiTrackerFormatException
	 *   当行号、段号不在有效范围内时
	 * @see FtmStatic#MAX_SECTIONS
	 * @see FtmStatic#MAX_PATTERN_LENGTH
	 */
	public FtmPosition(int section, int row) {
		if (section < 0 || section >= MAX_SECTIONS) {
			throw new FamiTrackerFormatException(
					"段号: " + section + " 需要在有效范围 [0, " + MAX_SECTIONS + "] 内");
		}
		if (row < 0 || row >= MAX_PATTERN_LENGTH) {
			throw new FamiTrackerFormatException(
					"行号: " + row + " 需要在有效范围 [0, " + MAX_PATTERN_LENGTH + "] 内");
		}
		
		this.section = section;
		this.row = row;
	}
	
	/**
	 * 复制一个 Ftm 位置实例.
	 * @param o
	 *   复制源
	 */
	public FtmPosition(FtmPosition o) {
		this.section = o.section;
		this.row = o.row;
	}

	@Override
	public String toString() {
		return "FtmPosition [section=" + section + ", row=" + row + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 211;
		int result = 1;
		result = prime * result + section;
		result = prime * result + row;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FtmPosition other = (FtmPosition) obj;
		if (row != other.row)
			return false;
		if (section != other.section)
			return false;
		return true;
	}
	
	@Override
	public FtmPosition clone() {
		try {
			return (FtmPosition) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int compareTo(FtmPosition o) {
		if (this.section != o.section) {
			return this.section - o.section;
		} else {
			return this.row - o.row;
		}
	}

}
