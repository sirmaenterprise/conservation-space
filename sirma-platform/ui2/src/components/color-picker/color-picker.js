import {View, Component, Inject, NgElement, NgScope} from 'app/app';
import {Configurable} from 'components/configurable';
import {TranslateService} from 'services/i18n/translate-service';
import {rgb as d3rgb} from 'd3';
import _ from 'lodash';
import 'spectrum-colorpicker';
import 'spectrum-colorpicker/spectrum.css!';
import './color-picker.css!';

/**
 * Color picker component allowing to pick a color based on spectrum library. See https://bgrins.github.io/spectrum/
 *
 * Config: {
 *  value: color string. Either hex, rgb or color name
 * }
 *
 * Events: {
 *  onChanged: fired when color is changed with new color as parameter. New color is hex string.
 * }
 *
 */
@Component({
  selector: 'seip-color-picker',
  properties: {
    'config': 'config'
  },
  events: ['onChanged']
})
@View({
  template: '<input type="text">'
})
@Inject(NgElement, NgScope, TranslateService)
export class ColorPicker extends Configurable {
  constructor($element, $scope, translateService) {
    super({});
    this.$element = $element;
    this.$scope = $scope;
    this.translateService = translateService;
  }

  ngOnInit() {
    this.$element.spectrum(this.getColorPickerConfiguration());
    this.$scope.$watch(() => {
      return this.config.value;
    }, (newValue) => {
      this.$element.spectrum('set', newValue);
    });
  }

  getColorPickerConfiguration() {
    return {
      containerClassName: 'color-picker',
      replacerClassName: 'color-picker',
      preferredFormat: 'hex',
      showInput: true,
      allowEmpty: true,
      showPaletteOnly: true,
      togglePaletteOnly: true,
      togglePaletteMoreText: this.translateService.translateInstant('color.picker.more'),
      togglePaletteLessText: this.translateService.translateInstant('color.picker.less'),
      cancelText: this.translateService.translateInstant('color.picker.cancel'),
      chooseText: this.translateService.translateInstant('color.picker.choose'),
      color: this.config.value,
      palette: [
        ['#000', '#444', '#666', '#999', '#ccc', '#eee', '#f3f3f3', '#fff'],
        ['#f00', '#f90', '#ff0', '#0f0', '#0ff', '#00f', '#90f', '#f0f'],
        ['#f4cccc', '#fce5cd', '#fff2cc', '#d9ead3', '#d0e0e3', '#cfe2f3', '#d9d2e9', '#ead1dc'],
        ['#ea9999', '#f9cb9c', '#ffe599', '#b6d7a8', '#a2c4c9', '#9fc5e8', '#b4a7d6', '#d5a6bd'],
        ['#e06666', '#f6b26b', '#ffd966', '#93c47d', '#76a5af', '#6fa8dc', '#8e7cc3', '#c27ba0'],
        ['#c00', '#e69138', '#f1c232', '#6aa84f', '#45818e', '#3d85c6', '#674ea7', '#a64d79'],
        ['#900', '#b45f06', '#bf9000', '#38761d', '#134f5c', '#0b5394', '#351c75', '#741b47'],
        ['#600', '#783f04', '#7f6000', '#274e13', '#0c343d', '#073763', '#20124d', '#4c1130']
      ],
      change: (color) => {
        let newColor;
        if (color) {
          newColor = color.toHexString();
        }
        if (_.isFunction(this.onChanged)) {
          this.$scope.$evalAsync(() => {
            this.config.value = newColor;
            this.onChanged({newColor});
          });
        }
        this.$element.spectrum('hide');
      }
    };
  }

  ngOnDestroy() {
    // It seems that destroy doesn't remove spectrum container (.sp-container element attached directly to document body)
    $('.sp-container').remove();
    this.$element.spectrum('destroy');
  }

  /**
   * Returns either black or white (as hex color string) for best contrast with the input color.
   * @param hexColor hex color string
   * @returns {string} hex color string (either black or white)
   */
  static getContrastBWColor(hexColor) {
    let rgbColor = d3rgb(hexColor);
    let yiq = ((rgbColor.r * 299) + (rgbColor.g * 587) + (rgbColor.b * 114)) / 1000;
    return (yiq >= 128) ? '#000' : '#fff';
  }
}
