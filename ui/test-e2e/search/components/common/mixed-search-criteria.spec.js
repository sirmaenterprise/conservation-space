const SANDBOX_URL = 'sandbox/search/components/common/mixed-search-criteria';

var MixedSearchCriteria = require('./mixed-search-criteria');
var SandboxPage = require('../../../page-object').SandboxPage;

const BASIC_SEARCH_OPTION_VALUE = 'basic';

describe('Search criteria switch ', function () {

  var page = new SandboxPage();

  beforeEach(() => {
    page.open(SANDBOX_URL);
  });

  it('should have an option for Basic search', function () {
    var mixedSearchCriteria = new MixedSearchCriteria();
    var basicSearchOption = mixedSearchCriteria.getBasicSearchOption();

    expect(basicSearchOption.isPresent()).to.eventually.be.true;
  });

  it('should have an option for Advanced search', function () {
    var mixedSearchCriteria = new MixedSearchCriteria();
    var advancedSearchOption = mixedSearchCriteria.getAdvancedSearchOption();

    expect(advancedSearchOption.isPresent()).to.eventually.be.true;
  });

  it('Basic search should be selected by default', function () {
    var mixedSearchCriteria = new MixedSearchCriteria();
    var checkedOption = mixedSearchCriteria.getCheckedOption();

    expect(checkedOption.getAttribute('value')).to.eventually.equal(BASIC_SEARCH_OPTION_VALUE);
  });

  describe('When advanced search is clicked', () => {
    it('should load Advanced search form', () => {
      var mixedSearchCriteria = new MixedSearchCriteria();
      var advancedSearchOption = mixedSearchCriteria.getAdvancedSearchOption();

      mixedSearchCriteria.clickOption(advancedSearchOption);
      var advancedSearchForm = mixedSearchCriteria.getAdvancedSearchForm();

      expect(advancedSearchForm.isDisplayed()).to.eventually.be.true;
    });
  });

  describe('When basic search is clicked', () => {
    it('should load Basic search form', () => {
      var mixedSearchCriteria = new MixedSearchCriteria();

      var advancedSearchOption = mixedSearchCriteria.getAdvancedSearchOption();
      mixedSearchCriteria.clickOption(advancedSearchOption);

      var basicSearchOption = mixedSearchCriteria.getBasicSearchOption();
      mixedSearchCriteria.clickOption(basicSearchOption);

      var basicSearchForm = mixedSearchCriteria.getBasicSearchForm();
      return expect(basicSearchForm.isDisplayed()).to.eventually.be.true;
    });
  });

});
