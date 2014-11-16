package xtuple.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import common_function.IdleConnectionMonitorThread;

import news_comment.crawler.comment_item;
import news_comment.crawler.excute_task;

public class test 
{
	public static void main(String argsp[])
	{
		cls_master_core core = new cls_master_core();
		
		core.get_newssite_list("news_list.txt");
		core.run();
	}
}