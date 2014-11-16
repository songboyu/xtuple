package common_function;

import java.io.InputStream;
import java.util.Date;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class httpclienttest {
	public static void main(String arg0[]){
		String result = "";
		
		httpclient h = new httpclient();
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpClient client = new HttpClient();
		httpclient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_0);
		
		long start = new Date().getTime();
		final GetMethod httpget = new GetMethod("http://club.kdnet.net/list.asp?boardid=1");
		//http://localhost/chunked.html
//		httpget.setRequestHeader("User-Agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 6.1; .NET CLR 2.0.50727)");
		httpget.setRequestHeader("Accept-encoding", "gzip,deflate");
		
		try{
			client.executeMethod(httpget);

		InputStream inputStream = httpget.getResponseBodyAsStream();
		InputStream gzipIn = new GZIPInputStream(inputStream);
//		InputStream chunkedIn = new MyChunkedInputStream(gzipIn,httpget);
//		InputStream chunkedIn = new MyChunkedInputStream(inputStream,httpget);
//		InputStream gzipIn = new GZIPInputStream(chunkedIn);
		
		
		StringBuffer  out  =  new  StringBuffer();
		byte [] b = new byte[8192];
		for  (int  n;  (n  =  gzipIn.read(b))  !=  -1;)  
		  {  
//			String temp = "" + (char)n;
				  out.append(new String(b,0,n,"gb2312"));  
				 System.out.println(out);
		  }
		result = out.toString();
//		result = StringEscapeUtils.escapeJava(result);
		}catch (Exception e){
			e.printStackTrace();
		}
		
		System.out.println(result);
		System.out.println(new Date().getTime() - start);
	}
}
