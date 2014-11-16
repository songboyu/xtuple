package blog.crawler;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BlockingQueue<String> tasks = new LinkedBlockingQueue<String>();
		// TODO Auto-generated method stub
		blog_master_core core = new blog_master_core();
		core.get_blog_list("blog_list.txt");
		core.start();
//		core.set_filter(new blog_filter(new blog_configure("D:\\Program Files\\eclipse_project\\sina_blog.config")));
//		core.run();
	}

}

//�޷�����̬����
