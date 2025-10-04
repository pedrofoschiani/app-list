import { registerPlugin } from '@capacitor/core';

export interface AppListPlugin {
  getInstalledApps(): Promise<{ apps: AppInfo[] }>;
}

export interface AppInfo {
  name: string;
  packageName: string;
}

export interface BlockAppsPLugin {
  setBlockedApps(options: { packageNames: string[] }): Promise<void>;
}

const AppList = registerPlugin<AppListPlugin>('AppList');
export { AppList };

const BlockApps = registerPlugin<BlockAppsPLugin>('BlockApps');
export { BlockApps };