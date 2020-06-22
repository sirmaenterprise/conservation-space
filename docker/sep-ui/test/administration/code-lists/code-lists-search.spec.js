import {CodeListsSearch} from 'administration/code-lists/search/code-lists-search';
import {TranslateService} from 'services/i18n/translate-service';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {stub} from 'test/test-utils';
import {getCodeLists} from './code-lists.stub';

describe('CodeListsSearch', () => {

  let codeListsSearch;
  beforeEach(() => {
    codeListsSearch = new CodeListsSearch(stub(TranslateService));
    codeListsSearch.translateService.translateInstant.returns('translated');
    codeListsSearch.codeLists = getCodeLists();
    codeListsSearch.onFilter = sinon.spy();
  });

  function getFiltered() {
    return codeListsSearch.onFilter.getCall(0).args[0].filtered;
  }

  describe('filter code lists with contains match', () => {
    it('should filter code lists by all fields', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'all',
        value: 'Коментар',
        operator: AdvancedSearchCriteriaOperators.CONTAINS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(2);
    });

    it('should filter code lists only by name', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'name',
        value: 'Име',
        operator: AdvancedSearchCriteriaOperators.CONTAINS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(2);
    });

    it('should filter code lists only by comment', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'comment',
        value: 'Коментар',
        operator: AdvancedSearchCriteriaOperators.CONTAINS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(2);
    });

    it('should filter code lists only by code list id', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'id',
        value: '1',
        operator: AdvancedSearchCriteriaOperators.CONTAINS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(1);
    });

    it('should filter code lists only by value', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'value',
        value: 'val',
        operator: AdvancedSearchCriteriaOperators.CONTAINS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(2);
    });

    it('should filter code lists only by extras', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'extra',
        value: 'extra',
        operator: AdvancedSearchCriteriaOperators.CONTAINS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(2);
    });
  });

  describe('filter code lists with exact match', () => {
    it('should filter code lists by all fields', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'all',
        value: 'Comment1',
        operator: AdvancedSearchCriteriaOperators.EQUALS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(1);
    });

    it('should filter code lists only by name', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'name',
        value: 'Name1',
        operator: AdvancedSearchCriteriaOperators.EQUALS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(1);
    });

    it('should filter code lists only by comment', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'comment',
        value: 'Comment2',
        operator: AdvancedSearchCriteriaOperators.EQUALS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(1);
    });

    it('should filter code lists only by code list id', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'id',
        value: '2',
        operator: AdvancedSearchCriteriaOperators.EQUALS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(1);
    });

    it('should filter code lists only by value', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'value',
        value: 'val21',
        operator: AdvancedSearchCriteriaOperators.EQUALS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(1);
    });

    it('should filter code lists only by extras', () => {
      codeListsSearch.ngOnInit();
      codeListsSearch.criteria = {
        field: 'extra',
        value: 'extra23',
        operator: AdvancedSearchCriteriaOperators.EQUALS.id
      };
      codeListsSearch.filter();
      expect(getFiltered().length).to.equal(1);
    });
  });

  describe('construct a default configuration for dependant components', () => {
    it('should fetch the code lists again', () => {
      let fields = {field1: {}, field2: {}, field3: {}};
      let values = Object.keys(fields).map(k => fields[k]);
      codeListsSearch.getSearchFields = sinon.spy(() => fields);

      codeListsSearch.getSearchConfig();
      expect(codeListsSearch.criteria).to.deep.eq({});
      expect(codeListsSearch.fieldsMap).to.deep.eq(fields);
      expect(codeListsSearch.searchFields).to.deep.eq(values);
      expect(codeListsSearch.searchConfig.renderRemoveButton).to.be.false;
    });
  });

  describe('compareValue()', () => {
    it('should check if two primitive values are equal', () => {
      expect(codeListsSearch.compareValue('test', 'test', comparator)).to.be.true;
      expect(codeListsSearch.compareValue('test', 'not-a-test', comparator)).to.be.false;
    });

    it('should check if a value is contained in an array', () => {
      expect(codeListsSearch.compareValue('test', ['1', '2', '3'], comparator)).to.be.false;
      expect(codeListsSearch.compareValue('test', ['1', '2', '3', 'test'], comparator)).to.be.true;
    });
  });

  describe('compareObject()', () => {
    it('should find present value inside an object taking into account all properties', () => {
      let object = {
        level1: {
          level2: {
            name: 'test'
          },
          random2: {}
        },
        random1: {}
      };
      let result = codeListsSearch.compareObject(object, 'test', [], comparator);
      expect(result).to.be.true;
    });

    it('should not find present value inside an object taking into account all properties', () => {
      let object = {
        level1: {
          level2: {
            name: 'not-a-test'
          },
          random2: {}
        },
        random1: {}
      };
      let result = codeListsSearch.compareObject(object, 'test', [], comparator);
      expect(result).to.be.false;
    });

    it('should not find present value inside an object taking account given properties', () => {
      let object = {
        level1: {
          level2: {
            name: 'test'
          },
          random2: {}
        },
        random1: {}
      };
      let result = codeListsSearch.compareObject(object, 'test', ['random1', 'random2'], comparator);
      expect(result).to.be.false;
    });

    it('should find present value inside an object taking account given properties', () => {
      let object = {
        level1: {
          level2: {},
          random2: {
            field1: 'test',
            field2: 'false'
          }
        },
        random1: {
          field3: 'false',
          field4: 'false'
        }
      };
      let result = codeListsSearch.compareObject(object, 'test', ['field1', 'field2'], comparator);
      expect(result).to.be.true;
    });
  });

  describe('reset', () => {
    it('should reset the operator and value for the model', () => {
      codeListsSearch.fieldsMap = {
        name: {
          id: 'name',
          operators: ['1', '2']
        }
      };
      codeListsSearch.criteria = {
        field: 'name',
        value: 'value',
        operator: '2'
      };

      codeListsSearch.reset();
      expect(codeListsSearch.criteria).to.deep.equal({
        field: 'name',
        value: '',
        operator: '1'
      });
    });
  });

  describe('on model change', () => {
    it('should call filter when value has changed', () => {
      codeListsSearch.filter = sinon.spy();
      codeListsSearch.onValue();
      expect(codeListsSearch.filter.calledOnce).to.be.true;
    });

    it('should call filter when operator has changed', () => {
      codeListsSearch.filter = sinon.spy();
      codeListsSearch.onOperator();
      expect(codeListsSearch.filter.calledOnce).to.be.true;
    });

    it('should call filter and reset when field has changed', () => {
      codeListsSearch.criteria = {};
      codeListsSearch.filter = sinon.spy();
      codeListsSearch.reset = sinon.spy();
      codeListsSearch.onField();
      expect(codeListsSearch.reset.calledOnce).to.be.true;
      expect(codeListsSearch.filter.calledOnce).to.be.true;
    });
  });

  function comparator(left, right) {
    return left === right;
  }

});