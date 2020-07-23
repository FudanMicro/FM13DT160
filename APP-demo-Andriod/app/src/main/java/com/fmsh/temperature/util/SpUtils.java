package com.fmsh.temperature.util;

import android.content.SharedPreferences;

/**
 * Created by wuyajiang on 2018/3/7.
 */
public class SpUtils {
	private static SharedPreferences sp;

	public static void putBooleanValue( String key,
			boolean value) {

		if (sp == null) {
			sp = UIUtils.getContext().getSharedPreferences("data", 0);
		}
		sp.edit().putBoolean(key, value).commit();
	}

	public static boolean getBooleanValue( String key,
			boolean defValue) {

		if (sp == null) {
			sp = UIUtils.getContext().getSharedPreferences("data", 0);
		}
		return sp.getBoolean(key, defValue);
	}

	public static void putStringValue( String key,
			String value) {

		if (sp == null) {
			sp = UIUtils.getContext().getSharedPreferences("data", 0);
		}
		sp.edit().putString(key, value).commit();
	}

	public static String getStringValue( String key,
			String defValue) {

		if (sp == null) {
			sp = UIUtils.getContext().getSharedPreferences("data", 0);
		}
		return sp.getString(key, defValue);
	}

	public static void rmove( String key) {
		// TODO Auto-generated method stub
		if (sp == null) {
			sp = UIUtils.getContext().getSharedPreferences("data", 0);
		}
		sp.edit().remove(key).commit();
	}

	public static int getIntValue( String key,
										int defValue) {

		if (sp == null) {
			sp = UIUtils.getContext().getSharedPreferences("data", 0);
		}
		return sp.getInt(key, defValue);
	}

	public static void putIntValue( String key,
									  int value) {

		if (sp == null) {
			sp = UIUtils.getContext().getSharedPreferences("data", 0);
		}
		sp.edit().putInt(key, value).commit();
	}

}
