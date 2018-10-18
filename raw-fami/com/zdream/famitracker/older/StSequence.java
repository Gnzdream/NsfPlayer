package com.zdream.famitracker.older;

import com.zdream.famitracker.FamitrackerTypes;

/**
 * <p>老版本的 Sequence
 * <p>Old sequence list, kept for compability
 * @author Zdream
 */
public class StSequence {
	public int count;
	public byte[] length = new byte[FamitrackerTypes.MAX_SEQUENCE_ITEMS];
	public byte[] value = new byte[FamitrackerTypes.MAX_SEQUENCE_ITEMS];
}
