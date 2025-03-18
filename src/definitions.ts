import { PluginListenerHandle } from "@capacitor/core";

// JS Flic2 Button Object
export interface FlicButtonDevice {
  buttonId: string,
  name: string,
  state: number
}

export interface FlicButtonPlugin {
  initialize(): Promise<{ value: string }>;
  getButtons(): Promise<{ buttons: FlicButtonDevice[] }>;
  isScanning(): Promise<{ isScanning: boolean }>;
  stopScanning(): Promise<{ isScanning: boolean }>;
  scanForButtons(): Promise<{ message: string }>;
  connectButton(options: { buttonId: string }): Promise<{ message: string }>;
  disconnectButton(options: { buttonId: string }): Promise<{ message: string }>;
  forgetButton(options: { buttonId: string }): Promise<{ message: string }>;
  removeAllButtons(): Promise<{ message: string }>;

  addListener<T = any>(
    eventName: string,
    listenerFunc: (data: T) => void
  ): PluginListenerHandle;
  
}
