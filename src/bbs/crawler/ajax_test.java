package bbs.crawler;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import xtuple.crawler.cls_item;

import blog.crawler.blog_download_ajax;

public class ajax_test {
	public static void main(String [] args)
	{
		blog_download_ajax getter_ajax = new blog_download_ajax();
		//DefaultHttpClient tempclient = new DefaultHttpClient();
		//tempclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5 * 1000);
		//tempclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2 * 1000);
		cls_item item;
		item = getter_ajax.download("");
	}

}
