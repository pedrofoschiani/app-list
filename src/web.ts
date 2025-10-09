import { WebPlugin, registerPlugin } from '@capacitor/core';
import type { AppInfo, AppListPlugin, BlockAppsPlugin} from './definitions';

export class AppListWeb extends WebPlugin implements AppListPlugin {
  async getInstalledApps(): Promise<{ apps: AppInfo[] }> {
    console.warn('O plugin AppList não é compatível com a plataforma web.');
    return { apps: [] };
  }
}
export const AppList = registerPlugin<AppListPlugin>('AppList', {
  web: () => import('./web').then(m => new m.AppListWeb()),
});

export class BlockAppsWeb extends WebPlugin implements BlockAppsPlugin {
  checkAndRequestPermissions(): Promise<{ overlay: boolean; usage: boolean; }> {
    throw new Error('Method not implemented.');
  }
  startService(_options: { packages: string[]; }): Promise<void> {
    throw new Error('Method not implemented.');
  }
  stopService(): Promise<void> {
    throw new Error('Method not implemented.');
  }
  async setBlockedPackages(_options: { packages: string[]; }): Promise<void> {
    throw new Error('Method not implemented.');
  }
}
export const BlockApps = registerPlugin<BlockAppsPlugin>('BlockApps', {
  web: () => import('./web').then(m => new m.BlockAppsWeb()),
});