import Foundation
import Capacitor
import flic2lib
import CoreBluetooth

@objc(FlicButtonPlugin)
public class FlicButtonPlugin: CAPPlugin, CAPBridgedPlugin, CBCentralManagerDelegate {
    private var centralManager: CBCentralManager?
    public let identifier = "FlicButtonPlugin"
    public let jsName = "FlicButton"
    private var isFlicInitialized = false
    
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "initialize", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getButtons", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "isScanning", returnType: CAPPluginReturnPromise),    
        CAPPluginMethod(name: "stopScanning", returnType: CAPPluginReturnPromise), 
        CAPPluginMethod(name: "scanForButtons", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "connectButton", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "disconnectButton", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "forgetButton", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "removeAllButtons", returnType: CAPPluginReturnPromise),
    ]
    
    @objc func initialize(_ call: CAPPluginCall) {
           if !hasBluetoothPermissions() {
               requestBluetoothPermissions { granted in
                   if granted {
                       self.setupFlic(call)
                   } else {
                       call.reject("Bluetooth permissions denied.")
                   }
               }
           } else {
               setupFlic(call)
           }
       }

       private func setupFlic(_ call: CAPPluginCall) {
           DispatchQueue.main.async {
               if self.isFlicInitialized {
                   call.resolve(["message": "Flic is already initialized."])
                   self.notifyListeners("initialized", data: ["message": "Flic initialization successful."])
                   return
               }
               FLICManager.configure(with: self, buttonDelegate: self, background: true)
               self.isFlicInitialized = true
               call.resolve(["message": "Flic initialization successful."])
               self.notifyListeners("initialized", data: ["message": "Flic initialization successful."])
           }
       }

       private func hasBluetoothPermissions() -> Bool {
           if #available(iOS 13.1, *) {
               return CBManager.authorization == .allowedAlways
           } else {
               return true
           }
       }

       private func requestBluetoothPermissions(completion: @escaping (Bool) -> Void) {
           if #available(iOS 13.1, *) {
               centralManager = CBCentralManager(delegate: self, queue: nil)
               DispatchQueue.main.asyncAfter(deadline: .now() + 1.5) { // Allow time for user prompt
                   completion(CBManager.authorization == .allowedAlways)
               }
           } else {
               completion(true)
           }
       }

       // Required delegate method to trigger permission prompt
       public func centralManagerDidUpdateState(_ central: CBCentralManager) {
           if central.state == .poweredOn {
               print("Bluetooth is ON")
           } else {
               print("Bluetooth is OFF or Unauthorized")
           }
       }

    @objc public func getButtons(_ call: CAPPluginCall) {
        guard let buttons = FLICManager.shared()?.buttons() as? [FLICButton] else {
            let errorMessage = "Flic Manager: No buttons found or unable to cast to [FLICButton]."
            call.reject(errorMessage)
            return
        }

        let buttonList = buttons.map { button in
            return [
                "buttonId": button.bluetoothAddress,
                "name": button.name ?? "",
                "state": button.state.rawValue
            ]
        }
        print("Returning Flic buttons: \(buttonList)")
        DispatchQueue.main.async {
            call.resolve(["buttons": buttonList])
        }
    }

    // MARK: - Start Scanning for Buttons
    @objc public func isScanning(_ call: CAPPluginCall) {
        let scanning = FLICManager.shared()?.isScanning ?? false
        call.resolve(["isScanning": scanning])
    }
    
    @objc public func stopScanning(_ call: CAPPluginCall) {
        FLICManager.shared()?.stopScan()
        call.resolve(["isScanning": false])
    }
    
    @objc func scanForButtons(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            FLICManager.shared()?.scanForButtons(stateChangeHandler: { event in
                var stateMessage = ""
                switch event {
                case .discovered: stateMessage = "A Flic was discovered."
                case .connected: stateMessage = "A Flic is being verified."
                case .verified: stateMessage = "The Flic was verified successfully."
                case .verificationFailed: stateMessage = "The Flic verification failed."
                default: break
                }
                self.notifyListeners("scanStarted", data: ["message": stateMessage])
            }) { (button, error:Error?) in
                
                if (error == nil) {
                    if let button = button {
                        button.triggerMode = .clickAndDoubleClickAndHold
                        print("Flic button found : \(button)")
                        self.notifyListeners("scanSuccess", data: ["buttonId": button.bluetoothAddress])
                        call.resolve(["message" : "<IOS FLIC:> Scan Successful"])
                    }
                } else {
                    self.notifyListeners("scanFailed", data: ["error": error.debugDescription])
                    call.reject("<IOS FLIC:> Scan failed : \(error.debugDescription)")
                }
            }
        }
    }

    // MARK: - Connect to a Button
    @objc func connectButton(_ call: CAPPluginCall) {
        guard let buttonId = call.getString("buttonId"),
              let button = FLICManager.shared()?.buttons().first(
                where: { $0.bluetoothAddress == buttonId }) else {
            call.reject("Button not found")
            return
        }
        button.connect()
        call.resolve(["message": "Button connected..."])
    }

    // MARK: - Disconnect a Button
    @objc func disconnectButton(_ call: CAPPluginCall) {
        guard let buttonId = call.getString("buttonId"),
              let button = FLICManager.shared()?.buttons().first(
                where: { $0.bluetoothAddress == buttonId }) else {
            call.reject("Button not found")
            return
        }
        button.disconnect()
        call.resolve(["message": "Button disconnected"])
    }

    // MARK: - Remove Current Button
    @objc func forgetButton(_ call: CAPPluginCall) {
        guard let buttonId = call.getString("buttonId"),
              let button = FLICManager.shared()?.buttons().first(
                where: { $0.bluetoothAddress == buttonId }) else {
            call.reject("Button not found")
            return
        }
        FLICManager.shared()?.forgetButton(button, completion: { (uuid, error) in
            self.notifyListeners("buttonRemoved", data: ["buttonId": uuid.uuidString])
        })
        call.resolve(["message": "button was removed"])
    }
    
    // MARK: - Remove All Buttons
    @objc func removeAllButtons(_ call: CAPPluginCall) {
        for button in FLICManager.shared()?.buttons() ?? [] {
            FLICManager.shared()?.forgetButton(button, completion: { (uuid, error) in
                self.notifyListeners("allButtonRemoved", data: ["buttonId": uuid.uuidString])
            })
        }
        call.resolve(["message": "All buttons removed"])
    }
}

// MARK: - FLICButtonDelegate
extension FlicButtonPlugin: FLICButtonDelegate {
    public func buttonDidConnect(_ button: FLICButton) {
        notifyListeners("buttonDidConnect", data: [
            "buttonId": button.bluetoothAddress,
            "name": button.name ?? "",
            "state": button.state.rawValue,
        ])
    }

    public func button(_ button: FLICButton, didDisconnectWithError error: (any Error)?) {
        notifyListeners("buttonDisconnected", data: [
            "buttonId": button.bluetoothAddress,
            "name": button.name ?? "",
            "state": button.state.rawValue,
            "error": error?.localizedDescription ?? "Unknown error"
        ])
    }

    public func button(_ button: FLICButton, didFailToConnectWithError error: (any Error)?) {
        notifyListeners("buttonConnectionFailed", data: [
            "buttonId": button.bluetoothAddress,
            "name": button.name ?? "",
            "state": button.state.rawValue,
            "error": error?.localizedDescription ?? "Unknown error"
        ])
    }

    public func buttonIsReady(_ button: FLICButton) {
        notifyListeners("buttonReady", data: [
            "buttonId": button.bluetoothAddress,
            "name": button.name ?? "",
            "state": button.state.rawValue,
        ])
    }

    public func button(_ button: FLICButton, didReceiveButtonClick queued: Bool, age: Int) {
        notifyListeners("buttonClick", data: [
            "buttonId": button.bluetoothAddress,
            "name": button.name ?? "",
            "state": button.state.rawValue,
            "event": "single_click"
        ])
    }
    
    public func button(_ button: FLICButton, didReceiveButtonDoubleClick queued: Bool, age: Int) {
        notifyListeners("buttonClick", data: [
            "buttonId": button.bluetoothAddress,
            "name": button.name ?? "",
            "state": button.state.rawValue,
            "event": "double_click"
        ])
    }
    
    public func button(_ button: FLICButton, didReceiveButtonHold queued: Bool, age: Int) {
        notifyListeners("buttonClick", data: [
            "buttonId": button.bluetoothAddress,
            "name": button.name ?? "",
            "state": button.state.rawValue,
            "event": "hold"
        ])
    }
}

// MARK: - FLICManagerDelegate
extension FlicButtonPlugin:  FLICManagerDelegate{
    public func managerDidRestoreState(_ manager: FLICManager) {
        notifyListeners("managerRestoreState", data: ["total": "\(manager.buttons().count) buttons found"])
    }

    public func manager(_ manager: FLICManager, didUpdate state: FLICManagerState) {
        notifyListeners("managerUpdateState", data: ["state": state.rawValue])
    }
}
