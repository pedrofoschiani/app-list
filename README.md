# app-list

Plugin para coletar o nome de todos os aplicativos instalados dentro do dispositivo, com exeção dos apps do sitema

## Install

```bash
npm install app-list
npx cap sync
```

## API

<docgen-index>

* [`getInstalledApps()`](#getinstalledapps)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getInstalledApps()

```typescript
getInstalledApps() => Promise<{ apps: { name: string; packageName: string; }[]; }>
```

**Returns:** <code>Promise&lt;{ apps: { name: string; packageName: string; }[]; }&gt;</code>

--------------------

</docgen-api>
