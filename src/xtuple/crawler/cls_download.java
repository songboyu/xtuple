package xtuple.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.codec.binary.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.LinkBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;

public class cls_download 
{
	static AtomicInteger pageNo = new AtomicInteger(0);
	
	public cls_item download(String link)
	{
		long starttime,endtime;
		starttime = System.currentTimeMillis();
//		System.out.println("Downloading...");
		
		cls_item ci = new cls_item();
		ci.link = link;
			
		try 
		{
			LinkBean linkBean = new LinkBean();  
		    linkBean.setURL(link); 
		    System.out.println("setURL");
		    URL[] urls = linkBean.getLinks();
		    System.out.println("getLinks");
		    
		    for(int cnt=0;cnt<urls.length;cnt++)
		    {
		    	ci.links.add(urls[cnt].toString());
		    }
		    
		    Parser p = new Parser();
			p.setURL(link);
			p.setEncoding(p.getEncoding());
			HtmlPage hp = new HtmlPage(p);
			p.visitAllNodesWith(hp);
			
//			ci.content = hp.getBody().asString();
			ci.html = hp.getBody().toHtml();
			System.out.println("NEWS CRAWLER HAVE DOWNLOADED " + pageNo.incrementAndGet() + " PAGE");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return null;
		}
		
		endtime = System.currentTimeMillis();
		System.out.println(endtime - starttime);
		
		try {
			BufferedWriter writer;

			writer = new BufferedWriter(new FileWriter(new File("newsPageNo.txt"),true));
			int num = pageNo.get();
			String n = "" + num;
			writer.write(n + "\r\n");
			writer.write(new Date() + "\r\n");
			writer.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ci;
	}
}
