import {
  SaveSearch,
  SAVED_SEARCH_DEFINITION_ID,
  BUTTON_CREATE_NEW,
  BUTTON_UPDATE
} from 'search/components/saved/save-search';
import {
  SavedSearchLoadedEvent,
  SavedSearchCreatedEvent,
  SavedSearchUpdatedEvent
} from 'search/components/saved/events';
import {SearchService} from 'services/rest/search-service';
import {SearchMediator, EVENT_CLEAR} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {DialogService} from 'components/dialog/dialog-service';
import {OPEN_SAVED_SEARCH_EVENT} from 'search/components/saved/saved-search-select/saved-search-select';

import {PromiseStub} from 'test/promise-stub';

class SavedSearchStub extends SaveSearch {
  constructor(instanceRest, dialogService, permissionService, translateService, notificationService, eventbus) {
    super(instanceRest, dialogService, permissionService, translateService, notificationService, eventbus);
  }
}

describe('SaveSearch', () => {

  var saveSearch;
  beforeEach(() => {
    SavedSearchStub.prototype.config = {
      searchMediator: new SearchMediator(undefined, new QueryBuilder({}))
    };

    var eventbus = {
      publish: sinon.spy(),
    };

    saveSearch = new SavedSearchStub(mockInstanceRestService('123'), mockDialogService(), mockPermissionsService(true), mockTranslateService(), mockNotificationService(), eventbus);
  });

  describe('registerMediatorListeners()', () => {
    it('should register a mediator listener for when a saved search is opened', () => {
      saveSearch.searchId = '';
      saveSearch.searchTitle = '';

      var openListeners = saveSearch.config.searchMediator.listeners[OPEN_SAVED_SEARCH_EVENT];
      expect(openListeners).to.exist;
      expect(openListeners.length).to.equal(1);

      var openListener = openListeners[0];
      var dummySavedSearch = {
        id: '123',
        text: 'My search'
      };

      openListener(dummySavedSearch);

      expect(saveSearch.searchId).to.equal('123');
      expect(saveSearch.searchTitle).to.equal('My search');

      // Should save the current model for later comparisons
      expect(saveSearch.tempModel).to.exist;
      expect(saveSearch.eventbus.publish.calledOnce).to.be.true;
      expect(saveSearch.eventbus.publish.getCall(0).args[0].getData()).to.deep.eq(dummySavedSearch);
      expect(saveSearch.eventbus.publish.getCall(0).args[0] instanceof SavedSearchLoadedEvent).to.be.true;
    });

    it('should register a mediator listener for when the search is cleared', () => {
      saveSearch.searchId = '123';
      saveSearch.searchTitle = 'My saved search';

      var clearListeners = saveSearch.config.searchMediator.listeners[EVENT_CLEAR];
      expect(clearListeners).to.exist;
      expect(clearListeners.length).to.equal(1);

      var clearListener = clearListeners[0];
      clearListener();

      expect(saveSearch.searchId).to.equal('');
      expect(saveSearch.searchTitle).to.equal('');
    });

    // TODO: Test if permissions are applied !!!
  });

  describe('getModel()', () => {
    it('should correctly construct the saved search model', () => {
      var criteriaTree = {condition: 'OR', rules: []};
      saveSearch.searchId = '123';
      saveSearch.searchTitle = 'My saved search';
      saveSearch.config.searchMediator.arguments.orderBy = 'emf:createdOn';
      saveSearch.config.searchMediator.arguments.orderDirection = 'ascending';
      saveSearch.config.searchMediator.searchMode = SearchCriteriaUtils.BASIC_MODE;
      saveSearch.config.searchMediator.queryBuilder.tree = criteriaTree;

      var searchModel = saveSearch.getModel();

      expect(searchModel).to.exist;
      expect(searchModel.definitionId).to.equal(SAVED_SEARCH_DEFINITION_ID);
      expect(searchModel.properties).to.exist;
      expect(searchModel.properties.title).to.equal('My saved search');
      expect(searchModel.properties.searchType).to.equal(SearchCriteriaUtils.BASIC_MODE);

      var filterCriteria = JSON.parse(searchModel.properties.searchCriteria);
      expect(filterCriteria.orderBy).to.equal('emf:createdOn');
      expect(filterCriteria.orderDirection).to.equal('ascending');
      expect(filterCriteria.criteria).to.deep.equal(criteriaTree);
    });
  });

  describe('saveSearch()', () => {
    it('should display a confirmation dialog for creating a new saved search', () => {
      saveSearch.saveSearch();
      expect(saveSearch.dialogService.confirmation.calledOnce).to.be.true;

      var dialogConfig = saveSearch.dialogService.confirmation.getCall(0).args[2];
      expect(dialogConfig).to.exist;
      expect(dialogConfig.buttons).to.exist;
      expect(dialogConfig.buttons.length).to.equal(2);

      expect(dialogConfig.buttons[0].id).to.equal(BUTTON_CREATE_NEW);
      expect(dialogConfig.buttons[1].id).to.equal(DialogService.CANCEL);
    });

    it('should display a confirmation dialog for creating a new saved search or updating the existing', () => {
      saveSearch.hasPermissions = true;
      saveSearch.searchId = '123';
      saveSearch.tempModel = saveSearch.getModel();
      saveSearch.tempModel.orderDirection = 'right';

      saveSearch.saveSearch();
      expect(saveSearch.dialogService.confirmation.calledOnce).to.be.true;

      var dialogConfig = saveSearch.dialogService.confirmation.getCall(0).args[2];
      expect(dialogConfig).to.exist;
      expect(dialogConfig.buttons).to.exist;
      expect(dialogConfig.buttons.length).to.equal(3);

      expect(dialogConfig.buttons[0].id).to.equal(BUTTON_CREATE_NEW);
      expect(dialogConfig.buttons[1].id).to.equal(BUTTON_UPDATE);
      expect(dialogConfig.buttons[2].id).to.equal(DialogService.CANCEL);
    });

    it('should save the search via the instance rest service', () => {
      var model = saveSearch.getModel();

      saveSearch.saveSearch();

      var dialogConfig = saveSearch.dialogService.confirmation.getCall(0).args[2];
      var buttonHandler = dialogConfig.onButtonClick;

      var dismissSpy = sinon.spy();
      buttonHandler(BUTTON_CREATE_NEW, undefined, {
        dismiss: dismissSpy
      });

      expect(dismissSpy.calledOnce).to.be.true;
      expect(saveSearch.instanceRestService.create.calledOnce).to.be.true;
      expect(saveSearch.instanceRestService.create.getCall(0).args[0]).to.deep.equal(model);
      expect(saveSearch.hasPermissions).to.be.true;

      expect(saveSearch.eventbus.publish.getCall(0).args[0] instanceof SavedSearchCreatedEvent).to.be.true;
    });

    it('should not allow to save search with same name', () => {
      saveSearch.instanceRestService = mockInstanceRestService(null, true);
      expect(saveSearch.createNew()).to.throw;
      expect(saveSearch.notificationService.warning.called).to.be.true;
    });

    it('should update the search via the instance rest service', () => {
      var model = saveSearch.getModel();

      saveSearch.searchId = '123';
      saveSearch.saveSearch();

      var dialogConfig = saveSearch.dialogService.confirmation.getCall(0).args[2];
      var buttonHandler = dialogConfig.onButtonClick;

      var dismissSpy = sinon.spy();
      buttonHandler(BUTTON_UPDATE, undefined, {
        dismiss: dismissSpy
      });

      expect(dismissSpy.calledOnce).to.be.true;
      expect(saveSearch.instanceRestService.update.calledOnce).to.be.true;
      expect(saveSearch.instanceRestService.update.getCall(0).args[0]).to.equal('123');
      expect(saveSearch.instanceRestService.update.getCall(0).args[1]).to.deep.equal(model);

      expect(saveSearch.eventbus.publish.getCall(0).args[0].getData().id).to.deep.eq('123');
      expect(saveSearch.eventbus.publish.getCall(0).args[0] instanceof SavedSearchUpdatedEvent).to.be.true;
    });
  });

  function mockInstanceRestService(id, savedSearchExist) {
    var instance = {
      data: {
        id: id,
        properties: {}
      }
    };
    return {
      create: sinon.spy(() => {
        if (!savedSearchExist) {
          return PromiseStub.resolve(instance);
        }
        return PromiseStub.reject({data: {message: 'error'}});
      }),
      update: sinon.spy(() => {
        return PromiseStub.resolve(instance);
      })
    };
  }

  function mockDialogService() {
    return {
      confirmation: sinon.spy(),
      createButton: (id, text) => {
        return {
          id: id,
          text: text
        }
      }
    };
  }

  function mockPermissionsService(editAllowed) {
    return {
      load: sinon.spy(() => {
        return PromiseStub.resolve({
          data: {
            editAllowed: editAllowed
          }
        });
      })
    };
  }

  function mockTranslateService() {
    return {
      translateInstant: () => {
        return 'translated';
      },
      translateInstantWithInterpolation: ()=> {
        return 'translated'
      }
    };
  }

  function mockNotificationService() {
    return {
      success: sinon.spy(),
      warning: sinon.spy()
    };
  }
});