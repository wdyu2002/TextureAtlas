package com.devculture.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {
	
	public static String DEFAULT_APPLE_DATE_FORMAT = "MM/dd/yyyy";
	
	public static Date getDate(String date) throws Exception {
		SimpleDateFormat format = new SimpleDateFormat(DEFAULT_APPLE_DATE_FORMAT);
		return format.parse(date);
	}
	
}
