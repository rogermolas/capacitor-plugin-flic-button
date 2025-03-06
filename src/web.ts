import { ListenerCallback, PluginListenerHandle, WebPlugin } from '@capacitor/core';

import type { FlicButtonPlugin } from './definitions';

export class FlickButtonWeb extends WebPlugin implements FlicButtonPlugin {

  async addListener(eventName: string, listenerFunc: ListenerCallback): Promise<PluginListenerHandle> {
    console.log('ADD LISTENER', eventName);
    return Promise.resolve({ remove: () => {} });
  }
  async getButtons(): Promise<{ buttons: { buttonId: string; name: string; state: number; }[]; }> {
    console.log('BUTTONS');
    return Promise.resolve({ buttons: [] });
  }
  isScanning(): Promise<{ scanning: boolean; }> {
    console.log('SCANNING');
    return Promise.resolve({ scanning: false });
  }
  async scanForButtons(): Promise<void> {
    console.log('SCAN');
  }
  connectButton(options: { buttonId: string; }): Promise<{ message: string; }> {
    console.log('CONNECT', options);
    return Promise.resolve({ message: 'Connected' });
  }
  disconnectButton(options: { buttonId: string; }): Promise<{ message: string; }> {
    console.log('DISCONNECT', options);
    return Promise.resolve({ message: 'Disconnected' });
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
