package bbs.crawler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class datetest {
	
	
	public static String date_trasform6(String timetemp)// Yesterday, 10:56 AM => 2014-05-05 10:56:00
	{
		
		
		String result = null;
		try {
			Calendar today = Calendar.getInstance();
			
			String year = String.valueOf(today.get(Calendar.YEAR));
			String month = String.valueOf(today.get(Calendar.MONTH)+1);
			if(month.length()==1)
				month = "0"+month;
			String day = String.valueOf(today.get(Calendar.DATE));
			if(day.length()==1)
				day="0"+day;
			
			String [] splits = timetemp.split(" ");
			
			String [] hour_minute = splits[1].split(":");
			
			if(splits[2].equals("AM")&&hour_minute[0].equals("12")) //12:59 AM ->00:59
			{
				hour_minute[0] = "00";
			}
			if(splits[2].equals("PM")&&!hour_minute[0].equals("12")) //
			{
				hour_minute[0] = String.valueOf(Integer.parseInt(hour_minute[0])+12);
			}
			int hour = today.get(Calendar.HOUR_OF_DAY);
			boolean minusoneday = false;
			if(hour<8)
			{
				minusoneday = true;
			}
			boolean yesterday=false;
			if(splits[0].equals("Yesterday,"))
			{
				yesterday = true;
			}

			//month="01";
			//day="01";
			String date = String.format("%s-%s-%s %s:%s:00",year,month,day,hour_minute[0],hour_minute[1]);
			
			Calendar c = Calendar.getInstance();
				
			try {
				c.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(date));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long qwer = c.getTimeInMillis();
			System.out.println("时间转化后的毫秒数为："+qwer);
			if(yesterday)
				qwer = qwer - 24*60*60*1000;
			if(minusoneday)
				qwer = qwer - 24*60*60*1000;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result = sdf.format(qwer);
			System.out.println("毫秒数转化后的时间为："+ result); 
				
			// return result;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			return result;
		}
	}
	


	public static void main(String[] args) {
		
		/*
		System.out.println((new Date()).toGMTString());
		System.out.println((new Date()).toString());

		*/
		
		
		System.out.println((new Date()).toLocaleString());
		System.out.println((new Date()).getYear());
		//System.out.println(Calendar.get(Calendar.MONTH));
		System.out.println((new Date()).getDate());
		
		//long nowtime = (new Date()).getTime();
		//long russiatime = nowtime-8*60*60*1000;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		//System.out.println("毫秒数转化后的时间为："+ sdf.format(russiatime)); 
		
		
		/**
		 * 将字符串数据转化为毫秒数
		 */
		String dateTime="08:00:01";

		Calendar c = Calendar.getInstance();
		System.out.println("timezone"+ c.getTimeZone());
		try {
			c.setTime(new SimpleDateFormat("HH:mm:ss").parse(dateTime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		long qwer = c.getTimeInMillis();
		
		System.out.println("时间转化后的毫秒数为："+qwer);
		
		System.out.println("毫秒数转化后的时间为："+ sdf.format(qwer));
		
		/*
		String dateTime = "0655";
		Calendar c = Calendar.getInstance();
		
		try {
			c.setTime(new SimpleDateFormat("HHmm").parse(dateTime));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long time = c.getTimeInMillis();
		System.out.println(time);
		*/
		date_trasform6("Yesterday, 1:56 PM");
	}
	
}
