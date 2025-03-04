import { PluginListenerHandle } from "@capacitor/core";
export interface FlicButtonPlugin {
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
    scanForButtons(): Promise<void>;
    connectButton(options: {
        buttonId: string;
    }): Promise<{
        message: string;
    }>;
    disconnectButton(options: {
        buttonId: string;
    }): Promise<{
        message: string;
    }>;
    removeAllButtons(): Promise<{
        message: string;
    }>;
    addListener<T = any>(eventName: string, listenerFunc: (data: T) => void): PluginListenerHandle;
}
