package bbs.crawler;
/*
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import xtuple.crawler.cls_item;

import blog.crawler.blog_download_ajax;
import blog.crawler.blog_download_new;

public class downloadtest {
	
	public static void main(String[] args)
	{
		DefaultHttpClient tempclient = new DefaultHttpClient();
		tempclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5 * 1000);
		tempclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2 * 1000);
		
		blog_download_new getter = new blog_download_new();
		cls_item item;
		item = getter.download(tempclient,"http://forum.vanhi.com/forum.php?mod=viewthread&tid=11979&extra=page%3D1%26filter%3Dreply%26orderby%3Dreplies","gbk");
		System.out.println(item.html);
		item = getter.download(tempclient,"http://forum.vanhi.com/forum.php?mod=viewthread&tid=189057&extra=page%3D4&_dsign=84e2c6f4","gbk");
		System.out.println(item.html);
		blog_download_ajax ajax_getter = new blog_download_ajax();
		item = ajax_getter.download("http://forum.vanhi.com/forum.php?mod=viewthread&tid=189057&extra=page%3D4");
		System.out.println(item.html);
	}

}
*/

import java.io.IOException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

public class downloadtest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		redirect02();
	}

	/**
	 * Http URL重定向
	 */
	private static void redirect02() {
		DefaultHttpClient httpclient = null;
		String url = "http://forum.vanhi.com/forum.php?mod=viewthread&tid=11979&extra=page%3D1%26filter%3Dreply%26orderby%3Dreplies";
		try {
			httpclient = new DefaultHttpClient();
			httpclient.setRedirectStrategy(new RedirectStrategy() {	//设置重定向处理方式

				@Override
				public boolean isRedirected(HttpRequest arg0,
						HttpResponse arg1, HttpContext arg2)
						throws ProtocolException {

					return false;
				}

				@Override
				public HttpUriRequest getRedirect(HttpRequest arg0,
						HttpResponse arg1, HttpContext arg2)
						throws ProtocolException {

					return null;
				}
			});

			// 创建httpget.
			HttpGet httpget = new HttpGet(url);
			// 执行get请求.
			HttpResponse response = httpclient.execute(httpget);

			int statusCode = response.getStatusLine().getStatusCode();
			if (statusCode == HttpStatus.SC_OK) {
				// 获取响应实体
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// 打印响应内容长度
					System.out.println("Response content length: "
							+ entity.getContentLength());
					// 打印响应内容
					System.out.println("Response content: "
							+ EntityUtils.toString(entity));
				}
			} else if (statusCode == HttpStatus.SC_MOVED_TEMPORARILY
					|| statusCode == HttpStatus.SC_MOVED_PERMANENTLY) {
				
				System.out.println("当前页面发生重定向了---");
				
				Header[] headers = response.getHeaders("Location");
				if(headers!=null && headers.length>0){
					String redirectUrl = headers[0].getValue();
					System.out.println("重定向的URL:"+redirectUrl);
					
					redirectUrl = redirectUrl.replace(" ", "%20");
					get(redirectUrl);
				}
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			httpclient.getConnectionManager().shutdown();
		}
	}

	/**
	 * 发送 get请求
	 */
	private static void get(String url) {

		HttpClient httpclient = new DefaultHttpClient();

		try {
			// 创建httpget.
			HttpGet httpget = new HttpGet(url);
			System.out.println("executing request " + httpget.getURI());
			// 执行get请求.
			HttpResponse response = httpclient.execute(httpget);
			
			// 获取响应状态
			int statusCode = response.getStatusLine().getStatusCode();
			if(statusCode==HttpStatus.SC_OK){
				// 获取响应实体
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					// 打印响应内容长度
					System.out.println("Response content length: "
							+ entity.getContentLength());
					// 打印响应内容
					System.out.println("Response content: "
							+ EntityUtils.toString(entity));
				}
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			// 关闭连接,释放资源
			httpclient.getConnectionManager().shutdown();
		}
	}

}

