package blog.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
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
import org.htmlparser.util.EncodingChangeException;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;

import xtuple.crawler.cls_item;

public class blog_download 
{
	static AtomicInteger pageNo = new AtomicInteger(0);
	
	public cls_item download(String link)
	{
//		System.out.println("Downloading...");
		
		cls_item ci = new cls_item();
		ci.link = link;
			
		try 
		{
			System.out.println("getpage...");
		    Parser p = new Parser();
			p.setURL(link);
		    
//			p.setEncoding("GBK");
//			p.setEncoding(CharsetAutoSwitch.dectedEncode(link));
			String encode = p.getEncoding();	
			//���getEncoding��Դ�뾭�����޸ģ���ȡmeta��ǩǰ������Ҳ���׳��쳣
			p.setEncoding(encode);

			HtmlPage hp = new HtmlPage(p);
//			try{
				p.visitAllNodesWith(hp);
//			}catch (EncodingChangeException e)
//			{
//				e.printStackTrace();
//			}
//			ci.content = hp.getBody().asString();
			ci.html = hp.getBody().toHtml();
//			ci.html =new String(ci.html.getBytes("GBK"),"UTF-8");
			System.out.println("htmlparser Downloaded2");
			System.out.println("CRAWLER HAVE DOWNLOADED " + pageNo.incrementAndGet() + " PAGE");
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
