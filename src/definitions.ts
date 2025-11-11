import { registerPlugin } from '@capacitor/core';

export interface AppListPlugin {
  getInstalledApps(): Promise<{ apps: AppInfo[] }>;
  setBlockedPackages(options: { packages: string[] }): Promise<void>;
  openAccessibilitySettings(): Promise<void>;
}

export interface AppInfo {
  name: string;
  packageName: string;
}

export const AppList = registerPlugin<AppListPlugin>('AppList');