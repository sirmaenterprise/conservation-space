'use strict';

let AdvancedSearchSandboxPage = require('../advanced-search.js').AdvancedSearchSandboxPage;
let AdvancedSearchRelationCriteria = require('../advanced-search.js').AdvancedSearchRelationCriteria;
let MultySelectMenu = require('../../../../form-builder/form-control.js').MultySelectMenu;
let ObjectPickerDialog = require('../../../../picker/object-picker').ObjectPickerDialog;

describe('AdvancedSearchRelationCriteria', () => {

  let advancedSearchSection;
  let page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearchSection = page.getAdvancedSearch().getSection(0);
    advancedSearchSection.addObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
  });

  describe('When a relation criteria is selected', () => {
    it('should have appropriate comparison operators', () => {
      return advancedSearchSection.selectProperty('hasChild').then((row) => {
        return row.getOperatorSelectValues().then((values) => {
          expect(values).to.deep.equal(['set_to', 'not_set_to', 'set_to_query', 'not_set_to_query', 'empty']);
        });
      });
    });

    it('should not allow sub-query in sub-query in sub-query', () => {
      // First level
      advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        row.changeProperty('hasChild');
        row.waitForOperatorSelectToRender();
        row.changeOperator('set_to_query');

        let relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
        relationCriteria.waitForAutomaticSelectionToBeVisible();
        relationCriteria.openPicker();

        // Second level
        let search = ObjectPickerDialog.getPickerDialog(1).getObjectPicker().getSearch();
        search.getCriteria().getSearchBar().toggleOptions().openAdvancedSearch();

        let advancedSearchSection = search.getCriteria().getAdvancedSearch().getSection(0);
        advancedSearchSection.addObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');

        advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((rowInner) => {
          rowInner.changeProperty('hasChild');
          rowInner.waitForOperatorSelectToRender();
          rowInner.changeOperator('set_to_query');

          let secondRelationCriteria = new AdvancedSearchRelationCriteria(rowInner.valueColumn);
          secondRelationCriteria.waitForAutomaticSelectionToBeVisible();
          secondRelationCriteria.openPicker();

          // Third level
          let secondPickerSearch = ObjectPickerDialog.getPickerDialog(2).getObjectPicker().getSearch();
          secondPickerSearch.getCriteria().getSearchBar().toggleOptions().openAdvancedSearch();

          let secondPickerAdvancedSearchSection = secondPickerSearch.getCriteria().getAdvancedSearch().getSection(0);
          secondPickerAdvancedSearchSection.addObjectType('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');

          secondPickerAdvancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((secondPickerRowInner) => {
            secondPickerRowInner.changeProperty('hasChild');
            secondPickerRowInner.waitForOperatorSelectToRender();

            // Should not allow set to query & not set to query
            secondPickerRowInner.getOperatorSelectValues().then((values) => {
              expect(values).to.deep.equal(['set_to', 'not_set_to', 'empty']);
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
            let relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
            relationCriteria.waitForManualSelectionToBeVisible();

            // TODO: CMF-22704 Clean up this mess ...
            let value = new MultySelectMenu(row.element.$('.relation-value > div'));
            return value.selectFromMenuByValue('1').then(() => {
              relationCriteria.openPicker();

              let pickerDialog = new ObjectPickerDialog();
              let picker = pickerDialog.getObjectPicker();

              let search = picker.getSearch();
              search.getResults().clickResultItem(1);

              pickerDialog.ok();

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
            let relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
            relationCriteria.waitForManualSelectionToBeVisible();
            relationCriteria.openPicker();

            let pickerDialog = new ObjectPickerDialog();
            let picker = pickerDialog.getObjectPicker();

            expect(picker.isNavigationPresent()).to.eventually.be.true;
          });
        });
      });
    });

    it('should not allow manual selection when operator is [not]set_to_query', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();

          return row.changeOperator('set_to_query').then(() => {
            let relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
            relationCriteria.waitForAutomaticSelectionToBeVisible();
            relationCriteria.openPicker();

            let pickerDialog = new ObjectPickerDialog();
            let picker = pickerDialog.getObjectPicker();

            // The relation criteria internally uses the picker
            expect(picker.isNavigationPresent()).to.eventually.be.false;
          });
        });
      });
    });

    it('should display object picked for value selection', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();

          let relationCriteria = new AdvancedSearchRelationCriteria(row.valueColumn);
          relationCriteria.waitForManualSelectionToBeVisible();
          relationCriteria.openPicker();

          let pickerDialog = new ObjectPickerDialog();
          let picker = pickerDialog.getObjectPicker();
          let search = picker.getSearch();
          search.waitUntilOpened();
        });
      });
    });

    it('should contain any and current objects in value select', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.changeProperty('hasChild').then(() => {
          row.waitForOperatorSelectToRender();

          let value = new MultySelectMenu(row.element.$('.relation-value > div'));
          return value.selectFromMenuByValue('anyObject').then(() => {
            return value.selectFromMenuByValue('current_object');
          });
        });
      });
    });
  });
});