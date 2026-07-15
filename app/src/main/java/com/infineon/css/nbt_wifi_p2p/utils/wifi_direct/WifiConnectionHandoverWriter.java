// SPDX-FileCopyrightText: Copyright (c) 2024-2025 Infineon Technologies AG
// SPDX-License-Identifier: MIT
package com.infineon.css.nbt_wifi_p2p.utils.wifi_direct;

import android.nfc.FormatException;
import androidx.annotation.NonNull;

import com.infineon.css.nbt_wifi_p2p.utils.commands.WriteNdef;
import com.infineon.hsw.apdu.ApduChannel;
import com.infineon.hsw.apdu.ApduException;
import com.infineon.hsw.ndef.IfxNdefMessage;
import com.infineon.hsw.ndef.exceptions.NdefException;
import com.infineon.hsw.ndef.records.AbstractRecord;
import com.infineon.hsw.ndef.records.rtd.IfxNdefRecord;
import com.infineon.hsw.utils.UtilException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class WifiConnectionHandoverWriter {

    /**
     * Establishes an interface channel with protocol (APDU) specific functionality
     */
    private ApduChannel apduChannel;

    /**
     * NDEF record encoder. Overwrites the existing NDEF record with provided SSID and MAC address.
     *
     * @param SSID SSID to write into NDEF record
     * @param macAddress MAC address to write into NDEF record
     * @param recordBytes Existing NDEF record bytes
     * @apiNote Currently no NDEF encoder library for wifi connection handover record. As a workaround, getting the NDEF
     * and updating info inplace, so no need to create from scratch
     * @return Updated NDEF record.
     *  @throws FormatException Thrown when NDEF parsing fails.
     */
    public byte[] encodeNDEFRecord(String SSID, String macAddress, byte[] recordBytes) throws FormatException {
        final int SSID_ID = 0x1045;
        final int MAC_ID = 0x1020;

        byte[] outNdef = new byte[recordBytes.length + SSID.length()];
        /* Convert SSID into bytes */
        byte[] ssid_bytes = SSID.getBytes(StandardCharsets.UTF_8);

        /* Convert MAC address xx:xx:xx:xx:xx:xx into bytes */
        byte[] mac_bytes = new byte[6];
        int i = 0;
        for (String element: macAddress.split(":")) {
            mac_bytes[i] = (byte) Integer.parseInt(element, 16);
        }

        int tag, length;

        // First two bytes are record length
        int recordLength = (recordBytes[0] << 8) + (recordBytes[1]);
        int offset = 2;
        int out_offset = 2;
        while (offset < recordBytes.length) {
            // Check buffer overflow for (tag + length)
            if ((offset + 3) < recordBytes.length) {
                tag = (recordBytes[offset] << 8) + (recordBytes[offset + 1]);
                length = (recordBytes[offset + 2] << 8) + (recordBytes[offset + 3]);

                /* Add tag and length into the output NDEF */
                outNdef[out_offset] = recordBytes[offset];
                outNdef[out_offset + 1] = recordBytes[offset + 1];

                outNdef[out_offset + 2] = recordBytes[offset + 2];
                outNdef[out_offset + 3] = recordBytes[offset + 3];
            } else {
                throw new FormatException("Buffer overflow for tag/length");
            }

            // Check buffer overflow for value
            if ((offset + 3 + length) >= recordBytes.length) {
                throw new FormatException("Buffer overflow for value");
            }

            switch (tag) {
                case SSID_ID:
                    /* Update the Tag length */
                    outNdef[out_offset + 2] = (byte) (ssid_bytes.length >> 8);
                    outNdef[out_offset + 3] = (byte) (ssid_bytes.length & 0xFF);

                    /* Fit the current SSID into the bytes */
                    for (i = 0; i < ssid_bytes.length; i++) {
                        outNdef[out_offset + 4 + i] = ssid_bytes[i];
                    }
                    out_offset += (ssid_bytes.length - length);
                    break;
                case MAC_ID:
                    /* Add the MAC address into the NDEF bytes */
                    for (i = 0; i < mac_bytes.length; i++) {
                        outNdef[out_offset + 4 + i] = mac_bytes[i];
                    }
                    break;
                default:
                    /* Copy the data as is. */
                    for (i = 0; i < length; i++) {
                        outNdef[out_offset + 4 + i] = recordBytes[offset + 4 + i];
                    }
                    break;
            }
            offset += (3 + length) + 1;
            out_offset += (3 + length) + 1;
        }

        if (recordLength != (offset - 2)) {
            throw new FormatException("Record length did not match with actual payload length");
        }

        /* Add the record length field into output NDEF */
        recordLength = out_offset - 2;
        outNdef[0] = (byte) (recordLength >> 8);
        outNdef[1] = (byte) (recordLength & 0xFF);

        /* Compress the Ndef bytes to recordLength */
        outNdef = Arrays.copyOf(outNdef, recordLength + 2);
        return outNdef;
    }

    /**
     * Executes necessary commands to personalize sample to connection handover state
     * - Write the File Access Policy accordingly
     * - Sets the interface configuration accordingly
     *
     * @param apduChannel APDU specific channel
     * @throws UtilException             Thrown by libraries utils
     * @throws ApduException             Thrown by command set of APDU library
     * @throws IOException               I/O exception has occurred, probably by a ByteArrayOutputStream
     * @throws NdefException             An exception in the NDEF file specific library occurred
     */
    public void execute(@NonNull ApduChannel apduChannel, @NonNull String SSID, @NonNull String macAddress, IfxNdefMessage ifxNdefMessage) throws UtilException, ApduException, FormatException, IOException, NdefException {
        this.apduChannel = apduChannel;
        ArrayList<AbstractRecord> records = new ArrayList<>(ifxNdefMessage.getNdefRecords());

        try {
            // We need Handover select record ("Hs")
            for (int i = 0; i < records.size(); i++) {
                // Get record type
                if (records.get(i).getRecordType().toString().equals("application/vnd.wfa.wsc")) {
                    // Decode the TLV and get SSID and Mac Address
                    IfxNdefRecord ifxNdefRecord = (IfxNdefRecord) records.get(i);
                    byte[] encodedRecord = encodeNDEFRecord(SSID, macAddress, Objects.requireNonNull(ifxNdefRecord.getPayload()));
                    ifxNdefRecord.setPayload(encodedRecord);

                    // Update the record
                    records.set(i, ifxNdefRecord);
                    // Break the loop.
                    break;
                }
            }

            IfxNdefMessage updatedNdefMessage = new IfxNdefMessage(records);

            // Write the update NDEF message into OPTIGA NBT.
            writeWifiConnectionHandoverNdefMessage(updatedNdefMessage);

        } catch (Exception e)
        {
            e.printStackTrace();
            throw new NdefException("Error writing NDEF message");
        }
    }

    /**
     * Reads WiFi Connection Handover information from NDEF file
     *
     * @throws UtilException Thrown by libraries utils
     * @throws ApduException Thrown by command set of APDU library
     */
    private void writeWifiConnectionHandoverNdefMessage(@NonNull IfxNdefMessage ifxNdefMessage) throws NdefException, IOException, ApduException, UtilException {
        WriteNdef writeNdef = new WriteNdef();
        writeNdef.setNdefMessage(ifxNdefMessage.toByteArray());
        writeNdef.execute(this.apduChannel);
    }
}
