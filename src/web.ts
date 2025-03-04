import { WebPlugin } from '@capacitor/core';

import type { FlicButtonPlugin } from './definitions';

export class FlickButtonWeb extends WebPlugin implements FlicButtonPlugin {

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
