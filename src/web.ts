import { WebPlugin } from '@capacitor/core';

import type { AppInfo, AppListPlugin } from './definitions';

export class AppListWeb extends WebPlugin implements AppListPlugin {
  async getInstalledApps(): Promise<{ apps: AppInfo[] }> {
    console.warn('O plugin AppList não é compatível com a plataforma web.');
    return { apps: [] };
  }
}