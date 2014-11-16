package blog.crawler;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.Html;
import org.htmlparser.util.NodeIterator;
/**
 * 根据网页的编码类型自动匹配编码
 * @author Administrator
 *
 */
public class CharsetAutoSwitch {
 private static final String oriEncode = "utf-8,gb2312,gbk,iso-8859-1";//字符编码集合，可根据实际编码类型进行扩充，试探器会不断试探该字符编码集合直到得到正确的编码方式
 /**
  * 检测URL指定的网页的字符集
  * @param url
  * @return 返回网页的实际编码方式
  */
 public static String dectedEncode(String url)
 {
	System.out.println("begin");
    String[] encodes = oriEncode.split(",");
    for (int i = 0; i < encodes.length; i++) {
    if (dectedCode(url, encodes[i])) {
    	System.out.println("end");
    	return encodes[i];
    }
    }
    return null;
  }
 /**
  * 编码匹配试探器，不断去试探utf-8,gb2312,gbk,iso-8859-1等编码方式，直到得到正确的结果
  * @param url
  * @param encode
  * @return
  */
 public static boolean dectedCode(String url, String encode) 
 {
    try {
    Parser parser = new Parser(url);
    parser.setEncoding(encode);
    for (NodeIterator e = parser.elements(); e.hasMoreNodes();) {
    Node node = (Node) e.nextNode();
//    System.out.println(node.getClass());
    if (node instanceof Html||node instanceof BodyTag) {
    	return true;
    }
    }
    } catch (Exception e) {
    }

    return false;
  }
}