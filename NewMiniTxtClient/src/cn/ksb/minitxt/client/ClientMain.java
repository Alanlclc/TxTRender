package cn.ksb.minitxt.client;

import java.io.Serializable;

import cn.ksb.minitxt.common.constants.Constains;

public class ClientMain {
	/*
	 * 客户端程序入口
	 */
	public static void main(String[] args) {
		new ClientMain().startClient();
	}
	
	/*
	 * 客户端运行开始函数
	 */
	public void startClient(){
		Service<? extends Serializable> service=ServiceFactory.getServers(Constains.COMMAND_START);
		while(true){
			if(service==null){
				System.out.println("退出应用");
				break;
			}
//			通过迭代进行循环调用业务功能代码
			service = service.execute();
		}
	}
}
