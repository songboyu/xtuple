package blog.crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.apache.http.impl.client.DefaultHttpClient;

import common_function.httpclient;

import bbs.crawler.bbs_configure;
import bbs.crawler.bbs_core;
import bbs.crawler.bbs_filter;

import xtuple.crawler.cls_download;
import xtuple.crawler.cls_filter;
import xtuple.crawler.cls_item;

public class blog_core implements Runnable{
	public BlockingQueue<String> tasks;
//	public blog_download getter = new blog_download();
	public blog_download_ajax ajaxGetter = new blog_download_ajax();
	public blog_download_new getter = new blog_download_new();
	public blog_filter filter;
	public Set<String> exist;
	private CountDownLatch countDownLatch;
	public DefaultHttpClient httpclient;
	private boolean isAjax;
	blog_configure config;
	
	blog_core(BlockingQueue<String> q,Set<String> exist,CountDownLatch countDownLatch,DefaultHttpClient httpclient,boolean isAjax,blog_configure config)
	{
		this.tasks = q;
		this.exist = exist;
		this.countDownLatch = countDownLatch;
		this.httpclient = httpclient;
		this.isAjax = isAjax;
		this.config = config;
	}
	
	public void set_filter(blog_filter filter)
	{
		this.filter = filter;
	}
	
	public void run()	
	{
		cls_item item;
//		new httpclient();
		
		try{
			while(!tasks.isEmpty())
			{
				String output = tasks.poll();
				if (output.equals("Ignore Me"))
				{
					Thread.sleep(10 * 1000);
					continue;
				}
				
				if (exist.contains(output))
					continue;
				
				cls_item item_output;
				
				//Need ajax to load articlelist
				//I simply assume only articlelist need to be loaded dynamically
				if (isAjax == true && filter.isarticle(output) == false)	
				{
					item_output = ajaxGetter.download(output);
				}
				else
				{
//					item_output = getter.download(output);
					item_output = getter.download(httpclient,output,config.encode);
				}
				
				cls_item i = item_output;
			
//				for(int cnt=0;cnt<item_output.links.size();cnt++)
//				{
//				if (finished.contains(item_output.links.elementAt(cnt)) == true || filter.todo(item_output.links.elementAt(cnt)) == false)
//					continue;
				if (item_output.links.size() == 1)	//homepage
				{
					//change item 
					i = getter.download(httpclient,item_output.links.elementAt(0),config.encode);
//					i = getter.download(item_output.links.elementAt(0));	
				}
				else if (item_output.links.size() > 1)
				{
					System.out.println("Error!");
					return;
				}
				else 
					;
//					System.out.println("before :" + tasks.peek().link);
				if (filter.isarticle(i.link))	
				{
					if(filter.check_article(i))	//article outdate
						;
//						tasks.clear();
				}
				else 
				{
//					System.out.println("articlelist:" + tasks.peek().link);
					//Edit articlelist like bbs   --uncomplete
					filter.check_articlelist(i);
					for (int j=0; j<i.links.size();j++)
					{
						tasks.offer(i.links.elementAt(j));
					}
				}
				
			}
		}catch (Exception e){
			e.printStackTrace();
		}finally{
			countDownLatch.countDown();
		}
		
		System.out.println("tasks.isEmpty():" + tasks.isEmpty());
	}
}
	

