import {ActionsMenu} from 'idoc/actions-menu/actions-menu';
import {ActionsService} from 'services/rest/actions-service';
import {ActionExecutor} from 'services/actions/action-executor';
import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

const PLACEHOLDER = 'actions.menu';

describe('ActionsMenu', () => {

  let actionsMenu;
  let actionsService;
  let actionExecutor;

  beforeEach(() => {
    PluginRegistry.clear();
    PluginRegistry.add('actions', {
      'name': 'transitionAction',
      'module': 'transition'
    });

    ActionsMenu.prototype.context = {
      placeholder: PLACEHOLDER,
      currentObject: {
        getId: () => 'emf:123456',
        getContextPathIds: () => ['emf:234567', 'emf:123456']
      }
    };

    actionsService = stubActionsService();
    actionExecutor = stub(ActionExecutor);
    actionsMenu = new ActionsMenu(actionsService, actionExecutor);
  });

  describe('createActionsMenuConfig()', () => {
    it('should initialize default value for display mode if undefined', () => {
      actionsMenu.createActionsMenuConfig();
      expect(actionsMenu.context.displayMode).to.equal(ActionsMenu.DROPDOWN_DISPLAY_MODE);
    });

    it('should create dropdown config when display mode is dropdown', () => {
      actionsMenu.createActionsMenuConfig();
      expect(actionsMenu.actionsMenuConfig).to.exist;
      expect(actionsMenu.actionsMenuConfig.placeholder).to.equal(PLACEHOLDER);
      expect(actionsMenu.actions).to.not.exist;
    });

    it('should initialize actions when display mode is for links', () => {
      actionsMenu.context.displayMode = ActionsMenu.LINKS_DISPLAY_MODE;

      actionsMenu.createActionsMenuConfig();

      expect(actionsMenu.actions).to.exist;
      expect(actionsMenu.actionsMenuConfig).to.not.exist;
    });
  });

  describe('loadItems()', () => {
    it('should invoke actions service and actions helper providing correct parameters', (done) => {
      actionsMenu.loadItems().then((actions) => {
        expect(actions.length).to.equal(4);
        done();
      }).catch(done);
    });

    it('should filter actions when callback is passed', (done) => {
      actionsMenu.context.filterActions = (actions) => {
        return actions.filter(action => action.userOperation === 'activate' || action.userOperation === 'deactivate');
      };

      actionsMenu.loadItems().then((actions) => {
        expect(actions.length).to.equal(2);
        expect(actions[0].action).to.equal('activate');
        expect(actions[1].action).to.equal('deactivate');
        done();
      }).catch(done);
    });

    it('should return empty array when there are no actions', () => {
      actionsService.getActions.returns(PromiseStub.resolve({}));

      expect(actionsMenu.loadItems()).to.eventually.deep.equal([]);
    });
  });

  describe('renderMenu()', () => {
    it('should return true if renderMenu handler passed to the menu returns true', () => {
      ActionsMenu.prototype.context = {
        renderMenu: () => true
      };

      expect(actionsMenu.renderMenu()).to.be.true;
    });

    it('should return true if renderMenu handler passed to the menu returns false', () => {
      ActionsMenu.prototype.context = {
        renderMenu: () => false
      };

      expect(actionsMenu.renderMenu()).to.be.false;
    });
  });

  describe('executeAction()', () => {
    it('should invoke action executor', () => {
      let action = {action: 'lock'};

      actionsMenu.executeAction(action);

      expect(actionExecutor.execute.calledWith(action, actionsMenu.context)).to.be.true;
    });
  });

  afterEach(() => {
    PluginRegistry.clear();
  });

  function stubActionsService() {
    let actionsService = stub(ActionsService);
    actionsService.getActions.returns(PromiseStub.resolve({
      data: [{
        userOperation: 'lock',
        serverOperation: 'transition'
      }, {
        userOperation: 'move',
        serverOperation: 'transition'
      }, {
        userOperation: 'activate',
        serverOperation: 'transition'
      }, {
        userOperation: 'deactivate',
        serverOperation: 'transition'
      }]
    }));
    return actionsService;
  }

});