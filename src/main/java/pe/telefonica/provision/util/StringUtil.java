package pe.telefonica.provision.util;

public class StringUtil {

	public static String getValue(String newValue, String oldValue) {
		if (newValue != null && !newValue.isEmpty()) {
			return newValue;
		} else {
			return oldValue;
		}

	}
}
