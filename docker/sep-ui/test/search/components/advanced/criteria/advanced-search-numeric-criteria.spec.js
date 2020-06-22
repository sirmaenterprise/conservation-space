import {AdvancedSearchNumericCriteria} from 'search/components/advanced/criteria/advanced-search-numeric-criteria';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import {AdvancedSearchMocks} from 'test/search/components/advanced/advanced-search-mocks'
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('AdvancedSearchNumericCriteria', () => {

  var criteria;
  var advancedSearchNumericCriteria;
  beforeEach(() => {
    criteria = AdvancedSearchMocks.getCriteria().rules[0];

    advancedSearchNumericCriteria = new AdvancedSearchNumericCriteria(mock$scope());
    advancedSearchNumericCriteria.criteria = criteria;
    advancedSearchNumericCriteria.ngOnInit();
  });

  it('should not be disabled by default', () => {
    expect(advancedSearchNumericCriteria.config.disabled).to.be.false;
  });

  it('should register an operator watcher', () => {
    advancedSearchNumericCriteria.$scope.$watch = sinon.spy();
    advancedSearchNumericCriteria.registerOperatorWatcher();
    expect(advancedSearchNumericCriteria.$scope.$watch.called).to.be.true;
  });

  it('should register a value watcher', () => {
    advancedSearchNumericCriteria.$scope.$watch = sinon.spy();
    advancedSearchNumericCriteria.registerValueWatcher();
    expect(advancedSearchNumericCriteria.$scope.$watch.called).to.be.true;
  });

  it('should assign the default model an empty value', () => {
    advancedSearchNumericCriteria.criteria.value = undefined;
    advancedSearchNumericCriteria.criteria.operator = AdvancedSearchCriteriaOperators.EQUALS.id;
    advancedSearchNumericCriteria.assignModel();
    expect(advancedSearchNumericCriteria.criteria.value).to.deep.equal('');
  });

  it('should assign the model a predefined value', () => {
    advancedSearchNumericCriteria.criteria.value = 123;
    advancedSearchNumericCriteria.criteria.operator = AdvancedSearchCriteriaOperators.EQUALS.id;
    advancedSearchNumericCriteria.assignModel();
    expect(advancedSearchNumericCriteria.criteria.value).to.deep.equal(123);
  });

  it('should reset the model as an array when between operator is selected', () => {
    advancedSearchNumericCriteria.criteria.value = undefined;
    advancedSearchNumericCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_BETWEEN.id;
    advancedSearchNumericCriteria.criteria.value = advancedSearchNumericCriteria.resetModelValue();
    expect(advancedSearchNumericCriteria.criteria.value).to.deep.equal(['', '']);
  });

  it('should return true when the between operator is selected', () => {
    advancedSearchNumericCriteria.criteria.operator = AdvancedSearchCriteriaOperators.IS_BETWEEN.id;
    expect(advancedSearchNumericCriteria.isBetween()).to.deep.equal(true);
  });

  it('should return false when every other but the between operator is selected', () => {
    advancedSearchNumericCriteria.criteria.operator = AdvancedSearchCriteriaOperators.EQUALS.id;
    expect(advancedSearchNumericCriteria.isBetween()).to.deep.equal(false);
  });

  it('should not allow for text to be entered as input', () => {
    validateNumericField(advancedSearchNumericCriteria, 'invalid', 0);
  });

  it('should not allow for invalid number formats as input', () => {
    validateNumericField(advancedSearchNumericCriteria, '++++23', 0);
  });

  it('should not allow for numeric expressions as input', () => {
    validateNumericField(advancedSearchNumericCriteria, '123+512', 0);
  });

  it('should not allow for invalid real number formats', () => {
    validateNumericField(advancedSearchNumericCriteria, '12..223', 0);
  });

  it('should reset criteria value when the operator is "Is empty"', () => {
    advancedSearchNumericCriteria.criteria.operator = AdvancedSearchCriteriaOperators.EMPTY.id;
    advancedSearchNumericCriteria.$scope.$digest();
    expect(advancedSearchNumericCriteria.criteria.value).to.equal(null);
  });

  function validateNumericField(numericCriteria, validate, expected) {
    numericCriteria.criteria.operator = AdvancedSearchCriteriaOperators.EQUALS.id;
    numericCriteria.registerValueWatcher();
    numericCriteria.criteria.numberModel = validate;
    numericCriteria.$scope.$digest();
    expect(numericCriteria.criteria.value).to.deep.equal(expected);
  }

});