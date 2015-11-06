package com.manu.dynasty.util;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author 李康
 * 2013-10-14 下午04:59:57
 *
 */
public class TimeUtil {

	/**
	 * 跟当前时间比是不是同一天
	 * @param currentDate
	 * @param judgeDate
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public static boolean isSameDay(Date currentDate, Date judgeDate) {
		return currentDate.getDate() == judgeDate.getDate()
				&& currentDate.getMonth() == judgeDate.getMonth()
				&& currentDate.getYear() == judgeDate.getYear();
	}

	/**
	 * 计算日期相隔天数
	 * @param date1
	 * @param date2
	 * @return
	 */
	public static int calcDaysMinus(Date currentDate, Date judgeDate) {
		currentDate = getDayBegin(currentDate);
		judgeDate = getDayBegin(judgeDate);
		return (int) (currentDate.getTime() - judgeDate.getTime())
		/ (1000 * 60 * 60 * 24);
	}

	
	public static Date getDayEnd(Date date) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		Date nextDayBegin = getDayBegin(calendar.getTime());
		return new Date(nextDayBegin.getTime() - 1);
	}
	
	
	public static Date getDayBegin(Date date) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
}
