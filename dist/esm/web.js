import { WebPlugin } from '@capacitor/core';
export class FlickButtonWeb extends WebPlugin {
    addListener(eventName, listenerFunc) {
        console.log(`Listening for ${eventName} on web`);
        const handlePromise = super.addListener(eventName, listenerFunc); // ✅ Don't await
        const handle = {
            remove: async () => {
                const resolvedHandle = await handlePromise;
                await resolvedHandle.remove();
            }
        };
        return handle;
    }
    initialize() {
        return Promise.resolve({ value: "INITIALIZED" });
    }
    async getButtons() {
        console.log('BUTTONS');
        return Promise.resolve({ buttons: [] });
    }
    isScanning() {
        console.log('SCANNING');
        return Promise.resolve({ isScanning: true });
    }
    stopScanning() {
        console.log('STOP SCANNING');
        return Promise.resolve({ isScanning: false });
    }
    async scanForButtons() {
        return Promise.resolve({ message: 'Connected' });
    }
    connectButton(options) {
        console.log('CONNECT', options);
        return Promise.resolve({ message: 'Connected' });
    }
    disconnectButton(options) {
        console.log('DISCONNECT', options);
        return Promise.resolve({ message: 'Disconnected' });
    }
    forgetButton(options) {
        console.log('FORGET', options);
        return Promise.resolve({ message: 'Forgotten' });
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