package com.aykow.aube.ble;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

public class SensorExtractData {

	public static double extractTemperature(BluetoothGattCharacteristic c) {
		double rawT = shortUnsignedAtOffset(c, 0);
		//c.getValue();
		if(rawT==-1){
			return 21;//error;
		}
		else
        {
			rawT= ((double)rawT*2-500.)/19.5;
			Log.d("BLE", "value temperature with modification : "+ rawT);
			return rawT;//-3;
		}

		//return ((double)rawT*2*20.)/900.;//rawT;// -46.85 + 175.72/65536 *(double)rawT;
		//return (((double)rawT)-400.)/900;
		//Log.d("BLE", "data raw value: "+ c.getValue().toString());
		// return rawT;

	}

	private static Integer shortUnsignedAtOffset(BluetoothGattCharacteristic c, int offset) {
		int value=-1;
		byte[] val;
		val = c.getValue();
		try{
			value = ((val[0] << 8) & 0x0000ff00) | (val[1] & 0x000000ff);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		if(BuildConfig.DEBUG)
		{
			//Log.d("BLE", "value temperature without modification : "+ value);
		}
		// Integer upperByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset);
		// Integer lowerByte = c.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, offset + 1); // Note: interpret MSB as unsigned.

		return value;//lowerByte;//(upperByte << 8) + lowerByte;
	}

	public static double extractAir(BluetoothGattCharacteristic c) {
		byte[] val;
		int value=30;
		try{
			val = c.getValue();
			value = ((val[0] << 8) & 0x0000ff00) | (val[1] & 0x000000ff);
		}catch(Exception e)
		{
			e.printStackTrace();
		}

		if(BuildConfig.DEBUG)
		{
			//Log.d("BLE", "value air : "+ value);
		}
		return value;
	}
	public static int extractState(BluetoothGattCharacteristic c) {
		int value=1;
		byte[] val;
		try{
			val = c.getValue();
			value = val[0] & 0x000000ff;
		}catch(Exception e){
			e.printStackTrace();
		}
		return value;
	}

}
