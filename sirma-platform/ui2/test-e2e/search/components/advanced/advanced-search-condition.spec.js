var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;
var hasClass = require('../../../test-utils.js').hasClass;

describe('AdvancedSearch Conditions', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
    advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
  });

  describe('When condition is changed to OR', () => {
    it('should change the condition value in the tree model', () => {
      advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.or();
        page.getTreeModel().then((treeModel) => {
          var condition = treeModel.rules[0].rules[1].condition;
          expect(condition).to.equal('OR');
        });
      });
    });

    it('should render the OR button as active', () => {
      advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.or();
        expect(hasClass(controls.orButton, 'btn-primary')).to.eventually.be.true;
        expect(hasClass(controls.andButton, 'btn-primary')).to.eventually.be.false;
      });
    });
  });

  describe('When condition is changed to AND', () => {
    it('should change the condition value in the tree model', () => {
      advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.and();
        page.getTreeModel().then((treeModel) => {
          var condition = treeModel.rules[0].rules[1].condition;
          expect(condition).to.equal('AND');
        });
      });
    });

    it('should render the AND button as active', () => {
      advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        controls.and();
        expect(hasClass(controls.andButton, 'btn-primary')).to.eventually.be.true;
        expect(hasClass(controls.orButton, 'btn-primary')).to.eventually.be.false;
      });
    });
  });
});