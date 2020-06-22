'use strict';

let InputField = require('./../../form-control.js').InputField;
let MultySelectMenu = require('./../../form-control.js').MultySelectMenu;
let SandboxPage = require('../../../page-object').SandboxPage;

let page = new SandboxPage();

describe('Multivalue field condition validator', function () {

  let selected_1;
  let selected_2;
  let selected_3;

  let textField1;
  let textField2;
  let textField3;

  beforeEach(() => {
    selected_1 = new MultySelectMenu($('#emf\\:selected_1-wrapper'));
    selected_2 = new MultySelectMenu($('#selected_2-wrapper'));
    selected_3 = new MultySelectMenu($('#selected_3-wrapper'));

    textField1 = new InputField($('#inputtext1-wrapper'));
    textField2 = new InputField($('#inputtext2-wrapper'));
    textField3 = new InputField($('#inputtext3-wrapper'));

    page.open('sandbox/form-builder/validation/multivalue-field-condition');
    browser.wait(EC.visibilityOf($('form')), DEFAULT_TIMEOUT);
  });

  // Comments notation:
  // Condition | Trigger field value | If condition should match

  // Single value, IN type conditions. If condition matches, the field should become readonly.
  describe('single value, IN type conditions', () => {
    it(`[selected_1] IN ('BGR') | [] | false`, () => {
      expect(textField1.isPreview()).to.eventually.be.false;
    });

    it(`[selected_1] IN ('BGR') | ["BGR"] | true`, () => {
      selected_1.selectFromMenuByIndex(1);//BGR
      expect(textField1.isPreview()).to.eventually.be.true;
    });

    it(`[selected_1] IN ('BGR') | [] | false`, () => {
      selected_1.selectFromMenuByIndex(1);//BGR
      selected_1.removeFromSelectionByTitle('България');
      expect(textField1.isPreview()).to.eventually.be.false;
    });

    it(`[selected_1] IN ('BGR') | ["AUS"] | false`, () => {
      selected_1.selectFromMenuByIndex(2);//AUS
      expect(textField1.isPreview()).to.eventually.be.false;
    });

    it(`[selected_1] IN ('BGR') | ["BGR", "AUS"] | true`, () => {
      selected_1.selectFromMenuByIndex(1);//BGR
      expect(textField1.isPreview()).to.eventually.be.true;
      selected_1.selectFromMenuByIndex(2);//AUS
      expect(textField1.isPreview()).to.eventually.be.true;
    });
  });

  // Single value, NOTIN type conditions. If condition matches, the field should become mandatory.
  describe('single value, NOTIN type conditions', () => {
    it(`[selected_1] NOTIN ('GBR') | [] | true`, () => {
      expect(textField1.isMandatory()).to.eventually.be.true;
    });

    it(`[selected_1] NOTIN ('GBR') | ["AUS"] | true`, () => {
      selected_1.selectFromMenuByIndex(2);//AUS
      expect(textField1.isMandatory()).to.eventually.be.true;
    });

    it(`[selected_1] NOTIN ('GBR') | ["AUS", "USA"] | true`, () => {
      selected_1.selectFromMenuByIndex(2);//AUS
      selected_1.selectFromMenuByIndex(3);//USA
      expect(textField1.isMandatory()).to.eventually.be.true;
    });

    it(`[selected_1] NOTIN ('GBR') | ["GBR"] | false`, () => {
      selected_1.selectFromMenuByIndex(4);//GBR
      expect(textField1.isMandatory()).to.eventually.be.false;
    });

    it(`[selected_1] NOTIN ('GBR') | ["AUS", "GBR"] | false`, () => {
      selected_1.selectFromMenuByIndex(2);//AUS
      selected_1.selectFromMenuByIndex(4);//GBR
      expect(textField1.isMandatory()).to.eventually.be.false;
    });
  });

  // two values, IN type conditions. If condition matches, the field should become readonly.
  describe('two values, IN type conditions', () => {
    it(`[selected_2] IN ('BGR', 'AUS') | [] | false`, () => {
      expect(textField2.isPreview()).to.eventually.be.false;
    });

    it(`[selected_2] IN ('BGR', 'AUS') | ["BGR"] | true`, () => {
      selected_2.selectFromMenuByIndex(1);//BGR
      expect(textField2.isPreview()).to.eventually.be.true;
    });

    it(`[selected_2] IN ('BGR', 'AUS') | ["BGR", "AUS"] | true`, () => {
      selected_2.selectFromMenuByIndex(1);//BGR
      expect(textField2.isPreview()).to.eventually.be.true;
      selected_2.selectFromMenuByIndex(2);//AUS
      expect(textField2.isPreview()).to.eventually.be.true;
    });

    it(`[selected_2] IN ('BGR', 'AUS') | ["BGR", "AUS", "USA"] | true`, () => {
      selected_2.selectFromMenuByIndex(1);//BGR
      selected_2.selectFromMenuByIndex(2);//AUS
      selected_2.selectFromMenuByIndex(3);//USA
      expect(textField2.isPreview()).to.eventually.be.true;
    });
  });

  // Two value, NOTIN type conditions. If condition matches, the field should become mandatory.
  describe('two values, NOTIN type conditions', () => {
    it(`[selected_2] NOTIN ('GBR', 'USA') | [] | true`, () => {
      expect(textField2.isMandatory()).to.eventually.be.true;
    });

    it(`[selected_2] NOTIN ('GBR', 'USA') | [FRA] | true`, () => {
      selected_2.selectFromMenuByIndex(5);//FRA
      expect(textField2.isMandatory()).to.eventually.be.true;
    });

    it(`[selected_2] NOTIN ('GBR', 'USA') | [FRA, CAN] | true`, () => {
      selected_2.selectFromMenuByIndex(5);//FRA
      selected_2.selectFromMenuByIndex(6);//CAN
      expect(textField2.isMandatory()).to.eventually.be.true;
    });

    it(`[selected_2] NOTIN ('GBR', 'USA') | [GBR] | false`, () => {
      selected_2.selectFromMenuByIndex(4);//GBR
      expect(textField2.isMandatory()).to.eventually.be.false;
    });

    it(`[selected_2] NOTIN ('GBR', 'USA') | [GBR, USA] | false`, () => {
      selected_2.selectFromMenuByIndex(3);//USA
      selected_2.selectFromMenuByIndex(4);//GBR
      expect(textField2.isMandatory()).to.eventually.be.false;
    });

    it(`[selected_2] NOTIN ('GBR', 'USA') | [GBR, USA, CAN] | false`, () => {
      selected_2.selectFromMenuByIndex(4);//GBR
      selected_2.selectFromMenuByIndex(3);//USA
      selected_2.selectFromMenuByIndex(6);//CAN
      expect(textField2.isMandatory()).to.eventually.be.false;
    });
  });

  // two values, ALL type condition. If condition matches, the field should become readonly.
  describe('two values, ALL type condition', () => {
    it(`[selected_3] ALL ('BGR', 'AUS') | [] | false`, () => {
      expect(textField3.isPreview()).to.eventually.be.false;
    });

    it(`[selected_3] ALL ('BGR', 'AUS') | ["BGR"] | false`, () => {
      selected_3.selectFromMenuByIndex(1);//BGR
      expect(textField3.isPreview()).to.eventually.be.false;
    });

    it(`[selected_3] ALL ('BGR', 'AUS') | ["BGR", "AUS"] | true`, () => {
      selected_3.selectFromMenuByIndex(1);//BGR
      selected_3.selectFromMenuByIndex(2);//AUS
      expect(textField3.isPreview()).to.eventually.be.true;
    });

    it(`[selected_3] ALL ('BGR', 'AUS') | ["BGR", "AUS", "USA"] | true`, () => {
      selected_3.selectFromMenuByIndex(1);//BGR
      selected_3.selectFromMenuByIndex(2);//AUS
      selected_3.selectFromMenuByIndex(3);//USA
      expect(textField3.isPreview()).to.eventually.be.true;
    });
  });

});
