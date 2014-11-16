package bbs.crawler;

import java.io.FileNotFoundException;
import java.io.FileReader;


import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import xtuple.crawler.cls_item;

import blog.crawler.blog_download_new;


public class jstest {
	
	/**
	 * @param args
	 * @throws ScriptException
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws ScriptException, FileNotFoundException
	{
		/*
		blog_download_new getter = new blog_download_new();
		
		DefaultHttpClient tempclient = new DefaultHttpClient();
		tempclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 5 * 1000);
		tempclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 2 * 1000);
		
		cls_item item = getter.download(tempclient,"http://forum.vanhi.com/forum.php?mod=viewthread&tid=190706&amp;extra=page%3D1","gbk");
		
		item.html = common_function.common_function.getInfo(item.html, "<script type=\"text/javascript\">", "</script>");
		*/
		
		 ScriptEngineManager manager = new ScriptEngineManager();  
		 
		 ScriptEngine engine = manager.getEngineByName("jav8");
		 
		engine.eval("var window = new Object(), location = new Object();");
		engine.eval("var url, get_url = function (x) {url = x};");
        engine.eval("location.assign = get_url, location.replace = get_url;");     
         

		 //engine.eval("if(age>=18){println('Old enough to vote!');}else{println('Back to school!');}");//解析 JavaScript 脚本,对脚本表达式进行求值  
         engine.eval(new FileReader("check.js"));//eval()函数返回执行脚本后所返回的值，默认情况下，将返回上次执行的表达式的值  
         //engine.eval(item.html);
         engine.eval("var mxy1111;");
         Object result = engine.eval("mxy1111 = (function(){return url || location.href || location})()");
         //Object result = engine.eval("(function () {return url;})()");
         
       
		 System.out.println(result.toString());
		
	}

}
