package zdream.nsfplayer.ftm;

import java.util.concurrent.LinkedBlockingQueue;

import zdream.nsfplayer.ftm.task.IFtmTask;

public class PlayThreadForFtm implements Runnable {

	LinkedBlockingQueue<IFtmTask> queue = new LinkedBlockingQueue<IFtmTask>();
	FtmPlayerConsole env;
	
	public PlayThreadForFtm(FtmPlayerConsole env) {
		this.env = env;
	}
	
	public synchronized final void putTask(IFtmTask t) {
		queue.add(t);
	}
	
	public final IFtmTask nextTask() {
		return queue.peek();
	}

	@Override
	public void run() {
		while (true) {
			IFtmTask t;
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
