import {ModelManagementLanguageService} from 'administration/model-management/services/utility/model-management-language-service';
import {TranslateService} from 'services/i18n/translate-service';
import {CodelistRestService} from 'services/rest/codelist-service';
import {Configuration} from 'common/application-config';
import {Eventbus} from 'services/eventbus/eventbus';

import {ConfigurationsUpdateEvent} from 'common/configuration-events';
import {LanguageChangeSuccessEvent} from 'services/i18n/language-change-success-event';
import {CodeListSavedEvent} from 'administration/code-lists/services/events/code-list-saved-event';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelManagementLanguageService', () => {

  let eventBusStub;
  let translateStub;
  let configurationStub;
  let codeListRestServiceStub;

  let modelManagementLanguageService;

  beforeEach(() => {
    translateStub = stub(TranslateService);
    configurationStub = stub(Configuration);

    eventBusStub = stub(Eventbus);
    codeListRestServiceStub = stub(CodelistRestService);

    configurationStub.get.returns('en');
    translateStub.getCurrentLanguage.returns('bg');
    codeListRestServiceStub.getCodeList.returns(PromiseStub.resolve(getCodeListData()));

    modelManagementLanguageService = new ModelManagementLanguageService(eventBusStub, translateStub, configurationStub, codeListRestServiceStub, PromiseStub);
  });

  it('should properly initialize user, system and default languages', () => {
    expect(modelManagementLanguageService.getUserLanguage()).to.eq('bg');
    expect(modelManagementLanguageService.getSystemLanguage()).to.eq('en');
    expect(modelManagementLanguageService.getDefaultLanguage()).to.eq('en');
  });

  it('should properly cache & retrieve languages from the given code list', () => {
    expect(modelManagementLanguageService.availableLanguages).to.not.exist;

    modelManagementLanguageService.getLanguages();
    expect(modelManagementLanguageService.availableLanguages).to.deep.eq([
      {value: 'en', label: 'English'},
      {value: 'bg', label: 'Bulgarian'},
      {value: 'de', label: 'German'}
    ]);
  });

  it('should properly merge the languages from a code list model', () => {
    expect(modelManagementLanguageService.availableLanguages).to.not.exist;
    modelManagementLanguageService.availableLanguages = [
      {value: 'en', label: 'en'},
      {value: 'gr', label: 'gr'}
    ];
    modelManagementLanguageService.initializeAvailableLanguages(getCodeListData().data);
    expect(modelManagementLanguageService.availableLanguages).to.deep.eq([
      {value: 'en', label: 'English'},
      {value: 'bg', label: 'Bulgarian'},
      {value: 'de', label: 'German'},
      {value: 'gr', label: 'gr'}
    ]);
  });

  it('should initialize the languages through the subscription callback', () => {
    modelManagementLanguageService.availableLanguages = [];
    modelManagementLanguageService.initializeAvailableLanguages = sinon.spy();

    let codeList = getCodeListData().data;
    let callback = eventBusStub.subscribe.args[0][1];
    callback([codeList]);

    expect(modelManagementLanguageService.initializeAvailableLanguages.calledOnce).to.be.true;
    expect(modelManagementLanguageService.initializeAvailableLanguages.calledWith(codeList)).to.be.true;
  });

  it('should have proper number of subscriptions', () => {
    expect(modelManagementLanguageService.subscriptions.length).to.eq(3);
  });

  it('should subscribe to user language change', () => {
    expect(eventBusStub.subscribe.calledWith(LanguageChangeSuccessEvent)).to.be.true;
  });

  it('should subscribe to system language change', () => {
    expect(eventBusStub.subscribe.calledWith(ConfigurationsUpdateEvent)).to.be.true;
  });

  it('should subscribe to code list save', () => {
    expect(eventBusStub.subscribe.calledWith(CodeListSavedEvent)).to.be.true;
  });

  function getCodeListData() {
    // English & Bulgarian are in upper case to test the normalization
    return {
      data: {
        'value': '13',
        'values': [
          {
            'value': 'EN',
            'descriptions': [
              {'language': 'en', 'name': 'English'},
              {'language': 'bg', 'name': 'Англииски'}
            ]
          },
          {
            'value': 'BG',
            'descriptions': [
              {'language': 'en', 'name': 'Bulgarian'},
              {'language': 'bg', 'name': 'Български'}
            ]
          },
          {
            'value': 'de',
            'descriptions': [
              {'language': 'en', 'name': 'German'},
              {'language': 'bg', 'name': 'Немски'}
            ]
          }
        ]
      }
    };
  }
});