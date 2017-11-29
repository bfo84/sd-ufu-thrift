package br.ufu.miguelpereira.hash;

import java.math.BigInteger;
import java.security.MessageDigest;

import io.netty.util.internal.StringUtil;

public class MD5 {

	public static int getGenerateServerId(String vertice, String numServidores) throws Exception {
		System.out.println("Vertice: "+vertice);
		System.out.println("numServe: "+numServidores);
		MessageDigest m = MessageDigest.getInstance("MD5");
		m.update(vertice.getBytes(), 0, vertice.length());
		return (new BigInteger(1, m.digest()).mod(new BigInteger(numServidores))).intValue();
		
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

