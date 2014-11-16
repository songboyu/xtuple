package blog.crawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import xtuple.crawler.cls_database;

public class blog_exist_info {
	public static Set<String> getExist(int day)
	{
		cls_database db = new cls_database();
		Set<String> exist = new HashSet<String>();
	
		String sql = "select * from blog where blog_time > DATE_SUB(now(),INTERVAL " + day + " DAY)";
		ResultSet rs = db.getrs(sql);{
	
			try {
				while (rs.next())
				{
					String temp = rs.getString("blog_url");
					System.out.println(rs.getString("blog_url") + "\t" + rs.getString("blog_time") + "\t");
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
	}
		return exist;
	}

}
