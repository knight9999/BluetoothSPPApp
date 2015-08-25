package info.nfuture.bluetoothsppapp;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.util.Log;

public class ServerThread extends ReceiverThread {
	private final String TAG = getClass().getSimpleName();
	private BluetoothServerSocket mServerSocket;
	
	public interface ServerMessageCallback extends MessageCallback {
		public void accepting();
		public void accepted();
		public void cancel();
	}
	
	public ServerThread(BluetoothAdapter bluetoothAdapter,String name,UUID uuid,ServerMessageCallback messageCallback) {
		super(messageCallback);
		Log.d(TAG,"name = " + name );
		try {
			mServerSocket = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(name, uuid);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void run() {
		try {
			Log.d(TAG, "accepting...");
			((ServerMessageCallback) mMessageCallback).accepting();
			mSocket = mServerSocket.accept();
			Log.d(TAG,"accepted");
			((ServerMessageCallback) mMessageCallback).accepted();
			loop();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cancel();
		}
	}

	public void cancel() {
		try {
			if (mServerSocket != null) {
				mServerSocket.close();
			}
			mServerSocket = null;
			((ServerMessageCallback) mMessageCallback).cancel();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
