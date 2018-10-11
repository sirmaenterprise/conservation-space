import {CodelistManagementService} from 'administration/code-lists/services/codelist-management-service';
import {CodelistRestService} from 'services/rest/codelist-service';
import {Configuration} from 'common/application-config';
import {TranslateService} from 'services/i18n/translate-service';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import _ from 'lodash';

describe('CodelistManagementService', () => {

  let managementService;
  beforeEach(() => {
    managementService = new CodelistManagementService(stubCodelistRest(getCodeListsData()), stubConfiguration(), stubTranslate(), PromiseStub);
  });

  describe('getCodeLists()', () => {

    let codeLists = [];
    beforeEach(() => {
      managementService.getCodeLists().then(response => codeLists = response);
    });

    it('should transform code lists into proper structure and sort them', () => {
      expect(codeLists.length).to.equal(5);
      expect(codeLists[0].id).to.equal('1');
      expect(codeLists[1].id).to.equal('2');
      expect(codeLists[2].id).to.equal('13');
      expect(codeLists[3].id).to.equal('99');
      expect(codeLists[4].id).to.equal('100');
    });

    it('should transform code list descriptions to proper map structure', () => {
      expect(codeLists[0].descriptions).to.deep.equal({
        'DE': {'name': '', 'comment': '', 'language': 'DE'},
        'EN': {'name': 'One', 'comment': '', 'language': 'EN'},
        'BG': {'name': 'Едно', 'comment': '', 'language': 'BG'}
      });
      expect(codeLists[0].description).to.equal(codeLists[0].descriptions['BG']);

      expect(codeLists[1].descriptions).to.deep.equal({
        'DE': {'name': '', 'comment': '', 'language': 'DE'},
        'BG': {'name': '', 'comment': '', 'language': 'BG'},
        'EN': {'name': 'Two', 'comment': 'The number two', 'language': 'EN'}
      });
      expect(codeLists[1].description).to.equal(codeLists[1].descriptions['EN']);
    });

    it('should transform code list extras to a map', () => {
      expect(codeLists[0].extras).to.deep.equal({
        '1': '',
        '2': '',
        '3': ''
      });

      expect(codeLists[1].extras).to.deep.equal({
        '1': 'extra 1',
        '2': 'extra 2',
        '3': ''
      });
    });

    it('should transform code values into proper structure', () => {
      expect(codeLists[0].values.length).to.equal(2);
      expect(codeLists[1].values.length).to.equal(0);

      expect(codeLists[0].values[0].id).to.equal('V1');
      expect(codeLists[0].values[0].active).to.be.false;
      expect(codeLists[0].values[0].codeListValue).to.equal('1');

      expect(codeLists[0].values[1].id).to.equal('V2');
      expect(codeLists[0].values[1].active).to.be.true;
      expect(codeLists[0].values[1].codeListValue).to.equal('1');
    });

    it('should sort code values by ID', () => {
      expect(codeLists[4].values[0].id).to.equal('1');
      expect(codeLists[4].values[1].id).to.equal('C100-V1');
      expect(codeLists[4].values[2].id).to.equal('C100-V2');
    });

    it('should transform code values descriptions into proper map structure', () => {
      expect(codeLists[0].values[0].descriptions).to.deep.equal({
        'DE': {'name': '', 'comment': '', 'language': 'DE'},
        'EN': {'name': 'Value One', 'comment': '', 'language': 'EN'},
        'BG': {'name': 'Стойност Едно', 'comment': '', 'language': 'BG'}
      });
      expect(codeLists[0].values[0].description).to.equal(codeLists[0].values[0].descriptions['BG']);

      expect(codeLists[0].values[1].descriptions).to.deep.equal({
        'DE': {'name': '', 'comment': '', 'language': 'DE'},
        'BG': {'name': '', 'comment': '', 'language': 'BG'},
        'EN': {'name': 'Value Two', 'comment': 'The value two', 'language': 'EN'}
      });
      expect(codeLists[0].values[1].description).to.equal(codeLists[0].values[1].descriptions['EN']);
    });

    it('should transform code values extras into a map', () => {
      expect(codeLists[0].values[0].extras).to.deep.equal({
        '1': 'extra V1-1',
        '2': 'extra V1-2',
        '3': ''
      });

      expect(codeLists[0].values[1].extras).to.deep.equal({
        '1': '',
        '2': '',
        '3': ''
      });
    });

    it('should register a cache with the available languages', () => {
      expect(managementService.languagesCache).to.deep.equal(['EN', 'BG', 'DE']);
    });
  });

  describe('saveCodeList(old, new)', () => {

    let original;
    let updated;
    beforeEach(() => {
      let codeLists = [];
      managementService.getCodeLists().then(response => codeLists = response);
      original = codeLists[0];
      updated = _.cloneDeep(original);
    });

    it('should transform code lists and values back to the structure supported by the backend', () => {
      updated.descriptions['EN'].name = 'One updated';
      updated.extras['3'] = 'New extra 3';

      managementService.saveCodeList(original, updated);
      expect(managementService.codelistRestService.saveCodeList.calledOnce).to.be.true;

      let transformed = managementService.codelistRestService.saveCodeList.getCall(0).args[0];
      expect(transformed.value).to.equal('1');
      expect(transformed.descriptions).to.deep.equal([
        {'name': 'One updated', 'comment': '', 'language': 'EN'},
        {'name': 'Едно', 'comment': '', 'language': 'BG'},
        {'name': '', 'comment': '', 'language': 'DE'}
      ]);
      expect(transformed.extra1).to.equal('');
      expect(transformed.extra2).to.equal('');
      expect(transformed.extra3).to.equal('New extra 3');
      expect(transformed.values).to.deep.equal([]);
    });

    it('should transform only the changed code values and discard the unchanged', () => {
      // Insert new description
      updated.values[1].descriptions['BG'] = {language: 'BG', name: 'Стойност две', comment: ''};

      managementService.saveCodeList(original, updated);
      expect(managementService.codelistRestService.saveCodeList.calledOnce).to.be.true;

      let transformed = managementService.codelistRestService.saveCodeList.getCall(0).args[0];
      expect(transformed.value).to.equal('1');
      expect(transformed.values.length).to.equal(1);

      let transformedValue = transformed.values[0];
      expect(transformedValue.value).to.equal('V2');
      expect(transformedValue.codeListValue).to.equal('1');
      expect(transformedValue.descriptions).to.deep.equal([
        {'name': 'Value Two', 'comment': 'The value two', 'language': 'EN'},
        {'name': 'Стойност две', 'comment': '', 'language': 'BG'},
        {'name': '', 'comment': '', 'language': 'DE'}
      ]);
      expect(transformedValue.extra1).to.equal('');
      expect(transformedValue.extra2).to.equal('');
      expect(transformedValue.extra3).to.equal('');
    });

    it('should transform all code values if the code list is new', () => {
      managementService.saveCodeList(undefined, updated);

      let transformed = managementService.codelistRestService.saveCodeList.getCall(0).args[0];
      expect(transformed.values.length).to.equal(2);
      expect(transformed.values[0].value).to.equal('V1');
      expect(transformed.values[1].value).to.equal('V2');
    });
  });

  describe('createCodeList()', () => {
    it('should produce proper code list object structure', () => {
      // Force language cache
      managementService.getCodeLists();

      let newCodeList = managementService.createCodeList();
      expect(newCodeList.id).to.equal('');
      expect(newCodeList.descriptions).to.deep.equal({
        'DE': {'name': '', 'comment': '', 'language': 'DE'},
        'EN': {'name': '', 'comment': '', 'language': 'EN'},
        'BG': {'name': '', 'comment': '', 'language': 'BG'}
      });
      expect(newCodeList.description).to.equal(newCodeList.descriptions['BG']);
      expect(newCodeList.extras).to.deep.equal({
        '1': '',
        '2': '',
        '3': ''
      });
      expect(newCodeList.values).to.deep.equal([]);
    });
  });

  describe('createCodeValue(codeList)', () => {
    it('should produce proper code value object structure', () => {
      // Force language cache
      managementService.getCodeLists();

      let codeList = getCodeListsData()[0];
      let newCodeValue = managementService.createCodeValue(codeList);
      expect(newCodeValue.id).to.equal('');
      expect(newCodeValue.codeListValue).to.equal(codeList.id);
      expect(newCodeValue.descriptions).to.deep.equal({
        'DE': {'name': '', 'comment': '', 'language': 'DE'},
        'EN': {'name': '', 'comment': '', 'language': 'EN'},
        'BG': {'name': '', 'comment': '', 'language': 'BG'}
      });
      expect(newCodeValue.description).to.equal(newCodeValue.descriptions['BG']);
      expect(newCodeValue.extras).to.deep.equal({
        '1': '',
        '2': '',
        '3': ''
      });
      expect(newCodeValue.active).to.be.true;
    });
  });

  describe('areCodeListsEqual()', () => {
    it('should correctly determine if two code lists are equal or not', () => {
      let codeLists = [];
      managementService.getCodeLists().then(response => codeLists = response);

      let codeList1 = codeLists[0];
      let codeList2 = _.cloneDeep(codeList1);

      expect(managementService.areCodeListsEqual(codeList1, codeList2)).to.be.true;

      codeList2.id = 'something else';
      expect(managementService.areCodeListsEqual(codeList1, codeList2)).to.be.false;

      codeList2 = _.cloneDeep(codeList1);
      codeList2.description.comment = 'new comment';
      expect(managementService.areCodeListsEqual(codeList1, codeList2)).to.be.false;

      codeList2 = _.cloneDeep(codeList1);
      codeList2.descriptions['latin'] = {};
      expect(managementService.areCodeListsEqual(codeList1, codeList2)).to.be.false;

      codeList2 = _.cloneDeep(codeList1);
      codeList2.values[0].extras['1'] = 'new extra 1';
      expect(managementService.areCodeListsEqual(codeList1, codeList2)).to.be.false;

      codeList2 = _.cloneDeep(codeList1);
      codeList2.values[0].active = !codeList2.values[0].active;
      expect(managementService.areCodeListsEqual(codeList1, codeList2)).to.be.false;
    });
  });

  describe('exportCodeLists()', () => {
    it('should transform the export response', () => {
      let exportedCodeLists = [];
      managementService.exportCodeLists().then(response => exportedCodeLists = response);

      expect(exportedCodeLists).to.deep.equal({
        data: 'data',
        status: 'ok'
      });
    });
  });

  function stubCodelistRest(codeLists) {
    let restStub = stub(CodelistRestService);
    restStub.getCodeLists.returns(PromiseStub.resolve({data: codeLists}));
    restStub.exportCodeLists.returns(PromiseStub.resolve({data: 'data', config: {}, status: 'ok', header: {}}));
    return restStub;
  }

  function stubConfiguration() {
    let configurationStub = stub(Configuration);
    configurationStub.get.withArgs(Configuration.SYSTEM_LANGUAGE).returns('EN');
    return configurationStub;
  }

  function stubTranslate() {
    let translateStub = stub(TranslateService);
    translateStub.getCurrentLanguage.returns('BG');
    return translateStub;
  }

  function getCodeListsData() {
    return [{
      value: '1',
      descriptions: [{
        language: 'en',
        name: 'One'
      }, {
        language: 'BG',
        name: 'Едно'
      }],
      values: [{
        value: 'V1',
        active: false,
        codeListValue: '1',
        descriptions: [{
          language: 'en',
          name: 'Value One'
        }, {
          language: 'BG',
          name: 'Стойност Едно'
        }],
        extra1: 'extra V1-1',
        extra2: 'extra V1-2',
        extra3: ''
      }, {
        value: 'V2',
        active: true,
        codeListValue: '1',
        descriptions: [{
          language: 'en',
          name: 'Value Two',
          comment: 'The value two'
        }]
      }]
    }, {
      value: '2',
      descriptions: [{
        language: 'en',
        name: 'Two',
        comment: 'The number two'
      }],
      extra1: 'extra 1',
      extra2: 'extra 2',
      extra3: ''
    }, {
      value: '13',
      descriptions: [{
        language: 'en',
        name: 'English',
        comment: 'Language'
      }],
      values: [{
        value: 'en',
        descriptions: []
      }, {
        value: 'bg',
        descriptions: []
      }, {
        value: 'de',
        descriptions: []
      }]
    }, {
      value: '100',
      descriptions: [],
      values: [{
        value: 'C100-V2',
        descriptions: []
      }, {
        value: 'C100-V1',
        descriptions: []
      }, {
        value: '1',
        descriptions: []
      }]
    }, {
      value: '99',
      descriptions: []
    }];
  }

});
