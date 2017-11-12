package br.com.miguelpereira.hash;

import java.util.HashMap;
import java.util.Map;

import br.com.miguelpereira.hash.MD5;
import io.netty.util.internal.StringUtil;

public class TableServer {
	
	/**
	 * Metodo responsavel por pegar a porta do servidor com o seu respectivo hash
	 * @param numberServers
	 * @return
	 */
	public Map<String, String> getMapServers(String numberServers, String port){

		Map<String, String> maptHashServer = null;
		if(!StringUtil.isNullOrEmpty(numberServers) && Integer.valueOf(numberServers) > 0) {
			
			maptHashServer = new HashMap<String,String>();
			
			for (int i = 0; i < Integer.valueOf(numberServers); i++) {
				String hash = MD5.hash(port + i);
				maptHashServer.put(port + i, hash);
				
			}
		}		
		
		return maptHashServer;
	}

}
