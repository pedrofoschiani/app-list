import { registerPlugin } from '@capacitor/core';

export interface AppListPlugin {
  getInstalledApps(): Promise<{ apps: AppInfo[] }>;
  setBlockedPackages(options: { packages: string[] }): Promise<void>;
  openAccessibilitySettings(): Promise<void>;
  isAccessibilityServiceEnabled(): Promise<{ enabled: boolean }>;
}

export interface AppInfo {
  name: string;
  packageName: string;
}

export const AppList = registerPlugin<AppListPlugin>('AppList');