import {ActionsMenu} from 'idoc/actions-menu/actions-menu';
import {IdocContext, InstanceObject} from 'idoc/idoc-context';
import {MODE_PREVIEW, MODE_EDIT} from 'idoc/idoc-constants';

const PLACEHOLDER = 'actions.menu';

describe('ActionsMenu', () => {

  let stubCreateActionsMenuConfig = sinon.stub(ActionsMenu.prototype, 'createActionsMenuConfig');

  describe('loadItems()', () => {
    it('should invoke actions service providing correct parameters', (done) => {
      let actionsService = {
        getActions: () => {
        }
      };
      sinon.stub(actionsService, 'getActions', () => {
        return new Promise((resolve) => {
          resolve({
            data: [
              {userOperation: 'start', serverOperation: 'transition', label: 'Start', disabled: false},
              {userOperation: 'delete', serverOperation: 'delete', label: 'Delete', disabled: true}
            ]
          });
        });
      });
      let id = 'emf:123456';
      let serviceConfig = {
        'context-id': null,
        'placeholder': PLACEHOLDER,
        'path': ['emf:234567', 'emf:123456']
      };

      let stubCollectImplementedHandlers = sinon.stub(ActionsMenu, 'collectImplementedHandlers', () => {
        return {
          'transitionAction': {},
          'deleteAction': {}
        };
      });
      let actionsMenu = new ActionsMenu(actionsService);
      actionsMenu.context = {
        placeholder: PLACEHOLDER,
        currentObject: {
          getId: () => {
            return 'emf:123456'
          },
          getContextPathIds: () => {
            return ['emf:234567', 'emf:123456']
          }
        }
      };
      actionsMenu.loadItems().then((actions) => {
        expect(actionsService.getActions.getCall(0).args[0]).to.equal('emf:123456');
        expect(actionsService.getActions.getCall(0).args[1]).to.eql({
          'context-id': null,
          'placeholder': PLACEHOLDER,
          'path': ['emf:234567', 'emf:123456']
        });
        expect(actions).to.eql([
          {
            action: 'start',
            name: 'transitionAction',
            data: undefined,
            label: 'Start',
            tooltip: undefined,
            disabled: false,
            confirmationMessage: undefined,
            extensionPoint: 'actions',
            configuration: undefined
          }
        ]);
        stubCollectImplementedHandlers.restore();
        done();
      }).catch(done);
    });

    it('should process correctly group actions', (done) => {
      let actionsService = {
        getActions: () => {
        }
      };
      sinon.stub(actionsService, 'getActions', () => {
        return new Promise((resolve) => {
          resolve({
            data: [
              {
                userOperation: 'start',
                serverOperation: 'transition',
                label: 'Start',
                disabled: false,
                data: [{userOperation: 'delete', serverOperation: 'delete', label: 'Delete', disabled: false}]
              }
            ]
          });
        });
      });
      let id = 'emf:123456';
      let serviceConfig = {
        'context-id': null,
        'placeholder': PLACEHOLDER,
        'path': ['emf:234567', 'emf:123456']
      };

      let stubCollectImplementedHandlers = sinon.stub(ActionsMenu, 'collectImplementedHandlers', () => {
        return {
          'transitionAction': {},
          'deleteAction': {}
        };
      });
      let actionsMenu = new ActionsMenu(actionsService);
      actionsMenu.context = {
        placeholder: PLACEHOLDER,
        currentObject: {
          getId: () => {
            return 'emf:123456'
          },
          getContextPathIds: () => {
            return ['emf:234567', 'emf:123456']
          }
        }
      };
      actionsMenu.loadItems().then((actions) => {
        expect(actionsService.getActions.getCall(0).args[0]).to.equal('emf:123456');
        expect(actionsService.getActions.getCall(0).args[1]).to.eql({
          'context-id': null,
          'placeholder': PLACEHOLDER,
          'path': ['emf:234567', 'emf:123456']
        });

        let response = [
          {
            action: 'start',
            name: undefined,
            data: [
              {userOperation: 'delete', serverOperation: 'delete', label: 'Delete', disabled: false}
            ],
            label: 'Start',
            tooltip: undefined,
            disabled: false,
            confirmationMessage: undefined,
            extensionPoint: 'actions',
            configuration: undefined
          }];
        response[0].data.items = [{
          action: 'delete',
          name: 'deleteAction',
          data: undefined,
          label: 'Delete',
          tooltip: undefined,
          disabled: false,
          confirmationMessage: undefined,
          extensionPoint: 'actions',
          configuration: undefined
        }];

        expect(actions).to.eql(response);
        stubCollectImplementedHandlers.restore();
        done();
      }).catch(done);
    });
  });

  describe('getActionsLoaderConfig()', () => {
    it('should construct proper configuration object for actions loading', () => {
      let models = {
        path: [
          {id: 'emf:123456', type: 'projectinstance', compactHeader: 'compactHeader'},
          {id: 'emf:234567', type: 'documentinstance', compactHeader: 'compactHeader'}
        ]
      };
      let instanceObject = new InstanceObject('emf:123456', models, 'content');
      instanceObject.setContextPath(models.path);
      let config = ActionsMenu.getActionsLoaderConfig(instanceObject, PLACEHOLDER);
      expect(config).to.deep.equal({
        'context-id': null,
        placeholder: PLACEHOLDER,
        path: ['emf:123456', 'emf:234567']
      });
    });
  });

  describe('renderMenu()', () => {
    it('should return true if renderMenu handler passed to the menu returns true', () => {
      ActionsMenu.prototype.context = {
        renderMenu: () => {
          return true
        }
      };
      let actionsMenu = new ActionsMenu({});
      expect(actionsMenu.renderMenu()).to.be.true;
    });

    it('should return true if renderMenu handler passed to the menu returns false', () => {
      ActionsMenu.prototype.context = {
        renderMenu: () => {
          return false
        }
      };
      let actionsMenu = new ActionsMenu({});
      expect(actionsMenu.renderMenu()).to.be.false;
    });
  });

  describe('collectImplementedHandlers()', () => {

    PluginRegistry.add('actions', {
      'name': 'start',
      'module': 'start/action/handler'
    });

    PluginRegistry.add('actions', {
      'name': 'stop',
      'module': 'stop/action/handler'
    });

    it('should convert returned by the PluginsRegistry array for registered actions to a map', () => {
      expect(ActionsMenu.collectImplementedHandlers('search')).to.eql({
        'start': {
          'name': 'start',
          'module': 'start/action/handler'
        },
        'stop': {
          'name': 'stop',
          'module': 'stop/action/handler'
        }
      });
    });

    it('should skip actions that are not applicable for given placeholder/context', () => {
      PluginRegistry.add('actions', {
        'name': 'printTab',
        'module': 'printTab/action/handler',
        'notApplicable': ['search']
      });

      PluginRegistry.add('actions', {
        'name': 'exportTab',
        'module': 'exportTab/action/handler',
        'notApplicable': ['search']
      });

      expect(ActionsMenu.collectImplementedHandlers('search')).to.eql({
        'start': {
          'name': 'start',
          'module': 'start/action/handler'
        },
        'stop': {
          'name': 'stop',
          'module': 'stop/action/handler'
        }
      });
    });
  });

});
