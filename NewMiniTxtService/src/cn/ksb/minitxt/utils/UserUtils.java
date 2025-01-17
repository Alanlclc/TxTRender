package cn.ksb.minitxt.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cn.ksb.minitxt.common.constants.UserConstants;
import cn.ksb.minitxt.common.entity.User;
import cn.ksb.minitxt.server.Init;

public class UserUtils {
	//	1、读取现有XML中的用户信息
	private static final String path = Init.getProperty("");
	private static Map<String , User> users = new HashMap<String , User>();
	private static Document doc = null;
	
	static{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
//			提取文件信息
//			读取存储类的用户和密码的键值对；
//			是一个XML文件,存储以上所有文件的路径
//			将文件和路劲分开，以便将来的路径变更所导致的项目代码影响
			doc = db.parse(new FileInputStream(path));
			NodeList usernames = doc.getElementsByTagName("username");
			NodeList passwords = doc.getElementsByTagName("password");
			User user = null;
			for (int i = 0; i < usernames.getLength() ; i++) {
				user = new User();
				user.setUsername(usernames.item(i).getFirstChild().getNodeValue().trim());
				user.setPassword(passwords.item(i).getFirstChild().getNodeValue().trim());
				users.put(user.getUsername(), user);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("配置文件初始化失败，请检查");
		} 
	}
	
//	登录所需要的工具
	public static int doLogin(User user){
		User check = null;
		if((check = users.get(user.getUsername()))!=null){
			if(check.getPassword().equals(user.getPassword())){
				return UserConstants.SUCCESS;
			}else{
				return UserConstants.PASSWORD_INVALID;
			}
		}
		return UserConstants.USERNAME_NOT_EXSITS;
	}
	
//	将判断方法单独写出来
	public static boolean exists(String username){
		return(users.get(username)!=null);
	}
	
//  注册所需要的工具
//	需要注意已有的XML文件是共有的   可能存在多名用户同时注册引起的同步问题
//	即需要考虑更改文件时所发生的线程安全问题
	public static synchronized int doRegister(User user){
		if(exists(user.getUsername())){
			return UserConstants.USERNAME_EXSITS;
		}
//		用户提供的注册用名字可用   创建相应的节点追加到XML中
		Element newUser = doc.createElement("user");
		Element password = doc.createElement("password");
		Element username = doc.createElement("username");
		newUser.appendChild(password);
		newUser.appendChild(username);
		username.appendChild(doc.createTextNode(user.getUsername()));
		password.appendChild(doc.createTextNode(user.getPassword()));
//		将链接好的新有用户注册信息添加到XML文件中
		doc.getDocumentElement().appendChild(newUser);
//	           将已经处理好的节点树  写入XML中
		OutputStream fos = null;
		try {
			TransformerFactory tff = TransformerFactory.newInstance();
			tff.setAttribute("indent-number", 4);
			Transformer tf = tff.newTransformer();
//			注意输出字符编码统一
			tf.setOutputProperty(OutputKeys.ENCODING, "GBK");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
//			缩进的字符长度  4个字符
			tff.setAttribute("indent-number", 4);
//			注意DOM解析的API熟悉   运用几个常用的输出流更改资源
			fos = new FileOutputStream(path);
			tf.transform(new DOMSource(doc),
							new StreamResult
							(new OutputStreamWriter
							(fos,"GBK")));
//			将初始化集合中添加新注册的用户   以便后面的用户注册比较
			users.put(user.getUsername(), user);
			return UserConstants.SUCCESS;
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		在finally块中将流关闭释放资源
		} finally{
			if(fos!=null){
//				考虑缓存问题  
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
//		注意考虑异常     如果发生错误异常  需要将已经写入的错误信息清理
		doc.removeChild(newUser);
		users.remove(user.getUsername());
		return UserConstants.ERROR;
	}
}
