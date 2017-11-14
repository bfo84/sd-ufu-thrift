package br.ufu.miguelpereira.hash;

import java.util.HashMap;
import java.util.Map;


import io.netty.util.internal.StringUtil;

public class TableServer {
	
	/**
	 * Metodo responsavel por pegar a porta do servidor com o seu respectivo hash
	 * @param numberServers
	 * @return
	 */
	public static Map<String, String> getMapServers(String numberServers, String port){

		Map<String, String> maptHashServer = null;
		if(!StringUtil.isNullOrEmpty(numberServers) && Integer.valueOf(numberServers) > 0) {
			
			maptHashServer = new HashMap<String,String>();
			
			for (int i = 0; i < Integer.valueOf(numberServers); i++) {
				Integer portServer = Integer.valueOf(port) + i;
				maptHashServer.put(Integer.toString(i),portServer.toString());
			}
		}		
		
		return maptHashServer;
	}

}
