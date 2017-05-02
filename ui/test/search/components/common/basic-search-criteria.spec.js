import {BasicSearchCriteria, FIELDS} from 'search/components/common/basic-search-criteria';
import {SearchMediator} from 'search/search-mediator';
import {InstanceObject} from 'idoc/idoc-context';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {CRITERIA_READY_EVENT} from 'search/components/common/search-criteria-component';
import {CURRENT_OBJECT} from 'search/resolvers/contextual-rules-resolver';
import {KEY_ENTER} from 'common/keys';
import _ from 'lodash';

import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {PromiseStub} from 'test/promise-stub';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('BasicSearchCriteria', () => {

  var basicSearchCriteria;
  var configureDatePickerStub;
  beforeEach(() => {
    // Defaults
    var queryBuilder = new QueryBuilder({});
    var mediator = new SearchMediator({}, queryBuilder);
    BasicSearchCriteria.prototype.config = {
      searchMediator: mediator
    };

    var scope = mock$scope();
    var element = mockElement();
    var translateService = mockTranslateService();
    var configuration = mockConfiguration();

    basicSearchCriteria = new BasicSearchCriteria(scope, element, translateService, configuration);
    configureDatePickerStub = sinon.stub(basicSearchCriteria, 'configureDatePickers', () => {
    });
  });

  afterEach(() => {
    configureDatePickerStub.reset();
    // Fixes scope issues in Karma
    BasicSearchCriteria.prototype.config = undefined;
    BasicSearchCriteria.prototype.context = undefined;
  });

  describe('initialize()', () => {
    it('should finally delegate to afterInit() to notify it is ready', () => {
      basicSearchCriteria.config.searchMediator.trigger = sinon.spy();
      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.config.searchMediator.trigger.calledOnce).to.be.true;
      expect(basicSearchCriteria.config.searchMediator.trigger.getCall(0).args[0]).to.equal(CRITERIA_READY_EVENT);
    });

    it('should retrieve the current object if there is context and should use the root context', () => {
      basicSearchCriteria.currentObject = undefined;
      basicSearchCriteria.context = mockContext([{id: '1'}]);
      basicSearchCriteria.config.useRootContext = true;
      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.currentObject).to.exist;
      expect(basicSearchCriteria.currentObject.getId()).to.equal('1');
    });
  });

  describe('setCriteria', () => {
    it('should initialize the criteria tree if it is undefined', () => {
      basicSearchCriteria.config.searchMediator.queryBuilder.tree = undefined;
      basicSearchCriteria.initialize();

      var tree = basicSearchCriteria.config.searchMediator.queryBuilder.tree;
      expect(tree).to.exist;
      expect(tree.rules).to.exist;
    });

    it('should initialize the criteria tree if it is empty', () => {
      var criteria = {};
      setCriteria(criteria);

      basicSearchCriteria.initialize();
      expect(criteria.rules).to.exist;
      expect(criteria.rules.length).to.equal(1);
      expect(criteria.rules[0].rules).to.exist;
    });

    it('should assign a criteria rule mapping if the tree is empty', () => {
      setCriteria({});
      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.criteriaMapping).to.exist;
      expect(Object.keys(basicSearchCriteria.criteriaMapping)).to.deep.equal(FIELDS);
    });

    it('should not assign criteria with default location CURRENT_OBJECT if there is no context', () => {
      setCriteria({});
      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.criteriaMapping.context.value).to.deep.equal([]);
    });

    it('should assign criteria with default location CURRENT_OBJECT if the criteria is not configured', () => {
      setCriteria({});
      basicSearchCriteria.context = {};

      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.criteriaMapping.context.value).to.deep.equal([CURRENT_OBJECT]);
    });

    it('should assign criteria with root location of the current context object if useRootContext property is specified', () => {
      setCriteria({});
      basicSearchCriteria.config.useRootContext = true;
      basicSearchCriteria.context = mockContext([{
        'id': 'child'
      }, {
        'id': 'parent'
      }, {
        'id': 'root'
      }]);

      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.criteriaMapping.context.value).to.deep.equal(['root']);
    });

    it('should assign no criteria when no root context exits and if useRootContext property is specified', () => {
      setCriteria({});
      basicSearchCriteria.config.useRootContext = true;
      basicSearchCriteria.context = mockContext([{
        'id': 'child'
      }]);

      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.criteriaMapping.context.value).to.deep.equal([]);
    });

    it('should add rule with default location CURRENT_OBJECT if there is a criteria with empty rules', () => {
      setCriteria({
        rules: []
      });
      basicSearchCriteria.context = {};

      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.criteriaMapping.context.value).to.deep.equal([CURRENT_OBJECT]);
    });

    it('should not add a rule with default location CURRENT_OBJECT if there is a configured criteria with rules', () => {
      basicSearchCriteria.context = {};

      var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      setCriteria(tree);

      basicSearchCriteria.initialize();

      expect(basicSearchCriteria.criteriaMapping.context).to.exist;
      expect(basicSearchCriteria.criteriaMapping.context.value).to.deep.equal([]);
    });

    it('should also add any configured predefined types if the rules are empty', () => {
      setCriteria({});
      basicSearchCriteria.config.predefinedTypes = ['emf:HashTag', 'emf:Quality'];

      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.criteriaMapping.types.value).to.deep.equal(['emf:HashTag', 'emf:Quality']);
    });

    it('should not add any configured predefined types if the provided config property is empty', () => {
      setCriteria({});
      basicSearchCriteria.config.predefinedTypes = [];

      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.criteriaMapping.types.value).to.deep.equal([]);
    });

    it('should populate the external model if it was empty', () => {
      setCriteria({});
      basicSearchCriteria.config.predefinedTypes = ['emf:HashTag', 'emf:Quality'];

      basicSearchCriteria.initialize();

      var tree = basicSearchCriteria.config.searchMediator.queryBuilder.tree;
      expect(tree.rules[0].rules[0].value).to.deep.equal(['emf:HashTag', 'emf:Quality']);
    });

    it('should populate the internal model if the external is not empty', () => {
      var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      tree.rules[0].rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule());
      tree.rules[0].rules[0].value = ['emf:Document'];
      tree.rules[0].rules.push(SearchCriteriaUtils.buildCondition());
      tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('freeText', '', '', '123'));
      setCriteria(tree);

      basicSearchCriteria.initialize();

      expect(basicSearchCriteria.criteriaMapping.types.value).to.deep.equal(['emf:Document']);
      expect(basicSearchCriteria.criteriaMapping.freeText.value).to.deep.equal('123');
    });
  });

  describe('registerCriteriaModelWatchers()', () => {
    it('should register watchers', () => {
      basicSearchCriteria.$scope.$watch = sinon.spy();
      basicSearchCriteria.registerCriteriaModelWatchers();
      expect(basicSearchCriteria.$scope.$watch.callCount).to.equal(2);
    });

    it('should set the criteria if the external model reference is changed', () => {
      // Initial digest so it could be compared later
      basicSearchCriteria.$scope.$digest();

      basicSearchCriteria.config.searchMediator.queryBuilder.tree = {};
      basicSearchCriteria.setCriteria = sinon.spy();

      basicSearchCriteria.$scope.$digest();
      expect(basicSearchCriteria.setCriteria.calledOnce).to.be.true;
    });

    it('should not set the criteria if the external model reference is not changed', () => {
      // Initial digest so it could be compared later
      basicSearchCriteria.$scope.$digest();

      basicSearchCriteria.setCriteria = sinon.spy();

      basicSearchCriteria.$scope.$digest();
      expect(basicSearchCriteria.setCriteria.calledOnce).to.be.false;
    });
  });

  describe('compareMappings()', () => {
    it('should tell if the mappings are equal', () => {
      var firstMap = {
        property: {value: ''}
      };
      var secondMap = {
        property: {value: ''}
      };
      expect(basicSearchCriteria.compareMappings(firstMap, secondMap)).to.be.true;

      firstMap.property.value = [];
      secondMap.property.value = [];
      expect(basicSearchCriteria.compareMappings(firstMap, secondMap)).to.be.true;

      firstMap.property.value = null;
      secondMap.property.value = [];
      expect(basicSearchCriteria.compareMappings(firstMap, secondMap)).to.be.true;
      expect(basicSearchCriteria.compareMappings(secondMap, firstMap)).to.be.true;
    });

    it('should tell if the mappings are not equal', ()=> {
      var firstMap = {
        property: {value: ''}
      };
      var secondMap = {
        property: {value: '1'}
      };
      expect(basicSearchCriteria.compareMappings(firstMap, secondMap)).to.be.false;

      firstMap.property.value = [];
      secondMap.property.value = ['1'];
      expect(basicSearchCriteria.compareMappings(firstMap, secondMap)).to.be.false;
    });
  });

  describe('getCriteriaMapping()', () => {
    it('should construct correct mapping', () => {
      var mapping = BasicSearchCriteria.getCriteriaMapping();
      expect(mapping.freeText.value).to.equal('');
      expect(mapping.createdFromDate.value).to.equal('');
      expect(mapping.createdToDate.value).to.equal('');
      expect(mapping.context.value).to.deep.equal([]);
      expect(mapping.relationships.value).to.deep.equal([]);
      expect(mapping.types.value).to.deep.equal([]);
      expect(mapping.createdBy.value).to.deep.equal([]);
    });
  });

  describe('configure()', () => {
    it('should construct a configuration object for the types select', () => {
      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.typesConfig).to.exist;
    });

    it('should construct a configuration object for the types select with a class filter', () => {
      basicSearchCriteria.config.predefinedTypes = ['emf:Document'];
      basicSearchCriteria.initialize();
      expect(basicSearchCriteria.typesConfig.classFilter).to.exist;
      expect(basicSearchCriteria.typesConfig.classFilter).to.deep.equal(['emf:Document']);
    });

    it('should construct a configuration object for the relationships select', () => {
      basicSearchCriteria.initialize();
      expectSelectConfig(basicSearchCriteria.relationshipsConfig);
    });

    it('should construct a configuration object for the context select', () => {
      basicSearchCriteria.initialize();
      expectSelectConfig(basicSearchCriteria.contextConfig);
    });

    it('should construct a configuration object for the context select with predefined types', () => {
      basicSearchCriteria.config.contextualItems = ['emf:Document'];
      basicSearchCriteria.initialize();
      expectSelectConfig(basicSearchCriteria.contextConfig);
      expect(basicSearchCriteria.contextConfig.predefinedItems).to.deep.equal(['emf:Document']);
    });

    it('should construct a configuration object for the created by select', () => {
      basicSearchCriteria.initialize();
      expectSelectConfig(basicSearchCriteria.userSelectConfig);
    });

    function expectSelectConfig(config) {
      expect(config).to.exist;
      expect(config.multiple).to.be.true;
      expect(config.placeholder).to.equal('translated');
    }
  });

  describe('configureDatePickers()', () => {

    beforeEach(() => {
      basicSearchCriteria.criteriaMapping.createdFromDate = {
        value: 'yesterday'
      };
      basicSearchCriteria.criteriaMapping.createdToDate = {
        value: 'today'
      };
      configureDatePickerStub.restore();
    });

    it('should construct a date picker configuration', () => {
      basicSearchCriteria.createdOnConfigs = undefined;
      basicSearchCriteria.configureDatePickers();

      expect(basicSearchCriteria.createdOnConfigs).to.exist;
      expectDateConfig(basicSearchCriteria.createdOnConfigs.to);
      expectDateConfig(basicSearchCriteria.createdOnConfigs.from);
    });

    it('should construct a date picker configuration with default values', () => {
      basicSearchCriteria.createdOnConfigs = undefined;
      basicSearchCriteria.configureDatePickers();

      expect(basicSearchCriteria.createdOnConfigs.from.defaultValue).to.equal('yesterday');
      expect(basicSearchCriteria.createdOnConfigs.to.defaultValue).to.equal('today');
    });

    function expectDateConfig(config) {
      expect(config).to.exist;
      expect(config.hideTime).to.be.true;
      expect(config.useCurrent).to.be.false;
      expect(config.placeholder).to.exist;
      expect(config.dateFormat).to.equal('configuration');
    }
  });

  describe('onFtsKeyPress()', () => {
    it('should initiate a search if the pressed key is Enter', () => {
      basicSearchCriteria.search = sinon.spy();
      basicSearchCriteria.onFtsKeyPress({
        keyCode: KEY_ENTER
      });
      expect(basicSearchCriteria.search.calledOnce).to.be.true;
    });

    it('should not initiate a search if the pressed key is not Enter', () => {
      basicSearchCriteria.search = sinon.spy();
      basicSearchCriteria.onFtsKeyPress({
        keyCode: KEY_ENTER + 1
      });
      expect(basicSearchCriteria.search.called).to.be.false;
    });
  });

  describe('clear()', () => {
    it('should clear the criteria', () => {
      var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      tree.rules[0].rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule());
      tree.rules[0].rules.push(SearchCriteriaUtils.buildCondition());
      tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('test'));
      setCriteria(tree);

      basicSearchCriteria.clear();

      //Tree rules should be reset
      expect(tree.rules[0].rules[1].rules.length).to.equal(0);
    });

    it('should delegate to clear the search results', () => {
      basicSearchCriteria.clearResults = sinon.spy();
      basicSearchCriteria.clear();
      expect(basicSearchCriteria.clearResults.calledOnce).to.be.true;
    });

    it('should reset all date fields', () => {
      var spy = sinon.spy();
      basicSearchCriteria.$element = mockElement(spy);
      basicSearchCriteria.clear();
      expect(spy.calledTwice).to.be.true;
    });
  });

  function setCriteria(tree) {
    basicSearchCriteria.config.searchMediator.queryBuilder.init(tree);
  }

  function mockElement(spy) {
    return {
      find: sinon.spy(()=> {
        return {
          data: () => {
            return {
              viewDate: spy || function () {
                return '';
              }
            };
          }
        };
      })
    };
  }

  function mockTranslateService() {
    return {
      translateInstant: sinon.spy(() => {
        return 'translated';
      })
    };
  }

  function mockConfiguration() {
    return {
      get: sinon.spy(() => {
        return 'configuration';
      })
    };
  }

  function mockContext(context) {
    return {
      getCurrentObject: sinon.spy(() => {
        let instance = new InstanceObject(context[0].id);
        instance.setContextPath(context);
        return PromiseStub.resolve(instance);
      })
    }
  }

});