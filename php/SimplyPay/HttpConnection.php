<?
	
	/*
	 * HTTP Connection 연결 클래스
	 */

	class HttpConnection {
		
		var $method;
		var $url ;
		var $data;
		var $hash;
		var $usecookie;
		var $cookiefilename;
		var $cookiecontent;

		var $info;
		var $html;
		var $error;
		var $errno;
		var $debug;
		
		public $nCTimeout = 30;
		public $nTimeout = 30;

		static $referrer = "";

		/* 생성자 , GET/POST , URL , 인자들 */
		function __construct($method , $url , $params = array() , $encode = true) {
			$method = strtolower($method);

			if ($encode)
				$url = $this->url_encode($url);
			
			
			
			if ($method == "post") {
				
				
					
				if (is_array($params)) {
					/*
					$files = array();
					$fields = array();
					
					foreach($params as $key=>$value) {
						
						if (strlen($value) > 0 && $value{0} == "@") {
							$files[$key] = $value;
						}else {
							$fields[$key] = $value;
						}
					}		
					*/
					
					$data = http_build_query($params);
					
					if ($encode)
						$data = $this->url_encode($data);
					
				}else $data = $params;

				$this->data = $data;
			}else if ($method == "upload") {
			
				$this->data = $params;
			}
			
			$this->url = $url;
			
			$this->method = $method;

			

			
			$this->hash = md5($this->url.$this->data);

			if (HttpConnection::$referrer == "")
				HttpConnection::$referrer = "http://www.naver.com/";
			else
				HttpConnection::$referrer = $this->url;
			
			$this->debug = false;
			$this->usecookie = false;
		}
		
		
		/* HTTP 응답코드 얻기 */
		function getHttpCode() {
			return $this->info['http_code'];
		}

		/* REQUEST 실행하기 , force가 false면 자동으로 캐쉬 콘트롤 */
		function request($headers=array()) {
			
			
			
			
				

				if ($this->debug) {
					echo "Online doc : ".$this->url."\n";
				}
				


				$ch = curl_init();
				curl_setopt($ch, CURLOPT_URL, $this->url);
				curl_setopt($ch, CURLOPT_TIMEOUT, $this->nCTimeout);
				curl_setopt($ch, CURLOPT_CONNECTTIMEOUT, $this->nTimeout );
				curl_setopt($ch, CURLOPT_FOLLOWLOCATION  ,1); 
				curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
				curl_setopt($ch, CURLOPT_USERAGENT, 'Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_2 like Mac OS X; en-us) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5');
				curl_setopt($ch , CURLOPT_REFERER, HttpConnection::$referrer);
				curl_setopt($ch,  CURLOPT_SSL_VERIFYPEER, false);
				
				if (is_array($headers) && count($headers) > 0 ) {
					 curl_setopt($ch, CURLOPT_HTTPHEADER, $headers); 
					 
					 if ($this->debug) {
						echo "HEADER DATA : \n";
						IO::dump($headers);
					 }
				}
				
				if ($this->usecookie) {
					curl_setopt($ch, CURLOPT_COOKIEFILE, $this->cookiefilename);
					curl_setopt($ch, CURLOPT_COOKIEJAR, $this->cookiefilename);
					//curl_setopt($ch, CURLOPT_COOKIE, true);
					curl_setopt($ch, CURLOPT_COOKIE, $this->cookiecontent);
    			}


				if ($this->method == "post" || $this->method == "upload") {
					
					curl_setopt($ch, CURLOPT_POST,true);
					curl_setopt($ch, CURLOPT_POSTFIELDS, $this->data);
					
					if ($this->debug) {
						echo "POSTDATA : ".$this->data."\n";
					}
					
				}

				$this->html = curl_exec($ch);
				$this->info = curl_getinfo($ch);
				$this->error = curl_error($ch);
				$this->errno = curl_errno($ch);
				
					

				curl_close($ch);
				if ($this->info['url'] != $this->url) {
					//URL MODIFIED
					$this->url = $this->info['url'];
					//echo "URL MODIFIED!!!!!!!";
				}


			return $this->html;
		}

		/* url parsing */
		function parse_url(&$url) {
			
			$url = $this->url;
			return parse_url($this->url);
		}

		
		/* 에러 메시지 얻기 */
		function get_error() {

			return array("errno"=>$this->errno,"error",$this->error);
		}
		
		/* URL path 수정 */
		
		function filter_path($path) {
	        $path = str_replace(array('/', '\\'), DIRECTORY_SEPARATOR, $path);
		    $parts = array_filter(explode(DIRECTORY_SEPARATOR, $path), 'strlen');
			$absolutes = array();
	
			foreach ($parts as $part) {
				if ('.' == $part) continue;
				if ('..' == $part) {
					array_pop($absolutes);
				} else {
					$absolutes[] = $part;
				}
	        }
		    return implode(DIRECTORY_SEPARATOR, $absolutes);
	    }

		/* URL ENCODING */
		function url_encode($url)
		{
				// safely cast back already encoded "&" within the query
				$urldata = parse_url($url); //path , host , scheme
				$pathdata = pathinfo($urldata['path']); //dirname , basename , extesion
				$query = isset($urldata['query']) ? $urldata['query'] : "";
				
				//$qs = explode("&",$query);
				parse_str($query,$qs);
				$query = "";
				foreach($qs as $k=> $d) {
					//if (trim($data) == "") continue;
					//list($k,$v) = explode("=",$data);
					$d = trim($d);
					unset($qs[$k]);
					if (strpos($urldata['host'],"127") !== false)
						$k = str_replace("_",".",$k);
					//$qs[$k] = $d;
					if ($query != "") $query.="&";
					
					//echo "K : ".$k."/".$d."\n";
					$query .= ($k)."=".(rawurlencode($d));
				}
				
				$rurl = "";
				
				if (isset($urldata['scheme']) && $urldata['scheme'] != "") {
					if (isset($urldata['port']) && $urldata['port'] != "80" )
						$rurl = $urldata['scheme'] ."://".$urldata['host'].":".$urldata['port'].$urldata['path'];
					else
						$rurl = $urldata['scheme'] ."://".$urldata['host'].$urldata['path'];

				}else {
					$rurl = $urldata['path'];
				}
				
				if ($query != "") $rurl .= "?".$query;
				//warn($rurl);
				return $rurl;
		}


	}



?>
