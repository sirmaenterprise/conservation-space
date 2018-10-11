var SandboxPage = require('../../page-object').SandboxPage;
var SingleSelectMenu = require('../../form-builder/form-control').SingleSelectMenu;

const SANDBOX_URL = '/sandbox/components/select/object-type';

var page = new SandboxPage();

//TODO: create select PO and refactor all tests
describe('Object type select', () => {
  var selectElement;
  var selectMenu;

  beforeEach(function () {
    page.open(SANDBOX_URL);
    selectElement = $('#seip-object-type-select span.select2-selection');

    browser.wait(EC.visibilityOf(selectElement), DEFAULT_TIMEOUT);

    selectMenu = new SingleSelectMenu(selectElement);

    selectMenu.open();
  });

  it('should have default value any object', () => {
    expect(selectElement.getText()).to.eventually.eq("Any Object");
  });

  it('should have any object select type at first position inside the dropdown list', () => {
    expect(browser.executeScript("return arguments[0].innerHTML;", selectMenu.getOptionByName('Any Object'))).to.eventually.equal('<span data-value="anyObject"><span class="type-undefined level-undefined">Any Object</span></span>');
  });

  it('should visualize results in options and option groups', () => {
    expect(browser.executeScript("return arguments[0].innerHTML;", selectMenu.getOptionByName('Document'))).to.eventually.equal('<span data-value="http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document"><span class="type-class level-1">Document</span></span>');
    expect(browser.executeScript("return arguments[0].innerHTML;", selectMenu.getOptionByName('Common document'))).to.eventually.equal('<span data-value="OT210027"><span class="type-definition level-2">Common document</span></span>');
  });

  it('should make object types selectable', ()=> {
    element.all(by.css('.select2-results li')).then(function (items) {
      items[1].click();
    });
    var inputElement = $('#seip-object-type-select-input');
    expect(inputElement.getAttribute('value')).to.eventually.equal('http://ittruse.ittbg.com/ontology/enterpriseManagementFramework#Document');
  });

  it('should be filterable', () => {
    selectMenu.filter('document');
    expect(element.all(by.css('.select2-results li')).count()).to.eventually.eq(12);
  });

});

describe('Object type select', () => {
  it('should be able to disable types without corresponding definition', () => {
    page.open(SANDBOX_URL, '?disableTypesWithoutDefinition=true');
    var objectTypeSelect = new SingleSelectMenu($('#seip-object-type-select span.select2-selection'));

    objectTypeSelect.open();

    expect(objectTypeSelect.isOptionDisabled('Document')).to.eventually.be.true;

    expect(objectTypeSelect.isOptionDisabled('Tag')).to.eventually.be.false;
  });
});