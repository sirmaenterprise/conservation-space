var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchRelationCriteria = require('./advanced-search.js').AdvancedSearchRelationCriteria;
var MultySelectMenu = require('../../../form-builder/form-control.js').MultySelectMenu;
var Search = require('../search.js');
var MixedSearchCriteria = require('../common/mixed-search-criteria');
var Dialog = require('../../../components/dialog/dialog');
var ObjectPickerDialog = require('../../../picker/object-picker').ObjectPickerDialog;

describe('AdvancedSearchRelationCriteria', () => {

  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearchSection = page.getAdvancedSearch().getSection(0);
    advancedSearchSection.addObjectType("http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document");
  });

  describe('When a relation criteria is selected', () => {
    it('should have appropriate comparison operators', () => {
      return advancedSearchSection.selectProperty('hasChild').then((row) => {
        return row.getOperatorSelectValues().then((values) => {
          expect(values).to.deep.equal(['set_to', 'not_set_to', 'set_to_query', 'not_set_to_query', 'empty']);
        });
      });
    });

    it('should not allow sub-query in sub-query', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();
          return row.changeOperator('set_to_query').then(() => {
            var relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
            relationCriteria.waitForAutomaticSelectionToBeVisible();
            relationCriteria.openPicker();

            var search = new Search(Search.COMPONENT_SELECTOR);
            search.waitUntilOpened();

            var mixedCriteria = new MixedSearchCriteria();
            mixedCriteria.clickOption(mixedCriteria.getAdvancedSearchOption());

            var section = page.getAdvancedSearch('.modal .seip-advanced-search').getSection(0);
            section.addObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');

            return section.getCriteriaRowForGroup(1, 0, 0).then((rowInner) => {
              return rowInner.changeProperty('hasChild').then(() => {
                rowInner.waitForOperatorSelectToRender();

                return rowInner.getOperatorSelectValues().then((values) => {
                  expect(values).to.deep.equal(['set_to', 'not_set_to', 'empty']);
                });
              });
            });
          });
        });
      });
    });

    it('should reflect changes in inline picker into object picker', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();

          return row.changeOperator('not_set_to').then(() => {
            var relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
            relationCriteria.waitForManualSelectionToBeVisible();

            // TODO: CMF-22704 Clean up this mess ...
            var value = new MultySelectMenu(row.element.$('.relation-value > div'));
            return value.selectFromMenuByValue('1').then(() => {
              relationCriteria.openPicker();

              var search = new Search(Search.COMPONENT_SELECTOR);
              search.waitUntilOpened();

              search.results.clickResultItem(1);

              var dialog = new Dialog($('.modal'));
              dialog.ok();

              browser.wait(EC.stalenessOf(dialog.element), DEFAULT_TIMEOUT);

              return value.removeFromSelectionByTitle('Object #1').then(() => {
                return value.removeFromSelectionByTitle('Title');
              });
            });
          });
        });
      });
    });

    it('should allow only manual selection when operator is [not]set_to', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();

          return row.changeOperator('not_set_to').then(() => {
            var relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
            relationCriteria.waitForManualSelectionToBeVisible();
            relationCriteria.openPicker();

            var search = new Search(Search.COMPONENT_SELECTOR);
            search.waitUntilOpened();

            expect(new ObjectPickerDialog().getObjectPicker().isNavigationPresent()).to.eventually.be.true;
          });
        });
      });
    });

    it('should not allow manual selection when operator is [not]set_to_query', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();

          return row.changeOperator('set_to_query').then(() => {
            var relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
            relationCriteria.waitForAutomaticSelectionToBeVisible();
            relationCriteria.openPicker();

            var search = new Search(Search.COMPONENT_SELECTOR);
            search.waitUntilOpened();

            // The relation criteria internally uses the picker
            expect(new ObjectPickerDialog().getObjectPicker().isNavigationPresent()).to.eventually.be.false;
          });
        });
      });
    });

    it('should display object picked for value selection', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();

          var relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
          relationCriteria.waitForManualSelectionToBeVisible();
          relationCriteria.openPicker();

          new Search(Search.COMPONENT_SELECTOR).waitUntilOpened();
        });
      });
    });

    it('should contain any and current objects in value select', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();

          var value = new MultySelectMenu(row.element.$('.relation-value > div'));
          return value.selectFromMenuByValue('anyObject').then(() => {
            return value.selectFromMenuByValue('current_object');
          });
        });
      });
    });
  });
});