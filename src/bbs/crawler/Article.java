package bbs.crawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.impl.client.DefaultHttpClient;

import blog.crawler.blog_download_new;

import xtuple.crawler.DateTransform;
import xtuple.crawler.cls_database;
import xtuple.crawler.cls_item;

import common_function.common_function;

/*
 * 帖子实体类
 * 包含一个帖子所有内容以及检验函数
 * 当帖子含有多页回复时，将自动获取所有回复页面并爬取所有回复
 * 回复内容在check_replys函数中写入数据库
 * 
 * @Author: HSS
 * @Modify: 2014-11-12
 */
public class Article {
	// 帖子实体（链接+内容）
	private cls_item item;
	// 配置信息
	private bbs_configure config;
	// HttpClient
	private DefaultHttpClient httpclient;
	// 存储点击和回复数的 HashTable
	private Hashtable<String, String> ReplyClickNum;
	// 数据库连接
	private cls_database db;
	// 时间转换
	private DateTransform d = new DateTransform();
	// 网页下载器
	public blog_download_new getter = new blog_download_new();
	// search_id
	public String post_batch = "";
	
	public int s_id;			// 帖子ID
	public String s_title;		// 帖子标题
	public String s_time;		// 发表时间
	public String s_content;	// 主贴内容
	public String s_author;		// 作者
	public String s_column;		// 板块
	public String last_reply_time; // 最后回复时间
	
	public int clicknum;		// 点击数
	public int replynum;		// 回复数
	

	/**
	 * article类构造函数
	 * @param item		帖子实体
	 * @param config	配置信息
	 * @param db		数据库连接
	 * @throws SQLException
	 */
	public Article(cls_item item, bbs_configure config, cls_database db) throws SQLException{
		this.item = item;
		this.config = config;
		this.db = db;
		
		ResultSet rs = db.getRs("select Max(title_id) from forum_title where website_name='" + config.site_name+ "'");
		rs.next();
		s_id = rs.getInt(1) + 1;
		System.out.println("Title_id: " + s_id);
	}

	/**
	 * 设置HttpClient
	 * @param httpclient
	 */
	public void set_httpclient(DefaultHttpClient httpclient){
		this.httpclient = httpclient;
	}

	/**
	 * 设置存储点击和回复数的 HashTable
	 * @param ReplyClickNum
	 */
	public void set_reply_click_num(Hashtable<String, String> ReplyClickNum){
		this.ReplyClickNum = ReplyClickNum;
	}

	/**
	 * 获取并检验帖子标题是否被获取到
	 * @return boolean
	 */
	public boolean check_title(){
		s_title = common_function.getInfo(item.html, config.title_begin,config.title_end);
		if (s_title != null) {
			s_title = common_function.purifyHtml(s_title);
			s_title = common_function.replaceSingleQuotation(s_title);
			s_title = common_function.getTextFromHtml(s_title);
			
			System.out.println("Title: " + s_title);
			return true;
		} else {
			System.out.println("Error: title not find");
			return false;
		}
	}

	/**
	 * 获取并检验发帖时间是否被获取到
	 * @return boolean
	 */
	public boolean check_post_date(){
		s_time = common_function.getInfo(item.html, config.date_begin,config.date_end);
		if (s_time != null) {
			String str_date = d.date_transform(s_time);
			System.out.println("Post_time: " + s_time);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				Date dt = sdf.parse(str_date.trim());
				long interval = ((new Date()).getTime() - dt.getTime()) / 3600000;
				if (interval > Integer.parseInt(config.outdate)) {
					System.out.println("["+interval+" hours ago] Long time from now");
					return false;
				} else{
					System.out.println("Date: " + dt);
					return true;
				}
			} catch (Exception e) {
				System.out.println(e.getMessage());
				return false;
			}
		} else {
			System.out.println("Error: post_date not find");
			return false;
		}
	}

	/**
	 * 获取并检验帖子内容是否被获取到
	 * @return boolean
	 */
	public boolean check_content(){
		s_content = common_function.getInfo(item.html, config.content_begin,config.content_end);
		if (s_content != null) {
			Pattern video = Pattern.compile("(<script type=\"text/javascript\">.+?</script>)|(<style>.+?</style>)",Pattern.DOTALL);
			Matcher find_video = video.matcher(s_content);
			String str_result = find_video.replaceAll("");
			s_content = common_function.purifyHtml(str_result);
			s_content = common_function.replaceSingleQuotation(s_content);
			s_content = common_function.getTextFromHtml(s_content);
			
			System.out.println("Content Check OK");
			return true;
		} else {
			System.out.println("Error: content not find");
			return false;
		}
	}

	/**
	 * 获取并检验作者是否被获取到
	 * @return boolean
	 */
	public boolean check_author(){
		s_author = common_function.getInfo(item.html, config.author_begin,config.author_end);
		if (s_author != null) {
			System.out.println("Author:" + s_author);
			return true;
		} else {
			System.out.println("Error: author not find");
			return false;
		}
	}

	/**
	 * 获取并检验板块是否被获取到
	 * @return boolean
	 */
	public boolean check_column(){
		s_column = common_function.getInfo(item.html, config.column_begin,config.column_end);
		if (s_column != null) {
			s_column = common_function.replaceSingleQuotation(s_column);
			System.out.println("Column:" + s_column);
			return true;
		} else {
			System.out.println("Error: column not find");
			return false;
		}
	}

	/**
	 * 获取并检验点击数是否被获取到
	 * @return boolean
	 */
	public boolean check_clicknum(){
		String s_clicknum = "";
		if (config.site_name.equals("文学城")) {
			s_clicknum = "0";
		} else if (config.passclickandreplynum.equals("true")) {
			s_clicknum = ReplyClickNum.get(item.link).split(",")[0];
		} else {
			s_clicknum = common_function.getInfo(item.html,config.clicknum_begin, config.clicknum_end);
		}
		if (s_clicknum != null) {
			clicknum = Integer.parseInt(s_clicknum);
			System.out.println("Clicknum:" + clicknum);
			return true;
		} else {
			System.out.println("Error: clicknum not find");
			return false;
		}
	}

	/**
	 * 获取并检验回复数是否被获取到
	 * @return boolean
	 */
	public boolean check_replynum(){
		String s_replynum = "";
		if (config.site_name.equals("文学城")) {
			s_replynum = "1";
		} else if (config.passclickandreplynum.equals("true")){
			s_replynum = ReplyClickNum.get(item.link).split(",")[1];
		} else {
			s_replynum = common_function.getInfo(item.html,config.replynum_begin, config.replynum_end);
		}
		if (s_replynum != null) {
			s_replynum = s_replynum.replace("\n", "");
			s_replynum = s_replynum.replace("\t", "");

			if (s_replynum.contains(";")) {
				s_replynum = s_replynum.split(";")[1];
			}
			replynum = Integer.parseInt(s_replynum);
			System.out.println("Replynum:" + replynum);
			return true;
		} else {
			System.out.println("Error: replynum not find");
			return false;
		}
	}
	
	/**
	 * 获取并检验所有回复
	 * @return boolean
	 */
	public boolean check_replys(){
		String s_reply = "";
		String replyer_tmp = "";
		String replytime_tmp = "";
		String replycontent_tmp = "";
		String nextpage = "";
		
		last_reply_time = s_time;

		cls_item currentpage = new cls_item();
		currentpage.link = item.link;
		currentpage.html = item.html;

		if (replynum > 0) {
			// 回复内容
			Pattern re_begin = Pattern.compile(config.reply_begin,Pattern.DOTALL);
			Pattern re_end = Pattern.compile(config.reply_end, Pattern.DOTALL);
			Matcher begin;
			Matcher end;
			// 回复列表下一页
			Pattern re_n_begin = Pattern.compile(config.reply_nextpage_begin,Pattern.DOTALL);
			Pattern re_n_end = Pattern.compile(config.reply_nextpage_end,Pattern.DOTALL);
			Matcher next_begin;
			Matcher next_end;
			// 回复者
			Pattern replyer_begin = Pattern.compile(config.replyer_begin,Pattern.DOTALL);
			Pattern replyer_end = Pattern.compile(config.replyer_end,Pattern.DOTALL);
			Matcher m_replyer_begin;
			Matcher m_replyer_end;
			// 回复时间
			Pattern replytime_begin = Pattern.compile(config.replytime_begin,Pattern.DOTALL);
			Pattern replytime_end = Pattern.compile(config.replytime_end,Pattern.DOTALL);
			Matcher m_replytime_begin;
			Matcher m_replytime_end;

			int reply_begin = 0, reply_end = 0;

			try {
				// 遍历每页回复
				while (true){
					int reply_nextpage_begin = -1;
					int reply_nextpage_end = -1;
					
					int last_begin = 0;
					int last_end = 0;

					int replyer_begin_index = 0;
					int replyer_end_index = 0;
					
					int replytime_begin_index = 0;
					int replytime_end_index = 0;

					int firstpage = 0;
					int first = 0;

					m_replyer_begin = replyer_begin.matcher(currentpage.html);
					m_replyer_end = replyer_end.matcher(currentpage.html);

					m_replytime_begin = replytime_begin.matcher(currentpage.html);
					m_replytime_end = replytime_end.matcher(currentpage.html);

					begin = re_begin.matcher(currentpage.html);
					end = re_end.matcher(currentpage.html);

					next_begin = re_n_begin.matcher(currentpage.html);
					next_end = re_n_end.matcher(currentpage.html);

					//遍历某一页中每一个回复
					while (m_replyer_begin.find(replyer_end_index)) {
						first = replyer_end_index;
						replyer_begin_index = m_replyer_begin.end();

						// 获取回复者
						if (m_replyer_end.find(replyer_begin_index)){
							replyer_end_index = m_replyer_end.start();
						}
						replyer_tmp = currentpage.html.substring(replyer_begin_index, replyer_end_index);
						// 获取回复时间
						if (m_replytime_begin.find(replyer_end_index)) {
							replytime_begin_index = m_replytime_begin.end();
							if (m_replytime_end.find(replytime_begin_index))
								replytime_end_index = m_replytime_end.start();
						}
						replytime_tmp = currentpage.html.substring(replytime_begin_index, replytime_end_index);
						replytime_tmp = d.date_transform(replytime_tmp);

						System.out.println("Reply Info: "+replytime_tmp+" "+currentpage.link+"  "+replyer_tmp);

						// 获取回复内容
						if (begin.find(replyer_end_index)) {
							reply_begin = begin.end();
							if (end.find(reply_begin))
								reply_end = end.start();
						}
						replycontent_tmp = currentpage.html.substring(reply_begin, reply_end);
						replycontent_tmp = common_function.purifyHtml(replycontent_tmp);
						replycontent_tmp = common_function.getTextFromHtml(replycontent_tmp);

						if (!config.site_name.equals("文学城"))
							if (first == 0 && firstpage == 0)
								continue;
						firstpage++;
						// 将每一条回复插入到数据库 forum_title_reply
						db.update("insert into forum_title_reply(website_id,website_name,title_id,replyer,content,re_time,search_id) values('"
								+ config.forumid + "','"
								+ config.site_name + "','"
								+ s_id + "','"
								+ replyer_tmp + "','"
								+ replycontent_tmp + "','"
								+ replytime_tmp + "','" 
								+ post_batch + "')");

					}
					//////////////////   跳转到回复列表下一页  ////////////////////   
					if (next_begin.find()
							&& !config.site_name.equals("欧浪论坛")
							&& !config.site_name.equals("文学城")) {
						// ==================== 如果有下一页 ====================
						reply_nextpage_begin = next_begin.end();
						if (next_end.find(reply_nextpage_begin))
							reply_nextpage_end = next_end.start();

						nextpage = currentpage.html.substring(reply_nextpage_begin, reply_nextpage_end);
						nextpage = nextpage.replace("&amp;", "&");

						if (config.needHost.equals("true")
								&& !config.site_name.equals("温哥华巅峰")
								&& !config.site_name.equals("欧浪论坛")) {
							String host = common_function.getHost(item.link);
							nextpage = host + nextpage;
						}

						currentpage = getter.download(httpclient, nextpage,config.encode);
						reply_begin = reply_end = 0;
					} else {
						// ==================== 如果到达最后一页 ====================
						// 最后一个回复时间
						Pattern la_begin = Pattern.compile(config.reply_time_begin, Pattern.DOTALL);
						Pattern la_end = Pattern.compile(config.reply_time_end, Pattern.DOTALL);
						
						begin = la_begin.matcher(currentpage.html);
						end = la_end.matcher(currentpage.html);
						
						if (begin.find(first)) {
							last_begin = begin.end();
							if (end.find(last_begin))
								last_end = end.start();
						}
						if (last_begin != 0 && last_end != 0) {
							last_reply_time = currentpage.html.substring(
									last_begin, last_end);
							last_reply_time = d.date_transform(last_reply_time);
						} else {
							last_reply_time = s_time;
							s_reply = "这个帖子还没有人回复";
						}
						System.out.println("Last_reply_time: " + last_reply_time);
						break;
					}
				} 
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (reply_begin == 0 || reply_end == 0) {
				System.out.println("Error: No reply at " + currentpage.link);
				return false;
			}
			return true;
		} else {
			last_reply_time = s_time;
			s_reply += "这个帖子还没有人回复";
			return true;
		}
	}
}
