import {
  SearchCriteriaService,
  SPACE,
  CONDITION_OR_LABEL,
  CONDITION_AND_LABEL
} from 'services/search/search-criteria-service';
import {PromiseStub} from 'test/promise-stub';

import {ModelsService} from 'services/rest/models-service';
import {TranslateService} from 'services/i18n/translate-service';
import {InstanceRestService} from 'services/rest/instance-service';
import {CodelistService} from 'services/codelist/codelist-service';
import {PropertiesRestService} from 'services/rest/properties-service';

import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';

import {stub} from 'test/test-utils';

describe('SearchCriteriaService', () => {

  let searchCriteriaService;

  beforeEach(() => {
    searchCriteriaService = new SearchCriteriaService(stub(TranslateService),
      stub(InstanceRestService), stub(PropertiesRestService), stub(CodelistService), stub(ModelsService), PromiseStub);
  });

  it('should stringify provided search criteria', () => {
    let criteria = getConstructedCriteria();
    let stringified = searchCriteriaService.stringifySearchCriteria(criteria);

    // the correct number of controls for each rule to be present along with style
    expect(matches(stringified, '<span style=color:#8B008B>OR</span>')).to.eq(1);
    expect(matches(stringified, '<span style=color:#8B008B>AND</span>')).to.eq(3);

    // the correct number of operators for each rule to be present along with style
    expect(matches(stringified, '<span style=color:#008000>in</span>')).to.eq(2);
    expect(matches(stringified, '<span style=color:#008000>equals</span>')).to.eq(3);

    // the correct number of properties for each rule to be present along with style
    expect(matches(stringified, '<span style=color:#0000FF>prop1</span>')).to.eq(1);
    expect(matches(stringified, '<span style=color:#0000FF>prop2</span>')).to.eq(1);
    expect(matches(stringified, '<span style=color:#0000FF>prop3</span>')).to.eq(2);
    expect(matches(stringified, '<span style=color:#0000FF>prop4</span>')).to.eq(1);

    // the correct number of values for each rule to be present along with style
    expect(matches(stringified, '<span style=color:#1a9bbc>list1</span>')).to.eq(1);
    expect(matches(stringified, '<span style=color:#1a9bbc>list2</span>')).to.eq(1);
    expect(matches(stringified, '<span style=color:#1a9bbc>model2</span>')).to.eq(1);
    expect(matches(stringified, '<span style=color:#1a9bbc>instance1</span>')).to.eq(1);
    expect(matches(stringified, '<span style=color:#1a9bbc>instance2</span>')).to.eq(1);
    expect(matches(stringified, '<span style=color:#1a9bbc>instance4</span>')).to.eq(2);
  });

  it('should translate provided search criteria', () => {
    let criteria = getConstructedCriteria();

    searchCriteriaService.translateService.translateInstant.withArgs(CONDITION_OR_LABEL).returns('Or');
    searchCriteriaService.translateService.translateInstant.withArgs(CONDITION_AND_LABEL).returns('And');

    searchCriteriaService.translateService.translateInstant.withArgs(AdvancedSearchCriteriaOperators.IN.label).returns('in');
    searchCriteriaService.translateService.translateInstant.withArgs(AdvancedSearchCriteriaOperators.EQUALS.label).returns('equals');

    searchCriteriaService.modelsService.getModels.returns(PromiseStub.resolve(getModels()));
    searchCriteriaService.instanceRestService.loadBatch.returns(PromiseStub.resolve(getInstances()));
    searchCriteriaService.codelistService.aggregateCodelists.returns(PromiseStub.resolve(getCodeLists()));
    searchCriteriaService.propertiesRestService.getSearchableProperties.returns(PromiseStub.resolve(getProperties()));

    searchCriteriaService.assignPromises();
    let translatedPromise = searchCriteriaService.translateSearchCriteria(criteria);

    translatedPromise.then(translated => {
      // assert both a promise is returned & translated value
      expect(translated).to.deep.eq(getTranslatedCriteria());
      expect(searchCriteriaService.dataPromises).to.not.exist;
    });
  });

  it('should build styled string when a style is provided', () => {
    let styled = searchCriteriaService.beautify('label', 'blue');
    expect(styled).to.eq('<span style=color:blue>label</span>');
  });

  it('should not build styled string when a style is not provided', () => {
    let styled = searchCriteriaService.beautify('label');
    expect(styled).to.eq('label');
  });

  it('should indent a string according to provided parameters', () => {
    expect(searchCriteriaService.indent('test', 0)).to.eq('test');
    expect(searchCriteriaService.indent('test', 1)).to.eq(SPACE + 'test');
  });

  it('should transform all entries in an array from a provided storage', () => {
    let storage = {
      one: {
        label: 'first'
      }, two: {
        label: 'second'
      }, three: {
        label: 'third'
      },
    };
    let list = ['one', 'two', 'three', 'four'];
    searchCriteriaService.translate(list, storage);
    expect(list).to.deep.eq(['first', 'second', 'third', 'four']);
  });

  it('should properly load data and cache it for further use', () => {
    let afterLoaded = sinon.spy(() => {
    });

    searchCriteriaService.modelsService.getModels.returns(PromiseStub.resolve(getModels()));
    searchCriteriaService.codelistService.aggregateCodelists.returns(PromiseStub.resolve(getCodeLists()));
    searchCriteriaService.propertiesRestService.getSearchableProperties.returns(PromiseStub.resolve(getProperties()));
    searchCriteriaService.assignPromises();

    expect(searchCriteriaService.propertiesPromise).to.exist;
    searchCriteriaService.loadData(afterLoaded);

    expect(afterLoaded.calledOnce).to.be.true;
    expect(sizeof(searchCriteriaService.models)).to.eq(8);
    expect(sizeof(searchCriteriaService.properties)).to.eq(4);
    expect(searchCriteriaService.dataPromises).to.not.exist;
  });

  it('should properly load operators', () => {
    searchCriteriaService.cacheOperators(AdvancedSearchCriteriaOperators);
    // all operators from the advanced search criteria operators utility should be present
    expect(sizeof(searchCriteriaService.operators)).to.eq(sizeof(AdvancedSearchCriteriaOperators));
  });

  it('should properly load controls', () => {
    searchCriteriaService.cacheControls();

    expect(sizeof(searchCriteriaService.controls)).to.eq(2);
    expect(searchCriteriaService.controls[SearchCriteriaUtils.OR_CONDITION]).to.exist;
    expect(searchCriteriaService.controls[SearchCriteriaUtils.AND_CONDITION]).to.exist;
  });

  it('should properly load models', () => {
    searchCriteriaService.cacheModels(getModels());

    expect(searchCriteriaService.models).to.deep.equal({
      model1: {
        id: 'model1',
        label: 'model1'
      },
      model2: {
        id: 'model2',
        label: 'model2'
      },
      model3: {
        id: 'model3',
        label: 'model3'
      },
      model4: {
        id: 'model4',
        label: 'model4'
      }
    });
  });

  it('should properly load code lists', () => {
    searchCriteriaService.cacheCodeLists(getCodeLists());

    expect(searchCriteriaService.models).to.deep.equal({
      list1: {
        id: 'list1',
        label: 'list1'
      },
      list2: {
        id: 'list2',
        label: 'list2'
      },
      list3: {
        id: 'list3',
        label: 'list3'
      },
      list4: {
        id: 'list4',
        label: 'list4'
      }
    });
  });

  it('should properly load instances', () => {
    searchCriteriaService.translateService.translateInstant.returns('translated');
    searchCriteriaService.cacheInstances(getInstances());

    expect(searchCriteriaService.instances).to.deep.equal({
      instance1: {
        id: 'instance1',
        label: 'instance1'
      },
      instance2: {
        id: 'instance2',
        label: 'instance2'
      },
      instance3: {
        id: 'instance3',
        label: 'instance3'
      },
      instance4: {
        id: 'instance4',
        label: 'instance4'
      },
      anyObject: {
        id: 'anyObject',
        label: 'translated'
      }
    });
  });

  it('should properly load properties', () => {
    searchCriteriaService.cacheProperties(getProperties());

    expect(searchCriteriaService.properties).to.deep.equal({
      prop1: {
        id: 'prop1',
        label: 'prop1'
      },
      prop2: {
        id: 'prop2',
        label: 'prop2'
      },
      prop3: {
        id: 'prop3',
        label: 'prop3'
      },
      prop4: {
        id: 'prop4',
        label: 'prop4'
      }
    });
    expect(searchCriteriaService.codeLists).to.deep.eq([1, 2, 3, 5, 7]);
  });

  function sizeof(object) {
    return Object.keys(object).length;
  }

  function matches(source, pattern) {
    return source.match((new RegExp(pattern, 'g')) || []).length;
  }

  function getCodeLists() {
    return [
      {
        value: 'list1',
        label: 'list1'
      }, {
        value: 'list2',
        label: 'list2'
      }, {
        value: 'list3',
        label: 'list3'
      }, {
        value: 'list4',
        label: 'list4'
      }
    ];
  }

  function getModels() {
    return {
      models: [
        {
          id: 'model1',
          label: 'model1'
        }, {
          id: 'model2',
          label: 'model2'
        }, {
          id: 'model3',
          label: 'model3'
        }, {
          id: 'model4',
          label: 'model4'
        }
      ]
    };
  }

  function getInstances() {
    return {
      data: [
        {
          id: 'instance1',
          properties: {title: 'instance1'}
        }, {
          id: 'instance2',
          properties: {title: 'instance2'}
        }, {
          id: 'instance3',
          properties: {title: 'instance3'}
        }, {
          id: 'instance4',
          properties: {title: 'instance4'}
        },
      ]
    };
  }

  function getProperties() {
    return [
      {
        id: 'prop1',
        text: 'prop1'
      }, {
        id: 'prop2',
        text: 'prop2'
      }, {
        id: 'prop3',
        text: 'prop3',
        codeLists: [1, 2, 3]
      }, {
        id: 'prop4',
        text: 'prop4',
        codeLists: [1, 5, 7]
      },
    ];
  }

  function getConstructedCriteria() {
    let inOperator = AdvancedSearchCriteriaOperators.IN;
    let equalsOperator = AdvancedSearchCriteriaOperators.EQUALS;

    let conditionOne = SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.AND_CONDITION, [
      SearchCriteriaUtils.buildRule('prop1', 'object', equalsOperator.id, ['instance1', 'instance4']),
      SearchCriteriaUtils.buildRule('prop2', 'codeList', inOperator.id, ['list1', 'list2']),
      SearchCriteriaUtils.buildRule('prop3', '', equalsOperator.id, ['keyword']),
    ]);
    let conditionTwo = SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.AND_CONDITION, [
      SearchCriteriaUtils.buildRule('prop3', 'codeList', inOperator.id, ['model2']),
      SearchCriteriaUtils.buildRule('prop4', 'object', equalsOperator.id, ['instance4', 'instance2']),
    ]);
    return SearchCriteriaUtils.buildCondition(SearchCriteriaUtils.OR_CONDITION, [conditionOne, conditionTwo]);
  }

  function getTranslatedCriteria() {
    return {
      'condition': 'Or',
      'rules': [
        {
          'condition': 'And',
          'rules': [
            {
              'field': 'prop1',
              'type': 'object',
              'operator': 'equals',
              'value': [
                'instance1',
                'instance4'
              ]
            },
            {
              'field': 'prop2',
              'type': 'codeList',
              'operator': 'in',
              'value': [
                'list1',
                'list2'
              ]
            },
            {
              'field': 'prop3',
              'type': '',
              'operator': 'equals',
              'value': [
                'keyword'
              ]
            }
          ]
        },
        {
          'condition': 'And',
          'rules': [
            {
              'field': 'prop3',
              'type': 'codeList',
              'operator': 'in',
              'value': [
                'model2'
              ]
            },
            {
              'field': 'prop4',
              'type': 'object',
              'operator': 'equals',
              'value': [
                'instance4',
                'instance2'
              ]
            }
          ]
        }
      ]
    };
  }
});