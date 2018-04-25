package com.zdream.famitracker.document;

import static com.zdream.famitracker.FamitrackerTypes.*;

import com.zdream.famitracker.FamitrackerTypes;

/**
 * <p>轨道 Note 的类, 存放了每行的数据<br>
 * Channel note struct / class, holds the data for each row in patterns.
 * <p>Famitracker: struct stChanNote, at PatternData.h
 * @author Zdream
 */
public class StChanNote {
	
	/**
	 * 该参数在 {@link FamitrackerTypes} 中定义, 数据是 {@link FamitrackerTypes#NOTE_NONE} 这类
	 */
	public int note;
	
	/**
	 * 八度音阶
	 */
	public int octave;
	
	public int vol;
	
	public int instrument;
	
	public final int[] effNumber = new int[MAX_EFFECT_COLUMNS];
	
	public final int[] effParam = new int[MAX_EFFECT_COLUMNS];
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder(30);
		String hex = "0123456789ABCDEF";
		
		switch (note) {
		case 0: b.append(".  "); break;
		case 1: b.append("C-"); break;
		case 2: b.append("C#"); break;
		case 3: b.append("D-"); break;
		case 4: b.append("D#"); break;
		case 5: b.append("E-"); break;
		case 6: b.append("F-"); break;
		case 7: b.append("F#"); break;
		case 8: b.append("G-"); break;
		case 9: b.append("G#"); break;
		case 10: b.append("A-"); break;
		case 11: b.append("A#"); break;
		case 12: b.append("B-"); break;
		case NOTE_HALT: b.append("---"); break;
		default: b.append("?."); break;
		}
		
		switch (note) {
		case 0:
		case NOTE_HALT:
			break;

		default:
			b.append(octave);
			break;
		}
		
		b.append(' ');
		if (instrument == MAX_INSTRUMENTS) {
			b.append(". ");
		} else {
			b.append(hex.charAt(instrument / 16));
			b.append(hex.charAt(instrument % 16));
		}
		b.append(' ');
		
		if (vol == MAX_VOLUME) {
			b.append('.');
		} else {
			b.append(hex.charAt(vol));
		}
		
		for (int i = 0; i < MAX_EFFECT_COLUMNS; i++) {
			if (effNumber[i] != 0) {
				b.append(' ').append(EFF_CHAR[effNumber[i] - 1]).append('-');
				String paramStr = Integer.toHexString(effParam[i] & 0xFF);
				if (paramStr.length() == 1) {
					b.append('0');
				}
				b.append(paramStr);
			}
		}
		
		return b.toString();
	}
	
	public void copyFrom(StChanNote src) {
		this.note = src.note;
		this.octave = src.octave;
		this.vol = src.vol;
		this.instrument = src.instrument;
		
		System.arraycopy(src.effNumber, 0, effNumber, 0, effNumber.length);
		System.arraycopy(src.effParam, 0, effParam, 0, effParam.length);
	}

}
