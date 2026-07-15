// SPDX-FileCopyrightText: Copyright (c) 2024-2025 Infineon Technologies AG
// SPDX-License-Identifier: MIT

package com.infineon.css.nbt_wifi_p2p.utils.wifi_direct;

import android.net.wifi.p2p.WifiP2pConfig;

import com.infineon.hsw.apdu.ApduChannel;
import com.infineon.hsw.ndef.IfxNdefMessage;

import java.net.InetAddress;

public class WifiP2pGlobalConfig {
    private static WifiP2pGlobalConfig instance;

    private String macAddress;
    private String deviceName;  // Not used yet
    private int connectionSucceed = 0;  /* 0- False, 1- On going, 2 - Connected */
    private InetAddress inetAddresses;
    private ApduChannel apduChannel;
    private IfxNdefMessage ifxNdefMessage;
    private WifiP2pConfig p2pConfig;

    public static final int CONNECTION_FAILED = 0;
    public static final int CONNECTION_INITIATED = 1;
    public static final int CONNECTION_SUCCEEDED = 2;

    // Restrict constructor from being instantiated
    private WifiP2pGlobalConfig(){}

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setDeviceName(String deviceName){
        this.deviceName = deviceName;
    }

    public String getDeviceName(){
        return this.deviceName;
    }

    public int getConnectionSucceed() {
        return connectionSucceed;
    }

    public void setConnectionSucceed(int connectionSucceed) {
        this.connectionSucceed = connectionSucceed;
    }

    public InetAddress getInetAddresses() {
        return inetAddresses;
    }

    public void setInetAddresses(InetAddress inetAddresses) {
        this.inetAddresses = inetAddresses;
    }

    public static synchronized WifiP2pGlobalConfig getInstance(){
        if(instance==null){
            instance=new WifiP2pGlobalConfig();
        }
        return instance;
    }

    public ApduChannel getApduChannel() {
        return apduChannel;
    }

    public void setApduChannel(ApduChannel apduChannel) {
        this.apduChannel = apduChannel;
    }

    public IfxNdefMessage getIfxNdefMessage() {
        return ifxNdefMessage;
    }

    public void setIfxNdefMessage(IfxNdefMessage ifxNdefMessage) {
        this.ifxNdefMessage = ifxNdefMessage;
    }

    public WifiP2pConfig getP2pConfig() {
        return p2pConfig;
    }

    public void setP2pConfig(WifiP2pConfig p2pConfig) {
        this.p2pConfig = p2pConfig;
    }
}
