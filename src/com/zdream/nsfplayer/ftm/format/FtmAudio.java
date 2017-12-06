package com.zdream.nsfplayer.ftm.format;

import com.zdream.nsfplayer.xgm.player.SoundDataMSP;

public class FtmAudio extends SoundDataMSP implements IInstParam {
	
	/**
	 * FamiTracker 导出文本的版本号, 常见的有 0.4.2 0.4.3 ... 0.4.6
	 */
	String version;
	
	/**
	 * 标题 TITLE
	 */
	String title;
	
	/**
	 * 作家 / 创作者 AUTHOR
	 */
	String author;
	
	/**
	 * 版权 COPYRIGHT
	 */
	String copyright;
	
	/**
	 * 评论, 列表形式
	 */
	String[] comments;
	
	/**
	 * 制式
	 */
	byte machine;
	public static final byte MACHINE_NTSC = 0;
	public static final byte MACHINE_PAL = 1;
	
	/**
	 * fps, 有效值为 [0, 800].<br>
	 * 如果设置为 0, 则为制式指定默认的数值
	 */
	int framerate;
	
	/**
	 * 各种芯片的使用情况
	 */
	boolean useVcr6, useVcr7, useFds, useMmc5, useN163, useS5b;
	
	/**
	 * 模式 mode<br>
	 * 0 for old style vibrato, 1 for new style (这是 Famitracker 文档的原话)
	 */
	byte vibrato;
	
	/**
	 * 原话是:
	 * split point where Fxx effect sets tempo instead of speed<br>
	 * 'Fxx' 这个函数影响多少, 其实我并不知道.
	 */
	int split;
	
	@Override
	public String getTitle() {
		return title;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public String getCopyright() {
		return copyright;
	}
	
	public String[] getComments() {
		return comments;
	}
	
	/**
	 * 制式, 包括 {@link #MACHINE_NTSC} 和 {@link #MACHINE_PAL}
	 * @return
	 */
	public byte getMachine() {
		return machine;
	}
	
	/**
	 * fps
	 * @return
	 */
	public int getFramerate() {
		if (framerate == 0) {
			// TODO return
		}
		return framerate;
	}
	
	/**
	 * 是否是该制式下默认的 fps
	 * @return
	 */
	public boolean isDefaultFramerate() {
		return framerate == 0;
	}

	public boolean isUseVcr6() {
		return useVcr6;
	}

	public boolean isUseVcr7() {
		return useVcr7;
	}

	public boolean isUseFds() {
		return useFds;
	}

	public boolean isUseMmc5() {
		return useMmc5;
	}

	public boolean isUseN163() {
		return useN163;
	}

	public boolean isUseS5b() {
		return useS5b;
	}
	
	/**
	 * @return
	 * {@link #vibrato}
	 */
	public byte getVibrato() {
		return vibrato;
	}
	
	/**
	 * @return
	 * {@link #split}
	 */
	public int getSplit() {
		return split;
	}

	@Override
	public int getSong() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setSong(int song) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getSongNum() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(100);
		
		builder.append("FTM 文本").append('\n');
		builder.append("标题").append(':').append(' ').append(title).append('\n');
		builder.append("作家").append(':').append(' ').append(author).append('\n');
		builder.append("版权").append(':').append(' ').append(copyright).append('\n');
		
		return builder.toString();
	}
	
	/* **********
	 * 乐器配置 *
	 ********** */
	/*
	 * Macros
	 */
	public static final int MACRO_2A03 = 0;
	
	/**
	 * 乐器接口. 这部分的类因为访问频繁, 不再设置 get set 等封装方法
	 * @author Zdream
	 */
	interface IMacros {
		public int macrosType();
	}
	
	/**
	 * 关于音量的配置
	 * @author Zdream
	 */
	public final class Macro2A03 implements IMacros {
		@Override
		public int macrosType() {
			return MACRO_2A03;
		}
		/**
		 * 类型, 0=volume, 1=arpeggio, 2=pitch, 3=hi-pitch, 4=duty 
		 */
		public int type;
		public static final int MACRO_TYPE_VOLUME = 0;
		public static final int MACRO_TYPE_ARPEGGIO = 1;
		public static final int MACRO_TYPE_PITCH = 2;
		public static final int MACRO_TYPE_HI_PITCH = 3;
		public static final int MACRO_TYPE_DUTY = 4;
		/**
		 * 编号
		 */
		public int seq;
		/**
		 * 循环开始的位置. -1 的话说明禁用循环
		 */
		public int loop;
		/**
		 * 释放开始的位置. -1 的话说明禁用释放
		 */
		public int release;
		/**
		 * 设置. 这个值我还没有见到除了 0 以外的数.
		 */
		public int setting;
		
		/**
		 * 序列
		 */
		public int[] macro;
	}
	IMacros[] macros;
	
	public IMacros[] getMacros() {
		return macros;
	}
	
	/* **********
	 *   乐器   *
	 ********** */
	/*
	 * 乐器部分 Instruments
	 */
	
	Inst2A03[] inst2a03s;
	
	/* **********
	 *   乐曲   *
	 ********** */
	/*
	 * 乐曲部分 Tracks
	 */
	
	FtmTrack[] tracks;

}
