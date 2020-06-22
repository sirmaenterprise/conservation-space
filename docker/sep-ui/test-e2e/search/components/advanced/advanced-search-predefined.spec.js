var AdvancedSearchSandboxPage = require('./advanced-search.js').AdvancedSearchSandboxPage;
var AdvancedSearchCodelistCriteria = require('./advanced-search.js').AdvancedSearchCodelistCriteria;
var AdvancedSearchBooleanCriteria = require('./advanced-search.js').AdvancedSearchBooleanCriteria;
var hasClass = require('../../../test-utils.js').hasClass;

describe('AdvancedSearch', () => {

  var advancedSearch;
  var advancedSearchSection;
  var page = new AdvancedSearchSandboxPage();

  beforeEach(() => {
    page.open();
    advancedSearch = page.getAdvancedSearch();
    advancedSearchSection = advancedSearch.getSection(0);
  });

  describe('When configured with predefined criteria', () => {
    it('should restore criteria section and criteria tree in the form', () => {
      page.changeCriteriaSet('predefined');
      return advancedSearchSection.getCriteriaControlsForGroup(1, 0).then((controls) => {
        return expect(hasClass(controls.andButton, 'btn-primary')).to.eventually.be.true;
      }).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0);
      }).then((firstRow) => {
        return firstRow.getSelectedPropertyValue().then((selectedProperty) => {
          expect(selectedProperty).to.equal('description');
          return firstRow.getValueExtension('string').getValue();
        }).then((stringValue) => {
          expect(stringValue).to.deep.equal(['1']);
        });
      }).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(2, 1, 0);
      }).then((nestedRow) => {
        return nestedRow.getSelectedPropertyValue().then((selectedProperty) => {
          expect(selectedProperty).to.equal('test-property');
          return nestedRow.getSelectedOperatorValue();
        }).then((operatorValue) => {
          expect(operatorValue).to.equal('test-operator');
        });
      }).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(3, 0, 0);
      }).then((nestedRow) => {
        return nestedRow.getSelectedPropertyValue().then((selectedProperty) => {
          expect(selectedProperty).to.equal('status');
          var codelistCriteria = new AdvancedSearchCodelistCriteria(nestedRow.valueColumn);
          return codelistCriteria.getSelectedValues();
        }).then((codelistValue) => {
          expect(codelistValue).to.deep.equal(['APPROVED', 'DELETED']);
        });
      }).then(() => {
        // Predefined numeric property with "Is empty" for operator
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 4);
      }).then((row) => {
        var booleanCriteria = new AdvancedSearchBooleanCriteria(row.valueColumn);
        return expect(booleanCriteria.getSelectedValue()).to.eventually.equal('false');
      });
    });

    it('should restore criteria section and criteria tree in the model', () => {
      page.changeCriteriaSet('predefined');
      return page.getTreeModel().then((treeModel) => {
        var objectType = treeModel.rules[0].rules[0].value;
        expect(objectType).to.deep.equal(['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document']);

        var criteria = treeModel.rules[0].rules[1];
        expect(criteria.condition).to.equal('AND');

        var firstLevelRow1 = criteria.rules[0];
        var expectedForFirstLevelRow1 = {
          'field': 'description',
          'type': 'string',
          'operator': 'contains'
        };
        expect(firstLevelRow1).to.contains(expectedForFirstLevelRow1);
        expect(firstLevelRow1.value).to.deep.equal(['1']);

        var firstLevelRow2 = criteria.rules[3];
        var expectedForFirstLevelRow2 = {
          'field': 'test-property',
          'type': 'test-type',
          'operator': 'test-operator2'
        };
        expect(firstLevelRow2).to.include(expectedForFirstLevelRow2);

        var firstGroup = criteria.rules[1];
        expect(firstGroup).to.exist;
        expect(firstGroup.condition).to.equal('OR');

        var innerGroup = firstGroup.rules[1];
        expect(innerGroup).to.exist;
        expect(innerGroup.condition).to.equal('AND');

        var innerCriteria = innerGroup.rules[0];
        var expectedForInnerCriteria = {
          'field': 'status',
          'type': 'codeList',
          'operator': 'in'
        };
        expect(innerCriteria).to.include(expectedForInnerCriteria);
        expect(innerCriteria.value).to.deep.equal(['APPROVED', 'DELETED']);
      });
    });
  });

  describe('When configured with predefined object type which is not the first in the object type select', () => {
    it('should restore criteria section and criteria tree in the form', () => {
      page.changeCriteriaSet('predefined2');
      return advancedSearchSection.getObjectTypeSelectValue().then((selectedTypes) => {
        expect(selectedTypes).to.deep.equal(['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Tag']);
      }).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(1, 0, 1);
      }).then((secondRow) => {
        return secondRow.getSelectedPropertyValue().then((selectedProperty) => {
          expect(selectedProperty).to.equal('title');
          return expect(secondRow.getValueExtension('string').getValue()).to.eventually.deep.equal(['123']);
        });
      }).then(() => {
        return advancedSearchSection.getCriteriaRowForGroup(2, 0, 0);
      }).then((nestedRow) => {
        return nestedRow.getSelectedPropertyValue().then((selectedProperty) => {
          expect(selectedProperty).to.equal('priority');
          return expect(nestedRow.getSelectedOperatorValue()).to.eventually.equal('in');
        }).then((operatorValue) => {
          var codelistCriteria = new AdvancedSearchCodelistCriteria(nestedRow.valueColumn);
          return expect(codelistCriteria.getSelectedValues()).to.eventually.deep.equal(['HIGH']);
        });
      });
    });
  });

  describe('When configured without predefined criteria', () => {
    it('should create a default search section and criteria tree', () => {
      return advancedSearchSection.getCriteriaRowForGroup(1, 0, 0).then((row) => {
        return row.getSelectedPropertyValue();
      }).then((selectedProperty) => {
        expect(selectedProperty).to.equal('title');
      });
    });
  });

});