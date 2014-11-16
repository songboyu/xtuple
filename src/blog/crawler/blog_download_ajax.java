package blog.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.gargoylesoftware.htmlunit.NicelyResynchronizingAjaxController;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import xtuple.crawler.cls_item;

public class blog_download_ajax {
	public cls_item download(String link)
	{
//		System.out.println("Downloading...");
		
		cls_item ci = new cls_item();
		ci.link = link;
			
		try 
		{
			System.out.println("getpage...");
		    
			WebClient webClient = new WebClient();
//			webClient.setJavaScriptEnabled(true);
			webClient.setAjaxController(new NicelyResynchronizingAjaxController());
			webClient.setCssEnabled(false);
			webClient.setThrowExceptionOnScriptError(false);
			HtmlPage page = webClient.getPage(link);
			
			String xml =  page.asXml();
			
			ci.html = xml;
			System.out.println("htmlunit Downloaded");
//			System.out.println("CRAWLER HAVE DOWNLOADED " + pageNo.incrementAndGet() + " PAGE");
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
