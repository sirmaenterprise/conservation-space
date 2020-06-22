import {IdocContext} from 'idoc/idoc-context';
import {PromiseStub} from 'test/promise-stub';
import {InstanceObject} from 'models/instance-object';
import {IdocPageTestHelper} from './idoc-page-test-helper';
import {stub} from 'test/test-utils';

const IDOC_ID = 'emf:123456';

describe('Idoc drafts', () => {

  it('should load draft and append content when mode is edit and idoc is persisted', () => {
    let instanceObject = stub(InstanceObject);
    instanceObject.isPersisted.returns(true);
    instanceObject.isLocked.returns(true);
    instanceObject.getId.returns('emf:123456');

    let idocContext = stub(IdocContext);
    idocContext.isEditMode.returns(true);
    idocContext.reloadObjectDetails.returns(PromiseStub.resolve());

    let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = instanceObject;
    idocPage.context = idocContext;
    idocPage.idocDraftService.loadDraft.returns(PromiseStub.resolve({loaded: true}));

    let spyAppendContent = sinon.spy(idocPage, 'appendContent');
    let spyStartDraftInterval = sinon.spy(idocPage, 'startDraftInterval');

    idocPage.appendDraftContent({editAllowed: true});
    expect(spyAppendContent.called).to.be.true;
    expect(spyStartDraftInterval.called).to.be.true;
    expect(idocPage.eventbus.publish.called).to.be.true;
  });

  it('should load draft but not append content when result not loaded', () => {
    let instanceObject = stub(InstanceObject);
    instanceObject.isPersisted.returns(true);
    instanceObject.isLocked.returns(true);
    instanceObject.getId.returns('emf:123456');

    let idocContext = stub(IdocContext);
    idocContext.isEditMode.returns(true);
    idocContext.reloadObjectDetails.returns(PromiseStub.resolve());

    let idocPage = IdocPageTestHelper.instantiateIdocPage(IDOC_ID);
    idocPage.currentObject = instanceObject;
    idocPage.context = idocContext;
    idocPage.idocDraftService.loadDraft.returns(PromiseStub.resolve({loaded: false}));

    let spyAppendContent = sinon.spy(idocPage, 'appendContent');
    let spyStartDraftInterval = sinon.spy(idocPage, 'startDraftInterval');

    idocPage.appendDraftContent({editAllowed: true});
    expect(spyAppendContent.called).to.be.false;
    expect(spyStartDraftInterval.called).to.be.true;
    expect(idocPage.eventbus.publish.called).to.be.true;
  });

  it('startDraftInterval should start an interval if mode is edit', () => {
    let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

    let idocPage = IdocPageTestHelper.instantiateIdocPage('emf:1234', 'edit');
    idocPage.currentObject = instanceObject;

    let intervalSpy = sinon.spy(idocPage, '$interval');

    idocPage.startDraftInterval();

    expect(intervalSpy.callCount).to.equal(1);
  });

  it('stopDraftInterval should cancel draft interval if such exists', () => {
    let instanceObject = new InstanceObject(IDOC_ID, IdocPageTestHelper.generateModels(), IdocPageTestHelper.generateIntialContent());

    let idocPage = IdocPageTestHelper.instantiateIdocPage('emf:1234', 'edit');
    idocPage.currentObject = instanceObject;

    idocPage.$interval.cancel = sinon.spy();
    idocPage.draftInterval = idocPage.$interval;

    idocPage.stopDraftInterval();

    expect(idocPage.$interval.cancel.callCount).to.equal(1);
  });

});