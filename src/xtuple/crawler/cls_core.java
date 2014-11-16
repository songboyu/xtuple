package xtuple.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.apache.http.impl.client.DefaultHttpClient;

import common_function.httpclient;

import news_comment.crawler.comment_item;

public class cls_core implements Runnable
{
	private BlockingQueue<comment_item> comment_tasks;
	public BlockingQueue<cls_item> tasks;
	public Set<String> finished;
	public Set<String> exist;
//	public cls_download getter = new cls_download();
	public cls_download_new getter = new cls_download_new();
	public cls_filter filter;
	public DefaultHttpClient httpclient;
	public String encode;
	
	private final CountDownLatch countDownLatch;
	
	cls_core(Set<String> f, CountDownLatch latch,BlockingQueue<cls_item> t,BlockingQueue<comment_item> queue, Set<String> e,DefaultHttpClient httpclient,String encode)
	{
		this.finished = f;
		this.countDownLatch = latch;
		this.tasks = t;
		this.comment_tasks = queue;
		this.exist = e;
		this.httpclient = httpclient;
		this.encode = encode;
	}
	
	public void set_filter(cls_filter filter)
	{
		this.filter = filter;
	}
	
	public void seed(String seed)
	{
		System.out.println("Add a seed");
		try{
			cls_item item = getter.download(httpclient,seed,encode);
			tasks.add(item);    //cls_item downloaded but have not stored 
		
		for (int i=0; i<10; i++)	//just to prevent the queue is empty
			tasks.add(item);
		}catch (NullPointerException  e){
			e.printStackTrace();
		}
	}
	
	public void run()
	{
	try{
		System.out.println("RUN!");
		while(!tasks.isEmpty())
		{
			cls_item item_output = tasks.poll();
						
			finished.add(item_output.link);
			
//			System.out.println(item_output.link);
			for(int cnt=0;cnt<item_output.links.size();cnt++)
			{
				String url = item_output.links.elementAt(cnt);
				if (url.length() == 0)
					continue;
//				System.out.println(url);
//				if (url.charAt(url.length()-1) == '#')	//duplicate url
//					url = url.substring(0,url.length()-1);
				
				try{
					String temp [] = url.split("#");
					url = temp[0];
				}catch (Exception e){
					//do nothing
				}
				
				if (finished.contains(url) == true || filter.todo(url) == false)
					continue;
				
				cls_item i = null;
				
				if (finished.contains(url) == false) //duplicate link in item.links 
				{
					finished.add(url);
					i = getter.download(httpclient,url,encode);
				}
				if (exist.contains(url))
				{
					System.out.println("----------------------------------------");
					System.out.println("Exist!");
					System.out.println("----------------------------------------");
					tasks.offer(i);
					filter.checkExist(i, comment_tasks);
					continue;
				}
				
				if (i != null && filter.todo(i.link) == true)
//				if (i != null)
				{
					filter.check(i,comment_tasks);
					tasks.offer(i);
//						filter.check(i,comment_tasks);
//					comment_crawler.crawl(i.link);
				}
			}
		}
		System.out.println("tasks.isEmpty():" + tasks.isEmpty());
		try{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:\\New.txt"), true));
		
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		
		
		writer.write(date.toString() + "\r\n");
		writer.close();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}catch (Exception e)
	{
		e.printStackTrace();
	}finally{
		countDownLatch.countDown();
	}
	
		
	}
}
