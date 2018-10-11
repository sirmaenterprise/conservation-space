'use strict';

let ModelManagementSandbox = require('./model-management.js').ModelManagementSandbox;

let Button = require('../../form-builder/form-control').Button;
let InputField = require('../../form-builder/form-control').InputField;
let FormControl = require('../../form-builder/form-control').FormControl;
let DatetimeField = require('../../form-builder/form-control').DatetimeField;
let CheckboxField = require('../../form-builder/form-control').CheckboxField;
let MultySelectMenu = require('../../form-builder/form-control').MultySelectMenu;
let SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;

describe('Models management fields section - browsing', () => {

  let fields;
  let section;
  let sandbox;

  function openPage(userLang, systemLang, modelId) {
    sandbox = new ModelManagementSandbox();
    sandbox.open(userLang, systemLang, modelId);

    section = sandbox.getModelSection();
    fields = section.getFieldsSection();
  }

  it('should show a message when an invalid model is selected', () => {
    // when the page is open with a model class instead of model definition
    openPage('en', 'bg', 'http://www.ontotext.com/proton/protontop#Entity');
    expect(fields.isIncorrectModelProvided()).to.eventually.be.true;
  });

  it('should have default filter configuration properly configured', () => {
    openPage('en', 'bg', 'MX1001');

    // system and hidden fields are by default disabled
    expect(fields.isSystemFilterEnabled()).to.eventually.be.false;
    expect(fields.isHiddenFilterEnabled()).to.eventually.be.false;

    // inherited fields are shown and keyword filter is empty
    expect(fields.isInheritedFilterEnabled()).to.eventually.be.true;
    expect(fields.isKeywordFilterEmpty()).to.eventually.be.true;
  });

  it('should properly filter fields based on a keyword search', () => {
    openPage('en', 'bg', 'MX1001');
    fields.filterByKeyword('Description');

    expect(fields.getField('description').isDisplayed()).to.eventually.be.true;
    expect(fields.getRegion('generalDetails').isCollapsed()).to.eventually.be.true;
    expect(fields.getRegion('specificDetails').isCollapsed()).to.eventually.be.true;

    fields.getRegion('generalDetails').getFields().then(items => expect(items.length).to.eq(0));
    fields.getRegion('specificDetails').getFields().then(items => expect(items.length).to.eq(0));
  });

  it('should show a message when no results are found due to filtering', () => {
    openPage('en', 'bg', 'MX1001');
    fields.filterByKeyword('random filter');

    // message that no results are found during the search is displayed
    expect(fields.isNoResultsMessageDisplayed()).to.eventually.be.true;

    // neither of the fields or regions should be present or visible
    expect(fields.isFieldDisplayed('description')).to.eventually.be.false;
    expect(fields.isRegionDisplayed('generalDetails')).to.eventually.be.false;
    expect(fields.isRegionDisplayed('specificDetails')).to.eventually.be.false;
  });

  it('should properly filter fields based on a display type', () => {
    openPage('en', 'bg', 'MX1001');
    fields.toggleSystem();

    expect(fields.getField('description').isDisplayed()).to.eventually.be.true;
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(8);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('System');
      expect(items[2].getLabel()).to.eventually.eq('Type');
      expect(items[3].getLabel()).to.eventually.eq('State');
      expect(items[4].getLabel()).to.eventually.eq('Level');
      expect(items[5].getLabel()).to.eventually.eq('Country');
      expect(items[6].getLabel()).to.eventually.eq('Checkbox');
      expect(items[7].getLabel()).to.eventually.eq('Date');
    });

    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(1);
      expect(items[0].getLabel()).to.eventually.eq('E-mail address');
    });
  });

  it('should properly filter fields based on inherited fields', () => {
    openPage('en', 'bg', 'MX1001');
    fields.toggleInherited();

    // inherited field should not be shown when inheritance is toggled
    expect(fields.isFieldDisplayed('description')).to.eventually.be.false;

    // rest of the fields and regions are not inherited and should not be affected
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(7);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('Type');
      expect(items[2].getLabel()).to.eventually.eq('State');
      expect(items[3].getLabel()).to.eventually.eq('Level');
      expect(items[4].getLabel()).to.eventually.eq('Country');
      expect(items[5].getLabel()).to.eventually.eq('Checkbox');
      expect(items[6].getLabel()).to.eventually.eq('Date');
    });

    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(1);
      expect(items[0].getLabel()).to.eventually.eq('E-mail address');
    });
  });

  it('should properly show all fields when all filters are enabled and not keyword is entered', () => {
    openPage('en', 'bg', 'MX1001');
    // show all the fields
    fields.toggleSystem();
    fields.toggleHidden();

    // base fields are not filtered and all should be visible
    expect(fields.getField('missing').getLabel()).to.eventually.eq('missing');
    expect(fields.getField('description').getLabel()).to.eventually.eq('Description');

    // base regions are not filtered and all should be visible
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    // regions are not collapsed since all fields inside are visible due to filtering
    expect(fields.getRegion('generalDetails').isCollapsed()).to.eventually.be.false;
    expect(fields.getRegion('specificDetails').isCollapsed()).to.eventually.be.false;

    // should show system fields from this region as well
    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(8);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('System');
      expect(items[2].getLabel()).to.eventually.eq('Type');
      expect(items[3].getLabel()).to.eventually.eq('State');
      expect(items[4].getLabel()).to.eventually.eq('Level');
      expect(items[5].getLabel()).to.eventually.eq('Country');
      expect(items[6].getLabel()).to.eventually.eq('Checkbox');
      expect(items[7].getLabel()).to.eventually.eq('Date');
    });

    // should show hidden fields from this region as well
    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(2);
      expect(items[0].getLabel()).to.eventually.eq('Hidden');
      expect(items[1].getLabel()).to.eventually.eq('E-mail address');
    });
  });

  it('should have proper region and fields displayed for selected model', () => {
    openPage('en', 'bg', 'MX1001');

    // inherited fields are not filtered since inherited filter is enabled by default
    expect(fields.getField('description').getLabel()).to.eventually.eq('Description');
    expect(fields.getRegion('generalDetails').getLabel()).to.eventually.eq('Base details');
    expect(fields.getRegion('specificDetails').getLabel()).to.eventually.eq('Specific details');

    // system filter is not toggled so system field is filtered
    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(7);
      expect(items[0].getLabel()).to.eventually.eq('Title');
      expect(items[1].getLabel()).to.eventually.eq('Type');
      expect(items[2].getLabel()).to.eventually.eq('State');
      expect(items[3].getLabel()).to.eventually.eq('Level');
      expect(items[4].getLabel()).to.eventually.eq('Country');
      expect(items[5].getLabel()).to.eventually.eq('Checkbox');
      expect(items[6].getLabel()).to.eventually.eq('Date');
    });

    // system filter is not toggled so hidden field is filtered
    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(1);
      expect(items[0].getLabel()).to.eventually.eq('E-mail address');
    });
  });

  it('should properly resolve which are inherited and not inherited fields', () => {
    openPage('en', 'bg', 'MX1001');
    // show all the fields
    fields.toggleSystem();
    fields.toggleHidden();

    expect(fields.getField('missing').isInherited()).to.eventually.be.false;

    expect(fields.getField('description').isInherited()).to.eventually.be.true;
    expect(fields.getField('description').getParent()).to.eventually.eq('(entity)');

    fields.getRegion('generalDetails').getFields().then(items => {
      expect(items.length).to.eq(8);
      items.forEach(item => expect(item.isInherited()).to.eventually.be.false);
    });

    fields.getRegion('specificDetails').getFields().then(items => {
      expect(items.length).to.eq(2);
      expect(items[0].isInherited()).to.eventually.be.false;
      expect(items[1].isInherited()).to.eventually.be.false;
    });
  });

  it('should properly resolve the type of input fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let title = items[0];
      expect(title.isEditable()).to.eventually.be.true;

      let input = new InputField(title.getEditControl());
      expect(input.getValue().then(value => value.trim())).to.eventually.eq('');
    });
  });

  it('should properly resolve the type of multi select fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let type = items[1];
      expect(type.isEditable()).to.eventually.be.true;

      let field = new MultySelectMenu(type.getEditControl());
      expect(field.getSelectedValue()).to.eventually.deep.eq([]);
      expect(field.getAvailableSelectChoices()).to.eventually.deep.eq(['ENG', 'INF', 'TSD', 'QLD']);
    });
  });

  it('should properly resolve the type of label fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      {
        let state = items[2];
        expect(state.isReadOnly()).to.eventually.be.true;

        let label = new FormControl(state.getViewControl());
        expect(label.getText()).to.eventually.eq('Approved');
      }

      {
        let level = items[3];
        expect(level.isReadOnly()).to.eventually.be.true;

        let label = new FormControl(level.getViewControl());
        expect(label.getText()).to.eventually.eq('High, Medium, Low');
      }
    });
  });

  it('should properly resolve the type of multi select fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let country = items[4];
      expect(country.isEditable()).to.eventually.be.true;

      let field = new SingleSelectMenu(country.getEditControl());
      expect(field.getSelectedValue()).to.eventually.eq('BGR');
      expect(field.getMenuValues()).to.eventually.deep.eq(['BGR', 'AUS', 'USA', 'GBR', 'FRA', 'CAN']);
    });
  });

  it('should properly resolve the type of checkbox fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let checkbox = items[5];
      expect(checkbox.isEditable()).to.eventually.be.true;

      let field = new CheckboxField(checkbox.getEditControl());
      expect(field.isChecked().then(checked => !!checked)).to.eventually.be.false;
    });
  });

  it('should properly resolve the type of date fields', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      let date = items[6];
      expect(date.isEditable()).to.eventually.be.true;

      let field = new DatetimeField(date.getEditControl());
      field.setDatetime(null, 'August/1/2018 13:35');
      // should take into account the format of the datetime field
      expect(field.getDate()).to.eventually.eq('August/01/2018 13:35');
    });
  });

  it('should properly resolve the type of picker fields', () => {
    openPage('en', 'bg', 'MX1001');

    let resource = fields.getField('resource');
    expect(resource.isEditable()).to.eventually.be.true;

    let button = new Button(resource.getEditControl());
    expect(button.isEnabled()).to.eventually.be.true;
  });

  it('should properly resolve mandatory fields', () => {
    openPage('en', 'bg', 'MX1001');

    expect(fields.getField('description').isMandatory()).to.eventually.be.true;

    fields.getRegion('generalDetails').getFields().then(items => {
      // title, type and state of this region are mandatory
      expect(items[0].isMandatory()).to.eventually.be.true;
      expect(items[1].isMandatory()).to.eventually.be.true;
      expect(items[2].isMandatory()).to.eventually.be.true;

      for (let i = 3; i < items.length; ++i) {
        // the rest of the fields are not mandatory
        expect(items[i].isMandatory()).to.eventually.be.false;
      }
    });

    fields.getRegion('specificDetails').getFields().then(items => {
      // e-mail field from this region is also mandatory
      expect(items[0].isMandatory()).to.eventually.be.true;
    });
  });

  it('should properly resolve hints or tooltips', () => {
    openPage('en', 'bg', 'MX1001');

    fields.getRegion('generalDetails').getFields().then(items => {
      // title field from this region should have a tooltip
      expect(items[0].hasTooltip()).to.eventually.be.true;
    });
  });
});