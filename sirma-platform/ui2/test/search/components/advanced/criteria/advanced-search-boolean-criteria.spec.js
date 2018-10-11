import {AdvancedSearchBooleanCriteria} from 'search/components/advanced/criteria/advanced-search-boolean-criteria';
import {TranslateService} from 'services/i18n/translate-service';
import {stub} from 'test/test-utils';

describe('AdvancedSearchBooleanCriteria', () => {

  var criteria;
  var advancedSearchBooleanCriteria;
  beforeEach(() => {
    criteria = {};

    advancedSearchBooleanCriteria = new AdvancedSearchBooleanCriteria(getTranslateServiceMock());
    advancedSearchBooleanCriteria.criteria = criteria;
    advancedSearchBooleanCriteria.ngOnInit();
  });

  it('should not be disabled by default', () => {
    expect(advancedSearchBooleanCriteria.config.disabled).to.be.false;
  });

  it('should build proper select config', () => {
    let expectedData = [{
      id: 'true',
      text: 'label'
    }, {
      id: 'false',
      text: 'label'
    }];
    expect(advancedSearchBooleanCriteria.selectConfig.defaultValue).to.equal('true');
    expect(advancedSearchBooleanCriteria.selectConfig.multiple).to.be.false;
    expect(advancedSearchBooleanCriteria.selectConfig.data).to.deep.equal(expectedData);
    expect(advancedSearchBooleanCriteria.selectConfig.selectOnClose).to.equal(true);
  });

  it('should provide predefined criteria value to select configuration', ()=> {
    advancedSearchBooleanCriteria.criteria.value = 'false';
    advancedSearchBooleanCriteria.createSelectConfig();
    expect(advancedSearchBooleanCriteria.selectConfig.defaultValue).to.equal('false');
  });

  it('should construct the select with a function for determining its disabled state', () => {
    expect(advancedSearchBooleanCriteria.selectConfig.isDisabled()).to.be.false;
    advancedSearchBooleanCriteria.config.disabled = true;
    expect(advancedSearchBooleanCriteria.selectConfig.isDisabled()).to.be.true;
  });
});

function getTranslateServiceMock() {
  var stubbedService = stub(TranslateService);
  stubbedService.translateInstant.returns('label');
  return stubbedService;
}