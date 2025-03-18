import { ListenerCallback, PluginListenerHandle, WebPlugin } from '@capacitor/core';
import type { FlicButtonPlugin } from './definitions';
export declare class FlickButtonWeb extends WebPlugin implements FlicButtonPlugin {
    addListener(eventName: string, listenerFunc: ListenerCallback): Promise<PluginListenerHandle>;
    getButtons(): Promise<{
        buttons: {
            buttonId: string;
            name: string;
            state: number;
        }[];
    }>;
    isScanning(): Promise<{
        isScanning: boolean;
    }>;
    stopScanning(): Promise<{
        isScanning: boolean;
    }>;
    scanForButtons(): Promise<{
        message: string;
    }>;
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
    forgetButton(options: {
        buttonId: string;
    }): Promise<{
        message: string;
    }>;
    removeAllButtons(): Promise<{
        message: string;
    }>;
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
