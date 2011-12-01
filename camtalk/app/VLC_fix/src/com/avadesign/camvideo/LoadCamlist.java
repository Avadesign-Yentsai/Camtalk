package com.avadesign.camvideo;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import android.util.Log;


public class LoadCamlist 
{
	private static String[] caminfo = null; //�ӧO�@�x IPCAM ���ݩʶ��X
	private static String[][] camattr = null; //[i][j], �� i �x IPCAM ���� j ���ݩ�
	
	public static String[][] load(String Email, String Pwd) 
	{
		String TAG="camtalk/LoadCamlist";
		
		String[] input = new String[5];
		
		input[0] = ServerInfo.getWebPath()+"loadcamlist.jsp";
		input[1] = "Email";
		input[2] = Email;
		input[3] = "Pwd";
		input[4] = Pwd;
		
		String re = HttpPostResponse.getHttpResponse(input);
		re = EregiReplace.eregi_replace("(\r\n|\r|\n|\n\r| |)", "", re);
		
		if(!re.equals("[]") && !re.equals(""))//IP Cam ��Ƥ�����
        {
        	String key[] = {"id","name","talkac","talkpw","talkport","videoport","videocode","ip"}; //�����M�^�Ǫ�JSON��ƲŦX ,�W�٬Ojsp�̪�CamInfo.java�]�w��,�Y�ק�,getItem()�]������
        	
        	/*���R�Ǧ^����ƶ}�l*/
	        re = re.replace("[", "");
	        re = re.replace("]", "");
	        
	        caminfo=re.split("\\},\\{");
	        
	        camattr = new String[caminfo.length][key.length];
	        
	        for(int i=0; i<caminfo.length; i++)
	        {
	        	caminfo[i] = caminfo[i].toString().replace("{", "");
	        	caminfo[i] = caminfo[i].toString().replace("}", "");
	        	StringBuffer sb = new StringBuffer("{");
	        	sb = sb.append(caminfo[i].toString());
	        	sb = sb.append("}");
	        	
	        	for(int j=0; j<key.length; j++)
	        	{
		        	JSONParser parser = new JSONParser();
		        	KeyFinder finder = new KeyFinder();
		        	try
		        	{
			        	finder.setMatchKey(key[j]);
		        		while(!finder.isEnd())
		        		{
		        			parser.parse(sb.toString(), finder, true);
		        			if(finder.isFound())
		        			{
		        				finder.setFound(false);
		        				camattr[i][j]=finder.getValue().toString();
		        			}
		        		}   
		        		
		        	}
		        	catch(ParseException pe)
		        	{
		        		pe.printStackTrace();
		        	}
	        	}
	        	
	        }
	        
	        return camattr;
        }
		else if(re.equals("")) //�����S�s�u
		{
			return camattr;
		}
		else if(re.equals("DB_error"))
		{
			return camattr;
		}
		else
		{
			return camattr;
		}
		
		
	}
}
