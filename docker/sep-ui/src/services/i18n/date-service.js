import {Injectable, Inject} from 'app/app';
import {Eventbus} from 'services/eventbus/eventbus';
import {LanguageChangeSuccessEvent} from 'services/i18n/language-change-success-event';

/**
 * Service for formatting dates
 */
@Injectable()
@Inject('$filter', Eventbus)
export class DateService {

  constructor($filter, eventbus) {
    this.$filter = $filter;
    this.eventbus = eventbus;
  }

  /**
   * Format given date using the given format.
   * If the formatted date has to be reevaluated after language/config change/update, all five parameters should be passed.
   * Formatted date will be set into controller.variableName variable and it will be automatically reevaluated when needed.
   *
   * @param date date object to be formatted
   * @param format either one of the build-in formats - fullDate, longDate, medium, mediumDate, mediumTime, short, shortDate, shortTime or another format string
   * @param scope (optional) used to remove all created subscriptions when scope is destroyed
   * @param controller (optional) an Object into which result will be returned into variable with name=variableName
   * @param variableName (optional) the name of the variable where result will be returned
   *
   * @returns formatted date
   */
  formatDate(date, format, scope, controller, variableName) {
    let result = this.$filter('date')(date, format);

    // bind result to controller variable and reevaluate it after language change
    // TODO: reevaluate after both language and configuration change succeed
    if (scope || controller || variableName) {
      if (scope && controller && variableName) {
        controller[variableName] = result;
        let unsubscribeFn = this.eventbus.subscribe(LanguageChangeSuccessEvent, function() {
          controller[variableName] = this.$filter('date')(date, format);
        }.bind(this));
        scope.$on('$destroy', function() {
          this.eventbus.unsubscribe(unsubscribeFn);
        }.bind(this));
      } else {
        throw new Error('DateService.formatDate expect all three arguments (scope, controller and variableName) to be set in order to work correctly.');
      }
    }

    return result;
  }
}
// Build-in date format constants
DateService.FULL_DATE = 'fullDate';
