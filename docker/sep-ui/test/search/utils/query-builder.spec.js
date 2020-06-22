import {QueryBuilder, DynamicTreeWalkListener} from 'search/utils/query-builder';
import {SearchCriteriaUtils} from 'search/utils/search-criteria-utils';
import {JsonUtil} from 'common/json-util';
import _ from 'lodash';

describe('QueryBuilder', () => {
  let builder;

  beforeEach(() => {
    builder = new QueryBuilder();
  });

  describe('add(criteria [, parentId])', () => {

    it('should require a rule', () => {
      expect(builder.add.bind(builder, null)).to.throw(Error, 'Rule with id is required.');
    });

    it('should require rule "id"', () => {
      expect(builder.add.bind(builder, {})).to.throw(Error, 'Rule with id is required.');
    });

    it('should add rule to root condition if no condition id is provided', () => {
      let rule = {id: 1, field: 'test'};
      builder.add(rule);
      expect(builder.tree.rules[0]).to.eq(rule);
    });

    it('should add rule to lookup map', () => {
      let rule = {id: 2, field: 'test'};
      builder.add(rule);

      expect(builder.lookup.get(2)).to.deep.eq(rule);
    });

    it('should add rule to specific condition', () => {
      let tree = {
        id: 1,
        condition: 'AND',
        rules: [
          {id: 2, field: 'test'},
          {
            id: 3,
            condition: 'OR',
            rules: []
          }
        ]
      };

      let rule = {id: 4, field: 'test'};
      builder = new QueryBuilder(tree);

      builder.add(rule, 3);
      expect(tree.rules[1].rules[0]).to.eq(rule);
    });

    it('should throw if trying to add to non-existent condition', () => {
      expect(builder.add.bind(builder, {
        id: 1,
        field: 'test'
      }, 2)).to.throw(Error, 'Condition with id 2 does not exist.');
    });

    it('should update existing rule', () => {
      let rule = {id: 1, field: 'test'};
      let ruleUpdated = {id: 1, field: 'test', operation: 'eq', value: 'testing'};
      builder.add(rule);

      builder.add(ruleUpdated);
      expect(builder.tree.rules[0]).to.deep.eq(ruleUpdated);
    });

    it('should register sub rules in the lookup map', () => {
      let rule = {id: 2, condition: 'test', rules: [{id: 3}]};
      builder.add(rule);

      expect(builder.lookup.get(2)).to.deep.eq(rule);
      expect(builder.lookup.get(3)).to.deep.eq(rule.rules[0]);
    });

    it('should fail if a sub rule has no id', () => {
      let rule = {id: 2, condition: 'test', rules: [{}]};
      expect(builder.add.bind(builder, rule)).to.throw(Error, 'Rule with id is required.');
    });
  });

  describe('remove(criteria)', () => {
    it('should require a criteria', () => {
      expect(builder.remove.bind(builder, null)).to.throw(Error);
    });

    it('should require a criteria with id', () => {
      expect(builder.remove.bind(builder, {})).to.throw(Error);
    });

    it('should require an existing criteria', () => {
      expect(builder.remove.bind(builder, {id: '123'})).to.throw(Error);
    });

    it('should remove a rule', () => {
      var tree = getTestTree();
      builder.init(tree);
      var criteriaForRemoving = QueryBuilder.getFirstRule(tree, 'createdBy');
      builder.remove(criteriaForRemoving);
      expect(QueryBuilder.getFirstRule(tree, 'createdBy')).to.not.exist;
    });

    it('should remove a nested condition', () => {
      var tree = getTestTree();
      builder.init(tree);
      // inner criteria with conditions and other rules
      var criteriaForRemoving = tree.rules[8];
      builder.remove(criteriaForRemoving);
      expect(tree.rules[8]).to.not.exist;
    });

    it('should remove an existing criteria from the lookup map', () => {
      var tree = getTestTree();
      builder.init(tree);
      var criteriaForRemoving = _.cloneDeep(tree.rules[0]);

      builder.remove(criteriaForRemoving);
      expect(builder.lookup.get(criteriaForRemoving.id)).to.not.exist;
    });

    it('should remove all nested criteria from the lookup map', () => {
      var tree = getTestTree();
      builder.init(tree);
      // inner criteria with conditions and other rules
      var criteriaForRemoving = _.cloneDeep(tree.rules[8]);
      builder.remove(criteriaForRemoving);
      expect(builder.lookup.get(criteriaForRemoving.rules[0].id)).to.not.exist;
      expect(builder.lookup.get(criteriaForRemoving.rules[1].id)).to.not.exist;
    });

    it('should not remove other criteria ids from the lookup map', () => {
      var tree = getTestTree();
      builder.init(tree);
      var criteriaForRemoving = _.cloneDeep(tree.rules[0]);

      builder.remove(criteriaForRemoving);

      tree.rules.forEach((rule) => {
        expect(builder.lookup.get(rule.id)).to.exist;
      });
    });

  });

  describe('toQueryParams(tree)', () => {

    it('should grab all rules at any level and use eq for all', () => {
      let tree = getTestTree();

      let expected = 'objectType[]=dev&objectType[]=qa' +
        '&subType[]=dev&subType[]=qa' +
        '&objectRelationship[]=dev&objectRelationship[]=qa' +
        '&metaText=dev' +
        '&location[]=dev&location[]=qa' +
        '&createdBy[]=dev&createdBy[]=qa' +
        '&createdFromDate=date&createdToDate=date';
      expect(QueryBuilder.toQueryParams(tree)).to.eq(expected);
    });

    it('should encode uri components in metaText', () => {
      let tree = {
        condition: 'OR',
        rules: [{field: 'metaText', operation: 'eq', value: '#111'}]
      };

      let expected = 'metaText=%23111';
      expect(QueryBuilder.toQueryParams(tree)).to.eq(expected);
    });

    it('should ignore undefined values', () => {
      let tree = {
        condition: 'OR',
        rules: [{field: 'metaText', operation: 'eq', value: undefined}]
      };
      expect(QueryBuilder.toQueryParams(tree)).to.eq('');
    });

    it('should ignore empty values', () => {
      let tree = {
        condition: 'OR',
        rules: [{field: 'metaText', operation: 'eq', value: ''}]
      };
      expect(QueryBuilder.toQueryParams(tree)).to.eq('');
    });

  });

  describe('walk(tree, listener)', () => {
    let listener;

    beforeEach(() => {
      listener = new DynamicTreeWalkListener();
    });

    it('should call the onAny listener method for each node in the tree', () => {
      let onAny = sinon.spy();
      let tree = {
        condition: 'AND',
        rules: [1, 2]
      };

      listener.addOnAny(onAny);
      QueryBuilder.walk(tree, listener);

      expect(onAny.callCount).to.eq(3);
      expect(onAny.getCall(0).args[0]).to.deep.eq(tree);
      expect(onAny.getCall(1).args[0]).to.deep.eq(tree.rules[0]);
      expect(onAny.getCall(2).args[0]).to.deep.eq(tree.rules[1]);
    });

    it('should reach nested conditions', () => {
      let onCondition = sinon.spy();
      let tree = {
        condition: 'AND',
        rules: [{condition: 'OR'}]
      };

      listener.addOnCondition(onCondition);
      QueryBuilder.walk(tree, listener);

      expect(onCondition.callCount).to.eq(2);
      expect(onCondition.getCall(0).args[0]).to.deep.eq(tree);
      expect(onCondition.getCall(1).args[0]).to.deep.eq(tree.rules[0]);
    });

    it('should call onRule listener method', () => {
      let onRule = sinon.spy();
      let tree = {
        condition: 'AND',
        rules: [1]
      };

      listener.addOnRule(onRule);
      QueryBuilder.walk(tree, listener);

      expect(onRule.calledOnce);
      expect(onRule.getCall(0).args[0]).to.eq(tree.rules[0]);
    });
  });

  describe('getRules(tree, field)', () => {
    it('should get a rule from the tree', () => {
      let tree = getTestTree();
      var expected = {field: 'location', operation: 'ge', value: ['dev', 'qa']};
      expect(QueryBuilder.getRules(tree, 'location')).to.deep.equal([expected]);
    });

    it('should get multiple rules from the tree about the same field', () => {
      let tree = getTestTree();
      var expected = [
        {field: 'types', operation: 'ge', value: ['dev', 'qa']},
        {field: 'types', operation: 'ge'}
      ];
      expect(QueryBuilder.getRules(tree, 'types')).to.deep.equal(expected);
    });

    it('should return empty array for non-existent tree rule', () => {
      let tree = getTestTree();
      expect(QueryBuilder.getRules(tree, 'missing')).to.deep.equal([]);
    });
  });

  describe('getFirstRule(tree, field)', () => {
    it('should get a single rule from the tree', () => {
      let tree = getTestTree();
      var expected = {field: 'location', operation: 'ge', value: ['dev', 'qa']};
      expect(QueryBuilder.getFirstRule(tree, 'location')).to.deep.equal(expected);
    });

    it('should get the first rule from a tree about with multiple rules for a field', () => {
      let tree = getTestTree();
      var expected = {field: 'types', operation: 'ge', value: ['dev', 'qa']};
      expect(QueryBuilder.getFirstRule(tree, 'types')).to.deep.equal(expected);
    });

    it('should not get a rule for non-existent tree rule', () => {
      let tree = getTestTree();
      expect(QueryBuilder.getFirstRule(tree, 'missing')).to.not.exist;
    });
  });

  describe('updateRules(tree, field, value)', () => {
    it('should update rules that correspond to the given field', () => {
      let tree = getTestTree();
      tree.rules.push({field: 'position', operation: 'ge', value: ['dev', 'qa']});

      let expectedTree = getTestTree();
      expectedTree.rules.push({field: 'position', operation: 'ge', value: ['dev-op']});

      QueryBuilder.updateRules(tree, 'position', ['dev-op']);
      expect(tree).to.deep.equal(expectedTree);
    });
  });

  describe('ruleHasValues()', () => {
    it('should tell rule has no values if the rule is undefined', () => {
      expect(QueryBuilder.ruleHasValues()).to.be.false;
    });

    it('should tell rule has no values if the rule has no such property', () => {
      expect(QueryBuilder.ruleHasValues({})).to.be.false;
    });

    it('should tell rule has no values if the rule values are empty', () => {
      expect(QueryBuilder.ruleHasValues({value: []})).to.be.false;
    });

    it('should tell rule has values if the rule values have at least one entry', () => {
      var rule = {
        value: [{}]
      };
      expect(QueryBuilder.ruleHasValues(rule)).to.be.true;
    });
  });

  describe('flattenSearchTree(tree)', () => {

    it('should flatten the tree to a map of the rules fields the associated rules', () => {
      let rule1 = SearchCriteriaUtils.buildRule('field1');
      let rule2 = SearchCriteriaUtils.buildRule('field2');
      let innerCondition = SearchCriteriaUtils.buildCondition();
      innerCondition.rules.push(rule1, rule2);

      let rule3 = SearchCriteriaUtils.buildRule('field1');
      let tree = SearchCriteriaUtils.buildCondition();
      tree.rules.push(rule3, innerCondition);

      let map = QueryBuilder.flattenSearchTree(tree);
      let fields = Object.keys(map);
      expect(fields).to.deep.equal(['field1', 'field2']);
      // Every rule has unique ID so the deep comparison is adequate here
      expect(map['field1']).to.deep.equal([rule3, rule1]);
      expect(map['field2']).to.deep.equal([rule2]);
    });
  });

  describe('isEqual(lhs, rhs)', () => {

    it('should properly compare two equal trees', () => {
      let innerCondition = SearchCriteriaUtils.buildCondition();
      innerCondition.rules.push(SearchCriteriaUtils.buildRule('field1'));
      let treeOne = SearchCriteriaUtils.buildCondition();
      treeOne.rules.push(innerCondition);

      innerCondition = SearchCriteriaUtils.buildCondition();
      innerCondition.rules.push(SearchCriteriaUtils.buildRule('field1'));
      let treeTwo = SearchCriteriaUtils.buildCondition();
      treeTwo.rules.push(innerCondition);

      expect(QueryBuilder.isEqual(treeOne, treeTwo)).to.be.true;
    });

    it('should properly compare two non equal trees', () => {
      let innerCondition = SearchCriteriaUtils.buildCondition();
      innerCondition.rules.push(SearchCriteriaUtils.buildRule('field1'));
      let treeOne = SearchCriteriaUtils.buildCondition();
      treeOne.rules.push(innerCondition);

      innerCondition = SearchCriteriaUtils.buildCondition();
      innerCondition.rules.push(SearchCriteriaUtils.buildRule('field2'));
      let treeTwo = SearchCriteriaUtils.buildCondition();
      treeTwo.rules.push(innerCondition);

      expect(QueryBuilder.isEqual(treeOne, treeTwo)).to.be.false;
    });
  });

  describe('encodeSearchTree(tree)', () => {

    it('should sanitize & encode the given search tree', () => {
      let rule1 = SearchCriteriaUtils.buildRule('field1');
      let rule2 = SearchCriteriaUtils.buildRule('field2');
      let innerCondition = SearchCriteriaUtils.buildCondition();
      innerCondition.rules.push(rule1, rule2);

      let rule3 = SearchCriteriaUtils.buildRule('field1');
      let tree = SearchCriteriaUtils.buildCondition();
      tree.rules.push(rule3, innerCondition);

      let sanitizedTree = {
        "condition":"AND",
        "rules":[
          {
            "field":"field1"
          },
          {
            "condition":"AND",
            "rules":[
              {
                "field":"field1"
              },
              {
                "field":"field2"
              }
            ]
          }
        ]
      };
      let encodedTree = JsonUtil.encode(sanitizedTree);
      expect(QueryBuilder.encodeSearchTree(tree)).to.deep.equal(encodedTree);
    });
  });

  describe('encodeSearchArguments(args) & decodeSearchArguments(string)', () => {

    it('should sanitize & encode the given search arguments', () => {
      let args = {
        pageSize: 25,
        pageNumber: 2,
        orderBy: 'relevance',
        orderDirection: 'ASC',
        properties: [1,2,3,4]
      };

      let sanitizedArgs = {
        pageSize: 25,
        pageNumber: 2,
        orderBy: 'relevance',
        orderDirection: 'ASC'
      };

      let encodedArgs = JsonUtil.encode(sanitizedArgs);
      expect(QueryBuilder.encodeSearchArguments(args)).to.deep.equal(encodedArgs);
    });

    it('should decode the given as base64 string search arguments', () => {
      let args = {
        pageSize: 25,
        pageNumber: 2,
        orderBy: 'relevance',
        orderDirection: 'ASC'
      };

      let encodedArgs = JsonUtil.encode(args);
      expect(QueryBuilder.decodeSearchArguments(encodedArgs)).to.deep.equal(args);
    });
  });

  function getTestTree() {
    return {
      condition: 'OR',
      rules: [
        {operation: 'ge', value: ['dev', 'qa']},
        {field: 'types', operation: 'ge', value: ['dev', 'qa']},
        {field: 'subtypes', operation: 'eq', value: ['dev', 'qa']},
        {field: 'types', operation: 'ge'},
        {field: 'relationship', operation: 'ge', value: ['dev', 'qa']},
        {field: 'metaText', operation: 'in', value: 'dev'},
        {field: 'location', operation: 'ge', value: ['dev', 'qa']},
        {field: 'createdBy', operation: 'ge', value: ['dev', 'qa']},
        {
          condition: 'OR',
          rules: [
            {field: 'createdFromDate', operation: 'eq', value: 'date'},
            {field: 'createdToDate', operation: 'eq', value: 'date'}
          ]
        }
      ]
    };
  }

});