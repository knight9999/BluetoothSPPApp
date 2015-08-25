package info.nfuture.bluetoothsppapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public abstract class ReceiverThread extends Thread {
	
	public interface MessageCallback {
		void receiveMessage(String message);
	}
	
	private final String TAG = getClass().getSimpleName();
	protected BluetoothSocket mSocket;
	protected MessageCallback mMessageCallback;
	
	public ReceiverThread(MessageCallback messageCallback) {
		mMessageCallback = messageCallback;
	}
	
	public void sendMessageRaw(String message) throws IOException {
		OutputStream os = mSocket.getOutputStream();
		os.write( message.getBytes() );
	}
	
	public void sendMessage(String message) throws IOException {
		sendMessageRaw( message + "\n" );
	}
	
	protected void loop() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			String message;
			while ((message = br.readLine()) != null) {
				Log.d(TAG,"message : " + message);
				mMessageCallback.receiveMessage(message);
			}
		} catch (IOException e) {
		
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				br = null;
			}
		}
	
	}
	
}
