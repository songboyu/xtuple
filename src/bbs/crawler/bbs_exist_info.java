package bbs.crawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import xtuple.crawler.cls_database;

public class bbs_exist_info {
	
	public static Set<String> getExist(String sitename, int day)
	{
		cls_database db = new cls_database();
		Set<String> exist = new HashSet<String>();
	
		String sql = "select * from forum_title where last_re_time > DATE_SUB(now(),INTERVAL " + day +" DAY) AND website_name='" + sitename + "'";
		ResultSet rs = db.getrs(sql);{
	
			try {
				while (rs.next())
				{
					String temp = rs.getString("url");
					System.out.println(rs.getString("url") + "\t" + rs.getString("last_re_time") + "\t");
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

