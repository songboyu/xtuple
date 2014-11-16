package news_comment.crawler;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.DefaultHttpClient;

import common_function.IdleConnectionMonitorThread;
import common_function.common_function;
import common_function.httpclient;

import xtuple.crawler.cls_database;

public class excute_task implements Runnable{
	public BlockingQueue<comment_item> comment_tasks;
	
	httpclient hc;
	ClientConnectionManager cm;
	DefaultHttpClient httpclient;
	IdleConnectionMonitorThread monitor; 
	
	public static AtomicInteger pageNo = new AtomicInteger(0);
	
	public excute_task(BlockingQueue<comment_item> queue)
	{
		comment_tasks = queue;
		
		hc = new httpclient();
		cm = hc.getManager();
		httpclient = hc.getDefaultHttpClient(cm);
		hc = new httpclient();
		monitor = new IdleConnectionMonitorThread(cm); 
		monitor.start();
		monitor.setPriority(Thread.MAX_PRIORITY);
	}
	
	public void run()
	{
		int out = 0;
		
		System.out.println("rruunn");
		while (true)
		{
			System.out.println("begin");
			System.out.println("----------------------------------");
			System.out.println("in   " + comment_tasks.isEmpty());
			System.out.println("----------------------------------");
			while (comment_tasks.isEmpty() == false)
			{
				System.out.println("begin2");
				comment_item task =  comment_tasks.poll();
				
				if(getComment(task) == false)
					comment_tasks.add(task);
				
				try {
					BufferedWriter writer;

					writer = new BufferedWriter(new FileWriter(new File("commentPageNo.txt"),true));
					String n = "" + pageNo.get();
					writer.write(n + "\r\n");
					writer.write(new Date() + "\r\n");
					writer.close();
					
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				out++;
				if (out > 12)	//the sixth time task queue id empty, exit
					break;
				System.out.println("----------------------------------");
				System.out.println("sleep");
				System.out.println("----------------------------------");
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		monitor.shutdown();
		System.out.println("out!");
	}
	
	private boolean getComment(comment_item task)
	{
		cls_database db = new cls_database();
		
		String data_link;
		String data;

		int joinNo = 0, comNo = 0;
		int id = 0;
		String comment = "";
		String old_comment = "";
		String last_modified_time = "1970-01-01 01:01:01";
		int newest = 0;
		int pagesize = 0;
		int pagenum = 0;
		int old_pagenum = 1;
		
		Date modifiy_time;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		try{
		comment = "";
		old_comment = "";
		last_modified_time = "1970-01-01 01:01:01";
		newest = 0;
		pagesize = 0;
		pagenum = 0;
		old_pagenum = 1;
		
		ResultSet rs;
		Statement stm;   
		Connection con;   
		String database = "jdbc:mysql://localhost:3306/xtuple?useUnicode=true&characterEncoding=UTF-8";   
		String classname = "com.mysql.jdbc.Driver";   
		String username =  "root";   
		String password =  "root";
		
		modifiy_time = sdf.parse(last_modified_time);
		
		if (task.exist == true)//exist=true
		{
			String sql = "select * from news_comment where news_link='" + task.news_link + "'";
			con=DriverManager.getConnection(database,username,password);
			try{
				
				stm=con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
				rs=stm.executeQuery(sql);
		
				boolean empty = rs.next();

			if (rs == null || empty == false)	//got news without comment
			{
				task.exist = false;
//				getComment(task);
				return false;
			}
			
//			rs.next();
			
//			task.news_comment_link = rs.getString("comment_link");
			id = rs.getInt("news_comment_id");
			int old_joinNo = 0;
			if (task.config.joinNo.equals("test") == false)
				old_joinNo = rs.getInt("comment_joinNo");
			int old_comNo = 0;
			old_comNo = rs.getInt("comment_commentNo");
			
			last_modified_time = rs.getString("last_modified_time");
			modifiy_time = sdf.parse(last_modified_time);
			old_comment = rs.getString("comment_comment");
			
			pagesize = Integer.parseInt(task.config.pagesize);
			old_pagenum = (old_comNo / pagesize);
			if (old_comNo % pagesize != 0)
				old_pagenum++;
		
			rs.close();
			stm.close();
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			con.close();
		}
		}
//		else
//		{
		if (task.news_comment_link.equals("void") == false)
		{
			int firstpage = 1;
			//check firstpage
			data_link = task.config.data_link_prefix + task.boardID +
				task.config.data_link_middle + task.threadID + 
				task.config.pageID + firstpage + task.config.data_link_suffix;
		
			System.out.println("dataurl:" + data_link);
//			data = getResponse(data_link,task.config.encode);
			
			data = null;
			data = hc.getResponse(httpclient,data_link,task.config.comment_encode);
			if (data == null)
				return false;
				
			System.out.println("COMMENT CRAWLER HAVE DOWNLOADED " + pageNo.incrementAndGet() + " PAGE");
			
			modifiy_time = null;
			try {
				modifiy_time = sdf.parse(last_modified_time);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (task.config.isJson.equals("false"))
			{
				data = null;
				data = hc.getResponse(httpclient, task.news_comment_link, task.config.comment_encode);
				if (data == null)
					return false;
				
				joinNo = Integer.parseInt(common_function.getInfo(data, task.config.joinNo, task.config.joinNoEnd).trim());
				comNo = Integer.parseInt(common_function.getInfo(data, task.config.comNo, task.config.comNoEnd).trim());
				
				pagesize = Integer.parseInt(task.config.pagesize);
				pagenum = (comNo / pagesize);
				if (comNo % pagesize != 0)
					pagenum++;
			
				if (comNo == 0)
					comment = "该新闻还没有人评论";
				
				for (int i=1; i<=pagenum-old_pagenum+1; i++)
				{
					try{
					data_link = task.config.data_link_prefix + task.boardID +
								task.config.data_link_middle + task.threadID + 
								task.config.pageID + i + task.config.data_link_suffix;
					System.out.println("dataurl:" + data_link);
//						data = getResponse(data_link,task.config.encode);
					data = null;
					data = hc.getResponse(httpclient,data_link,task.config.comment_encode);
					
					if (data == null)
						continue;
					
					System.out.println("COMMENT CRAWLER HAVE DOWNLOADED " + pageNo.incrementAndGet() + " PAGE");
						
//					if (comment_remain < pagesize)
//						comment_num = comment_remain;
					
					Pattern t_begin = Pattern.compile(task.config.comment_time, Pattern.DOTALL);
					Pattern t_end = Pattern.compile(task.config.comment_time_end, Pattern.DOTALL);
					
					Pattern re_begin = Pattern.compile(task.config.comment_content, Pattern.DOTALL);
					Pattern re_end = Pattern.compile(task.config.comment_content_end, Pattern.DOTALL);
					
					Matcher t__begin = t_begin.matcher(data);
					Matcher t__end = t_end.matcher(data);
					Matcher re__begin = re_begin.matcher(data);
					Matcher re__end = re_end.matcher(data);
					
					int time_begin = 0, time_end = 0, reply_begin = 0, reply_end = 0;
					
					newest = 0;
					while (t__begin.find(time_end))
					{
						time_begin = t__begin.end();
						try {
							if (t__end.find(time_begin))
								time_end = t__end.start();
							if (re__begin.find(time_end))
								reply_begin = re__begin.end();
							if (re__end.find(reply_begin))
								reply_end = re__end.start();
							
						}catch (IndexOutOfBoundsException e){
							;
						}
				
						String comment_time = data.substring(time_begin,time_end);
						Date comment_date = null;
						
						try{
							comment_date = sdf.parse(comment_time.trim());
						}catch (ParseException e1){
							long now = Long.parseLong(comment_time);
							if (now < 10000000000L)	//10 digit
								now *= 1000;
							comment_date = new Date(now);
							comment_time = sdf.format(comment_date);
						}
						
						if (comment_date.getTime() - modifiy_time.getTime() > 0)
						{
							comment += data.substring(reply_begin,reply_end);
							comment += "_commentend_";
						}
						
						if (newest == 0)
						{
							last_modified_time = comment_time;
						}
						newest++;
					}
//					comment_remain -= pagesize;
//					System.out.println(comment);
					}catch(Exception e){
						//do nothing
					}
				}
			}
			else
			{
				//JSON---------------------------
				//JSON---------------------------
				data = hc.extractJsonArray(data);
				JSONObject json = JSONObject.fromObject(data);
				if (task.config.comment_object.equals("test") == false)	//in sub dir
					json = json.getJSONObject(task.config.comment_object);
				//uncompleted
				if (task.config.joinNo.equals("test") == false)
				{
					joinNo = Integer.parseInt(common_function.getInfo(data, task.config.joinNo, ",|}").trim());
				}
				else
				{
					String joinData;
					joinData = null;
					joinData = hc.getResponse(httpclient, task.news_comment_link, task.config.comment_encode);
					if (joinData == null)
						return false;
					joinNo = Integer.parseInt(common_function.getInfo(joinData," 有<em>", "</em>人参与").trim());
					System.out.println("COMMENT CRAWLER HAVE DOWNLOADED " + pageNo.incrementAndGet() + " PAGE");
				}
				System.out.println("joinNo: " + joinNo);

//				comNo = json.getInt(task.config.comNo);
				comNo = Integer.parseInt(common_function.getInfo(data, task.config.comNo, ",|}").trim());
				System.out.println("comNo: " + comNo);
			
				pagesize = Integer.parseInt(task.config.pagesize);
				pagenum = (comNo / pagesize);
				if (comNo % pagesize != 0)
					pagenum++;
			
				if (comNo == 0)
					comment = "该新闻还没有人评论";
			
				for (int i=1; i<=pagenum-old_pagenum+1 && i<500; i++)	//at most 500 page one time
				{
					try{
					if (i != 1)	//data of the first page have already gotten
					{
						data_link = task.config.data_link_prefix + task.boardID +
								task.config.data_link_middle + task.threadID + 
								task.config.pageID + i + task.config.data_link_suffix;
						System.out.println("dataurl:" + data_link);
//						data = getResponse(data_link,task.config.encode);
//						httpclient hc2 = new httpclient();
						data = hc.getResponse(httpclient,data_link,task.config.comment_encode);
						if (data == null)
							continue;
						data = hc.extractJsonArray(data);
					}
					
					if (data == null)
					{
						System.out.println("Can't get comment!");
						continue;
					}
					
					System.out.println("COMMENT CRAWLER HAVE DOWNLOADED " + pageNo.incrementAndGet() + " PAGE");
					json = JSONObject.fromObject(data);
					if (task.config.comment_object.equals("test") == false)	//in sub dir
						json = json.getJSONObject(task.config.comment_object);
//					if (json.getString(task.config.comment_array) == "null")
//					{
						//163 backup
//						task.config.comment_array = "hotPosts";
//					}
//					if (json.isArray(task.config.comment_array))
					
					String hotComment = "";
					
					boolean isHotComment;
					if (i==1 && task.config.site_name.equals("163新闻")){
						isHotComment = true;
					}
					else{
						isHotComment = false;
					}
					
					boolean isNewComment;
					isNewComment = true;
					
					while (isHotComment || isNewComment)
					{
						isNewComment = false;
						JSONArray newPosts = null;
						
						if (i==1 && task.config.site_name.equals("163新闻") && isHotComment){
							newPosts = json.optJSONArray("hotPosts");
							//the first comment page of 163 got two chance
							isHotComment = false;
							isNewComment = true;
							
						}
						else{
							newPosts = json.optJSONArray(task.config.comment_array);
						}
						if (newPosts == null || newPosts.isArray() == false)
						{
							System.out.println("JSONArray is empty!");
							if (comment.equals("") == true)
								comment = "该新闻还没有人评论";
							continue;
						}

						for (int j=0; j<newPosts.size(); j++)
						{
							JSONObject temp = newPosts.getJSONObject(j);
						
							JSONObject temp2;
							String size = String.valueOf(temp.size());
							temp2 = temp.getJSONObject(size);
							if (temp2.isNullObject() == false)
								temp = temp2;
						
							Date comment_date = null;
							String comment_time = temp.getString(task.config.comment_time);///////////////
							try{
								comment_date = sdf.parse(comment_time.trim());
							}catch (ParseException e1){
								long now = Long.parseLong(comment_time);
								if (now < 10000000000L)	//10 digit
									now *= 1000;
								comment_date = new Date(now);
								comment_time = sdf.format(comment_date);
							}
						
						
							if (comment_date.getTime() - modifiy_time.getTime() > 0)
							{
								comment += temp.getString(task.config.comment_content);
								comment += "_commentend_";
							}
							else
								System.out.println("old comment!");
						
							if (newest == 0)
							{
								last_modified_time = comment_time;
								newest++;
							}
						}
						if (isNewComment){	//now the new-post in 163 begin
							newest = 0;
						}
					}
//					comment_remain -= pagesize;
//					System.out.println(comment);
					}catch (Exception e){
						//do nothing
					}
				}
				System.out.println(comment);
			}
		}
		else
		{
			comment = "该新闻不能被评论";
		}
			
		
//		last_modified_time = sdf.format(new Date().getTime());
		if (old_comment.equals("该新闻还没有人评论") == false && old_comment.equals("该新闻不能被评论") == false)
			comment += old_comment;
		else 
			;
		comment = common_function.replaceSingleQuotation(comment);
		comment = comment.replaceAll("%u", "\\\\u");
		comment = StringEscapeUtils.unescapeJava(comment);
//		comment = StringEscapeUtils.unescapeXml(comment);
		int success = 0;
		if (task.exist == false)
			success = db.update("insert into news_comment(news_link,comment_link,comment_joinNo,comment_commentNo,comment_comment,last_modified_time) " +
				"values('" + task.news_link + "','" + task.news_comment_link + "','" + joinNo + "','" + comNo + "','" + comment + "','" + last_modified_time + "')");
		else
			success = db.update("update news_comment SET comment_joinNo ='" + joinNo + "',comment_commentNo='" + comNo + "',comment_comment='" +
					comment + "',last_modified_time='"+ last_modified_time +"' WHERE news_comment_id='"+ id +"'");
		
		db.closed();
		
		if (success == 0)
		{
			try {
				BufferedWriter writer;
	
				writer = new BufferedWriter(new FileWriter(new File("InsertError_Links.txt"), true));
			
				
				String error = "";
				error += "News link:" + task.news_link +"\r\n";
				error += "Error link:" + task.news_comment_link +"\r\n";
				error += "comment:" + comment + "\r\n\r\n";
				writer.write(error);

				writer.close();
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("----------------------------------");
		System.out.println("completed");
		System.out.println(comment);
		System.out.println("----------------------------------");
		}catch (Exception e)
		{
			e.printStackTrace();
		}
		return true;
	}
		
	private boolean commentInJson(comment_item task)
	{
		
		
		return true;
	}

	private boolean commentInText(comment_item task)
	{
//		httpclient hc2 = new httpclient();
		
		return true;
	}
}


