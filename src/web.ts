import { WebPlugin } from '@capacitor/core';
import type { AppInfo, BlomePlugin } from './definitions';

export class BlomeWeb extends WebPlugin implements BlomePlugin {

  async setBlockedApps(_options: { packages: string[]; }): Promise<void> {
    console.warn('O método setBlockedPackages não é compatível com a web.');
    return;
  }
  
  async getInstalledApps(): Promise<{ apps: AppInfo[] }> {
    console.warn('O método getInstalledApps não é compatível com a web.');
    return { apps: [] };
  }
}