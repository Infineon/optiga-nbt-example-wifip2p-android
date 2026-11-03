// SPDX-FileCopyrightText: Copyright (c) 2024-2026 Infineon Technologies AG
// SPDX-License-Identifier: MIT

package com.infineon.css.nbt_wifi_p2p;

import android.Manifest;
import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.DeviceInfoListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.infineon.css.nbt_wifi_p2p.utils.wifi_direct.WiFiDirectBroadcastReceiver;
import com.infineon.css.nbt_wifi_p2p.utils.wifi_direct.WifiConnectionHandoverWriter;
import com.infineon.css.nbt_wifi_p2p.utils.wifi_direct.WifiP2pGlobalConfig;

import com.infineon.hsw.apdu.ApduChannel;
import com.infineon.hsw.ndef.IfxNdefMessage;

import java.util.ArrayList;
import java.util.Objects;


/**
 * An activity that uses Wi-Fi Direct APIs to discover and connect with available
 * devices. Wi-Fi Direct APIs are asynchronous and rely on callback mechanism
 * using interfaces to notify the application of operation success or failure.
 * The application should also register a BroadcastReceiver for notification of
 * Wi-Fi state related events.
 */
public class WiFiDirectActivity extends AppCompatActivity implements ChannelListener, DeviceInfoListener {
    public static final String TAG = "wifidirectdemo";
    private static final int P2P_PERMISSIONS_DISCOVERY_CODE = 1001;
    private static final int P2P_PERMISSIONS_CONNECT_CODE = 1002;
    private WifiP2pManager manager;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private Channel channel;
    private BroadcastReceiver receiver = null;

    /**
     * Instruction text to the user
     */
    TextView instructionText;
    ImageButton buttonReturn;

    /**
     * @param isWifiP2pEnabled the isWifiP2pEnabled to set
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        if (isWifiP2pEnabled) {
            Toast.makeText(this, "P2P is enabled", Toast.LENGTH_SHORT).show();
            // Call discover peers
            discoverPeers();
        } else {
            Toast.makeText(this, "P2P is disabled, Please turn on WiFi.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean initP2p() {
        // Device capability definition check
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e(TAG, "Wi-Fi Direct is not supported by this device.");
            return false;
        }
        // Hardware capability check
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.e(TAG, "Cannot get Wi-Fi system service.");
            return false;
        }
        if (!wifiManager.isP2pSupported()) {
            Log.e(TAG, "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
            return false;
        }
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (manager == null) {
            Log.e(TAG, "Cannot get Wi-Fi Direct system service.");
            return false;
        }
        channel = manager.initialize(this, getMainLooper(), null);
        if (channel == null) {
            Log.e(TAG, "Cannot initialize Wi-Fi Direct.");
            return false;
        }
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifidirect);

        //GUI initialization
        instructionText = findViewById(R.id.textView_instrText);
        instructionText.setText(R.string.string_wifi_direct_establishment);

        ProgressBar progressBar = findViewById(R.id.progressBar);

        // Hide the progress bar
        progressBar.setVisibility(View.GONE);

        // Show the progress bar
        progressBar.setVisibility(View.VISIBLE);

        buttonReturn = findViewById(R.id.button_return);
        buttonReturn.setOnClickListener(v -> finish());

        // Set connection status.
        WifiP2pGlobalConfig reqConfig = WifiP2pGlobalConfig.getInstance();
        reqConfig.setConnectionSucceed(WifiP2pGlobalConfig.CONNECTION_FAILED);

        // add necessary intent values to be matched.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        if (!initP2p()) {
            finish();
        }

        /* Request for device Info */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED) {
            // Permission should have granted in the upper activity
            finish();
        }

        manager.requestDeviceInfo(channel, this);
    }

    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
        registerReceiver(receiver, intentFilter);
        // discoverPeers();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void discoverPeers() {
        ArrayList<String> permissions = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED)) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }

        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), P2P_PERMISSIONS_DISCOVERY_CODE);
        }

        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated!",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Initiation failed!",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void connect(WifiP2pConfig config) {
        ArrayList<String> permissions = new ArrayList<>();
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) && (checkSelfPermission(Manifest.permission.NEARBY_WIFI_DEVICES) != PackageManager.PERMISSION_GRANTED)) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES);
        }

        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), P2P_PERMISSIONS_CONNECT_CODE);
        }
        manager.connect(channel, config, new ActionListener() {
            @Override
            public void onSuccess() {
                WifiP2pGlobalConfig.getInstance().setConnectionSucceed(WifiP2pGlobalConfig.CONNECTION_SUCCEEDED);
                Log.d(TAG, "Connection request sent successfully");
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Connection request failed with error: " + reason);
                WifiP2pGlobalConfig.getInstance().setConnectionSucceed(WifiP2pGlobalConfig.CONNECTION_FAILED);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean permissionsGranted = true;
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, ": P2P_PERMISSIONS_DISCOVERY_CODE: Permission " + permissions[i] + " not granted");
                permissionsGranted = false;
                break;
            } else {
                Log.d(TAG, ": P2P_PERMISSIONS_DISCOVERY_CODE: Permission " + permissions[i] + " granted");
            }
        }

        if (requestCode == P2P_PERMISSIONS_DISCOVERY_CODE) {
            if (permissionsGranted) {
                discoverPeers();
            }
        } else if (requestCode == P2P_PERMISSIONS_CONNECT_CODE) {
            if (permissionsGranted) {
                connect(WifiP2pGlobalConfig.getInstance().getP2pConfig());
            }
        }

    }

    public void disconnect() {
        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.d(TAG, "Failed to remove group");
            }

            @Override
            public void onSuccess() {
                Log.d(TAG, "Successfully removed group");
            }
        });
    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (manager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            retryChannel = true;
            manager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }

    /**
     * The requested {@link WifiP2pDevice} is available.
     *
     * @param wifiP2pDevice Wi-Fi p2p {@link WifiP2pDevice}
     */
    @Override
    public void onDeviceInfoAvailable(@Nullable WifiP2pDevice wifiP2pDevice) {
        Log.d(TAG, "This device P2p info: " + Objects.requireNonNull(wifiP2pDevice));

        WifiP2pGlobalConfig reqDevice = WifiP2pGlobalConfig.getInstance();
        ApduChannel apduChannel = reqDevice.getApduChannel();
        IfxNdefMessage ndefMessage = reqDevice.getIfxNdefMessage();

        WifiConnectionHandoverWriter my_wifiConnectionHandoverwriter = new WifiConnectionHandoverWriter();
        try {
            my_wifiConnectionHandoverwriter.execute(apduChannel, wifiP2pDevice.deviceName, wifiP2pDevice.deviceAddress, ndefMessage);

            if (wifiP2pDevice.status != WifiP2pDevice.AVAILABLE) {
                Log.d(TAG, "Status is not Unavailable");
                manager.removeGroup(channel, new ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG, "Successfully removed group");
                    }

                    @Override
                    public void onFailure(int reason) {
                        Log.d(TAG, "Failed to remove group");
                    }
                });
            }
            // Disconnect the apduChannel
            apduChannel.disconnect();
            Log.d(TAG, "Successfully written NDEF message.");

        } catch (Exception e) {
            Log.e(TAG, "Error while trying to write NDEF message");
            Log.d(TAG, e.toString());
            Toast.makeText(this, "Error while trying to write NDEF message", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
