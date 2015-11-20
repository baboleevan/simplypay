<?

	
	
	//set_include_path( APP_SYSTEM_ROOT); 
	require ("Crypt/RSA.php");
	require ("SimplyPay/SimplyPayAPI.php");
	
	
	$rsaKey = '-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm7uQAOYvS4oYWd5UFUeZPobo28QWBE/ACK7xmxghmmPa7XmxjoeejCZyl1wit+KTZB/ZX2vaVmU/jZHip4y70FeJhvrB02UjcYkQOq8xIj/fCbNlr6yAzh07Z7wlP1NU/IAyCIwG90KawW2Ve6v6TRSY+0SAeAsr/Pc4ZA1GeWg9+NugNvvzziqMQDTwXJYbjH3Oo3PnzGz2Kof+NV11Z8xDxC/gv0mX+EXm2WJGJDl32MMpEXExGoL1kh7I0I5Ix+VW6lOlticzYTQLJfokzCq2bmU/+9VFjGNpB/o2D896XE/eb0p0PXJQ8NTKxPusmo9LdWOOcLDWYyfeEdCE/wIDAQAB
-----END PUBLIC KEY-----';

	$realm = "tester";
	$screen_name = "test";
	
	$api = new SimplyPayAPI($realm,$rsaKey);
	$api->setDevServer();
	
	try {
		
		$vUser = null;
		$vAesKey = null;
		$vToken = null;
		$vUUID = null;
		$vOrderNo = null;
		
		$has_error = false;
		
		$json = $api->createSession($screen_name);
		
		if (isset($json) && isset($json->code)) {
	  		
	  		if ($json->code == 200) {
				$vUser = $json->data->user;
				$vAesKey = $json->data->key;	  			
				$vToken = $json->data->token;
	  			
	  		}else {
	  			$has_error = true;
	  		}
	  	}else {
	  		$has_error = true;
	  	}
	  	
	  	if ($has_error) exit(0);
	  	
	  	$api = new SimplyPayAPI($realm,$rsaKey,$vAesKey,$vToken);
	  	$api->setDevServer();
	  	
	  	$json = $api->registerCard(
		  	$screen_name,
		  	"테스트카드",
		  	"5424160189XXXXXX",
		  	"202007",
		  	"09",
		  	"800616",
		  	"1111"
	  	);
	  	
	  	
	  	if (isset($json) && isset($json->code)) {
	  		
	  		if ($json->code == 200) {
				//PASSED
	  			
	  		}else {
	  			$has_error = true;
	  		}
	  	}else {
	  		$has_error = true;
	  	}
	  	
	  	if ($has_error) exit(0);
	  	
	  	
	  	
	  	$json = $api->listCard(
		  	$screen_name
	  	);
	  	
	  	if (isset($json) && isset($json->code)) {
	  		
	  		if ($json->code == 200) {
				$vUUID = $json->data->list[0]->card_uuid;
	  			
	  		}else {
	  			$has_error = true;
	  		}
	  	}else {
	  		$has_error = true;
	  	}
	  	
	  	if ($has_error) exit(0);
	  	
	  	
	  	$json = $api->payCard(
		  	$screen_name,
		  	$vUUID,
		  	"1111",
		  	0,
		  	1004 + (rand() %100),
		  	0,
		  	0
	  	);
	  	
	  	if (isset($json) && isset($json->code)) {
	  		
	  		if ($json->code == 200) {
				$vOrderNo = $json->data->order_no;
	  			
	  		}else {
	  			$has_error = true;
	  		}
	  	}else {
	  		$has_error = true;
	  	}
	  	
	  	if ($has_error) exit(0);
	  	
	  	
	  	$json = $api->cancelCard(
		  	$screen_name,
		  	$vOrderNo
	  	);
	  	
	  	if (isset($json) && isset($json->code)) {
	  		
	  		if ($json->code == 200) {
				//
	  			
	  		}else {
	  			$has_error = true;
	  		}
	  	}else {
	  		$has_error = true;
	  	}
	  	
	  	if ($has_error) exit(0);
	  	
	  	
	  	
	  	
	  	
	  	
	  	
	  	$json = $api->unregisterCard(
		  	$screen_name,
		  	$vUUID,
		  	"1111"
	  	);
	  	
	  	if (isset($json) && isset($json->code)) {
	  		
	  		if ($json->code == 200) {
				//
	  			
	  		}else {
	  			$has_error = true;
	  		}
	  	}else {
	  		$has_error = true;
	  	}
	  	
	  	if ($has_error) exit(0);
	  	
	  	
	  	$json = $api->listCard(
		  	$screen_name
	  	);
	  	
	  	if (isset($json) && isset($json->code)) {
	  		
	  		if ($json->code == 200) {
				
	  			
	  		}else {
	  			$has_error = true;
	  		}
	  	}else {
	  		$has_error = true;
	  	}
	  	
	  	if ($has_error) exit(0);
	  	
	  	
	  	
	}catch(Exception $e) {
		print_r($e);
	}

	
	
?>
