'use strict';

let CodeListsSandbox = require('./code-lists.js').CodeListsSandbox;
let assertCodeListHeader = require('./code-lists-assertions').assertCodeListHeader;

describe('Controlled vocabularies search', () => {

  let search;
  let management;

  beforeEach(() => {
    let sandbox = new CodeListsSandbox();
    sandbox.open(false);

    management = sandbox.getManagement();
    search = management.getSearch();
  });

  it('should search code lists by all fields', () => {
    search.getStringCriteria().enterValue('Project');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(2);
      assertCodeListHeader(codeLists[0], '1', 'Project state');
      assertCodeListHeader(codeLists[1], '2', 'Project type');
    });
  });

  it('should search code lists by all fields in another language', () => {
    search.getStringCriteria().enterValue('проект');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(2);
      assertCodeListHeader(codeLists[0], '1', 'Project state');
      assertCodeListHeader(codeLists[1], '2', 'Project type');
    });
  });

  it('should search code lists by id field', () => {
    search.changeProperty('id');
    search.getStringCriteria().enterValue('1');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      assertCodeListHeader(codeLists[0], '1', 'Project state');
    });
  });

  it('should search code lists by name field', () => {
    search.changeProperty('name');
    search.getStringCriteria().enterValue('Language');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      assertCodeListHeader(codeLists[0], '13', 'Language');
    });
  });

  it('should search code lists by comment field', () => {
    search.changeProperty('comment');
    search.getStringCriteria().enterValue('The available');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      assertCodeListHeader(codeLists[0], '1', 'Project state');
    });
  });

  it('should search code lists by values field', () => {
    search.changeProperty('value');
    search.getStringCriteria().enterValue('PR0001');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      assertCodeListHeader(codeLists[0], '2', 'Project type');
    });
  });

  it('should search code lists by extras field', () => {
    search.changeProperty('extra');
    search.getStringCriteria().enterValue('Two extras');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      assertCodeListHeader(codeLists[0], '2', 'Project type');
    });
  });

  it('should search and find only code lists that have name', () => {
    search.changeProperty('name');
    search.getStringCriteria().enterValue('');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(5);
      assertCodeListHeader(codeLists[0], '1', 'Project state');
      assertCodeListHeader(codeLists[1], '2', 'Project type');
      assertCodeListHeader(codeLists[2], '3', 'Level');
      assertCodeListHeader(codeLists[3], '13', 'Language');
      assertCodeListHeader(codeLists[4], '555', 'Country');
    });
  });

  it('should search and find only code lists that have comment', () => {
    search.changeProperty('comment');
    search.getStringCriteria().enterValue('');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      assertCodeListHeader(codeLists[0], '1', 'Project state');
    });
  });

  it('should search and find only code lists that have values', () => {
    search.changeProperty('value');
    search.getStringCriteria().enterValue('');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(5);
      assertCodeListHeader(codeLists[0], '1', 'Project state');
      assertCodeListHeader(codeLists[1], '2', 'Project type');
      assertCodeListHeader(codeLists[2], '3', 'Level');
      assertCodeListHeader(codeLists[3], '13', 'Language');
      assertCodeListHeader(codeLists[4], '555', 'Country');
    });
  });

  it('should search and find only code lists that have extras', () => {
    search.changeProperty('extra');
    search.getStringCriteria().enterValue('');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      assertCodeListHeader(codeLists[0], '2', 'Project type');
    });
  });

  it('should trigger the search on field change', () => {
    search.changeProperty('comment');
    search.getStringCriteria().enterValue('');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      // assert for code lists that have any comment present
      assertCodeListHeader(codeLists[0], '1', 'Project state');

      search.changeProperty('extra');
      // assert for code lists that have any values
      management.getCodeLists().then(codeLists => {
        expect(codeLists.length).to.equal(1);
        assertCodeListHeader(codeLists[0], '2', 'Project type');
      });
    });
  });

  it('should trigger the search on operator change', () => {
    search.changeProperty('id');
    search.getStringCriteria().enterValue('1');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      // assert for code lists that strictly equal the given id
      assertCodeListHeader(codeLists[0], '1', 'Project state');

      search.changeOperator('contains');
      // assert for code lists that contain the id
      management.getCodeLists().then(codeLists => {
        expect(codeLists.length).to.equal(2);
        assertCodeListHeader(codeLists[0], '1', 'Project state');
        assertCodeListHeader(codeLists[1], '13', 'Language');
      });
    });
  });

  it('should search and list a controlled vocabulary that is being created', () => {
    management.addCodeList().then((newCode) => {
      let newCodeDetails = newCode.getDetails();
      newCodeDetails.setId('1233');
      newCodeDetails.setName('New code list');
    });

    search.getStringCriteria().enterValue('33');

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(1);
      assertCodeListHeader(codeLists[0], '1233', 'New code list');
    });
  });

  it('should list newly inserted controlled vocabulary if there is applied filter', () => {
    search.getStringCriteria().enterValue('13');

    management.addCodeList();

    management.getCodeLists().then(codeLists => {
      expect(codeLists.length).to.equal(2);
      // New CL should be the first
      assertCodeListHeader(codeLists[0], '', '');
      assertCodeListHeader(codeLists[1], '13', 'Language');
    });
  });

});