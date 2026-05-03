import UIAbility from '@ohos.app.ability.UIAbility';
import window from '@ohos.window';
import preferences from '@ohos.data.preferences';

export default class EntryAbility extends UIAbility {
  onCreate(want, launchParam) {
    console.info('AI-OA EntryAbility onCreate');
    // 初始化应用配置
    this.initAppConfig();
  }

  onDestroy() {
    console.info('AI-OA EntryAbility onDestroy');
  }

  onWindowStageCreate(windowStage) {
    // 加载主页面
    windowStage.loadContent('pages/Main', (err, data) => {
      if (err.code) {
        console.error('Failed to load the content. Cause: ' + JSON.stringify(err));
        return;
      }
      console.info('Succeeded in loading the content. Data: ' + JSON.stringify(data));
    });
  }

  onWindowStageDestroy() {
    console.info('AI-OA EntryAbility onWindowStageDestroy');
  }

  onForeground() {
    console.info('AI-OA EntryAbility onForeground');
  }

  onBackground() {
    console.info('AI-OA EntryAbility onBackground');
  }

  private async initAppConfig() {
    try {
      const options = await preferences.getOptions(this.context, 'app_config');
      // 默认配置
    } catch (err) {
      console.error('Failed to get app config. Cause: ' + JSON.stringify(err));
    }
  }
}
