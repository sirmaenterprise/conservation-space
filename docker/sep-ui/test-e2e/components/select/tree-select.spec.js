var TreeSelect = require('../../form-builder/form-control.js').TreeSelect;
var SandboxPage = require('../../page-object').SandboxPage;

var page = new SandboxPage();

describe('Tree select component', function () {

  beforeEach(function () {
    page.open('/sandbox/components/select/tree-select');
  });
  
  it('should expand parent nodes', function() {
    let select = new TreeSelect($('.seip-select-wrapper'));

    select.open();

    browser.wait(EC.not(EC.visibilityOf(select.getOptionByName('Reinforced'))), DEFAULT_TIMEOUT);

    select.toggleOption('Concrete');

    browser.wait(EC.visibilityOf(select.getOptionByName('Reinforced')), DEFAULT_TIMEOUT);
  });

  it('should expand the hierarchy upwards when filter matches child node', function() {
    let select = new TreeSelect($('.seip-select-wrapper'));

    select.open();

    select.filter('bar');

    browser.wait(EC.visibilityOf(select.getOptionByName('Rebar')), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(select.getOptionByName('Reinforced')), DEFAULT_TIMEOUT);
    browser.wait(EC.visibilityOf(select.getOptionByName('Concrete')), DEFAULT_TIMEOUT);

    browser.wait(EC.not(EC.visibilityOf(select.getOptionByName('Fiber'))), DEFAULT_TIMEOUT);
  });

});