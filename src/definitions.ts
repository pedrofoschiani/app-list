export interface AppListPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
