var MultySelectMenu = require('../../form-builder/form-control.js').MultySelectMenu;
var SandboxPage = require('../../page-object').SandboxPage;

var page = new SandboxPage();

describe('Select component', function () {

  beforeEach(function () {
    page.open('/sandbox/components/select');
  });

  it('should display provided data', function () {
    var selectElement = element(by.css('#seip-multiple-select span.select2-selection'));
    selectElement.click();
    var selectResults = element.all(by.css('.select2-results li')).then(function (items) {
      expect(items.length).to.equal(3);
      expect(items[1].getText()).to.eventually.equal('Option 2');
      items[1].click();
      browser.wait(EC.visibilityOf($('#seip-multiple-select li[title="Option 2"]')), DEFAULT_TIMEOUT);
      return $('#seip-multiple-select input').sendKeys(protractor.Key.ESCAPE, 'Tag 1', protractor.Key.ENTER);
    }).then(function () {
      browser.wait(EC.visibilityOf($('#seip-multiple-select li[title="Tag 1"]')), DEFAULT_TIMEOUT);
      browser.executeScript('return $("select").select2().val();').then(function (result) {
        expect(result).to.have.length(2);
        expect(result).to.include('2');
        expect(result).to.include('Tag 1');
      });
    });
  });

  it('should automatically select first non-disabled item if defaultToFirstValue option is true', function () {
    expect($('#seip-single-select_default_to_first_value').getText()).to.eventually.equal('key2');
  });

  it('should automatically select first item if defaultToSingleValue option is true and there is only one item', function () {
    expect($('#seip-single-select_default_to_single_value').getText()).to.eventually.equal('key');
  });

  it('should support automatic reload when data changes', function() {
    expect($('#seip-single-select_reload_on_data_change_value').getText()).to.eventually.equal('key1');

    $('#reload-button').click();

    expect($('#seip-single-select_reload_on_data_change_value').getText()).to.eventually.equal('key2');
  });

  describe('When the formatting is overridden in the select component.', function () {
    it('Then the selection should not be escaped and must be in strong font.', function () {
      var renderedSelectionElement = element(by.css('#seip-formatted-select span.select2-selection__rendered'));
      expect(browser.executeScript("return arguments[0].innerHTML;", renderedSelectionElement)).to.eventually.equal('<b>VaLuE</b>');
    });

    it('Then the results should not be escaped and must be in italics font.', function () {
      var formattedSelectElement = element(by.css('#seip-formatted-select span.select2-selection'));
      formattedSelectElement.click();
      return element.all(by.css('.select2-results li')).then(function (items) {
        expect(items.length).to.equal(1);
        expect(browser.executeScript("return arguments[0].innerHTML;", items[0])).to.eventually.equal('<span data-value="key1"><i>VaLuE</i></span>');
      });
    });
  });

  it('should not open automatically when an item is deselected', () => {
    var multiSelectElement = $('#seip-multiple-select');
    var menu = new MultySelectMenu(multiSelectElement);
    return menu.selectFromMenuByValue(1).then(() => {
      return menu.removeFromSelection(undefined, 1, true);
    }).then(() => {
      return expect($$('.select2-dropdown').count()).to.eventually.equal(0);
    });
  });

});
