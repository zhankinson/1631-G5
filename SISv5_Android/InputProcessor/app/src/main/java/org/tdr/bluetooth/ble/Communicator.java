package org.tdr.bluetooth.ble;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import android.os.AsyncTask;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import java.net.Socket;
import java.net.SocketException;
import java.io.OutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Random;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.tdr.tdr.entity.*;

import org.tdr.R;

public class Communicator extends Activity {
	private final static String TAG = Communicator.class.getSimpleName();

	public static final String EXTRAS_DEVICE = "EXTRAS_DEVICE";
	private TextView dataView = null;

	private EditText et = null;
	private Button btn = null;
	private String mDeviceName;
	private String mDeviceAddress;
	private RBLService mBluetoothLeService;
	private Map<UUID, BluetoothGattCharacteristic> map = new HashMap<UUID, BluetoothGattCharacteristic>();

	private EditText uploaderIp = null;
	private EditText uploaderPort = null;

	//ClientSimulator client;

	private Button setip = null;

	private final ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName componentName,
									   IBinder service) {
			mBluetoothLeService = ((RBLService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			mBluetoothLeService.connect(mDeviceAddress);
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			mBluetoothLeService = null;
		}
	};

	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();

			if (RBLService.ACTION_GATT_DISCONNECTED.equals(action)) {
			} else if (RBLService.ACTION_GATT_SERVICES_DISCOVERED
					.equals(action)) {
				getGattService(mBluetoothLeService.getSupportedGattService());
			} else if (RBLService.ACTION_DATA_AVAILABLE.equals(action)) {
				displayData(intent.getByteArrayExtra(RBLService.EXTRA_DATA));
			}
		}
	};
	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	private GoogleApiClient client2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.second);

		dataView = (TextView) findViewById(R.id.dataView);

		dataView.setMovementMethod(ScrollingMovementMethod.getInstance());
		et = (EditText) findViewById(R.id.editText);
		btn = (Button) findViewById(R.id.send);
		btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BluetoothGattCharacteristic characteristic = map.get(RBLService.UUID_BLE_SHIELD_TX);

				String str = et.getText().toString();
				byte b = 0x00;
				byte[] tmp = str.getBytes();
				byte[] tx = new byte[tmp.length + 1];
				tx[0] = b;
				for (int i = 1; i < tmp.length + 1; i++) {
					tx[i] = tmp[i - 1];
				}
				characteristic.setValue(tx);
				mBluetoothLeService.writeCharacteristic(characteristic);
				et.setText("");
			}
		});

		uploaderIp = (EditText) findViewById(R.id.uploaderIp);
		uploaderPort = (EditText) findViewById(R.id.uploaderPort);
		setip = (Button) findViewById(R.id.setip);
		setip.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final String ip = uploaderIp.getText().toString();
				final String strPort = uploaderPort.getText().toString();
				if (ip != null && strPort != null) {
					new Thread(new Runnable() {
						@Override
						public void run() {
							//client = new ClientSimulator(ip, Integer.parseInt(strPort));
							//TODO client.initialize();

						}
					}).start();

				}

			}
		});

		Intent intent = getIntent();

		mDeviceAddress = intent.getStringExtra(Device.EXTRA_DEVICE_ADDRESS);
		mDeviceName = intent.getStringExtra(Device.EXTRA_DEVICE_NAME);

		getActionBar().setTitle(mDeviceName);
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent gattServiceIntent = new Intent(this, RBLService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			mBluetoothLeService.disconnect();
			mBluetoothLeService.close();
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onStop() {
		super.onStop();// ATTENTION: This was auto-generated to implement the App Indexing API.
// See https://g.co/AppIndexing/AndroidStudio for more information.
		AppIndex.AppIndexApi.end(client2, getIndexApiAction());
		unregisterReceiver(mGattUpdateReceiver);
		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client2.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mBluetoothLeService.disconnect();
		mBluetoothLeService.close();
	}

	private void displayData(byte[] byteArray) {
		if (byteArray != null) {
			String data = new String(byteArray);
			Log.e(TAG, "data->" + data);
			if (data.startsWith("Hi")) {
				String tmp = dataView.getText().toString();
				if (!tmp.endsWith("\nCommunicating...")) {
					dataView.setText(tmp + "\nCommunicating...");
				}
			} else {
				Log.e("socket", "Received data: "+data);
				dataView.append("\n=======================");
				dataView.append("\n" + data);
				//TODO
				if(data.contains("EMG:") && data.contains("ECG:")){
					Intent intent = new Intent(Communicator.this, input.MainActivity.class);
					intent.putExtra("data",data);
					startActivity(intent);
				}
			}

			// find the amount we need to scroll. This works by
			// asking the TextView's internal layout for the position
			// of the final line and then subtracting the TextView's height
			final int scrollAmount = dataView.getLayout().getLineTop(dataView.getLineCount()) - dataView.getHeight();
			// if there is no need to scroll, scrollAmount will be <=0
			if (scrollAmount > 0)
				dataView.scrollTo(0, scrollAmount);
			else
				dataView.scrollTo(0, 0);
		}
	}

	private void getGattService(BluetoothGattService gattService) {
		if (gattService == null)
			return;
		BluetoothGattCharacteristic characteristic = gattService
				.getCharacteristic(RBLService.UUID_BLE_SHIELD_TX);
		map.put(characteristic.getUuid(), characteristic);
		BluetoothGattCharacteristic characteristicRx = gattService
				.getCharacteristic(RBLService.UUID_BLE_SHIELD_RX);
		mBluetoothLeService.setCharacteristicNotification(characteristicRx, true);
		mBluetoothLeService.readCharacteristic(characteristicRx);
	}

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(RBLService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(RBLService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(RBLService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}

	private static final String SCOPE = "SIS.Scope1";
	private static final String SENDER = "AndroidSensor";



	/**
	 * ATTENTION: This was auto-generated to implement the App Indexing API.
	 * See https://g.co/AppIndexing/AndroidStudio for more information.
	 */
	public Action getIndexApiAction() {
		Thing object = new Thing.Builder()
				.setName("Communicator Page") // TODO: Define a title for the content shown.
				// TODO: Make sure this auto-generated URL is correct.
				.setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
				.build();
		return new Action.Builder(Action.TYPE_VIEW)
				.setObject(object)
				.setActionStatus(Action.STATUS_TYPE_COMPLETED)
				.build();
	}

	@Override
	public void onStart() {
		super.onStart();

		// ATTENTION: This was auto-generated to implement the App Indexing API.
		// See https://g.co/AppIndexing/AndroidStudio for more information.
		client2.connect();
		AppIndex.AppIndexApi.start(client2, getIndexApiAction());
	}
//	private KeyValueList random_data() {
//		String data;
//
//		KeyValueList sensor_data = new KeyValueList();
//		sensor_data.putPair("Scope", SCOPE);
//		sensor_data.putPair("MessageType", "Reading");
//		sensor_data.putPair("Sender", "test");
//
//		Random r = new Random();
//
//		data = String.valueOf(r.nextInt(10) + 75) + "/" + String.valueOf(r.nextInt(10) + 115);
//		sensor_data.putPair("Data_BP", data);
//
//		data = String.valueOf(r.nextInt(10) + 840);
//		sensor_data.putPair("Data_EMG", data);
//
//		data = "4.1" + String.valueOf(r.nextInt(10));
//		sensor_data.putPair("Data_ECG", data);
//
//		data = String.valueOf(r.nextInt(50) + 70);
//		sensor_data.putPair("Data_Pulse", data);
//
//		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		Date date = new Date();
//		data = dateFormat.format(date);
//		sensor_data.putPair("Data_Date", data);
//
//		return sensor_data;
//	}
//	private class ClientConnection extends AsyncTask<Void, Void, Void> {
//		String serverAddress;
//		int serverPort;
//		String response = "";
//
//		ClientConnection(String addr, int port) {
//			Log.e("socket", "Socket->0");
//			serverAddress = addr;
//			serverPort = port;
//			Log.e("socket", "Socket->111");
//		}
//
//		@Override
//		protected void onPostExecute(Void aVoid) {
//			super.onPostExecute(aVoid);
//		}
//
//		@Override
//		protected Void doInBackground(Void... voids) {
//			Socket socket = null;
//
//			try {
//				Log.e("socket", "Socket->11");
//				socket = new Socket(serverAddress, serverPort);
//
//				Log.e("socket", "Socket->1");
//				OutputStream os = socket.getOutputStream();
//				MsgEncoder sentMsg = new MsgEncoder(os);
//				sentMsg.sendMsg(random_data());
//				Log.e("socket", "Socket->2");
//			} catch (UnknownHostException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				if (socket != null) {
//					try {
//						socket.close();
//					} catch (IOException e) {
//						e.printStackTrace();
//					}
//				}
//			}
//			return null;
//		}
//	}
}
