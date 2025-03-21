import { PluginListenerHandle, WebPlugin } from '@capacitor/core';
import type { FlicButtonPlugin } from './definitions';

export class FlickButtonWeb extends WebPlugin implements FlicButtonPlugin {
  
  addListener<T = any>(
    eventName: string,
    listenerFunc: (data: T) => void
  ): PluginListenerHandle { // ✅ Now returns PluginListenerHandle instead of Promise
    console.log(`Listening for ${eventName} on web`);

    const handlePromise = super.addListener(eventName, listenerFunc); // ✅ Don't await

    const handle: PluginListenerHandle = {
      remove: async () => {
        const resolvedHandle = await handlePromise;
        await resolvedHandle.remove();
      }
    };
    return handle;
  }

  initialize(): Promise<{ value: string; }> {
      return Promise.resolve({ value: "INITIALIZED"  });
  }

  async getButtons(): Promise<{ buttons: { buttonId: string; name: string; state: number; }[]; }> {
    console.log('BUTTONS');
    return Promise.resolve({ buttons: [] });
  }
  isScanning(): Promise<{ isScanning: boolean; }> {
    console.log('SCANNING');
    return Promise.resolve({ isScanning: true });
  }
  stopScanning(): Promise<{ isScanning: boolean; }> {
    console.log('STOP SCANNING');
    return Promise.resolve({ isScanning: false });
  }
  async scanForButtons(): Promise<{ message: string; }>{
    return Promise.resolve({ message: 'Connected' });
  }
  connectButton(options: { buttonId: string; }): Promise<{ message: string; }> {
    console.log('CONNECT', options);
    return Promise.resolve({ message: 'Connected' });
  }
  disconnectButton(options: { buttonId: string; }): Promise<{ message: string; }> {
    console.log('DISCONNECT', options);
    return Promise.resolve({ message: 'Disconnected' });
  }
  forgetButton(options: { buttonId: string; }): Promise<{ message: string; }> {
    console.log('FORGET', options);
    return Promise.resolve({ message: 'Forgotten' });
  }
  removeAllButtons(): Promise<{ message: string; }> {
    console.log('REMOVE ALL');
    return Promise.resolve({ message: 'Removed' });
  }
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
