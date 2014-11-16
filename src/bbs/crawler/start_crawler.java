package bbs.crawler;

public class start_crawler {
	bbs_master_core core1 = new bbs_master_core();
	
	public void run()
	{
		core1.get_bbs_list("bbs_list.txt");
		core1.start();
	}
	
	public bbs_master_core getMaster()
	{
		return core1;
	}
}
