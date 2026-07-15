// SPDX-FileCopyrightText: Copyright (c) 2024-2025 Infineon Technologies AG
// SPDX-License-Identifier: MIT

package com.infineon.css.nbt_wifi_p2p.utils.commands;

import androidx.annotation.NonNull;

import com.infineon.hsw.apdu.ApduChannel;
import com.infineon.hsw.apdu.ApduException;
import com.infineon.hsw.apdu.nbt.model.FileAccessPolicyException;
import com.infineon.hsw.utils.UtilException;

import java.io.IOException;

/**
 * Interface to multiple command flows essential for the NBT use cases
 */
public interface ICommandFlow {

    /**
     * Execute will run a cascade of commands necessary to fulfill a specific command flow and interaction
     * with the NBT sample
     *
     * @param apduChannel Channel for APDU communication
     * @throws UtilException             Thrown by libraries utils
     * @throws ApduException             Thrown by command set of APDU library
     * @throws FileAccessPolicyException Thrown by APDU library in case of FAP error
     * @throws IOException               Signals that an I/O exception of some sort has occurred
     */
    void execute(@NonNull ApduChannel apduChannel) throws UtilException, ApduException, FileAccessPolicyException, IOException;

}
