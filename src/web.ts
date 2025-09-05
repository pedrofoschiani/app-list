import { WebPlugin, registerPlugin } from '@capacitor/core';
import type { AppInfo, AppListPlugin } from './definitions';

export class AppListWeb extends WebPlugin implements AppListPlugin {
  async getInstalledApps(): Promise<{ apps: AppInfo[] }> {
    console.warn('O plugin AppList não é compatível com a plataforma web.');
    return { apps: [] };
  }
}
export const AppList = registerPlugin<AppListPlugin>('AppList', {
  web: () => import('./web').then(m => new m.AppListWeb()),
});