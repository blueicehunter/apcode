package com.example.usb_hid;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class RecvActivity extends Activity {

	private ToggleButton tbtn01;
    private TextView tvRecv1;
    
    private Thread mythread;
    private boolean flag = true;
	
    
    
    private UsbManager manager;       //USB管理对象.
    private UsbDevice mUsbDevice;     //USB设备对象.
    private UsbInterface mInterface;  //USB接口对象. 
    private UsbDeviceConnection mDeviceConnection; //USB设备连接对象。
    
    private UsbEndpoint epOut; //USB设备端点(OUT)
    private UsbEndpoint epIn;  //USB设备端点(IN)
    
 	
    private HashMap<String, UsbDevice> deviceList;
    private Iterator<UsbDevice> deviceIterator;     //USB设备类型的遍历器。
    private ArrayList<String> USBDeviceList;        //存放USB设备信息的列表。
    
    
    private byte[] SendBuff;    //发送buffer
    private byte[] RecvBuff = new byte[64];    //接收buffer
    
    
    MyHandler myHandler = new MyHandler();
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); //声明使用自定义标题 
		setContentView(R.layout.activity_recv);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);//自定义布局赋值 
		
		tvRecv1 = (TextView)findViewById(R.id.tvreceive1);
		
		//开关按钮事件监听.
		tbtn01 = (ToggleButton)findViewById(R.id.tbtn01);
		tbtn01.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if(isChecked){
					flag = true;
					mythread = new Thread(recvRunnable);
					mythread.start();
				}
				else{
					flag = false;
				}
			}
		});
	}
	
	
	class MyHandler extends Handler{
		public void handleMessage(Message msg) {
			tvRecv1.setText(String.valueOf(msg.arg1));
		}
		
	}
	

	/*
	 * 数据接收线程
	 */
	Runnable recvRunnable = new Runnable() {
		@Override
		public void run(){
			while(flag){
				if(openConnection()){
					String isoString = "a";
		
					try {
						SendBuff = isoString.getBytes("UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						return;
					}
					
					//传输成功则返回所传输的字节数组的长度，失败则返回负数。
					int ret = mDeviceConnection.bulkTransfer(epOut, SendBuff, SendBuff.length, 100);
					
					//如果发送失败,进入下次循环
					if(ret<0){
						closeConnection();
						continue;
					}
					else{
						
						int recvInt = 0;
						//传输成功则返回所传输的字节数组的长度，失败则返回负数。
						int ret2 = mDeviceConnection.bulkTransfer(epIn, RecvBuff, RecvBuff.length, 200);
						
						if(ret2>0){
							for(int i=0;i<RecvBuff.length;i++){
		
								if((int)RecvBuff[i]!=0){
									/*
									 * 由于java中的byte是有符号的,所以最高只能是128,大于128就会变成负数,所以此处要对数据进行计算后才输出。
									 */
									if((int)RecvBuff[i]<0){
										recvInt = (int)RecvBuff[i]&0x7F + 128;
									}
									else{
										recvInt = (int)RecvBuff[i];
									}
									break;
								}
							}
							Message msg = new Message();
							msg.arg1 = recvInt;
							myHandler.sendMessage(msg);
						}
					}
		
					closeConnection();
		
					try {
				        Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					
				}
				else{
					return;
				}
			}

		}
	};

	
	/*
	 * 打开连接
	 */
	public boolean openConnection(){

		manager = null;
		mUsbDevice = null;
		
		// 获取USB设备
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            return false;
        } 

        //返回当前连接的USB设备的key和value(设备对象)，如果没有连接任何设备或者USB host模式没有开启，返回值就为空。
        deviceList = manager.getDeviceList(); 
        
        deviceIterator = deviceList.values().iterator();
        
        USBDeviceList = new ArrayList<String>(); //存放USB设备信息的列表。
        
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            mUsbDevice = device;
        }

		if(mUsbDevice != null){
			mInterface = mUsbDevice.getInterface(0);

			if(mInterface != null){
				epIn = mInterface.getEndpoint(0);
				epOut = mInterface.getEndpoint(1);
				
				// 判断是否有权限
				if (manager.hasPermission(mUsbDevice)){
					//打开USB设备，并建立连接
					mDeviceConnection = manager.openDevice(mUsbDevice);
					
					if(mDeviceConnection == null){
						
						return false;
					}
					if(mDeviceConnection.claimInterface(mInterface, true)){
						
						return true;
					
					}else{
						return false;
					}
				}
				else{
					return false;
				}
				
			}else{
				return false;
			}
			
		}else{
			return false;
		}
	}
	
	/*
	 * 关闭连接
	 */
	public void closeConnection(){
		mDeviceConnection.releaseInterface(mInterface);
		mDeviceConnection.close();
	}

	
	/*
	 * 按下返回键先关闭线程.
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){
			flag = false;
			return super.onKeyDown(keyCode, event);
		}
		
		return false;
		
	}

}
