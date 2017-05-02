var BasicSearchCriteriaSandbox = require('./basic-search-criteria.js').BasicSearchCriteriaSandbox;
var DatetimeField = require('../../../form-builder/form-control.js').DatetimeField;

describe('BasicSearchCriteria', () => {

  var firstCriteria;
  var page = new BasicSearchCriteriaSandbox();

  beforeEach(() => {
    page.open();
    firstCriteria = page.getFirstCriteria();
  });

  describe('When a text is entered in the FTS field', () => {
    it('should trigger a search on enter key', () => {
      return firstCriteria.freeTextField.sendKeys(protractor.Key.ENTER).then(() => {
        var textArea = page.getCriteriaTextArea();
        return browser.wait(EC.textToBePresentInElementValue(textArea, 'searched'), DEFAULT_TIMEOUT);
      });
    });

    it('should correctly change the tree model for free text field', () => {
      return firstCriteria.freeTextField.sendKeys('test').then(() => {
        return expectInnerCriteriaRule('freeText', 'fts', 'contains', 'test');
      });
    });
  });

  describe('When the search button is pressed', () => {
    it('should trigger a search', () => {
      return firstCriteria.search().then(() => {
        var textArea = page.getCriteriaTextArea();
        return browser.wait(EC.textToBePresentInElementValue(textArea, 'searched'), DEFAULT_TIMEOUT);
      });
    });
  });

  describe('When the clear button is pressed', () => {
    it('should clear any initially preset or added criteria', () => {
      var thirdCriteria = page.getThirdCriteria();
      return Promise.all([
        thirdCriteria.freeTextField.sendKeys('more free text search'),
        thirdCriteria.modifySelectValue(thirdCriteria.typesSelectElement, 'OT210027', false),
        thirdCriteria.modifySelectValue(thirdCriteria.relationshipsSelectElement, 'rel:this', false),
        thirdCriteria.modifySelectValue(thirdCriteria.contextSelectElement, '1', false),
        thirdCriteria.modifySelectValue(thirdCriteria.createdBySelectElement, 'johndoe@doeandco.com', false)
      ]).then(() => {
        return thirdCriteria.clearCriteria();
      }).then(() => {
        var expected = {
          metaText: '',
          types: [],
          relationships: [],
          createdBy: [],
          context: [],
          createdFromDate: '',
          createdFromDate: ''
        };
        return expectForm(thirdCriteria, expected);
      });
    });

    it('should correctly clear the tree model', () => {
      return Promise.all([
        firstCriteria.freeTextField.sendKeys('more free text search'),
        firstCriteria.modifySelectValue(firstCriteria.typesSelectElement, 'OT210027', false),
        firstCriteria.modifySelectValue(firstCriteria.relationshipsSelectElement, 'rel:this', false),
        firstCriteria.modifySelectValue(firstCriteria.contextSelectElement, '1', false),
        firstCriteria.modifySelectValue(firstCriteria.createdBySelectElement, 'johndoe@doeandco.com', false)
      ]).then(() => {
        return firstCriteria.clearCriteria();
      }).then(() => {
        // If there are no object types - anyObject should be in the model
        return expectTypeCriteria(['anyObject']);
      }).then(() => {
        return page.getInnerCriteria()
      }).then((innerCriteria) => {
        // No inner rules should be present
        return expect(innerCriteria.rules.length).to.equal(0);
      });
    });
  });

  describe('Types field', () => {
    it('should correctly change the tree model for types field', () => {
      var semanticType = 'http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document';
      var definitionType = 'OT210027';

      return firstCriteria.modifySelectValue(firstCriteria.typesSelectElement, semanticType).then(() => {
        return expectTypeCriteria([semanticType]);
      }).then(() => {
        return firstCriteria.modifySelectValue(firstCriteria.typesSelectElement, definitionType);
      }).then(() => {
        return expectTypeCriteria([semanticType, definitionType]);
      });
    });
  });

  describe('Created on (from)', () => {
    it('should correctly change the tree model for emf:createdOn', () => {
      var fromPicker = new DatetimeField(firstCriteria.dateFromPickerElement);
      fromPicker.setTodayDateByPicker();
      var midnight = getMidnightISODate();
      return expectInnerCriteriaRule('emf:createdOn', 'dateTime', 'between', [midnight, '']);
    });
  });

  describe('Created on (to)', () => {
    it('should correctly change the tree model for emf:createdOn', () => {
      var toPicker = new DatetimeField(firstCriteria.dateToPickerElement);
      toPicker.setTodayDateByPicker();
      var endOfDay = getEndOfDayUTC();
      return expectInnerCriteriaRule('emf:createdOn', 'dateTime', 'between', ['', endOfDay]);
    });
  });

  describe('Created by field', () => {
    it('should correctly change the tree model for emf:createdBy', () => {
      return firstCriteria.modifySelectValue(firstCriteria.createdBySelectElement, 'johndoe@doeandco.com').then(() => {
        return expectInnerCriteriaRule('emf:createdBy', 'object', 'set_to', ['johndoe@doeandco.com']);
      });
    });
  });

  describe('Context field', () => {
    it('should correctly change the tree model for context field', () => {
      return firstCriteria.modifySelectValue(firstCriteria.contextSelectElement, '1').then(() => {
        return expectInnerCriteriaRule('anyRelation', 'object', 'set_to', ['1']);
      });
    });

    it('should display contextual items on top', () => {
      return firstCriteria.getAvailableSelectChoices(firstCriteria.contextSelectElement).then((choices) => {
        expect(choices.length > 0).to.be.true;
        expect(choices[0]).to.equal('current_object');
      });
    });
  });

  describe('Relationships field', () => {
    it('should correctly change the tree model for relations field', () => {
      return firstCriteria.modifySelectValue(firstCriteria.relationshipsSelectElement, 'rel:this').then(() => {
        return expectInnerCriteriaRule('rel:this', 'object', 'set_to', ['anyObject']);
      });
    });
  });

  describe('Context & Relations', () => {
    it('should correctly change the tree model for context and relations fields', () => {
      return firstCriteria.modifySelectValue(firstCriteria.relationshipsSelectElement, 'rel:this').then(() => {
        return firstCriteria.modifySelectValue(firstCriteria.contextSelectElement, '1')
      }).then(() => {
        return expectInnerCriteriaRule('rel:this', 'object', 'set_to', ['1']);
      });
    });
  });

  describe('When configured with initial criteria', () => {
    it('should render the initial criteria', () => {
      var secondCriteria = page.getSecondCriteria();

      secondCriteria.waitForSelectedOption('rel:that');

      var expected = {
        metaText: 'initial',
        types: ['http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document'],
        relationships: ['rel:that'],
        createdBy: ['janedoe@doeandco.com'],
        context: ['2']
      };
      return expectForm(secondCriteria, expected);
    });
  });

  function expectForm(criteria, expectedMapping) {
    return criteria.getFreeTextValue().then((ftsValue) => {
      if (expectedMapping.metaText) {
        expect(ftsValue).to.equal(expectedMapping.metaText);
      }
      return criteria.getSelectedValue(criteria.typesSelectElement);
    }).then((selectedTypes) => {
      if (expectedMapping.types) {
        expect(selectedTypes).to.deep.equal(expectedMapping.types);
      }
      return criteria.getSelectedValue(criteria.relationshipsSelectElement);
    }).then((selectedRelationships) => {
      if (expectedMapping.relationships) {
        expect(selectedRelationships).to.deep.equal(expectedMapping.relationships);
      }
      return criteria.getSelectedValue(criteria.createdBySelectElement);
    }).then((selectedUsers) => {
      if (expectedMapping.createdBy) {
        expect(selectedUsers).to.deep.equal(expectedMapping.createdBy);
      }
      return criteria.getSelectedValue(criteria.contextSelectElement);
    }).then((selectedContext) => {
      if (expectedMapping.context) {
        expect(selectedContext).to.deep.equal(expectedMapping.context);
      }
    });
    // TODO: Test somehow the dates
  }

  function expectTypeCriteria(value) {
    return page.getTypeCriteriaRule().then((typeRule) => {
      expect(typeRule).to.exist;
      expect(typeRule.value).to.deep.equal(value);
    });
  }

  function expectInnerCriteriaRule(field, type, operator, value) {
    return page.getInnerCriteriaRule(field).then((rule) => {
      expect(rule).to.exist;
      expect(rule.field).to.equal(field);
      expect(rule.type).to.equal(type);
      expect(rule.operator).to.equal(operator);
      expect(rule.value).to.deep.equal(value);
    });
  }

  function getMidnightISODate() {
    var current = new Date();
    current.setHours(0);
    current.setMinutes(0);
    current.setSeconds(0);
    current.setMilliseconds(0);
    return current.toISOString();
  }

  function getEndOfDayUTC() {
    var current = new Date();
    current.setHours(23);
    current.setMinutes(59);
    current.setSeconds(59);
    current.setMilliseconds(0);
    return current.toISOString();
  }
});
