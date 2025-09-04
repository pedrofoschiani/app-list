export interface AppListPlugin {
  getInstalledApps(): Promise<{ apps: { name: string; packageName: string }[] }>;
}