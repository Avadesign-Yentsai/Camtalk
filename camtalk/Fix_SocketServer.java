import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
 
public class Fix_SocketServer
{
 
	private static int serverport = 9000;
	private static ServerSocket serverSocket;
	private static String DB_path = "localhost:3306/yen_camtalk";
	private static String DB_user = "admin";
	private static String DB_pwd = "avadesign";
	private static StringBuffer sb ;
	private static SimpleDateFormat sdDate = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat sdTime = new SimpleDateFormat("HH:mm:ss");

	private static String CamURL;
	private static int timeout = 5000;
	
	// 用串列來儲存每個client
	//private static ArrayList<Socket> players=new ArrayList<Socket>();

	// 用map來儲存每個client
	private static Map<String, Socket> users = new HashMap<String, Socket>();

	// 程式進入點
	public static void main(String[] args) 
	{
		try
		{
			serverSocket = new ServerSocket(serverport);
			System.out.println("Server is start.");

			// 當Server運作中時
			while (!serverSocket.isClosed()) 
			{
				// 顯示等待客戶端連接
				//System.out.println("Wait new clinet connect");

				// 呼叫等待接受客戶端連接
				waitNewPlayer();
			}
	 
		} 
		catch (IOException e) 
		{
			System.out.println("Server Socket ERROR");
		}
	}
 
	// 等待接受客戶端連接
	private static void waitNewPlayer() 
	{
		try 
		{
			Socket socket = serverSocket.accept();
			System.out.println(socket+" connect");
			// 呼叫創造新的使用者
			createNewPlayer(socket);
		} 
		catch (IOException e) 
		{

		}
	}
 
	// 創造新的使用者
	private static void createNewPlayer(final Socket socket) 
	{
		// 以新的執行緒來執行
		Thread t = new Thread(new Runnable() 
		{
			@Override
			public void run() 
			{
				String user=null;
				try 
				{
					socket.setSoTimeout(timeout);
					// 取得網路串流 
					BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					
					// 當Socket已連接時連續執行
					while (socket.isConnected()) 
					{
						String l = null;
						l = br.readLine();
						
						if(l!=(null))
						{
							String[] msg= l.split(":SPLIT:");
							if (msg[0].equals("phone")) //連接來源是手機
							{
								user = msg[1];
								System.out.println("usermail:"+user+","+sdTime.format(Calendar.getInstance().getTime()));
								
								/*
								// 檢查帳號是否已在線
								if(users.containsKey(user))
								{
									System.out.println(user+" has login");	
									kickuser(users.get(user));
								}
								*/
								
								// 增加新的使用者
								//players.add(socket);
								users.put(user, socket);
								System.out.println(users);
							}
							
							else if(msg[0].equals("test"))//手機傳送的測試連線訊息
							{
								if(users.containsValue(socket))
								{
									System.out.println(user+","+socket.getPort()+" send test"+","+sdTime.format(Calendar.getInstance().getTime()));
									sendRe(users.get(user));
								}
								else
								{
									//break;
								}
							}
							
							else // 連接來源不是手機(可能是IPCAM)
							{
								String fromIP = (socket.getInetAddress().toString().replace("/",""));
								
								// 藉由來源IP檢查IPCAM的擁有人帳號
								getUserMail(fromIP);
								
								// 輸出訊息 
								System.out.println(msg[0]+","+sdTime.format(Calendar.getInstance().getTime()));
								
								// 廣播訊息給其它的客戶端
								//castMsg(msg[0]);
								
								response(socket);
								break;
								
							}
							
							
							
						}
						else // user 中斷連線
						{
							break;
						}
					}
					socket.close();
					//players.remove(socket);   
					
					if(users.containsValue(socket))
					{
						users.remove(user);	
						System.out.println(user+" disconnect "+socket.getPort()+","+sdTime.format(Calendar.getInstance().getTime()));
					}
					
					
				} 
				
				catch (SocketException e) 
				{
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.out.println("line189"+e+","+user);
					// 移除客戶端
					//players.remove(socket);   
					if(users.containsValue(socket))
					{
						System.out.println(user+" disconnect"+socket.getPort()+","+sdTime.format(Calendar.getInstance().getTime()));
						users.remove(user);	
					}
					
				}
				catch (IOException e) 
				{
					try {
						socket.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.out.println("line204"+e+","+user);
					// 移除客戶端
					//players.remove(socket);   
					if(users.containsValue(socket))
					{
						System.out.println(user+" disconnect"+socket.getPort()+","+sdTime.format(Calendar.getInstance().getTime()));
						users.remove(user);	
					}
					
				}
				
				
				
			}

			

		});
		// 啟動執行緒
		t.start();
	}
	
	private static void response(Socket socket) 
	{
		try
		{
			BufferedWriter bw;
			bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));
			
			String re ="HTTP/1.0 200 OK";
			
			bw.write(re+"\n");
	
			// 立即發送
			bw.flush();
		}
		catch (IOException e) 
		{
			System.out.println(e.toString());
		}
		
	}
	
	//send re
	private static void sendRe(Socket usersocket)
	{
		try
		{	
			BufferedWriter bw;
			bw = new BufferedWriter( new OutputStreamWriter(usersocket.getOutputStream()));
			
			String Msg = "test";

			// 寫入訊息到串流
			bw.write(Msg+"\n");

			// 立即發送
			bw.flush();
			System.out.println("reply "+usersocket.getPort()+" test, "+sdTime.format(Calendar.getInstance().getTime()));
		} 
		catch (IOException e) 
		{
			System.out.println(e.toString());
		}
		
	}
	
	
	
	/*
	// 廣播訊息給其它的客戶端
	public static void castMsg(String Msg)
	{
		// 創造socket陣列
		Socket[] ps=new Socket[players.size()]; 
	 
		// 將players轉換成陣列存入ps
		players.toArray(ps);
	 
		// 走訪ps中的每一個元素
		for (Socket socket :ps )
		{
			try 
			{
				System.out.println("CAST:"+socket);
				// 創造網路輸出串流
				BufferedWriter bw;
				bw = new BufferedWriter( new OutputStreamWriter(socket.getOutputStream()));

				// 寫入訊息到串流
				bw.write(Msg+"\n");

				// 立即發送
				bw.flush();
			} 
			catch (IOException e) 
			{
				System.out.println(e.toString());
			}
		}
	}
	*/
	// 傳送訊息給IPCAM的擁有人
	private static void sendMsg(Socket usersocket, String Msg)
	{
		try
		{
			if(usersocket.isConnected())
			{
				BufferedWriter bw;
				bw = new BufferedWriter( new OutputStreamWriter(usersocket.getOutputStream(),"UTF8"));

				// 寫入訊息到串流
				bw.write(Msg+"\n");

				// 立即發送
				bw.flush();
			}
        } 
		catch (IOException e) 
		{
			System.out.println("ERR sendMsg:"+e.toString());
		}
        
	}
	
	private static void getUserMail(String ip)
	{
		try
		{	
			String usermail="";
			String camID="";
			//sb = new StringBuffer("");
		
			Connection conn = DriverManager.getConnection("jdbc:mysql://"+DB_path+"?autoReconnect=true&useUnicode=true&characterEncoding=big5&user="+DB_user+"&password="+DB_pwd);
			Statement stmt=conn.createStatement();
			String sql="SELECT * "+
					"FROM `caminfo_tb` , `camlist_tb` , `user_tb` "+
					"WHERE `caminfo_tb`.`camID` = `camlist_tb`.`camID` "+
					"AND `camlist_tb`.`userMail` = `user_tb`.`userMail` "+
					"AND `user_tb`.`user_md_set`='on'"+
					"AND `caminfo_tb`.`camIP`='"+ip+"'"+
					"AND `camlist_tb`.`camAvailable` = 'true'";
			
			ResultSet rs=stmt.executeQuery(sql);
			
			while(rs.next())
			{
				
				JSONObject obj=new JSONObject();
				if((rs.getString("camVideoCode")).equals("h264"))
				{
					CamURL="rtsp://"+rs.getString("camIP")+":"+rs.getString("camVideoPort")+"/ipcam_h264.sdp";
				}
				else
				{
					CamURL="rtsp://"+rs.getString("camIP")+":"+rs.getString("camVideoPort")+"/ipcam.sdp";
				}
				obj.put("camURL",CamURL);
				obj.put("camIP",rs.getString("camIP"));
				obj.put("camTalkPort",rs.getString("camTalkPort"));
				obj.put("camTalkAc",rs.getString("camTalkAc"));
				obj.put("camTalkPw",rs.getString("camTalkPw"));
				obj.put("camName",rs.getString("camName"));
				
				camID = rs.getString("camID");
				usermail = rs.getString("userMail");
				
				if(!usermail.equals("") && users.containsKey(usermail)) //如果資料庫有IPCAM的資料且擁有者在線
				{
					System.out.println("SEND:"+usermail+","+users.get(usermail).getPort()+","+sdTime.format(Calendar.getInstance().getTime()));
					//傳送CAM的IP給IPCAM的擁有人
					//sendMsg(users.get(usermail),sb.toString());
					sendMsg(users.get(usermail),JSONValue.toJSONString(obj));
				}
				
				if(!usermail.equals("")) //如果資料庫有IPCAM的資料,紀錄事件
				{
					System.out.println("SAVE EVENT_LOG:"+usermail+","+sdTime.format(Calendar.getInstance().getTime()));
					saveEventLog(usermail,camID,sdDate.format(Calendar.getInstance().getTime()),sdTime.format(Calendar.getInstance().getTime()));
				}
				
				
				sb=null;
			}
			
			rs.close();
			stmt.close();
			conn.close();
			
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			
		}
	}

	private static void saveEventLog(String usermail, String camID, String date, String time) 
	{
		try
		{
			Connection conn = DriverManager.getConnection("jdbc:mysql://"+DB_path+"?autoReconnect=true&useUnicode=true&characterEncoding=big5&user="+DB_user+"&password="+DB_pwd);
			Statement stmt=conn.createStatement();
			String sql="INSERT INTO `event_log` "+
					"(`userMail` ,`camID` ,`eventDate` ,`eventTime` ,`eventType`)"+
					"VALUES "+
					"('"+usermail+"', '"+camID+"', '"+date+"', '"+time+"', 'md');";
			
			//System.out.println(sql);
			stmt.executeUpdate(sql);
			
			conn.close();
			stmt.close();
		}
		catch(Exception e)
		{
			System.out.println(e.toString());
			
		}
	}
}