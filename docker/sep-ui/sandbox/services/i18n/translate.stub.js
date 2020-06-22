import {Component, View, Inject} from 'app/app';
import {TranslateService} from 'services/i18n/translate-service';
import {DateService} from 'services/i18n/date-service';
import translateTemplate from 'i18n/translate-template!text';

@Component({
  selector: 'seip-translate',
  properties: {
    'config': 'config',
  },
})
@View({
  template: translateTemplate,
})
@Inject('TranslateService', 'DateService', '$scope')
export class Translate {
  constructor(translateService, dateService, $scope) {
    this.$scope = $scope;
    this.dateService = dateService;
    this.translateService = translateService;

    // Translate label instantly before labels are loaded and bind it to controller variable. On language change this label will be reevaluated.
    // It is immediately set to 'homepage' after which LanguageChangeSuccessEvent (language is changed/loaded asynchronously) is observed and it is automatically changed to its correct value.
    this.translateService.translateInstant('homepage', this.$scope, this, 'homepage');

    // Translate label instantly before labels are loaded. That is why 'homepage.unbind' is returned and it doesn't change on language change.
    this.homepageunbind = this.translateService.translateInstant('homepage.unbind');

    // Translate label and bind it to controller variable.
    this.translateService.translate('menu', this.$scope, this, 'menu');

    // Translate label and bind it to controller variable.
    this.translateService.translate('fallback', this.$scope, this, 'fallback');

    // Translate label after labels are loaded. It is properly set first time. On language change it doesn't reevaluate.
    this.translateService.translate('menu.unbind').then((value)=> {
      this.menuunbind = value;
    });

    this.dateService.formatDate(new Date('July 10, 2015 11:13:00'), DateService.FULL_DATE, this.$scope, this, 'aDate');
    this.dateService.formatDate(new Date('October 13, 2014 11:13:00'), DateService.FULL_DATE, this.$scope, this, 'anotherDate');
    this.dateService.formatDate(new Date('June 6, 1983 03:00:00'), DateService.FULL_DATE, this.$scope, this, 'yetAnotherDate');
  }

  changeLanguage(lang) {
    this.translateService.changeLanguage(lang);
  }

  refresh() {
    this.translateService.refresh();
  }
}
