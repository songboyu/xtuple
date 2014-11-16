package xtuple.crawler;

import java.util.concurrent.BlockingQueue;

import news_comment.crawler.comment_item;

public interface cls_filter 
{
	public boolean check(cls_item item,BlockingQueue<comment_item> comment_tasks);
	public void checkExist(cls_item news_link,BlockingQueue<comment_item> comment_tasks);
	public boolean todo(String link);
}
