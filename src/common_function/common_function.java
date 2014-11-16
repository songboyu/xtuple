package common_function;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import xtuple.crawler.DateTransform;

public class common_function {
	public static String getInfo(String data, String begin, String end)
	{
		if (begin.equals("ignore") && end.equals("ignore"))
			return "Ignore Me";
			
		String result = null;
		//System.out.println(data);
		Pattern _begin = Pattern.compile(begin, Pattern.DOTALL);
		Pattern _end = Pattern.compile(end, Pattern.DOTALL);
		Matcher __begin = _begin.matcher(data);
		Matcher __end = _end.matcher(data);
		
		int beginpos = -1, endpos = -1;
		
		if (__begin.find())
			beginpos = __begin.end();
		try {
		if (__end.find(beginpos))
			endpos = __end.start();
		}catch (IndexOutOfBoundsException e){
			;
		}
			
		if (beginpos != -1 && endpos != -1)
		{
			//System.out.println(data);
			result = (data.substring(beginpos,endpos));
		}
		return result;
	}
	
	public static String getMultiInfo(String data, String begin, String end)
	{
		String result = null;
		
		if (begin.equals("ignore") && end.equals("ignore"))
			return "Ignore Me";
		
		Pattern _begin = Pattern.compile(begin, Pattern.DOTALL);
		Pattern _end = Pattern.compile(end, Pattern.DOTALL);
		Matcher __begin = _begin.matcher(data);
		Matcher __end = _end.matcher(data);
		
		int beginpos = 0, endpos = 0;
		
		while (__begin.find(endpos))
		{
			beginpos = __begin.end();
			try {
				if (__end.find(beginpos))
					endpos = __end.start();
			}catch (IndexOutOfBoundsException e){
				;
			}
			result += data.substring(beginpos,endpos);
		}
		
		return result;
	}
	
	//replace all single quotation
	//single quotation will cause SQLException while insert
	public static String replaceSingleQuotation(String input)	
	{
		String output = input;
		
		Pattern pattern = Pattern.compile("'", Pattern.DOTALL);
		Matcher matcher = pattern.matcher(output);
		output = matcher.replaceAll("''");
		
		pattern = Pattern.compile("\\\\", Pattern.DOTALL);
		matcher = pattern.matcher(output);
		output = matcher.replaceAll("\\\\\\\\");
//		output = matcher.replaceAll("");

		return output;
	}
	
	public static String getHost(String link)
	{
		String str_result = link;
		
		String spilt [] = str_result.split("/");
		str_result = spilt[0] + "//" + spilt[2] + "/";
//		System.out.println(str_result);
		
		return str_result;
	}
	
	//return correct date format
	public static String getDate(String time)
	{
		DateTransform d = new DateTransform();
		String result;
		String whichDate = d.date_trasform(time);
		System.out.println(whichDate);
		time = d.date_trasform2(time);
		System.out.println(time);
		if (whichDate.length() > time.length())
			time = whichDate;
		
		result = time;
		
		return result;
	}

	public static String purifyHtml(String input) {
		String output = input;

		Pattern video = Pattern.compile(
				"(<script.*?>.+?</script>)|(<style.*?>.+?</style>)",
				Pattern.DOTALL);
		Matcher find_video = video.matcher(output);
		String str_result = find_video.replaceAll("");

		Pattern pattern = Pattern.compile("(</?div.*?>)|(</?iframe.*?>)");
		Matcher matcher = pattern.matcher(str_result);
		str_result = matcher.replaceAll("");

		output = str_result;
		output = StringEscapeUtils.unescapeHtml(output);
		output = common_function.replaceSingleQuotation(output);

		return output;
	}
	
	public static String getTextFromHtml(String htmlStr){
		Document doc = Jsoup.parse(htmlStr);
		String text = doc.body().text();
		return text;
	}
}
