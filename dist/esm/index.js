import { registerPlugin } from '@capacitor/core';
const FlicButton = registerPlugin('FlicButton', {
    web: () => import('./web').then((m) => new m.FlickButtonWeb()),
});
export * from './definitions';
export { FlicButton };
//# sourceMappingURL=index.js.map