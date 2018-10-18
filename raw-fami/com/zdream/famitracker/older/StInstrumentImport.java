package com.zdream.famitracker.older;

import com.zdream.famitracker.FamitrackerTypes;

public class StInstrumentImport {
	String name;
	boolean free;
	int[] modEnable = new int[FamitrackerTypes.SEQ_COUNT];
	int[] modIndex = new int[FamitrackerTypes.SEQ_COUNT];
	/**
	 * For DPCM
	 */
	int assignedSample;
}
