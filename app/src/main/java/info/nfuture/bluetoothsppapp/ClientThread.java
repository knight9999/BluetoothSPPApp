package info.nfuture.bluetoothsppapp;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;

public class ClientThread extends ReceiverThread {

	public ClientThread(BluetoothDevice server,UUID uuid,MessageCallback messageCallback) {
		super(messageCallback);
		try {
			mSocket = server.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	@Override
	public void run() {
		try {
			mSocket.connect();
			loop();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			cancel();
		}
	}

	public void cancel() {
		try {
			mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
