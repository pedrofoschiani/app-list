import { WebPlugin } from '@capacitor/core';

import type { AppListPlugin } from './definitions';

export class AppListWeb extends WebPlugin implements AppListPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
