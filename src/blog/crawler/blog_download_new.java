package blog.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.impl.client.DefaultHttpClient;

import common_function.httpclient;

import xtuple.crawler.cls_item;

public class blog_download_new {
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
