package blog.crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import common_function.httpclient;

import bbs.crawler.bbs_configure;
import bbs.crawler.bbs_exist_info;

import xtuple.crawler.cls_item;

public class blog_master_core extends Thread{
	public List<String> blog_list = new ArrayList<String>();
	public BlockingQueue<String> tasks;
	public Set<String> exist;
//	public blog_download getter = new blog_download();
	public blog_download_new getter = new blog_download_new();
	public blog_filter filter;
	
	public void set_filter(blog_filter filter)
	{
		this.filter = filter;
	}
	
	public void get_blog_list(String filename)
	{
		try{
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			
			while((line = br.readLine()) != null)
			{
				System.out.println(line);
				blog_list.add(line);
			}
			
		}catch (Exception  e){
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		cls_item item;
		
		
			while (true)
			{
				try{
				exist = blog_exist_info.getExist(4);
				boolean isconfig = false;
				
				DefaultHttpClient tempclient = new DefaultHttpClient();
				tempclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20 * 1000);
				tempclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5 * 1000);
				
				for (int i=0; i<blog_list.size(); i++)	//����ȡ�Ĳ����б�
				{
					System.out.println("i:" + i);
					String configPath = blog_list.get(i);	//config's location
					blog_configure config = new blog_configure(configPath);
					this.set_filter(new blog_filter(config));
					
					tasks = new LinkedBlockingQueue<String>();
					
					isconfig = false;
					for (int j=i+1; isconfig==false && j<blog_list.size(); j++)
					{
						System.out.println("j:" + j);
						String homepage = blog_list.get(j);
						if (homepage.endsWith(".config"))
						{
							isconfig = true;
							i = j-1;
							System.out.println("j->i:" + i);
							break;
						}
						int times = 0;
						times = 0;
//						do{
							item = getter.download(tempclient,homepage,config.encode);
//							times++;
//						}while (item == null && times < 5);
						filter.check_homepage(item);
						for (String link:item.links)
							tasks.add(link);
						i = j;
					}
					int threadnum = 5;
//					for (int j=0; j<threadnum; j++)
//						tasks.add("Ignore Me");
					
					startThread(tasks,config,threadnum,tempclient);
//					Thread.sleep(20 * 60 * 1000);
				}
				
				System.out.println("Blog Task Finished");
		
				Thread.sleep(20 * 60 * 1000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		
		
	}
	
	private void startThread(BlockingQueue<String> tasks, blog_configure config, int threadnum,DefaultHttpClient httpclient)
	{
		CountDownLatch countDownLatch = new CountDownLatch(threadnum);
		
		boolean isAjax = false;
		if (config.isAjax.equals("true"))
			isAjax = true;
		blog_core core2 = new blog_core(tasks,exist,countDownLatch,httpclient,isAjax,config);
		core2.set_filter(new blog_filter(config));
		
		for (int j=0; j<threadnum; j++)
		{
//			Thread t = new Thread(core2);
			new Thread(core2).start();
//			threadls.add(t);
//			t.start();
		}
		try {
			
			countDownLatch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
