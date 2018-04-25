package com.zdream.famitracker.document;

/**
 * <p>这个接口用来为自定义的数据输出来提供方法的.
 * <p>This class is a pure virtual interface to Sequence, which can be used by custom exporters
 * @author Zdream
 */
public interface ISequence {
	
	int getItem(int index);
	
	int	getItemCount();
	
	int getLoopPoint();

}
