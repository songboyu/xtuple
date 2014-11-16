package bbs.crawler;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.impl.client.DefaultHttpClient;

import news_comment.crawler.comment_item;

import xtuple.crawler.cls_item;
import blog.crawler.blog_download;
import blog.crawler.blog_download_new;
import java.util.Hashtable;

public class bbs_core extends Thread implements Cloneable{
	/**
	 * Logger for this class
	 */
	private final Logger logger;

		public BlockingQueue<String> tasks;
		public blog_download_new getter = new blog_download_new();
//		public blog_download getter = new blog_download();
		public bbs_filter filter;
		public static List<String> bbs_list = new ArrayList<String>();
		public static List<String> bbs_column_list = new ArrayList<String>();
		public Set<String> error_link = new HashSet<String>();

		public Set<String> finished = new HashSet<String>();
		public Set<String> exist;
		public AtomicInteger titleid; 
		
		DefaultHttpClient httpclient;
		String encode;
		
		
		
		private static Object A = new Object();
		private int [] bbs_change;
		volatile boolean stop = false;
		
		private long loop_begintime = new Date().getTime();
		
		static int column_task = 0;
		static int taskend = 0;
		static int existnum = 0;
		
		String link = "";
		
		public void set_filter(bbs_filter filter)
		{
			this.filter = filter;
		}
		
		private final CountDownLatch countDownLatch;
		private long old_time; 
		
		bbs_core(BlockingQueue<String> q,Set<String> e,CountDownLatch stopLatch,DefaultHttpClient client,String en, Logger log,long time,Set<String> error_link,AtomicInteger titleid)
		{
			super();
			this.tasks = q;
			this.exist = e;
			this.countDownLatch = stopLatch;
			this.httpclient = client;
			this.encode = en;
			this.logger = log;
			this.old_time = time;
			this.error_link = error_link;
			this.titleid = titleid;
		}
		
		 @Override
         public bbs_core clone() throws CloneNotSupportedException 
         {
			bbs_core newThread = new bbs_core(tasks,exist,countDownLatch,httpclient,encode,logger,old_time,error_link,titleid); 
			newThread.set_filter(filter);
			tasks.offer(link);
			 
			return newThread;
			 
         }
		
		private volatile Thread blinker;
		
		
		public void stopMe()
		{
			BlockingQueue<String> emptyQueue = new LinkedBlockingQueue<String>();
			tasks = emptyQueue;
		}
		
		public void run()	
		{
			cls_item item;
			int me;
//			int i = -1;
			
			try {
				
			while(!tasks.isEmpty())
			{
				try{
					setLoop_begintime(new Date().getTime());
				
					link = tasks.poll();
					System.out.println(link + "task");
					item = getter.download(httpclient,link,encode);
					System.out.println(link + "Downloaded!!!");
//					item = getter.download(link);
					
					int i = 0;
					//try to download articlelist 5 times
					while (item == null && filter.ispost(link) && i<5)
					{
						item = getter.download(httpclient,link,encode);
						i++;
					}
					if (item == null)
					{
						System.out.println("||||||||||||||||||||------------------------");
						System.out.println("Task Abort!");
						System.out.println("--------------------|||||||||||||||||||||||||");
						
						try {
							BufferedWriter writer = new BufferedWriter(new FileWriter(new File("c:\\taskError.txt"), true));
							writer.write("Task Abort" + "\r\n");
							writer.write(link + "\r\n");
							
							DateFormat d6 = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
							String str = d6.format(new Date());
							
							writer.write(str + "\r\n\r\n");
							writer.close();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						if (error_link.contains(link))	//no more chance
							;
						else{
							error_link.add(link);
							tasks.add(link);	//try again,you got one more chance
						}
						continue;
						
					}
					
					System.out.println("Downloaded:" + link);

					if (filter.ispost(item.link))	
					{
						if (exist.contains(link))
						{
							System.out.println("----------------------------------");
							System.out.println("Exist!");
							System.out.println("----------------------------------");
							existnum++;
							filter.check_exist(item,httpclient);
						}
						else
						{
							filter.check_article(item,httpclient,titleid);
//							articlelistFirst.release(permits);
								
							System.out.println("----------------------------------");
							System.out.println("Existnum :" + existnum);
						}
					}
					else if(filter.iscolumn(item.link))
					{
//do articlelist first to avoid an empty task
						this.setPriority(Thread.MAX_PRIORITY);
						int j;
						if (filter.check_articlelist(item,old_time)){
							j=0;
						}
						else{
							j=1;	//omit nextpage
						}
						for (; j<item.links.size();j++)
						{
							tasks.offer(item.links.elementAt(j));
						}
						this.setPriority(Thread.NORM_PRIORITY);
					}
					else
						continue;
//					wait(1000);
				}catch (Exception ex){
					ex.printStackTrace();
				}
//				articlelistFirst.acquire();	
			}
/*			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();*/
//			}
//			if (tasks.isEmpty())
//				break;
			
//			}
//			synchronized (A)
//			{
				taskend++;
				System.out.println("---------------------------------------------");
				System.out.println("No more task!     " + taskend);
				System.out.println("---------------------------------------------");
				long time = System.currentTimeMillis();
				Date date = new Date(time);
				
				System.out.println(date);
//				stop = true;
//			}
			}catch (Exception e)
			{
				e.printStackTrace();
			}finally{
				countDownLatch.countDown();
			}
			
		}

		public long getLoop_begintime() {
			return loop_begintime;
		}

		public void setLoop_begintime(long loop_begintime) {
			this.loop_begintime = loop_begintime;
		}
}
