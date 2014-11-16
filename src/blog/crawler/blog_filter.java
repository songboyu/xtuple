package blog.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;

import common_function.common_function;

import xtuple.crawler.DateTransform;
import xtuple.crawler.cls_database;
import xtuple.crawler.cls_item;

public class blog_filter {
	private blog_configure config;
	private cls_database db = new cls_database();
	private static int articlenum = 1;
	
	
	public blog_filter(blog_configure config)
	{
		this.config = config;
	}
	
	public boolean check_homepage(cls_item blog)
	{
		boolean re = true;
		
		try{
		String s_listlink = common_function.getInfo(blog.html,config.list_link_begin,config.list_link_end);
		
		if (s_listlink != null)
		{
			blog.links.add(s_listlink);
			System.out.println("blog_list_link:" + s_listlink);
		}
		else
		{
			System.out.println("return at listlink");
			return false;
		}
		}catch (Exception e){
			e.printStackTrace();
		}
		
		return re;
	}
	
	public boolean check_articlelist(cls_item item)
	{
		boolean re = true;
		
		String s_articlelink;
		String s_nextpagelink;
		
		Pattern t_begin = Pattern.compile(config.article_link_begin, Pattern.DOTALL);
		Pattern t_end = Pattern.compile(config.article_link_end, Pattern.DOTALL);
		Matcher begin = t_begin.matcher(item.html);
		Matcher end = t_end.matcher(item.html);
		
		Pattern tt_begin = Pattern.compile(config.list_time_begin, Pattern.DOTALL);
		Pattern tt_end = Pattern.compile(config.list_time_end, Pattern.DOTALL);
		Matcher time_begin = tt_begin.matcher(item.html);
		Matcher time_end = tt_end.matcher(item.html);
		
		int list_link_begin = 0, list_link_end = 0;
		int list_time_begin = 0, list_time_end = 0;
		
		try{
		while (begin.find(list_link_end))
		{
			list_link_begin = begin.end();
			if (end.find(list_link_begin))
				list_link_end = end.start();
		
			s_articlelink = item.html.substring(list_link_begin,list_link_end);
			
			if (time_begin.find(list_link_end))
				list_time_begin = time_begin.end();
			if (time_end.find(list_time_begin))
				list_time_end = time_end.start();
			
			String time = item.html.substring(list_time_begin,list_time_end);
			time = common_function.getDate(time);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date dt = sdf.parse(time.trim());
			
			if (((new Date()).getTime() - dt.getTime()) / 3600000 > Integer.parseInt(config.outdate))
				return false;
			
			item.links.add(s_articlelink);
			System.out.println("blog_articlelink " + articlenum + ": "+ s_articlelink);
			articlenum++;
		}
		}catch(Exception e){
			e.printStackTrace();
		}
		if (list_link_begin == 0 | list_link_end == 0)
		{
			System.out.println("return at articlelinks");
			return false;
		}
		
		s_nextpagelink = common_function.getInfo(item.html,config.nextpage_link_begin,config.nextpage_link_end);
		if (s_nextpagelink != null)
		{
			item.links.add(s_nextpagelink);
			System.out.println("nextpagelink:" + s_nextpagelink);
		}
		
		return re;
	}
		
	
	
	public boolean check_article(cls_item item)	//analysis HTML and store
	{
		//ֻ�ڲ鵽���ڲ���ʱ�ŷ���true
		String s_title;
		String s_time;
		String s_content;
		String s_url = item.link;
		String s_author;
		
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
			String whichDate = d.date_trasform2(s_time);
			s_time = d.date_trasform(s_time);
			
			if (whichDate.length() > s_time.length())
				s_time = whichDate;
			
			String str_date = s_time;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try 
			{
				Date dt = sdf.parse(str_date.trim());
				if (((new Date()).getTime() - dt.getTime()) / 3600000 > Integer.parseInt(config.outdate))
				{
					System.out.println("Old Date:" + dt);
					return  true;
				}
				else
					System.out.println("Date:" + dt);
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
			s_content = StringEscapeUtils.unescapeHtml(s_content);
			s_content = common_function.replaceSingleQuotation(s_content);
			System.out.println("content:" + s_content);
		}
		else
		{
			System.out.println("return at content");
			return false;
		}
		
		s_author = common_function.getInfo(item.html,config.author_begin,config.author_end);
		if (s_author != null)
		{
			System.out.println("author:" + s_author);
		}
		else
		{
			System.out.println("return at author");
			return false;
		}
		
		s_author = s_author.replaceAll("<.+?>", "");
		
		int ok = db.update("insert into blog(blog_title,blog_time,blog_content,blog_url,blog_author) " +
				"values('" + s_title + "','" + s_time  + "','" + s_content + "','" + s_url + "','" + s_author + "')");
		db.closed();
		
		if (ok == 0)
		{
			try {
				BufferedWriter writer;
	
				writer = new BufferedWriter(new FileWriter(new File("InsertError_Links.txt"), true));
			
				
				String error = "";
				error = "Error link:" + s_url +"\r\n";
				error += "title:" + s_title + "\r\n";
				error += "content:" + s_content + "\r\n\r\n";
				writer.write(error);

				writer.close();
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return false;
		
	}
	
	public boolean isarticle(String link)
	{
		System.out.println(config.article_filter);
		System.out.println(link);
		Pattern pattern = Pattern.compile(config.article_filter);
		Matcher matcher = pattern.matcher(link);
		System.out.println(matcher.matches());
	  
		return matcher.matches();
	}
}
