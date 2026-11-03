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
public class ReadNdef implements ICommandFlow {

    /**
     * The empty ndef file to be read
     */
    private byte[] ndef_message;

    /**
     * Selects the ndef file and read the ndef file.
     *
     * @param apduChannel APDU channel to the NFC Interface
     * @throws UtilException Thrown by libraries utils
     * @throws ApduException Thrown by command set of APDU library
     */
    public void execute(@NonNull ApduChannel apduChannel) throws UtilException, ApduException {

        NbtCommandSet commandSet = new NbtCommandSet(apduChannel, 0);
        NbtApduResponse apduResponse = commandSet.selectApplication();
        apduResponse.checkOK();

        apduResponse = commandSet.readNdefMessage();
        apduResponse.checkOK();

        this.setNdefMessage(apduResponse.getData());
    }

    /**
     * Getter function to get ndef message, need to call execute() first.
     *
     * @throws UtilException Thrown by libraries utils
     */
    public byte[] getNdefMessage() throws UtilException {
        if (ndef_message != null) {
            return ndef_message;
        } else {
            throw new UtilException("Ndef message is empty. Make sure ReadNdef.execute() is called!");
        }
    }


    /**
     * Setter function to set ndef message.
     *
     * @param ndef_message The ndef message to be set
     *
     * @throws UtilException Thrown by libraries utils
     */
    public void setNdefMessage(byte[] ndef_message) throws UtilException {
        if (ndef_message != null) {
            this.ndef_message = ndef_message;
        } else {
            throw new UtilException("Provided Ndef message is empty.");
        }
    }
}
