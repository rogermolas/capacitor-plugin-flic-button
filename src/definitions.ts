import { PluginListenerHandle } from "@capacitor/core";

export interface FlicButtonPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
  getButtons(): Promise<{ buttons: { buttonId: string; name: string; state: number }[] }>;
  isScanning(): Promise<{ scanning: boolean }>;
  scanForButtons(): Promise<void>;
  connectButton(options: { buttonId: string }): Promise<{ message: string }>;
  disconnectButton(options: { buttonId: string }): Promise<{ message: string }>;
  removeAllButtons(): Promise<{ message: string }>;

  addListener<T = any>(
    eventName: string,
    listenerFunc: (data: T) => void
  ): PluginListenerHandle;
  
}
