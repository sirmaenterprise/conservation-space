var SingleSelectMenu = require('./form-control.js').SingleSelectMenu;
var InputField = require('./form-control.js').InputField;
var SandboxPage = require('../page-object').SandboxPage;

const DEPARTMENT = '#department';
const FUNCTIONAL = '#functional';
const TITLE = '#title';
const DESCRIPTION = '#description';
const NAME = '#name';

var page = new SandboxPage();

describe('ЕТ220001', () => {

  var singleSelect;

  beforeEach(() => {
    singleSelect = new SingleSelectMenu();
    page.open('/sandbox/form-builder/ET220001');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  it('should show all available results by default', () => {
    singleSelect.toggleMenu(FUNCTIONAL);
    expect(singleSelect.getMenuElements().count()).to.eventually.eq(2);
    singleSelect.getMenuElements().then(function (items) {
      expect(items[0].getText()).to.eventually.equal('Mechanical Design Group');
      expect(items[1].getText()).to.eventually.equal('Electrical Design Group');
    });
  });

  it('should load correct values in related dropdown', () => {
    singleSelect.selectFromMenu(DEPARTMENT, 1, true).then(() => {
      expect(singleSelect.getSelectedValue(DEPARTMENT)).to.eventually.equal('ENG');
    });
    singleSelect.toggleMenu(FUNCTIONAL);
    expect(singleSelect.getMenuElements().count()).to.eventually.eq(1);
    singleSelect.getMenuElements().then(function (items) {
      expect(items[0].getText()).to.eventually.equal('Mechanical Design Group');
    });
  });

  it('should clear selected value and reload related dropdown', () => {
    singleSelect.selectFromMenu(DEPARTMENT, 1, true).then(() => {
      expect(singleSelect.getSelectedValue(DEPARTMENT)).to.eventually.equal('ENG');
    });
    singleSelect.toggleMenu(FUNCTIONAL);
    expect(singleSelect.getMenuElements().count()).to.eventually.eq(1);
    singleSelect.getMenuElements().then(function (items) {
      expect(items[0].getText()).to.eventually.equal('Mechanical Design Group');
    });
    singleSelect.toggleMenu(FUNCTIONAL);
    singleSelect.selectFromMenu(FUNCTIONAL, 1, true).then(() => {
      expect(singleSelect.getSelectedValue(FUNCTIONAL)).to.eventually.equal('MDG');
    });

    singleSelect.selectFromMenu(DEPARTMENT, 2, true).then(() => {
      expect(singleSelect.getSelectedValue(DEPARTMENT)).to.eventually.equal('INF');
    });

    singleSelect.toggleMenu(FUNCTIONAL);
    expect(singleSelect.getMenuElements().count()).to.eventually.eq(1);
    singleSelect.getMenuElements().then(function (items) {
      expect(items[0].getText()).to.eventually.equal('Electrical Design Group');
    });
  });

  it('should reload related dropdown and keep selected value', () => {
    singleSelect.selectFromMenu(DEPARTMENT, 1, true).then(() => {
      expect(singleSelect.getSelectedValue(DEPARTMENT)).to.eventually.equal('ENG');
    });
    singleSelect.toggleMenu(FUNCTIONAL);
    expect(singleSelect.getMenuElements().count()).to.eventually.eq(1);
    singleSelect.getMenuElements().then(function (items) {
      expect(items[0].getText()).to.eventually.equal('Mechanical Design Group');
    });
    singleSelect.toggleMenu(FUNCTIONAL);

    singleSelect.selectFromMenu(FUNCTIONAL, 1, true).then(() => {
      expect(singleSelect.getSelectedValue(FUNCTIONAL)).to.eventually.equal('MDG');
    });

    singleSelect.selectFromMenu(DEPARTMENT, 3, true).then(() => {
      expect(singleSelect.getSelectedValue(DEPARTMENT)).to.eventually.equal('TSD');
    });
    expect(singleSelect.getSelectedValue(FUNCTIONAL)).to.eventually.equal('MDG');
  });

  it('should switch document to print view', ()=> {
    singleSelect.selectFromMenu(DEPARTMENT, 1, true);
    singleSelect.selectFromMenu(FUNCTIONAL, 1, true);
    var inputField = new InputField();

    inputField.setValue(TITLE, 'Test Title Text');
    inputField.setValue(DESCRIPTION, 'Test Description Text');
    inputField.setValue(NAME, 'Test Name Text');

    element(by.css('#viewModeInput input[value="PRINT"')).click();
    expect(singleSelect.isPrint(DEPARTMENT)).to.eventually.be.true;
    expect(singleSelect.isPrint(FUNCTIONAL)).to.eventually.be.true;
    expect(inputField.isPrint(TITLE)).to.eventually.be.true;
    expect(inputField.isPrint(DESCRIPTION)).to.eventually.be.true
    expect(inputField.isPrint(NAME)).to.eventually.be.true
    expect(singleSelect.getSelectedValue(DEPARTMENT)).to.eventually.equal('ENG');
    expect(singleSelect.getSelectedValue(FUNCTIONAL)).to.eventually.equal('MDG');
    expect(inputField.getText(TITLE + '.print-field')).to.eventually.equal('Test Title Text');
    expect(inputField.getText(DESCRIPTION + '.print-field')).to.eventually.equal('Test Description Text');
    expect(inputField.getText(NAME + '.print-field')).to.eventually.equal('Test Name Text');
  });

});