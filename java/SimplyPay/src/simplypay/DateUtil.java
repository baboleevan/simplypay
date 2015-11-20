package simplypay;


import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateUtil {
	
	private static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ ");
	
	public static Date getCurrentSQLDate() {
		 Calendar currenttime = Calendar.getInstance () ; 
	     Date sqldate = new Date (( currenttime.getTime ()) .getTime ()) ; 
	     return sqldate;
	}

	public static Timestamp getCurrentSQLTimestamp() {
		 Calendar currenttime = Calendar.getInstance () ; 
		 Timestamp sqldate = new Timestamp (( currenttime.getTime ()) .getTime ()) ; 
	     return sqldate;
	}
	
	public static Timestamp getCurrentSQLTimestamp(String addType, int addDate) {
		 Calendar currenttime  = Calendar.getInstance () ;
		 if(addType.equals("day")) currenttime.add(Calendar.DATE, addDate);
		 else if(addType.equals("hour")) currenttime.add(Calendar.HOUR, addDate);
		 else if(addType.equals("minute")) currenttime.add(Calendar.MINUTE, addDate);
		 Timestamp sqldate = new Timestamp (( currenttime.getTime ()) .getTime ()) ; 
	     return sqldate;
	}
	
	public static Timestamp getRelativeSQLTimestamp(long millis) {
		 Calendar currenttime = Calendar.getInstance () ; 
		 Timestamp sqldate = new Timestamp (( currenttime.getTime ()) .getTime () + millis) ; 
	     return sqldate;
	}
	
	public static String formatTZ(java.util.Date date) {
		return dateFormat.format(date);
	}
	
	public static String formatTZ(java.sql.Timestamp date) {
		return dateFormat.format(date);
	}
}

