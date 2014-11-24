package bbs.crawler;

import java.util.Date;

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
