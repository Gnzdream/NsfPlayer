package com.zdream.nsfplayer.ctrl.cmd;

import com.zdream.nsfplayer.ctrl.INsfPlayerEnv;

public interface ICommandHandler {
	
	/**
	 * 表示这个类似控制台命令的处理器能够处理哪些操作.
	 * @return
	 *   请注意如果是英文字符串必须小写
	 */
	public String[] canHandle();
	
	/**
	 * 处理操作
	 * @param args
	 *   用户或者其他输入的参数, 用于解释该操作的目的.<br>
	 *   类似于 <code>main()</code> 方法的输入参数.<br>
	 *   <code>args[0]</code> 一定存在,
	 *   它必定等同于 <code>canHandle()</code> 方法返回的数组的一个元素, 必定全小写.
	 * @param env
	 *   环境
	 */
	public void handle(String[] args, INsfPlayerEnv env);

}
