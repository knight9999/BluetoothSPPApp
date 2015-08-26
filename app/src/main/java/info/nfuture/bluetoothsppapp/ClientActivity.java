package info.nfuture.bluetoothsppapp;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ClientActivity extends ActionBarActivity {
    private static final int REQUEST_ENABLE_BT = 1234;
    private static final int REQUEST_DISCOVERABLE_BT = 5678;
    private final String TAG = getClass().getSimpleName();
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static final String TARGET_DEVICE_NAME = "TargetDeviceName";
    private static final String TARGET_UUID = "TargetUUID";

    private String mTargetDeviceName = null;
    private String mTargetUUID = null;

    private AlertDialog dialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mTargetDeviceName = SharedData.loadString(this, TARGET_DEVICE_NAME);
        mTargetUUID = SharedData.loadString(this, TARGET_UUID);

        showTargetDevice();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter;
        filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(receiver, filter);

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void toggleEnable(boolean isChecked) {
        if (isChecked) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothAdapter.disable();
        }
    }


    public void setTargetDevice(TargetDevice targetDevice) {
        mTargetDeviceName = targetDevice.name;
        if (mTargetDeviceName != null) {
            SharedData.saveString(this, TARGET_DEVICE_NAME, mTargetDeviceName);
        } else {
            SharedData.remove(this, TARGET_DEVICE_NAME);
        }
        showTargetDevice();
    }

    public void setTargetUUID(UUID uuid) {
        mTargetUUID = uuid.toString();
        if (mTargetUUID != null) {
            SharedData.saveString(this, TARGET_UUID, mTargetUUID);
        } else {
            SharedData.remove(this, TARGET_UUID);
        }
        showTargetUUID();
    }

    public void showTargetDevice() {
        TextView textView = (TextView) findViewById(R.id.text_device);
        if (mTargetDeviceName != null) {
            textView.setText(mTargetDeviceName);
        } else {
            textView.setText(R.string.device_empty);
        }
    }

    public void showTargetUUID() {
        TextView textView = (TextView) findViewById(R.id.text_uuid);
        if (mTargetUUID != null) {
            textView.setText(mTargetUUID);
        } else {
            textView.setText(R.string.uuid_empty);
        }
    }

    public class TargetDevice {
        public String name;
        public String address;

        @Override
        public int hashCode() {
            return (name+"/"+address).hashCode();
        }

        @Override
        public boolean equals(Object o) {
            TargetDevice t = (TargetDevice) o;
            return t.name.equals( name ) && t.address.equals( address );
        }
    }

    public void searchDevice(View v) {
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);

        final ViewGroup myview = (ViewGroup) getLayoutInflater().inflate(R.layout.view_select_devices,null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Device");
        builder.setView(myview);

        dialog = builder.create();
        dialog.show();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog = null;
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
            }
        });
//        dialog.getWindow().setLayout( (int) (size.x*0.8) , 2000 );
//        dialog.getWindow().setLayout( (int) (size.x*0.8) , (int) (size.y*0.8) );

        List<TargetDevice> deviceList = getTargetDeviceList();

        final Context context = this;
        ListView lv = (ListView) myview.findViewById(R.id.listDevices);
        ArrayAdapter<TargetDevice> adapter = new CustomArrayAdapter<TargetDevice>(context,R.layout.row_device,deviceList) {
            private LayoutInflater layoutInflater;

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TargetDevice item = getItem(position);
                if (null == convertView) {
                    if (layoutInflater == null) {
                        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    }
                    convertView = layoutInflater.inflate(R.layout.row_device,null);
                }
                TextView textView = (TextView) convertView.findViewById(R.id.row_textview1);
                textView.setText( item.name );
                return convertView;
            }
        };
        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                TargetDevice targetDevice = (TargetDevice) listView.getItemAtPosition(position);
                setTargetDevice(targetDevice);
                dialog.dismiss();
            }
        });

        Button btnCancel = (Button) myview.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        Button btnRefresh = (Button) myview.findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ListView lv = (ListView) dialog.findViewById(R.id.listDevices);
                ArrayAdapter adapter = (ArrayAdapter) lv.getAdapter();
                adapter.clear();

                List<TargetDevice> deviceList = getTargetDeviceList();
                for (TargetDevice targetDevice : deviceList) {
                    adapter.add(targetDevice);
                }
                adapter.notifyDataSetChanged();


                ProgressBar progressBar = (ProgressBar) myview.findViewById(R.id.progressBar);
                progressBar.setVisibility(View.VISIBLE);
//                for (BluetoothDevice device : mBluetoothAdapter.getBondedDevices() ) {
//                    if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
//                        Method removeBond = null;
//                        try {
//                            removeBond = device.getClass().getMethod("removeBond");
//                            removeBond.invoke(device);
//                        } catch (NoSuchMethodException e) {
//                            e.printStackTrace();
//                        } catch (InvocationTargetException e) {
//                            e.printStackTrace();
//                        } catch (IllegalAccessException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                };
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                mBluetoothAdapter.startDiscovery();

            }
        });
    }

    private List<TargetDevice> getTargetDeviceList() {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        List<TargetDevice> deviceList = new ArrayList<TargetDevice>();
        for ( BluetoothDevice device : devices ) {
            TargetDevice targetDevice = convertTargetDeviceFromBluetoothDevice(device);
            deviceList.add( targetDevice );
        }
        return deviceList;
    }

    private TargetDevice convertTargetDeviceFromBluetoothDevice(BluetoothDevice device) {
        TargetDevice targetDevice = new TargetDevice();
        targetDevice.name = device.getName();
        targetDevice.address = device.getAddress();
        return targetDevice;
    }

    public void searchUuid(View v) {


    }


    BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(TAG, "DISCOVERY START");
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action) ) {
                Log.d(TAG, "DISCOVERY FINISHED");
                if (dialog != null) {
                    ProgressBar progressBar = (ProgressBar) dialog.findViewById(R.id.progressBar);
                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (foundDevice.getName() != null) {
                    if (foundDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
                        TargetDevice targetDevice = convertTargetDeviceFromBluetoothDevice(foundDevice);
                        ListView lv = (ListView) dialog.findViewById(R.id.listDevices);
                        CustomArrayAdapter adapter = (CustomArrayAdapter) lv.getAdapter();
                        if (! adapter.contains(targetDevice)) {
                            adapter.add(targetDevice);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            } else if (BluetoothDevice.ACTION_NAME_CHANGED.equals(action)) {
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (foundDevice.getName() != null) {
                    int bondState = foundDevice.getBondState();
                    if (bondState != BluetoothDevice.BOND_BONDED) {
                        TargetDevice targetDevice = convertTargetDeviceFromBluetoothDevice(foundDevice);
                        ListView lv = (ListView) dialog.findViewById(R.id.listDevices);
                        CustomArrayAdapter adapter = (CustomArrayAdapter) lv.getAdapter();
                        if (! adapter.contains(targetDevice)) {
                            adapter.add(targetDevice);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice foundDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (foundDevice.getName() != null) {
                    int bondState = foundDevice.getBondState();
                    if (bondState != BluetoothDevice.BOND_BONDED) {
                        TargetDevice targetDevice = convertTargetDeviceFromBluetoothDevice(foundDevice);
                        ListView lv = (ListView) dialog.findViewById(R.id.listDevices);
                        CustomArrayAdapter adapter = (CustomArrayAdapter) lv.getAdapter();
                        if (!adapter.contains(targetDevice)) {
                            adapter.add(targetDevice);
                            adapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == REQUEST_ENABLE_BT) {
//        } else if (requestCode == REQUEST_DISCOVERABLE_BT) {
//        }

        super.onActivityResult(requestCode, resultCode, data);
    }



    public static int MENU_ID_TOOGLE_BLUETOOTH = 1;
    public static int MENU_ID_SERVER_MODE = 2;

    public static String BLUETOOTH_ON = "Bluetooth On";
    public static String BLUETOOTH_OFF= "Bluetooth Off";

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem actionItem;

        actionItem = menu.add(Menu.NONE,MENU_ID_TOOGLE_BLUETOOTH,Menu.NONE,BLUETOOTH_ON);
        actionItem.setIcon(android.R.drawable.ic_menu_manage);
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);

        actionItem = menu.add(Menu.NONE,MENU_ID_SERVER_MODE,Menu.NONE,"Server Mode");
        actionItem.setIcon(android.R.drawable.ic_menu_manage);
        actionItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(MENU_ID_TOOGLE_BLUETOOTH);
        if (mBluetoothAdapter.isEnabled()) {
            item.setTitle(BLUETOOTH_OFF);
        } else {
            item.setTitle(BLUETOOTH_ON);
        }
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == MENU_ID_TOOGLE_BLUETOOTH) {
            toggleEnable( ! mBluetoothAdapter.isEnabled() );
        } else if (item.getItemId() == MENU_ID_SERVER_MODE) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
