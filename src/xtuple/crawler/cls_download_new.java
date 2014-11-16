package xtuple.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlparser.beans.LinkBean;

import common_function.httpclient;

public class cls_download_new {
	static AtomicInteger pageNo = new AtomicInteger(0);
	static AtomicInteger failedPageNo = new AtomicInteger(0);
		public cls_item download(DefaultHttpClient httpclient,String link,String encode)
		{
//			System.out.println("Downloading...");
			
			cls_item ci = new cls_item();
			ci.link = link;
				
			try 
			{
				System.out.println("getpage...");
				httpclient getter = new httpclient();
//				System.out.println(encode);
				ci.html = getter.getResponse(httpclient,link,encode);
				if (ci.html != null)
				{
					/*
					LinkBean linkBean = new LinkBean();  
				    linkBean.setURL(link); 
				    System.out.println("setURL");
				    URL[] urls = linkBean.getLinks();
				    System.out.println("getLinks");
				    
				    for(int cnt=0;cnt<urls.length;cnt++)
				    {
				    	ci.links.add(urls[cnt].toString());
				    }
				    */
				    
					Pattern pattern1 = Pattern.compile("a href=[\"']");
					Pattern pattern2 = Pattern.compile("[\"']");
					Matcher begin = pattern1.matcher(ci.html);
					Matcher end = pattern2.matcher(ci.html);
					
					int link_begin = 0, link_end = 0;
					
					while (begin.find(link_end))
					{
						link_begin = begin.end();
						if (end.find(link_begin))
							link_end = end.start();
						
						String tempLink = ci.html.substring(link_begin,link_end);
						ci.links.add(tempLink);
					}
					
					System.out.println("httpclient Downloaded");
					System.out.println("CRAWLER HAVE DOWNLOADED " + pageNo.incrementAndGet() + " PAGE");
				}
				else
				{
					System.out.println("Download task failed");
					System.out.println("CRAWLER HAVE FAILED TO DOWNLOADED " + failedPageNo.incrementAndGet() + " PAGE");
					return null;	//ignore this link
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				String ErrorLink = "Unable to open:" + link + "\r\n";
				try {
					BufferedWriter writer = new BufferedWriter(new FileWriter(new File("DownloadError_Links.txt"), true));
					writer.write(ErrorLink);
					writer.write(e.toString() + "\r\n");
					writer.close();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				return null;
			}

			return ci;
		}

}
