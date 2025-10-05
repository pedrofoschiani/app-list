import { registerPlugin } from '@capacitor/core';
import type { AppListPlugin } from './definitions';

const AppList = registerPlugin<AppListPlugin>('AppList');

export { AppListPlugin, AppInfo } from './definitions';
export { AppList };

import type { BlockAppsPlugin } from './definitions';

const BlockApps = registerPlugin<BlockAppsPlugin>('BlockApps');

export { BlockAppsPlugin} from './definitions';
export { BlockApps };