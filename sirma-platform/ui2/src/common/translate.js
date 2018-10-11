import angular from 'angular';
import application from 'app/app';
import 'angular-translate';
import {LabelRestService} from 'services/rest/label-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {LanguageChangeSuccessEvent} from 'services/i18n/language-change-success-event';

var translateModule = angular.module('translate', [application.name, 'pascalprecht.translate']);

application.requires.push(translateModule.name);

translateModule.config(['$translateProvider', '$provide', function($translateProvider, $provide) {
  function makeStateful($delegate) {
    $delegate.$stateful = true;
    return $delegate;
  }
  // Make filters stateful so change of locale will trigger update of the values
  $provide.decorator('dateFilter', ['$delegate', makeStateful]);
  $provide.decorator('numberFilter', ['$delegate', makeStateful]);
  $provide.decorator('currencyFilter', ['$delegate', makeStateful]);

  $translateProvider
  .useLoader('labelLoader')
  .preferredLanguage('en')
  .fallbackLanguage('en');
}]);

translateModule.factory('labelLoader',['LabelRestService', function (labelRestService) {
  return function (options) {
    return labelRestService.getLabels(options.key);
  };
}]);


translateModule.run(['$rootScope','$locale', '$translate', 'Eventbus', function($rootScope, $locale, $translate, eventbus) {
  // Update locale labels
  $rootScope.$on('$translateChangeSuccess', function (event, opts) {
    $locale.id = opts.language;

    let labels = ['month.january', 'month.february', 'month.march',
                  'month.april', 'month.may', 'month.june',
                  'month.july', 'month.august', 'month.september',
                  'month.october', 'month.november', 'month.december',
                  'day.sunday', 'day.monday', 'day.tuesday',
                  'day.wednesday', 'day.thursday', 'day.friday',
                  'day.saturday'];
    //TODO: Update other locale variables - SHORTMONTH, SHORTDAY, AMPMS, ERANAMES, ERAS
    $translate(labels).then(function(result) {
      $locale.DATETIME_FORMATS.MONTH[0] = result['month.january'];
      $locale.DATETIME_FORMATS.MONTH[1] = result['month.february'];
      $locale.DATETIME_FORMATS.MONTH[2] = result['month.march'];
      $locale.DATETIME_FORMATS.MONTH[3] = result['month.april'];
      $locale.DATETIME_FORMATS.MONTH[4] = result['month.may'];
      $locale.DATETIME_FORMATS.MONTH[5] = result['month.june'];
      $locale.DATETIME_FORMATS.MONTH[6] = result['month.july'];
      $locale.DATETIME_FORMATS.MONTH[7] = result['month.august'];
      $locale.DATETIME_FORMATS.MONTH[8] = result['month.september'];
      $locale.DATETIME_FORMATS.MONTH[9] = result['month.october'];
      $locale.DATETIME_FORMATS.MONTH[10] = result['month.november'];
      $locale.DATETIME_FORMATS.MONTH[11] = result['month.december'];

      $locale.DATETIME_FORMATS.DAY[0] = result['day.sunday'];
      $locale.DATETIME_FORMATS.DAY[1] = result['day.monday'];
      $locale.DATETIME_FORMATS.DAY[2] = result['day.tuesday'];
      $locale.DATETIME_FORMATS.DAY[3] = result['day.wednesday'];
      $locale.DATETIME_FORMATS.DAY[4] = result['day.thursday'];
      $locale.DATETIME_FORMATS.DAY[5] = result['day.friday'];
      $locale.DATETIME_FORMATS.DAY[6] = result['day.saturday'];

      eventbus.publish(new LanguageChangeSuccessEvent(opts));
    }.bind(this));
  }.bind(this));
}]);

export default translateModule;
