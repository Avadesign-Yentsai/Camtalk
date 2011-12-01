package com.avadesign.camvideo;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Log;

public class ReadXML 
{
	  public static String[][] readCamInfoXML(InputStream XMLinputStream,String TagName,String[] attr) 
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
	    	  int camAttribNum=doc.getDocumentElement().getFirstChild().getChildNodes().getLength();//IPCAM的資訊名目數量
	    	  
	    	  /*
	    	   * camAttribNum 必須等於 attr.length 
	    	   */
	    	 
	    	  re=new String[camNum][camAttribNum];
	    	  
	    	  
	    	  for (int i = 0; i <camNum; i++) 
	    	  {
	    		  Node nd = nodeList.item(i);
	    		  if (nd.getNodeType() == Node.ELEMENT_NODE) 
	    		  {
	    			  Element eElement = (Element) nd;
	    			  for (int j = 0; j <attr.length; j++) 
	    			  {
		    			  re[i][j]=getTagValue(attr[j], eElement);
	    			  }
	    		  }
	    	  }
	    	  
    	  } 
    	  catch (Exception e)
    	  {
    		  e.printStackTrace();
    		  Log.v("ReadXML",e.toString());
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
