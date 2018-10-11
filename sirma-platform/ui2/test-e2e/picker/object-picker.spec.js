var ObjectPickerSandbox = require('./object-picker').ObjectPickerSandbox;
var SEARCH_EXTENSION = require('./object-picker').SEARCH_EXTENSION;
var TEST_EXTENSION = require('./object-picker').TEST_EXTENSION;

describe('Object picker', () => {

  var page = new ObjectPickerSandbox();

  var objectPicker;
  beforeEach(() => {
    page.open();
  });

  describe('When configured with extensions', () => {
    beforeEach(() => {
      objectPicker = page.getEmbeddedPicker();
    });

    it('should have navigation for extensions', () => {
      objectPicker.waitUntilNavigationIsVisible();
      objectPicker.getExtensionTabs().then((items) => {
        expect(items.length).to.be.above(1);
      });
    });

    it('should switch between extensions via navigation', () => {
      expect(objectPicker.isExtensionVisible(SEARCH_EXTENSION)).to.eventually.be.true;
      expect(objectPicker.isExtensionVisible(TEST_EXTENSION)).to.eventually.be.false;

      objectPicker.switchToExtension(TEST_EXTENSION);
      expect(objectPicker.isExtensionVisible(SEARCH_EXTENSION)).to.eventually.be.false;
      expect(objectPicker.isExtensionVisible(TEST_EXTENSION)).to.eventually.be.true;
    });
  });

  describe('When configured with one extension', () => {
    it('should not show the extensions navigation', () => {
      var objectPickerDialog = page.openSingleExtensionPickerDialog();
      objectPicker = objectPickerDialog.getObjectPicker();
      expect(objectPicker.isNavigationPresent()).to.eventually.be.false;
    });
  });

  describe('When embedded in a dialog', () => {
    var objectPickerDialog;
    var objectPicker;

    beforeEach(() => {
      objectPickerDialog = page.openPickerDialog();
      objectPicker = objectPickerDialog.getObjectPicker();
    });

    it('should display the picker inside the dialog', () => {
      expect(objectPicker.isExtensionVisible(SEARCH_EXTENSION)).to.eventually.be.true;
    });

    it('should have a button for OK', () => {
      expect(objectPickerDialog.getOkButton().isDisplayed()).to.eventually.be.true;
    });

    it('should have a button for Cancel', () => {
      expect(objectPickerDialog.getCancelButton().isDisplayed()).to.eventually.be.true;
    });
  });

});

