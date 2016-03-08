package padmal.remote;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class Remote extends AppCompatActivity {

    String device_name, sending_data;
    Integer found = 0, opened = 0, closed = 0;
    Button up, down, left, right, stop, connect;
    EditText device;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);

        setup_main();
    }

    public void clicks(View view) {
        if (connect.getText().toString().equalsIgnoreCase("|≈|")) {

            device_name = device.getText().toString();
            new bluetooth_stuff().execute("Find Devices");
            new bluetooth_stuff().execute("Open Device");

        } else {
            new bluetooth_stuff().execute("Close Device");
        }
    }

    public void controls(View view) {

        if (view.getId() == R.id.btnstop) {
            sending_data = "1";
        } else if (view.getId() == R.id.btnup) {
            sending_data = "2";
        } else if (view.getId() == R.id.btndown) {
            sending_data = "3";
        } else if (view.getId() == R.id.btnleft) {
            sending_data = "4";
        } else if (view.getId() == R.id.btnright) {
            sending_data = "5";
        }

        try {
            send_command(sending_data);
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error!!!", Toast.LENGTH_SHORT).show();
        }
    }

    // Initiate main objects
    private void setup_main() {

        // Define connect button
        connect = (Button)findViewById(R.id.btn_connect);
        // Define movement keys
        up = (Button)findViewById(R.id.btnup); up.setClickable(false);
        down = (Button)findViewById(R.id.btndown); down.setClickable(false);
        left = (Button)findViewById(R.id.btnleft); left.setClickable(false);
        right = (Button)findViewById(R.id.btnright); right.setClickable(false);
        stop = (Button)findViewById(R.id.btnstop); stop.setClickable(false);
        // Define device ID text field
        device = (EditText)findViewById(R.id.device_id);
    }

    // Enable all buttons
    private void enable_buttons(boolean state) {

        up.setClickable(state);
        down.setClickable(state);
        right.setClickable(state);
        left.setClickable(state);
        stop.setClickable(state);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_remote, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void find_devices() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            new bluetooth_stuff().execute("Do nothing");
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enBT, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice d : pairedDevices) {
                if (d.getName().equalsIgnoreCase(device_name))
                {
                    mmDevice = d;
                    found = 1;
                    break;
                }
            }
        }
    }

    void open_device() throws IOException {

        if (mmDevice == null) {
            return;
        }

        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
        mmSocket.connect();
        mmOutputStream = mmSocket.getOutputStream();
        enable_buttons(true);
        opened = 1;
    }

    void close_device() throws IOException {

        if (mmOutputStream == null) {
            return;
        }

        if (mmSocket != null) {
            mmOutputStream.close();
            mmSocket.close();
            enable_buttons(false);
            closed = 1;
        }
    }

    void send_command(String D_data) throws IOException {
        if (mmOutputStream == null) {
            return;
        }

        mmOutputStream.write(D_data.getBytes());
    }

    private class bluetooth_stuff extends AsyncTask<String, Void, Integer> {

        @Override
        protected Integer doInBackground(String... command) {
            if (command[0].equalsIgnoreCase("Find Devices")) {
                find_devices();
                return 1;
            }
            if (command[0].equalsIgnoreCase("Open Device")){
                try {
                    open_device();
                    return 2;
                } catch (IOException e) {
                    return 3;
                }
            }
            if (command[0].equalsIgnoreCase("Close Device")) {
                try {
                    close_device();
                    return 4;
                } catch (IOException e) {
                    return 5;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Integer integer) {
            switch (integer) {
                case 1:
                    if (found == 1) {
                        Toast.makeText(getApplicationContext(), device_name + " found! :)",Toast.LENGTH_SHORT).show();
                        break;
                    } else {
                        break;
                    }
                case 2:
                    if (opened == 1) {
                        Toast.makeText(getApplicationContext(), "Connection established to " + device_name + " :)", Toast.LENGTH_LONG).show();
                        connect.setText("|=|");
                        break;
                    } else  {
                        break;
                    }
                case 3:
                    Toast.makeText(getApplicationContext(), "Cannot find the device :(", Toast.LENGTH_LONG).show();
                case 4:
                    connect.setText("|≈|");
                    Toast.makeText(getApplicationContext(),"Device disconnected!!! :o", Toast.LENGTH_LONG).show();
                    break;
                case 5:
                    connect.setText("|=|");
                    Toast.makeText(getApplicationContext(),"Error closing the connection!!! :o", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }
    }

}
