// SPDX-FileCopyrightText: Copyright (c) 2024-2026 Infineon Technologies AG
// SPDX-License-Identifier: MIT

package com.infineon.css.nbt_wifi_p2p;

import static com.infineon.css.nbt_wifi_p2p.utils.generic.InterfaceChannel.InitializeChannel;

import java.io.OutputStream;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.infineon.css.nbt_wifi_p2p.utils.wifi_direct.WiFiConnectionHandoverReader;
import com.infineon.css.nbt_wifi_p2p.utils.wifi_direct.WifiP2pGlobalConfig;
import com.infineon.hsw.apdu.ApduChannel;
import com.infineon.hsw.channel.IChannel;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;


/**
 * Detects tag and configures it to the ConnectionHandover use case
 */
public class WiFiConnectionHandoverActivity extends AppCompatActivity {

    /**
     * Instruction text to the user
     */
    TextView instructionText;

    /**
     * Result text to the user
     */
    TextView resultText;

    /**
     * Connection handover icon for GUI
     */
    ImageView logoConnectionHandover;

    /**
     * Button to return to the main activity
     */
    ImageButton buttonReturn;

    private static final String TAG = "WiFiConnectionHandoverActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wificonnectionhandover_read);

        //GUI initialization
        logoConnectionHandover = null;
        instructionText = findViewById(R.id.textView_cardTap);
        instructionText.setText(R.string.string_wifi_tap);
        buttonReturn = findViewById(R.id.button_return);

        buttonReturn.setOnClickListener(v -> returnToMain());
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Setting intent to recognized NFC Type A tags with NFC interface
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
        IntentFilter[] intentFiltersArray = new IntentFilter[]{ndef,};
        String[][] techListsArray = new String[][]{new String[]{NfcA.class.getName()}};
        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }

    /**
     * Intent is called when a NFC tag with a NDEF message is detected. Communication channel is
     * opened and further use case steps are triggered
     *
     * @param intent NFC tag intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {

            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            IsoDep com = IsoDep.get(tag);
            IChannel channel = InitializeChannel(com);
            // Initialize APDU channel based on the NFC channel
            ApduChannel apduChannel = new ApduChannel(channel);

            handleUsecase(apduChannel);
        }
    }


    /**
     * NFC communication channel is used to:
     * 1. Open APDU channel to tag
     * 2. Call and execute the corresponding state class to configure the sample for the use case
     * 3. Close APDU channel to tag
     *
     * @param apduChannel NFC communication channel
     */
    private void handleUsecase(ApduChannel apduChannel) {

        try {
            apduChannel.connect();

            WiFiConnectionHandoverReader my_wifiConnectionHandoverReader = new WiFiConnectionHandoverReader();
            my_wifiConnectionHandoverReader.execute(apduChannel);

            Intent wifiIntent = new Intent(this, WiFiDirectActivity.class);
            startActivityForResult(wifiIntent, 1);

            // APDU Disconnection will happen in WifiDirectActivity.onDeviceInfoAvailable
        } catch (Exception e) {
            runOnUiThread(() -> instructionText.setText(R.string.string_error));
        }
    }

    /**
     * Transfers a file to the server
     */
    private void fileTransfer() {
        // Create another activity to send an image to the server (Group owner)
        WifiP2pGlobalConfig rpiConfig = WifiP2pGlobalConfig.getInstance();
        InetAddress host = rpiConfig.getInetAddresses();
        Socket socket = new Socket();
        String sendText = "Sending Some text\n";
        byte[] buf = sendText.getBytes();
        int port = 5005;

        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(host, port)), 5000);
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(buf, 0, buf.length);
            Log.d(TAG, "Successfully sent data to server.");
            outputStream.close();
        }  catch (FileNotFoundException e) {
            Log.d(TAG, "fileTransfer: FileNotFoundException");
        } catch (IOException e) {
            Log.d(TAG, "fileTransfer: IOException");
        }

        /*
          Clean up any open sockets when done
          transferring or if an exception occurred.
         */
        finally {
            if (socket.isConnected()) {
                try {
                    socket.close();
                } catch (IOException e) {
                    //catch logic
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        setContentView(R.layout.activity_wificonnectionhandover_result);

        //GUI initialization
        logoConnectionHandover = findViewById(R.id.image_logo);
        logoConnectionHandover.setImageResource(R.drawable.icon_wifi_lightgrey);
        instructionText = findViewById(R.id.textView_cardTap);
        instructionText.setText(R.string.string_wifi_tap);
        resultText = findViewById(R.id.textView_resultText);
        resultText.setText(R.string.string_wifi_tap);

        buttonReturn = findViewById(R.id.button_return);

        buttonReturn.setOnClickListener(v -> returnToMain());


        boolean is_success = false;
        if (resultCode == RESULT_OK && data != null && Objects.equals(data.getStringExtra("result"), "WIFI_P2P_SUCCESS"))
        {
            Log.d(TAG, "onActivityResult: WIFI_P2P_SUCCESS");

            // Transfer file in new thread
            Thread fileTransferThread =  new Thread(this::fileTransfer);

            fileTransferThread.start();

            // Wait for the thread to finish
            try {
                fileTransferThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            is_success = true;
        } else {
            Log.d(TAG, "onActivityResult: WIFI_P2P_FAILURE");
        }
        setGui(is_success);
    }

    /**
     * Set the GUI of the App
     */
    public void setGui(Boolean success) {
        runOnUiThread(() -> {
            if(success) {
                logoConnectionHandover.setImageResource(R.drawable.icon_wifi_green);
                instructionText.setText(R.string.string_wifi_success);
                resultText.setText(R.string.string_wifi_res_success);
            }
            else
            {
                logoConnectionHandover.setImageResource(R.drawable.icon_wifi_lightgrey);
                instructionText.setText(R.string.string_wifi_failure);
                resultText.setText(R.string.string_wifi_res_failure);
            }
            buttonReturn.setImageResource(R.drawable.icon_return_grey);
            buttonReturn.setEnabled(true);
        });

        // Returns to main activity after pressing the button
    }

    /**
     * Returns to main activity
     */
    private void returnToMain() {
        Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(activityIntent);
        finish();
    }
}
