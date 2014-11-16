package news_comment.crawler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;

public class add_task implements Runnable
{
	public BlockingQueue<comment_item> comment_tasks;
	private comment_item newtask;
	static int task_num = 0;
	public add_task(comment_item task, BlockingQueue<comment_item> queue)
	{
		comment_tasks = queue;
		newtask = task;
	}
	public void run()
	{
		task_num++;
		try {
			comment_tasks.add(newtask);
		
			BufferedWriter writer;

			writer = new BufferedWriter(new FileWriter(new File("d:\\Comment_Links.txt"), true));

			String error = "";
			error += "news_link:" + newtask.news_link + "\r\n";
			error += "news_comment_link:" + newtask.news_comment_link + "\r\n";
			error += "task_num:" + task_num + "\r\n";
			writer.write(error);

			writer.close();
		
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}