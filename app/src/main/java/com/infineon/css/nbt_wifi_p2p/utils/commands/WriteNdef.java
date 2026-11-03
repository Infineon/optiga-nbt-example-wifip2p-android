// SPDX-FileCopyrightText: Copyright (c) 2024-2026 Infineon Technologies AG
// SPDX-License-Identifier: MIT

package com.infineon.css.nbt_wifi_p2p.utils.commands;

import androidx.annotation.NonNull;

import com.infineon.hsw.apdu.ApduChannel;
import com.infineon.hsw.apdu.ApduException;
import com.infineon.hsw.apdu.nbt.NbtApduResponse;
import com.infineon.hsw.apdu.nbt.NbtCommandSet;
import com.infineon.hsw.utils.UtilException;

/**
 * The WriteNdef class generates and sends the commands to write a valid ndef file onto a NBT sample.
 * It needs to be provided with ApduChannel and the ndef message as byte array.
 */
public class WriteNdef implements ICommandFlow {

    /**
     * The empty ndef file to be written
     */
    private byte[] ndef_message;

    /**
     * Selects the ndef file and write the ndef message into the file, it can only be called
     * successfully after the ndef message was set using setNdefMessage()
     *
     * @param apduChannel APDU channel to the NFC Interface
     * @throws UtilException Thrown by libraries utils
     * @throws ApduException Thrown by command set of APDU library
     */
    public void execute(@NonNull ApduChannel apduChannel) throws UtilException, ApduException {

        if (ndef_message != null) {
            NbtCommandSet commandSet = new NbtCommandSet(apduChannel, 0);
            NbtApduResponse apduResponse = commandSet.selectApplication();
            apduResponse.checkOK();

            apduResponse = commandSet.updateNdefMessage(ndef_message);
            apduResponse.checkOK();
        } else {
            throw new UtilException("Ndef message must not be empty!");
        }
    }

    /**
     * Setter function to set ndef message, needs to be set before execute() is called
     */
    public void setNdefMessage(@NonNull byte[] ndefMessage) {
        ndef_message = ndefMessage;
    }
}
