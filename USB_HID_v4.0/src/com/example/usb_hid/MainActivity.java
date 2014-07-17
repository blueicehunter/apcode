/*
 * 在3.2版本的基础上，让接收数据在新一Activity中运行。
 */
package com.example.usb_hid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.*;

public class MainActivity extends Activity {
	
	private static final String TAG = "MainActivity"; //记录标识
	private Button btnGetInfo;     //获取信息按钮

    private UsbManager manager;       //USB管理对象.
    private UsbDevice mUsbDevice;     //USB设备对象.
 	
    private HashMap<String, UsbDevice> deviceList;
    private Iterator<UsbDevice> deviceIterator;     //USB设备类型的遍历器。
    private ArrayList<String> USBDeviceList;        //存放USB设备信息的列表。
    
    
  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); //声明使用自定义标题 
		setContentView(R.layout.activity_main);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);//自定义布局赋值 
		
		
		
		btnGetInfo = (Button)findViewById(R.id.btnGetInfo);
		btnGetInfo.setOnClickListener(new GetInfoListener());


	}
	

	/*
	 * 获取设备信息按钮事件监听器。
	 */
	class GetInfoListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// 获取USB设备
	        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
	        if (manager == null) {
	        	Toast.makeText(MainActivity.this, "没有连接有效的设备", Toast.LENGTH_SHORT).show();
	            return;
	        } 

	        //返回当前连接的USB设备的key和value(设备对象)，如果没有连接任何设备或者USB host模式没有开启，返回值就为空。
	        deviceList = manager.getDeviceList(); 
	        
	        deviceIterator = deviceList.values().iterator();
	        
	        USBDeviceList = new ArrayList<String>(); //存放USB设备信息的列表。
	        
	        while (deviceIterator.hasNext()) {
	            UsbDevice device = deviceIterator.next();
	            mUsbDevice = device;
	            
	            Intent intent = new Intent(MainActivity.this,RecvActivity.class);
	            startActivity(intent);
	        }

		}
		
	}
	
}































