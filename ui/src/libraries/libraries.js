import {Component, View, Inject} from 'app/app';
import {LibrariesService} from 'services/rest/libraries-service';
import {ActionExecutor} from 'services/actions/action-executor';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';
import {TranslateService} from 'services/i18n/translate-service';
import {NotificationService} from 'services/notification/notification-service';
import 'search/components/common/search-results';
import 'header-container/header-container';
import './libraries.css!css';
import librariesTemplate from './libraries.html!text';

const WRONG_LIBRARY_TYPE = 'wrong.library.type';

@Component({
  selector: 'seip-libraries'
})
@View({
  template: librariesTemplate
})
@Inject(LibrariesService, ActionExecutor, StateParamsAdapter, NotificationService, TranslateService)
export class Libraries {

  constructor(librariesService, actionExecutor, stateParamsAdapter, notificationService, translateService) {
    this.librariesService = librariesService;
    this.actionExecutor = actionExecutor;
    this.stateParamsAdapter = stateParamsAdapter;
    this.notificationService = notificationService;
    this.translateService = translateService;
    this.libraries = [];
    this.config = {'selection': 'none', renderMenu: true, selectedItems: []};
    this.results = {};
  }

  ngAfterViewInit() {
    this.librariesService.loadLibraries().then((response)=> {
      this.libraries = response.data.values;
      this.results = {config: this.config, data: this.libraries};
    });
  }

}