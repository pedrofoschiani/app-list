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

<<<<<<< HEAD
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
=======
export class BlockAppsWeb extends WebPlugin implements BlockAppsPLugin {
  async setBlockedApps(_options: { packageNames: string[] }): Promise<void> {
    console.warn('O plugin BlockApps não é compatível com a plataforma web.');
    return;
>>>>>>> parent of ad688ac (mudança no web.ts e na versão do plugin)
  }
}
export const BlockApps = registerPlugin<BlockAppsPLugin>('BlockApps', {
  web: () => import('./web').then(m => new m.BlockAppsWeb()),
});