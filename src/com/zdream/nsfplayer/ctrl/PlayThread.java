package com.zdream.nsfplayer.ctrl;

import java.util.concurrent.LinkedBlockingQueue;

import com.zdream.nsfplayer.ctrl.task.ITask;

public class PlayThread implements Runnable {
	
	LinkedBlockingQueue<ITask> queue = new LinkedBlockingQueue<ITask>();
	INsfPlayerEnv env;
	
	public PlayThread(INsfPlayerEnv env) {
		this.env = env;
	}

	@Override
	public void run() {
		while (true) {
			try {
				ITask task = queue.take();
				task.execute(env);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
