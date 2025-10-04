export interface AppInfo {
  name: string;
  packageName: string;
}

export interface AppListPlugin {
  getInstalledApps(): Promise<{ apps: AppInfo[] }>;
}

export interface BlockAppsPlugin {
  setBlockedPackages(options: { packages: string[] }): Promise<void>;
}

export interface BlomePlugin extends AppListPlugin, BlockAppsPlugin {}