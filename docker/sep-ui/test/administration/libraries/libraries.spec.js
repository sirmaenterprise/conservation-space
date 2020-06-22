import {Libraries, CLASS_OBJECT_TYPE, CLASS_OBJECT_DEFINITION} from 'administration/libraries/libraries';
import {ModelsService} from 'services/rest/models-service';
import {TranslateService} from 'services/i18n/translate-service';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {AdvancedSearchCriteriaOperators} from 'search/components/advanced/criteria/advanced-search-criteria-operators';
import {DCTERMS_TITLE} from 'instance/instance-properties';
import {NO_SELECTION} from 'search/search-selection-modes';
import {ORDER_ASC} from 'search/order-constants';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';
import {stubSearchService} from 'test/services/rest/search-service-mock';

describe('Libraries', () => {

  let libraries;
  let searchService;
  let modelsService;
  let translateService;

  beforeEach(() => {
    modelsService = stub(ModelsService);
    modelsService.getOntologies.returns(PromiseStub.resolve([]));

    searchService = stubSearchService();

    translateService = stub(TranslateService);
    translateService.translateInstant.returns('translated');

    libraries = new Libraries(modelsService, searchService, translateService);
  });

  it('should construct list of ontology selection options', () => {
    let ontologies = [
      {
        id: 'ontology1',
        title: 'Ontology 1'
      },
      {
        id: 'ontology2',
        title: 'Ontology 2'
      }
    ];

    modelsService.getOntologies.returns(PromiseStub.resolve(ontologies));

    libraries.ngOnInit();

    expect(libraries.ontologySelectorConfig.data.length).to.equal(2);
    expect(libraries.ontologySelectorConfig.data[0]).to.eql({id: 'ontology1', text: 'Ontology 1'});
    expect(libraries.ontologySelectorConfig.data[1]).to.eql({id: 'ontology2', text: 'Ontology 2'});

    expect(libraries.ontologySelectorConfig.placeholder).to.equal('translated');
  });

  it('should construct instance list configuration', () => {
    libraries.ngOnInit();
    expect(libraries.instanceListConfig.selection).to.equal(NO_SELECTION);
  });

  it('should construct query for fetching libraries based on ontology and title filters', () => {
    libraries.ngOnInit();

    libraries.titleFilter = 'Test title';
    libraries.selectedOntologies = ['ontology1', 'ontology2'];

    libraries.searchService = stubSearchService([{
      id: 'document',
      definitionId: CLASS_OBJECT_DEFINITION
    }]);

    libraries.fetchLibraries();

    expect(libraries.searchService.search.calledOnce);

    let searchRequest = libraries.searchService.search.getCall(0).args[0];

    expect(searchRequest.arguments).to.eql({
      orderBy: DCTERMS_TITLE,
      orderDirection: ORDER_ASC,
      properties: ['id', 'default_header'],
      pageSize: 0
    });

    let query = searchRequest.query.tree;

    // query by types, ontologies and title
    expect(query.rules.length).to.equal(3);

    expect(query.rules[0].operator).to.eql(AdvancedSearchCriteriaOperators.EQUALS.id);
    expect(query.rules[0].value).to.eql([CLASS_OBJECT_TYPE]);

    let ontologyCondition = query.rules[1];
    expect(ontologyCondition.condition).to.eql(SearchCriteriaUtils.OR_CONDITION);

    expect(ontologyCondition.rules[0].operator).to.eql(AdvancedSearchCriteriaOperators.CONTAINS.id);
    expect(ontologyCondition.rules[0].value).to.eql(libraries.selectedOntologies[0]);

    expect(ontologyCondition.rules[1].value).to.eql(libraries.selectedOntologies[1]);

    expect(query.rules[2].operator).to.eql(AdvancedSearchCriteriaOperators.CONTAINS.id);
    expect(query.rules[2].value).to.eql(libraries.titleFilter);

    expect(libraries.libraries).to.eql([{id: 'document', definitionId: CLASS_OBJECT_DEFINITION}]);
  });

  it('should remove duplicate classes from the results list', () => {
    libraries.ngOnInit();

    libraries.titleFilter = 'Test title';
    libraries.selectedOntologies = ['ontology1', 'ontology2'];

    libraries.searchService = stubSearchService([
      {
        id: 'document',
        definitionId: CLASS_OBJECT_DEFINITION
      },
      {
        id: 'object',
        definitionId: CLASS_OBJECT_DEFINITION
      },
      {
        id: 'document',
        definitionId: CLASS_OBJECT_DEFINITION
      },
      {
        id: 'object',
        definitionId: CLASS_OBJECT_DEFINITION
      },
      {
        id: 'video',
        definitionId: 'otherDefinition'
      }
    ]);

    libraries.fetchLibraries();

    expect(libraries.libraries).to.eql([
      {id: 'document', definitionId: CLASS_OBJECT_DEFINITION},
      {id: 'object', definitionId: CLASS_OBJECT_DEFINITION}
    ]);
  });

});