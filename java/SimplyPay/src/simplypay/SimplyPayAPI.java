package simplypay;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonNode;


@SuppressWarnings("unused")
public class SimplyPayAPI {
	
	
	public boolean DEBUG = true;
	
	
	private String rsaKey = null;
	private String aesKey = null;
	private String realm = null;
	private String token = null;
	
	private String host = "www.kkokjee.com";
	private String scheme = "https";
	private int port = 443;
	
	
	private static final int CODE_OK = 200;
	private static final int CODE_ERROR = 400;
	
	private static final String PATH_CREATE_SESSION = "/simple/v1/create_session";
	private static final String PATH_REGISTER_CARD = "/simple/v1/register_card";
	private static final String PATH_LIST_CARD = "/simple/v1/card_list";
	private static final String PATH_UNREGISTER_CARD = "/simple/v1/unregister_card";
	private static final String PATH_PAY_CARD = "/simple/v1/do_pay_card";
	private static final String PATH_CANCEL_CARD = "/simple/v1/do_cancel_card";
	
	public void setDevServer() {
		host = "dev.kkokjee.com";
		scheme = "http";
		port = 80;
	}
	
	public void setRealServer() {
		host = "www.kkokjee.com";
		scheme = "https";
		port = 443;
	}
	
	public SimplyPayAPI(String realm , String rsaKey) throws SimplyPayException {
		
		this(realm,rsaKey,null,null);
	}
	
	public SimplyPayAPI(String realm,String rsaKey,String aesKey,String token) throws SimplyPayException{
		if (rsaKey == null || rsaKey.length() < 1) {
			throw new SimplyPayException("RSA 키가 필요합니다");
		}
		
		if (realm == null || realm.length() < 1) {
			throw new SimplyPayException("REALM 값이 필요합니다");
		}
		
		try {
			Base64.decodeBase64(rsaKey);
		}catch(Exception e) {
			throw new SimplyPayException("올바르게 인코딩된 RSA 키가 아닙니다");
		}
		
		this.realm = realm;
		this.rsaKey = rsaKey;
		
		if (aesKey != null && aesKey.length() > 1) {
			try {
				Base64.decodeBase64(aesKey);
			}catch(Exception e) {
				throw new SimplyPayException("올바르게 인코딩된 AES 키가 아닙니다");
			}
			
			this.aesKey = aesKey;
			
			if (token == null || token.length() < 1) {
				throw new SimplyPayException("세션 토큰값이 필요합니다");
			}
			
			this.token = token;
		}
	}
	
	public String getToken() {
		return token;
	}
	
	public String getAesKey() {
		return aesKey;
	}
	
	public void d(String msg) {
		if (DEBUG) {
			System.out.println("DEBUG ==> "+msg);
		}
	}

	public void createSession(String screen_name) throws SimplyPayException {
		
		if (realm == null || realm.length() < 1)  
			throw new SimplyPayException("REALM 값이 필요합니다");
		
		if (screen_name == null || screen_name.length() < 1) 
			throw new SimplyPayException("유저 아이디가 필요합니다");
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("realm", realm);
		params.put("screen_name" , screen_name);
		params.put("callback_url" , "reserved_for_future");
		
		d(params.toString());
		
		params = SimplyPayUtil.encryptParams(rsaKey, null, params);
		
		d(params.toString());
		
		JsonNode json = SimplyPayUtil.callJsonNode(scheme,host,port,PATH_CREATE_SESSION,params);
		d("createSession : "+json.toString());
		
		try {
			if (json.get("code").asInt() != CODE_OK) {
				String message = json.get("message").asText();
				throw new SimplyPayException(message);
			}
			
			Map<String,Object> data = (Map<String,Object>) StringUtil.jsonToMap(json.get("data"));
			
			String aesKey = data.get("key").toString();
			String token = data.get("token").toString();

			
			if (aesKey != null && aesKey.length() > 1) {
				try {
					Base64.decodeBase64(aesKey);
				}catch(Exception e) {
					throw new SimplyPayException("올바르게 인코딩된 AES 키가 아닙니다");
				}
				
				this.aesKey = aesKey;
				
				if (token == null || token.length() < 1) {
					throw new SimplyPayException("올바른 토큰 값이 아닙니다");
				}
				
				this.token = token;
			}
			
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SimplyPayException(e);
		}
	}
	
	
	public boolean registerCard(
			String screen_name,
			String title,
			String card_no,
			String card_date,
			String card_secure,
			String ssn,
			String pin
		) throws SimplyPayException {
		
		
		if (realm == null || realm.length() < 1)  
			throw new SimplyPayException("REALM 값이 필요합니다");
		
		if (token == null || token.length() < 1) 
			throw new SimplyPayException("createSession이후 token값이 필요합니다");
		
		if (aesKey == null || aesKey.length() < 1) 
			throw new SimplyPayException("createSession이후 AES 키값이 필요합니다");
		
		if (screen_name == null || screen_name.length() < 1) 
			throw new SimplyPayException("유저 아이디가 필요합니다");
		
		if (title == null) title = "";
		
		if (card_no == null || card_no.length() < 14 ) 
			throw new SimplyPayException("카드번호는 최소 15자리입니다");
		
		if (card_date == null || card_date.length() < 6) 
			throw new SimplyPayException("YYYYMM형식의 카드 유효기간이 필요합니다");
		
		if (card_secure == null || card_secure.length() < 2) 
			throw new SimplyPayException("카드 비밀번호 앞 2자리가 필요합니다");
		
		if (ssn == null || ssn.length() < 6) 
			throw new SimplyPayException("개인의 경우 생년월일 6자리 , 사업자의 경우 사업자번호 10자리가 필요합니다");
		
		if (pin == null || pin.length() < 4) 
			throw new SimplyPayException("간편결제 비밀번호는 최소 4자리입니다");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("realm", realm);
		params.put("token",this.token);
		params.put("screen_name" , screen_name);
		params.put("title" , title);
		
		params.put("card_no" , card_no);
		params.put("card_date" , card_date);
		params.put("card_secure" , card_secure);
		
		params.put("ssn" , ssn);
		params.put("pin" , pin);
		
		d(params.toString());
		
		params = SimplyPayUtil.encryptParams(rsaKey, aesKey, params);
		
		d(params.toString());
		
		JsonNode json = SimplyPayUtil.callJsonNode(scheme,host,port,PATH_REGISTER_CARD,params);
		d("registerCard : "+json.toString());
		
		boolean ret = false;
		try {
			if (json.get("code").asInt() != CODE_OK) {
				String message = json.get("message").asText();
				throw new SimplyPayException(message);
			}else {
				ret = true;
			}
			
			Map<String,Object> data = (Map<String,Object>) StringUtil.jsonToMap(json.get("data"));
			
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SimplyPayException(e);
		}
		
		
		return ret;
	}
	
	
	public List<Map<?,?>> listCard(
			String screen_name
		) throws SimplyPayException {
		
		
		if (realm == null || realm.length() < 1)  
			throw new SimplyPayException("REALM 값이 필요합니다");
		
		if (token == null || token.length() < 1) 
			throw new SimplyPayException("createSession이후 token값이 필요합니다");
		
		if (aesKey == null || aesKey.length() < 1) 
			throw new SimplyPayException("createSession이후 AES 키값이 필요합니다");
		
		if (screen_name == null || screen_name.length() < 1) 
			throw new SimplyPayException("유저 아이디가 필요합니다");
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("realm", realm);
		params.put("token",this.token);
		params.put("screen_name" , screen_name);
		
		d(params.toString());
		
		params = SimplyPayUtil.encryptParams(rsaKey, aesKey, params);
		
		d(params.toString());
		
		JsonNode json = SimplyPayUtil.callJsonNode(scheme,host,port,PATH_LIST_CARD,params);
		d("listCard : "+json.toString());
		
		List<Map<?,?>> list = null;
		
		try {
			if (json.get("code").asInt() != CODE_OK) {
				String message = json.get("message").asText();
				throw new SimplyPayException(message);
			}else {
				
			}
			
			Map<String,Object> data = (Map<String,Object>) StringUtil.jsonToMap(json.get("data"));
			list = ( List<Map<?,?>> ) data.get("list");
			
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SimplyPayException(e);
		}
		
		
		return list;
	}
	
	public Map<?,?> payCard(
			String screen_name,
			
			String card_uuid,
			String pin,
			
			int installment,
			int amount,
			int tax,
			int fee
	) throws SimplyPayException {
		return payCard(screen_name,card_uuid,pin,installment,amount,tax,fee,null,null,null,null,null,null);
	}
	
	public Map<?,?> payCard(
			String screen_name,
			
			String card_uuid,
			String pin,
			
			int installment,
			int amount,
			int tax,
			int fee,
			
			String comment
	) throws SimplyPayException {
		return payCard(screen_name,card_uuid,pin,installment,amount,tax,fee,comment,null,null,null,null,null);
	}
	
	public Map<?,?> payCard(
			String screen_name,
			
			String card_uuid,
			String pin,
			
			int installment,
			int amount,
			int tax,
			int fee,
			
			String comment,
			String additional_data
	) throws SimplyPayException {
		return payCard(screen_name,card_uuid,pin,installment,amount,tax,fee,comment,additional_data,null,null,null,null);
	}
	
			
	public Map<?,?> payCard(
			String screen_name,
			
			String card_uuid,
			String pin,
			
			int installment,
			int amount,
			int tax,
			int fee,
			
			String comment,
			String additional_data,
			
			String customer_name,
			String customer_email,
			String customer_phone,
			String customer_mobile

		) throws SimplyPayException {
		
		
		if (realm == null || realm.length() < 1)  
			throw new SimplyPayException("REALM 값이 필요합니다");
		
		if (token == null || token.length() < 1) 
			throw new SimplyPayException("createSession이후 token값이 필요합니다");
		
		if (aesKey == null || aesKey.length() < 1) 
			throw new SimplyPayException("createSession이후 AES 키값이 필요합니다");
		
		if (screen_name == null || screen_name.length() < 1) 
			throw new SimplyPayException("유저 아이디가 필요합니다");
		
		if (card_uuid == null || card_uuid.length() < 32 ) 
			throw new SimplyPayException("카드 고유 번호가 필요합니다");
		
		if (pin == null || pin.length() < 4) 
			throw new SimplyPayException("간편결제 비밀번호는 최소 4자리입니다");
		
		
		if (comment == null || comment.length() < 1)  {
			comment = "";
		}
		
		if (additional_data == null || additional_data.length() < 1)  {
			additional_data = "";
		}
		
		if (customer_name == null || customer_name.length() < 1)  {
			customer_name = "";
		}
		if (customer_email == null || customer_email.length() < 1)  {
			customer_email = "";
		}
		if (customer_phone == null || customer_phone.length() < 1)  {
			customer_phone = "";
		}
		if (customer_mobile == null || customer_mobile.length() < 1)  {
			customer_mobile = "";
		}
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("realm", realm);
		params.put("token",this.token);
		params.put("screen_name" , screen_name);
		
		params.put("card_uuid" , card_uuid);
		params.put("pin" , pin);
		
		params.put("installment" , installment+"");
		params.put("amount" , amount+"");
		params.put("tax" , tax+"");
		params.put("fee" , fee+"");
		
		
		params.put("comment" , comment);
		params.put("additional_data" , additional_data);
		
		params.put("customer_name" , customer_name);
		params.put("customer_email" , customer_email);
		params.put("customer_phone" , customer_phone);
		params.put("customer_mobile" , customer_mobile);
		
		
		
		d(params.toString());
		
		params = SimplyPayUtil.encryptParams(rsaKey, aesKey, params);
		
		d(params.toString());
		
		JsonNode json = SimplyPayUtil.callJsonNode(scheme,host,port,
				PATH_PAY_CARD,params);
		d("payCard : "+json.toString());
		
		Map<String,Object> data = null;
		
		try {
			if (json.get("code").asInt() != CODE_OK) {
				String message = json.get("message").asText();
				throw new SimplyPayException(message);
			}else {
				
			}
			
			data = (Map<String,Object>) StringUtil.jsonToMap(json.get("data"));
			
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SimplyPayException(e);
		}
		
		
		return data;
	}
	
	
	public Map<?,?> cancelCard(
			String screen_name,
			
			String order_no

		) throws SimplyPayException {
		
		
		if (realm == null || realm.length() < 1)  
			throw new SimplyPayException("REALM 값이 필요합니다");
		
		if (token == null || token.length() < 1) 
			throw new SimplyPayException("createSession이후 token값이 필요합니다");
		
		if (aesKey == null || aesKey.length() < 1) 
			throw new SimplyPayException("createSession이후 AES 키값이 필요합니다");
		
		if (screen_name == null || screen_name.length() < 1) 
			throw new SimplyPayException("유저 아이디가 필요합니다");
		
		if (order_no == null || order_no.length() < 1 ) 
			throw new SimplyPayException("거래 고유 주문번호가 필요합니다");

		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("realm", realm);
		params.put("token",this.token);
		params.put("screen_name" , screen_name);
		
		params.put("order_no" , order_no);
		
		
		d(params.toString());
		
		params = SimplyPayUtil.encryptParams(rsaKey, aesKey, params);
		
		d(params.toString());
		
		JsonNode json = SimplyPayUtil.callJsonNode(scheme,host,port,
				PATH_CANCEL_CARD,params);
		d("cancelCard : "+json.toString());
		
		Map<String,Object> data = null;
		
		try {
			if (json.get("code").asInt() != CODE_OK) {
				String message = json.get("message").asText();
				throw new SimplyPayException(message);
			}else {
				
			}
			
			data = (Map<String,Object>) StringUtil.jsonToMap(json.get("data"));
			
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SimplyPayException(e);
		}
		
		
		return data;
	}
	
	
	public boolean unregisterCard(
			String screen_name,
			
			String card_uuid,
			String pin
			

		) throws SimplyPayException {
		
		
		if (realm == null || realm.length() < 1)  
			throw new SimplyPayException("REALM 값이 필요합니다");
		
		if (token == null || token.length() < 1) 
			throw new SimplyPayException("createSession이후 token값이 필요합니다");
		
		if (aesKey == null || aesKey.length() < 1) 
			throw new SimplyPayException("createSession이후 AES 키값이 필요합니다");
		
		if (screen_name == null || screen_name.length() < 1) 
			throw new SimplyPayException("유저 아이디가 필요합니다");
		
		if (card_uuid == null || card_uuid.length() < 32 ) 
			throw new SimplyPayException("카드 고유 번호가 필요합니다");
		
		if (pin == null || pin.length() < 4) 
			throw new SimplyPayException("간편결제 비밀번호는 최소 4자리입니다");
		
		
		HashMap<String,Object> params = new HashMap<String,Object>();
		
		params.put("realm", realm);
		params.put("token",this.token);
		params.put("screen_name" , screen_name);
		
		params.put("card_uuid" , card_uuid);
		params.put("pin" , pin);
		
		
		d(params.toString());
		
		params = SimplyPayUtil.encryptParams(rsaKey, aesKey, params);
		
		d(params.toString());
		
		JsonNode json = SimplyPayUtil.callJsonNode(scheme,host,port,
				PATH_UNREGISTER_CARD,params);
		d("unregisterCard : "+json.toString());
		
		boolean ret = false;
		
		try {
			if (json.get("code").asInt() != CODE_OK) {
				String message = json.get("message").asText();
				throw new SimplyPayException(message);
			}else {
				ret = true;
			}
			
//			(Map<String,Object>) StringUtil.jsonToMap(json.get("data"));
			
		}
		catch(Exception e) {
			e.printStackTrace();
			throw new SimplyPayException(e);
		}
		
		
		return ret;
	}
	
}
