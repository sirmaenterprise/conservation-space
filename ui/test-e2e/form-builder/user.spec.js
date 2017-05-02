var UserField = require('./form-control.js').User;
var FormWrapper = require('./form-wrapper').FormWrapper;
var SandboxPage = require('../page-object').SandboxPage;

const HIDDEN_USER = '#userHidden';
const PREVIEW_USER = '#userPreview';
const PREVIEW_EMPTY_USER = '#userPreviewEmpty';
const TOOLTIP = 'seip-hint';

var page = new SandboxPage();

describe('User control', ()=> {
  var userField, userPreviewField, userHiddenField;
  var formWrapper;

  beforeEach(() => {
    formWrapper = new FormWrapper($('.container'));
    userField = new UserField();
    userPreviewField = new UserField($(PREVIEW_USER));
    userHiddenField = new UserField($(HIDDEN_USER));
    page.open('/sandbox/form-builder/user');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  describe('tooltips', ()=> {
    it('should be displayed correctly in edit mode and hidden in preview mode', ()=> {
      var fields = element.all(by.className(TOOLTIP));
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'user information edit').to.eventually.be.true;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview empty edit').to.eventually.be.false;
      expect(fields.get(2).element(by.css('i')).isDisplayed(), 'hidden edit').to.eventually.be.false;

      formWrapper.togglePreviewMode();
      expect(fields.get(0).element(by.css('i')).isDisplayed(), 'user information preview').to.eventually.be.false;
      expect(fields.get(1).element(by.css('i')).isDisplayed(), 'preview empty preview').to.eventually.be.false;
      expect(fields.get(2).element(by.css('i')).isDisplayed(), 'hidden preview').to.eventually.be.false;
    });
  });

  describe('Value is correctly displayed in preview mode', ()=> {
    it('Should be displayed correctly', ()=> {
      formWrapper.togglePreviewMode();

      expect(userField.isPresent(TOOLTIP)).to.eventually.be.false;
      expect(userPreviewField.isHeaderPresent()).to.eventually.be.true;
      expect(userPreviewField.getHeaderText()).to.eventually.equal('Compact header');
    });
  });

  describe('Field value is available in the DOM if field is with displayType=hidden', ()=> {
    it('Hidden user field should not be visible, but should contain its value in the DOM', ()=> {
      expect(userField.isVisible(HIDDEN_USER)).to.eventually.be.false;
      expect(userHiddenField.getHeaderText()).to.eventually.equal('Compact header hidden');
    });
    it('Should be displayed when preview is toggled', ()=> {
      formWrapper.togglePreviewMode();
      expect(userField.isPresent(HIDDEN_USER)).to.eventually.be.true;
    });
  });

  describe('Checking behaviour when previewEmpty value is switched to true/false', ()=> {
    it('Preview empty field should not be visible when the preview is toggled.', ()=> {
      formWrapper.togglePreviewMode();
      expect(userField.isVisible(PREVIEW_EMPTY_USER)).to.eventually.be.false;
    });
    it('Field should be visible when preview is toggled.', ()=> {
      formWrapper.togglePreviewMode();
      expect(userField.isVisible(PREVIEW_USER)).to.eventually.be.true;
    });
  });
});