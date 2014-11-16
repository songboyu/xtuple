package xtuple.crawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class news_exist_info {
	public static Set<String> getExist(String sitename, int day)
	{
		cls_database db = new cls_database();
		Set<String> exist = new HashSet<String>();
	
		try {
			String sql = "select * from news where news_time > DATE_SUB(now(),INTERVAL " + day + " DAY) AND news_site='"+ sitename +"'";
			ResultSet rs = db.getrs(sql);
	
			
				while (rs.next())
				{
					String temp = rs.getString("news_link");
//					temp.post_url = rs.getString("post_url");
//					temp.last_modified_time = rs.getString("last_modified_time");
//					temp.post_replynum = rs.getString("post_replynum");
					System.out.println(rs.getString("news_link") + "\t" + rs.getString("news_time") + "\t");
					exist.add(temp);
				}
				Iterator<String> ite = exist.iterator();
				while (ite.hasNext())
				{
					String temp;
					temp = ite.next();
					System.out.println(temp);
				}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
			
		return exist;
	
	}

}
