import { registerPlugin } from '@capacitor/core';

export interface AppListPlugin {
  getInstalledApps(): Promise<{ apps: AppInfo[] }>;
}

export interface AppInfo {
  name: string;
  packageName: string;
}

export interface BlockAppsPlugin {
  setBlockedPackages(options: { packages: string[] }): Promise<void>;
}

export const AppList = registerPlugin<AppListPlugin>('AppList');

export const BlockApps = registerPlugin<BlockAppsPlugin>('BlockApps');