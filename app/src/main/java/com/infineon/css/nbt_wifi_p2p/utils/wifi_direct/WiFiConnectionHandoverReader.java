// SPDX-FileCopyrightText: Copyright (c) 2024-2025 Infineon Technologies AG
// SPDX-License-Identifier: MIT

package com.infineon.css.nbt_wifi_p2p.utils.wifi_direct;

import android.nfc.FormatException;

import androidx.annotation.NonNull;

import com.infineon.css.nbt_wifi_p2p.utils.commands.ReadNdef;
import com.infineon.hsw.apdu.ApduChannel;
import com.infineon.hsw.apdu.ApduException;
import com.infineon.hsw.apdu.nbt.model.FileAccessPolicyException;
import com.infineon.hsw.ndef.IfxNdefMessage;
import com.infineon.hsw.ndef.exceptions.NdefException;
import com.infineon.hsw.ndef.records.AbstractRecord;
import com.infineon.hsw.ndef.records.rtd.IfxNdefRecord;
import com.infineon.hsw.utils.UtilException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;


public class WiFiConnectionHandoverReader {

    private static final int TLV_ID_SSID = 0x1045;
    private static final int TLV_ID_MAC  = 0x1020;
    private static final int HEADER_FIELD_BYTES = 2; // bytes per tag or length field
    private static final int TLV_OVERHEAD = 2 + 2;   // tag(2) + length(2)

    /** APDU channel used to communicate with the NBT */
    private ApduChannel apduChannel;

    public WiFiConnectionHandoverReader() { }

    /**
     * Decode a WFA WSC TLV payload extracting SSID (0x1045) and MAC (0x1020)
     * @param recordBytes full TLV payload (first two bytes contain total payload length)
     * @return array: [0] SSID (String), [1] MAC address (uppercase colon separated)
     * @throws FormatException if structure, bounds, or logical validation fails
     */
    public String[] decodeNDEFRecord(byte[] recordBytes) throws FormatException {
        if (recordBytes == null || recordBytes.length < 2) {
            throw new FormatException("Record too short");
        }

        String ssid = null;
        String mac = null;

        // Unsigned 16-bit length (avoid sign extension by masking)
        int declaredLength = ((recordBytes[0] & 0xFF) << 8) | (recordBytes[1] & 0xFF);
        int offset = 2;

        while (offset + TLV_OVERHEAD <= recordBytes.length) {
            int tag = ((recordBytes[offset] & 0xFF) << 8) | (recordBytes[offset + 1] & 0xFF);
            int length = ((recordBytes[offset + 2] & 0xFF) << 8) | (recordBytes[offset + 3] & 0xFF);
            int valueStart = offset + TLV_OVERHEAD;
            int valueEnd = valueStart + length;

            if (valueEnd > recordBytes.length) {
                throw new FormatException("TLV value exceeds buffer");
            }

            byte[] value = Arrays.copyOfRange(recordBytes, valueStart, valueEnd);
            if (tag == TLV_ID_SSID) {
                ssid = new String(value, StandardCharsets.UTF_8);
            } else if (tag == TLV_ID_MAC) {
                if (length != 6) { // MAC must be 6 bytes
                    throw new FormatException("Invalid MAC length: " + length);
                }
                StringBuilder sb = new StringBuilder(17);
                for (int i = 0; i < value.length; i++) {
                    sb.append(String.format("%02X", value[i] & 0xFF));
                    if (i < value.length - 1) sb.append(":");
                }
                mac = sb.toString();
            }
            offset = valueEnd; // Move to next TLV
        }

        if (declaredLength != (offset - 2)) {
            throw new FormatException("Declared length mismatch");
        }
        if (ssid == null) {
            throw new FormatException("SSID TLV (0x1045) missing");
        }
        if (mac == null) {
            throw new FormatException("MAC TLV (0x1020) missing");
        }

        return new String[]{ssid, mac};
    }

    /**
     * Executes reading & parsing of Wi-Fi connection handover data and stores results globally.
     * @param apduChannel channel to the NBT
     * @throws UtilException library utility failure
     * @throws ApduException APDU transmission failure
     * @throws FileAccessPolicyException access policy violation
     * @throws IOException IO related failure
     * @throws NdefException NDEF structure or parsing failure
     */
    public void execute(@NonNull ApduChannel apduChannel) throws UtilException, ApduException, FileAccessPolicyException, IOException, NdefException {
        this.apduChannel = apduChannel;
        WifiP2pGlobalConfig reqConfig = WifiP2pGlobalConfig.getInstance();

        byte[] raw;
        try {
            raw = readWifiConnectionHandoverNdefMessage();
            IfxNdefMessage ndefMessage = new IfxNdefMessage(raw);
            reqConfig.setIfxNdefMessage(ndefMessage);
            reqConfig.setApduChannel(apduChannel);

            boolean parsed = false;
            for (AbstractRecord record : ndefMessage.getNdefRecords()) {
                String type = new String(record.getRecordType().getType());
                if ("application/vnd.wfa.wsc".equals(type)) {
                    IfxNdefRecord ifxNdefRecord = (IfxNdefRecord) record;
                    byte[] payload = Objects.requireNonNull(ifxNdefRecord.getPayload(), "Record payload null");
                    String[] parts = decodeNDEFRecord(payload);
                    reqConfig.setDeviceName(parts[0]);
                    reqConfig.setMacAddress(parts[1]);
                    parsed = true;
                }
            }
            if (!parsed) {
                throw new NdefException("No WFA WSC record found in NDEF message");
            }
        } catch (FormatException fe) {
            throw new NdefException("Format error decoding WSC TLV: " + fe.getMessage());
        } catch (NdefException ne) {
            throw ne; // rethrow specific
        } catch (Exception e) {
            throw new NdefException("Unexpected error reading NDEF: " + e.getMessage());
        }
    }

    private byte[] readWifiConnectionHandoverNdefMessage() throws UtilException, ApduException {
        ReadNdef readNdef = new ReadNdef();
        readNdef.execute(this.apduChannel);
        return readNdef.getNdefMessage();
    }
}
