package pe.telefonica.provision.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import pe.telefonica.provision.util.constants.Constants;

public class DateUtil {

	public static String dateToString(Date date) {
		return dateToString(date, Constants.DATE_FORMAT_WS);
	}

	public static String dateToString(Date date, String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
		return dateFormat.format(date);
	}

	public static Date stringToDate(String stringDate) throws ParseException {
		return stringToDate(stringDate, Constants.DATE_FORMAT_WS);
	}

	public static Date stringToDate(String stringDate, String format) {
		DateFormat dateFormat = new SimpleDateFormat(format);
		try {
			return dateFormat.parse(stringDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static LocalDateTime stringToLocalDateTime(String stringDate) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIMESTAMP_FORMAT_USER);
			LocalDateTime dateTime = LocalDateTime.parse(stringDate, formatter);
			return dateTime;
		} catch (Exception e) {
			return null;
		}
	}

	public static String localDateTimeToString(LocalDateTime date) {
		try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(Constants.TIMESTAMP_FORMAT_USER);
			return date.format(formatter);
		} catch (Exception e) {
			return null;
		}
	}

	public static Date getToday() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.HOUR_OF_DAY, 0);
		return now.getTime();
	}

	public static String getNowPsi(String format) {
		Date date = new Date();
		DateFormat dateFormat = new SimpleDateFormat(format);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
		return dateFormat.format(date);
	}
}
