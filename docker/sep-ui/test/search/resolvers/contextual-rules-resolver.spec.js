import {ContextualRulesResolver} from 'search/resolvers/contextual-rules-resolver';
import {CURRENT_OBJECT_TEMP_ID} from 'models/instance-object';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';

describe('ContextualRulesResolver', ()=> {

  it('should not resolve contextual items if context isn\'t provided', (done) => {
    var tree = getTestTree(['current_object']);
    var resolver = new ContextualRulesResolver(PromiseAdapterMock.mockAdapter());
    resolver.resolve(tree).then(()=> {
      expect(tree.rules[0].value).to.deep.equal(['current_object']);
      done();
    }).catch(done);
  });

  it('should resolve contextual item current_object when not in an array', (done) => {
    var id = 'emf:123-456-abc';
    var tree = getTestTree('current_object');
    var context = getTestContext(id, true);

    var resolver = new ContextualRulesResolver(PromiseAdapterMock.mockAdapter());
    resolver.resolve(tree, context).then(()=> {
      expect(tree.rules[0].value).to.deep.equal(id);
      done();
    }).catch(done);
  });

  it('should resolve contextual item current_object when in an array', (done) => {
    var id = 'emf:123-456-abc';
    var tree = getTestTree(['current_object']);
    var context = getTestContext(id, true);
    var resolver = new ContextualRulesResolver(PromiseAdapterMock.mockAdapter());
    resolver.resolve(tree, context).then(()=> {
      expect(tree.rules[0].value).to.deep.equal([id]);
      done();
    }).catch(done);
  });

  it('should set fake contextual rule value if current_object is not persisted', (done) => {
    var tree = getTestTree('current_object');
    var context = getTestContext(CURRENT_OBJECT_TEMP_ID, false);
    var resolver = new ContextualRulesResolver(PromiseAdapterMock.mockAdapter());
    resolver.resolve(tree, context).then(()=> {
      expect(tree.rules[0].value).to.exist;
      expect(tree.rules[0].value).to.not.equal(CURRENT_OBJECT_TEMP_ID);
      done();
    }).catch(done);
  });

  it('should set fake contextual rule value if current_object is not persisted and is in an array', (done) => {
    var tree = getTestTree(['current_object', 'emf:123']);
    var context = getTestContext(CURRENT_OBJECT_TEMP_ID, false);
    var resolver = new ContextualRulesResolver(PromiseAdapterMock.mockAdapter());
    resolver.resolve(tree, context).then(()=> {
      expect(tree.rules[0].value).to.exist;
      expect(tree.rules[0].value).to.not.include(CURRENT_OBJECT_TEMP_ID);
      done();
    }).catch(done);
  });

  it('should resolve embedded search trees', (done) => {
    var tree = getTestTree(['current_object', 'emf:123']);
    var embeddedTree = getTestTree(['current_object', 'emf:456']);
    var embeddedRule = {field:'emf:createdBy', operation: 'set_to_query', value: embeddedTree};
    tree.rules.push(embeddedRule);

    var context = getTestContext('emf:789', true);
    var resolver = new ContextualRulesResolver(PromiseAdapterMock.mockAdapter());

    resolver.resolve(tree, context).then(()=> {
      var embeddedRules = tree.rules[1].value.rules;
      expect(embeddedRules[0]).to.exist;
      expect(embeddedRules[0].value).to.not.include('current_object');
      expect(embeddedRules[0].value).to.include('emf:789');
      done();
    }).catch(done);
  });

  function getTestTree(value) {
    return {
      condition: 'OR',
      rules: [
        {field: 'location', operation: 'equals', value: value}
      ]
    };
  }

  function getTestContext(id, persisted) {
    return {
      getCurrentObject: sinon.spy(() => {
        return new Promise((resolve) => {
          resolve({
            getId: (() => {
              return id;
            }),isPersisted: (() => {
              return persisted;
            })
          });
        });
      })
    };
  }

});
