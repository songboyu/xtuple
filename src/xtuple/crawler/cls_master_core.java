package xtuple.crawler;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import common_function.IdleConnectionMonitorThread;
import common_function.httpclient;

import bbs.crawler.bbs_configure;
import bbs.crawler.bbs_exist_info;
import bbs.crawler.bbs_filter;

import news_comment.crawler.comment_item;
import news_comment.crawler.excute_task;

public class cls_master_core implements Runnable{
	Set<String> finished;
	BlockingQueue<comment_item> comment_tasks;
	BlockingQueue<cls_item> tasks;
	Set<String> exist;
	
	public static List<String> newssite_list = new ArrayList<String>();
	cls_common_filter filter;
	cls_download_new getter = new cls_download_new();
	
	public void set_filter(cls_common_filter filter)
	{
		this.filter = filter;
	}
	
	public void get_newssite_list(String filename)
	{
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"utf8"));
			String line;
			
			while((line = br.readLine()) != null)
			{
				System.out.println(line);
				newssite_list.add(line);
			}
			
		}catch (Exception  e){
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		while (true)
		{
//--------------------------------------------------------------
		for (int i=0; i<newssite_list.size(); i+=2)
		{
			cls_item item_output = new cls_item();
			
			String configPath = newssite_list.get(i);	//config's location
			cls_configure config = new cls_configure(configPath);
			this.set_filter(new cls_common_filter(config));
			
			String site_name = "";
			site_name = config.site_name;
			
			tasks = new LinkedBlockingQueue<cls_item>();
			comment_tasks = new LinkedBlockingQueue<comment_item>();
			finished = Collections.synchronizedSet(new HashSet<String>());
			exist = news_exist_info.getExist(site_name,Integer.parseInt(config.outdate) / 24);
			
			String beginpage = newssite_list.get(i+1);
			
			DefaultHttpClient tempclient = new DefaultHttpClient();
			tempclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20 * 1000);
			tempclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 5 * 1000);
			
			do{
				item_output = getter.download(tempclient,beginpage,config.encode);
				System.out.println("Download beginpage");
			}while (item_output == null);

			filter.check(item_output, comment_tasks);	//�ӿ�ʼҳ��ȡ����б�

			int threadnum = 10;
			for (int j=0; j<threadnum; j++)
				tasks.offer(item_output);
			startThread(tasks,comment_tasks,exist,finished,config,threadnum);
			
			try {
			int pageNobegin = excute_task.pageNo.get();
			Thread.sleep(60 * 1000);
			int pageNoend = excute_task.pageNo.get();
			
			int pageNo = pageNoend - pageNobegin;
			
			try {
				BufferedWriter writer;

				writer = new BufferedWriter(new FileWriter(new File("commentPageNo.txt"),true));
				String n = "" + pageNo;
				writer.write(n + "\r\n");
				writer.write(new Date() + "\r\n");
				writer.close();
				
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			Thread.sleep(20 * 60 * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//-----------------------------------------------------------------------------
		}
	}
		
		public void startThread(BlockingQueue<cls_item> tasks,BlockingQueue<comment_item> comment_tasks, Set<String> exist, Set<String> finished, cls_configure config, int threadnum)
		{
			CountDownLatch stopLatch = new CountDownLatch(threadnum);
			
			httpclient getClient = new httpclient();
			ClientConnectionManager cm = getClient.getManager();
			DefaultHttpClient httpclient = getClient.getDefaultHttpClient(cm);
			
			cls_core core = new cls_core(finished, stopLatch,tasks,comment_tasks,exist,httpclient,config.encode);
			core.set_filter(new cls_common_filter(config));
			
			excute_task a = new excute_task(comment_tasks);
			
			for (int i=0; i<threadnum; i++)
			{
				new Thread(core).start();
				new Thread(a).start();
				System.out.println("i:" + i);
			}
			try {
				stopLatch.await();
				tasks.clear();
				finished.clear();
				exist.clear();
//				comment_tasks.clear();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(new File("d:\\Complete.txt"), true));
				writer.write("One news finished" + "\r\n");
				
				long date = new Date().getTime();
				DateFormat d6 = DateFormat.getDateTimeInstance(DateFormat.LONG,DateFormat.LONG);
				String now = d6.format(date);
				
				writer.write(now + "\r\n");
				writer.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			
		}
	
}
		
	
	

