package com.demo.util;

import java.text.SimpleDateFormat;

public class TimeUtil {
	
	public static String millionSecondToClockTime(long millionSecond) {
		millionSecond = millionSecond - 8 * 3600 *1000;
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		return sdf.format(millionSecond);
	}

}
