package xtuple.crawler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTransform {
	// ���ڸ�ʽ����ʽ��2012-01-01 00:00:00��2012��01��01��00:00��
	// �ո��λ��Ҳ��һ��date_tranformͳһת���ɵ�һ����ʽ

	public DateTransform() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String date_transform(String timetemp) {
		String result = this.date_trasform(timetemp);
		if (this.leagledatetime(result))
			return result;
		result = this.date_trasform2(timetemp);
		if (this.leagledatetime(result))
			return result;
		result = this.date_trasform3(timetemp);
		if (this.leagledatetime(result))
			return result;
		result = this.date_trasform4(timetemp);
		if (this.leagledatetime(result))
			return result;
		result = this.date_trasform5(timetemp);
		if (this.leagledatetime(result))
			return result;
		result = this.date_trasform6(timetemp);
		if (this.leagledatetime(result))
			return result;
		result = this.date_trasform7(timetemp);
		if (this.leagledatetime(result))
			return result;
		return null;

	}

	public boolean leagledatetime(String timetemp) {
		try {
			if (timetemp.length() < 14 || timetemp.length() > 19)
				return false;
			String[] date_time = timetemp.split(" ");
			String date = date_time[0];
			String time = date_time[1];
			String[] year_month_day = date.split("-");
			String year = year_month_day[0];
			if (year.length() != 4)
				return false;
			String month = year_month_day[1];
			if ((month.length() > 2) || (Integer.parseInt(month) > 12))
				return false;

			String day = year_month_day[2];
			if ((day.length() > 2) || (Integer.parseInt(day) > 31))
				return false;
			String[] hour_minute_second = time.split(":");
			String hour = hour_minute_second[0];
			if ((hour.length() > 2) || (Integer.parseInt(hour) > 24))
				return false;
			String minute = hour_minute_second[1];
			if ((minute.length() > 2) || Integer.parseInt(minute) > 60)
				return false;
			if (hour_minute_second.length == 2)
				return false;
			else {
				String second = hour_minute_second[2];
				if ((second.length() > 2) || (Integer.parseInt(second) > 60))
					return false;
			}
		} catch (Exception e) {
			//e.printStackTrace();
			return false;
		}
		return true;
	}

	public String date_trasform(String timetemp) {
		String result = "";
		try {
			if (timetemp.length() < 11)
				return "";
			Pattern digit = Pattern.compile("\\d+");
			Matcher m_digit = digit.matcher(timetemp);
			int index = 0, i = 0;

			while (m_digit.find(index)) {
				result += m_digit.group();
				// System.out.println(m_digit.group());
				index = m_digit.end();
				i++;
				if (i == 1) {
					if (Integer.parseInt(result) < 2000)
						return result; // dont return a null value
				}
				if (i < 3)
					result += "-";
				else if (i == 3)
					result += " ";
				else
					result += ":";
				// System.out.println(result);
			}
			if (i == 6) // yyyy-mm-dd hh:mm:ss
				result = result.substring(0, result.length() - 1);
			else if (i == 5) // yyyy-mm-dd hh:mm
				result += "00";
			else if (i == 3) // yyyy-mm-dd
				result += "00:00:00";

			// return result;
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			return result;
		}
	}

	public String date_trasform2(String timetemp) // ��Ӧ5-5 13:01�ĸ�ʽ
	{
		String result = "";
		try {
			if (timetemp.length() < 11)
				return "";
			Pattern digit = Pattern.compile("\\d+");
			Matcher m_digit = digit.matcher(timetemp);
			int index = 0, i = 0;

			while (m_digit.find(index)) {
				result += m_digit.group();
				// System.out.println(m_digit.group());
				index = m_digit.end();
				i++;
				if (i == 1) {
					if (Integer.parseInt(result) > 12)
						return result; // dont return a null value
					result += "-";
				} else if (i == 2)
					result += " ";
				else if (i == 3)
					result += ":";
				else
					break;
			}
			result += ":00";
			String temp = result;

			if (Calendar.getInstance().get(Calendar.MONTH) == 12
					&& Calendar.getInstance().get(Calendar.DAY_OF_MONTH) >= 28)
				result = (Calendar.getInstance().get(Calendar.YEAR) - 1) + "-";
			else
				result = Calendar.getInstance().get(Calendar.YEAR) + "-";

			result += temp;

			// return result;
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			return result;
		}
	}

	public String date_trasform3(String timetemp)// 11/17/2013 01:46:57 =>
													// 2013-11-17 01:46:57
	{
		String result = null;
		try {
			if (timetemp.length() < 11)
				return "";

			String[] date_time = timetemp.split(" ");
			String date = date_time[0];
			String time = date_time[1];
			String[] year_month_day = date.split("/");
			String year = year_month_day[2];
			String month = year_month_day[0];
			if (month.length() == 1)
				month = "0" + month;
			String day = year_month_day[1];
			if (day.length() == 1)
				day = "0" + day;
			String[] hour_minute_second = time.split(":");
			String hour = hour_minute_second[0];
			if (hour.length() == 1)
				hour = "0" + hour;
			String minute = hour_minute_second[1];
			if (minute.length() == 1)
				minute = "0" + minute;
			result = year + "-" + month + "-" + day + " " + hour + ":" + minute;
			if (hour_minute_second.length == 2)
				result = result + ":00";
			else {
				String second = hour_minute_second[2];
				if (second.length() == 1)
					second = "0" + second;
				result = result + ":" + second;
			}

		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			return result;
		}

	}

	public String date_trasform4(String timetemp)// 11/17/2013 => 2013-11-17
													// 23:59:59
	{
		String result = null;
		try {
			String[] year_month_day = timetemp.split("/");
			String year = year_month_day[2];
			String month = year_month_day[0];
			if (month.length() == 1)
				month = "0" + month;
			String day = year_month_day[1];
			if (day.length() == 1)
				day = "0" + day;
			result = "20" + year + "-" + month + "-" + day + " 23:59:59";
			// return result;

		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			return result;
		}
	}

	public String monthtodigit(String month) {
		String[] months = { "Jan", "Fri", "Mar", "Apr", "May", "Jun", "Jul",
				"Aug", "Sep", "Oct", "Nov", "Dec" };
		int i = 1;
		for (; i < 13; i++) {
			if (month.equals(months[i - 1]))
				break;
		}
		return Integer.toString(i);
	}

	public String date_trasform5(String timetemp)// 03 May 2014 => 2014-05-03
													// 23:59:59
	{
		timetemp = timetemp.replaceAll("\n", "");
		timetemp = timetemp.replaceAll("\t","");
		String result = null;
		try {
			String[] year_month_day = timetemp.split(" ");
			String year = year_month_day[2];
			String month = year_month_day[1];
			month = monthtodigit(month);
			if (month.length() == 1)
				month = "0" + month;
			String day = year_month_day[0];
			if (day.length() == 1)
				day = "0" + day;
			result = year + "-" + month + "-" + day + " 23:59:59";
			// return result;
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			return result;
		}
	}

	public String date_trasform6(String timetemp)// Yesterday, 10:56 AM
														// => 2014-05-05
														// 10:56:00
	{
		timetemp = timetemp.replaceAll("\n", "");
		timetemp = timetemp.replaceAll("\t","");
		String result = null;
		try {
			Calendar today = Calendar.getInstance();
			

			String year = String.valueOf(today.get(Calendar.YEAR));
			String month = String.valueOf(today.get(Calendar.MONTH) + 1);
			if (month.length() == 1)
				month = "0" + month;
			String day = String.valueOf(today.get(Calendar.DATE));
			if (day.length() == 1)
				day = "0" + day;

			String[] splits = timetemp.split(" ");

			String[] hour_minute = splits[1].split(":");

			if (splits[2].equals("AM") && hour_minute[0].equals("12")) // 12:59
																		// AM
																		// ->00:59
			{
				hour_minute[0] = "00";
			}
			if (splits[2].equals("PM") && !hour_minute[0].equals("12")) //
			{
				hour_minute[0] = String.valueOf(Integer
						.parseInt(hour_minute[0]) + 12);
			}
			int hour = today.get(Calendar.HOUR_OF_DAY);
			boolean minusoneday = false;
			if (hour < 8) {
				minusoneday = true;
			}
			boolean yesterday = false;
			if (splits[0].equals("Yesterday,")) {
				yesterday = true;
			}
			String date = String.format("%s-%s-%s %s:%s:00", year, month, day,
					hour_minute[0], hour_minute[1]);

			Calendar c = Calendar.getInstance();

			try {
				c.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
						.parse(date));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			long qwer = c.getTimeInMillis();
			//System.out.println("时间转化后的毫秒数为：" + qwer);
			if (yesterday)
				qwer = qwer - 24 * 60 * 60 * 1000;
			if (minusoneday)
				qwer = qwer - 24 * 60 * 60 * 1000;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			result = sdf.format(qwer);
			//System.out.println("毫秒数转化后的时间为：" + result);

			// return result;
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			return result;
		}
	}
	public String date_trasform7(String timetemp)//2014-03-07T07:03:50+00:00->2014-03-07 07:03:50
	{
		String result = null;
		try
		{
			String [] splits = timetemp.split("\\+");
			result = splits[0].replace("T", " ");
		}
		catch(Exception e)
		{
			//e.printStackTrace();
		}
		finally
		{
			return result;
		}
	}

}
