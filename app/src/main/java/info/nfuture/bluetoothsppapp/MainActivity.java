package info.nfuture.bluetoothsppapp;

import info.nfuture.bluetoothsppapp.ServerThread.ServerMessageCallback;

import java.io.IOException;
import java.util.UUID;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	private static final int REQUEST_ENABLE_BT = 1234;
	private static final int REQUEST_DISCOVERABLE_BT = 5678;
	private static final int DURATION = 300;
	private final String TAG = getClass().getSimpleName();
	private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	private UUID mUuid;

//	private static final String turnOn = "Bluetooth On";
//	private static final String turnOff = "Bluetooth Off";
	
	private ServerThread mServerThread = null;
	private ClientThread mClientThread = null;
	private Handler mHandler = new Handler();
	
	private ServerMessageCallback mServerMessageCallback = new ServerMessageCallback() {

		@Override
		public void receiveMessage(final String message) {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					MainActivity.this.receiveMessage(message);
				} 
				
			});
		}
		
		@Override
		public void accepting() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					TextView tv = (TextView) findViewById(R.id.server_side);
					tv.setText( getString( R.string.server_side ) + " accepting...(" + mUuid.toString() + ")");
					
				} 
				
			});
		}

		@Override
		public void accepted() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					TextView tv = (TextView) findViewById(R.id.server_side);
					tv.setText( getString( R.string.server_side ) + " accepted (" + mUuid.toString() + ")" );
					
				} 
				
			});
			
		}

		@Override
		public void cancel() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					TextView tv = (TextView) findViewById(R.id.server_side);
					tv.setText( getString( R.string.server_side ) );
					
				} 
				
			});
		}

		
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		mUuid = UUID.randomUUID();
		Log.d(TAG, "UUID = " + mUuid.toString());
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Switch sw = (Switch) findViewById(R.id.toggleEnable);
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleEnable(isChecked);
            }
        });

		showToggleEnableButton();
	}
	
	public void showToggleEnableButton() {
        Switch sw = (Switch) findViewById(R.id.toggleEnable);
		if (mBluetoothAdapter.isEnabled()) {
            if (! sw.isChecked()) {
                sw.setChecked(true);
            }
		} else {
            if (sw.isChecked()) {
                sw.setChecked(false);
            }
		}
	}
	
	public void toggleEnable(boolean isChecked) {
		if (isChecked) {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(intent, REQUEST_ENABLE_BT);
		} else {
			mBluetoothAdapter.disable();
		}
        showToggleEnableButton();
	}
	
	public void startDiscoverable(View v) {
		if (!mBluetoothAdapter.isEnabled()) {
			alertDialog("Warning", "Bluetoothが有効になっていません");
		} else {
			Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DURATION);
			startActivityForResult(intent, REQUEST_DISCOVERABLE_BT);

		}
	}
	
	public void startServer(View v) {
		if (!mBluetoothAdapter.isEnabled()) {
			alertDialog("Warning", "Bluetoothが有効になっていません");
		} else {
			if (mServerThread != null) {
				mServerThread.cancel();
			}
			
			mServerThread = new ServerThread(mBluetoothAdapter,this.getPackageName(),mUuid,mServerMessageCallback);
			mServerThread.start();
		}
	}
	
	
	public void stopServer(View v) {
		if (mServerThread != null) {
			mServerThread.cancel();
			mServerThread = null;
		}
	}
	
	public void writeData(View v) {
		if (mServerThread != null) {
			EditText et = (EditText) findViewById(R.id.writeText);
			String message = et.getText().toString();
			try {
				mServerThread.sendMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void clearResult(View v) {
		TextView tv = (TextView) findViewById(R.id.result_text);
		tv.setText( "" );

	}
	public void alertDialog(String title,String message) {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				
			}
			
		});
		alertDialog.show();
		
	}
	
	

	@Override
	protected void onResume() {
		super.onResume();
		showToggleEnableButton();
	}

	@Override
	protected void onPause() {
		super.onPause();
		stopServer(null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_ENABLE_BT) {
			showToggleEnableButton();
		} else if (requestCode == REQUEST_DISCOVERABLE_BT) {
//			if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
//				
//			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	public void receiveMessage(String message) {
		TextView tv = (TextView) findViewById(R.id.result_text);
		String text = (String) tv.getText();
		tv.setText( text + message + "\n");
	}

	public static int MENU_ID_CLIENT_MODE = 1;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem actionItem = menu.add(Menu.NONE,MENU_ID_CLIENT_MODE,Menu.NONE,"Client Mode");
        actionItem.setIcon(android.R.drawable.ic_menu_manage);
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == MENU_ID_CLIENT_MODE) {
			//	Toast.makeText(this, "Selected Item", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(this,ClientActivity.class);
			startActivity(intent);
		}
        return super.onOptionsItemSelected(item);
    }
}
