import {Component, View} from 'app/app';
import 'components/color-picker/color-picker';

import './widget-common-display-configurations.css!';
import template from './widget-common-display-configurations.html!text';

@Component({
  selector: 'seip-widget-common-display-configurations',
  properties: {
    config: 'config'
  }
})
@View({
  template
})
export class WidgetCommonDisplayConfigurations {

  constructor() {
    this.widgetHeaderBackgroundColorConfig = {
      value: this.config.widgetHeaderBackgroundColor
    };

    this.widgetBackgroundColorConfig = {
      value: this.config.widgetBackgroundColor
    };
  }

  onWidgetHeaderBackgroundColorChanged(newColor) {
    this.config.widgetHeaderBackgroundColor = newColor;
  }

  onWidgetBackgroundColorChanged(newColor) {
    this.config.widgetBackgroundColor = newColor;
  }

  /**
   * Remove all widget color configurations
   */
  resetDefaultColors() {
    delete this.widgetHeaderBackgroundColorConfig.value;
    delete this.widgetBackgroundColorConfig.value;

    delete this.config.widgetHeaderBackgroundColor;
    delete this.config.widgetBackgroundColor;
  }
}
