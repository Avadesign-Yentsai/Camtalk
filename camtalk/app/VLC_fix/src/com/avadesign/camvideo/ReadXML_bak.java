package com.avadesign.camvideo;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ReadXML_bak 
{
	  public static String[][] readXML(InputStream XMLinputStream,String TagName) 
	    {
	    	  String[][] re = null;
	    	  DocumentBuilderFactory docBuilderFactory = null;
	    	  DocumentBuilder docBuilder = null;
	    	  Document doc = null;
	    	 
	    	  try 
	    	  {
	    		  docBuilderFactory = DocumentBuilderFactory.newInstance();
		    	  docBuilder = docBuilderFactory.newDocumentBuilder();
		    	  doc = docBuilder.parse(XMLinputStream);
		    	  doc.getDocumentElement().normalize();
		    	  
		    	  NodeList nodeList = doc.getElementsByTagName(TagName);
		    	  
		    	  int camNum =nodeList.getLength();//XML內的IPCAM數量
		    	  int camAttribNum=doc.getDocumentElement().getFirstChild().getChildNodes().getLength();//每個IPCAM的資訊名目數量
		    	 
		    	  re=new String[camNum][camAttribNum];
		    	  
		    	  
		    	  for (int temp = 0; temp <camNum; temp++) 
		    	  {
		    		  Node nd = nodeList.item(temp);
		    		  if (nd.getNodeType() == Node.ELEMENT_NODE) 
		    		  {
		    			  Element eElement = (Element) nd;
		    			  re[temp][0]=getTagValue("CamName", eElement);
		    			  re[temp][1]=getTagValue("CamIP", eElement);
		    		  }
		    	  }
		    	  
	    	  } 
	    	  catch (Exception e)
	    	  {
	    		  e.printStackTrace();
	    		  Log.v("testXML",e.toString());
	    	  } 
	    	  finally 
	    	  {
	    		  doc = null;
	    		  docBuilder = null;
	    		  docBuilderFactory = null;
	    	  }
	    	  return re;
	    }

	private static String getTagValue(String  sTag, Element eElement)
	{
		NodeList nlList = eElement.getElementsByTagName(sTag).item(0).getChildNodes();
		 
        Node nValue = (Node) nlList.item(0);
 
        return nValue.getNodeValue();
	}
}
