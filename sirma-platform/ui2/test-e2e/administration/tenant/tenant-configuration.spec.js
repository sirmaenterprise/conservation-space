var TenantConfiguration = require('./tenant-configuration').TenantConfiguration;
var Dialog = require('../../components/dialog/dialog');
var Notification = require('../../components/notification').Notification;
var SandboxPage = require('../../page-object').SandboxPage;

const DIALOG = '.modal-dialog';
const FILTER_INPUT = '.filter-input';
const URI_INPUT = '#uri\\.sample\\.website';
const FLOAT_INPUT = '#float\\.sample\\.real';
const STRING_INPUT = '#string\\.sample\\.string';
const BOOLEAN_INPUT = '#boolean\\.sample\\.flag';
const FILESIZE_INPUT = '#file\\.sample\\.maxsize';
const FILENAME_INPUT = '#file\\.sample\\.filename';
const PASSWORD_INPUT = '#password\\.sample\\.real';

//select the immediate sibling of the boolean input field
const BOOLEAN_ELEMENT = BOOLEAN_INPUT + ' ~i';

var page = new SandboxPage();

describe('Tenant Configuration', () => {

  var tenantConfig;

  beforeEach(() => {
    page.open('/sandbox/administration/tenant');
    tenantConfig = new TenantConfiguration();
    tenantConfig.expandAllRegions();
  });

  describe('save button', ()=> {

    it('should be displayed by default', ()=> {
      expect(tenantConfig.getSaveButton().isPresent()).to.eventually.be.true;
      expect(tenantConfig.getSaveButton().isDisplayed()).to.eventually.be.true;
    });

    it('should be disabled when the configuration contains invalid mandatory field', ()=> {
      //send backspace to invalidate the field
      tenantConfig.modifyFieldValue(FLOAT_INPUT, "a");
      expect(tenantConfig.isSaveButtonDisabled()).to.eventually.be.true;
    });

    it('should be enabled when the configuration is complete', ()=> {
      expect(tenantConfig.isSaveButtonDisabled()).to.eventually.be.false;
    });

    it('should prompt with confirmation dialog when input field has been modified', ()=> {
      tenantConfig.modifyFieldValue(FLOAT_INPUT, 123);
      tenantConfig.getSaveButton().click().then(() => {
        dialogWindow = new Dialog($(DIALOG));
        expect(dialogWindow.isPresent()).to.eventually.be.true;
      });
    });

    it('should prompt with confirmation dialog when boolean field has been modified', ()=> {
      tenantConfig.getSpecificElement(BOOLEAN_ELEMENT).click().then(() => {
        tenantConfig.getSaveButton().click().then(() => {
          dialogWindow = new Dialog($(DIALOG));
          expect(dialogWindow.isPresent()).to.eventually.be.true;
        });
      });
    });

    it('should prompt with notification when dialog has been confirmed', ()=> {
      tenantConfig.modifyFieldValue(FLOAT_INPUT, 123);
      tenantConfig.getSaveButton().click().then(() => {
        dialogWindow = new Dialog($(DIALOG));
        dialogWindow.waitUntilOpened();
        dialogWindow.ok();
        note = new Notification();
        expect(note.element.isPresent()).to.eventually.be.true;
      });
    });
  });

  describe('cancel button', ()=> {

    it('should be displayed by default', ()=> {
      expect(tenantConfig.getCancelButton().isPresent()).to.eventually.be.true;
      expect(tenantConfig.getCancelButton().isDisplayed()).to.eventually.be.true;
    });

    it('should be enabled by default', ()=> {
      expect(tenantConfig.isCancelButtonDisabled()).to.eventually.be.false;
    });

    it('should prompt with notification when clicked', ()=> {
      return tenantConfig.getCancelButton().click().then(() => {
        note = new Notification();
        expect(note.element.isPresent()).to.eventually.be.true;
      });
    });

    it('should clear the filter keyword', ()=> {
      tenantConfig.modifyFieldValue(FILTER_INPUT, 'filter');
      tenantConfig.waitUntilVisible();
      return tenantConfig.getCancelButton().click().then(() => {
        expect(tenantConfig.getFieldValue(FILTER_INPUT)).to.eventually.equal('');
      });
    });
  });

  describe('configuration form', ()=> {

    it('should have the input fields configured with values', ()=> {
      expect(tenantConfig.getFieldValue(FILESIZE_INPUT)).to.eventually.equal('1048');
      expect(tenantConfig.getFieldValue(FLOAT_INPUT)).to.eventually.equal('123.456');
      expect(tenantConfig.getFieldValue(FILENAME_INPUT)).to.eventually.equal('file.txt');
      expect(tenantConfig.getFieldValue(STRING_INPUT)).to.eventually.equal('test string');
      expect(tenantConfig.getFieldValue(URI_INPUT)).to.eventually.equal('https://www.youtube.com');
      expect(tenantConfig.getFieldValue(BOOLEAN_INPUT)).to.eventually.equal('on');
      expect(tenantConfig.getFieldValue(PASSWORD_INPUT),'password').to.eventually.equal('testPassword');
      expect(tenantConfig.getFieldType(PASSWORD_INPUT)).to.eventually.equal('password');
      expect(tenantConfig.getFieldType(STRING_INPUT)).to.eventually.equal('text');

    });

    it('should have the labels correctly configured', ()=> {
      var fields = tenantConfig.getAllLabelFields();
      //checkbox fields are wrapped in labels and they are all captured by the selector
      expect(fields.count()).to.eventually.equal(9);
      fields.each((field, number) => {
        //exclude preview and print checkboxes which are not displayed.
        if (number === 1 || number === 2) {
          expect(field.isDisplayed()).to.eventually.be.false;
        } else {
          expect(field.isDisplayed()).to.eventually.be.true;
        }
      });
    });

    it('should have the tooltips correctly configured', ()=> {
      var fields = tenantConfig.getAllTooltipFields();
      expect(fields.count()).to.eventually.equal(7);
      fields.each((field) => {
        expect(field.element(by.css('i')).isDisplayed()).to.eventually.be.true;
      });
    });
  });

  describe('configurations key word filtration', ()=> {

    it('should have the filter button and field rendered & visible', ()=> {
      expect(tenantConfig.isFieldDisplayed(FILTER_INPUT)).to.eventually.be.true;
      expect(tenantConfig.getFilterButton().isPresent()).to.eventually.be.true;
      expect(tenantConfig.getFilterButton().isDisplayed()).to.eventually.be.true;
    });

    it('should not filter configurations when key word is empty', ()=> {
      tenantConfig.modifyFieldValue(FILTER_INPUT, '');
      tenantConfig.waitUntilVisible();
      return tenantConfig.getFilterButton().click().then(() => {
        tenantConfig.expandAllRegions();
        expect(tenantConfig.isFieldDisplayed(FILESIZE_INPUT)).to.eventually.be.true;
        expect(tenantConfig.isFieldDisplayed(FLOAT_INPUT)).to.eventually.be.true;
        expect(tenantConfig.isFieldDisplayed(BOOLEAN_ELEMENT)).to.eventually.be.true;
        expect(tenantConfig.isFieldDisplayed(FILENAME_INPUT)).to.eventually.be.true;
        expect(tenantConfig.isFieldDisplayed(STRING_INPUT)).to.eventually.be.true;
        expect(tenantConfig.isFieldDisplayed(URI_INPUT)).to.eventually.be.true;
      });
    });

    it('should filter single configurations when key word is entered', ()=> {
      tenantConfig.modifyFieldValue(FILTER_INPUT, 'size');
      tenantConfig.waitUntilVisible();
      return tenantConfig.getFilterButton().click().then(() => {
        tenantConfig.expandAllRegions();
        expect(tenantConfig.isFieldDisplayed(FILESIZE_INPUT)).to.eventually.be.true;
        expect(tenantConfig.isFieldPresent(BOOLEAN_ELEMENT)).to.eventually.be.false;
        expect(tenantConfig.isFieldPresent(FLOAT_INPUT)).to.eventually.be.false;
        expect(tenantConfig.isFieldPresent(FILENAME_INPUT)).to.eventually.be.false;
        expect(tenantConfig.isFieldPresent(STRING_INPUT)).to.eventually.be.false;
        expect(tenantConfig.isFieldPresent(URI_INPUT)).to.eventually.be.false;
      });
    });

    it('should filter multiple configurations when key word is entered', ()=> {
      tenantConfig.modifyFieldValue(FILTER_INPUT, 'file');
      tenantConfig.waitUntilVisible();
      return tenantConfig.getFilterButton().click().then(() => {
        tenantConfig.expandAllRegions();
        expect(tenantConfig.isFieldDisplayed(FILESIZE_INPUT)).to.eventually.be.true;
        expect(tenantConfig.isFieldDisplayed(FILENAME_INPUT)).to.eventually.be.true;
        expect(tenantConfig.isFieldPresent(BOOLEAN_ELEMENT)).to.eventually.be.false;
        expect(tenantConfig.isFieldPresent(FLOAT_INPUT)).to.eventually.be.false;
        expect(tenantConfig.isFieldPresent(STRING_INPUT)).to.eventually.be.false;
        expect(tenantConfig.isFieldPresent(URI_INPUT)).to.eventually.be.false;
      });
    });
  });

});
