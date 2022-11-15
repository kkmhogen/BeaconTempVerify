package beaconMqttDemo;


import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;


import beaconMqttDemo.MqttConnNotify.ActionNotify;
import beaconMqttDemo.MqttConnNotify.ConnectionNotify;

public class BeaconMqttPushCallback implements MqttCallback {  
    BeaconMqttClient mClient;
    MqttConnNotify mMqttNotify;

    public static final int ERR_INVALID_INPUT = 1;
   	public static final int ERR_PARSE_SUCCESS = 0;
   	
   	public static final int EDDY_TLM_EXTEND = 0x21;
   	
   	private String mGatewaySubaction;    //gateway using this topic to receive command
   	private FileUtils mFileUtils;
   	
   	public class BeaconObject
   	{
   		String mMacAddress;    //device id
   		String mAdvData;       //adv data
   		int mRssi;
   		String uuid;
   		int mReferancePower;
   		long mLastUpdateMsec;  //report time
   		long mCommandCause;
   	};
   	private HashMap<String, BeaconObject> mDeviceMap = new HashMap<>();
    
    BeaconMqttPushCallback(BeaconMqttClient conn, MqttConnNotify mqttNotify){
    	mClient = conn;
    	mMqttNotify = mqttNotify;
    	mFileUtils = new FileUtils();
    	
    }
    
    public String getGatewaySubAction()
    {
    	return mGatewaySubaction;
    }

    public void connectionLost(Throwable cause) {  
        //connection lost, now reconnect
        System.err.println("MQTT client connection disconnected");
        mClient.setConnected(false);
        
        mGatewaySubaction = null;
        mDeviceMap.clear();
        
        mMqttNotify.connectionNotify(ConnectionNotify.CONN_NTF_DISCONNECTED);
    }  
    
    public void deliveryComplete(IMqttDeliveryToken token) {
        
    }
    
    public void clearAllDevice()
    {
    	mDeviceMap.clear();
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        // subscribe后得到的消息会执行到这里面  
        handleMqttMsg(topic, new String(message.getPayload()));
    }  
    
    
    protected void handleMqttMsg(String topic, String strMqttInfo)  {
		// TODO Auto-generated method stub	
		//parse jason object
		if (strMqttInfo == null){
			System.out.println("Receive invalid null data");
			return;
		}

		parseJsonReq(topic, strMqttInfo);
	}
			
	public int parseJsonReq(String topic, String strMqttInfo)
	{
		
		try 
		{
			JSONObject cmdReq = JSONObject.fromObject(strMqttInfo);
			if (cmdReq == null)
			{
				System.out.println("Connection to Mqtt server failed");
				return 0;
			}
			
			//message type
			String strDataType = cmdReq.getString("msg");
			if (strDataType.equalsIgnoreCase("advdata"))
			{
				return handleBeaconRpt(cmdReq);
			}
			else if (strDataType.equalsIgnoreCase("alive"))
			{
				return handleShakeReq(cmdReq);
			}
			else if (strDataType.equalsIgnoreCase("adminrsp"))
			{
				return mMqttNotify.handleAdminRsp(topic, cmdReq);
			}
			
			return ERR_PARSE_SUCCESS;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return ERR_INVALID_INPUT;
		}
	}
	
	public int handleShakeReq(JSONObject cmdReqAgent)
	{
		return ERR_PARSE_SUCCESS;
	}
	
		 
	 public int handleBeaconRpt(JSONObject cmdReqAgent)
		{		
			try 
			{		
				//mac address
				String strGwAddress = cmdReqAgent.getString("gmac");
				strGwAddress = strGwAddress.toUpperCase();
				if (!Utils.isMacAddressValid(strGwAddress)){
					System.out.println("beacon mqtt input invalid error");
					return ERR_INVALID_INPUT;
				}
						
				//obj list
				JSONArray objArray = cmdReqAgent.getJSONArray("obj");
				if (objArray == null)
				{
					System.out.println("unknown obj data");
					return ERR_INVALID_INPUT;
				}
				
				//update mac
				java.text.DateFormat timeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				String currTick = timeFormat.format(new Date());
				for (int i = 0; i < objArray.size(); i++)
				{
					JSONObject obj = objArray.getJSONObject(i);
					String strDevMac = obj.getString("dmac");

					BeaconMqttClient.DevAdvInfo advInfo = mClient.isDevSubscribe(strDevMac);
					if (advInfo != null)
					{
						Double temp = null;
						Double humidity = null;
						
						int nRssi = obj.getInt("rssi");
						String strTime = obj.getString("time");
						int advCnt = 0 ;

						//get temperature
						String data = obj.getString("data1");
						if (data.length() > 30)
						{
							String data_Temp = data.substring(26,30);
							byte[] data_temp_byte = Utils.hexStringToBytes(data_Temp);
							float measure_Temp = Utils.signedBytes2Float(data_temp_byte[0],data_temp_byte[1]);
							BigDecimal b = new BigDecimal(measure_Temp);
							temp = b.setScale(2,BigDecimal.ROUND_HALF_UP).doubleValue();
						}

						//other for Eddystone TLM
						if (obj.has("temp"))
						{
							temp = obj.getDouble("temp");
						}
						if (obj.has("hum"))
						{
							humidity = obj.getDouble("hum");
						}
						if(obj.has("advCnt"))
						{
							advCnt = obj.getInt("advCnt");
						}
						
						advInfo.advNum++;
						if (advInfo.advNum % 20 == 1)
						{
							this.mMqttNotify.appendLog("GW:"+ strGwAddress + ",Dev:" + strDevMac + ",num:" + advInfo.advNum + ",rssi:" + nRssi);
						}
//						mFileUtils.addMacAddressToFile(strTime, strDevMac, strGwAddress, nRssi, temp, humidity,advCnt);
						
						mFileUtils.addMacAddressToFile(strTime, strDevMac, strGwAddress, nRssi, temp, humidity, null);
					}
				}
			} 
			catch (Exception e) 
			{
				return ERR_INVALID_INPUT;
			}

			return ERR_PARSE_SUCCESS;
		}
	
	public int handleDownloadAck(JSONObject cmdReqAgent)
	{
		try 
		{
			//mac address
			String strDevMac = cmdReqAgent.getString("mac");
			if (strDevMac == null)
			{
				return ERR_INVALID_INPUT;
			}
			strDevMac = strDevMac.toUpperCase();
			if (!Utils.isMacAddressValid(strDevMac)){
				System.out.println("beacon mqtt input invalid error");
				return ERR_INVALID_INPUT;
			}
			
			//found device
			BeaconObject eslObj = this.mDeviceMap.get(strDevMac);
			if (eslObj == null)
			{
				this.mMqttNotify.actionNotify(ActionNotify.MSG_DEVICE_NOT_FOUND, null);
				return ERR_INVALID_INPUT;
			}
			
			String strResult = cmdReqAgent.getString("rslt");
			if (strResult == null)
			{
				return ERR_INVALID_INPUT;
			}
			
			String strCause = cmdReqAgent.getString("cause");
			if (strCause == null)
			{
				return ERR_INVALID_INPUT;
			}
			int nCause = Integer.valueOf(strCause);
			eslObj.mCommandCause = nCause;
			
			if (strResult.equals("succ"))
			{
				if (nCause == 1)
				{
					this.mMqttNotify.actionNotify(ActionNotify.MSG_DOWNLOAD_SUCCESS, eslObj);
				}
				else
				{
					this.mMqttNotify.actionNotify(ActionNotify.MSG_EXECUTE_SUCCESS, eslObj);
				}
			}
			else
			{
				this.mMqttNotify.actionNotify(ActionNotify.MSG_EXECUTE_FAIL, eslObj);
			}
		}
		catch (Exception e) 
		{
			return ERR_INVALID_INPUT;
		}
		
		return ERR_PARSE_SUCCESS;
	}
	
}