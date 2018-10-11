import {ResultsToolbar} from 'search/components/common/results-toolbar';
import {ModelsService} from 'services/rest/models-service';
import {QueryBuilder} from 'search/utils/query-builder';
import {SearchMediator, EVENT_CLEAR, EVENT_SEARCH} from 'search/search-mediator';
import {PromiseStub} from 'test/promise-stub';
import {IdocMocks} from 'test/idoc/idoc-mocks';
import {
  ANY_OBJECT,
  ANY_RELATION,
  CRITERIA_FTS_RULE_FIELD,
  SearchCriteriaUtils
} from 'search/utils/search-criteria-utils';
import {stub} from 'test/test-utils';

describe('ResultsToolbar', () => {

  var resultsToolbar;

  beforeEach(() => {
    resultsToolbar = constructAndConfigureResultsBar();
  });

  it('should properly load searchable model types', () => {
    expect(resultsToolbar.modelPromise).to.exist;
    expect(resultsToolbar.modelsService.getModels.calledOnce).to.be.true;

    let args = resultsToolbar.modelsService.getModels.getCall(0).args;
    expect(args[0]).to.eq(ModelsService.PURPOSE_SEARCH);
  });

  it('should evaluate isSearchPerformed to true when a valid payload is passed', () => {
    sinon.stub(resultsToolbar.instanceRestService, 'loadBatch').returns(PromiseStub.resolve(mockRelationsPayload()));
    resultsToolbar.config.searchMediator.trigger(EVENT_SEARCH, getSearchPayload());

    expect(resultsToolbar.isSearchPerformed).to.be.true;
    //assert page lower and upper boundaries
    expect(resultsToolbar.resultData.lower).to.eq(26);
    expect(resultsToolbar.resultData.upper).to.eq(50);
    //assert page size, number and results arguments
    expect(resultsToolbar.resultData.pageSize).to.eq(25);
    expect(resultsToolbar.resultData.pageNumber).to.eq(2);
    expect(resultsToolbar.resultData.resultSize).to.eq(50);
    //assert that fts, relation & type are correctly evaluated
    expect(resultsToolbar.resultData.fts).to.eq('free text search');
    expect(resultsToolbar.resultData.types).to.eq('Case, Document, Project');
    expect(resultsToolbar.resultData.context).to.deep.eq('<span>header1</span>');
  });

  it('should not extract types if not configured to do so', () => {
    resultsToolbar.config.message.renderType = false;
    sinon.stub(resultsToolbar.instanceRestService, 'loadBatch').returns(PromiseStub.resolve(mockRelationsPayload()));
    resultsToolbar.config.searchMediator.trigger(EVENT_SEARCH, getSearchPayload());

    expect(resultsToolbar.isSearchPerformed).to.be.true;
    expect(resultsToolbar.resultData.types).to.not.exist;
    expect(resultsToolbar.resultData.fts).to.eq('free text search');
    expect(resultsToolbar.resultData.context).to.deep.eq('<span>header1</span>');
  });

  it('should resolve model types promise only once', () => {
    expect(resultsToolbar.modelPromise).to.exist;
    expect(resultsToolbar.modelTypes).to.not.exist;
    // trigger search with proper payload & validate model promise should be deleted
    sinon.stub(resultsToolbar.instanceRestService, 'loadBatch').returns(PromiseStub.resolve(mockRelationsPayload()));
    resultsToolbar.config.searchMediator.trigger(EVENT_SEARCH, getSearchPayload());

    expect(resultsToolbar.modelTypes).to.exist;
    expect(resultsToolbar.modelPromise).to.not.exist;
    expect(resultsToolbar.isSearchPerformed).to.be.true;
    expect(resultsToolbar.resultData.types).to.eq('Case, Document, Project');
  });

  it('should not extract context if not configured to do so', () => {
    resultsToolbar.config.message.renderContext = false;
    resultsToolbar.config.searchMediator.trigger(EVENT_SEARCH, getSearchPayload());

    expect(resultsToolbar.isSearchPerformed).to.be.true;
    expect(resultsToolbar.resultData.context).to.not.exist;
    expect(resultsToolbar.resultData.fts).to.eq('free text search');
    expect(resultsToolbar.resultData.types).to.eq('Case, Document, Project');
  });

  it('should not extract fts if not configured to do so', () => {
    resultsToolbar.config.message.renderFts = false;
    sinon.stub(resultsToolbar.instanceRestService, 'loadBatch').returns(PromiseStub.resolve(mockRelationsPayload()));
    resultsToolbar.config.searchMediator.trigger(EVENT_SEARCH, getSearchPayload());

    expect(resultsToolbar.isSearchPerformed).to.be.true;
    expect(resultsToolbar.resultData.fts).to.not.exist;
    expect(resultsToolbar.resultData.types).to.eq('Case, Document, Project');
    expect(resultsToolbar.resultData.context).to.deep.eq('<span>header1</span>');
  });

  it('should evaluate isSearchPerformed to false when an invalid payload is passed', () => {
    resultsToolbar.config.searchMediator.trigger(EVENT_SEARCH, {
      query: {},
      response: {}
    });

    expect(resultsToolbar.isSearchPerformed).to.be.false;
  });

  it('should subscribe to EVENT_CLEAR & properly reset component\'s internal data', () => {
    resultsToolbar.config.searchMediator.trigger(EVENT_CLEAR);
    expect(resultsToolbar.resultData).to.deep.eq({});
    expect(resultsToolbar.isSearchPerformed).to.be.false;
  });

  it('should properly extract model type label', () => {
    resultsToolbar.loadModelTypes({models: mockModelsPayload()});

    expect(resultsToolbar.extractModelTypesAsPlainText([1, 2, 3])).to.eq('Case, Document, Project');
    expect(resultsToolbar.extractModelTypesAsPlainText([ANY_OBJECT])).to.eq(undefined);
  });

  it('should properly calculate page number boundaries', () => {
    let argumentsMap = {
      pageSize: 10,
      pageNumber: 5
    };

    resultsToolbar.configureResultsAndBoundaries(45, argumentsMap);
    expect(resultsToolbar.resultData.lower).to.eq(41);
    expect(resultsToolbar.resultData.upper).to.eq(45);
  });

  it('should properly calculate page lower bound', () => {
    resultsToolbar.resultData.pageSize = 10;
    resultsToolbar.resultData.pageNumber = 2;
    resultsToolbar.resultData.resultSize = 45;
    expect(resultsToolbar.calculateLowerBound()).to.eq(11);
  });

  it('should properly calculate page upper bound', () => {
    resultsToolbar.resultData.pageSize = 10;
    resultsToolbar.resultData.pageNumber = 5;
    resultsToolbar.resultData.resultSize = 45;
    expect(resultsToolbar.calculateUpperBound()).to.eq(45);
  });

  it('should properly extract rule from tree', () => {
    let firstRuleStub = sinon.stub(QueryBuilder, 'getFirstRule').returns({
      value: 'relation'
    });
    expect(resultsToolbar.extractFieldFromTree({tree: {}}, ANY_RELATION)).to.eq('relation');
    //restore stubbed method
    firstRuleStub.restore();
  });

  it('should evaluate if context should be rendered & showed', () => {
    resultsToolbar.resultData.relations = undefined;
    resultsToolbar.config.message.renderContext = true;
    expect(resultsToolbar.shouldRenderContext()).to.be.false;

    resultsToolbar.resultData.relations = ['Task3'];
    resultsToolbar.config.message.renderContext = false;
    expect(resultsToolbar.shouldRenderContext()).to.be.false;
  });

  it('should evaluate if type should be rendered & showed', () => {
    resultsToolbar.resultData.types = ['Book'];
    resultsToolbar.config.message.renderType = true;
    expect(resultsToolbar.shouldRenderType()).to.be.true;

    resultsToolbar.resultData.types = undefined;
    resultsToolbar.config.message.renderType = true;
    expect(resultsToolbar.shouldRenderType()).to.be.false;

    resultsToolbar.resultData.types = ['Book'];
    resultsToolbar.config.message.renderType = false;
    expect(resultsToolbar.shouldRenderType()).to.be.false;
  });

  it('should evaluate if results should be rendered & showed', () => {
    resultsToolbar.resultData.resultSize = 20;
    resultsToolbar.config.message.renderResults = true;
    expect(resultsToolbar.shouldRenderResults()).to.be.true;

    resultsToolbar.resultData.resultSize = 0;
    resultsToolbar.config.message.renderResults = true;
    expect(resultsToolbar.shouldRenderResults()).to.be.false;

    resultsToolbar.resultData.resultSize = 25;
    resultsToolbar.config.message.renderResults = false;
    expect(resultsToolbar.shouldRenderResults()).to.be.false;
  });

  it('should evaluate if text should be rendered & showed', () => {
    resultsToolbar.resultData.fts = 'Search Terms';
    resultsToolbar.config.message.renderFts = true;
    expect(resultsToolbar.shouldRenderFts()).to.be.true;

    resultsToolbar.resultData.fts = '';
    resultsToolbar.config.message.renderFts = true;
    expect(resultsToolbar.shouldRenderFts()).to.be.false;

    resultsToolbar.resultData.fts = 'Search Terms';
    resultsToolbar.config.message.renderFts = false;
    expect(resultsToolbar.shouldRenderFts()).to.be.false;
  });

  it('should properly convert headers when valid relations are present', () => {
    sinon.stub(resultsToolbar, 'extractFieldFromTree').returns(['emf:Test']);
    // trigger search with proper payload & validate model promise should be deleted
    sinon.stub(resultsToolbar.instanceRestService, 'loadBatch').returns(PromiseStub.resolve(mockRelationsPayload()));
    resultsToolbar.config.searchMediator.trigger(EVENT_SEARCH, getSearchPayload());
    expect(resultsToolbar.resultData.relations).to.exist;
    expect(resultsToolbar.resultData.context).to.eq('<span>header1</span>');
  });

  it('should properly convert headers when invalid relations are present', () => {
    sinon.stub(resultsToolbar, 'extractFieldFromTree').returns([ANY_OBJECT]);
    resultsToolbar.config.searchMediator.trigger(EVENT_SEARCH, getSearchPayload());
    expect(resultsToolbar.resultData.context).to.not.exist;
    expect(resultsToolbar.resultData.relations).to.not.exist;
  });
});

function constructAndConfigureResultsBar() {
  let resultsToolbar = new ResultsToolbar(IdocMocks.mockInstanceRestService(1), mockModelsService());
  resultsToolbar.config.searchMediator = new SearchMediator({});
  resultsToolbar.ngOnInit();
  return resultsToolbar;
}

function mockRelationsPayload() {
  return {
    data: [{
      headers: {
        breadcrumb_header: '<span>header1</span>'
      }
    }]
  };
}


function mockModelsPayload() {
  return [{
    id: 1,
    label: 'Case'
  }, {
    id: 2,
    label: 'Document'
  }, {
    id: 3,
    label: 'Project'
  }, {
    id: 4,
    label: 'Task'
  }];
}

function mockModelsService() {
  let modelsService = stub(ModelsService);
  modelsService.getModels.returns(PromiseStub.resolve({
    models: mockModelsPayload()
  }));
  return modelsService;
}

function getSearchQuery() {
  // construct the default template for the tree
  let tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
  tree.rules[0].rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule([1, 2, 3]));
  tree.rules[0].rules.push(SearchCriteriaUtils.buildCondition());
  // attach fts & relation rules to the constructed tree
  tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule(CRITERIA_FTS_RULE_FIELD, 'fts', 'contains', 'free text search'));
  tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule(ANY_RELATION, 'object', 'set_to', ['emf:123456']));
  return new QueryBuilder(tree);
}

function getSearchPayload() {
  // search args
  let args = {};
  args.pageSize = 25;
  args.pageNumber = 2;

  // search response containing the mocked response data
  let response = SearchMediator.buildEmptySearchResults();
  response.data.resultSize = 50;
  response.data.values = [1, 2, 3];

  return SearchMediator.buildSearchResponse(getSearchQuery(), response, args);
}

