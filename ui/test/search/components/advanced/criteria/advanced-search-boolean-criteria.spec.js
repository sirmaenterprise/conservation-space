import {AdvancedSearchBooleanCriteria} from 'search/components/advanced/criteria/advanced-search-boolean-criteria';

import {AdvancedSearchMocks} from 'test/search/components/advanced/advanced-search-mocks'
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('AdvancedSearchBooleanCriteria', () => {

  var criteria;
  var advancedSearchBooleanCriteria;
  beforeEach(() => {
    // Fix scope mixing issue in karma
    AdvancedSearchBooleanCriteria.prototype.config = undefined;

    criteria = {};
    AdvancedSearchBooleanCriteria.prototype.criteria = criteria;

    advancedSearchBooleanCriteria = new AdvancedSearchBooleanCriteria(mock$scope(), getTranslateServiceMock());
  });

  afterEach(() => {
    // Fix scope mixing issue in karma
    AdvancedSearchBooleanCriteria.prototype.criteria = undefined;
    AdvancedSearchBooleanCriteria.prototype.config = undefined;
  });

  it('should not be disabled by default', () => {
    expect(advancedSearchBooleanCriteria.config.disabled).to.be.false;
  });

  it('should build proper select config', () => {
    let expected = {
      defaultValue: 'true',
      multiple: false,
      data: [{
        id: 'true',
        text: 'label'
      }, {
        id: 'false',
        text: 'label'
      }],
      disabled: false,
      selectOnClose: true
    };
    expect(advancedSearchBooleanCriteria.selectConfig).to.deep.equal(expected);
  });

  it('should provide predefined criteria value to select configuration', ()=> {
    advancedSearchBooleanCriteria.criteria.value = 'false';
    advancedSearchBooleanCriteria.createSelectConfig();
    expect(advancedSearchBooleanCriteria.selectConfig.defaultValue).to.equal('false');
  });

  it('should register a watcher for the disabled property', () => {
    advancedSearchBooleanCriteria.$scope.$watch = sinon.spy();
    advancedSearchBooleanCriteria.registerDisabledWatcher();
    expect(advancedSearchBooleanCriteria.$scope.$watch.called).to.be.true;
  });

  it('should disable the select when disabled config is true', () => {
    advancedSearchBooleanCriteria.config.disabled = true;
    advancedSearchBooleanCriteria.selectConfig = {};
    advancedSearchBooleanCriteria.$scope.$digest();
    expect(advancedSearchBooleanCriteria.selectConfig.disabled).to.be.true;
  });

});

function getTranslateServiceMock() {
  return {
    translateInstant: sinon.spy(() => {
      return 'label';
    })
  };
}