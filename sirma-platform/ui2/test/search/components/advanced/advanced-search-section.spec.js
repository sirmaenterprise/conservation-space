import {AdvancedSearchSection} from 'search/components/advanced/advanced-search-section';
import {ANY_OBJECT} from 'search/utils/search-criteria-utils';
import {CRITERIA_READY_EVENT} from 'search/components/common/search-criteria-component';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {SearchMediator, EVENT_CRITERIA_RESET} from 'search/search-mediator';
import {QueryBuilder} from 'search/utils/query-builder';
import {TranslateService} from 'services/i18n/translate-service';

import {AdvancedSearchMocks} from './advanced-search-mocks'
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('AdvancedSearchSection', () => {

  var advancedSearchSection;
  var criteria;

  beforeEach(() => {
    criteria = AdvancedSearchMocks.getSectionCriteria();

    advancedSearchSection = new AdvancedSearchSection(mock$scope(), stub(TranslateService));
    advancedSearchSection.criteria = criteria;
    advancedSearchSection.config = {
      searchMediator: new SearchMediator({}, new QueryBuilder(criteria))
    };
    advancedSearchSection.loaders = {
      properties: sinon.spy(() => {
        return PromiseStub.resolve([{'id': 'title'}])
      })
    };
    advancedSearchSection.ngOnInit();
  });

  afterEach(() => {
    AdvancedSearchSection.prototype.config = undefined;
  });

  it('should delegate to afterInit() to notify it is ready', () => {
    advancedSearchSection.config.searchMediator.trigger = sinon.spy();
    advancedSearchSection.ngOnInit();
    expect(advancedSearchSection.config.searchMediator.trigger.calledOnce).to.be.true;
    expect(advancedSearchSection.config.searchMediator.trigger.getCall(0).args[0]).to.equal(CRITERIA_READY_EVENT);
  });

  it('should assign default criteria if empty', () => {
    var criteria = {};
    AdvancedSearchSection.prototype.criteria = criteria;
    AdvancedSearchSection.prototype.config = {
      searchMediator: new SearchMediator({}, new QueryBuilder(criteria))
    };
    advancedSearchSection = new AdvancedSearchSection(mock$scope(), stub(TranslateService));
    expect(advancedSearchSection.criteria).to.exist;
    expect(advancedSearchSection.criteria.rules).to.exist;
  });

  it('should convert the model to an array', () => {
    expect(Array.isArray(advancedSearchSection.criteria.rules[0].value)).to.be.true;
  });

  it('should sets the field value in the type rule', () => {
    expect(advancedSearchSection.criteria.rules[0].field).to.equals(SearchCriteriaUtils.CRITERIA_TYPES_RULE_FIELD);
  });

  describe('registerModelWatchers()', () => {
    it('should register a watcher for the object type and trigger property change', () => {
      // Ensuring new scope is clean
      advancedSearchSection.$scope = mock$scope();
      advancedSearchSection.criteria.rules[0].value = ['type'];
      advancedSearchSection.registerModelWatchers();
      advancedSearchSection.$scope.$digest();
      advancedSearchSection.onObjectTypeChange = sinon.spy();

      advancedSearchSection.criteria.rules[0].value = ['new-type'];
      advancedSearchSection.$scope.$digest();
      expect(advancedSearchSection.onObjectTypeChange.calledOnce).to.be.true;
    });

    it('should assign default criteria when digest cycle gets the property', () => {
      advancedSearchSection.criteria.rules = [];
      advancedSearchSection.$scope.$digest();
      expect(advancedSearchSection.criteria.rules[0]).to.exist;
      expect(advancedSearchSection.criteria.rules[1]).to.exist;
    });

    it('should reset the criteria if the new type is different from the old one', () => {
      advancedSearchSection.config.searchMediator.trigger = sinon.spy();
      // Ensuring new scope is clean
      advancedSearchSection.$scope = mock$scope();
      advancedSearchSection.criteria.rules[0].value = ['type'];
      advancedSearchSection.registerModelWatchers();
      advancedSearchSection.$scope.$digest();

      advancedSearchSection.resetCriteria = sinon.spy();
      advancedSearchSection.criteria.rules[0].value = ['new-type'];

      advancedSearchSection.$scope.$digest();
      expect(advancedSearchSection.resetCriteria.calledOnce).to.be.true;
      expect(advancedSearchSection.config.searchMediator.trigger.calledWith(EVENT_CRITERIA_RESET)).to.be.true;
    });

    it('should not reset the criteria if the new type is empty', () => {
      // Ensuring new scope is clean
      advancedSearchSection.$scope = mock$scope();
      advancedSearchSection.criteria.rules[0].value = ['type'];
      advancedSearchSection.registerModelWatchers();
      advancedSearchSection.$scope.$digest();

      advancedSearchSection.resetCriteria = sinon.spy();
      advancedSearchSection.criteria.rules[0].value = [];

      advancedSearchSection.$scope.$digest();
      expect(advancedSearchSection.resetCriteria.called).to.be.false;
    });

    it('should not reset the criteria if the new type is the same as the old one', () => {
      // Ensuring new scope is clean
      advancedSearchSection.$scope = mock$scope();
      advancedSearchSection.criteria.rules[0].value = ['type'];
      advancedSearchSection.registerModelWatchers();
      advancedSearchSection.$scope.$digest();

      advancedSearchSection.resetCriteria = sinon.spy();
      advancedSearchSection.criteria.rules[0].value = ['type'];

      advancedSearchSection.$scope.$digest();
      expect(advancedSearchSection.resetCriteria.called).to.be.false;
    });

    it('should reset types to any object if any object is present', () => {
      // Ensuring new scope is clean
      advancedSearchSection.$scope = mock$scope();
      advancedSearchSection.registerModelWatchers();
      advancedSearchSection.criteria.rules[0].value = ['type','other-type'];
      advancedSearchSection.$scope.$digest();

      advancedSearchSection.criteria.rules[0].value = ['type','other-type', ANY_OBJECT];
      advancedSearchSection.$scope.$digest();

      expect(advancedSearchSection.criteria.rules[0].value).to.deep.equal(ANY_OBJECT);
    });

  });

  describe('assignDefaultCriteria()', () => {
    it('should not assign default criteria rules if the current criteria has them', () => {
      var objectTypeRule = advancedSearchSection.criteria.rules[0];
      var rules = advancedSearchSection.criteria.rules[1];

      advancedSearchSection.assignDefaultCriteria();
      expect(advancedSearchSection.criteria.rules[0]).to.equal(objectTypeRule);
      expect(advancedSearchSection.criteria.rules[1]).to.equal(rules);
    });

    it('should assign default criteria rules if the current criteria has none', () => {
      advancedSearchSection.criteria.rules = undefined;
      var defaultTypeCriteria = {id: 'type-id'};
      var typeStub = sinon.stub(SearchCriteriaUtils, 'getDefaultObjectTypeRule', () => {
        return defaultTypeCriteria;
      });
      var defaultCriteria = {id: 'criteria-id'};
      var criteriaStub = sinon.stub(SearchCriteriaUtils, 'getDefaultCriteriaCondition', () => {
        return defaultCriteria;
      });

      advancedSearchSection.assignDefaultCriteria();
      expect(advancedSearchSection.criteria.rules[0]).to.exist;
      expect(advancedSearchSection.criteria.rules[0]).to.deep.equal(defaultTypeCriteria);
      expect(advancedSearchSection.criteria.rules[1]).to.exist;
      expect(advancedSearchSection.criteria.rules[1]).to.deep.equal(defaultCriteria);
      typeStub.restore();
      criteriaStub.restore();
    });

    it('should assign default criteria rules if the current criteria rules is empty', () => {
      advancedSearchSection.criteria.rules = [];
      var defaultTypeCriteria = {id: 'type-id'};
      var typeStub = sinon.stub(SearchCriteriaUtils, 'getDefaultObjectTypeRule', () => {
        return defaultTypeCriteria;
      });
      var defaultCriteria = {id: 'criteria-id'};
      var criteriaStub = sinon.stub(SearchCriteriaUtils, 'getDefaultCriteriaCondition', () => {
        return defaultCriteria;
      });

      advancedSearchSection.assignDefaultCriteria();
      expect(advancedSearchSection.criteria.rules[0]).to.exist;
      expect(advancedSearchSection.criteria.rules[0]).to.deep.equal(defaultTypeCriteria);
      expect(advancedSearchSection.criteria.rules[1]).to.exist;
      expect(advancedSearchSection.criteria.rules[1]).to.deep.equal(defaultCriteria);
      typeStub.restore();
      criteriaStub.restore();
    });

    it('should assign inner condition if it lacks one', () => {
      advancedSearchSection.criteria.rules = [SearchCriteriaUtils.buildRule()];
      advancedSearchSection.assignDefaultCriteria();
      expect(advancedSearchSection.criteria.rules[1]).to.exist;
    });

    it('should assign predefined types if configured', () => {
      advancedSearchSection.config.predefinedTypes = ['emf:HashTag', 'emf:Quality'];
      advancedSearchSection.criteria.rules = [];

      advancedSearchSection.assignDefaultCriteria();
      expect(advancedSearchSection.criteria.rules[0]).to.exist;
      expect(advancedSearchSection.criteria.rules[0].value).to.deep.equal(['emf:HashTag', 'emf:Quality']);
    });
  });

  describe('createObjectTypeConfig()', () => {
    it('should create a configuration for an object type select', () => {
      expect(advancedSearchSection.objectTypeSelectConfig).to.exist;
    });

    it('should configure a callback function to obtain the object types', () => {
      expect(advancedSearchSection.objectTypeSelectConfig.publishCallback).to.exist;
    });

    it('should configure a default value', () => {
      expect(advancedSearchSection.objectTypeSelectConfig.defaultValue).to.deep.equal(['emf:Document']);
    });

    it('should provide the models loader if present', () => {
      var modelsLoader = () => {
      };
      advancedSearchSection.loaders = {models: modelsLoader};
      advancedSearchSection.createObjectTypeConfig();
      expect(advancedSearchSection.objectTypeSelectConfig.dataLoader).to.equal(modelsLoader);
    });

    it('should build predefinedData to not be empty when there are no predefined types', () => {
      advancedSearchSection.config.predefinedTypes = [];
      advancedSearchSection.createObjectTypeConfig();
      expect(advancedSearchSection.objectTypeSelectConfig.predefinedData.length).to.equal(1);
      expect(advancedSearchSection.objectTypeSelectConfig.predefinedData[0].id).to.equal(ANY_OBJECT);
    });

    it('should build predefinedData to be empty when there are predefined types specified', () => {
      var predefinedTypes = [{id: 'type'}, {id: 'other-type'}];
      advancedSearchSection.config.predefinedTypes = predefinedTypes;
      advancedSearchSection.createObjectTypeConfig();
      expect(advancedSearchSection.objectTypeSelectConfig.predefinedData).to.deep.eq([]);
    });

    it('should construct the select with an update function for the disabled state', () => {
      expect(advancedSearchSection.objectTypeSelectConfig.isDisabled()).to.be.false;
      advancedSearchSection.config.disabled = true;
      expect(advancedSearchSection.objectTypeSelectConfig.isDisabled()).to.be.true;
    });
  });

  describe('objectTypeSelectCallback()', () => {
    it('should set the provided object types in the component and trigger change', () => {
      advancedSearchSection.onObjectTypeChange = sinon.spy();
      advancedSearchSection.objectTypes = undefined;

      var objectTypes = [{id: 'type'}];
      advancedSearchSection.objectTypeSelectCallback(objectTypes);
      expect(advancedSearchSection.objectTypes).to.deep.equal(objectTypes);

      expect(advancedSearchSection.onObjectTypeChange.called).to.be.true;
    });
  });

  describe('onObjectTypeChange()', () => {
    beforeEach(() => {
      advancedSearchSection.objectTypes = [{id: ANY_OBJECT}, {id: 'type'}, {id: 'other-type'}];
      advancedSearchSection.criteria.rules[0].value = ['type'];
    });

    it('should set default object type if none is defined', () => {
      advancedSearchSection.criteria.rules[0].value = undefined;
      advancedSearchSection.onObjectTypeChange();
      expect(advancedSearchSection.criteria.rules[0].value).to.deep.equal([ANY_OBJECT]);
    });

    it('should not load properties if no object type is defined initially', () => {
      advancedSearchSection.criteria.rules[0].value = undefined;
      advancedSearchSection.onObjectTypeChange();
      var propertiesSpy = advancedSearchSection.loaders.properties;
      expect(propertiesSpy.called).to.be.false;
    });

    it('should trim Any object if there is more than one object types', () => {
      advancedSearchSection.criteria.rules[0].value = [ANY_OBJECT, 'emf:Case'];
      advancedSearchSection.onObjectTypeChange();
      expect(advancedSearchSection.criteria.rules[0].value).to.deep.equal(['emf:Case']);
    });

    it('should not trim object properties if Any object is not present', () => {
      advancedSearchSection.criteria.rules[0].value = ['emf:Project', 'emf:Case'];
      advancedSearchSection.onObjectTypeChange();
      expect(advancedSearchSection.criteria.rules[0].value).to.deep.equal(['emf:Project', 'emf:Case']);
    });

    it('should not load properties if there are no object types', () => {
      advancedSearchSection.objectTypes = undefined;
      advancedSearchSection.onObjectTypeChange();
      var propertiesSpy = advancedSearchSection.loaders.properties;
      expect(propertiesSpy.called).to.be.false;
    });

    it('should load properties', () => {
      advancedSearchSection.onObjectTypeChange();
      var propertiesSpy = advancedSearchSection.loaders.properties;
      expect(propertiesSpy.called).to.be.true;
      expect(propertiesSpy.getCall(0).args[0]).to.deep.equal(['type']);
    });

    it('should send nullable value if the is set to ject', () => {
      advancedSearchSection.criteria.rules[0].value = [ANY_OBJECT];
      advancedSearchSection.onObjectTypeChange();
      var propertiesSpy = advancedSearchSection.loaders.properties;
      expect(propertiesSpy.called).to.be.true;
      expect(propertiesSpy.getCall(0).args[0]).to.equal(null);
    });

    it('should set the properties', () => {
      var expected = [{'id': 'title'}];
      advancedSearchSection.onObjectTypeChange();
      expect(advancedSearchSection.criteriaProperties).to.exist;
      expect(advancedSearchSection.criteriaProperties).to.deep.equal(expected);
    });

    it('should not set the properties if they are empty', () => {
      advancedSearchSection.loaders.properties = () => {
        return PromiseStub.resolve([]);
      };
      advancedSearchSection.onObjectTypeChange();
      expect(advancedSearchSection.criteriaProperties).to.not.exist;
    });
  });

  describe('resetCriteria()', () => {
    it('should reset the current criteria', () => {
      var rules = advancedSearchSection.criteria.rules[1];
      advancedSearchSection.criteriaGroups = rules;

      advancedSearchSection.resetCriteria();
      expect(advancedSearchSection.criteria.rules[1]).to.exist;
      expect(advancedSearchSection.criteria.rules[1]).to.not.equal(rules);
    });
  });

  describe('getObjectTypeValues()', () => {
    it('should construct correct object type value for type without parent', () => {
      var types = getObjectTypes();
      var expected = 'emf:MainType1';
      expect(advancedSearchSection.getObjectTypeValues(types)[0]).to.equal(expected);
    });

    it('should construct correct object type value for semantic type with parent', () => {
      var types = getObjectTypes();
      var expected = 'emf:SubType1';
      expect(advancedSearchSection.getObjectTypeValues(types)[1]).to.equal(expected);
    });
  });

  describe('getSelectedObjectTypes()', ()=> {
    it('should fetch the correct object type', () => {
      advancedSearchSection.objectTypes = getObjectTypes();
      advancedSearchSection.criteria.rules[0].value = ['emf:SubType1'];
      var type = advancedSearchSection.getSelectedObjectTypes();
      var expected = [{
        id: 'emf:SubType1',
        parent: 'emf:MainType1',
        type: 'class'
      }];
      expect(type).to.deep.equal(expected);
    });
  });
});

function getObjectTypes() {
  return [{
    id: 'emf:MainType1',
    type: 'class'
  }, {
    id: 'emf:SubType1',
    parent: 'emf:MainType1',
    type: 'class'
  }, {
    id: 'CLSUBTYPE1',
    parent: 'emf:MainType1'
  }];
}