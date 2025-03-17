package com.nrby2.capacitor.flic2button;
import android.Manifest;
import android.os.Handler;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;

import io.flic.flic2libandroid.Flic2Button;
import io.flic.flic2libandroid.Flic2ButtonListener;
import io.flic.flic2libandroid.Flic2Manager;
import io.flic.flic2libandroid.Flic2ScanCallback;

import java.util.List;

@CapacitorPlugin(
        name = "FlicButton",
        permissions = {
                @Permission(strings = { Manifest.permission.BLUETOOTH }, alias = "BLUETOOTH"),
                @Permission(strings = { Manifest.permission.BLUETOOTH_SCAN }, alias = "BLUETOOTH_SCAN"),
                @Permission(strings = { Manifest.permission.ACCESS_FINE_LOCATION }, alias = "ACCESS_FINE_LOCATION")
        }
)
public class FlicButtonPlugin extends Plugin {
    private static final String TAG = "FlicButtonPlugin";
    private Boolean isScanning = false;
    
    @Override
    public void load() {
        super.load();
        Flic2Manager.initAndGetInstance(getContext(), new Handler());
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
        result.put("stopScan", true);
        call.resolve(result);
    }

    @PluginMethod
    public void getButtons(PluginCall call) {
        List<Flic2Button> buttons = Flic2Manager.getInstance().getButtons();
        JSObject response = new JSObject();
        for (Flic2Button button : buttons) {
            JSObject buttonData = new JSObject();
            buttonData.put("buttonId", button.getBdAddr());
            buttonData.put("name", button.getName());
            buttonData.put("state", button.getConnectionState());
            response.put(button.getBdAddr(), buttonData);
        }
        call.resolve(response);
    }

    @PluginMethod
    public void scanForButtons(PluginCall call) {
        Flic2Manager.getInstance().startScan(new Flic2ScanCallback() {
            @Override
            public void onDiscoveredAlreadyPairedButton(Flic2Button button) {
                isScanning = true;
            }

            @Override
            public void onDiscovered(String bdAddr) {
                isScanning = true;
                notifyListeners("scanStarted", new JSObject().put("message", "A Flic was discovered: " + bdAddr));
            }

            @Override
            public void onConnected() {}

            @Override
            public void onComplete(int result, int subCode, Flic2Button button) {
                isScanning = false;
                if (result == Flic2ScanCallback.RESULT_SUCCESS) {
                    notifyListeners("scanSuccess", new JSObject().put("buttonId", button.getBdAddr()));
                    JSObject r = new JSObject();
                    r.put("message", "<IOS FLIC:> Scan Successful");
                    call.resolve(r);
                } else {
                    notifyListeners("scanFailed", new JSObject().put("error", "Scan failed."));
                    call.reject("<IOS FLIC:> Scan failed");
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
        notifyListeners("buttonConnected", new JSObject().put("buttonId", buttonId));
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
        notifyListeners("buttonDisconnected", new JSObject().put("buttonId", buttonId));
        call.resolve(new JSObject().put("message", "Button disconnected"));
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

    private class CustomFlickButtonListener extends Flic2ButtonListener {
        @Override
        public void onFailure(Flic2Button button, int errorCode, int subCode) {
            super.onFailure(button, errorCode, subCode);
            JSObject event = new JSObject();
            event.put("buttonId", button.getBdAddr());
            event.put("name", button.getName());
            event.put("state", button.getConnectionState());
            event.put("error", errorCode);
            notifyListeners("buttonConnectionFailed", event);
        }

        @Override
        public void onReady(Flic2Button button, long timestamp) {
            super.onReady(button, timestamp);
            JSObject event = new JSObject();
            event.put("buttonId", button.getBdAddr());
            event.put("name", button.getName());
            event.put("state", button.getConnectionState());
            notifyListeners("buttonReady", event);
        }

        @Override
        public void onUnpaired(Flic2Button button) {
            super.onUnpaired(button);
            JSObject event = new JSObject();
            event.put("buttonId", button.getBdAddr());
            event.put("name", button.getName());
            event.put("state", button.getConnectionState());
            notifyListeners("buttonConnectionFailed", event);
        }

        @Override
        public void onButtonClickOrHold(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isClick, boolean isHold) {
            super.onButtonClickOrHold(button, wasQueued, lastQueued, timestamp, isClick, isHold);
            JSObject event = new JSObject();
            event.put("buttonId", button.getBdAddr());
            event.put("name", button.getName());
            event.put("event", isHold ? "hold" : "isClick");
            notifyListeners("buttonClick", event);
        }

        @Override
        public void onButtonSingleOrDoubleClick(Flic2Button button, boolean wasQueued, boolean lastQueued, long timestamp, boolean isSingleClick, boolean isDoubleClick) {
            super.onButtonSingleOrDoubleClick(button, wasQueued, lastQueued, timestamp, isSingleClick, isDoubleClick);
            JSObject event = new JSObject();
            event.put("buttonId", button.getBdAddr());
            event.put("name", button.getName());
            event.put("event", isSingleClick ? "single_click" : "double_click");
            notifyListeners("buttonClick", event);
        }

        @Override
        public void onConnect(Flic2Button button) {
            JSObject event = new JSObject();
            event.put("buttonId", button.getBdAddr());
            event.put("name", button.getName());
            event.put("status", "connected");
            notifyListeners("buttonConnected", event);
        }

        @Override
        public void onDisconnect(Flic2Button button) {
            JSObject event = new JSObject();
            event.put("buttonId", button.getBdAddr());
            event.put("name", button.getName());
            event.put("status", "disconnected");
            notifyListeners("buttonDisconnected", event);
        }
    }
}