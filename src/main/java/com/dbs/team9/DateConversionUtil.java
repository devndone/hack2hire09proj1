package com.dbs.team9;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateConversionUtil {

	public static String getCurrentDateInGMTForDB() {
		SimpleDateFormat dateFormat = new SimpleDateFormat(CommonConstants.DB_DATE_FORMAT);
		dateFormat.setTimeZone(TimeZone.getTimeZone(CommonConstants.TIMEZONE_GMT));
		return dateFormat.format(new Date());
	}
	
}
