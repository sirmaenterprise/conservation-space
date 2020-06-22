import {Inject, Injectable} from 'app/app';
import {Configuration} from 'common/application-config';

import miradorStaticConfig from './mirador-static-config.json!';
import miradorStaticConfigZenMode from './mirador-static-config-zen-mode.json!';

@Injectable()
@Inject(Configuration)
export class MiradorConfig {

  constructor(config) {
  	this.config = config;
  }

  get() {
  	return this.make(miradorStaticConfig);
  }

  zen() {
  	return this.make(miradorStaticConfigZenMode);
  }

  make(from) {
    let drawingToolsSettings = from.drawingToolsSettings || {};
    const customPalette = this.config.get(Configuration.IMAGE_TOOL_PALETTE_REPLACE);
    const extendedPalette = this.config.get(Configuration.IMAGE_TOOL_PALETTE_EXTEND);

    if (customPalette) {
      const palette = MiradorConfig.makePalette(customPalette);
      const defaultColor = MiradorConfig.getFistColorFromPallete(palette);

      drawingToolsSettings.colorPalette = palette;
      if (defaultColor) {
        drawingToolsSettings.strokeColor = defaultColor;
        drawingToolsSettings.fillColor = defaultColor;
      }
    } else if (extendedPalette) {
      drawingToolsSettings.extendColorPalette = MiradorConfig.makePalette(extendedPalette);
    }

    from.drawingToolsSettings = drawingToolsSettings;
  	return from;
  }

  static makePalette(palette) {
    return palette.split('|')
      .map(colors => {
        return colors.split(',');
      });
  }

  static getFistColorFromPallete(palette) {
    if (!palette.length || palette[0].length) {
      return '';
    }
    return palette[0][0];
  }
}
