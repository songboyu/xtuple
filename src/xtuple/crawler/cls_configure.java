package xtuple.crawler;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;

public class cls_configure 
{
	public String title_begin;
	public String title_end;
	public String date_begin;
	public String date_end;
	public String content_begin;
	public String content_end;
	public String outdate;
	
	public String source_link_begin;
	public String source_link_end;
	public String source_name_begin;
	public String source_name_end;
	
	public String column_begin;
	public String column_end;
	
	public String site_name;
	public String site_filter;
	
	
//comment	
	//comment_link
	public String comment_link_prefix;
	public String boardID_begin;
	public String boardID_end;
	public String comment_link_middle;
	public String threadID_begin;
	public String threadID_end;
	public String comment_link_suffix;
	//json link
	public String encode;
	public String comment_encode;
	public String pagesize;
	public String data_link_prefix;
	public String data_link_middle;
	public String pageID;
	public String data_link_suffix;
	
	
	public String joinNo;			//optional
	public String comNo;
	public String comment_object;	//optional,sub dir
	public String comment_array;
	public String comment_content;
	public String comment_time;
	
	public String haveComment;
	public String isJson;
	
	public String joinNoEnd;
	public String comNoEnd;
	public String comment_content_end;
	public String comment_time_end;
	
	public cls_configure(String filename)
	{
		try 
		{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"gbk"));
			
			title_begin = br.readLine();
			title_end = br.readLine();
			date_begin = br.readLine();
			date_end = br.readLine();
			content_begin = br.readLine();
			content_end = br.readLine();
			outdate = br.readLine();
			
			source_link_begin = br.readLine();
			source_link_end = br.readLine();
			source_name_begin = br.readLine();
			source_name_end = br.readLine();
			
			column_begin = br.readLine();
			column_end = br.readLine();
			
			site_name = br.readLine();
			site_filter = br.readLine();
			
			comment_link_prefix = br.readLine();
			boardID_begin = br.readLine();
			boardID_end = br.readLine();
			comment_link_middle = br.readLine();
			threadID_begin = br.readLine();
			threadID_end = br.readLine();
			comment_link_suffix = br.readLine();
			
			encode = br.readLine();
			comment_encode = br.readLine();
			pagesize = br.readLine();
			data_link_prefix = br.readLine();
			data_link_middle = br.readLine();
			pageID = br.readLine();
			data_link_suffix = br.readLine();
			
			
			joinNo = br.readLine();
			comNo = br.readLine();
			comment_object = br.readLine();
			comment_array = br.readLine();
			comment_content = br.readLine();
			comment_time = br.readLine();
			haveComment = br.readLine();
			isJson = br.readLine();
			
			joinNoEnd = br.readLine();
			comNoEnd = br.readLine();
			comment_content_end = br.readLine();
			comment_time_end = br.readLine();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
