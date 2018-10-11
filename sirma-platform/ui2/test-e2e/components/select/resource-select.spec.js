var SandboxPage = require('../../page-object').SandboxPage;

var page = new SandboxPage();

describe('Resource select', ()=> {

  describe('When the select is opened', ()=> {

    it('Then a user can be selected', ()=> {
      page.open('/sandbox/components/select');

      var selectElement = element(by.css('#seip-resource-select span.select2-selection'));
      selectElement.click();

      var loadingElement = element(by.css('.select2-results .loading-results'));
      browser.wait(EC.not(EC.presenceOf(loadingElement)), DEFAULT_TIMEOUT);

      var selectResults = element.all(by.css('.select2-results li')).then(function (items) {
        expect(items.length).to.equal(1);
        expect(items[0].getText()).to.eventually.equal('Administrator');
        items[0].click();
      });

      var selection = element(by.css('#seip-resource-select span.select2-selection .select2-selection__choice'));
      expect(selection.getAttribute('title')).to.eventually.equal('Administrator');
    });
  });
});