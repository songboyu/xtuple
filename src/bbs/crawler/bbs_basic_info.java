package bbs.crawler;

public class bbs_basic_info {
	public String post_url;
	public String post_replynum;
	public String last_modified_time;
	
	
	public String toString()
	{
		return post_url + "\t" + post_replynum + "\t" + last_modified_time;
	}
}
