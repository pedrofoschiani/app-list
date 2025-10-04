import { registerPlugin } from '@capacitor/core';
import type { AppListPlugin } from './definitions';

const AppList = registerPlugin<AppListPlugin>('AppList');

export { AppListPlugin, AppInfo } from './definitions';
export { AppList };

import type { BlockAppsPLugin } from './definitions';

const BlockApps = registerPlugin<BlockAppsPLugin>('BlockApps');

export { BlockAppsPLugin} from './definitions';
export { BlockApps };