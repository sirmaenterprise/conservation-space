import {InstanceObject} from 'models/instance-object';
import {ActionsHelper} from 'idoc/actions/actions-helper';

describe('ActionsHelper', () => {

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
      let config = ActionsHelper.getActionsLoaderConfig(instanceObject, 'placeholder');
      expect(config).to.deep.equal({
        'context-id': null,
        placeholder: 'placeholder',
        path: ['emf:123456', 'emf:234567']
      });
    });
  });

  describe('collectImplementedHandlers()', () => {

    PluginRegistry.clear();

    PluginRegistry.add('actions', {
      'name': 'start',
      'module': 'start/action/handler'
    });

    PluginRegistry.add('actions', {
      'name': 'stop',
      'module': 'stop/action/handler'
    });

    it('should convert returned by the PluginsRegistry array for registered actions to a map', () => {
      expect(ActionsHelper.collectImplementedHandlers('search')).to.eql({
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
      PluginRegistry.clear();

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

      expect(ActionsHelper.collectImplementedHandlers('search')).to.eql({
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

  describe('should filter actions properly', () => {

    it('should remove actions without implemented handlers', () => {
      let filterCriteria = {
        handlers: {
          'startAction': {},
          'stopAction': {},
          'printAction': {}
        }
      };

      let actions = [
        {serverOperation: 'start'},
        {serverOperation: 'edit'},
        {data: [
          {serverOperation: 'print'},
          {serverOperation: 'printTab'}
        ]}
      ];

      actions = ActionsHelper.filterGroupsAndActions(actions, filterCriteria);

      expect(actions.length).to.equal(2);
      expect(actions[0].serverOperation).to.equal('start');
      expect(actions[1].data[0].serverOperation).to.equal('print');
    });
  });

});