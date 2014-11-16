package common_function;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import bbs.crawler.bbs_core;

public class ThreadMonitor implements Runnable{
	
	List<bbs_core> threadList;
	bbs_core temp;
	private boolean shutdown = false;
	long pastTime;
	
	public ThreadMonitor(List<bbs_core> list)
	{
		super();
		this.threadList = list;
	}
	
	@SuppressWarnings("deprecation")
	public void run()
	{
//		this.setPriority(Thread.MAX_PRIORITY);
		shutdown = false;
		System.out.println("||||||||||||||||||||||||||||||||||||");
		System.out.println("Monitor start");
		System.out.println("||||||||||||||||||||||||||||||||||||");
		try {
			while (shutdown == false)
			{
				Thread.sleep(180 * 1000);
				System.out.println("||||||||||||||||||||||||||||||||||||");
				System.out.println("Wake");
				System.out.println("||||||||||||||||||||||||||||||||||||");
				for (int i = 0; i<threadList.size(); i++)
				{
					temp = threadList.get(i);
					if (temp.isAlive() == false)
						continue;
					pastTime = new Date().getTime() - temp.getLoop_begintime();
					if (pastTime > 180 * 1000)
					{
						bbs_core newThread = temp.clone();
						threadList.set(i, newThread);
						newThread.start();
						System.out.println("-------||||||||||||||||||||||||||||||||||||------");
						System.out.println("Restart");
						System.out.println("-------||||||||||||||||||||||||||||||||||||------");
//						temp.stopMe();
						temp.interrupt();
						temp.stop();
					}
				}
				System.gc();
				System.out.println("||||||||||||||||||||||||||||||||||||");
				System.out.println("Sleep");
				System.out.println("||||||||||||||||||||||||||||||||||||");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:\\ThreadError.txt"), true));
				writer.write(e.toString() + "\r\n");
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		System.out.println("-------||||||||||||||||||||||||||||||||||||------");
		System.out.println("Monitor closed");
		System.out.println("-------||||||||||||||||||||||||||||||||||||------");
	}

	public boolean isShutdown() {
		return shutdown;
	}

	public void setShutdown(boolean shutdown) {
		this.shutdown = shutdown;
	}
}	
