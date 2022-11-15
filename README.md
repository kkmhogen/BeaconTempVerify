# BeaconTempVerify
This tool is based on Java, and tested on JDK 1.8.  

You can running \bin\BeaconTest.bat for test. When the tool is run, it receives data from MQTT Broker and save it to a CSV file.

You can add your own parsing code in handleBeaconRpt function of BeaconMqttPushCallback.java in the src/beaconMqttDemo file.
