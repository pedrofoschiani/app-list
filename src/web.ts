import { WebPlugin, registerPlugin } from '@capacitor/core';
import type { AppInfo, AppListPlugin, BlockAppsPLugin} from './definitions';

export class AppListWeb extends WebPlugin implements AppListPlugin {
  async getInstalledApps(): Promise<{ apps: AppInfo[] }> {
    console.warn('O plugin AppList não é compatível com a plataforma web.');
    return { apps: [] };
  }
}
export const AppList = registerPlugin<AppListPlugin>('AppList', {
  web: () => import('./web').then(m => new m.AppListWeb()),
});

export class BlockAppsWeb extends WebPlugin implements BlockAppsPLugin {
  async setBlockedApps(_options: { packageNames: string[] }): Promise<void> {
    console.warn('O plugin BlockApps não é compatível com a plataforma web.');
    return;
  }
}
export const BlockApps = registerPlugin<BlockAppsPLugin>('BlockApps', {
  web: () => import('./web').then(m => new m.BlockAppsWeb()),
});