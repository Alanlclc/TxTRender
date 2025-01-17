package cn.ksb.minitxt.services;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Scanner;


import cn.ksb.minitxt.client.BaseService;
import cn.ksb.minitxt.client.Init;
import cn.ksb.minitxt.client.Service;
import cn.ksb.minitxt.client.ServiceFactory;
import cn.ksb.minitxt.clientutils.DefaultCommunicatorImp1;
import cn.ksb.minitxt.common.constants.Constains;
import cn.ksb.minitxt.common.constants.UserConstants;
import cn.ksb.minitxt.common.entity.DataTransfer;
import cn.ksb.minitxt.common.entity.User;

public class LoginService extends BaseService<Serializable> {
	private String OUTPUT_TEXT_USERNAME = "请输入登录名:";
	private String OUTPUT_TEXT_PASSWORD = "请输入密码:";
	private String OUTPUT_TEXT_INVALIDINPUT = "你的输入无效，请重新输入！";
	private String OUTPUT_TEXT_SERVERERROR = "服务器故障，请重试！";
	private String OUTPUT_TEXT_ERROR = "系统存在错误，服务终止！";
	private String OUTPUT_TEXT_PASSWORD2 = "请再次输入密码:";
	private String OUTPUT_TEXT_USEREXIST = "用户名已存在，请重新注册！";
	private String OUTPUT_TEXT_PASSWORDNOTEQUAL = "两次密码不一样！";
	private String OUTPUT_USER_SAVESUCESS = "用户注册成功，请登录！";
	private String OUTPUT_USER_SAVEFAIL = "用户注册失败，请重新注册！";
	@Override
	public Service<? extends Serializable> execute() {
		Scanner scanner = new Scanner(System.in);
		while(true){
			System.out.println(OUTPUT_TEXT_USERNAME);
			String username = scanner.next().trim();
			System.out.println(OUTPUT_TEXT_PASSWORD);
			String password = scanner.next().trim();
			if(username.length()==0||password.length()==0){
				System.out.println(OUTPUT_TEXT_INVALIDINPUT);
				continue;
			}
			User user = new User();
			user.setUsername(username);
			user.setPassword(password);
			DataTransfer<User> dto = new DataTransfer<User>();
			dto.setDate(user);
			dto.setKey(Constains.COMMAND_REGISTER);
			
			DefaultCommunicatorImp1<User, ?> comm = new DefaultCommunicatorImp1<User, Serializable>();
			DataTransfer<?> response = null;
			try {
				comm.init(Init.getProperty("ip"),Integer.parseInt(Init.getProperty("post")) );
				response = comm.conmunicate1(dto);
			} catch (NumberFormatException e) {
				e.printStackTrace();
				System.out.println(OUTPUT_TEXT_ERROR);
				System.exit(-1);
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.out.println(OUTPUT_TEXT_ERROR);
				System.exit(-1);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println(OUTPUT_TEXT_SERVERERROR);
				 return ServiceFactory.getServers(Constains.COMMAND_START);
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally{
				try {
					comm.destory();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if( UserConstants.SUCCESS == response.getResult()){
				System.out.println(OUTPUT_USER_SAVESUCESS);
//				ServiceFactory.getServers(Constains.COMMAND_START);
				return null;
			} else if(UserConstants.PASSWORD_INVALID == response.getResult()
					||UserConstants.USERNAME_NOT_EXSITS == response.getResult()){
				System.out.println(OUTPUT_USER_SAVEFAIL);
				continue;
			} else{
				System.out.println(OUTPUT_TEXT_SERVERERROR);
				continue;
			}
			
		}
	}
	
}
