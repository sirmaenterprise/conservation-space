import {
  AdvancedSearchCriteriaRow,
  ADVANCED_CRITERIA_EXTENSION_POINT
} from 'search/components/advanced/advanced-search-criteria-row';
import {SearchCriteriaUtils, ANY_RELATION} from 'search/utils/search-criteria-utils';
import {AdvancedSearchComponents} from 'search/components/advanced/advanced-search-components';
import {SearchMediator} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import _ from 'lodash';

import {AdvancedSearchMocks} from './advanced-search-mocks'
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';

describe('AdvancedSearchCriteriaRow', () => {

  var advancedSearchCriteriaRow;
  var criteria;

  beforeEach(() => {
    criteria = AdvancedSearchMocks.getCriteria();

    advancedSearchCriteriaRow = new AdvancedSearchCriteriaRow(mock$scope());
    advancedSearchCriteriaRow.criteria = criteria;
    advancedSearchCriteriaRow.config.searchMediator = new SearchMediator({}, new QueryBuilder(criteria));
    advancedSearchCriteriaRow.advancedSearchFilterExecutor = {
      filterOperators: (config, property, options) => {
        return {then: (cb) => cb(options)};
      },
      filterProperties: (config, criteria, properties) => {
        return {then: (cb) => cb(properties)};
      },
    };
    advancedSearchCriteriaRow.ngOnInit();
  });

  it('should configure the component default configuration', () => {
    expect(advancedSearchCriteriaRow.config.disabled).to.be.false;
    expect(advancedSearchCriteriaRow.config.renderRemoveButton).to.be.true;
  });

  it('should construct a configuration for an object property select on init', () => {
    expect(advancedSearchCriteriaRow.propertySelectConfig).to.exist;
    expect(advancedSearchCriteriaRow.propertySelectConfig.selectOnClose).to.be.true;
  });

  describe('createPropertySelectConfig', () => {
    it('should construct the object property select to be a single select', () => {
      expect(advancedSearchCriteriaRow.propertySelectConfig.multiple).to.be.false;
    });

    it('should construct the configuration for the object property select with the provided properties as data', () => {
      var data = getProperties();
      advancedSearchCriteriaRow.properties = data;
      advancedSearchCriteriaRow.createPropertySelectConfig();
      expect(advancedSearchCriteriaRow.propertySelectConfig.data).to.deep.equal(data);
    });

    it('should configure the select with a function to determine its disabled state', () => {
      expect(advancedSearchCriteriaRow.propertySelectConfig.isDisabled()).to.be.false;
      advancedSearchCriteriaRow.config.disabled = true;
      expect(advancedSearchCriteriaRow.propertySelectConfig.isDisabled()).to.be.true;
    });

    it('should configure the select to have a default value', () => {
      advancedSearchCriteriaRow.criteria.field = 'title';
      advancedSearchCriteriaRow.createPropertySelectConfig();
      expect(advancedSearchCriteriaRow.propertySelectConfig.defaultValue).to.equal('title');
    });

    it('should properly filter out properties when creating select config', () => {
      var data = getProperties();
      advancedSearchCriteriaRow.properties = data;
      advancedSearchCriteriaRow.advancedSearchFilterExecutor.filterProperties = () => {
        return PromiseStub.resolve([{id: 1}, {id: 2}, {id: 3}]);
      };
      advancedSearchCriteriaRow.createPropertySelectConfig();
      expect(advancedSearchCriteriaRow.propertySelectConfig.data).to.deep.equal([{id: 1}, {id: 2}, {id: 3}]);
    });
  });

  describe('component events', () => {
    it('should call component event when value has changed', () => {
      advancedSearchCriteriaRow.onValueChange = sinon.spy();
      advancedSearchCriteriaRow.onChangedValue();
      expect(advancedSearchCriteriaRow.onValueChange.calledOnce).to.be.true;
    });

    it('should call component event when property has changed', () => {
      advancedSearchCriteriaRow.onFieldChange = sinon.spy();
      advancedSearchCriteriaRow.onChangedField();
      expect(advancedSearchCriteriaRow.onFieldChange.calledOnce).to.be.true;
    });

    it('should call component event when operator has changed', () => {
      advancedSearchCriteriaRow.onOperatorChange = sinon.spy();
      advancedSearchCriteriaRow.onChangedOperator();
      expect(advancedSearchCriteriaRow.onOperatorChange.calledOnce).to.be.true;
    });
  });

  describe('registerModelWatchers()', () => {
    it('should register watchers for the criteria field', () => {
      // setting a rule
      advancedSearchCriteriaRow.criteria = criteria.rules[0];
      advancedSearchCriteriaRow.$scope.$watch = sinon.spy();
      advancedSearchCriteriaRow.registerModelWatchers();
      expect(advancedSearchCriteriaRow.$scope.$watch.calledOnce).to.be.true
    });

    it('should call onPropertyChange() when the field is changed', () => {
      advancedSearchCriteriaRow.criteria = criteria.rules[0];
      advancedSearchCriteriaRow.criteria.field = 'new field';
      advancedSearchCriteriaRow.onPropertyChange = sinon.spy();
      advancedSearchCriteriaRow.registerModelWatchers();
      advancedSearchCriteriaRow.$scope.$digest();
      expect(advancedSearchCriteriaRow.onPropertyChange.called).to.be.true;
    });

    it('should not call onPropertyChange() when the field is undefined', () => {
      advancedSearchCriteriaRow.criteria = criteria.rules[0];
      advancedSearchCriteriaRow.criteria.field = undefined;
      advancedSearchCriteriaRow.onPropertyChange = sinon.spy();
      advancedSearchCriteriaRow.registerModelWatchers();
      advancedSearchCriteriaRow.$scope.$digest();
      expect(advancedSearchCriteriaRow.onPropertyChange.called).to.be.false;
    });

    it('should not call onPropertyChange() when the field is empty', () => {
      advancedSearchCriteriaRow.criteria = criteria.rules[0];
      advancedSearchCriteriaRow.criteria.field = '';
      advancedSearchCriteriaRow.onPropertyChange = sinon.spy();
      advancedSearchCriteriaRow.registerModelWatchers();
      advancedSearchCriteriaRow.$scope.$digest();
      expect(advancedSearchCriteriaRow.onPropertyChange.called).to.be.false;
    });
  });

  describe('remove()', () => {
    it('should remove a single rule from the provided criteria', () => {
      var ruleForRemoval = criteria.rules[0];
      advancedSearchCriteriaRow.criteria = ruleForRemoval;

      var expected = _.cloneDeep(criteria);
      expected.rules.splice(0, 1);

      advancedSearchCriteriaRow.remove();
      expect(criteria).to.deep.equal(expected);
    });
  });

  describe('onPropertyChange()', () => {

    beforeEach(() => {
      var properties = getProperties();
      advancedSearchCriteriaRow.properties = getProperties();
      // title rule
      advancedSearchCriteriaRow.criteria = criteria.rules[0];
      advancedSearchCriteriaRow.getCriteriaExtension = sinon.spy(() => {
        // Resolve immediately
        return PromiseStub.resolve(getStringExtension());
      });
      advancedSearchCriteriaRow.translateService = getTranslateServiceMock();
      advancedSearchCriteriaRow.$compile = mockCompile();
      advancedSearchCriteriaRow.$element = mockElement();
    });

    it('should update the type value of the current rule', () => {
      advancedSearchCriteriaRow.onPropertyChange();
      expect(advancedSearchCriteriaRow.criteria.type).to.equal('string');
    });

    it('should delegate getting an extension for the criteria rule type', () => {
      advancedSearchCriteriaRow.onPropertyChange();
      expect(advancedSearchCriteriaRow.getCriteriaExtension.called).to.be.true;
      expect(advancedSearchCriteriaRow.getCriteriaExtension.getCall(0).args[0]).to.equal('string');
    });

    it('should delegate to translate the operators', () => {
      advancedSearchCriteriaRow.translateOperators = sinon.spy();
      advancedSearchCriteriaRow.extensionType = 'test-type';
      advancedSearchCriteriaRow.onPropertyChange();
      expect(advancedSearchCriteriaRow.translateOperators.called).to.be.true;
    });

    it('should update the operators if the new extension type is different from the old one', () => {
      advancedSearchCriteriaRow.updateOperatorsSelect = sinon.spy();
      advancedSearchCriteriaRow.extensionType = 'test-type';
      advancedSearchCriteriaRow.onPropertyChange();
      expect(advancedSearchCriteriaRow.updateOperatorsSelect.called).to.be.true;
      expect(advancedSearchCriteriaRow.extensionType).to.equal('string');
    });

    it('should update the operators if the extension type is the same but they differ from before', () => {
      advancedSearchCriteriaRow.updateOperatorsSelect = sinon.spy();
      //advancedSearchCriteriaRow.extensionType = 'test-type';
      advancedSearchCriteriaRow.onPropertyChange();
      advancedSearchCriteriaRow.advancedSearchFilterExecutor.filterOperators = () => {
        return PromiseStub.resolve([{id: 'new', text: 'different'}]);
      };
      expect(advancedSearchCriteriaRow.updateOperatorsSelect.called).to.be.true;
      expect(advancedSearchCriteriaRow.extensionType).to.equal('string');
    });

    it('should not update the operators if the new extension type is the same as the old one', () => {
      advancedSearchCriteriaRow.updateOperatorsSelect = sinon.spy();
      advancedSearchCriteriaRow.extensionType = 'string';
      advancedSearchCriteriaRow.onPropertyChange();
      expect(advancedSearchCriteriaRow.updateOperatorsSelect.called).to.be.false;
    });

    it('should delegate to rendering the extension for the given criteria type', () => {
      advancedSearchCriteriaRow.compileExtension = sinon.spy();
      advancedSearchCriteriaRow.onPropertyChange();
      expect(advancedSearchCriteriaRow.compileExtension.called).to.be.true;
    });

    it('should reset the criteria value if the new property is different from the old one', () => {
      // Current/new property is title
      advancedSearchCriteriaRow.onPropertyChange('status');
      expect(advancedSearchCriteriaRow.criteria.value).to.equal('');
    });

    it('should not reset the criteria value if the old property is the same as the new one', () => {
      // Current/new property is title
      advancedSearchCriteriaRow.onPropertyChange('title');
      expect(advancedSearchCriteriaRow.criteria.value).to.deep.equal(['1', '2']);
    });

    it('should not reset the criteria value if the old property is blank', () => {
      // Current/new property is title
      advancedSearchCriteriaRow.onPropertyChange('');
      expect(advancedSearchCriteriaRow.criteria.value).to.deep.equal(['1', '2']);
    });

    it('should not reset the criteria value if the old property is empty', () => {
      // Current/new property is title
      advancedSearchCriteriaRow.onPropertyChange();
      expect(advancedSearchCriteriaRow.criteria.value).to.deep.equal(['1', '2']);
    });
  });

  describe('getCriteriaExtension()', () => {
    it('should get the extension for the given criteria type', (done) => {
      var stringExtension = getStringExtension();
      advancedSearchCriteriaRow.pluginsService = {
        loadComponentModules: sinon.spy(() => {
          return Promise.resolve({
            'string': stringExtension
          });
        })
      };

      advancedSearchCriteriaRow.getCriteriaExtension('string').then((extension) => {
        var pluginsSpy = advancedSearchCriteriaRow.pluginsService.loadComponentModules;
        expect(pluginsSpy.called).to.be.true;
        expect(pluginsSpy.getCall(0).args[0]).to.equal(ADVANCED_CRITERIA_EXTENSION_POINT);
        expect(pluginsSpy.getCall(0).args[1]).to.equal('type');
        expect(extension).to.deep.equal(stringExtension);
        done();
      }).catch(done);
    });

    it('should return the string criteria extension by default if no other is available for the given type', (done) => {
      var stringExtension = getStringExtension();
      advancedSearchCriteriaRow.pluginsService = {
        loadComponentModules: sinon.spy(() => {
          return Promise.resolve({
            'string': stringExtension
          });
        })
      };

      advancedSearchCriteriaRow.getCriteriaExtension('non-existing-type').then((extension) => {
        var pluginsSpy = advancedSearchCriteriaRow.pluginsService.loadComponentModules;
        expect(extension).to.deep.equal(stringExtension);
        done();
      }).catch(done);
    });
  });

  describe('translateOperators()', () => {
    it('should translate the operators of the given extension', () => {
      var translateService = getTranslateServiceMock();
      advancedSearchCriteriaRow.translateService = translateService;

      var stringExtension = getStringExtension();
      var expected = [{
        id: 'contains',
        text: 'translation'
      }, {
        id: 'very_specific_operator',
        text: 'translation'
      }];

      var translated = advancedSearchCriteriaRow.translateOperators(stringExtension);
      expect(translated).to.deep.equal(expected);
    });

    it('should not fail if the extension has no operators', () => {
      var translated = advancedSearchCriteriaRow.translateOperators({});
      expect(translated).to.deep.equal([]);
    });
  });

  describe('updateOperatorsSelect()', () => {

    it('should construct a select configuration with the provided operators', () => {
      var operators = getOperators();
      advancedSearchCriteriaRow.updateOperatorsSelect(operators);
      expect(advancedSearchCriteriaRow.operatorSelectConfig).to.exist;
      expect(advancedSearchCriteriaRow.operatorSelectConfig.multiple).to.be.false;
      expect(advancedSearchCriteriaRow.operatorSelectConfig.data).to.deep.equals(operators);
      expect(advancedSearchCriteriaRow.operatorSelectConfig.selectOnClose).to.be.true;
    });

    it('should configure the select with a function to determine its disabled state', () => {
      advancedSearchCriteriaRow.updateOperatorsSelect(getOperators());
      expect(advancedSearchCriteriaRow.operatorSelectConfig.isDisabled()).to.be.false;
      advancedSearchCriteriaRow.config.disabled = true;
      expect(advancedSearchCriteriaRow.operatorSelectConfig.isDisabled()).to.be.true;
    });

    it('should construct the select configuration to have a default value', () => {
      advancedSearchCriteriaRow.criteria.operator = 'equals';
      advancedSearchCriteriaRow.updateOperatorsSelect(getOperators());
      expect(advancedSearchCriteriaRow.operatorSelectConfig.defaultValue).to.equal('equals');
    });

    it('should delete the select configuration if it existed before creating it', () => {
      var operators = getOperators();
      var createConfigFunction;
      var timeout = (fun) => {
        createConfigFunction = fun;
      };
      advancedSearchCriteriaRow.$timeout = timeout;
      advancedSearchCriteriaRow.operatorSelectConfig = {};

      advancedSearchCriteriaRow.updateOperatorsSelect(operators);
      expect(advancedSearchCriteriaRow.operatorSelectConfig).to.not.exist;
      // Invoking the function passed to the timeout
      createConfigFunction();
      expect(advancedSearchCriteriaRow.operatorSelectConfig).to.exist;
    });

    it('should preserve the previous operator if it exist in the new operators', () => {
      advancedSearchCriteriaRow.$timeout = () => {
      };
      advancedSearchCriteriaRow.operatorSelectConfig = {};
      advancedSearchCriteriaRow.criteria.operator = 'equals';

      advancedSearchCriteriaRow.updateOperatorsSelect(getOperators());

      expect(advancedSearchCriteriaRow.criteria.operator).to.equal('equals');
    });

    it('should not preserve the previous operator if it does not exist in the new operators', () => {
      advancedSearchCriteriaRow.$timeout = () => {
      };
      advancedSearchCriteriaRow.operatorSelectConfig = {};
      advancedSearchCriteriaRow.criteria.operator = 'test-operator';

      advancedSearchCriteriaRow.updateOperatorsSelect(getOperators());

      expect(advancedSearchCriteriaRow.criteria.operator).to.equal('contains');
    });

    function getOperators() {
      return [{
        id: 'contains',
        text: 'Contains'
      }, {
        id: 'equals',
        text: 'Equals'
      }];
    }
  });

  describe('compileExtension()', () => {
    it('should construct correct html', () => {
      var stringExtension = getStringExtension();
      advancedSearchCriteriaRow.$compile = mockCompile();
      advancedSearchCriteriaRow.$element = mockElement();

      advancedSearchCriteriaRow.compileExtension(stringExtension);

      var tag = stringExtension.component;
      var expected = `<${tag} config="advancedSearchCriteriaRow.config" on-change="advancedSearchCriteriaRow.onChangedValue()" property="advancedSearchCriteriaRow.property"`;
      expected += ` context="advancedSearchCriteriaRow.context" criteria="advancedSearchCriteriaRow.criteria"></${tag}>`;
      expect(advancedSearchCriteriaRow.$compile.getCall(0).args[0]).to.equal(expected);
    });

    it('should append the constructed html in the value column', () => {
      var stringExtension = getStringExtension();
      advancedSearchCriteriaRow.$compile = mockCompile(['compiled']);
      var element = {
        append: sinon.spy(),
        empty: sinon.spy()
      };
      advancedSearchCriteriaRow.$element = mockElement(element);

      advancedSearchCriteriaRow.compileExtension(stringExtension);
      expect(advancedSearchCriteriaRow.$element.find.getCall(0).args[0]).to.contains('.criteria-value');
      expect(element.empty.called).true;
      expect(element.append.getCall(0).args[0]).to.equal('compiled');
      expect(element.append.calledAfter(element.empty)).to.be.true;
    });
  });

  describe('isRemoveRuleButtonDisabled()', () => {
    it('should tell that the button is not disabled if it is not configured to be but criteria is not topmost', () => {
      advancedSearchCriteriaRow.config.searchMediator.queryBuilder = getSearchQuery();

      advancedSearchCriteriaRow.config.disabled = false;
      expect(advancedSearchCriteriaRow.isRemoveRuleButtonDisabled()).to.be.false;
    });

    it('should tell that the button is disabled if it is configured to be but criteria is not topmost', () => {
      advancedSearchCriteriaRow.config.searchMediator.queryBuilder = getSearchQuery();

      advancedSearchCriteriaRow.config.disabled = true;
      expect(advancedSearchCriteriaRow.isRemoveRuleButtonDisabled()).to.be.true;
    });

    it('should tell that the button is disabled if it is configured to be locked but criteria is not topmost', () => {
      advancedSearchCriteriaRow.config.searchMediator.queryBuilder = getSearchQuery();

      advancedSearchCriteriaRow.config.disabled = false;
      advancedSearchCriteriaRow.config.locked = [AdvancedSearchComponents.REMOVE_RULE];
      expect(advancedSearchCriteriaRow.isRemoveRuleButtonDisabled()).to.be.true;
    });

    it('should tell that the button is disabled if it is not configured but criteria is topmost', () => {
      advancedSearchCriteriaRow.config.searchMediator.queryBuilder = getSearchQuery();

      let config = advancedSearchCriteriaRow.config;
      let query = config.searchMediator.queryBuilder;
      let rules = query.tree.rules[0].rules[1].rules;
      advancedSearchCriteriaRow.criteria.id = rules[0].id;

      advancedSearchCriteriaRow.config.disabled = false;
      expect(advancedSearchCriteriaRow.isRemoveRuleButtonDisabled()).to.be.true;
    });
  });

  function getProperties() {
    return [{
      id: 'title',
      type: 'string'
    }, {
      id: 'status',
      type: 'string'
    }];
  }

  function getStringExtension() {
    return {
      'order': 10,
      'name': 'seip-advanced-search-string-criteria',
      'type': 'string',
      'operators': ['contains', {
        id: 'very_specific_operator',
        label: 'Very specific operator'
      }],
      'component': 'seip-advanced-search-string-criteria',
      'module': 'search/components/advanced/criteria/seip-advanced-search-string-criteria'
    };
  }

  function mockElement(element) {
    return {
      find: sinon.spy(() => {
        if (element) {
          return element
        }
        return {
          append: () => {
          },
          empty: () => {
          }
        };
      })
    };
  }

  function mockCompile(compiled) {
    var compile = () => {
      var returnFunc = () => {
        if (compiled) {
          return compiled;
        }
        return ['compiled-extension']
      };
      return sinon.spy(returnFunc);
    };
    return sinon.spy(compile);
  }

  function getTranslateServiceMock() {
    return {
      translateInstant: () => {
        return 'translation';
      }
    };
  }

  function getSearchQuery() {
    let tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
    tree.rules[0].rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule());
    tree.rules[0].rules.push(SearchCriteriaUtils.buildCondition());
    tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule(ANY_RELATION, 'object', 'set_to', ['emf:123456']));
    return new QueryBuilder(tree);
  }
});