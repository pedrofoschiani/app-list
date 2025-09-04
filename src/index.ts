import { registerPlugin } from '@capacitor/core';
import type { AppListPlugin } from './definitions';

const AppList = registerPlugin<AppListPlugin>('AppList');

export * from './definitions';
export { AppList };
