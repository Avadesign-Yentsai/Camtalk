package com.avadesign.camvideo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EregiReplace 
{
	//EX:("(\r\n|\r|\n|\n\r| |)", "", "AAAAAA");
	public static String eregi_replace(String strFrom,String strTo,String strTarget)
	{
		String strPattern="(?i)"+strFrom;
		Pattern p =Pattern.compile(strPattern);
		Matcher m =p.matcher(strTarget);
		if(m.find())
		{
			return strTarget.replaceAll(strFrom, strTo);
		}
		else
		{
			return strTarget;
		}
	}
}
