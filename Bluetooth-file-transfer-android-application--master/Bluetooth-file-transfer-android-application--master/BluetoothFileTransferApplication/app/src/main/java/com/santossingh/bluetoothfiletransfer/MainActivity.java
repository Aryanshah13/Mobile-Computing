package com.santossingh.bluetoothfiletransfer;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    // Define UI elements
    Button buttonOpenDialog, buttonUp, send;
    TextView textFolder;
    EditText dataPath;
    ListView dialogListView;

    // Constants for Bluetooth
    private static final int DISCOVER_DURATION = 300;
    private static final int REQUEST_BLU = 1;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    // File variables
    File root, curFolder;
    private List<String> fileList = new ArrayList<String>();
    private static final int CUSTOM_DIALOG_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI elements
        dataPath = findViewById(R.id.FilePath);
        buttonOpenDialog = findViewById(R.id.opendailog);
        send = findViewById(R.id.sendBtooth);

        buttonOpenDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataPath.setText("");
                showDialog(CUSTOM_DIALOG_ID);
            }
        });

        root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        curFolder = root;

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendViaBluetooth();
            }
        });
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
            case CUSTOM_DIALOG_ID:
                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dailoglayout);
                dialog.setTitle("File Selector");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);
                textFolder = dialog.findViewById(R.id.folder);
                buttonUp = dialog.findViewById(R.id.up);

                buttonUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListDir(curFolder.getParentFile());
                    }
                });

                dialogListView = dialog.findViewById(R.id.dialoglist);
                dialogListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        File selected = new File(fileList.get(position));
                        if (selected.isDirectory()) {
                            ListDir(selected);
                        } else if (selected.isFile()) {
                            getSelectedFile(selected);
                        } else {
                            dismissDialog(CUSTOM_DIALOG_ID);
                        }
                    }
                });
                break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case CUSTOM_DIALOG_ID:
                ListDir(curFolder);
                break;
        }
    }

    public void getSelectedFile(File file) {
        dataPath.setText(file.getAbsolutePath());
        fileList.clear();
        dismissDialog(CUSTOM_DIALOG_ID);
    }

    public void ListDir(File file) {
        if (file.equals(root)) {
            buttonUp.setEnabled(false);
        } else {
            buttonUp.setEnabled(true);
        }
        curFolder = file;
        textFolder.setText(file.getAbsolutePath());
        dataPath.setText(file.getAbsolutePath());
        File[] files = file.listFiles();
        fileList.clear();

        if (files != null) {
            for (File f : files) {
                fileList.add(f.getPath());
            }
        }

        ArrayAdapter<String> directoryList = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        dialogListView.setAdapter(directoryList);
    }

    public void sendViaBluetooth() {
        if (!dataPath.getText().toString().isEmpty()) {
            if (bluetoothAdapter == null) {
                Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_LONG).show();
            } else {
                enableBluetooth();
            }
        } else {
            Toast.makeText(this, "Please select a file.", Toast.LENGTH_LONG).show();
        }
    }

    public void enableBluetooth() {
        Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, DISCOVER_DURATION);
        startActivityForResult(discoveryIntent, REQUEST_BLU);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == DISCOVER_DURATION && requestCode == REQUEST_BLU) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setType("*/*");
            File file = new File(dataPath.getText().toString());
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> list = packageManager.queryIntentActivities(intent, 0);

            if (list.size() > 0) {
                String packageName = null;
                String className = null;
                boolean found = false;

                for (ResolveInfo info : list) {
                    packageName = info.activityInfo.packageName;
                    if (packageName.equals("com.android.bluetooth")) {
                        className = info.activityInfo.name;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Toast.makeText(this, "Bluetooth not found", Toast.LENGTH_LONG).show();
                } else {
                    intent.setClassName(packageName, className);
                    startActivity(intent);
                }
            }
        } else {
            Toast.makeText(this, "Bluetooth is canceled", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Toast.makeText(this, "****************\nDeveloper: Santosh Kumar Singh\nContact: superssingh@gmail.com\n****************", Toast.LENGTH_LONG).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
