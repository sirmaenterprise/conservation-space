import {Inject,Injectable} from 'app/app';
import 'angular-translate';

/**
 * Adapter for angular-translate's $translate service
 */
@Injectable()
@Inject('$translate')
export class TranslateAdapter {
  constructor($translate) {
    this.$translate = $translate;
  }

  translate(key) {
    return this.$translate(key);
  }

  instant(key, parameters) {
    return this.$translate.instant(key, parameters);
  }

  changeLanguage(lang) {
    return this.$translate.use(lang);
  }

  refresh(lang) {
    return this.$translate.refresh(lang);
  }

  getCurrentLanguage() {
    return this.$translate.use();
  }
}
