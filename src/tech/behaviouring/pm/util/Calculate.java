package tech.behaviouring.pm.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import tech.behaviouring.pm.util.DataStructures.TimeOfDay;

/*
 * Created by Mohan on 7/1/2016
 */

// Commonly performed calculations in our app

public class Calculate {

	// Add number of months to given date and return new date as a string

	public static String addMonthsToDate(Date date, int nMonths) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, nMonths);
		String newDate = sdf.format(c.getTime());
		return newDate;
	}

	// Add number of days to given date and return new date as a string

	public static String addDaysToDate(Date date, int nDays) {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, nDays);
		String newDate = sdf.format(c.getTime());
		return newDate;
	}

	// Add number of days to given date and return new date as a Date object

	public static Date addDaysToDate1(Date date, int nDays) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_YEAR, nDays);
		return c.getTime();
	}

	// Add number of months to given date and return new date as a Date object

	public static Date addMonthsToDate1(Date date, int nMonths) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MONTH, nMonths);
		return c.getTime();
	}

	// Calculate difference between two dates in days
	public static int dateDiffInDays(Date date1, Date date2) {
		long diff = date2.getTime() - date1.getTime();
		double daysDiff = diff / (1000 * 60 * 60 * 24);
		return (int) daysDiff;
	}

	// Return whether its Morning or Evening based on time

	public static TimeOfDay getTimeOfDay() {
		Calendar c = Calendar.getInstance();
		if (c.get(Calendar.HOUR_OF_DAY) < 12)
			return TimeOfDay.Morning;
		else
			return TimeOfDay.Evening;
	}

	// Get today's date without time

	public static Date getTodayDateWithoutTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			Date dateWithoutTime = sdf.parse(sdf.format(new Date()));
			return dateWithoutTime;
		} catch (ParseException e) {
			return new Date();
		}
	}
}
