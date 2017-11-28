package br.ufu.miguelpereira.hash;

import java.math.BigInteger;
import java.security.MessageDigest;

import io.netty.util.internal.StringUtil;

public class MD5 {

	public static int getGenerateServerId(String vertice, String numServidores) throws Exception {

		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(vertice.getBytes(), 0, vertice.length());
		System.out.print(new BigInteger(1, m.digest()).intValue());
		return (new BigInteger(1, m.digest())).intValue();
		
	}
	
	/*
	 * Metodo responsavel por realizar o hash de cada string
	 * @param word
	 * @return
	 */
	public static String hash(String word) {
		
		String hash = null;
		if(!StringUtil.isNullOrEmpty(word)) {
			try {
			MessageDigest m = MessageDigest.getInstance("MD5");
			hash = m.digest(word.getBytes()).toString();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return hash;
		
		
	}

}

