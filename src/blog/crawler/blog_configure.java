package blog.crawler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

public class blog_configure 
{
	public String list_link_begin;
	public String list_link_end;
	public String article_link_begin;
	public String article_link_end;
	public String nextpage_link_begin;
	public String nextpage_link_end;
	public String title_begin;
	public String title_end;
	public String date_begin;
	public String date_end;
	public String author_begin;
	public String author_end;
	public String content_begin;
	public String content_end;
	public String outdate;
	public String article_filter;
	
	public String encode;
	public String list_time_begin;	//get time in article list
	public String list_time_end;
	
	public String isAjax;
	
	public blog_configure(String filename)
	{
		try
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"gbk"));
			
			list_link_begin = br.readLine();
			list_link_end = br.readLine();
			article_link_begin = br.readLine();
			article_link_end = br.readLine();
			nextpage_link_begin = br.readLine();
			nextpage_link_end = br.readLine();
			title_begin = br.readLine();
			title_end = br.readLine();
			date_begin = br.readLine();
			date_end = br.readLine();
			author_begin = br.readLine();
			author_end = br.readLine();
			content_begin = br.readLine();
			content_end = br.readLine();
			outdate = br.readLine();
			article_filter = br.readLine();	//����isarticle
			
			encode = br.readLine();
			list_time_begin = br.readLine();
			list_time_end = br.readLine();
			//---------------
			isAjax = br.readLine();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
