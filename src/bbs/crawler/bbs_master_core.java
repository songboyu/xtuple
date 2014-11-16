package bbs.crawler;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import common_function.IdleConnectionMonitorThread;
import common_function.ThreadMonitor;
import common_function.httpclient;

import blog.crawler.blog_download;
import blog.crawler.blog_download_ajax;
import blog.crawler.blog_download_new;

import xtuple.crawler.cls_item;

public class bbs_master_core extends Thread {
	/**
	 * Logger for this class
	 */
	private static final Logger logger = Logger
			.getLogger(bbs_master_core.class);
	public BlockingQueue<String> tasks;
	public static List<String> bbs_list = new ArrayList<String>();
	public blog_download_ajax getter_ajax = new blog_download_ajax();
	public blog_download_new getter = new blog_download_new();
	public bbs_filter filter;
	public Set<String> finished;
	public Set<String> exist;
	public Set<String> error_link;
	public AtomicInteger title_id = new AtomicInteger(1);

	List<bbs_core> threadList = new ArrayList<bbs_core>();

	private boolean stop = false;

	private long crawlerStartTime;
	private long crawlerLastStartTime;


	public void set_filter(bbs_filter filter) {
		this.filter = filter;
	}

	public void get_bbs_list(String filename) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(filename), "utf8"));
			String line;

			while ((line = br.readLine()) != null) {
				System.out.println(line);
				bbs_list.add(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void run() {
		try {
			crawlerLastStartTime = 0;// crawlerStartTime = new Date().getTime()
										// - 100L * 24 * 60 * 60 * 1000;
			//filter.title_id.set(1);//此ID用于帖子ID
			String yesterday = "-1";
			String today = (new Date()).toLocaleString().split(" ")[0];
			int batchs=0;
			String Batch="";
			
			while (true) {
				//批次生成
				today = (new Date()).toLocaleString().split(" ")[0];
				if(yesterday.equals("-1")||!today.equals(yesterday))
				{
					batchs=0;
					yesterday = today;
				}
				else
				{
					batchs++;
				}		
				Batch = today + ","+String.valueOf(batchs);
				
				System.out.println(Batch);
				
				
				crawlerStartTime = new Date().getTime();
				System.out.println("crawlerLastStartTime:"
						+ crawlerLastStartTime);
				System.out.println("crawlerStartTime:" + crawlerStartTime);
				// --------------------------------------------------------------
				for (int i = 0; i < bbs_list.size(); i++) {
					cls_item item = new cls_item();

					String configPath = bbs_list.get(i); // config's location
					bbs_configure config = new bbs_configure(configPath);
					this.set_filter(new bbs_filter(config));
					this.filter.post_batch = Batch;
					boolean isconfig = false;

					String site_name = config.site_name;
					System.out.println("site_name: "+config.site_name);
					System.out.println("encode: "+config.encode);
					// 重新调整crawlerLastStartTime，调整其与配置文件中的一致
					System.out.println("Date: "+(new Date().getTime()));
					long outdate111 = (long)(Integer.parseInt(config.outdate));
					long cccc = 3600000;
					long tmptest =  outdate111 * cccc;
					crawlerLastStartTime = (new Date().getTime()) - tmptest; 
					tasks = new LinkedBlockingQueue<String>();
					// days
					exist = bbs_exist_info.getExist(site_name,
							Integer.parseInt(config.outdate) / 24);
					finished = new HashSet<String>();
					error_link = new HashSet<String>();
					String beginpage;
					DefaultHttpClient tempclient = new DefaultHttpClient();
					tempclient.getParams().setParameter(
							CoreConnectionPNames.SO_TIMEOUT, 5 * 1000);
					tempclient.getParams().setParameter(
							CoreConnectionPNames.CONNECTION_TIMEOUT, 2 * 1000);

					isconfig = false;
					for (int j = i + 1; j < bbs_list.size()
							&& isconfig == false; j++) {
						try {
							beginpage = bbs_list.get(j);
							if (beginpage.endsWith(".config")) {
								isconfig = true;
								i = j - 1;
								break;
							}

							do {
								try {
									item = getter.download(tempclient,
											beginpage, config.encode);
								} catch (Exception e) {
									// do nothing
								}
							} while (item == null);
							// get all article page
							this.filter.postlistpagenum = 0;
							int firstFivePage = 0;
							int tryAgain = 0;
							boolean haveNextPage = filter.check_articlelist(
									item, crawlerLastStartTime);
							while (true) {
								for (int cnt = 1; cnt < item.links.size(); cnt++) {
									if (finished.contains(item.links
											.elementAt(cnt)) == true) 
										continue;
									finished.add(item.links.elementAt(cnt));

									tasks.add(item.links.elementAt(cnt));
								}
								tryAgain = 0;
								if (haveNextPage == false)
									break;
								String nextPage = item.links.elementAt(0);
								item = null;
								
								if (config.site_name.equals("凯迪社区")) {
									nextPage = nextPage.replace(" ", "%20");
								}
								
								item = getter.download(tempclient, nextPage,
										config.encode);
								while (item == null && tryAgain < 10) {
									System.out.println("Try again!");
									item = getter.download(tempclient,
											nextPage, config.encode);
									tryAgain++;
								}
								haveNextPage = filter.check_articlelist(item,
										crawlerLastStartTime);
								firstFivePage++;
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						if (j == bbs_list.size() - 1)
							i = j;
					}

					startThread(tasks, configPath, 1);

				}
				
				event_of_title eventtmp=new event_of_title();
				eventtmp.update_eventid_subeventid();
				if (stop)
					break;
				Thread.sleep(2 * 60 * 60 * 1000);
				if (stop)
					break;
				

				// -----------------------------------------------------------------------------
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("BBS Crawler Stopped!");
	}

	public void stopMe() {
		stop = true;
		tasks.clear();
		this.interrupt();
	}

	private void startThread(BlockingQueue<String> tasks, String configPath,
			int threadnum) {
		try {
			CountDownLatch stopLatch = new CountDownLatch(threadnum);
			// ThreadMonitor monitor = null;
			httpclient getClient = new httpclient();
			ClientConnectionManager cm = getClient.getManager();
			DefaultHttpClient httpclient = getClient.getDefaultHttpClient(cm);
			IdleConnectionMonitorThread monitor = new IdleConnectionMonitorThread(
					cm);
			monitor.start();
			monitor.setPriority(Thread.MAX_PRIORITY);

			bbs_configure config = new bbs_configure(configPath);

			int INFINITE_NUM = threadnum * 3;
			// final Semaphore articlelistFirst = new Semaphore(INFINITE_NUM,
			// true);
			for (int j = 0; j < threadnum; j++) {
				bbs_core core2 = new bbs_core(tasks, exist, stopLatch,
						httpclient, config.encode, logger,
						crawlerLastStartTime, error_link,title_id);
				core2.set_filter(new bbs_filter(config));
				core2.filter.ReplyClickNum = this.filter.ReplyClickNum;
				core2.filter.post_batch = this.filter.post_batch;
				core2.start();
				// new Thread(core2).start();
				threadList.add(j, core2);
				System.out.println("Thread started");
			}
			stopLatch.await();
			monitor.shutdown();

			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						new File("Complete.txt"), true));
				writer.write("One BBS finished" + "\r\n");

				long date = new Date().getTime();
				DateFormat d6 = DateFormat.getDateTimeInstance(DateFormat.LONG,
						DateFormat.LONG);
				String now = d6.format(date);
				writer.write(now + "\r\n");
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			for (int j = 0; j < threadList.size(); j++) {
				System.out.println("Thread check");
				Thread temp = threadList.get(j);
				if (temp.isAlive() == true)
					temp.stop();
			}
			threadList.clear();
			tasks.clear();
			exist.clear();
			finished.clear();
			System.gc();

			if (logger.isInfoEnabled()) {
				logger.info("BBS: " + config.site_name + " finished!");
			}
			System.out.println("One BBS finished");
		} catch (Exception e) {
			e.printStackTrace();
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						new File("ThreadError_Links.txt"), true));
				writer.write(e.toString() + "\r\n");
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
}