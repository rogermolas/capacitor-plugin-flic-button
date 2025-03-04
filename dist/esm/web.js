import { WebPlugin } from '@capacitor/core';
export class FlickButtonWeb extends WebPlugin {
    async scanForButtons() {
        console.log('SCAN');
    }
    connectButton(options) {
        console.log('CONNECT', options);
        return Promise.resolve({ message: 'Connected' });
    }
    disconnectButton(options) {
        console.log('DISCONNECT', options);
        return Promise.resolve({ message: 'Disconnected' });
    }
    removeAllButtons() {
        console.log('REMOVE ALL');
        return Promise.resolve({ message: 'Removed' });
    }
    async echo(options) {
        console.log('ECHO', options);
        return options;
    }
}
//# sourceMappingURL=web.js.map