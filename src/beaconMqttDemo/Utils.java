package beaconMqttDemo;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Utils {

    public static final int ERR_INVALID_INPUT = 1;
   	public static final int ERR_PARSE_SUCCESS = 0;

	private static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	public final static int LOG_LEVEL_DEBUG = 1;
	public final static int LOG_LEVEL_INFO = 2;
	public final static int LOG_LEVEL_ERR = 3;

	public static String getCurrentTime() {
		long nCurrentTime = System.currentTimeMillis();

		Date date = new Date(nCurrentTime);
		return DATE_FORMAT.format(date) + " ";
	}

	public static byte[] stringToBytes(String strString){
	       if (strString == null || strString.equals("")) {
	           return null;
	       }
	       char []chars = strString.toCharArray();
	       Charset cs = Charset.forName("UTF-8");
	       CharBuffer cb = CharBuffer.allocate(chars.length);
	       cb.put(chars);
	       cb.flip();

	       ByteBuffer bb = cs.encode(cb);
	       return bb.array();
	   }

	   public static String bytesToHexString(byte[] src){
	       StringBuilder stringBuilder = new StringBuilder("");
	       if (src == null || src.length <= 0) {
	           return null;
	       }
	       for (int i = 0; i < src.length; i++) {
	           int v = src[i] & 0xFF;
	           String hv = Integer.toHexString(v);
	           if (hv.length() < 2) {
	               stringBuilder.append(0);
	           }
	           stringBuilder.append(hv);
	       }
	       return stringBuilder.toString();
	   }

	public static String GetWorkDir()
	{
		return System.getProperty("user.dir");
	}

	public static boolean isMacAddressValid(String strMacAddr)
	{
		if (strMacAddr == null || strMacAddr.length() != 12)
		{
			return false;
		}
		
		for (int j = 0; j < strMacAddr.length(); j++)
	    {
	    	char cMac = strMacAddr.charAt(j);
	    	if ((cMac >= '0' && cMac <= '9')
	    			|| (cMac >= 'A' && cMac <= 'F')
	    			|| (cMac >= 'a' && cMac <= 'f'))
	    	{
	    		continue;
	    	}
	    	else
	    	{
	    		return false;
	    	}
	    }
		
		return true;
	}

	public static boolean isNumber(String string) {
        if (string == null)
            return false;
        Pattern pattern = Pattern.compile("^-?\\d+(\\.\\d+)?$");
        return pattern.matcher(string).matches();
    }

   public static boolean isUUIDString(String hexString)
    {
        String pattern = "^[0-9A-Fa-f]{8}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{4}-[0-9A-Fa-f]{12}";
        return Pattern.matches(pattern, hexString);
    }

	public static boolean isHexString(String hexString)
    {
        String pattern = "([0-9A-Fa-f]{2})+";
        String pattern2 = "^0X|^0x([0-9A-Fa-f]{2})+";
        if (!Pattern.matches(pattern, hexString))
        {
            if (!Pattern.matches(pattern2, hexString))
            {
                return false;
            }
        }

        return true;
    }


	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		char[] hexCharacter = hexString.toCharArray();
		for (int i = 0; i < hexCharacter.length; i++) {
			if (-1 == charToByte(hexCharacter[i])) {
				return null;
			}
		}

		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));

		}
		return d;
	}

	public static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}

	public static String FormatHexUUID2User(String strUUID)
    {
        strUUID = strUUID.toUpperCase().replace("0X", "");
        if (strUUID.length() != 32)
        {
            return "";
        }

        String strUserUUID;
        strUserUUID = strUUID.substring(0, 8);
        strUserUUID += "-";

        strUserUUID += strUUID.substring(8, 12);
        strUserUUID += "-";

        strUserUUID += strUUID.substring(12, 16);
        strUserUUID += "-";

        strUserUUID += strUUID.substring(16, 20);
        strUserUUID += "-";

        strUserUUID += strUUID.substring(20);

        return strUserUUID;
    }



//	public static AdvData ble_advdata_search(byte[] p_encoded_data,
//            int      	p_offset,
//            int         ad_type)
//	{
//		if (p_encoded_data.length == 0)
//		{
//			return null;
//		}
//
//		int i = 0;
//
//		while (((i+1) < p_encoded_data.length) &&
//				((i < p_offset) || ((p_encoded_data[i + 1] & 0xFF) != ad_type)))
//		{
//			// Jump to next data.
//			i += (p_encoded_data[i] + 1);
//		}
//
//
//		if (i >= p_encoded_data.length)
//		{
//			return null;
//		}
//		else
//		{
//			AdvData advData = new AdvData();
//			advData.nNextDataPos = i + 2;
//
//			int nLen = p_encoded_data[i] - 1;
//			advData.data = new byte[nLen];
//
//			for (int j = 0; j < nLen; j++)
//			{
//				advData.data[j] = p_encoded_data[advData.nNextDataPos + j];
//			}
//
//			return advData;
//		}
//	}

	public static float signedBytes2Float(byte byHeight, byte byLow)
    {
        float nTempPointLeft = byHeight;
        int nTempPointRight = (byLow & 0xFF);
        float fTempPointRight = ((float)nTempPointRight) / 256;
        float result;
        if (nTempPointLeft < 0)
        {
            result = nTempPointLeft - fTempPointRight;
        }
        else
        {
            result = nTempPointLeft + fTempPointRight;
        }

        BigDecimal bigTemp = new BigDecimal(result);
        return bigTemp.setScale(2,   BigDecimal.ROUND_HALF_UP).floatValue();
    }

	public static JSONObject parseDeviceOutput(String strFamJsonMsg)
    {
    	//parse device id
		try
		{
			JSONObject device = JSONObject.fromObject(strFamJsonMsg);
			return device;
		}
		catch(Exception excpt)
		{

		}

		return null;
    }


}
