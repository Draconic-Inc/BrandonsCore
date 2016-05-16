package com.brandon3055.brandonscore.utils;

import java.util.Locale;

/**
 * Created by brandon3055 on 31/3/2016.
 */
public class ArrayUtils {

	public static String[] arrayToLowercase(String[] array){
		String[] lowercaseArray = new String[array.length];
		for (int i = 0; i < array.length; i++){
			lowercaseArray[i] = array[i].toLowerCase(Locale.ENGLISH);
		}
		return lowercaseArray;
	}
}
