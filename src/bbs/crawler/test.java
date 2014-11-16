package bbs.crawler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xtuple.crawler.DateTransform;

public class test {
	static start_crawler start;
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println((new Date()).toLocaleString());
		System.out.println("==================================");
		long begin = (new Date()).getTime();
		start = new start_crawler();
		start.run();
		long end = (new Date()).getTime();
		System.out.println("==================================");
		System.out.println(end - begin);
		
		System.out.println(bbs_filter.articlenum);
	}
	public start_crawler getMaster()
	{
		return start;
	}
}
