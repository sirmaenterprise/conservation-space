var ObjectPickerSandbox = require('./object-picker').ObjectPickerSandbox;
var ObjectPickerDialog = require('./object-picker').ObjectPickerDialog;
var InstanceList = require('../instance/instance-list').InstanceList;

var SEARCH_EXTENSION = require('./object-picker').SEARCH_EXTENSION;
var BASKET_EXTENSION = require('./object-picker').BASKET_EXTENSION;
var RECENT_EXTENSION = require('./object-picker').RECENT_EXTENSION;

describe('Selecting in Object picker', () => {

  var page = new ObjectPickerSandbox();

  beforeEach(() => {
    page.open();
  });

  describe('When selecting items in the object picker', () => {

    var picker;
    beforeEach(() => {
      picker = page.getEmbeddedPicker();
    });

    it('should display a basket tab', () => {
      picker.getExtensionTab(BASKET_EXTENSION);
    });

    it('should display a recently used tab', () => {
      picker.getExtensionTab(RECENT_EXTENSION);
    });

    it('should display the selected items count in the basket tab', () => {
      picker.waitForBasketCount(0);

      var search = picker.getSearch();
      search.getResults().clickResultItem(0);
      search.getResults().clickResultItem(1);
      search.getResults().clickResultItem(2);
      search.getResults().clickResultItem(1);

      picker.waitForBasketCount(2);
    });
  });

  describe('When object picker is for single selection mode', () => {
    it('should display the last selected item in the basket tab', () => {
      var pickerDialog = page.openPickerDialog();
      var picker = pickerDialog.getObjectPicker();

      var search = picker.getSearch();
      search.getResults().clickResultItem(0);
      search.getResults().clickResultItem(1);
      search.getResults().clickResultItem(2);
      search.getResults().clickResultItem(3);
      search.getResults().clickResultItem(2);

      var basketExtension = picker.switchToExtension(BASKET_EXTENSION);
      var basket = new InstanceList(basketExtension);

      expect(basket.getItemsCount()).to.eventually.equal(1);
      return basket.getItems().then((items) => {
        return expect(items[0].element.getText()).to.eventually.equal('Object #2');
      });
    });
  });

  describe('When object picker is for multi selection mode', () => {
    it('should display all selected items in the basket tab', () => {
      var picker = page.getEmbeddedPicker();
      var search = picker.getSearch();
      search.getResults().clickResultItem(0);
      search.getResults().clickResultItem(1);
      search.getResults().clickResultItem(2);
      search.getResults().clickResultItem(3);
      search.getResults().clickResultItem(1);

      var basketExtension = picker.switchToExtension(BASKET_EXTENSION);
      var basket = new InstanceList(basketExtension);

      expect(basket.getItemsCount()).to.eventually.equal(3);
      return basket.getItems().then((items) => {
        return Promise.all([
          expect(items[0].element.getText()).to.eventually.equal('Object #0'),
          expect(items[1].element.getText()).to.eventually.equal('Object #2'),
          expect(items[2].element.getText()).to.eventually.equal('Object #3')
        ]);
      });
    });

    it('should deselect items in the search results if deselected from the basket', ()=> {
      var picker = page.getEmbeddedPicker();
      var search = picker.getSearch();
      search.getResults().clickResultItem(0);
      search.getResults().clickResultItem(1);

      var basketExtension = picker.switchToExtension(BASKET_EXTENSION);
      var basket = new InstanceList(basketExtension);

      return basket.getItems().then((items) => {
        items[0].select();
        expect(basket.getItemsCount()).to.eventually.equal(1);
        picker.switchToExtension(SEARCH_EXTENSION);

        return Promise.all([
          expect(search.getResults().isResultSelected(0)).to.eventually.be.false,
          expect(search.getResults().isResultSelected(1)).to.eventually.be.true
        ]);
      });
    });
  });

  describe('When opened with current object', () => {
    it('should display current object as selection in the basket', () => {
      let picker = page.getEmbeddedPicker();
      // Current object is selected by default in the embedded picker so open another picker with that selection
      picker.getSearch().getCriteria().getSearchBar().openContextPicker();

      let nestedPicker = new ObjectPickerDialog().getObjectPicker();
      nestedPicker.waitForBasketCount(1);

      var basketExtension = nestedPicker.switchToExtension(BASKET_EXTENSION);
      var basket = new InstanceList(basketExtension);

      expect(basket.getItemsCount()).to.eventually.equal(1);
      return basket.getItems().then((items) => {
        return expect(items[0].element.getText()).to.eventually.equal('Current object');
      });
    });
  });
});