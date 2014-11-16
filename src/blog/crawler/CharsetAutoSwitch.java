package blog.crawler;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.tags.BodyTag;
import org.htmlparser.tags.Html;
import org.htmlparser.util.NodeIterator;
/**
 * ������ҳ�ı��������Զ�ƥ�����
 * @author Administrator
 *
 */
public class CharsetAutoSwitch {
 private static final String oriEncode = "utf-8,gb2312,gbk,iso-8859-1";//�ַ����뼯�ϣ��ɸ���ʵ�ʱ������ͽ������䣬��̽���᲻����̽���ַ����뼯��ֱ���õ���ȷ�ı��뷽ʽ
 /**
  * ���URLָ������ҳ���ַ���
  * @param url
  * @return ������ҳ��ʵ�ʱ��뷽ʽ
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
  * ����ƥ����̽��������ȥ��̽utf-8,gb2312,gbk,iso-8859-1�ȱ��뷽ʽ��ֱ���õ���ȷ�Ľ��
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