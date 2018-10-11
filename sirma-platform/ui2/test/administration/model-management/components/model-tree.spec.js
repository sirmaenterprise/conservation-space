import {ModelTree} from 'administration/model-management/components/model-tree';
import {PromiseAdapter} from 'adapters/angular/promise-adapter';
import {ModelTreeService} from 'administration/model-management/services/model-tree-service';

import {stub} from 'test/test-utils';

describe('ModelTree', () => {

  let modelTree;
  let promiseAdapterStub = stub(PromiseAdapter);
  let modelTreeServiceStub = stub(ModelTreeService);

  beforeEach(() => {
    modelTree = new ModelTree(modelTreeServiceStub, promiseAdapterStub);
  });

  it('should have default configuration provided', () => {
    expect(modelTree.config).to.deep.eq({
      nodePath: [],
      selectable: false,
      enableSearch: true,
      openInNewWindow: false,
      preventLinkRedirect: true,
    });
  });

  it('should initialize tree configuration ', () => {
    modelTreeServiceStub.findNodePathById.returns('actual-node-path');
    modelTree.ngOnInit();

    expect(modelTree.loader.getNodes).to.exist;
    expect(modelTree.config.nodePath).to.eq('actual-node-path');
    expect(modelTreeServiceStub.getTree.calledOnce).to.be.true;
    expect(modelTreeServiceStub.findNodePathById.calledOnce).to.be.true;
  });
});