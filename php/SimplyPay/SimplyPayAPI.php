<?

	require_once ("HttpConnection.php");
	
	class SimplyPayAPI {
		
		
		public function __construct() 
	    { 
	        $a = func_get_args(); 
	        $i = func_num_args(); 
	        if (method_exists($this,$f='__construct'.$i)) { 
	            call_user_func_array(array($this,$f),$a); 
	        } 
	    } 
	    
		
		public function __construct2($realm , $rsaKey) {
			
			
			if (!$realm || $realm == "") {
				throw new Exception("REALM 값이 필요합니다");
			}
			
			if (!$rsaKey || $rsaKey == "") {
				throw new Exception("RSA Key값이 필요합니다");
			}
			
			
			$this->realm = $realm;
			$this->rsaKey = $rsaKey;
			
			$rsa = new Crypt_RSA();
			$rsa->setEncryptionMode(CRYPT_RSA_ENCRYPTION_PKCS1);
		    $rsa->loadKey($this->rsaKey); 
		    
		    $this->rsa = $rsa;
			
			
		}
		
		public function __construct4($realm , $rsaKey,$aesKey,$token) {
			$this->__construct2($realm,$rsaKey);
			$this->aesKey = $aesKey;
			$this->token = $token;
			
			try {
				base64_decode($aesKey);
			}catch(Exception $e) {
				throw new Exception("올바르게 암호화된 AES KEY가 아닙니다.");
			}
			
			if (!$token || $token == "") {
				throw new Exception("세션 토큰이 필요합니다.");
			}
			
		}
		

		private function aes_encrypt($sKey,$sStr) {
		  return base64_encode(
		    mcrypt_encrypt(
		        MCRYPT_RIJNDAEL_128, 
		        base64_decode($sKey),
		        $this->pkcs5_pad($sStr,16),
		        MCRYPT_MODE_ECB
		    )
		  );
		}
		
		private function aes_decrypt($sKey,$sStr) {
		  return mcrypt_decrypt(
		    MCRYPT_RIJNDAEL_128, 
		    base64_decode($sKey), 
		    base64_decode($sStr), 
		    MCRYPT_MODE_ECB
		  );
		}
		
		private function pkcs5_pad ($text, $blocksize = 16) { 
		  $pad = $blocksize - (strlen($text) % $blocksize); 
		  return $text . str_repeat(chr($pad), $pad); 
		}
		
		private $DEBUG = true;
		private $rsaKey ;
		private $aesKey ;
		private $realm;
		private $token ;
		private $host = "www.kkokjee.com" ;
		private $scheme = "https" ;
		private $port = 443;
		private $rsa;
		
		private $encTable = array(
			"token"=>"plain",
			"realm"=>"plain",
			"callback_url"=>"plain",
			"screen_name"=>"rsa"	
		);
		
		public static $CODE_OK = 200;
		public static $CODE_ERROR = 400;		
		
		private static $PATH_CREATE_SESSION = "/simple/v1/create_session";
		private static $PATH_REGISTER_CARD = "/simple/v1/register_card";
		private static $PATH_LIST_CARD = "/simple/v1/card_list";
		private static $PATH_UNREGISTER_CARD = "/simple/v1/unregister_card";
		private static $PATH_PAY_CARD = "/simple/v1/do_pay_card";
		private static $PATH_CANCEL_CARD = "/simple/v1/do_cancel_card";
		
		
		public function setDevServer() {
			$this->host = "dev.kkokjee.com";
			$this->scheme = "http";
			$this->port = 80;
		}
		
		public function setRealServer() {
			$this->host = "www.kkokjee.com";
			$this->scheme = "https";
			$this->port = 443;
		}
		
		public function getToken() {
			return $this->token;
		}
		
		public function getAesKey() {
			return $this->aesKey;
		}
		
		public function encryptPostData($data) {
			$encdata = array();
			foreach($data as $key=>$value) {
				if (!isset($this->encTable[$key])) {
					if ($this->aesKey != null) {
						$encdata[$key] = $this->aes_encrypt($this->aesKey,$value);
					}
				}else {
					$enc = $this->encTable[$key];
					if ($enc == "plain") {
						$encdata[$key] = $value;
					}else {
						$encdata[$key] = base64_encode($this->rsa->encrypt($value));
					}
				}
			}
			return $encdata;
		}
		
		public function callApi($endpoint , $post_data) {
			
			if ($this->DEBUG) {
				echo "BEFORE ENCRYPTING\n";
				print_r($post_data);
			}


			$enc_data = $this->encryptPostData($post_data);
			$scheme = $this->scheme;
			$host = $this->host;
			$url = "${scheme}://${host}${endpoint}";
			
			if ($this->DEBUG) {
				echo "REQUEST : ${url}\n";
				print_r($enc_data);
			}
			
			$http = new HttpConnection("post",$url , $enc_data); 
		  	$json = json_decode($http->request());
			if ($this->DEBUG) {
				print_r($json);
			}
			return $json;
		}
		
		public function createSession($screen_name) {
			
			if (!$this->realm || $this->realm == "") {
				throw new Exception("REALM 값이 필요합니다");
			}
			
			if (!$screen_name || $screen_name == "") {
				throw new Exception("유저 아이디 값이 필요합니다");
			}
			
			$post_data = array(
				"realm"=>$this->realm,
				"screen_name"=>$screen_name,
				"callback_url"=>"reserved_for_future"
			);
			
			$json = $this->callApi(SimplyPayAPI::$PATH_CREATE_SESSION , $post_data);
			return $json;
		}
		
		
		public function registerCard(
			$screen_name,
			$title,
			$card_no,
			$card_date,
			$card_secure,
			$ssn,
			$pin
			
		) {
			
			try {
				base64_decode($this->aesKey);
			}catch(Exception $e) {
				throw new Exception("올바르게 암호화된 AES KEY가 아닙니다.");
			}
			
			if (!$this->token || $this->token == "") {
				throw new Exception("세션 토큰이 필요합니다.");
			}
			
			$post_data = array(
				"realm"=>$this->realm,
				"token"=>$this->token,
				"screen_name"=>$screen_name,
				"card_no"=>$card_no,
				"card_date"=>$card_date,
				"card_secure"=>$card_secure,
				"ssn"=>$ssn,
				"pin"=>$pin
			);
			
			$json = $this->callApi(SimplyPayAPI::$PATH_REGISTER_CARD , $post_data);
			return $json;
			
		}
		
		
		public function listCard($screen_name) {
			
			try {
				base64_decode($this->aesKey);
			}catch(Exception $e) {
				throw new Exception("올바르게 암호화된 AES KEY가 아닙니다.");
			}
			
			if (!$this->token || $this->token == "") {
				throw new Exception("세션 토큰이 필요합니다.");
			}
			
			$post_data = array(
				"realm"=>$this->realm,
				"token"=>$this->token,
				"screen_name"=>$screen_name
			);
			
			$json = $this->callApi(SimplyPayAPI::$PATH_LIST_CARD , $post_data);
			return $json;
		}
		
		
		public function payCard(
			$screen_name,
			$card_uuid,
			$pin,
			$installment,
			$amount,
			$tax,
			$fee,
			$comment = "",
			$additional_data = "",
			$customer_name = "",
			$customer_email = "",
			$customer_phone = "",
			$customer_mobile = ""
		) {
			
			try {
				base64_decode($this->aesKey);
			}catch(Exception $e) {
				throw new Exception("올바르게 암호화된 AES KEY가 아닙니다.");
			}
			
			if (!$this->token || $this->token == "") {
				throw new Exception("세션 토큰이 필요합니다.");
			}
			
			$post_data = array(
				"realm"=>$this->realm,
				"token"=>$this->token,
				"screen_name"=>$screen_name,
				"card_uuid"=>$card_uuid,
				"pin"=>$pin,
				"installment"=>$installment,
				"amount"=>$amount,
				"tax"=>$tax,
				"fee"=>$fee,
				
				"comment"=>$comment,
				"additional_data"=>$additional_data,
				
				"customer_name"=>$customer_name,
				"customer_email"=>$customer_email,
				"customer_phone"=>$customer_phone,
				"customer_mobile"=>$customer_mobile
			);
			
			$json = $this->callApi(SimplyPayAPI::$PATH_PAY_CARD , $post_data);
			return $json;
		}
		
		
		public function cancelCard(
			$screen_name,
			$order_no
		)
		{
			
			try {
				base64_decode($this->aesKey);
			}catch(Exception $e) {
				throw new Exception("올바르게 암호화된 AES KEY가 아닙니다.");
			}
			
			if (!$this->token || $this->token == "") {
				throw new Exception("세션 토큰이 필요합니다.");
			}
			
			$post_data = array(
				"realm"=>$this->realm,
				"token"=>$this->token,
				"screen_name"=>$screen_name,
				"order_no"=>$order_no
			);
			
			$json = $this->callApi(SimplyPayAPI::$PATH_CANCEL_CARD , $post_data);
			return $json;
		}
		
		
		public function unregisterCard(
			$screen_name,
			$card_uuid,
			$pin
		) {
			
			try {
				base64_decode($this->aesKey);
			}catch(Exception $e) {
				throw new Exception("올바르게 암호화된 AES KEY가 아닙니다.");
			}
			
			if (!$this->token || $this->token == "") {
				throw new Exception("세션 토큰이 필요합니다.");
			}
			
			$post_data = array(
				"realm"=>$this->realm,
				"token"=>$this->token,
				"screen_name"=>$screen_name,
				"card_uuid"=>$card_uuid,
				"pin"=>$pin
			);
			
			$json = $this->callApi(SimplyPayAPI::$PATH_UNREGISTER_CARD , $post_data);
			return $json;
		}
		
		
	}


?>