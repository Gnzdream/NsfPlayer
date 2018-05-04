package zdream.nsfplayer.ctrl;

import java.util.concurrent.LinkedBlockingQueue;

import zdream.nsfplayer.ctrl.task.ITask;

public class PlayThread implements Runnable {
	
	LinkedBlockingQueue<ITask> queue = new LinkedBlockingQueue<ITask>();
	INsfPlayerEnv env;
	
	public PlayThread(INsfPlayerEnv env) {
		this.env = env;
	}
	
	public synchronized final void putTask(ITask t) {
		queue.add(t);
	}
	
	public final ITask nextTask() {
		return queue.peek();
	}

	@Override
	public void run() {
		while (true) {
			ITask t;
			try {
				t = queue.take();
				t.execute(env);
			} catch (InterruptedException e) {
				
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}

}