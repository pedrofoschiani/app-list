export interface AppListPlugin {
  getInstalledApps(): Promise<{ apps: AppInfo[] }>;
}

export interface AppInfo {
  name: string;
  packageName: string;
}