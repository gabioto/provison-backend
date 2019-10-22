package pe.telefonica.provision.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import pe.telefonica.provision.util.constants.Constants;

public class DateUtil {

	public static String dateToString(Date date) {
		DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_WS);
		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT-5:00"));
		return dateFormat.format(date);
	}

	public static Date stringToDate(String stringDate) {
		DateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT_WS);
		try {
			return dateFormat.parse(stringDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
