import { registerPlugin } from '@capacitor/core';

export interface AppListPlugin {
  getInstalledApps(): Promise<{ apps: AppInfo[] }>;
}

export interface AppInfo {
  name: string;
  packageName: string;
}

export interface BlockAppsPlugin {
  checkAndRequestPermissions(): Promise<{ overlay: boolean, usage: boolean }>;
  startService(options: { packages: string[] }): Promise<void>;
  stopService(): Promise<void>;
}

export const AppList = registerPlugin<AppListPlugin>('AppList');

export const BlockApps = registerPlugin<BlockAppsPlugin>('BlockApps');