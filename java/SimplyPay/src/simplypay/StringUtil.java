package simplypay;


import java.io.BufferedReader;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

public class StringUtil {
	
	
    /** @param filePath the name of the file to open. Not sure if it can accept URLs or just filenames. Path handling could be better, and buffer sizes are hardcoded
    */ 

	public static String html2textWithRegex(String html) {
		
		final Pattern tagPattern = Pattern.compile("<([^\\s>/]+).*?>");
		final Matcher matcher = tagPattern.matcher(html);
		final StringBuffer sb = new StringBuffer(html.length());
		while(matcher.find()){
		    matcher.appendReplacement(sb,"");
		}
		matcher.appendTail(sb);

		final String parsedText = sb.toString();
		return parsedText;
	}
	
    public static float versionToFloat(String version) {
    	
    	version = version.replace(".", "");

    	
    	float v = 0.0f;
    	try {
    		v = Float.parseFloat(version);
    		
    	}catch(Exception e) {
    		
    	}
    	
    	return v;
    }
	public static String toCurrency(int num) {
		  DecimalFormat df = new DecimalFormat("#,###");
		  return df.format(num);
	}
	
	
	public static String readFileAsString(String filePath)
    throws java.io.IOException{
        StringBuffer fileData = new StringBuffer(1000);
        BufferedReader reader = new BufferedReader(
                new FileReader(filePath));
        char[] buf = new char[1024];
        int numRead=0;
        while((numRead=reader.read(buf)) != -1){
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }
	
	public static String cut(String str,int len,String tail) {
		
		if (str.length() <= len) {
			return str;
		}else {
			str = str.substring(0,len) + tail;
		}
		return str;
	}
	
	public static String cut(String str,int len) {
		
		return cut(str,len,"");
	}

   
	public static boolean isValidEmail(String inputStr) {
		boolean err = false;
		Pattern p = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
		
		Matcher m = p.matcher(inputStr);
		if( !m.matches() ) {
			err = true; 
		}
		return !err;
	}
	
	
	
	public static boolean isValidPhone(String inputStr) {
		boolean err = false;
		String regex = "^(070|02|031|032|033|041|042|043|051|052|053|054|055|061|062|063|064)-\\d{3,4}-\\d{4}$";

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(inputStr);
		if( !m.matches() ) {
			err = true; 
		}
		return !err;
	}
	
	public static boolean isValidMobile(String inputStr) {
		boolean err = false;
		String regex = "^(010|011|016|017|018|019)-\\d{3,4}-\\d{4}$";

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(inputStr);
		if( !m.matches() ) {
			err = true; 
		}
		return !err;
	}
	
	public static boolean isPossibleMobile(String inputStr) {
		boolean err = false;
		String regex = "^(010|011|016|017|018|019)\\d{7,8}$";

		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(inputStr);
		if( !m.matches() ) {
			err = true; 
		}
		return !err;
	}
	
	public static String filterNumeric(final String string) {
	    if (string == null || string.length() == 0) {
	        return "";
	    }
	    return string.replaceAll("[^0-9]+", "");
	}
	
	public static String filterAlphaNumeric(final String string) {
	    if (string == null || string.length() == 0) {
	        return "";
	    }
	    return string.replaceAll("[^a-zA-Z0-9]+", "");
	}
	
	public static String filterAlphaNumericHyphen(final String string) {
	    if (string == null || string.length() == 0) {
	        return "";
	    }
	    return string.replaceAll("[^a-zA-Z0-9-]+", "");
	}
	
	
    public static String getPrettyJson(String jsonString) {

        final String INDENT = "    ";
        StringBuffer prettyJsonSb = new StringBuffer();

        int indentDepth = 0;
        String targetString = null;
        for(int i=0; i<jsonString.length(); i++) {
            targetString = jsonString.substring(i, i+1);
            if(targetString.equals("{")||targetString.equals("[")) {
                prettyJsonSb.append(targetString).append("\n");
                indentDepth++;
                for(int j=0; j<indentDepth; j++) {
                    prettyJsonSb.append(INDENT);
                }
            }
            else if(targetString.equals("}")||targetString.equals("]")) {
                prettyJsonSb.append("\n");
                indentDepth--;
                for(int j=0; j<indentDepth; j++) {
                    prettyJsonSb.append(INDENT);
                }
                prettyJsonSb.append(targetString);
            }
            else if(targetString.equals(",")) {
                prettyJsonSb.append(targetString);
                prettyJsonSb.append("\n");
                for(int j=0; j<indentDepth; j++) {
                    prettyJsonSb.append(INDENT);
                }
            }
            else {
                prettyJsonSb.append(targetString);
            }

        }


        return prettyJsonSb.toString();

    }
    
    
    
    public static Object compatibleJsonObject(Object value)
	{
		if (value == null) {
			return "";
		}
		else if (value instanceof Map<?,?>) {
			return StringUtil.compatibleJsonMap((Map<?,?>)value);
		}
		else if (value instanceof List<?>) {
			return StringUtil.compatibleJsonList((List<?>)value);
		}
		else if (value instanceof java.util.Date) {
			return DateUtil.formatTZ((java.util.Date)value);
		}
		else if (value instanceof java.sql.Timestamp) {
			return DateUtil.formatTZ((java.sql.Timestamp)value);
		}
		else if (value instanceof String) {
			return value.toString();
			
		}
		else {
			ObjectMapper mapper = new ObjectMapper(); 
			return mapper.convertValue(value, JsonNode.class);
		}
		
	}
	
	public static List<?> compatibleJsonList(List<?> list) {
		
		
		List<Object> newList = new ArrayList<Object>();
		
		for (int i = 0; i< list.size(); i++) {
			Object value = list.get(i);
			newList.add(StringUtil.compatibleJsonObject(value));
		}
		
		return newList;
	}
	
	
	
	public static Map<?,?> compatibleJsonMap(Map<?,?> data) {
		
		
		Map<?,?> map = (Map<?,?>) data;
		Map<String,Object> newMap = new Hashtable<String,Object>();
		Set<?> keys = map.keySet();
		Iterator<?> iter = keys.iterator();
		while(iter.hasNext()) {
			Object key = iter.next();
			Object value = map.get(key);
			if (key == null) continue;
			newMap.put(key.toString(), StringUtil.compatibleJsonObject(value));
		}
		return newMap;
	}
	
	public static String getFileBasename(String fileName) {

		String ext = "mp4";
		if (fileName.indexOf(".") > -1) {
			int pos = fileName.lastIndexOf(".");
			ext = fileName.substring(pos+1);
		}
		fileName = fileName.replace("."+ext, "");
		return fileName;
	}
	
	public static String getRangedString(String str,String del1,String del2) {
		
		int pos1 = str.indexOf(del1);
		if (pos1 == -1) return "";
		pos1 = pos1+del1.length();
		while(pos1 < str.length()) {
			
			if (str.charAt(pos1) == ' ' || str.charAt(pos1) == '\t' ) pos1++;
			else break;
		}
		
		int pos2 = str.indexOf(del2,pos1);
		if (pos2 == -1) return "";
		return str.substring(pos1,pos2);
		
	}
	
	
	public static Map<?,?> jsonToMap(JsonNode node) {
		ObjectMapper mapper = new ObjectMapper();
		Map<?, ?> result = mapper.convertValue(node, Map.class);
		return result;
	}
	
	

}