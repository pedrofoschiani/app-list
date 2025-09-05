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
* [Interfaces](#interfaces)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### getInstalledApps()

```typescript
getInstalledApps() => Promise<{ apps: AppInfo[]; }>
```

**Returns:** <code>Promise&lt;{ apps: AppInfo[]; }&gt;</code>

--------------------


### Interfaces


#### AppInfo

| Prop              | Type                |
| ----------------- | ------------------- |
| **`name`**        | <code>string</code> |
| **`packageName`** | <code>string</code> |
| **`icon`**        | <code>string</code> |

</docgen-api>
