package news_comment.crawler;

import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

import xtuple.crawler.cls_configure;

public class comment_item {
	public String news_link;
	public String news_comment_link;
	public cls_configure config;
	public boolean exist;
	
	public String boardID;
	public String threadID;	//newsID
//	public Date last_updated_time;
//	public String news_comment;
	
}
