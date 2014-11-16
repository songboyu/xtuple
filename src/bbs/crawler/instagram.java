package bbs.crawler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.util.JSONTokener;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import common_function.common_function;


import xtuple.crawler.cls_database;
import xtuple.crawler.cls_item;

import blog.crawler.blog_download_new;

public class instagram {
	
	public class instagram_conf{
		
		String link_begin;
		String link_end;
		String author_begin;
		String author_end;
		String caption_begin;
		String caption_end;
		String likes_begin;
		String likes_end;
		String display_src_begin;
		String display_src_end;
		String time_begin;
		String time_end;
		String comment_begin;
		String comment_end;
		String username_begin;
		String username_end;
		String text_begin;
		String text_end;
		
		public instagram_conf(String file) throws IOException
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file),"utf8"));
			
			this.link_begin = br.readLine();
			this.link_end = br.readLine();
			this.author_begin = br.readLine();
			this.author_end = br.readLine();
			this.caption_begin = br.readLine();
			this.caption_end = br.readLine();
			this.likes_begin = br.readLine();
			this.likes_end = br.readLine();
			this.display_src_begin = br.readLine();
			this.display_src_end = br.readLine();
			this.time_begin = br.readLine();
			this.time_end = br.readLine();
			this.comment_begin = br.readLine();
			this.comment_end = br.readLine();
			this.username_begin = br.readLine();
			this.username_end = br.readLine();
			this.text_begin = br.readLine();
			this.text_end = br.readLine();
			
			br.close();
		}
	}
	
	public BlockingQueue<String> links = new LinkedBlockingQueue<String>();
	public instagram_conf configure;
	
	public blog_download_new getter = new blog_download_new();
	public DefaultHttpClient tempclient;
	public cls_database db = new cls_database();
	
	
	public void craw() throws IOException, SQLException
	{
		configure = new instagram_conf("C:\\Users\\lxq\\workspace\\xtuple\\instagram.config");
		
		String beginpage="http://instagram.com/xijinpingofficial1";
		//getlinks(beginpage);
		beginpage="http://instagram.com/xijinpingofficial";
		getlinks(beginpage);
		downloadlink();
	}
	
	public void getlinks(String url)
	{
		tempclient = new DefaultHttpClient();
		tempclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5 * 1000);
		tempclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2 * 1000);
		
		cls_item item = new cls_item();
		do
		{
			item = getter.download(tempclient,url, "utf-8");
		}while(item==null);
		
		analyselink(item);
	}
	
	public void analyselink(cls_item page)
	{
		Pattern link_begin = Pattern.compile(configure.link_begin,Pattern.DOTALL);
		Pattern link_end = Pattern.compile(configure.link_end, Pattern.DOTALL);
		
		Matcher m_link_begin = link_begin.matcher(page.html);
		Matcher m_link_end = link_end.matcher(page.html);
		
		int link_begin_index=0,link_end_index=0;
		
		while(m_link_begin.find(link_end_index))
		{
			link_begin_index = m_link_begin.end();
			if(m_link_end.find(link_begin_index))
				link_end_index = m_link_end.start();
			String tmplinks = page.html.subSequence(link_begin_index, link_end_index).toString();
			//System.out.println(tmplinks);
			tmplinks = "http://" + tmplinks.replace("\\", "");
			System.out.println(tmplinks);
			links.add(tmplinks);
		}
	}
	
	public void downloadlink() throws SQLException
	{
		while(!links.isEmpty())
		{
			String url = links.poll();
			System.out.println(url);
			cls_item item = new cls_item();
			do
			{
				item = getter.download(tempclient,url, "utf-8");
			}while(item==null);
			
			analysepage(item);
		}
	}
	
	public String pureforSQL(String sql)
	{
		String result = "";
		result = sql.replace("'", "");
		
		return result;
	}
	
	
	public void analysepage(cls_item page) throws SQLException
	{
		String link = page.link;
		String caption = common_function.getInfo(page.html, configure.caption_begin,configure.caption_end);
		String display_src = common_function.getInfo(page.html, configure.display_src_begin, configure.display_src_end);
		String time = common_function.getInfo(page.html, configure.time_begin, configure.time_end);
		String author = common_function.getInfo(page.html, configure.author_begin, configure.author_end);
		
		System.out.println("link:"+link);
		caption = unicodeToUtf8(caption);
		caption = pureforSQL(caption);
		System.out.println("caption:"+caption);
		System.out.println("display_src:"+display_src);
		display_src = pureforSQL(display_src);
		System.out.println("time:"+time);
		System.out.println("author:"+author);
		author = pureforSQL(author);
		
		db.update("insert into post_info(link,caption,display_src,time,author) values ('"+link+"','"+caption+"','"+ display_src+"','"+time+"','"+author+"')");
		
		ResultSet rs;
		rs = db.getRs("select * from post_info where link='" + link+ "'");
		rs.next();
		String post_id = rs.getString("id");
		
		String comment = common_function.getInfo(page.html,configure.comment_begin,configure.comment_end);
		
		Pattern username_begin = Pattern.compile(configure.username_begin,Pattern.DOTALL);
		Pattern username_end = Pattern.compile(configure.username_end, Pattern.DOTALL);
		
		Matcher m_username_begin = username_begin.matcher(comment);
		Matcher m_username_end = username_end.matcher(comment);
		
		int username_begin_index=0,username_end_index=0;
		
		
		Pattern text_begin = Pattern.compile(configure.text_begin,Pattern.DOTALL);
		Pattern text_end = Pattern.compile(configure.text_end, Pattern.DOTALL);
		
		Matcher m_text_begin = text_begin.matcher(comment);
		Matcher m_text_end = text_end.matcher(comment);
		
		int text_begin_index=0,text_end_index=0;
		
		while(m_text_begin.find(text_end_index))
		{
			text_begin_index = m_text_begin.end();
			if(m_text_end.find(text_begin_index))
				text_end_index = m_text_end.start();
			String tmptext = comment.subSequence(text_begin_index, text_end_index).toString();
			tmptext = unicodeToUtf8(tmptext);
			System.out.println(tmptext);
			if(m_username_begin.find(text_end_index))
			{
				username_begin_index = m_username_begin.end();
				if(m_username_end.find(username_begin_index))
					username_end_index = m_username_end.start();
				String tmpusername = comment.subSequence(username_begin_index, username_end_index).toString();
				System.out.println(tmpusername);
				
				db.update("insert into comment (post_id,username,text) value('" +post_id+"','"+pureforSQL(tmpusername)+"','"+pureforSQL(tmptext)+"')");
			}
		}
	}
	
	
	public void storeimg(String url)
	{
		
	}
	
	 public static String unicodeToUtf8(String theString) {
	        char aChar;
	        int len = theString.length();
	        StringBuffer outBuffer = new StringBuffer(len);
	        for (int x = 0; x < len;) {
	            aChar = theString.charAt(x++);
	            if (aChar == '\\') {
	                aChar = theString.charAt(x++);
	                if (aChar == 'u') {
	                    // Read the xxxx
	                    int value = 0;
	                    for (int i = 0; i < 4; i++) {
	                        aChar = theString.charAt(x++);
	                        switch (aChar) {
	                        case '0':
	                        case '1':
	                        case '2':
	                        case '3':
	                        case '4':
	                        case '5':
	                        case '6':
	                        case '7':
	                        case '8':
	                        case '9':
	                            value = (value << 4) + aChar - '0';
	                            break;
	                        case 'a':
	                        case 'b':
	                        case 'c':
	                        case 'd':
	                        case 'e':
	                        case 'f':
	                            value = (value << 4) + 10 + aChar - 'a';
	                            break;
	                        case 'A':
	                        case 'B':
	                        case 'C':
	                        case 'D':
	                        case 'E':
	                        case 'F':
	                            value = (value << 4) + 10 + aChar - 'A';
	                            break;
	                        default:
	                            throw new IllegalArgumentException(
	                                    "Malformed   \\uxxxx   encoding.");
	                        }
	                    }
	                    outBuffer.append((char) value);
	                } else {
	                    if (aChar == 't')
	                        aChar = '\t';
	                    else if (aChar == 'r')
	                        aChar = '\r';
	                    else if (aChar == 'n')
	                        aChar = '\n';
	                    else if (aChar == 'f')
	                        aChar = '\f';
	                    outBuffer.append(aChar);
	                }
	            } else
	                outBuffer.append(aChar);
	        }
	        return outBuffer.toString();
	    }
	 

	
	public static void main(String args[]) throws IOException, SQLException
	{
		//String code="\u4e60\u5927\u5927,\u4f60\u770b\u770b\u51e4\u51f0\u536b\u89c6\u7684\"\u4e10\u5e2e\u9ed1\u5e55\"\u5427,\u8bb2\u5230\u64cd\u7eb5\u5b69\u5b50\u7684\u5730\u65b9,\u771f\u7684\u5f88\u5fc3\u75db!\u5f04\u6765\u7684\u5b69\u5b50\u4e00\u4e24\u5c81\u7684\u65f6\u5019\u5f04\u6b8b\u5e9f,\u786c\u751f\u751f\u6253\u65ad\u817f!\u6d41\u8113\u5356\u76f8\u8d8a\u597d,\u5b69\u5b50\u817f\u597d\u4e00\u4e9b\u4e86,\u5c31\u518d\u6572,\u770b\u4e86\u8fd9\u4e9b\u771f\u7684\u4e0d\u77e5\u9053\u4ec0\u4e48\u6ecb\u5473,\u60a8\u662f\u4e3b\u5e2d,\u53ea\u8981\u4e00\u53e5\u8bdd,\u5c31\u80fd\u6539\u53d8,\u4e3e\u624b\u4e4b\u52b3!";
		//System.out.println(unicodeToUtf8(code));
		instagram test = new instagram();
		test.craw();
	}

}
