package bbs.crawler;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;

import blog.crawler.blog_configure;

public class bbs_configure extends blog_configure{
	public String column_filter;
	
	public String last_modified_begin;
	public String last_modified_end;
	
	public String site_name;
	public String column_begin;
	public String column_end;
	
	public String reply_begin;
	public String reply_end;
	public String reply_nextpage_begin;
	public String reply_nextpage_end;
	
	public String clicknum_begin;
	public String clicknum_end;
	public String replynum_begin;
	public String replynum_end;
	
	public String reply_time_begin;
	public String reply_time_end;
	
//	public String beginpage;	//包含版块列表的网址
	public String lastpageFlag;
	public String pageExample;
	public String currentpageFlag;
	public String encode;
	public String needHost;
	public String basic;
	public String passclickandreplynum;
	public String getexistmode;
	public String lastpagenum_begin;
	public String lastpagenum_end;
	public String prevpagelinkmode;
	public String currentpagenum_begin;
	public String currentpagenum_end;
	public String forumid;
	public String replyer_begin;
	public String replyer_end;
	public String replytime_begin;
	public String replytime_end;

	private final int blog_conf_len = 16;
	int i;
	
	public bbs_configure(String filename)
	{
		super(filename);
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filename),"gbk"));
			
			for(i=0; i<blog_conf_len; i++)	//跳过在父类已读取的部分
				br.readLine();
			
			column_filter = br.readLine();
			last_modified_begin = br.readLine();
			last_modified_end = br.readLine();
			
			site_name = br.readLine();
			column_begin = br.readLine();
			column_end = br.readLine();
			
			reply_begin = br.readLine();
			reply_end = br.readLine();
			reply_nextpage_begin = br.readLine();
			reply_nextpage_end = br.readLine();
			clicknum_begin = br.readLine();
			clicknum_end = br.readLine();
			replynum_begin = br.readLine();
			replynum_end = br.readLine();
			reply_time_begin = br.readLine();
			reply_time_end = br.readLine();
			lastpageFlag = br.readLine();
			pageExample = br.readLine();
			currentpageFlag = br.readLine();
			encode = br.readLine();
			needHost = br.readLine();
			basic = br.readLine();
			passclickandreplynum = br.readLine();
			getexistmode = br.readLine();
			lastpagenum_begin = br.readLine();
			lastpagenum_end = br.readLine();
			prevpagelinkmode = br.readLine();
			currentpagenum_begin = br.readLine();
			currentpagenum_end = br.readLine();
			forumid = br.readLine();
			replyer_begin = br.readLine();
			replyer_end = br.readLine();
			replytime_begin = br.readLine();
			replytime_end = br.readLine();
			//isAjax = br.readLine();
		}catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
