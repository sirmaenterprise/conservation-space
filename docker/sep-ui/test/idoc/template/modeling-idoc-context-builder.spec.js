import {ModelingIdocContextBuilder} from 'idoc/template/modeling-idoc-context-builder';
import {IdocContext} from 'idoc/idoc-context';
import {DefinitionService} from 'services/rest/definition-service';
import {TEMPLATE_DEFINITION_TYPE, FOR_OBJECT_TYPE} from 'idoc/template/template-constants';
import {stub} from 'test/test-utils';
import {PromiseStub} from 'test/promise-stub';

describe('ModelingIdocContextBuilder', function () {

  it('should calculate the modeling flag based on the type of the current object', function () {
    var idocContext = stub(IdocContext);

    var currentObject = {
      instanceType: TEMPLATE_DEFINITION_TYPE
    };

    idocContext.getCurrentObject.returns(PromiseStub.resolve(currentObject));

    var modelingIdocContextBuilder = new ModelingIdocContextBuilder(stub(DefinitionService), PromiseStub);
    modelingIdocContextBuilder.wrapIdocContext(idocContext);

    idocContext.getCurrentObject();

    expect(idocContext.setModeling.firstCall.args[0]).to.be.true;

    idocContext.getCurrentObject();

    expect(idocContext.getCurrentObject.calledTwice).to.equal(true, 'The original getCurrentObject should be unwrapped after the first call');
  });

  it('should undecorate decorated methods if the current object is not template', function () {
    var idocContext = stub(IdocContext);

    var currentObject = {
      instanceType: 'test',
      getId: function () {
        return 'id'
      }
    };

    idocContext.getCurrentObject.returns(PromiseStub.resolve(currentObject));

    var modelingIdocContextBuilder = new ModelingIdocContextBuilder(stub(DefinitionService), PromiseStub);

    var wrappedContext = modelingIdocContextBuilder.wrapIdocContext(idocContext);

    var wrappedGetCurrentObjectSpy = sinon.spy(wrappedContext, 'getCurrentObject');

    wrappedContext.getCurrentObject();
    wrappedContext.getCurrentObject();

    // the second time, the method gets called it should not be the spy, but the unwrapped method
    expect(wrappedGetCurrentObjectSpy.calledOnce).to.be.true;

    expect(idocContext.getCurrentObject.calledTwice).to.be.true;
  });

  it('should cache the substitution object', function () {
    var idocContext = stub(IdocContext);

    const TYPE_UNDER_MODELING = 'testType';

    var currentObject = {
      instanceType: TEMPLATE_DEFINITION_TYPE,
      getPropertyValue: function () {
        return TYPE_UNDER_MODELING;
      },
      getId: function () {
        return 'id'
      }
    };

    idocContext.getCurrentObject.returns(PromiseStub.resolve(currentObject));

    var definitionServiceStub = stub(DefinitionService);
    definitionServiceStub.getDefinitions.returns(PromiseStub.resolve({
      data: {
        [TYPE_UNDER_MODELING]: {id: 1}
      }
    }));

    var modelingIdocContextBuilder = new ModelingIdocContextBuilder(definitionServiceStub, PromiseStub);

    var wrappedContext = modelingIdocContextBuilder.wrapIdocContext(idocContext);

    var wrappedGetCurrentObjectSpy = sinon.spy(wrappedContext, 'getCurrentObject');

    var firstResult;

    wrappedContext.getCurrentObject().then(function (result) {
      firstResult = result;
    });

    var secondResult;

    wrappedContext.getCurrentObject().then(function (result) {
      secondResult = result;
    });

    expect(firstResult).to.equal(secondResult);

    expect(wrappedGetCurrentObjectSpy.calledTwice).to.be.true;

    expect(idocContext.getCurrentObject.calledOnce).to.be.true;
  });

  it('should provide substituted object if a shared object with the current object id is requires', function (done) {
    const ID = 'testId';
    var idocContext = stub(IdocContext);
    idocContext.getCurrentObjectId.returns(ID);

    var modelingIdocContextBuilder = new ModelingIdocContextBuilder(stub(DefinitionService), PromiseStub);

    var wrappedContext = modelingIdocContextBuilder.wrapIdocContext(idocContext);

    var substitution = 'sub';
    wrappedContext.substitution = substitution;

    wrappedContext.getSharedObject(ID).then(object => {
      expect(object).to.equal(substitution);
      done();
    });
  });
});