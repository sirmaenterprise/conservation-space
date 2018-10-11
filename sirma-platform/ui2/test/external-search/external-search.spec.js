import {ExternalSearch, DEFAULT_ORDER_DIRECTION} from 'external-search/external-search';
import {ExternalSearchService} from 'services/rest/external-search-service';
import {EAIService} from 'services/rest/eai-service';
import {Eventbus} from 'services/eventbus/eventbus';
import {DialogService} from 'components/dialog/dialog-service';
import {Configuration} from 'common/application-config';
import {NotificationService} from 'services/notification/notification-service';
import {ReloadSearchEvent} from 'external-search/actions/reload-search-event';
import {SearchMediator, EVENT_SEARCH} from 'search/search-mediator';
import {AdvancedSearchComponents} from 'search/components/advanced/advanced-search-components';

import {PromiseStub} from 'test/promise-stub';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {stub} from 'test/test-utils';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('ExternalSearch', () => {

  let externalSearch;
  beforeEach(() => {
    externalSearch = getComponentInstance();
  });

  function getComponentInstance() {
    var scope = mock$scope();
    var externalSearchService = mockExternalSearchService();
    var eaiService = mockEaiService();
    var translateService = IdocMocks.mockTranslateService();
    var promiseAdapter = PromiseAdapterMock.mockImmediateAdapter();
    var dialogService = stub(DialogService);
    var configuration = mockConfiguration();
    var actionsService = IdocMocks.mockActionsService();
    var eventbus = stub(Eventbus);
    var notificationService = stub(NotificationService);
    return new ExternalSearch(scope, externalSearchService, eaiService, translateService, promiseAdapter, dialogService, configuration, actionsService, eventbus, notificationService);
  }

  describe('configureMediator()', () => {
    it('should configure new search mediator different from the provisioned', () => {
      var mediator = new SearchMediator();
      externalSearch.config.searchMediator = mediator;
      externalSearch.ngOnInit();

      expect(externalSearch.searchMediator).to.exist;
      expect(externalSearch.searchMediator).to.not.equal(mediator);
    });

    it('should use another search service', () => {
      externalSearch.ngOnInit();
      expect(externalSearch.searchMediator.service).to.equal(externalSearch.externalSearchService);
    });
  });

  describe('configureSection()', () => {
    it('should construct a configuration for the embedded advanced search section', () => {
      externalSearch.ngOnInit();
      expect(externalSearch.sectionConfig).to.exist;
      expect(externalSearch.sectionConfig.searchMediator).to.equal(externalSearch.searchMediator);
      expect(externalSearch.sectionConfig.disabled).to.be.false;
    });

    it('should lock the advanced search components', () => {
      externalSearch.ngOnInit();
      expect(externalSearch.sectionConfig.locked).to.exist;
      expect(externalSearch.sectionConfig.locked).to.deep.equal(AdvancedSearchComponents.getAllComponents());
    });
  });

  describe('configurePagination()', () => {
    it('should construct a configuration object for the pagination component', () => {
      externalSearch.ngOnInit();
      expect(externalSearch.paginationConfig).to.exist;
      expect(externalSearch.paginationConfig.page).to.equal(1);
      expect(externalSearch.paginationConfig.pageSize).to.equal(24);
    });

    it('should define a pagination callback', () => {
      externalSearch.ngOnInit();
      expect(externalSearch.paginationCallback).to.exist;
    });
  });

  describe('loadSystems()', () => {
    it('should request the available external systems', () => {
      externalSearch.ngOnInit();
      expect(externalSearch.eaiService.getRegisteredSystems.calledOnce).to.be.true;
    });

    it('should construct a proper configuration for the systems select', () => {
      var expectedData = [{id: 'system1', text: 'system1'}, {id: 'system2', text: 'system2'}];
      externalSearch.ngOnInit();
      expect(externalSearch.systemsSelectConfig).to.exist;
      expect(externalSearch.systemsSelectConfig.multiple).to.be.false;
      expect(externalSearch.systemsSelectConfig.data).to.deep.equal(expectedData);
      expect(externalSearch.systemsSelectConfig.defaultValue).to.equal('system1');
    });

    it('should not assign any data if there are no systems', () => {
      externalSearch.eaiService.getRegisteredSystems = sinon.spy(() => {
        return PromiseStub.resolve({data: []});
      });
      externalSearch.ngOnInit();
      expect(externalSearch.systemsSelectConfig).to.exist;
      expect(externalSearch.systemsSelectConfig.data).to.deep.equal([]);
      expect(externalSearch.systemsSelectConfig.defaultValue).to.not.exist;
    });
  });

  describe('registerSystemsWatcher()', () => {
    it('should register a scope watcher for the current system', () => {
      externalSearch.$scope.$watch = sinon.spy();
      externalSearch.ngOnInit();
      expect(externalSearch.$scope.$watch.calledOnce).to.be.true;

      var watchFunction = externalSearch.$scope.$watch.getCall(0).args[0];
      externalSearch.system = 'system1';
      expect(watchFunction()).to.equal(externalSearch.system);
    });

    it('should not trigger system change if the system is not defined', () => {
      externalSearch.system = 'system1';
      externalSearch.ngOnInit();
      externalSearch.system = undefined;
      externalSearch.$scope.$digest();
      expect(externalSearch.eaiService.getModels.called).to.be.false;
    });

    it('should not trigger system change if the system is the same', () => {
      externalSearch.system = 'system1';
      externalSearch.ngOnInit();
      externalSearch.$scope.$digest();
      externalSearch.eaiService.getModels.reset();
      externalSearch.$scope.$digest();
      expect(externalSearch.eaiService.getModels.called).to.be.false;
    });

    it('should trigger system change if the new system is different from the old one', () => {
      externalSearch.system = 'system1';
      externalSearch.ngOnInit();
      externalSearch.$scope.$digest();
      externalSearch.eaiService.getModels.reset();
      externalSearch.system = 'system2';
      externalSearch.$scope.$digest();
      expect(externalSearch.eaiService.getModels.calledOnce).to.be.true;
      expect(externalSearch.eaiService.getModels.getCall(0).args[0]).to.equal(externalSearch.system);
    });

    it('should clear any results and selection if it should trigger a system change', () => {
      externalSearch.ngOnInit();
      externalSearch.system = 'system1';
      externalSearch.clearResults = sinon.spy();
      externalSearch.clearSelection = sinon.spy();
      externalSearch.$scope.$digest();
      expect(externalSearch.clearResults.calledOnce).to.be.true;
      expect(externalSearch.clearSelection.calledOnce).to.be.true;
    });
  });

  describe('onSystemChange(system)', () => {

    var system = 'system1';
    beforeEach(() => {
      externalSearch.ngOnInit();
    });

    it('should fetch related models for the given system and search configurations', () => {
      externalSearch.onSystemChange(system);
      expect(externalSearch.eaiService.getModels.calledOnce).to.be.true;
      expect(externalSearch.eaiService.getModels.getCall(0).args[0]).to.equal(system);
      expect(externalSearch.externalSearchService.getSystemConfiguration.calledOnce).to.be.true;
      expect(externalSearch.externalSearchService.getSystemConfiguration.getCall(0).args[0]).to.equal(system);
    });

    it('should not construct configurations if there are no models for the given system', () => {
      externalSearch.eaiService.getModels.returns(PromiseStub.resolve({data: []}));
      externalSearch.onSystemChange(system);

      expect(externalSearch.toolbarConfig).to.not.exist;
      expect(externalSearch.eaiService.getProperties.called).to.be.false;
      expect(externalSearch.sectionLoaders).to.not.exist;
    });

    it('should update the search toolbar configuration', () => {
      externalSearch.onSystemChange(system);
      expect(externalSearch.toolbarConfig).to.exist;
    });

    it('should fetch the properties for the first model type', () => {
      externalSearch.onSystemChange(system);
      expect(externalSearch.eaiService.getProperties.calledOnce).to.be.true;
      expect(externalSearch.eaiService.getProperties.getCall(0).args[0]).to.equal(system);
      expect(externalSearch.eaiService.getProperties.getCall(0).args[1]).to.equal(getModels()[0].id);
    });

    it('should construct default search criteria from the first model type properties', () => {
      expect(externalSearch.criteria).to.not.exist;
      externalSearch.onSystemChange(system);
      expect(externalSearch.criteria).to.exist;
    });

    it('should configure section loaders', () => {
      expect(externalSearch.sectionLoaders).to.not.exist;
      externalSearch.onSystemChange(system);
      expect(externalSearch.sectionLoaders).to.exist;
    });
  });

  describe('getSectionLoaders(system, models, firstTypeProperties)', () => {
    it('should create a loader for models types', () => {
      var models = getModels();
      var firstTypeProperties = getProperties();

      var loaders = externalSearch.getSectionLoaders('system1', models, firstTypeProperties);
      expect(loaders.models).to.exist;
      loaders.models().then((fetchedModels) => {
        expect(fetchedModels).to.deep.equal(models);
      });
    });

    it('should create a loader for given model type properties', () => {
      var models = getModels();
      var firstTypeProperties = getProperties();

      var loaders = externalSearch.getSectionLoaders('system1', models, firstTypeProperties);
      expect(loaders.properties).to.exist;

      loaders.properties(['ext:type2']);
      expect(externalSearch.eaiService.getProperties.calledOnce).to.be.true;
      expect(externalSearch.eaiService.getProperties.getCall(0).args[0]).to.equal('system1');
      expect(externalSearch.eaiService.getProperties.getCall(0).args[1]).to.equal('ext:type2');
    });

    it('should optimize the loading for the first model type properties', () => {
      var models = getModels();
      var firstTypeProperties = getProperties();

      var loaders = externalSearch.getSectionLoaders('system1', models, firstTypeProperties);
      expect(loaders.properties).to.exist;
      loaders.properties([models[0].id]).then((fetchedProperties) => {
        expect(fetchedProperties).to.deep.equal(firstTypeProperties);
      });
    });
  });

  describe('buildDefaultSearchCriteria(type, properties)', () => {
    it('should build correct search criteria', () => {
      var type = {id: 'ext:type'};
      var properties = getProperties();
      var criteria = externalSearch.buildDefaultSearchCriteria(type, properties);

      expect(criteria).to.exist;
      expect(criteria.rules).to.exist;

      var sectionCriteria = criteria.rules[0];
      expect(sectionCriteria).to.exist;
      expect(sectionCriteria.rules).to.exist;

      var typeCriteria = sectionCriteria.rules[0];
      expect(typeCriteria).to.exist;
      expect(typeCriteria.value).to.deep.equal([type.id]);

      var innerSectionCriteria = sectionCriteria.rules[1];
      expect(innerSectionCriteria).to.exist;
      expect(innerSectionCriteria.rules).to.exist;
      expect(innerSectionCriteria.rules.length).to.equal(3);

      var firstPropertyRule = innerSectionCriteria.rules[0];
      expect(firstPropertyRule.field).to.equal(properties[0].id);
      expect(firstPropertyRule.type).to.equal(properties[0].type);
      expect(firstPropertyRule.operator).to.equal(properties[0].operators[0]);

      var secondPropertyRule = innerSectionCriteria.rules[1];
      expect(secondPropertyRule.field).to.equal(properties[1].id);
      expect(secondPropertyRule.type).to.equal(properties[1].type);
      expect(secondPropertyRule.operator).to.equal(properties[1].operators[0]);

      var thirdPropertyRule = innerSectionCriteria.rules[2];
      expect(thirdPropertyRule.field).to.equal(properties[2].id);
      expect(thirdPropertyRule.type).to.equal(properties[2].type);
      expect(thirdPropertyRule.operator).to.not.exist;
    });
  });

  describe('updateSearchToolbar(searchConfig)', () => {
    it('should not update the search toolbar if the search configuration has no order config', () => {
      externalSearch.updateSearchToolbar();
      expect(externalSearch.toolbarConfig).to.not.exist;
    });

    it('should update the search toolbar if the search configuration has an order config', () => {
      externalSearch.ngOnInit();
      externalSearch.updateSearchToolbar(getSearchConfiguration());
      expect(externalSearch.toolbarConfig).to.exist;
      expect(externalSearch.toolbarCallback).to.exist;
      expect(externalSearch.currentOrderBy).to.equal('ext:property');
      expect(externalSearch.currentOrderDirection).to.equal(DEFAULT_ORDER_DIRECTION);
    });
  });

  describe('search()', () => {
    it('should reset and assign correct search arguments', () => {
      externalSearch.ngOnInit();
      externalSearch.system = 'system2';
      externalSearch.currentOrderBy = 'ext:property';
      externalSearch.currentOrderDirection = 'up';
      externalSearch.searchMediator.arguments = {};
      externalSearch.searchMediator.search = sinon.spy(() => {
        return PromiseStub.resolve({response: {data: {}}});
      });

      externalSearch.search();
      expect(externalSearch.searchMediator.arguments.context).to.equal('system2');
      expect(externalSearch.searchMediator.arguments.pageNumber).to.equal(1);
      expect(externalSearch.searchMediator.arguments.pageSize).to.equal(24);
      expect(externalSearch.searchMediator.arguments.orderBy).to.equal('ext:property');
      expect(externalSearch.searchMediator.arguments.orderDirection).to.equal('up');
      expect(externalSearch.searchMediator.search.calledOnce).to.be.true;
      expect(externalSearch.searchMediator.search.getCall(0).args[0]).to.be.true;
    });

    it('should display a notification if there are no results', () => {
      externalSearch.ngOnInit();
      externalSearch.searchMediator.search = sinon.spy(() => {
        return PromiseStub.resolve({response: {data: {resultSize: 0}}});
      });

      expect(externalSearch.notificationService.info.called).to.be.false;
      externalSearch.search();
      expect(externalSearch.notificationService.info.calledOnce).to.be.true;
    });
  });

  describe('clear()', () => {
    it('should reset the system', () => {
      externalSearch.ngOnInit();
      externalSearch.system = 'system1';
      externalSearch.clearResults = sinon.spy();
      externalSearch.clear();
      expect(externalSearch.eaiService.getModels.calledOnce).to.be.true;
      expect(externalSearch.eaiService.getModels.getCall(0).args[0]).to.equal(externalSearch.system);
    });

    it('should clear results and selection', () => {
      externalSearch.ngOnInit();
      externalSearch.clearResults = sinon.spy();
      externalSearch.config.results.config.selectedItems = [{id: 'emf:123'}];
      externalSearch.clear();
      expect(externalSearch.clearResults.calledOnce).to.be.true;
      expect(externalSearch.config.results.config.selectedItems).to.deep.equal([]);
    });
  });

  describe('clearResults()', () => {
    it('should trigger an after search event with empty response', () => {
      externalSearch.ngOnInit();
      externalSearch.searchMediator.trigger = sinon.spy();
      externalSearch.clearResults();
      expect(externalSearch.searchMediator.trigger.calledOnce).to.be.true;
      expect(externalSearch.searchMediator.trigger.getCall(0).args[0]).to.equal(EVENT_SEARCH);
      var response = externalSearch.searchMediator.trigger.getCall(0).args[1];
      expect(response).to.exist;
      expect(response.response).to.exist;
      expect(response.response.data).to.exist;
      expect(response.response.data.values).to.deep.equal([]);
    });
  });

  describe('ngOnDestroy()', () => {
    it('should unsubscribe from any event subscription', () => {
      var unsubscribeSpy = sinon.spy();
      externalSearch.events = [{
        unsubscribe: () => unsubscribeSpy()
      }];
      externalSearch.ngOnDestroy();
      expect(unsubscribeSpy.calledOnce).to.be.true;
    });
  });

  it('should set lastSearchArguments param during search', () => {
    externalSearch.ngOnInit();
    let expected = {test: "value"};
    externalSearch.searchMediator.arguments = expected;
    externalSearch.searchMediator.trigger(EVENT_SEARCH, {response: {data: {}}});
    expect(externalSearch.lastSearchArguments).to.equal(expected);
  });

  it('should trigger search on ReloadSearchEvent with the last search arguments if present', () => {
    externalSearch.ngOnInit();
    expect(externalSearch.events).to.exist;
    expect(externalSearch.eventbus.subscribe.calledOnce).to.be.true;

    externalSearch.lastSearchArguments = {context: 'system1'};
    externalSearch.searchMediator.search = sinon.spy(() => {
      return PromiseStub.resolve({response: {data: {}}});
    });

    var onReloadHandler = externalSearch.eventbus.subscribe.getCall(0).args[1];
    onReloadHandler();
    expect(externalSearch.searchMediator.search.calledOnce).to.be.true;
    expect(externalSearch.searchMediator.arguments).to.deep.equal(externalSearch.lastSearchArguments);
  });

  function mockTimeout() {
    return sinon.spy((func) => {
      func();
    });
  }

  function mockExternalSearchService() {
    var externalSearchService = stub(ExternalSearchService);
    externalSearchService.getSystemConfiguration.returns(PromiseStub.resolve({
      data: getSearchConfiguration()
    }));
    return externalSearchService;
  }

  function mockEaiService() {
    var service = stub(EAIService);
    service.getRegisteredSystems.returns(PromiseStub.resolve({data: ['system1', 'system2']}));
    service.getModels.returns(PromiseStub.resolve({data: getModels()}));
    service.getProperties.returns(PromiseStub.resolve({data: getProperties()}));
    return service;
  }

  function mockConfiguration() {
    var service = stub(Configuration);
    service.get.returns(24);
    return service;
  }

  function getModels() {
    return [{
      id: 'ext:type',
      text: 'Type'
    }];
  }

  function getProperties() {
    return [{
      id: 'ext:prop1',
      text: 'Prop1',
      type: 'type1',
      operators: ['contains']
    }, {
      id: 'ext:prop2',
      text: 'Prop2',
      type: 'type2',
      operators: ['equals', 'less_than']
    },{
      id: 'ext:prop3',
      text: 'Prop3',
      type: 'type3'
    }];
  }

  function getSearchConfiguration() {
    return {
      order: {
        default: 'ext:property',
        properties: [{id: 'ext:property', text: 'Prop'}]
      }
    };
  }

});
