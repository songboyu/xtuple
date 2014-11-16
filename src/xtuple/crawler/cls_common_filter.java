package xtuple.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import common_function.common_function;

import news_comment.crawler.add_task;
import news_comment.crawler.comment_item;

public class cls_common_filter implements cls_filter
{

	private cls_configure config;
	private cls_database db = new cls_database();
	
	
	public cls_common_filter(cls_configure config)
	{
		this.config = config;
	}
	
	public boolean check(cls_item item,BlockingQueue<comment_item> comment_tasks)	//analysis HTML and store
	{
		boolean re = true;
		String s_title;
		String s_time;
		String s_content;
		
		String s_source_link;
		String s_source_name;
		
		String s_column;
		String s_site = config.site_name;
		String s_link = item.link;
		
		String s_commentID;
		
		System.out.println(s_link);
		s_title = common_function.getInfo(item.html,config.title_begin,config.title_end);
		if (s_title != null)
		{
			System.out.println("title:" + s_title);
		}
		else
		{
			System.out.println("return at title");
			return false;
		}
		
		s_time = common_function.getInfo(item.html,config.date_begin,config.date_end);
		if (s_time != null)
		{
			
			//���ڸ�ʽ����ʽ��2012-01-01 00:00:00��2012��01��01��00:00��
			//�ո��λ��Ҳ��һ��date_tranform�Ѹ�������ͳһת���ɵ�һ����ʽ
			DateTransform d = new DateTransform();
			s_time = d.date_trasform(s_time);
			
			String str_date = s_time;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try 
			{
				Date dt = sdf.parse(str_date.trim());
				if (((new Date()).getTime() - dt.getTime()) / 3600000 > Integer.parseInt(config.outdate))
				{
					System.out.println("Old Date:" + dt);
					return false;
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
		}
		else
		{
			System.out.println("return at date");
			return false;
		}

		String str = common_function.getInfo(item.html,config.content_begin,config.content_end);
		if (str != null)
		{
			s_content = str;
			
			Pattern video = Pattern.compile("(<script.*?>.+?</script>)|(<style.*?>.+?</style>)", Pattern.DOTALL);
			Matcher find_video = video.matcher(str);
			String str_result = find_video.replaceAll("");
			
//			Pattern pattern = Pattern.compile("<[^(p|b|a|(img)|(br /))]+?>");
//			Pattern pattern = Pattern.compile("(<^[pba].*?>)|(</^[pba]>)|^(<[/]{0,1}img.*?>)|^(<[/]{0,1}strong>)|^(</ br>)", Pattern.DOTALL);
			Pattern pattern = Pattern.compile("(</?div.*?>)|(</?iframe.*?>)");
			Matcher matcher = pattern.matcher(str_result);
			str_result = matcher.replaceAll("");
			
			s_content = str_result;
			System.out.println("content:" + s_content);
		}
		else
		{
			System.out.println("return at content");
			return false;
		}
		
		//------
		s_source_link = common_function.getInfo(item.html,config.source_link_begin,config.source_link_end);
		if (s_source_link != null)
		{
			System.out.println("link:" + s_source_link);
		}
		else
		{
			System.out.println("source link is empty!");
			s_source_link = "这个新闻没有来源链接";
//			return false;
		}
		//-------
		s_source_name = common_function.getInfo(item.html,config.source_name_begin,config.source_name_end);
		
		if (s_source_name != null)
		{
			System.out.println("name:" + s_source_name);
		}
		else
		{
			System.out.println("return at name");
			return false;
		}
		
		
		s_column = common_function.getInfo(item.html,config.column_begin,config.column_end);
			
		if (s_column != null)
		{
			System.out.println("column:" + s_column);
		}
		else
		{
			System.out.println("column--" + config.column_begin + " " + config.column_end);
			System.out.println("return at colunm");
			return false;
		}
		
		System.out.println("Insert");
//		s_time = s_time.trim();
		s_time = s_time.substring(0,"yyyy-mm-dd hh:mm:ss".length());
		
		s_content = common_function.replaceSingleQuotation(s_content);
		s_content = StringEscapeUtils.unescapeHtml(s_content);
		
		int success  = 0;
		success = db.update("insert into news(news_title,news_time,news_source_link,news_source_name,news_content,news_column,news_site,news_link) " +
				"values('" + s_title + "','" + s_time + "','" + s_source_link + "','" + s_source_name + "','" + s_content + "','" + s_column + "','" + s_site + "','" + s_link + "')");		

		db.closed();
		
		if (config.haveComment.equals("false"))
			return false;
		
//comment-------------------------------------------
//--------------------------------------------------
//--------------------------------------------------
		String board_ID = "";
		String thread_ID = "";
		if (success != 0)	//������ȡ�ɹ��������Ӧ���۵���ȡ����
		{	
			comment_item newtask = new comment_item();
			newtask.news_link = item.link;
			newtask.news_comment_link = "?";
			newtask.config = config;
			newtask.exist = false;
			
		//	System.out.println("boardID_begin:" + config.boardID_begin);
		//	System.out.println("boardID_end:" + config.boardID_end);
			board_ID = common_function.getInfo(item.html,config.boardID_begin,config.boardID_end);
			newtask.boardID = board_ID;
			if (board_ID != null)
			{
				System.out.println("boardID:" + board_ID);
			}
			else
			{
				System.out.println("return at boardID");	//the comment of this new have been shut down
				newtask.news_comment_link = "void";
				
				add_task a = new add_task(newtask,comment_tasks);	//�������
				new Thread(a).start();
				
				return false;
			}
			
			thread_ID = common_function.getInfo(item.html,config.threadID_begin,config.threadID_end);
			newtask.threadID = thread_ID;
			
			newtask.news_comment_link = config.comment_link_prefix + board_ID + config.comment_link_middle + thread_ID + config.comment_link_suffix;
			
			add_task a = new add_task(newtask,comment_tasks);	//�������
			new Thread(a).start();
		}
		else
		{
			try {
				BufferedWriter writer;
	
				writer = new BufferedWriter(new FileWriter(new File("d:\\InsertError_Links.txt"), true));
			
				
				String error = "";
				error = "Error link:" + item.link +"\r\n";
				error += "colnum:" + s_column + "\r\n";
				error += "content:" + s_content + "\r\n\r\n";
				writer.write(error);

				writer.close();
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return re;
	}
	
	public void checkExist(cls_item item,BlockingQueue<comment_item> comment_tasks)
	{
		if (config.haveComment.equals("false"))
			return;
		
		String board_ID = "";
		String thread_ID = "";
		
		try {
			comment_item newtask = new comment_item();
			newtask.news_link = item.link;
			newtask.news_comment_link = "?";
			newtask.config = config;
			newtask.exist = true;
			
			board_ID = common_function.getInfo(item.html,config.boardID_begin,config.boardID_end);
			newtask.boardID = board_ID;
			if (board_ID != null)
			{
				System.out.println("boardID:" + board_ID);
			}
			else
			{
				System.out.println("return at boardID");	//the comment of this new have been shut down
				newtask.news_comment_link = "void";
				
				add_task a = new add_task(newtask,comment_tasks);	//�������
				new Thread(a).start();
				
				return;
			}
			
			thread_ID = common_function.getInfo(item.html,config.threadID_begin,config.threadID_end);
			newtask.threadID = thread_ID;
			
			newtask.news_comment_link = config.comment_link_prefix + board_ID + config.comment_link_middle + thread_ID + config.comment_link_suffix;
			
			add_task a = new add_task(newtask,comment_tasks);	//�������
			new Thread(a).start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean todo(String link)
	{
		Pattern pattern = Pattern.compile(config.site_filter);
		Matcher matcher = pattern.matcher(link);
//		System.out.println(matcher.matches());
	  
		return matcher.matches();
	}
	
}
