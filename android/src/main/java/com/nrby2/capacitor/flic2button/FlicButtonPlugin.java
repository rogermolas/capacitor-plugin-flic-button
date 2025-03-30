package com.nrby2.capacitor.flic2button;
import android.Manifest;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.PermissionState;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;

import java.util.List;

@CapacitorPlugin(
        name = "FlicButton",
        permissions = {
            @Permission(strings = { Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT }, alias = "BLUETOOTH")
        }
)
public class FlicButtonPlugin extends Plugin {
    private static final String TAG = "FlicButtonPlugin";
    private Boolean isScanning = false;
    private static final int PERMISSION_REQUEST_CODE = 1001;
    private boolean isInitialized = false;

    private PluginCall savedCall; // Store call for permission result handling

    @PluginMethod
    public void initialize(PluginCall call) {
        if (isInitialized && Flic2Manager.getInstance() != null) {
            Log.i(TAG, "Flic is already initialized.");
            notifyListeners("initialized", new JSObject().put("message", "Flic initialization successful."));
            call.resolve(new JSObject().put("message", "Flic is already initialized."));
            return;
        }

        if (checkBluetoothPermissions(call)) {
            Log.i(TAG, "Flic permision granter");
            setupFlicButton(call);
        } else {
            savedCall = call;  // Store the call to resume later
        }
    }

    private void setupFlicButton(PluginCall call) {
        if (!isInitialized) {
            Flic2Manager.initAndGetInstance(getContext(), new Handler());
            isInitialized = true;
            Log.i(TAG, "Flic initialization successful.");
            notifyListeners("initialized", new JSObject().put("message", "Flic initialization successful."));
            call.resolve(new JSObject().put("message", "Flic initialization successful."));
        }
    }

    private boolean checkBluetoothPermissions(PluginCall call) {
        boolean needBluetoothPerms = isPermissionDeclared("BLUETOOTH");
        boolean hasBluetoothPerms = !needBluetoothPerms || getPermissionState("BLUETOOTH") == PermissionState.GRANTED;

        boolean needLocationPerms = isPermissionDeclared("LOCATION");
        boolean hasLocationPerms = !needLocationPerms || getPermissionState("LOCATION") == PermissionState.GRANTED;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ requires BLUETOOTH_SCAN and BLUETOOTH_CONNECT
            if (!hasBluetoothPerms) {
                requestPermissionForAlias("BLUETOOTH", call, "bluetoothPermissionsCallback");
                return false;
            }
            return true;
        }

        // For older versions, location is required
        if (!hasLocationPerms) {
            requestPermissionForAlias("LOCATION", call, "bluetoothPermissionsCallback");
            return false;
        }

        return true;
    }

    /**
     * Handles the Bluetooth permission request result.
     */
    @PermissionCallback
    private void bluetoothPermissionsCallback(PluginCall call) {
        if (getPermissionState("BLUETOOTH") != PermissionState.GRANTED) {
            call.reject("Bluetooth permission was denied.");
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S && getPermissionState("LOCATION") != PermissionState.GRANTED) {
            call.reject("Location permission required for Bluetooth on this Android version.");
            return;
        }

        // Continue with Bluetooth initialization
        initialize(call);
    }

    @PluginMethod
    public void isScanning(PluginCall call) {
        JSObject result = new JSObject();
        result.put("isScanning", isScanning);
        call.resolve(result);
    }

    @PluginMethod
    public void stopScanning(PluginCall call) {
        Flic2Manager.getInstance().stopScan();
        JSObject result = new JSObject();
        result.put("isScanning", false);
        call.resolve(result);
    }

    @PluginMethod
    public void getButtons(PluginCall call) {
        List<Flic2Button> buttons = Flic2Manager.getInstance().getButtons();
        JSArray buttonsArray = new JSArray();
        for (Flic2Button button : buttons) {
            JSObject buttonData = generateDeviceObject(button);
            buttonsArray.put(buttonData);
        }
        JSObject response = new JSObject();
        response.put("buttons", buttonsArray);

        call.resolve(response);
        Log.i(TAG, response.toString());
    }

    @PluginMethod
    public void scanForButtons(PluginCall call) {
        notifyListeners("scanStarted", new JSObject().put("message", "Scanning started"));
        isScanning = true;
        Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
            @Override
            public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                notifyListeners("scanSuccess", new JSObject().put("buttonId", button.getBdAddr()));
            }

            @Override
            public void onDiscovered(String bdAddr) {
                notifyListeners("scanSuccess", new JSObject().put("buttonId", bdAddr));
            }

            @Override
            public void onConnected() {

            }

            @Override
            public void onComplete(int result, int subCode, Flic2Button button) {
                isScanning = false;
                if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                    button.disconnectOrAbortPendingConnection();
                    notifyListeners("scanSuccess", new JSObject().put("buttonId", button.getBdAddr()));
                    JSObject r = new JSObject();
                    r.put("message", "<FLIC:> Scan Successful");
                    call.resolve(r);
                } else {
                    Log.e(TAG, String.format("FLIC Error: %s", result));
                    String message = "<FLIC:> Scan failed with error code: " + subCode;
                    notifyListeners("scanFailed", new JSObject().put("message", message));
                    call.reject(message);
                }
            }
        });
    }

    @PluginMethod
    public void connectButton(PluginCall call) {
        String buttonId = call.getString("buttonId");
        if (buttonId == null) {
            call.reject("Button ID is required");
            return;
        }
        Flic2Button button = Flic2Manager.getInstance().getButtonByBdAddr(buttonId);
        if (button == null) {
            call.reject("Button not found");
            return;
        }
        button.connect();
        button.addListener(new CustomFlickButtonListener());
        call.resolve(new JSObject().put("message", "Button connected..."));
    }

    @PluginMethod
    public void disconnectButton(PluginCall call) {
        String buttonId = call.getString("buttonId");
        if (buttonId == null) {
            call.reject("Button ID is required");
            return;
        }
        Flic2Button button = Flic2Manager.getInstance().getButtonByBdAddr(buttonId);
        if (button == null) {
            call.reject("Button not found");
            return;
        }
        button.disconnectOrAbortPendingConnection();
        call.resolve(new JSObject().put("message", "Button disconnected"));
    }

    @PluginMethod
    public void forgetButton(PluginCall call) {
        String buttonId = call.getString("buttonId");
        if (buttonId == null) {
            call.reject("Button ID is required");
            return;
        }
        Flic2Button button = Flic2Manager.getInstance().getButtonByBdAddr(buttonId);
        if (button == null) {
            call.reject("Button not found");
            return;
        }
        Flic2Manager.getInstance().forgetButton(button);
        call.resolve(new JSObject().put("message", "Button was removed"));
    }

    @PluginMethod
    public void removeAllButtons(PluginCall call) {
        List<Flic2Button> buttons = Flic2Manager.getInstance().getButtons();
        for (Flic2Button button : buttons) {
            Flic2Manager.getInstance().forgetButton(button);
            notifyListeners("buttonRemoved", new JSObject().put("buttonId", button.getBdAddr()));
        }
        call.resolve(new JSObject().put("message", "All buttons removed"));
    }

    private JSObject generateDeviceObject(Flic2Button button) {
        JSObject device = new JSObject();
        device.put("buttonId", button.getBdAddr());
        String buttonName = (button.getName() != null && !button.getName().isEmpty()) ? button.getName() : button.getSerialNumber();
        device.put("name", buttonName);
        device.put("state", button.getConnectionState());
        return  device;
    }

    private class CustomFlickButtonListener extends Flic2ButtonListener {
        @Override
        public void onFailure(Flic2Button button, int errorCode, int subCode) {
            super.onFailure(button, errorCode, subCode);
            JSObject event = generateDeviceObject(button);
            event.put("error", errorCode);
            notifyListeners("buttonConnectionFailed", event);
        }

        @Override
        public void onReady(Flic2Button button, long timestamp) {
            super.onReady(button, timestamp);
            JSObject event = generateDeviceObject(button);
            notifyListeners("buttonReady", event);
            notifyListeners("buttonDidConnect", event);
        }

        @Override
        public void onUnpaired(Flic2Button button) {
            super.onUnpaired(button);
            JSObject event = generateDeviceObject(button);
            notifyListeners("buttonRemoved", event);
        }

        @Override
        public void onButtonClickOrHold(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isClick, boolean isHold) {
            super.onButtonClickOrHold(button, wasQueued, lastQueued, timestamp, isClick, isHold);
            JSObject event = generateDeviceObject(button);
            event.put("event", isHold ? "hold" : "isClick");
            notifyListeners("buttonClick", event);
        }

        @Override
        public void onButtonSingleOrDoubleClick(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isSingleClick, boolean isDoubleClick) {
            super.onButtonSingleOrDoubleClick(button, wasQueued, lastQueued, timestamp, isSingleClick, isDoubleClick);
            JSObject event = generateDeviceObject(button);
            event.put("event", isSingleClick ? "single_click" : "double_click");
            notifyListeners("buttonClick", event);
        }

        @Override
        public void onConnect(Flic2Button button) {
            super.onConnect(button);
            Log.i(TAG, "Toggle On onConnect");
            JSObject event = generateDeviceObject(button);
            notifyListeners("buttonDidConnect", event);
        }

        @Override
        public void onDisconnect(Flic2Button button) {
            super.onDisconnect(button);
            Log.i(TAG, "Toggle On onDisconnect");
            JSObject event = generateDeviceObject(button);
            notifyListeners("buttonDisconnected", event);
        }
    }
}