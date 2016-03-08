package simplypay;


import java.net.URI;
import java.security.Key;
import java.security.KeyFactory;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;



import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;


public class SimplyPayUtil {
	
	
	public static boolean DEBUG = true;
	
	public static void d(String msg) {
		System.out.println("DEBUG ==> "+msg);
	}
	
	
	public static String ENC_PLAIN = "plain";
	public static String ENC_RSA = "rsa";
	public static String ENC_AES = "aes";
	
	
	//각 인자별 암호화 방식 설정
	//기본은 AES 암호화를 선택
	
	public static Hashtable<String,String> keyMappings = new Hashtable<String,String>();
	static {
		keyMappings.put("token", ENC_PLAIN); 
		keyMappings.put("realm", ENC_PLAIN);
		keyMappings.put("callback_url", ENC_PLAIN);
		keyMappings.put("screen_name", ENC_RSA);
		
	}
	
	
	//keyMappings에 있는 인자들을 선택적 암호화를 한다.
	public static HashMap<String,Object> encryptParams(String rsaKey , String aesKey , HashMap<String,Object> map) throws SimplyPayException {
		
		Iterator<String> keys = map.keySet().iterator();
		
		try {
			while(keys.hasNext()) {
				String key = keys.next();
				if (!keyMappings.containsKey(key)) {
					//DO AES DECRYPT
					if (map.get(key) instanceof String ) {
						if (aesKey != null && aesKey.length() > 0 && ((String)map.get(key)).length() > 0) {
							map.put(key, aes_encrypt(aesKey,(String)map.get(key)));
						}
					}
					
				}else {
					String encType = keyMappings.get(key);
					if (encType.equals(ENC_PLAIN)) {
						//do nothing
					}
					else if (encType.equals(ENC_RSA)) {
						if (rsaKey != null && rsaKey.length() > 0  && ((String)map.get(key)).length() > 0) {
							map.put(key, encrypt(rsaKey,(String)map.get(key)));
						}
					}
				}
			}
		}catch(Exception e) {
			e.printStackTrace();
			throw new SimplyPayException(e);
		}
		
		return map;
	}
	
	
	
	static {
		Security.addProvider(new BouncyCastleProvider());
	}
	
	///////////////////////////////////////////////////
	// RSA Enc / Dec
	///////////////////////////////////////////////////
	
	public static String encrypt(String pubKeyBase64Encoded,String data) throws Exception {
		byte[] pubkey = Base64.decodeBase64(pubKeyBase64Encoded);
		String ret = "";
		
		X509EncodedKeySpec spec = new X509EncodedKeySpec(pubkey);
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    Key pubKey = kf.generatePublic(spec);
	    
	    final Cipher cipher =  Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");
	    cipher.init(Cipher.ENCRYPT_MODE, pubKey);
	    ret = Base64.encodeBase64String(cipher.doFinal(data.getBytes()));
	      
	    return ret;
	}
	
	
	public static String decrypt(String privateKeyBase64Encoded,String data) throws Exception {
		
		if (data == null || data.length() < 1) return data;
		
		byte[] privatekey = Base64.decodeBase64(privateKeyBase64Encoded);
		String ret = "";
		
		PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privatekey);
	    KeyFactory kf = KeyFactory.getInstance("RSA");
	    Key privKey = kf.generatePrivate(spec);
	    

	    final Cipher cipher =  Cipher.getInstance("RSA/ECB/PKCS1Padding", "BC");//Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
	    cipher.init(Cipher.DECRYPT_MODE, privKey);
	    ret = new String(cipher.doFinal(Base64.decodeBase64(data)));
	      
	    return ret;
	}
	
	///////////////////////////////////////////////////
	// AES Enc / Dec
	///////////////////////////////////////////////////
		
	public static String aes_encrypt(String key,String input){
	    byte[] crypted = null;
	    try{
	        SecretKeySpec skey = new SecretKeySpec(Base64.decodeBase64(key), "AES");
	        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, skey);
	        crypted = cipher.doFinal(input.getBytes());
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    return Base64.encodeBase64String(crypted);
	}

	public static String aes_decrypt(String key,String input){
		
		if (input == null || input.length() < 1) return input;
		
	    byte[] output = null;
	    try{
	        SecretKeySpec skey = new SecretKeySpec(Base64.decodeBase64(key), "AES");
	        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	        cipher.init(Cipher.DECRYPT_MODE, skey);
	        output = cipher.doFinal(Base64.decodeBase64(input));
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	    
	    return new String(output);
	}
	
	
	
	///////////////////////////////////////////////////
	// Http Call 후 JSON 응답 받는 함수 
	///////////////////////////////////////////////////

	public static JsonNode callJsonNode(String scheme , String host , int port , String path ,Map<String,Object> params) throws SimplyPayException {
		JsonNode returnValue = stringToJson(getEntity(scheme,host,port,path,params));
		return returnValue;
	}
	
	public static JsonNode stringToJson (HttpEntity entity) throws SimplyPayException{
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = null;
		String response =  null;
		
		try {
			response = EntityUtils.toString(entity);
			d(response);
			
			json = mapper.readValue(
					response
				    , JsonNode.class
			);
			//mapper.readValue(,JsonNode.class);
		}catch(Exception e) {
			if (response != null)
				throw new SimplyPayException(response);
			else 
				throw new SimplyPayException(e);
		}
		return json;
	}
	
	
	
	///////////////////////////////////////////////////
	// Apache HttpClient 4.1  - HTTP 네트워킹 함수 
	///////////////////////////////////////////////////
	
	private static HttpEntity getEntity (String scheme,String host , int port , String path,Map<String,Object> Params) throws SimplyPayException {
	    
		HttpEntity entity = null;
		try {
			URI uri = URIUtils.createURI(scheme,host,port,path,null,null );
		
		    HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(uri.toString());
	
	
		    MultipartEntity mpEntity = new MultipartEntity();
			for (Entry<String,Object> e : Params.entrySet()){
				mpEntity.addPart(e.getKey(),new StringBody(e.getValue().toString(),Charset.forName("UTF-8")));	
			}
		    httppost.setEntity(mpEntity);
		    httpclient = wrapClient(httpclient);
	
	
			HttpResponse response = httpclient.execute(httppost);
			entity = response.getEntity();
		}catch(Exception e) {
			throw new SimplyPayException(e);
		}
		
		return entity;
	}
	
	@SuppressWarnings("deprecation")
	public static HttpClient wrapClient(HttpClient base) {
	     try {
	         SSLSocketFactory ssf = new SSLSocketFactory(new TrustStrategy() {
	             public boolean isTrusted(
	                     final X509Certificate[] chain, String authType) throws CertificateException {
	                 // Oh, I am easy...
	                 return true;
	             }

	         });
	         ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	         ClientConnectionManager ccm = base.getConnectionManager();
	         SchemeRegistry sr = ccm.getSchemeRegistry();
	         
	         sr.register(new Scheme("https", ssf, 8443));
	         sr.register(new Scheme("https", ssf, 443));
	         return new DefaultHttpClient(ccm, base.getParams());
	     } catch (Exception ex) {
	         return null;
	     }
	 }
	
	
}
