import { WebPlugin } from '@capacitor/core';
export class FlickButtonWeb extends WebPlugin {
    async addListener(eventName, listenerFunc) {
        console.log('ADD LISTENER', eventName);
        return Promise.resolve({ remove: () => { } });
    }
    async getButtons() {
        console.log('BUTTONS');
        return Promise.resolve({ buttons: [] });
    }
    isScanning() {
        console.log('SCANNING');
        return Promise.resolve({ scanning: false });
    }
    async scanForButtons() {
        console.log('START SCAN');
        return Promise.resolve({ message: "Scanning" });
    }
    async stopScanning() {
        console.log('STOP SCAN');
        return Promise.resolve({ message: 'Stopped' });
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