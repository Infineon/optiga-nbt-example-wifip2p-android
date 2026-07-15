// SPDX-FileCopyrightText: Copyright (c) 2024-2026 Infineon Technologies AG
// SPDX-License-Identifier: MIT

package com.infineon.css.nbt_wifi_p2p;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

/**
 * Main activity of the app which is intended to configure a NBT sample to the desired use case
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Button to open Wi-Fi pass through activity
     */
    ImageButton buttonWifiConnectionHandover;

    private ActivityResultLauncher<String[]> permissionsLauncher;
    private static final int P2P_PERMISSIONS_REQUEST_CODE = 1001;
    private boolean permissionsRequestRetryAttempted = false;
    private static final String TAG = "MainActivity";
    private static final String[] BASE_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
    private static final String[] TIRAMISU_PERMISSIONS = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.NEARBY_WIFI_DEVICES};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // WiFi button connection handover
        buttonWifiConnectionHandover = findViewById(R.id.button_wifiConnectionHandover);
        if (buttonWifiConnectionHandover != null) {
            // Disabled until permissions granted
            buttonWifiConnectionHandover.setEnabled(false);
            buttonWifiConnectionHandover.setImageResource(R.drawable.icon_wifi_green);
        }

        registerPermissionLauncher();
        maybeRequestPermissions();
    }

    private void registerPermissionLauncher() {
        permissionsLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            boolean allGranted = true;
            for (Boolean granted : result.values()) {
                if (granted == null || !granted) { allGranted = false; break; }
            }
            if (allGranted) {
                enableWifiButton();
            } else {
                if (buttonWifiConnectionHandover != null) {
                    buttonWifiConnectionHandover.setEnabled(false);
                    if (permissionsRequestRetryAttempted) {
                        Toast.makeText(this, "Permissions denied. Wi-Fi P2P handover disabled.", Toast.LENGTH_LONG).show();
                        Log.w(TAG, "Permissions denied after retry. Wi-Fi P2P handover disabled.");
                    } else {
                        permissionsRequestRetryAttempted = true;
                        new AlertDialog.Builder(this)
                            .setTitle("Permissions Required")
                            .setMessage("Location/Wi-Fi permissions are needed for P2P handover.")
                            .setPositiveButton("OK", (dialog, which) -> permissionsLauncher.launch(requiredPermissions()))
                            .setNegativeButton("Cancel", null)
                            .show();
                    }
                }
            }
        });
    }

    private String[] requiredPermissions() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? TIRAMISU_PERMISSIONS : BASE_PERMISSIONS;
    }

    private boolean hasAllPermissions() {
        for (String p : requiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) return false;
        }
        return true;
    }

    private void maybeRequestPermissions() {
        if (hasAllPermissions()) {
            enableWifiButton();
            return;
        }

        boolean firstRun = getSharedPreferences("prefs", MODE_PRIVATE).getBoolean("first_run", true);
        if (firstRun) {
            // Mark first run as completed
            getSharedPreferences("prefs", MODE_PRIVATE).edit().putBoolean("first_run", false).apply();
            permissionsLauncher.launch(requiredPermissions());
            return;
        }
        else {
            boolean permanentDisable = false;
            for (String p : requiredPermissions()) {
                if (shouldShowRequestPermissionRationale(p) == false) { permanentDisable = true; break; }
            }

            if (!permanentDisable) {
                permissionsLauncher.launch(requiredPermissions());
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Enable Permissions")
                        .setMessage("Enable Location/Wi-Fi permissions them in the app settings.")
                        .setPositiveButton("OK", null)
                        .show();
            }
        }
    }

    private void enableWifiButton() {
        if (buttonWifiConnectionHandover == null) return;
        buttonWifiConnectionHandover.setEnabled(true);
        if (buttonWifiConnectionHandover.hasOnClickListeners()) return; // Avoid re-adding
        buttonWifiConnectionHandover.setOnClickListener(v -> {
            Intent activityIntent = new Intent(getApplicationContext(), WiFiConnectionHandoverActivity.class);
            startActivity(activityIntent);
            finish();
        });
    }

    // Removed deprecated callback usage in favor of Activity Result API
}
