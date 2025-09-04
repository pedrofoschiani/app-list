import { registerPlugin } from '@capacitor/core';

import type { AppListPlugin } from './definitions';

const AppList = registerPlugin<AppListPlugin>('AppList', {
  web: () => import('./web').then((m) => new m.AppListWeb()),
});

export * from './definitions';
export { AppList };
