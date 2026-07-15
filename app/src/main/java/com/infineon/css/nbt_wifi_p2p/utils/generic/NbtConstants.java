// SPDX-FileCopyrightText: Copyright (c) 2024-2025 Infineon Technologies AG
// SPDX-License-Identifier: MIT

package com.infineon.css.nbt_wifi_p2p.utils.generic;

import com.infineon.hsw.apdu.nbt.model.AccessCondition;
import com.infineon.hsw.apdu.nbt.model.AccessConditionType;
import com.infineon.hsw.apdu.nbt.model.FileAccessPolicy;
import com.infineon.hsw.apdu.nbt.model.FileAccessPolicyException;

/**
 * Stores basic constants for the NBT product line
 */
@SuppressWarnings("unused")
public class NbtConstants {

    /**
     * Global variable for CC file ID
     */
    static public final short NBT_ID_CC_FILE = (short) 57603;

    /**
     * Global variable for NDEF file ID
     */
    static public final short NBT_ID_NDEF_FILE = (short) 57604;

    /**
     * Global variable for FAP file ID
     */
    static public final short NBT_ID_FAP_FILE = (short) 57775;

    /**
     * Global variable for proprietary file1 ID
     */
    static public final short NBT_ID_PP1_FILE = (short) 57761;

    /**
     * Global variable for proprietary file2 ID
     */
    static public final short NBT_ID_PP2_FILE = (short) 57762;

    /**
     * Global variable for proprietary file3 ID
     */
    static public final short NBT_ID_PP3_FILE = (short) 57763;

    /**
     * Global variable for proprietary file4 ID
     */
    static public final short NBT_ID_PP4_FILE = (short) 57764;

    /**
     * Global variable for BMK key file ID
     */
    static public final short NBT_ID_BMK = (short) 40961;

    /**
     * Global variable for BSK key file ID
     */
    static public final short NBT_ID_BSK = (short) 40962;

    /**
     * Global variable for password file ID
     */
    static public final short NBT_ID_PW_DATA = (short) 40963;

    /**
     * Global variable for the default offset
     */
    static public final short DEFAULT_OFFSET = (short) 0;

    /**
     * Global variable for the CC file offset
     */
    static public final short CC_OFFSET = (short) 15;

    /**
     * Global variable for the default apdu frame size
     */
    static public final short APDU_FRAME_SIZE = (short) 255;

    /**
     * Global variable for the default fap file
     */
    static public final byte[] DEFAULT_FAP = new byte[]{(byte) 0xE1, (byte) 0x03, (byte) 0x40, (byte) 0x00,
            (byte) 0x40, (byte) 0x00, (byte) 0xE1, (byte) 0x04, (byte) 0x40, (byte) 0x40, (byte) 0x40,
            (byte) 0x40, (byte) 0xE1, (byte) 0xA1, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40,
            (byte) 0xE1, (byte) 0xA2, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0xE1,
            (byte) 0xA3, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0xE1, (byte) 0xA4,
            (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0x40, (byte) 0xE1, (byte) 0xAF, (byte) 0x40,
            (byte) 0x40, (byte) 0x40, (byte) 0x40};

    /**
     * Global variable for access condition to block access
     */
    static public AccessCondition BLOCK;

    /**
     * Global variable for access condition to allow access
     */
    static public AccessCondition ALLOW;

    // Initializing access conditions
    static {
        try {
            BLOCK = new AccessCondition(AccessConditionType.NEVER);
            ALLOW = new AccessCondition(AccessConditionType.ALWAYS);
        } catch (FileAccessPolicyException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initializing CC file specific file access policy
     */
    public static FileAccessPolicy DEFAULT_FAP_CC = new FileAccessPolicy(NBT_ID_CC_FILE, ALLOW, BLOCK, ALLOW, BLOCK);

    /**
     * Initializing NDEF file specific file access policy
     */
    public static FileAccessPolicy DEFAULT_FAP_NDEF = new FileAccessPolicy(NBT_ID_NDEF_FILE, ALLOW, ALLOW, ALLOW, ALLOW);

    /**
     * Initializing FAP file specific file access policy
     */
    public static FileAccessPolicy DEFAULT_FAP_FAP = new FileAccessPolicy(NBT_ID_FAP_FILE, ALLOW, ALLOW, ALLOW, ALLOW);

    /**
     * Initializing proprietary file1 specific file access policy
     */
    public static FileAccessPolicy DEFAULT_FAP_FILE1 = new FileAccessPolicy(NBT_ID_PP1_FILE, ALLOW, ALLOW, ALLOW, ALLOW);

    /**
     * Initializing proprietary file2 specific file access policy
     */
    public static FileAccessPolicy DEFAULT_FAP_FILE2 = new FileAccessPolicy(NBT_ID_PP2_FILE, ALLOW, ALLOW, ALLOW, ALLOW);

    /**
     * Initializing proprietary file3 specific file access policy
     */
    public static FileAccessPolicy DEFAULT_FAP_FILE3 = new FileAccessPolicy(NBT_ID_PP3_FILE, ALLOW, ALLOW, ALLOW, ALLOW);

    /**
     * Initializing proprietary file4 specific file access policy
     */
    public static FileAccessPolicy DEFAULT_FAP_FILE4 = new FileAccessPolicy(NBT_ID_PP4_FILE, ALLOW, ALLOW, ALLOW, ALLOW);

    /**
     * Initializing global interface tag
     */
    static public short NBT_TAG_COMM_IF_ENABLE = (short) -16288;

    /**
     * Initializing global GPIO tag
     */
    static public short NBT_TAG_GPIO_FUNCTION = (short) -16336;

    /**
     * Initializing GPIO configurations with no IRQ
     */
    static public byte NBT_GPIO_NO_IRQ = 0x01;

    /**
     * Initializing GPIO configurations with RF IRQ
     */
    static public byte NBT_GPIO_RF_IRQ = 0x02;

    /**
     * Initializing GPIO configurations with I2C IRQ
     */
    static public byte NBT_GPIO_I2C_IRQ = 0x03;

    /**
     * Initializing GPIO configurations with PT IRQ
     */
    static public byte NBT_GPIO_PT_RF_IRQ = 0x04;

    /**
     * Initializing interface configurations to allow CL and CB
     */
    static public byte NBT_INT_NFC_I2C = 0x11;

    /**
     * Initializing interface configurations to allow CB only
     */
    static public byte NBT_INT_I2C = 0x01;

    /**
     * Initializing interface configurations to allow CL only
     */
    static public byte NBT_INT_NFC = 0x10;
}
