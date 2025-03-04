import { registerPlugin } from '@capacitor/core';

import type { FlicButtonPlugin } from './definitions';

const FlicButton = registerPlugin<FlicButtonPlugin>('FlicButton', {
  web: () => import('./web').then((m) => new m.FlickButtonWeb()),
});

export * from './definitions';
export { FlicButton };
