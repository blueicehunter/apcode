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
	
    
    
    private UsbManager manager;       //USB�������.
    private UsbDevice mUsbDevice;     //USB�豸����.
    private UsbInterface mInterface;  //USB�ӿڶ���. 
    private UsbDeviceConnection mDeviceConnection; //USB�豸���Ӷ���
    
    private UsbEndpoint epOut; //USB�豸�˵�(OUT)
    private UsbEndpoint epIn;  //USB�豸�˵�(IN)
    
 	
    private HashMap<String, UsbDevice> deviceList;
    private Iterator<UsbDevice> deviceIterator;     //USB�豸���͵ı�������
    private ArrayList<String> USBDeviceList;        //���USB�豸��Ϣ���б�
    
    
    private byte[] SendBuff;    //����buffer
    private byte[] RecvBuff = new byte[64];    //����buffer
    
    
    MyHandler myHandler = new MyHandler();
    

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE); //����ʹ���Զ������ 
		setContentView(R.layout.activity_recv);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title);//�Զ��岼�ָ�ֵ 
		
		tvRecv1 = (TextView)findViewById(R.id.tvreceive1);
		
		//���ذ�ť�¼�����.
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
	 * ���ݽ����߳�
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
					
					//����ɹ��򷵻���������ֽ�����ĳ��ȣ�ʧ���򷵻ظ�����
					int ret = mDeviceConnection.bulkTransfer(epOut, SendBuff, SendBuff.length, 100);
					
					//�������ʧ��,�����´�ѭ��
					if(ret<0){
						closeConnection();
						continue;
					}
					else{
						
						int recvInt = 0;
						//����ɹ��򷵻���������ֽ�����ĳ��ȣ�ʧ���򷵻ظ�����
						int ret2 = mDeviceConnection.bulkTransfer(epIn, RecvBuff, RecvBuff.length, 200);
						
						if(ret2>0){
							for(int i=0;i<RecvBuff.length;i++){
		
								if((int)RecvBuff[i]!=0){
									/*
									 * ����java�е�byte���з��ŵ�,�������ֻ����128,����128�ͻ��ɸ���,���Դ˴�Ҫ�����ݽ��м����������
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
	 * ������
	 */
	public boolean openConnection(){

		manager = null;
		mUsbDevice = null;
		
		// ��ȡUSB�豸
        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        if (manager == null) {
            return false;
        } 

        //���ص�ǰ���ӵ�USB�豸��key��value(�豸����)�����û�������κ��豸����USB hostģʽû�п���������ֵ��Ϊ�ա�
        deviceList = manager.getDeviceList(); 
        
        deviceIterator = deviceList.values().iterator();
        
        USBDeviceList = new ArrayList<String>(); //���USB�豸��Ϣ���б�
        
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            mUsbDevice = device;
        }

		if(mUsbDevice != null){
			mInterface = mUsbDevice.getInterface(0);

			if(mInterface != null){
				epIn = mInterface.getEndpoint(0);
				epOut = mInterface.getEndpoint(1);
				
				// �ж��Ƿ���Ȩ��
				if (manager.hasPermission(mUsbDevice)){
					//��USB�豸������������
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
	 * �ر�����
	 */
	public void closeConnection(){
		mDeviceConnection.releaseInterface(mInterface);
		mDeviceConnection.close();
	}

	
	/*
	 * ���·��ؼ��ȹر��߳�.
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode==KeyEvent.KEYCODE_BACK && event.getRepeatCount()==0){
			flag = false;
			return super.onKeyDown(keyCode, event);
		}
		
		return false;
		
	}

}
