package beaconMqttDemo;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import net.sf.json.JSONObject;

import beaconMqttDemo.BeaconMqttPushCallback.BeaconObject;

public class BeaconPannel extends JPanel implements MqttConnNotify {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static String CFG_MQTT_SRV_URL = "MqttSrvUrl";
	private final static String CFG_MQTT_USR_NAME = "MqttUserName";
	private final static String CFG_MQTT_USR_PASSWORD = "MqttPassword";

	BeaconMqttClient mMqttClient; // mqtt connection

	private JLabel labelMqttSrv, labelGwID, labelDevList, labelMqttUser, labelMqttPwd,
			labelFilterMac;

	private JButton buttonConn, buttonClear, buttonSetFilter, buttonGetFilter;
	private JTextField textMqttSrv, textGwID, textDevList, textMqttUser, textMqttPwd,
			textFilterMac;
	private JTextArea textLogInfo;
	private JPanel pannelMqttSrv, pannelGwID, pannelDevList, pannelUser, pannelPwd,
			pannelFilter, pannelLogin, pannelLogInfo;

	public BeaconPannel() {
		mMqttClient = new BeaconMqttClient(this);

		this.labelMqttSrv = new JLabel("Mqtt address");
		this.labelGwID = new JLabel("Gateway mac list");
		this.labelDevList = new JLabel("Beacon mac list:");
		this.labelMqttUser = new JLabel("Mqtt name");
		this.labelMqttPwd = new JLabel("Mqtt password");
		this.labelFilterMac = new JLabel("Filter MAC");

		this.textMqttSrv = new JTextField(30);
		this.textMqttUser = new JTextField(10);
		this.textMqttPwd = new JTextField(10);

		this.textGwID = new JTextField(40);
		this.textDevList = new JTextField(40);
		this.textFilterMac = new JTextField(40);

		this.textLogInfo = new JTextArea(14, 60);
		JScrollPane scroll = new JScrollPane(textLogInfo);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		this.buttonConn = new JButton("Connected");
		this.buttonClear = new JButton("Clear");

		this.setLayout(new GridLayout(2, 1)); // 网格式布局
		JPanel upper = new JPanel();
		upper.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.setLayout(new GridLayout(7, 1)); // 网格式布局

		this.pannelMqttSrv = new JPanel();
		this.pannelMqttSrv.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.add(pannelMqttSrv);
		
		this.pannelUser = new JPanel();
		this.pannelUser.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.add(pannelUser);
		
		this.pannelPwd = new JPanel();
		this.pannelPwd.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.add(pannelPwd);
		
		this.pannelGwID = new JPanel();
		this.pannelGwID.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.add(pannelGwID);
		
		this.pannelDevList = new JPanel();
		this.pannelDevList.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.add(pannelDevList);
		
		this.pannelFilter = new JPanel();
		this.pannelFilter.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.add(pannelFilter);
		
		this.pannelLogin = new JPanel();
		this.pannelLogin.setLayout(new FlowLayout(FlowLayout.LEFT));
		upper.add(pannelLogin);
		this.add(upper);

		this.pannelLogInfo = new JPanel();
		this.pannelLogInfo.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.add(pannelLogInfo);

		//mqtt server info
		this.pannelMqttSrv.add(this.labelMqttSrv);
		this.pannelMqttSrv.add(this.textMqttSrv);

		this.pannelUser.add(this.labelMqttUser);
		this.pannelUser.add(this.textMqttUser);

		this.pannelPwd.add(this.labelMqttPwd);
		this.pannelPwd.add(this.textMqttPwd);

		//monitor gw list
		this.pannelGwID.add(this.labelGwID);
		this.pannelGwID.add(this.textGwID);
		String strGwList = BeaconConfig.getPropertyValue("GWList", null);
		textGwID.setText(strGwList);
		
		//monitor beacon list
		this.pannelDevList.add(this.labelDevList);
		this.pannelDevList.add(this.textDevList);
		String strDevList = BeaconConfig.getPropertyValue("DevList", null);
		if (strDevList != null){
			textDevList.setText(strDevList);
		}

		//mac filter
		this.pannelFilter.add(this.labelFilterMac);
		this.pannelFilter.add(this.textFilterMac);
		this.buttonSetFilter = new JButton("Setting Mac filter");
		this.pannelFilter.add(this.buttonSetFilter);
		
		//get filter
		this.buttonGetFilter = new JButton("Get Gateway filter");
		this.pannelFilter.add(this.buttonGetFilter);

		this.pannelLogin.add(this.buttonConn);
		this.pannelLogin.add(this.buttonClear);
		this.pannelLogInfo.add(scroll);

		String strMqttSrv = BeaconConfig.getPropertyValue(CFG_MQTT_SRV_URL,
				mMqttClient.getHostAddr());
		this.textMqttSrv.setText(strMqttSrv);

		String strMqttUserName = BeaconConfig.getPropertyValue(
				CFG_MQTT_USR_NAME, mMqttClient.getUserName());
		this.textMqttUser.setText(strMqttUserName);

		String strMqttUserPassword = BeaconConfig.getPropertyValue(
				CFG_MQTT_USR_PASSWORD, mMqttClient.getPassword());
		this.textMqttPwd.setText(strMqttUserPassword);
		addClickListener();
	}

	private void addClickListener() 
	{
		buttonConn.addActionListener(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e) 
			{
				String strMqttSrvAddr = textMqttSrv.getText();
				String strMqttGwList = textGwID.getText();
				String strMqttUser = textMqttUser.getText();
				String strMqttPwd = textMqttPwd.getText();
				String strDevList = textDevList.getText();

				if (!mMqttClient.isConnected()) 
				{
					BeaconConfig.savePropertyValue(CFG_MQTT_USR_PASSWORD,
							strMqttPwd);
					BeaconConfig.savePropertyValue(CFG_MQTT_USR_NAME,
							strMqttUser);
					BeaconConfig.savePropertyValue(CFG_MQTT_SRV_URL,
							strMqttSrvAddr);
					BeaconConfig.savePropertyValue("GWList", strMqttGwList);
					BeaconConfig.savePropertyValue("DevList", strDevList);
					
					//beacon list
					ArrayList<String> beaconList = new ArrayList<>();
					String[] beaconArray = strDevList.split(",");
			        if (beaconArray != null)
			        {
				        for (int i = 0; i < beaconArray.length; i++)
				        {
				        	if (Utils.isMacAddressValid(beaconArray[i]))
				        	{
				        		beaconList.add(beaconArray[i]);
				        	}
				        	else
				        	{
				        		textLogInfo.append("beacon list invalid\r\n");
				        		return;
				        	}
				        }
			        }
					
					//gw list
		        	ArrayList<String> gwList = new ArrayList<>();
					String[] gwArray = strMqttGwList.split(",");
			        if (gwArray != null)
			        {
				        for (int i = 0; i < gwArray.length; i++)
				        {
				        	if (Utils.isMacAddressValid(gwArray[i]))
				        	{
				        		gwList.add(gwArray[i]);
				        	}
				        	else
				        	{
				        		textLogInfo.append("gateway list invalid\r\n");
				        		return;
				        	}
				        }
			        }
			        
					textLogInfo.append("Subscribe beacon:" + strDevList + "\r\n");
					mMqttClient.setConnectinInfo(strMqttSrvAddr, gwList, beaconList, strMqttUser, strMqttPwd);
					mMqttClient.connect();
				}
			}
		});

		buttonClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				mMqttClient.mBeaconCallback.clearAllDevice();
				textLogInfo.setText("");
			}
		});

		buttonSetFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setMacFilter();
			}
		});

		buttonGetFilter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				getMacFilter();
			}
		});
	}

	public HashMap<String, FilterSettingResult> mSetFilterMap = new HashMap<>();
	private Integer mSetFilterNum = 0;

	public void getMacFilter() {
		String strMqttGwList = textGwID.getText();
		String strGwArray[] = strMqttGwList.split(",");
		if (strGwArray != null) {
			for (int i = 0; i < strGwArray.length; i++) {
				JSONObject object = new JSONObject();
				object.put("msg", "admin");
				object.put("stype", "getCfg");
				object.put("seq", mSetFilterNum++);

				String strTopic = "kbeacon/subadmin/" + strGwArray[i];
				this.mMqttClient
						.pubCommand2Gateway(strTopic, object.toString());
			}

			buttonConn.setEnabled(false);
		}
	}

	public void setMacFilter() {
		mSetFilterMap.clear();
		String strMqttGwList = textGwID.getText();

		String strGwArray[] = strMqttGwList.split(",");
		if (strGwArray != null) {
			for (int i = 0; i < strGwArray.length; i++) {
				String strFilterMac = textFilterMac.getText();

				JSONObject object = new JSONObject();
				object.put("msg", "admin");
				object.put("stype", "cfg");
				object.put("filtermac", strFilterMac);
				object.put("seq", mSetFilterNum);

				FilterSettingResult filter = new FilterSettingResult();
				filter.mResult = false;
				filter.mSeq = mSetFilterNum;
				mSetFilterMap.put(strGwArray[i], filter);

				// publish message
				String strTopic = "kbeacon/subadmin/" + strGwArray[i];
				this.mMqttClient
						.pubCommand2Gateway(strTopic, object.toString());
				mSetFilterNum = mSetFilterNum + 1;
			}
		}
	}

	public int handleAdminRsp(String strTopic, JSONObject cmdReqAgent) {
		try {
			String strArray[] = strTopic.split("/");
			if (strArray == null || strArray.length == 0) {
				return -1;
			}

			String strMac = strArray[strArray.length - 1];

			if (cmdReqAgent.getString("stype").compareToIgnoreCase("getCfg") == 0) {
				handleGetCfgPara(strMac, cmdReqAgent);
			} else if (cmdReqAgent.getString("stype")
					.compareToIgnoreCase("cfg") == 0) {
				handleSetFilterRsp(strMac, cmdReqAgent);
			}

		} catch (Exception e) {
			return -1;
		}

		return 0;
	}

	public int handleGetCfgPara(String strMac, JSONObject cmdReqAgent) {
		if (cmdReqAgent.has("filtermac")) {
			String strFilter = cmdReqAgent.getString("filtermac");
			textFilterMac.setText(strFilter);

			textLogInfo.append("网关:" + strMac + "过滤:  " + strFilter + " \r\n");
		}

		return 0;
	}

	public int handleSetFilterRsp(String strMac, JSONObject cmdReqAgent) {
		Integer seq = (Integer) cmdReqAgent.get("seq");

		// mac address
		FilterSettingResult result = (FilterSettingResult) mSetFilterMap
				.get(strMac);
		if (seq != result.mSeq) {
			return -1;
		}
		result.mResult = true;
		textLogInfo.append("网关:" + strMac + "设置过滤成功 \r\n");
		return 0;
	}

	@Override
	public void connectionNotify(MqttConnNotify.ConnectionNotify connNtf) {
		// TODO Auto-generated method stub
		if (connNtf == ConnectionNotify.CONN_NTF_CONNECED) {
			buttonConn.setEnabled(false);
			textLogInfo.append("Mqtt Server connected\r\n");
		} else if (connNtf == ConnectionNotify.CONN_NTF_DISCONNECTED) {
			buttonConn.setEnabled(true);
			textLogInfo.append("Mqtt Server disconnected\r\n");
		} else if (connNtf == ConnectionNotify.CONN_SHAKE_SUCCESS) {
			textLogInfo.append("Gateway shake success\r\n");
		}
	}
	
	public void appendLog(String strLog)
	{
		textLogInfo.append(strLog
				+ "\r\n");
	}

	@Override
	public void actionNotify(MqttConnNotify.ActionNotify downNtf, Object obj) {
		// TODO Auto-generated method stub
		if (downNtf == ActionNotify.MSG_DOWNLOAD_SUCCESS) {
			if (obj != null) {
				BeaconObject eslObj = (BeaconObject) obj;
				textLogInfo.append(eslObj.mMacAddress
						+ ", download msg succ\r\n");
			}
		} else if (downNtf == ActionNotify.FOUND_DEVICE) {
			if (obj != null) {
				BeaconObject eslObj = (BeaconObject) obj;
				textLogInfo.append(eslObj.mMacAddress
						+ ", found new device succ\r\n");
			}
		} else if (downNtf == ActionNotify.MSG_EXECUTE_SUCCESS) {
			if (obj != null) {
				BeaconObject eslObj = (BeaconObject) obj;
				textLogInfo.append(eslObj.mMacAddress
						+ ", execute msg succ\r\n");
			}
		} else if (downNtf == ActionNotify.MSG_EXECUTE_FAIL) {
			if (obj != null) {
				BeaconObject eslObj = (BeaconObject) obj;
				textLogInfo.append(eslObj.mMacAddress
						+ ", execute msg failed, err:" + eslObj.mCommandCause
						+ "\r\n");
			}
		}
	}
}