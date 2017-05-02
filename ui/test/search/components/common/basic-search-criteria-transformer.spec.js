import {BasicSearchCriteriaTransformer} from 'search/components/common/basic-search-criteria-transformer';
import {BasicSearchCriteria} from 'search/components/common/basic-search-criteria';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import _ from 'lodash';

describe('BasicSearchCriteriaTransformer', () => {

  describe('convertBasicToAdvancedCriteria', () => {
    it('should correctly transform the basic search criteria to advanced', () => {
      var criteriaMapping = getPopulatedCriteriaMapping();
      var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

      BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(criteriaMapping, tree);

      expect(tree.rules[0].rules[0]).to.exist;
      expect(tree.rules[0].rules[0].value).to.deep.equal(['emf:Document', 'emf:Image']);

      var innerCriteria = tree.rules[0].rules[1];
      expect(innerCriteria).to.exist;
      expect(innerCriteria.rules).to.exist;

      var fts = getRule(innerCriteria, 'freeText');
      expect(fts).to.exist;
      expect(fts.value).to.equal('123');

      var createdOn = getRule(innerCriteria, 'emf:createdOn');
      expect(createdOn).to.exist;
      expect(createdOn.value).to.deep.equal(['yesterday', 'today']);

      var createdBy = getRule(innerCriteria, 'emf:createdBy');
      expect(createdBy).to.exist;
      expect(createdBy.value).to.deep.equal(['emf:admin', 'emf:guest']);
      expect(createdBy.renderSeparately).to.be.true;

      var partOf = getRule(innerCriteria, 'emf:partOf');
      expect(partOf).to.exist;
      expect(partOf.value).to.deep.equal(['emf:123', 'current_object']);

      var hasAttachment = getRule(innerCriteria, 'emf:hasAttachment');
      expect(hasAttachment).to.exist;
      expect(hasAttachment.value).to.deep.equal(['emf:123', 'current_object']);
    });

    it('should correctly transform empty criteria mapping', () => {
      var criteriaMapping = BasicSearchCriteria.getCriteriaMapping();
      var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

      BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(criteriaMapping, tree);

      expect(tree.rules[0].rules[0]).to.exist;
      expect(tree.rules[0].rules[0].value).to.deep.equal([SearchCriteriaUtils.ANY_OBJECT]);

      var innerCriteria = tree.rules[0].rules[1];
      expect(innerCriteria).to.exist;
      expect(innerCriteria.rules).to.exist;
      expect(innerCriteria.rules.length).to.equal(0);
    });

    it('should correctly transform relations if there are no context', () => {
      var criteriaMapping = getPopulatedCriteriaMapping();
      criteriaMapping.context.value = [];
      var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

      BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(criteriaMapping, tree);

      var innerCriteria = tree.rules[0].rules[1];

      var partOf = getRule(innerCriteria, 'emf:partOf');
      expect(partOf).to.exist;
      expect(partOf.value).to.deep.equal([SearchCriteriaUtils.ANY_OBJECT]);

      var hasAttachment = getRule(innerCriteria, 'emf:hasAttachment');
      expect(hasAttachment).to.exist;
      expect(hasAttachment.value).to.deep.equal([SearchCriteriaUtils.ANY_OBJECT]);
    });

    it('should correctly transform context if there are no relations', () => {
      var criteriaMapping = getPopulatedCriteriaMapping();
      criteriaMapping.relationships.value = [];
      var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

      BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(criteriaMapping, tree);

      var innerCriteria = tree.rules[0].rules[1];

      var anyRelation = getRule(innerCriteria, SearchCriteriaUtils.ANY_RELATION);
      expect(anyRelation).to.exist;
      expect(anyRelation.value).to.deep.equal(['emf:123', 'current_object']);
    });

    it('should not convert the tree if the criteria mapping is missing', () => {
      var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
      BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(undefined, tree);
      // types
      expect(tree.rules[0].rules[0]).to.not.exist;
      // inner criteria
      expect(tree.rules[0].rules[1]).to.not.exist;
    });
  });

  describe('convertAdvancedToBasicCriteria()', () => {
    it('should correctly transform advanced search criteria to basic search criteria', () => {
      var criteriaMapping = BasicSearchCriteria.getCriteriaMapping();
      var tree = getPopulatedTree();

      BasicSearchCriteriaTransformer.convertAdvancedToBasicCriteria(criteriaMapping, tree);

      expect(criteriaMapping.freeText.value).to.equal('123');
      expect(criteriaMapping.types.value).to.deep.equal(['emf:Document', 'emf:Image']);
      expect(criteriaMapping.relationships.value).to.deep.equal(['emf:partOf', 'emf:hasAttachment']);
      expect(criteriaMapping.context.value).to.deep.equal(['emf:123', 'current_object']);
      expect(criteriaMapping.createdFromDate.value).to.equal('yesterday');
      expect(criteriaMapping.createdToDate.value).to.equal('today');
      expect(criteriaMapping.createdBy.value).to.deep.equal(['emf:admin', 'emf:guest']);
    });

    it('should correctly transform undefined object value', () => {
      var criteriaMapping = BasicSearchCriteria.getCriteriaMapping();
      var tree = getPopulatedTree();

      tree.rules[0].rules[1].rules[0].value = undefined;

      BasicSearchCriteriaTransformer.convertAdvancedToBasicCriteria(criteriaMapping, tree);
      expect(criteriaMapping.freeText.value).to.deep.equal('');
    });

    it('should correctly transform any object type', () => {
      var criteriaMapping = BasicSearchCriteria.getCriteriaMapping();
      var tree = getPopulatedTree();

      tree.rules[0].rules[0].value = [SearchCriteriaUtils.ANY_OBJECT];

      BasicSearchCriteriaTransformer.convertAdvancedToBasicCriteria(criteriaMapping, tree);
      expect(criteriaMapping.types.value).to.deep.equal([]);
    });

    it('should correctly transform tree without inner criteria to the type', () => {
      var criteriaMapping = BasicSearchCriteria.getCriteriaMapping();
      var tree = getPopulatedTree();

      tree.rules[0].rules[1].rules = [];

      BasicSearchCriteriaTransformer.convertAdvancedToBasicCriteria(criteriaMapping, tree);

      expect(criteriaMapping.freeText.value).to.equal('');
      expect(criteriaMapping.types.value).to.deep.equal(['emf:Document', 'emf:Image']);
      expect(criteriaMapping.relationships.value).to.deep.equal([]);
      expect(criteriaMapping.context.value).to.deep.equal([]);
      expect(criteriaMapping.createdFromDate.value).to.equal('');
      expect(criteriaMapping.createdToDate.value).to.equal('');
      expect(criteriaMapping.createdBy.value).to.deep.equal([])
    });

    it('should not assign the criteria mapping if the tree is missing', () => {
      var criteriaMapping = BasicSearchCriteria.getCriteriaMapping();
      criteriaMapping.types.value = ['emf:Document'];
      BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(criteriaMapping, undefined);
      // Should had have reset it if the tree was provided.
      expect(criteriaMapping.types.value).to.deep.equal(['emf:Document']);
    });
  });

  it('should correctly transform a mapping to an advanced search tree and back to a mapping', () => {
    var criteriaMapping = getPopulatedCriteriaMapping();
    // Testing special case where the createdBy is added twice - one from the hardcoded form and one as a relation
    criteriaMapping.relationships.value.push('emf:createdBy');

    var criteriaMappingCopy = _.cloneDeep(criteriaMapping);

    // Basic -> Advanced
    var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();
    BasicSearchCriteriaTransformer.convertBasicToAdvancedCriteria(criteriaMapping, tree);

    // Advanced -> Basic
    criteriaMapping = BasicSearchCriteria.getCriteriaMapping();
    BasicSearchCriteriaTransformer.convertAdvancedToBasicCriteria(criteriaMapping, tree);

    expect(criteriaMapping.freeText.value).to.deep.equal(criteriaMappingCopy.freeText.value);
    expect(criteriaMapping.types.value).to.deep.equal(criteriaMappingCopy.types.value);
    expect(criteriaMapping.relationships.value).to.deep.equal(criteriaMappingCopy.relationships.value);
    expect(criteriaMapping.context.value).to.deep.equal(criteriaMappingCopy.context.value);
    expect(criteriaMapping.createdFromDate.value).to.deep.equal(criteriaMappingCopy.createdFromDate.value);
    expect(criteriaMapping.createdToDate.value).to.deep.equal(criteriaMappingCopy.createdToDate.value);
    expect(criteriaMapping.createdBy.value).to.deep.equal(criteriaMappingCopy.createdBy.value);
  });

  function getPopulatedCriteriaMapping() {
    var mapping = BasicSearchCriteria.getCriteriaMapping();
    mapping.freeText.value = '123';
    mapping.types.value = ['emf:Document', 'emf:Image'];
    mapping.relationships.value = ['emf:partOf', 'emf:hasAttachment'];
    mapping.context.value = ['emf:123', 'current_object'];
    mapping.createdFromDate.value = 'yesterday';
    mapping.createdToDate.value = 'today';
    mapping.createdBy.value = ['emf:admin', 'emf:guest'];
    return mapping;
  }

  function getPopulatedTree() {
    var tree = SearchCriteriaUtils.getDefaultAdvancedSearchCriteria();

    tree.rules[0].rules.push(SearchCriteriaUtils.getDefaultObjectTypeRule(['emf:Document', 'emf:Image']));
    tree.rules[0].rules.push(SearchCriteriaUtils.buildCondition());

    tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('freeText', '', '', '123'));
    tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('emf:createdOn', '', '', ['yesterday','today']));
    tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('emf:partOf', '', '', ['emf:123']));
    tree.rules[0].rules[1].rules.push(SearchCriteriaUtils.buildRule('emf:hasAttachment', '', '', ['current_object']));

    var createdBy = SearchCriteriaUtils.buildRule('emf:createdBy', '', '', ['emf:admin', 'emf:guest']);
    createdBy.renderSeparately = true;
    tree.rules[0].rules[1].rules.push(createdBy);

    return tree;
  }

  function getRule(condition, field) {
    var founded;
    condition.rules.forEach((rule) => {
      if (rule.field === field) {
        founded = rule;
      }
    });
    return founded;
  }
});
