package com.neu.cloud.cloudapp.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

	public static boolean isOnlyNumber(String str) {
		try {
			int p = Integer.parseInt(str);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isOnlyText(String str) {
		try {
			return ((str != null) && (!str.trim().equals(""))
					&& (str.chars().allMatch(ch -> Character.isLetter(ch) || Character.isWhitespace(ch))));
		} catch (Exception e) {
			return false;
		}
	}

	// same as above
	public static boolean isOnlyTextWithWhiteSpaces(String str) {
		try {
			return ((str != null) && (!str.equals("")));
		} catch (Exception e) {
			return false;
		}
	}

	public static Date convertStringToDate(String str) {
		try {
			return new SimpleDateFormat("dd/MM/yyyy").parse(str);
		} catch (ParseException ex) {
			return null;
		}
	}

	public static String convertDateToString(Date date) {
		try {
			SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
			return formatter.format(date);
		} catch (Exception e) {
			return null;
		}
	}

	public static boolean isEmailValidated(String email) {
		try {
			if (isValidString(email) == false) {
				return false;
			}
			String regex = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(email);
			return matcher.matches();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isPhoneNumberVerified(String phone) {
		try {
			String regex = "^(\\+\\d{1,2}\\s?)?1?\\-?\\.?\\s?\\(?\\d{3}\\)?[\\s.-]?\\d{3}[\\s.-]?\\d{4}$";
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(phone);
			return matcher.matches();
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isOnlyTextWithCharAndNums(String str) {
		try {
			return ((str != null) && (!str.equals("")) && (str.chars()
					.allMatch(ch -> Character.isLetter(ch) || Character.isWhitespace(ch) || Character.isDigit(ch))));
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isValidString(String str) {
		try {
			return ((str != null) && (!str.equals("")));
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isValidNumber(String str) {
		try {
			if ((str != null) && (!str.equals(""))) {
				int p = Integer.parseInt(str);
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}

	}

}