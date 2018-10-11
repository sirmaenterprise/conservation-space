import {Injectable, Inject} from 'app/app';
import {TranslateAdapter} from 'adapters/angular-translate/translate-adapter';
import {Eventbus} from 'services/eventbus/eventbus';
import {LanguageChangeSuccessEvent} from 'services/i18n/language-change-success-event';
import {Logger} from 'services/logging/logger';

/**
 * Service for translating labels and another language related functionalities.
 */
@Injectable()
@Inject(TranslateAdapter, Eventbus, Logger)
export class TranslateService {
  constructor(translateAdapter, eventbus, logger) {
    this.translateAdapter = translateAdapter;
    this.eventbus = eventbus;
    this.logger = logger;
  }

  /**
   * Translate given key.
   * If the translation has to be reevaluated on language change all four parameters should be passed.
   *
   * @param key the key to be translated
   * @param scope (optional) used to remove all created subscriptions when scope is destroyed
   * @param controller (optional) an Object into which result will be returned into variable with name=variableName
   * @param variableName (optional) the name of the variable where result will be returned
   *
   * @returns a promise which will be resolved when the requested label is available
   */
  translate(key, scope, controller, variableName) {
    let result = this.translateAdapter.translate(key);

    if (scope || controller || variableName) {
      if (scope && controller && variableName) {
        result.then((value) => {
          controller[variableName] = value;
        }).catch((error) => {
          this.logger.error(error);
        });
        let unsubscribeFn = this.eventbus.subscribe(LanguageChangeSuccessEvent, () => {
          this.translateAdapter.translate(key).then((value) => {
            controller[variableName] = value;
          }).catch((error) => {
            this.logger.error(error);
          });
        });
        scope.$on('$destroy', function () {
          this.eventbus.unsubscribe(unsubscribeFn);
        }.bind(this));
      } else {
        throw new Error('TranslateService.translate expect all three arguments (scope, controller and variableName) to be set in order to work correctly.');
      }
    }

    return result;
  }

  /**
   * Translate given key instantly and provides parameter interpolation. I.e. 'Hello {{name}}!'.
   * If the translation has to be reevaluated on language change all four parameters should be passed.
   *
   * @param key the key to be translated
   * @param parameters parameters to be interpolated in the labe.
   * @param scope (optional) used to remove all created subscriptions when scope is destroyed
   * @param controller (optional) an Object into which result will be returned into variable with name=variableName
   * @param variableName (optional) the name of the variable where result will be returned
   *
   * @returns translated value
   */
  translateInstantWithInterpolation(key, parameters, scope, controller, variableName) {
    let result = this.translateAdapter.instant(key, parameters);

    if (scope || controller || variableName) {
      if (scope && controller && variableName) {
        controller[variableName] = result;
        let unsubscribeFn = this.eventbus.subscribe(LanguageChangeSuccessEvent, function () {
          controller[variableName] = this.translateAdapter.instant(key);
        }.bind(this));
        scope.$on('$destroy', function () {
          this.eventbus.unsubscribe(unsubscribeFn);
        }.bind(this));
      } else {
        throw new Error('TranslateService.translateInstant expect all three arguments (scope, controller and variableName) to be set in order to work correctly.');
      }
    }

    return result;
  }

  /**
   * Translate given key instantly.
   * If the translation has to be reevaluated on language change all four parameters should be passed.
   *
   * @param key the key to be translated
   * @param scope (optional) used to remove all created subscriptions when scope is destroyed
   * @param controller (optional) an Object into which result will be returned into variable with name=variableName
   * @param variableName (optional) the name of the variable where result will be returned
   *
   * @returns translated value
   */
  translateInstant(key, scope, controller, variableName) {
    return this.translateInstantWithInterpolation(key, undefined, scope, controller, variableName);
  }

  /**
   * Change language to the given language. After language is successfully changed a LanguageChangeSuccessEvent is fired.
   *
   * @returns a promise which will be resolved when the language is changed and labels for it are loaded
   */
  changeLanguage(lang) {
    return this.translateAdapter.changeLanguage(lang);
  }

  /**
   * Refresh key-value map for given language.
   *
   * @returns a promise which will be resolved in case a translation tables refreshing process is finished successfully, and rejected if not
   */
  refresh(lang) {
    return this.translateAdapter.refresh(lang);
  }

  /**
   * Refresh key-value map for current language.
   *
   * @returns a promise which will be resolved in case a translation tables refreshing process is finished successfully, and rejected if not
   */
  refreshCurrentLanguage() {
    return this.translateAdapter.refresh(this.translateAdapter.getCurrentLanguage());
  }

  /**
   * Returns current language.
   *
   * @returns language string
   */
  getCurrentLanguage() {
    return this.translateAdapter.getCurrentLanguage();
  }
}
