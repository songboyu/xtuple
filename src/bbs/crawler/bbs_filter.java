package bbs.crawler;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import sun.misc.BASE64Decoder;
import xtuple.crawler.DateTransform;
import xtuple.crawler.cls_database;
import xtuple.crawler.cls_item;
import blog.crawler.blog_download;
import blog.crawler.blog_download_new;

import common_function.common_function;

public class bbs_filter {
	private bbs_configure config;
	private cls_database db = new cls_database();

	private static AtomicInteger article_num = new AtomicInteger(0);
	private static AtomicInteger error_number = new AtomicInteger(0);

	public static int page_number = 0;
	public static int postlistpagenum = 0;
	public static int update_number = 0;
	public static int update_request_number = 0;

	public blog_download singe_thread_getter = new blog_download();
	public blog_download_new getter = new blog_download_new();

	public Hashtable<String, String> ReplyClickNum = new Hashtable<String, String>();

	DateTransform d = new DateTransform();

	ScriptEngineManager js_eng_manager;
	ScriptEngine js_engine;

	private static int columnnum = 1;
	static int articlenum = 1;

	public String post_batch = "";

	public bbs_filter(bbs_configure config) {
		this.config = config;
		js_eng_manager = new ScriptEngineManager();
		js_engine = js_eng_manager.getEngineByName("jav8");
	}

	public boolean check_beginpage(cls_item item) {
		boolean re = true;
		String s_columnlink;
		Pattern list_begin = Pattern.compile(config.list_link_begin,
				Pattern.DOTALL);
		Pattern list_end = Pattern
				.compile(config.list_link_end, Pattern.DOTALL);
		Matcher begin = list_begin.matcher(item.html);
		Matcher end = list_end.matcher(item.html);

		int list_link_begin = 0, list_link_end = 0;
		try {
			while (begin.find(list_link_end)) {
				// System.out.println("begin");
				list_link_begin = begin.end();
				if (end.find(list_link_begin))
					list_link_end = end.start();
				// System.out.println("end");
				s_columnlink = item.html.substring(list_link_begin,
						list_link_end);
				// Have Host?
				//
				if (iscolumn(s_columnlink)) {
					if (config.needHost.equals("true")) {
						String host = common_function.getHost(item.link);
						s_columnlink = host + s_columnlink;
					}
					item.links.add(s_columnlink);
					System.out.println("columnlink " + columnnum + ": "
							+ s_columnlink);
					columnnum++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (list_link_begin == 0 | list_link_end == 0) {
			System.out.println(config.list_link_begin);
			System.out.println("return at beginpage");
			// System.out.println(item.html);
			return false;
		}
		// pageNo.incrementAndGet();
		return re;
	}

	public void debug_write(String s) {
		FileWriter fw;
		try {
			fw = new FileWriter("Debug.log");
			fw.write(s, 0, s.length());
			fw.flush();
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean check_articlelist(cls_item item, long old_time) {
		page_number++;
		postlistpagenum++;
		boolean re = true;

		Date lastArticleDate = null;
		String s_articlelink;
		String s_nextpagelink;

		Pattern t_begin = Pattern.compile(config.article_link_begin,
				Pattern.DOTALL);
		Pattern t_end = Pattern
				.compile(config.article_link_end, Pattern.DOTALL);
		Matcher begin = t_begin.matcher(item.html);
		Matcher end = t_end.matcher(item.html);

		Matcher m_reply_begin = null;
		Matcher m_reply_end = null;
		Matcher m_click_begin = null;
		Matcher m_click_end = null;
		if (config.passclickandreplynum.equals("true"))
			// if(config.site_name.equals("多维论坛")||config.site_name.equals("阿波罗论坛")||config.site_name.equals("起点论坛"))
		{
			// replynum
			Pattern reply_begin = Pattern.compile(config.replynum_begin,
					Pattern.DOTALL);
			Pattern reply_end = Pattern.compile(config.replynum_end,
					Pattern.DOTALL);
			m_reply_begin = reply_begin.matcher(item.html);
			m_reply_end = reply_end.matcher(item.html);
			// clicknum
			Pattern click_begin = Pattern.compile(config.clicknum_begin,
					Pattern.DOTALL);
			Pattern click_end = Pattern.compile(config.clicknum_end,
					Pattern.DOTALL);
			m_click_begin = click_begin.matcher(item.html);
			m_click_end = click_end.matcher(item.html);
		}

		Pattern d_begin = Pattern.compile(config.last_modified_begin,
				Pattern.DOTALL);
		Pattern d_end = Pattern.compile(config.last_modified_end,
				Pattern.DOTALL);
		Matcher m_begin = d_begin.matcher(item.html);
		Matcher m_end = d_end.matcher(item.html);

		s_nextpagelink = common_function.getInfo(item.html,
				config.nextpage_link_begin, config.nextpage_link_end);
		if (s_nextpagelink != null) {
			page_number++;
			// Have Host?
			if (config.site_name.equals("乌有之乡")) {
				s_nextpagelink = "http://ap345.com/a/wuyouzhixiang/"
						+ s_nextpagelink;
			} else if (config.site_name.equals("欧浪论坛")) {
				s_nextpagelink = common_function.getHost(item.link)
						+ "ShowForum.asp"
						+ get_nextpagelink_from_js(s_nextpagelink);
			} else if (config.needHost.equals("true")
					&& !config.site_name.equals("温哥华巅峰")
					&& !config.site_name.equals("倍可亲论坛")) {
				String host = common_function.getHost(item.link);
				s_nextpagelink = host + s_nextpagelink;
			}
			s_nextpagelink = s_nextpagelink.replace("&amp;", "&");
			item.links.add(s_nextpagelink);

			System.out.println("nextpagelink:" + s_nextpagelink);
		} else
			System.out.println("No Nextpage!!!");

		int list_link_begin = 0, list_link_end = 0;
		int clicknum_begin = 0, clicknum_end = 0;
		int replynum_begin = 0, replynum_end = 0;
		int date_begin = 0, date_end = 0;

		try {
			while (begin.find(list_link_end)) {

				list_link_begin = begin.end();
				if (end.find(list_link_begin))
					list_link_end = end.start();
				if (config.passclickandreplynum.equals("true"))
					// if(config.site_name.equals("多维论坛")||config.site_name.equals("阿波罗论坛")||config.site_name.equals("起点论坛"))
				{
					if (m_reply_begin.find(list_link_end))
						replynum_begin = m_reply_begin.end();
					try {
						if (m_reply_end.find(replynum_begin))
							replynum_end = m_reply_end.start();
					} catch (IndexOutOfBoundsException e) {
						;
					}

					if (m_click_begin.find(list_link_end))
						clicknum_begin = m_click_begin.end();
					try {
						if (m_click_end.find(clicknum_begin))
							clicknum_end = m_click_end.start();
					} catch (IndexOutOfBoundsException e) {
						;
					}
				}

				if (m_begin.find(list_link_end))
					date_begin = m_begin.end();
				try {
					if (m_end.find(date_begin))
						date_end = m_end.start();
				} catch (IndexOutOfBoundsException e) {
					;
				}

				if (date_begin != 0 && date_end != 0) {
					String last_time = (item.html.substring(date_begin,
							date_end));

					System.out.println("last_modifiedtime:" + last_time
							+ " end");

					last_time = d.date_transform(last_time);
					String str_date = last_time;
					// System.out.println(last_time);
					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyy-MM-dd HH:mm:ss");
					try {
						Date dt = sdf.parse(str_date);
						if (dt.getTime() < old_time) {
							System.out.println("Old Date:" + dt);
							System.out.println("Old Date1:" + dt.getTime());
							System.out.println("Old Date1:" + old_time);
							System.out.println("LastArticleDate"
									+ lastArticleDate);
							// irregular sorted post
							if (lastArticleDate == null
									|| lastArticleDate.getTime() - dt.getTime() > 120 * 1000
									|| dt.getTime() - lastArticleDate.getTime() > 120 * 1000) {

								// re = false;
								lastArticleDate = dt;
								continue;
							} else {
								lastArticleDate = dt;
								re = false;
								break;
							}
						} else
							System.out.println("Last Modified Date:" + dt);
						lastArticleDate = dt;
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (lastArticleDate == null) {
							System.out
							.println("-------------------------------------------------------------");
							System.out
							.println("Attention!    Something wrong happened to lastArticleDate!!!!");
							System.out
							.println("-------------------------------------------------------------");
						}
					}

				} else {
					System.out.println("return at last modified date");
					return false;
				}

				s_articlelink = item.html.substring(list_link_begin,
						list_link_end);
				if (config.needHost.equals("true") && config.site_name.equals("网易教育论坛")) {
					String host = common_function.getHost(item.link);
					s_articlelink = host + "bbs/jiaoyu/"+s_articlelink;
				}
				else if (config.needHost.equals("true") && config.site_name.equals("网易八卦论坛")) {
					String host = common_function.getHost(item.link);
					s_articlelink = host + "bbs/bagua/"+s_articlelink;
				}
				else if (config.needHost.equals("true") && config.site_name.equals("网易篮球论坛")) {
					String host = common_function.getHost(item.link);
					s_articlelink = host + "bbs/basketball/"+s_articlelink;
				}
				else if (config.needHost.equals("true")) {
					String host = common_function.getHost(item.link);
					s_articlelink = host + s_articlelink;
				}

				if (config.passclickandreplynum.equals("true"))
					// if(config.site_name.equals("多维论坛")||config.site_name.equals("阿波罗论坛")||config.site_name.equals("起点论坛"))
				{
					if (config.site_name.equals("多维论坛")) {
						// http://forum.dwnews.com/threadshow.php?tid=117584
						s_articlelink = s_articlelink.substring(0, 50);
					}
					System.out.println(clicknum_end);
					String s_clicknum = item.html.substring(clicknum_begin,
							clicknum_end);
					s_clicknum = s_clicknum.replace(",", "");
					String s_replynum = item.html.substring(replynum_begin,
							replynum_end);
					s_replynum = s_replynum.replace(",", "");

					String insert_value = s_clicknum + "," + s_replynum;
					this.ReplyClickNum.put(s_articlelink, insert_value);
				}

				s_articlelink = s_articlelink.replace("&amp;", "&");
				if (config.site_name.equals("温哥华巅峰")) {
					do {
						s_articlelink = getencodeurl(s_articlelink);
					} while (s_articlelink == null);
					if (config.needHost.equals("true")) {
						String host = common_function.getHost(item.link);
						s_articlelink = host + s_articlelink;
					}
				}

				item.links.add(s_articlelink);
				System.out.println("bbs_articlelink " + articlenum + ": "
						+ s_articlelink);
				articlenum++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (list_link_begin == 0 | list_link_end == 0) {
			System.out.println("return at articlelinks");
			return false;
		}

		// pageNo.incrementAndGet();
		return re;
	}

	public static String getFromBASE64(String s) {
		if (s == null)
			return null;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			byte[] b = decoder.decodeBuffer(s);
			return new String(b);
		} catch (Exception e) {
			return null;
		}
	}

	public String get_nextpagelink_from_js(String js_function) {
		Object result = null;
		String nextpagelink = "";
		String Js_Base64 = "ZnVuY3Rpb24gU2hvd1BhZ2UoYSxiLGMpe3ZhciBlLGQ9IiI7Zm9yKFBhZ2VJbmRleDE9Yi0xLD"
				+ "A9PVBhZ2VJbmRleDEmJihQYWdlSW5kZXgxPTEpLFBhZ2VJbmRleDI9YisxLFBhZ2VJbmRleDI9PWErMSYmKFB"
				+ "hZ2VJbmRleDI9YiksZD0iPGEgaWQ9J3ByZScgaHJlZj0nP1BhZ2VJbmRleD0iK1BhZ2VJbmRleDErIiYiK2Mr"
				+ "Iic+PGltZyBzcmM9J2ltYWdlcy9wcmUuanBnJyBib3JkZXI9JzAnIGFsaWduPSdhYnNtaWRkbGUnLz48L2E+J"
				+ "m5ic3A7Jm5ic3A7IixQYWdlTG9uZz02PmI/MTEtYjo2PmEtYj8xMC0oYS1iKTo1LGU9MTthPj1lO2UrKykoYi"
				+ "tQYWdlTG9uZz5lJiZlPmItUGFnZUxvbmd8fDE9PWV8fGU9PWEpJiYoZCs9Yj09ZT8iPGIgY2xhc3M9dC15PiI"
				+ "rZSsiPC9iPiZuYnNwOyI6IjxhIGNsYXNzPXBhZ2VOdW0gaHJlZj0/UGFnZUluZGV4PSIrZSsiJiIrYysiPiIr"
				+ "ZSsiPC9hPiZuYnNwOyIpO3JldHVybiBkKz0iPGEgaWQ9J25leHQnIGhyZWY9Jz9QYWdlSW5kZXg9IitQYWdlS"
				+ "W5kZXgyKyImIitjKyInPjxpbWcgc3JjPSdpbWFnZXMvbmV4dC5qcGcnIGJvcmRlcj0nMCcgYWxpZ249J2Fic2"
				+ "1pZGRsZScvPjwvYT4mbmJzcDsmbmJzcDsifQ==";
		try {
			String js_code = getFromBASE64(Js_Base64);
			js_code += js_function;
			result = js_engine.eval(js_code);
			Pattern p_next = Pattern.compile("id='next' href='(\\S.+?)'");
			Matcher m_next = p_next.matcher(result.toString());
			if (m_next.find()) {
				nextpagelink = m_next.group(1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return nextpagelink;
		}
	}

	public String getencodeurl(String old_url) throws ScriptException,
	FileNotFoundException {
		Object result = null;
		try {
			blog_download_new my_getter = new blog_download_new();

			DefaultHttpClient my_tempclient = new DefaultHttpClient();
			my_tempclient.getParams().setParameter(
					CoreConnectionPNames.SO_TIMEOUT, 5 * 1000);
			my_tempclient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 2 * 1000);

			cls_item my_item = my_getter
					.download(my_tempclient, old_url, "gbk");
			System.out
			.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
			System.out.println(my_item.html);
			System.out
			.println("+++++++++++++++++++++++++++++++++++++++++++++++++");
			my_item.html = common_function.getInfo(my_item.html,
					"<script type=\"text/javascript\">", "</script>");
			my_item.html = my_item.html.replace("-> ", ".");

			ScriptEngineManager manager = new ScriptEngineManager();
			ScriptEngine engine = manager.getEngineByName("jav8");
			engine.eval("var window = new Object(), location = new Object();");
			engine.eval("var url, get_url = function (x) {url = x};");
			engine.eval("location.assign = get_url, location.replace = get_url;");

			// engine.eval("if(age>=18){println('Old enough to vote!');}else{println('Back to school!');}");//解析
			// JavaScript 脚本,对脚本表达式进行求值
			// engine.eval(new
			// FileReader("check.js"));//eval()函数返回执行脚本后所返回的值，默认情况下，将返回上次执行的表达式的值
			engine.eval(my_item.html);
			engine.eval("var mxy1111;");
			result = engine
					.eval("mxy1111 = (function(){return url || location.href || location})()");
			// Object result = engine.eval("(function () {return url;})()");

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result.toString();
		}

	}

	public boolean check_exist(cls_item item, DefaultHttpClient httpclient) {
		if (config.basic.equals("true"))
			return true;
		boolean re = true;
		String url = item.link;
		String ji_lu;
		int id = 0;
		int old_replynum = 0;
		String old_modified_time = "1970-01-01 01:01:01";
		String old_reply = "";
		Date old_dt = null;
		int clicknum = 0, replynum = 0;
		String s_clicknum = null, s_replynum = null;
		String title_id = null;
		String author = null;
		String title = null;
		SimpleDateFormat sdf = null;

		ResultSet rs;
		ji_lu = common_function.getInfo(item.html, config.date_begin,
				config.date_end);
		try {

			rs = db.getRs("select * from forum_title where url='" + item.link
					+ "'");
			rs.next();
			id = rs.getInt("id");
			title_id = rs.getString("title_id");
			old_replynum = rs.getInt("reply_num");
			old_modified_time = rs.getString("last_re_time");
			old_reply = rs.getString("content");
			author = rs.getString("author");
			title = rs.getString("title");

			sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				old_dt = sdf.parse(old_modified_time.trim());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// clicknum------------------------------------
			if (config.site_name.equals("文学城")) {
				s_clicknum = "0";
			} else if (config.passclickandreplynum.equals("true"))
				// else
				// if(config.site_name.equals("多维论坛")||config.site_name.equals("阿波罗论坛")||config.site_name.equals("起点论坛"))
			{
				s_clicknum = ReplyClickNum.get(url).split(",")[0];
			} else {
				s_clicknum = common_function.getInfo(item.html,
						config.clicknum_begin, config.clicknum_end);
			}
			if (s_clicknum != null) {
				clicknum = Integer.parseInt(s_clicknum);
				System.out.println("clicknum:" + clicknum);
			} else {
				System.out.println("return at clicknum");
				return false;
			}

			// replynum------------------------------------------
			if (config.site_name.equals("文学城")) {
				s_replynum = "0";
			} else if (config.passclickandreplynum.equals("true"))
				// else
				// if(config.site_name.equals("多维论坛")||config.site_name.equals("阿波罗论坛")||config.site_name.equals("起点论坛"))
			{
				s_replynum = ReplyClickNum.get(url).split(",")[1];
			} else {
				s_replynum = common_function.getInfo(item.html,
						config.replynum_begin, config.replynum_end);
			}

			if (s_replynum != null) {
				s_replynum = s_replynum.replace("\n", "");
				s_replynum = s_replynum.replace("\t", "");
				if (s_replynum.contains(";")) {
					s_replynum = s_replynum.split(";")[1];
				}
				replynum = Integer.parseInt(s_replynum);
				System.out.println("replynum:" + replynum);
			} else {
				System.out.println("return at replynum");
				return false;
			}
			// -------------------------------------------------------------------------------------------------------------------------修改于20140728
			if (old_replynum == replynum) {
				System.out.println("No need to update!");
				// 更新forum_title表
				String sql = "update forum_title set click_num='" + clicknum
						+ "',reply_num='" + replynum + "',last_re_time='"
						+ old_modified_time + "' where id='" + id + "'";
				// int complete = db.update(sql);
				int complete = db.update(sql);
				// necessary for update
				// int ok = db
				// .update("insert into forum_title(website_id,website_name,title_id,title,url,click_num,content,reply_num,author,last_re_time,time,search_id)"
				// + "values('"
				// + config.forumid
				// + "','"
				// + config.site_name
				// + "','"
				// + title_id
				// + "','"
				// + title
				// + "','"
				// + url
				// + "','"
				// + clicknum
				// + "','"
				// + old_reply
				// + "','"
				// + replynum
				// + "','"
				// + author
				// + "','"
				// + old_modified_time+ "','" +ji_lu+ "','" + post_batch +
				// "')");
				//
				rs.close();
				db.closed();
				// --------------------------------------------------------------------------------------------------------------------------修改结束
				return true;
			}
			rs.close();
			db.closed();
			// stm.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// con.close();
		}

		int pagesize = 100; // config
		int old_pagenum = (old_replynum + pagesize - 1) / pagesize;
		int new_pagenum = (replynum + pagesize - 1) / pagesize;
		int pagenum = new_pagenum - old_pagenum;

		List<String> temp = new LinkedList<String>();
		String lastpage_link = null;

		Pattern pattern2 = Pattern.compile(config.currentpageFlag); // config

		Pattern re_begin = Pattern.compile(config.reply_begin, Pattern.DOTALL);
		Pattern re_end = Pattern.compile(config.reply_end, Pattern.DOTALL);

		Pattern da_begin = Pattern.compile(config.reply_time_begin,
				Pattern.DOTALL);
		Pattern da_end = Pattern.compile(config.reply_time_end, Pattern.DOTALL);

		Pattern replyer_begin = Pattern.compile(config.replyer_begin,
				Pattern.DOTALL);
		Pattern replyer_end = Pattern.compile(config.replyer_end,
				Pattern.DOTALL);

		cls_item currentpage = new cls_item();
		currentpage.html = item.html;
		currentpage.link = item.link;

		boolean last_page = true;

		// lastpage
		Pattern pattern = Pattern.compile(config.lastpageFlag); // config
		Matcher matcher = pattern.matcher(item.html);

		// lastpage
		Pattern pattern_begin = Pattern.compile(config.lastpagenum_begin); // config
		Matcher matcher_begin = pattern_begin.matcher(item.html);

		if (config.getexistmode.equals("1") && matcher_begin.find())
			// if(config.site_name.equals("多维论坛")||config.site_name.equals("阿波罗论坛"))
		{
			int lastpagenumbeginindex = matcher_begin.end();
			String LastPageNum = null;

			Pattern pattern_end = Pattern.compile(config.lastpagenum_end);
			Matcher matcher_end = pattern_end.matcher(item.html);
			if (matcher_end.find(lastpagenumbeginindex))
				LastPageNum = currentpage.html.substring(lastpagenumbeginindex,
						matcher_end.start());
			else
				return true;
			if (config.site_name.equals("多维论坛")
					|| config.site_name.equals("迷米香港")) {
				lastpage_link = currentpage.link + "&page=";
			}
			lastpage_link = lastpage_link + LastPageNum;
			lastpage_link = lastpage_link.replace("&amp;", "&");
			do {
				currentpage = getter.download(httpclient, lastpage_link,
						config.encode);
			} while (currentpage == null);

		} else if (config.getexistmode.equals("2") && matcher.find()) {

			int index = matcher.start();
			index -= (config.pageExample.length() + 5);
			String temp1 = currentpage.html.substring(index);
			lastpage_link = common_function.getInfo(temp1, "<a href=\"",
					config.lastpageFlag);
			lastpage_link = lastpage_link.replace("&amp;", "&");
			if (config.needHost.equals("true")) {
				String host = common_function.getHost(item.link);
				lastpage_link = host + lastpage_link;
			}
//			System.out.println("=========="+lastpage_link);
			do {
				currentpage = getter.download(httpclient, lastpage_link,
						config.encode);
			} while (currentpage == null);
		}

		boolean new_reply = true, nomore_page = true;

		String last_modified_time = old_modified_time;
		String previouspage_link = null;
		int i = 0;
		while (new_reply && nomore_page) {
			if (config.prevpagelinkmode.equals("1")) {
				Matcher previous = pattern2.matcher(currentpage.html);
				if (previous.find()) {
					previouspage_link = common_function.getInfo(
							currentpage.html, config.currentpageFlag, "\"");

					if (previouspage_link == null
							|| ispost(previouspage_link) == false)
						previouspage_link = null;
				}
			} else if (config.prevpagelinkmode.equals("2")) {
				String currentpagenum = common_function.getInfo(
						currentpage.html, config.currentpagenum_begin,
						config.currentpagenum_end);

				if (currentpagenum != null) {
					if (currentpagenum.equals("1"))
						previouspage_link = null;
					else {
						int previouspagenum = Integer.parseInt(currentpagenum) - 1;
						if (config.site_name.equals("无忧论坛")) {
							// http://bbs.51.ca/thread-548292-3-1.html
							String[] urlsplit = currentpage.link.split("-");
							previouspage_link = "thread-" + urlsplit[1] + "-"
									+ String.valueOf(previouspagenum) + "-"
									+ urlsplit[3];
						} else if (config.site_name.equals("多维论坛")) {
							// http://forum.dwnews.com/threadshow.php?tid=1184409&v=2&extra=&page=2
							String[] urlsplit = currentpage.link.split("=");
							previouspage_link = "threadshow.php?tid="
									+ urlsplit[1] + "&page="
									+ String.valueOf(previouspagenum);
						}
					}
				} else {
					previouspage_link = null;
				}
			}
			int reply_begin, reply_end;
			int reply_date_begin, reply_date_end;
			int replyer_begin_index, replyer_end_index;
			replyer_begin_index = replyer_end_index = reply_begin = reply_end = reply_date_begin = reply_date_end = 0;
			Matcher begin = re_begin.matcher(currentpage.html);
			Matcher end = re_end.matcher(currentpage.html);
			Matcher d_begin = da_begin.matcher(currentpage.html);
			Matcher d_end = da_end.matcher(currentpage.html);
			Matcher m_replyer_begin = replyer_begin.matcher(currentpage.html);
			Matcher m_replyer_end = replyer_end.matcher(currentpage.html);
			String replyer_tmp = "";
			String replytime_tmp = "";
			String replycontent_tmp = "";

			while (m_replyer_begin.find(replyer_end_index)) {
				replyer_begin_index = m_replyer_begin.end();
				if (m_replyer_end.find(replyer_begin_index))
					replyer_end_index = m_replyer_end.start();

				replyer_tmp = currentpage.html.substring(replyer_begin_index,
						replyer_end_index);

				if (d_begin.find(replyer_end_index)) {
					reply_date_begin = d_begin.end();
					if (d_end.find(reply_date_begin))
						reply_date_end = d_end.start();
				}
				replytime_tmp = currentpage.html.substring(reply_date_begin,
						reply_date_end);
				System.out.println("---replytime_tmp------:"+replytime_tmp);
				DateTransform d = new DateTransform();
				replytime_tmp = d.date_transform(replytime_tmp);
				if (last_page == true)
					last_modified_time = replytime_tmp;

				try {
					Date dt = sdf.parse(replytime_tmp.trim());
					if (old_dt.getTime() - dt.getTime() >= 0) // reply already
						// exist
					{
						new_reply = false;
						continue;
					}
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (begin.find(replyer_end_index)) {
					reply_begin = begin.end();
					if (end.find(reply_begin))
						reply_end = end.start();
				}
				replycontent_tmp = currentpage.html.substring(reply_begin,
						reply_end);
				replycontent_tmp = common_function.purifyHtml(replycontent_tmp);
				/*
				 * s_reply += temp; s_reply += "_replyend_";
				 */
				db.update("insert into forum_title_reply(website_id,website_name,title_id,replyer,content,re_time,search_id) values('"
						+ config.forumid
						+ "','"
						+ config.site_name
						+ "','"
						+ title_id
						+ "','"
						+ replyer_tmp
						+ "','"
						+ replycontent_tmp
						+ "','"
						+ replytime_tmp
						+ "','"
						+ post_batch + "')");
			}
			i++;
			if (previouspage_link == null)
				nomore_page = false;
			else if (new_reply == true) {
				System.out.println("currentpage_link:" + currentpage.link);
				if (config.needHost.equals("true")) {
					String host = common_function.getHost(item.link);
					previouspage_link = host + previouspage_link;
				}
				System.out.println("previouspage_link:" + previouspage_link);
				currentpage = getter.download(httpclient, previouspage_link,
						config.encode);
				previouspage_link = null;
			}
			last_page = false;
		}

		// 更新forum_title表
		String sql = "update forum_title set click_num='" + clicknum
				+ "',reply_num='" + replynum + "',last_re_time='"
				+ old_modified_time + "' where id='" + id + "'";
		int complete = db.update(sql);
		// necessary for update
		db.closed();
		// int ok = db
		// .update("insert into forum_title(website_id,website_name,title_id,title,url,click_num,content,reply_num,author,last_re_time,time,search_id)"
		// + "values('"
		// + config.forumid
		// + "','"
		// + config.site_name
		// + "','"
		// + title_id
		// + "','"
		// + title
		// + "','"
		// + url
		// + "','"
		// + clicknum
		// + "','"
		// + old_reply
		// + "','"
		// + replynum
		// + "','"
		// + author
		// + "','"
		// + last_modified_time
		// + "','"
		// +"'0000'"
		// + "','"
		// + post_batch + "')");
		return re;
	}

	public boolean ispost(String link) {
		System.out.println(config.article_filter);
		System.out.println(link);
		Pattern pattern = Pattern.compile(config.article_filter);
		Matcher matcher = pattern.matcher(link);
		System.out.println(matcher.matches());

		return matcher.matches();
	}

	public boolean iscolumn(String link) {
		System.out.println(config.column_filter);
		System.out.println(link);
		Pattern pattern = Pattern.compile(config.column_filter);
		Matcher matcher = pattern.matcher(link);
		System.out.println(matcher.matches());

		return matcher.matches();
	}

	public boolean check_article(cls_item item, DefaultHttpClient httpclient, AtomicInteger title_id) throws InterruptedException, SQLException {

		Article article = new Article(item, config, db);
		article.set_httpclient(httpclient);
		article.set_reply_click_num(ReplyClickNum);

		if(!article.check_title()) 		return false;
		if(!article.check_post_date()) 	return false;
		if(!article.check_content()) 	return false;
		if(!article.check_author()) 	return false;
		if(!article.check_column()) 	return false;
		if(!article.check_clicknum()) 	return false;
		if(!article.check_replynum()) 	return false;
		if(!article.check_replys()) 	return false;

		article_num.getAndIncrement();
		int ok = db.update("insert into forum_title(website_id,website_name,title_id,title,url,click_num,content,reply_num,author,last_re_time,time,search_id)"
				+ "values('"
				+ config.forumid
				+ "','"
				+ config.site_name
				+ "','"
				+ article.s_id
				+ "','"
				+ article.s_title
				+ "','"
				+ item.link
				+ "','"
				+ article.clicknum
				+ "','"
				+ article.s_content
				+ "','"
				+ article.replynum
				+ "','"
				+ article.s_author
				+ "','"
				+ article.last_reply_time
				+ "','" 
				+ article.s_time 
				+ "','" 
				+ article.post_batch + "')");
		error_number.getAndAdd(ok);
		if (ok == 0) {
			//记录错误日志
		}
		System.out.println("Pagenum: " + page_number);
		System.out.println("Linknum: " + article_num.get());
		System.out.println("success:"+ error_number.get());

		return true;
	}
}