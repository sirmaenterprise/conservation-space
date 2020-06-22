import {CreateNew} from 'layout/top-header/main-menu/create-new/create-new';
import {PromiseStub} from 'test/promise-stub';

const DIALOG_TITLE = 'You have unsaved data which will be lost';

describe('CreateNew', function () {

  describe('createNew()', () => {
    it('should open instance create dialog with no parent (parentId = null) if no context is available', () => {
      let createPanelService = {
        openCreateInstanceDialog: sinon.spy()
      };
      var createNew = new CreateNew(createPanelService, {}, mockWindowAdapter(), {getCurrentContext: () => {}}, {});
      createNew.createNew();
      var expectedObject = {
        parentId: null,
        returnUrl: 'return_url',
        operation: 'create',
        scope: {}
      };
      expect(createNew.createPanelService.openCreateInstanceDialog.callCount).to.equal(1);
      var actualConfiguration = createNew.createPanelService.openCreateInstanceDialog.getCall(0).args[0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(expectedObject);
    });

    it('should open instance create dialog with expected data and objectId', () => {
      let createPanelService = {
        openCreateInstanceDialog: sinon.spy()
      };
      var createNew = new CreateNew(createPanelService, {}, mockWindowAdapter(), mockIdocContextFactory(true), {});
      createNew.createNew();
      var expectedObject = {
        parentId: '123',
        returnUrl: 'return_url',
        operation: 'create',
        scope: {}
      };
      expect(createNew.createPanelService.openCreateInstanceDialog.callCount).to.equal(1);
      var actualConfiguration = createNew.createPanelService.openCreateInstanceDialog.getCall(0).args[0];
      delete actualConfiguration.onClosed;
      expect(actualConfiguration).to.deep.equal(expectedObject);
    });

    it('should open instance create dialog with expected data and parentId', () => {
      let createPanelService = {
        openCreateInstanceDialog: sinon.spy()
      };
      var spyConfirmation = sinon.spy();
      var dialogServiceMock = {
        confirmation: spyConfirmation
      };

      var createNew = new CreateNew(createPanelService, {}, mockWindowAdapter(), mockIdocContextFactory(false),
        dialogServiceMock, mockTranslateService());
      createNew.createNew();

      var args = spyConfirmation.getCall(0).args;
      expect(spyConfirmation.callCount).to.equal(1);
      expect(args[0]).to.equal(DIALOG_TITLE);
      expect(args[1]).to.equal(null);
      expect(args[2].buttons.length).to.equal(2);
      expect(args[2].buttons[0].id).to.equal(CreateNew.CONFIRM);
      expect(args[2].buttons[1].id).to.equal(CreateNew.CANCEL);
      expect(args[2].onButtonClick !== undefined).to.be.true;

      var buttonClick = args[2].onButtonClick;
      var dialogConfig = {
        dismiss: sinon.spy()
      };
      buttonClick(CreateNew.CONFIRM, {}, dialogConfig);
      expect(createNew.createPanelService.openCreateInstanceDialog.callCount).to.equal(1);
      expect(dialogConfig.dismiss.callCount).to.equal(1);

      createNew.createPanelService.openCreateInstanceDialog.reset();
      dialogConfig.dismiss.reset();

      buttonClick(CreateNew.CANCEL, {}, dialogConfig);
      expect(createNew.createPanelService.openCreateInstanceDialog.callCount).to.equal(0);
      expect(dialogConfig.dismiss.callCount).to.equal(1);
    });
  });
});

function mockTranslateService() {
  return {
    translateInstant: () => DIALOG_TITLE
  };
}

function mockIdocContextFactory(persisted) {
  return {
    getCurrentContext: () => {
      return {
        getCurrentObject: () => {
          return PromiseStub.resolve({
            id : '123',
            models: {
              parentId: 'parent_id'
            },
            isPersisted: () => {
              return persisted;
            }
          });
        }
      };
    }
  };
}

function mockWindowAdapter() {
  return {
    location: {
      href: 'return_url'
    }
  };
}