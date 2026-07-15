// SPDX-FileCopyrightText: Copyright (c) 2024-2025 Infineon Technologies AG
// SPDX-License-Identifier: MIT


package com.infineon.css.nbt_wifi_p2p.utils.wifi_direct;


import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import android.net.wifi.WpsInfo;

import com.infineon.css.nbt_wifi_p2p.WiFiDirectActivity;

/**
 * A BroadcastReceiver that notifies of important Wi-Fi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private final WifiP2pManager manager;
    private final Channel channel;
    public static final String TAG = "WiFiDirectBroadCastReceiver";
    private final WiFiDirectActivity activity;

    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       WiFiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }


    private final PeerListListener peerListListener = new PeerListListener() {
        private final List<WifiP2pDevice> peers = new ArrayList<>();

        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            peers.clear();
            peers.addAll(peerList.getDeviceList());

            if (peers.isEmpty()) {
                Log.d(WiFiDirectActivity.TAG, "No devices found");
                // Retry
                activity.discoverPeers();
                return;
            }

            // Check if the mac address and device name matches the one present globally.
            WifiP2pGlobalConfig reqDevice = WifiP2pGlobalConfig.getInstance();
            WifiP2pConfig reqConfig = new WifiP2pConfig();
            for (WifiP2pDevice device: peers) {
                if (device.deviceName.equals(reqDevice.getDeviceName()) && device.deviceAddress.equals(reqDevice.getMacAddress().toLowerCase())) {
                    if (reqDevice.getConnectionSucceed() == WifiP2pGlobalConfig.CONNECTION_FAILED) {
                        reqConfig.deviceAddress = device.deviceAddress;
                        reqConfig.wps.setup = WpsInfo.PBC;
                        reqConfig.groupOwnerIntent = 0; // Make sure android doesn't become group owner.
                        reqDevice.setP2pConfig(reqConfig);
                        Log.d(TAG, "Connecting to device with MAC: " + reqDevice.getMacAddress() + ", SSID: " +  reqDevice.getDeviceName());
                        activity.connect(reqConfig);
                        reqDevice.setConnectionSucceed(WifiP2pGlobalConfig.CONNECTION_INITIATED);
                    }
                }
            }
        }

    };

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Check to see if Wi-Fi is enabled and notify appropriate activity
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            // Wifi P2P is enabled
            // Wi-Fi P2P is not enabled
            activity.setIsWifiP2pEnabled(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED);
            Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // Call WifiP2pManager.requestPeers() to get a list of current peers
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Log.d(WiFiDirectActivity.TAG, "ACCESS_FINE_LOCATION permission is not given!");
                    return;
                }
                manager.requestPeers(channel, peerListListener);
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null)
            {
                return;
            }
            Log.d(WiFiDirectActivity.TAG, "Wifi P2P connection changed.");
            manager.requestConnectionInfo(channel, connectionInfoListener);
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            // Respond to this device's wifi state changing
            Log.d(WiFiDirectActivity.TAG, "Device State changed");
        }
    }

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if (wifiP2pInfo.groupFormed) {
                // Set the group owner address in the global config
                WifiP2pGlobalConfig config = WifiP2pGlobalConfig.getInstance();
                config.setInetAddresses(wifiP2pInfo.groupOwnerAddress);

                Log.d(WiFiDirectActivity.TAG, "Connected to P2P network.");
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result", "WIFI_P2P_SUCCESS");
                activity.setResult(Activity.RESULT_OK, resultIntent);
                activity.finish();
            }
        }
    };
}
