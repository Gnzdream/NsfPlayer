package com.zdream.nsfplayer.ctrl;

import java.util.concurrent.LinkedBlockingQueue;

import com.zdream.nsfplayer.ctrl.task.ITask;

public class PlayThread implements Runnable {
	
	LinkedBlockingQueue<ITask> queue = new LinkedBlockingQueue<ITask>();

	INsfPlayerEnv env;
	
	public PlayThread(INsfPlayerEnv env) {
		this.env = env;
	}
	
	public synchronized final void putTask(ITask t) {
		queue.add(t);
	}

	@Override
	public void run() {
		while (true) {
			ITask t;
			try {
				t = queue.take();
				t.execute(env);
			} catch (InterruptedException e) { }
		}
	}

}
