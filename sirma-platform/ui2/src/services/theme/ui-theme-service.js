import {Injectable, Inject} from 'app/app';
import {BootstrapService} from 'services/bootstrap-service';
import {ThemeRestClient} from 'services/rest/ui-theme-rest-service';
import {darken, invert} from 'polished';

import _ from 'lodash';
import template from './template.html!text';

/* constants that need additional darkening before applying */
const SHOULD_DARKEN = ['button_color', 'button_hover_color', 'button_disabled_color'];

/**
 * Theme bootstrap service used load, evaluate and template custom system skin, based on model definition.
 * Lodash _.template method is used to evaluate and append css styling directly to the `body` of the document.
 *
 * More on templates: http://2ality.com/2012/06/underscore-templates.html
 */
@Injectable()
@Inject(ThemeRestClient)
export class ThemeBootstrapService extends BootstrapService {

  constructor(themeRestClient) {
    super();
    this.themeRestClient = themeRestClient;
  }

  initialize() {
    this.themeRestClient.getThemeData().then(response => {
      if (Object.keys(response.data).length) {
        let compiledTemplateFn = _.template(template, {variable: 'styles'});
        let styles = this.processStyles(response.data);
        $('body').append(compiledTemplateFn(styles));
      }
    });
  }

  /**
   * Checks if included styles have constants which need a complimented ``darken`` color for specific component styles.
   * @param styles styles map.
   * @returns {*} styles map with added darkened constants (with extra added ``_dark`` postfix)
   */
  processStyles(styles) {
    let includedDarkenStyes = _.intersection(Object.keys(styles), SHOULD_DARKEN);
    includedDarkenStyes.forEach(style => {
      styles[style + '_dark'] = darken(0.05, styles[style]);
    });

    if (styles.link_color) {
      styles['link_color_darker'] = darken(0.15, styles['link_color']);
    }

    return styles;
  }

}
