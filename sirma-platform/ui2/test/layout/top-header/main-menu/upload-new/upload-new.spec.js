import {PromiseStub} from 'test/promise-stub';
import {CreatePanelService} from 'services/create/create-panel-service';
import {IdocContextFactory} from 'services/idoc/idoc-context-factory';
import {DialogService} from 'components/dialog/dialog-service';
import {TranslateService} from 'services/i18n/translate-service';
import {UploadNew} from 'layout/top-header/main-menu/upload-new/upload-new';
import {stub} from 'test/test-utils';
import {Eventbus} from 'services/eventbus/eventbus';

describe('UploadNew', () => {

  const HREF = 'href';

  let createPanelService;
  let windowAdapter = {
    location: {
      href: HREF
    }
  };
  let idocContextFactory;
  let dialogService;
  let translateService;
  let eventbus;
  let scope;

  let uploadNew;

  beforeEach(function () {
    createPanelService = stub(CreatePanelService);
    idocContextFactory = stub(IdocContextFactory);
    dialogService = stub(DialogService);
    translateService = stub(TranslateService);
    eventbus = stub(Eventbus);

    uploadNew = new UploadNew(createPanelService, scope, windowAdapter, idocContextFactory, dialogService, translateService, eventbus);
  });

  describe('execute upload with context', () => {
    it('should not display confirmation dialog if current object is persisted', () => {
      idocContextFactory.getCurrentContext.returns(createContext(true));

      uploadNew.uploadNew();

      expect(dialogService.confirmation.called).to.be.false;
    });

    it('should display confirmation dialog if current object is not persisted', () => {
      idocContextFactory.getCurrentContext.returns(createContext(false));

      uploadNew.uploadNew();

      expect(dialogService.confirmation.called).to.be.true;
    });

    describe('onButtonClick', () => {
      it('should open upload instance dialog when ok button is clicked', () => {
        idocContextFactory.getCurrentContext.returns(createContext(false));

        uploadNew.uploadNew();

        let args = dialogService.confirmation.getCall(0).args[2];
        let onButtonClick = args.onButtonClick;
        let dialogConfig = {dismiss: sinon.spy()};

        onButtonClick(UploadNew.CONFIRM, null, dialogConfig);

        expect(createPanelService.openCreateInstanceDialog.calledOnce).to.be.true;
        expect(dialogConfig.dismiss.calledOnce).to.be.true;
      });

      it('should not open upload instance dialog when cancel button is clicked', () => {
        idocContextFactory.getCurrentContext.returns(createContext(false));

        uploadNew.uploadNew();

        let args = dialogService.confirmation.getCall(0).args[2];
        let onButtonClick = args.onButtonClick;
        let dialogConfig = {dismiss: sinon.spy()};

        onButtonClick(UploadNew.CANCEL, null, dialogConfig);

        expect(createPanelService.openUploadInstanceDialog.called).to.be.false;
        expect(dialogConfig.dismiss.calledOnce).to.be.true;
      });
    });

    function createContext(persisted) {
      return {
        getCurrentObject() {
          return PromiseStub.resolve({
            id: 'emf:001',
            models: {parentId: 'emf:parentId'},
            isPersisted() {
              return persisted;
            }
          });
        }
      };
    }
  });

  it('should open create instance dialog without confirmation when button is clicked with out context', () =>{

    uploadNew.uploadNew();

    let args = createPanelService.openCreateInstanceDialog.getCall(0).args[0];
    expect(args.parentId === null).to.be.true;
    expect(args.returnUrl).to.equal(HREF);
    expect(args.operation).to.equal('upload');
    expect(args.defaultTab).to.equal('file-upload-panel');
    expect(args.scope).to.equal(scope);
  });

  describe('dialog onClose', () => {
    it('should not publish RefreshWidgetsCommand when instance not created', () => {
      uploadNew.uploadNew();
      let args = createPanelService.openCreateInstanceDialog.getCall(0).args[0];
      let onClosed = args.onClosed;

      onClosed({result:{}});

      expect(eventbus.publish.called).to.be.false;

    });

    it('should publish RefreshWidgetsCommand when instance is created', () => {
      uploadNew.uploadNew();
      let args = createPanelService.openCreateInstanceDialog.getCall(0).args[0];
      let onClosed = args.onClosed;

      onClosed({instanceCreated: true});

      expect(eventbus.publish.called).to.be.true;

    });
  });
});