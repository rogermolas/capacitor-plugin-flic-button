import { WebPlugin } from '@capacitor/core';
import type { FlicButtonPlugin } from './definitions';
export declare class FlickButtonWeb extends WebPlugin implements FlicButtonPlugin {
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
    echo(options: {
        value: string;
    }): Promise<{
        value: string;
    }>;
}
