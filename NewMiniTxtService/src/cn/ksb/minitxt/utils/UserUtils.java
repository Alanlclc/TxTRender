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
	//	1����ȡ����XML�е��û���Ϣ
	private static final String path = Init.getProperty("");
	private static Map<String , User> users = new HashMap<String , User>();
	private static Document doc = null;
	
	static{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = null;
		try {
			db = dbf.newDocumentBuilder();
//			��ȡ�ļ���Ϣ
//			��ȡ�洢����û�������ļ�ֵ�ԣ�
//			��һ��XML�ļ�,�洢���������ļ���·��
//			���ļ���·���ֿ����Ա㽫����·����������µ���Ŀ����Ӱ��
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
			throw new RuntimeException("�����ļ���ʼ��ʧ�ܣ�����");
		} 
	}
	
//	��¼����Ҫ�Ĺ���
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
	
//	���жϷ�������д����
	public static boolean exists(String username){
		return(users.get(username)!=null);
	}
	
//  ע������Ҫ�Ĺ���
//	��Ҫע�����е�XML�ļ��ǹ��е�   ���ܴ��ڶ����û�ͬʱע�������ͬ������
//	����Ҫ���Ǹ����ļ�ʱ���������̰߳�ȫ����
	public static synchronized int doRegister(User user){
		if(exists(user.getUsername())){
			return UserConstants.USERNAME_EXSITS;
		}
//		�û��ṩ��ע�������ֿ���   ������Ӧ�Ľڵ�׷�ӵ�XML��
		Element newUser = doc.createElement("user");
		Element password = doc.createElement("password");
		Element username = doc.createElement("username");
		newUser.appendChild(password);
		newUser.appendChild(username);
		username.appendChild(doc.createTextNode(user.getUsername()));
		password.appendChild(doc.createTextNode(user.getPassword()));
//		�����Ӻõ������û�ע����Ϣ���ӵ�XML�ļ���
		doc.getDocumentElement().appendChild(newUser);
//	           ���Ѿ������õĽڵ���  д��XML��
		OutputStream fos = null;
		try {
			TransformerFactory tff = TransformerFactory.newInstance();
			tff.setAttribute("indent-number", 4);
			Transformer tf = tff.newTransformer();
//			ע������ַ�����ͳһ
			tf.setOutputProperty(OutputKeys.ENCODING, "GBK");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
//			�������ַ�����  4���ַ�
			tff.setAttribute("indent-number", 4);
//			ע��DOM������API��Ϥ   ���ü������õ������������Դ
			fos = new FileOutputStream(path);
			tf.transform(new DOMSource(doc),
							new StreamResult
							(new OutputStreamWriter
							(fos,"GBK")));
//			����ʼ��������������ע����û�   �Ա������û�ע��Ƚ�
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
//		��finally���н����ر��ͷ���Դ
		} finally{
			if(fos!=null){
//				���ǻ�������  
				try {
					fos.flush();
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
//		ע�⿼���쳣     ������������쳣  ��Ҫ���Ѿ�д��Ĵ�����Ϣ����
		doc.removeChild(newUser);
		users.remove(user.getUsername());
		return UserConstants.ERROR;
	}
}