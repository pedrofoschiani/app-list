import { registerPlugin } from '@capacitor/core';
import type { BlomePlugin } from './definitions';

const Blome = registerPlugin<BlomePlugin>('BlockApps', {
  web: () => import('./web').then(m => new m.BlomeWeb()),
});

export * from './definitions';
export { Blome };