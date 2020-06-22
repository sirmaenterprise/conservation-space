var FormControl = require('./../../form-control.js').FormControl;
var InputField = require('./../../form-control.js').InputField;
var SingleSelectMenu = require('./../../form-control.js').SingleSelectMenu;
var MultySelectMenu = require('./../../form-control.js').MultySelectMenu;
var DatetimeField = require('./../../form-control.js').DatetimeField;
var RadioButtonGroup = require('./../../form-control.js').RadioButtonGroup;
var CheckboxField = require('./../../form-control.js').CheckboxField;
var Button = require('./../../form-control.js').Button;
var User = require('./../../form-control.js').User;
var Region = require('./../../form-control.js').Region;
var ObjectControl = require('./../../form-control.js').ObjectControl;
var SandboxPage = require('../../../page-object').SandboxPage;

var page = new SandboxPage();

// +[field1] - if a field has value
// -[field1] - if a field has no value
// [field1] IN ('opt1','opt2') - if a field has one of the listed values
// [field1] NOTIN ('opt1','opt2') - if a field has value not included in the listed values
// AND - conjunction of conditions
// OR - disjunction of conditions

describe('Condition validator', function () {

  var triggerField1;
  var triggerField2;
  var triggerField3;
  var triggerCheckbox;

  var textField1;
  var textarea1;
  var codelistfield1;
  var multiselectfield1;
  var datetimefield1;
  var radioButtonGroup1;
  var checkboxfield1;
  //var resourceField1;
  var userField1;
  var region1;
  var fields;
  var objectControl;

  beforeEach(() => {
    triggerField1 = new InputField($('#triggertextfield1-wrapper'));
    triggerField2 = new InputField($('#emf\\:triggertextfield2-wrapper'));
    triggerField3 = new InputField($('#triggertextfield3-wrapper'));
    triggerCheckbox = new CheckboxField($('#triggerCheckbox8-wrapper'));

    textField1 = new InputField($('#inputtext1-wrapper'));
    textarea1 = new InputField($('#textarea1-wrapper'));
    codelistfield1 = new SingleSelectMenu($('#codelistfield1-wrapper'));
    multiselectfield1 = new MultySelectMenu($('#multiselectfield1-wrapper'));
    datetimefield1 = new DatetimeField($('#datetimefield1-wrapper'));
    radioButtonGroup1 = new RadioButtonGroup($('#radioButtonGroup1-wrapper'));
    checkboxfield1 = new CheckboxField($('#checkboxfield1-wrapper'));
    //resourceField1 = new User($('#resourceField1-wrapper'));
    userField1 = new User($('#userField1-wrapper'));
    objectControl = new ObjectControl($('#objectPropertyMultiple-wrapper'));
    region1 = new Region($('.region:nth-child(1)'));

    fields = [textField1, textarea1, codelistfield1, multiselectfield1, datetimefield1, radioButtonGroup1, checkboxfield1];

    page.open('sandbox/form-builder/validation/condition');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  // ---------------------------------------------------------
  // +[field] and -[field] conditions
  // ---------------------------------------------------------
  // ENABLED-READONLY
  // - input text field1 should be READONLY if trigger field1 has value
  // - input text field1 should be ENABLED if trigger field1 has no value
  // - codelist field1 should be READONLY if trigger field1 has value
  // - codelist field1 should be ENABLED if trigger field1 has no value
  // - multiselect field1 should be READONLY if trigger field1 has value
  // - multiselect field1 should be ENABLED if trigger field1 has no value
  // - datetime field1 should be READONLY if trigger field1 has value
  // - datetime field1 should be ENABLED if trigger field1 has no value
  // - radiobutton group1 should be READONLY if trigger field1 has value
  // - radiobutton group1 should be ENABLED if trigger field1 has no value
  // - checkbox field1 should be READONLY if trigger field1 has value
  // - checkbox field1 should be ENABLED if trigger field1 has no value
  // - resource field1 should be READONLY if trigger field1 has value
  // - resource field1 should be ENABLED if trigger field1 has no value
  // - user field1 should be READONLY if trigger field1 has value
  // - user field1 should be ENABLED if trigger field1 has no value
  // - region1 should be READONLY if trigger field1 has value
  // - region1 should be ENABLED if trigger field1 has no value
  describe('ENABLED-READONLY', () => {
    it('should change fields to be READONLY when trigger field has value', () => {
      triggerField1.setValue(null, 'test');
      fields.forEach((field) => {
        expect(field.isPreview()).to.eventually.be.true;
      });
      //expect(resourceField1.isPreview()).to.eventually.be.true;
      //expect(userField1.isPreview()).to.eventually.be.true;
      region1.getFields().each((field) => {
        expect(new FormControl(field).isPreview()).to.eventually.be.true;
      });
    });

    it('should change fields to be ENABLED when trigger field has no value', () => {
      triggerField1.setValue(null, 'test');
      triggerField1.clearValue();
      fields.forEach((field) => {
        expect(field.isEditable()).to.eventually.be.true;
      });
      //expect(resourceField1.isEditable()).to.eventually.be.true;
      //expect(userField1.isEditable()).to.eventually.be.true;
      region1.getFields().each((field) => {
        expect(new FormControl(field).isEditable()).to.eventually.be.true;
      });
    });
  });

  // VISIBLE-HIDDEN
  // - input text field1 should be HIDDEN if trigger field2 has value
  // - input text field1 should be VISIBLE if trigger field2 has no value
  // - codelist field1 should be HIDDEN if trigger field2 has value
  // - codelist field1 should be VISIBLE if trigger field2 has no value
  // - multiselect field1 should be HIDDEN if trigger field2 has value
  // - multiselect field1 should be VISIBLE if trigger field2 has no value
  // - datetime field1 should be HIDDEN if trigger field2 has value
  // - datetime field1 should be VISIBLE if trigger field2 has no value
  // - radiobutton group1 should be HIDDEN if trigger field2 has value
  // - radiobutton group1 should be VISIBLE if trigger field2 has no value
  // - checkbox field1 should be HIDDEN if trigger field2 has value
  // - checkbox field1 should be VISIBLE if trigger field2 has no value
  // - resource field1 should be HIDDEN if trigger field2 has value
  // - resource field1 should be VISIBLE if trigger field2 has no value
  // - user field1 should be HIDDEN if trigger field2 has value
  // - user field1 should be VISIBLE if trigger field2 has no value
  // - region1 should be HIDDEN if trigger field2 has value
  // - region1 should be VISIBLE if trigger field2 has no value
  describe('VISIBLE-HIDDEN', () => {
    it('should change fields to be HIDDEN when trigger field has value', () => {
      triggerField2.setValue(null, 'test');
      fields.forEach((field) => {
        expect(field.isVisible(), 'Field should be hidden').to.eventually.be.false;
      });
      expect(objectControl.isVisible(), 'Object control should be hidden').to.eventually.be.false;
      //expect(resourceField1.isVisible()).to.eventually.be.false;
      //expect(userField1.isVisible()).to.eventually.be.false;
      region1.getFields().each((field) => {
        expect(new FormControl(field).isVisible(), 'Region field should be hidden').to.eventually.be.false;
      });
    });

    it('should change fields to be VISIBLE when trigger field has no value', () => {
      triggerField2.setValue(null, 'test');
      triggerField2.clearValue();
      fields.forEach((field) => {
        expect(field.isVisible()).to.eventually.be.true;
      });
      expect(objectControl.isVisible()).to.eventually.be.true;
      //expect(resourceField1.isVisible()).to.eventually.be.true;
      //expect(userField1.isVisible()).to.eventually.be.true;
      region1.getFields().each((field) => {
        expect(new FormControl(field).isVisible()).to.eventually.be.true;
      });
    });

    it('shound display HIDDEN fields in preview mode, should become editable when trigger is pressed in edit mode', () => {
      // test covering CMF-27288
      var previewToggle = new Button($('#togglePreview'));
      var HIDDEN_FIELD = '#hiddenProperty-wrapper';
      expect($(HIDDEN_FIELD).isPresent()).to.eventually.be.false;
      previewToggle.click();
      browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
      expect($(HIDDEN_FIELD).isPresent()).to.eventually.be.true;
      previewToggle.click();
      browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
      triggerCheckbox.toggleCheckbox();
      expect($(HIDDEN_FIELD).isPresent()).to.eventually.be.true;
    });
  });

  // MANDATORY-OPTIONAL
  // - input text field1 should be OPTIONAL if input trigger field3 has value
  // - input text field1 should be MANDATORY if input trigger field3 has no value
  // - codelist field1 should be OPTIONAL if input trigger field3 has value
  // - codelist field1 should be MANDATORY if input trigger field3 has no value
  // - multiselect field1 should be OPTIONAL if input trigger field3 has value
  // - multiselect field1 should be MANDATORY if input trigger field3 has no value
  // - datetime field1 should be OPTIONAL if input trigger field3 has value
  // - datetime field1 should be MANDATORY if input trigger field3 has no value
  // - radiobutton group1 should be OPTIONAL if input trigger field3 has value
  // - radiobutton group1 should be MANDATORY if input trigger field3 has no value
  // - checkbox field1 should be OPTIONAL if input trigger field3 has value
  // - checkbox field1 should be MANDATORY if input trigger field3 has no value
  // - resource field1 should be OPTIONAL if input trigger field3 has value
  // - resource field1 should be MANDATORY if input trigger field3 has no value
  // - user field1 should be OPTIONAL if input trigger field3 has value
  // - user field1 should be MANDATORY if input trigger field3 has no value
  // - region1 should be OPTIONAL if input trigger field3 has value
  // - region1 should be MANDATORY if input trigger field3 has no value
  describe('MANDATORY-OPTIONAL', () => {
    it('should change fields to be OPTIONAL when trigger field has no value', () => {
      triggerField3.setValue(null, 'test');
      triggerField3.clearValue();
      fields = [textField1, textarea1, codelistfield1, multiselectfield1, datetimefield1, radioButtonGroup1];
      fields.forEach((field) => {
        expect(field.isMandatory(), `Field has to be optional!`).to.eventually.be.false;
      });
      expect(objectControl.isMandatory(), 'Object property field has to be optional!').to.eventually.be.false;
      //expect(checkboxfield1.isMandatory()).to.eventually.be.false; // not applicable
      //expect(resourceField1.isMandatory()).to.eventually.be.false;
      //expect(userField1.isMandatory()).to.eventually.be.false; // not applicable
      region1.getFields().each((field) => {
        expect(new FormControl(field).isMandatory(), `Region field has to be optional!`).to.eventually.be.false;
      });
    });

    it('should change fields to be MANDATORY when trigger field has value', () => {
      triggerField3.setValue(null, 'test');
      fields = [textField1, textarea1, codelistfield1, multiselectfield1, datetimefield1, radioButtonGroup1];
      fields.forEach((field) => {
        expect(field.isMandatory(), 'Field has to be mandatory!').to.eventually.be.true;
      });
      expect(objectControl.isMandatory(), 'Object property field has to be mandatory!').to.eventually.be.true;
      //expect(checkboxfield1.isMandatory()).to.eventually.be.true; // not applicable
      //expect(resourceField1.isMandatory()).to.eventually.be.true;
      //expect(userField1.isMandatory()).to.eventually.be.true; // not applicable
      region1.getFields().each((field) => {
        expect(new FormControl(field).isMandatory(), 'Region field has to be mandatory!').to.eventually.be.true;
      });
    });

    //Connected with CMF-22250.
    it('should trigger fields to be MANDATORY and display message, then remove message when fields are set to OPTIONAL', ()=> {
      triggerField3.setValue(null, 'test');
      fields = [textField1, textarea1, codelistfield1, multiselectfield1, datetimefield1, radioButtonGroup1];
      fields.forEach((field) => {
        expect(field.isMandatory()).to.eventually.be.true;
      });
      //validation messages should be displayed too.
      expect(codelistfield1.getHtml('#codelistfield1-wrapper .form-field-wrapper')).to.eventually.contains('id="mandatory"');
      expect(multiselectfield1.getHtml('#multiselectfield1-wrapper .form-field-wrapper')).to.eventually.contains('id="mandatory"');

      triggerField3.clearValue();
      fields = [textField1, textarea1, codelistfield1, multiselectfield1, datetimefield1, radioButtonGroup1];
      fields.forEach((field) => {
        expect(field.isMandatory()).to.eventually.be.false;
      });
      expect(codelistfield1.getHtml('#codelistfield1-wrapper .form-field-wrapper')).to.eventually.not.contains('id="mandatory"');
      expect(multiselectfield1.getHtml('#multiselectfield1-wrapper .form-field-wrapper')).to.eventually.not.contains('id="mandatory"');
    });

  });

  // regression test for CMF-26186
  describe('datetime field', () => {
    it('should render correct date when hid and shown back', () => {
      expect(datetimefield1.isVisible()).to.eventually.be.true;
      expect(datetimefield1.getDate()).to.eventually.equal('22.12.15');
      triggerField2.setValue(null, 'test');
      expect(datetimefield1.isVisible()).to.eventually.be.false;
      triggerField2.clearValue();
      expect(datetimefield1.isVisible()).to.eventually.be.true;
      expect(datetimefield1.getDate()).to.eventually.equal('');
    });
  });

  // Overriding the base attributes from the definition
  //
  // readonly field1 should be EDITABLE if trigger field1 has value
  // hidden field1 should be VISIBLE if trigger field1 has value
  // mandatory field1 should be OPTIONAL if trigger field1 has value

  //
  // [field1] IN ('opt1','opt2') and [field1] NOTIN ('opt1','opt2') conditions
  //
  // Testing also if codelist field can trigger the condition validator
  //
  // - input text field1 should be EDITABLE if trigger codelist field1 has value 'opt1' [codelistfield1] IN ('opt1', 'opt2')
  // - input text field1 should be EDITABLE if trigger codelist field1 has value 'opt2' [codelistfield1] IN ('opt1', 'opt2')
  // - input text field1 should be READONLY if trigger codelist field1 has value 'opt3' [codelistfield1] NOTIN ('opt1', 'opt2')
  //
  // Testing also if radiobutton group can trigger the condition validator
  // - input text field1 should be EDITABLE if trigger radiobutton group1 has value 'opt1' [radiobuttongroup1] IN ('opt1', 'opt2')
  // - input text field1 should be EDITABLE if trigger radiobutton group1 has value 'opt2' [radiobuttongroup1] IN ('opt1', 'opt2')
  // - input text field1 should be READONLY if trigger radiobutton group1 has value 'opt3' [radiobuttongroup1] NOTIN ('opt1', 'opt2')
  //
  // Testing also if multiselect field can trigger the condition validator
  // - input text field1 should be EDITABLE if trigger multiselect field1 has value 'opt1' [multiselectfield1] IN ('opt1', 'opt2')
  // - input text field1 should be EDITABLE if trigger multiselect field1 has value 'opt2' [multiselectfield1] IN ('opt1', 'opt2')
  // - input text field1 should be READONLY if trigger multiselect field1 has value 'opt3' [multiselectfield1] NOTIN ('opt1', 'opt2')
  //

  //
  // AND/OR
  //
  // - input text field1 should be EDITABLE if +[field2] AND -[field3] AND (+[field4] OR +[field5])
  // - input text field1 should be HIDDEN if -[field4] OR -[field5]
  // - input text field1 should be MANDATORY if ([codelist6] IN ('opt1', 'opt2')) AND +[field4]

  // Test in idoc in edit mode:
  // In ODW:
  //
  // In DTW:
  //
  // Test in idoc in preview mode:
  // In ODW:
  //
  // In DTW:
  //
  // Test in dialogs:
  //

});
