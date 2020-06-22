import {ModelTree, MODIFIED_NODE_CLASS} from 'administration/model-management/components/tree/model-tree';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelTreeService} from 'administration/model-management/services/model-tree-service';
import {EventEmitter} from 'common/event-emitter';
import {ModelEvents} from 'administration/model-management/model/model-events';
import {ModelClass} from 'administration/model-management/model/model-class';
import {ModelValue} from 'administration/model-management/model/model-value';
import {ModelSingleAttribute} from 'administration/model-management/model/attributes/model-single-attribute';
import {ModelAttributeTypes} from 'administration/model-management/model/attributes/model-attribute-types';
import {
  NODE_ADD_CLASS_EVENT,
  NODE_REMOVE_CLASS_EVENT,
  NODE_SET_TEXT_EVENT
} from 'components/object-browser/object-browser';
import {stub} from 'test/test-utils';

const LABEL = ModelAttributeTypes.MULTI_VALUE.MODEL_LABEL_TYPE;

describe('ModelTree', () => {

  let modelTree;
  let promiseAdapterStub = stub(PromiseAdapter);
  let modelTreeServiceStub = stub(ModelTreeService);
  let ngElement = {
    resizable: sinon.spy(() => {
    }),
    css: sinon.spy(() => {
    })
  };

  beforeEach(() => {
    modelTree = new ModelTree(ngElement, modelTreeServiceStub, promiseAdapterStub);
    modelTree.emitter = stub(EventEmitter);
  });

  it('should have default configuration provided', () => {
    expect(modelTree.config).to.deep.eq({
      nodePath: [],
      selectable: false,
      enableSearch: true,
      openInNewWindow: false,
      preventLinkRedirect: true
    });
  });

  it('should initialize tree configuration', () => {
    modelTreeServiceStub.findNodePathById.returns('actual-node-path');
    modelTree.ngOnInit();

    expect(modelTree.loader.getNodes).to.exist;
    expect(modelTree.config.nodePath).to.eq('actual-node-path');
    expect(modelTreeServiceStub.getTree.calledOnce).to.be.true;
    expect(modelTreeServiceStub.findNodePathById.calledOnce).to.be.true;
  });

  it('should subscribe to model events for state & attribute change and model revert', () => {
    modelTree.ngOnInit();

    expect(modelTree.emitter.subscribe.calledThrice).to.be.true;
    expect(modelTree.emitter.subscribe.calledWith(ModelEvents.MODEL_CHANGED_EVENT)).to.be.true;
    expect(modelTree.emitter.subscribe.calledWith(ModelEvents.MODEL_STATE_CHANGED_EVENT)).to.be.true;
    expect(modelTree.emitter.subscribe.calledWith(ModelEvents.MODEL_ATTRIBUTE_CHANGED_EVENT)).to.be.true;
  });

  it('should properly resolve and publish event for tree node color', () => {
    modelTree.resolveNodeColor(new ModelClass('emf:Entity'), true);
    expect(modelTree.emitter.publish.calledWith(NODE_ADD_CLASS_EVENT, {
      clazz: MODIFIED_NODE_CLASS,
      id: 'emf:Entity'
    })).to.be.true;

    modelTree.resolveNodeColor(new ModelClass('emf:Entity'), false);
    expect(modelTree.emitter.publish.calledWith(NODE_REMOVE_CLASS_EVENT, {
      clazz: MODIFIED_NODE_CLASS,
      id: 'emf:Entity'
    })).to.be.true;
  });

  it('should not publish tree node color when provided model is not of base type', () => {
    modelTree.resolveNodeColor(new ModelSingleAttribute('attribute'), false);
    expect(modelTree.emitter.publish.called).to.be.false;
  });

  it('should properly resolve and publish tree node name', () => {
    let model = new ModelClass('clazz');
    let name = new ModelValue('en', 'name');
    let label = new ModelSingleAttribute()
      .setType(LABEL)
      .setParent(model)
      .setValue(name);

    modelTree.resolveNodeText(label);
    expect(modelTree.emitter.publish.calledWith(NODE_SET_TEXT_EVENT, {
      id: 'clazz',
      text: 'name'
    })).to.be.true;
  });

  it('should not publish event for tree node name', () => {
    modelTree.resolveNodeText(new ModelSingleAttribute('type').setType('type'));
    expect(modelTree.emitter.publish.called).to.be.false;
  });
});