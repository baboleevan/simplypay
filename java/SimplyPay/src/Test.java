import java.util.List;
import java.util.Map;

import simplypay.SimplyPayAPI;
import simplypay.SimplyPayException;



public class Test {

	
	private static String realm = "tester"; //발급 받은 고객사 아이디
	private static String rsaPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm7uQAOYvS4oYWd5UFUeZPobo28QWBE/ACK7xmxghmmPa7XmxjoeejCZyl1wit+KTZB/ZX2vaVmU/jZHip4y70FeJhvrB02UjcYkQOq8xIj/fCbNlr6yAzh07Z7wlP1NU/IAyCIwG90KawW2Ve6v6TRSY+0SAeAsr/Pc4ZA1GeWg9+NugNvvzziqMQDTwXJYbjH3Oo3PnzGz2Kof+NV11Z8xDxC/gv0mX+EXm2WJGJDl32MMpEXExGoL1kh7I0I5Ix+VW6lOlticzYTQLJfokzCq2bmU/+9VFjGNpB/o2D896XE/eb0p0PXJQ8NTKxPusmo9LdWOOcLDWYyfeEdCE/wIDAQAB";
	//발급받은 RSA 퍼블릭키 
	
	private static String token = null;
	private static String aesKey = null;
	
	public static void main(String[] args)  {
		
		
		String testUserId = "USER_ID";
		
		SimplyPayAPI apiClient = null;
		
		try {
			apiClient = 
					new SimplyPayAPI(realm,rsaPublicKey);
			//apiClient.setDevServer(); //개발 서버로 테스트시 ..
			apiClient.setRealServer(); //실서버로 테스트시 ..
			
			apiClient.createSession(testUserId);
		}catch(SimplyPayException e) {
		
			e.printStackTrace();
			System.exit(0);
			
		}
		
		aesKey = apiClient.getAesKey();
		token = apiClient.getToken();
		
		
		//인증 받은 세션으로 새롭게 구성..
		//1. 카드 등록
		boolean retValue = false;
		
		
		try {
			apiClient = 
					new SimplyPayAPI(realm,rsaPublicKey,
							aesKey,
							token
							);
			//apiClient.setDevServer(); //개발 서버로 테스트시 ..
			apiClient.setRealServer(); //실서버로 테스트시 ..
			retValue = apiClient.registerCard(testUserId, 
					"테스트카드", 
					"944003805741XXXX" /*카드번호필요*/, 
					"202012" /*유효기간필요*/, 
					"00" /*비밀번호 앞2자리 필요*/, 
					"1198631214" /*사업자번호 혹은 생년월일*/, 
					"1111"); 
		}catch(SimplyPayException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		
		//2. 카드 목록 받기
		List< Map<?,?> > cardList = null;
		String firstCardUuid = null;
		
		try {
			cardList = apiClient.listCard(testUserId); 
			System.out.println("CARDLIST : "+cardList.toString());
			for (int i = 0 ; i < cardList.size(); i ++) {
				Map<?,?> item = cardList.get(i);
				System.out.println(" ==== "+(i+1)+"th Card for User : "+testUserId+" ==== ");
				System.out.println("CARD NO : "+item.get("plain_card_no"));
				System.out.println("CARD UUID : "+item.get("card_uuid"));
				System.out.println("CARD TITLE : "+item.get("title"));
				
				System.out.println("CARD TYPE : "+item.get("card_type"));
				System.out.println("CARD NAME : "+item.get("card_name"));
				
				if (i == 0) {
					firstCardUuid = item.get("card_uuid").toString();
				}
			}
			
		}catch(SimplyPayException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		if (firstCardUuid == null) {
			System.out.println("등록된 카드가 없습니다");
			System.exit(0);
		}
		
		//3. 카드 결제
		
		Map<?,?>  paymentData = null;
		
		try {
			paymentData = apiClient.payCard(testUserId, 
					firstCardUuid,
					"1111",
					0,
					914,
					90,
					0,
					"메모",
					"고객사전용부가데이터"); 
			
			System.out.println("PAY RESULT : "+paymentData.toString());
			
		}catch(SimplyPayException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		
		//4. 결제 취소 
		//결제 리턴 데이터는 항상 고객사에서 보관해두어야 , 취소에 사용할 수 있다.
		
		Map<?,?>  cancelData = null;
		
		try {
			cancelData = apiClient.cancelCard(testUserId, 
					paymentData.get("order_no").toString()); 
			
			System.out.println("CANCEL RESULT : "+cancelData.toString());
			
		}catch(SimplyPayException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		
		//5. 카드 삭제 
		
		
		try {
			retValue = apiClient.unregisterCard(testUserId, 
					firstCardUuid,
					"1111"); 
			
			
		}catch(SimplyPayException e) {
			e.printStackTrace();
			System.exit(0);
		}
		
		System.out.println(" ==== END OF UNIT TESTING ===== ");
		System.out.println(" ==== END OF UNIT TESTING ===== ");
		System.out.println(" ==== END OF UNIT TESTING ===== ");
		System.out.println(" ==== END OF UNIT TESTING ===== ");
		
	}

}
