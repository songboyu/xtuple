package bbs.crawler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import xtuple.crawler.cls_database;

public class event_of_title {
	
	public void update_eventid_subeventid() throws SQLException
	{
		cls_database db = new cls_database();
	
		String sql = "select * from keyword_of_event";
		ResultSet rs = db.getrs(sql);
		while (rs.next())
			{
				int event_id = rs.getInt("Event_ID");
				int subevent_id = rs.getInt("Sub_event_id");
				String keyword = rs.getString("Keyword");
				//keyword = "中共";
				
				ResultSet rs2 = db.getrs("select distinct title_id from forum_title where title like '%"+keyword+"%'");
				while(rs2.next())
				{
					int title_id = rs2.getInt("title_id");
					db.update("insert into event_of_title(event_id,title_id) values ('"+event_id+"','"+title_id+"')");
					db.update("insert into subevent_of_title(subevent_id,title_id) values ('"+subevent_id+"','"+title_id+"')");
				}	
			}
	}
	public static void main(String args[]) throws SQLException
	{
		event_of_title test = new event_of_title();
		test.update_eventid_subeventid();
	}
}


