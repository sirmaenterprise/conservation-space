import {RoleActionsTable} from 'administration/role-actions-table/role-actions-table';
import {PromiseStub} from 'test/promise-stub';

describe('RoleActionsTable', () => {

  let serviceData = {
    actions: [{
      "id": "unlock",
      "enabled": true,
      "label": "Unlock",
      "tooltip": "Unlock an instance"
    }, {
      "id": "approve",
      "enabled": true,
      "label": "Approve",
      "tooltip": "Approve an instance"
    }],
    roles: [{
      "id": "CONTRIBUTOR",
      "label": "Contributor",
      "canRead": true,
      "canWrite": true,
      "order": 10
    }],
    roleActions: [{
      "action": "unlock",
      "role": "CONTRIBUTOR",
      "enabled": true,
      "filters": ["CREATEDBY"]
    }, {
      "action": "approve",
      "role": "CONSUMER",
      "enabled": false,
      "filters": []
    }]
  };
  let roleManagementService = {
    getRoleActions: () => {
      return PromiseStub.resolve({
        data: serviceData
      });
    },
    getFilters: () => {
      return PromiseStub.resolve(() => {
        data: ['CREATEDBY', 'ASSIGNEE']
      });
    },
    saveRoleActions: sinon.spy(() => {
      return PromiseStub.resolve({
        data: serviceData
      });
    })
  };
  let translateService = {
    translateInstant: () => {
      return 'label';
    }
  };
  let roleActionsTable;

  beforeEach(function () {
    roleActionsTable = new RoleActionsTable(roleManagementService, PromiseStub, translateService);
  });

  describe('initialize', function () {
    it('should convert role actions to map', () => {
      expect(roleActionsTable.roleActions['CONTRIBUTOR']['unlock']).to.exist;
    });
  });

  describe('createFiltersSelectConfig', function () {
    it('should correctly create a select config', () => {
      expect(roleActionsTable.createFiltersSelectConfig()).to.deep.equal({
        multiple: true,
        placeholder: translateService.translateInstant(),
        data: roleActionsTable.filters
      });
    });
  });

  describe('convertToMap', function () {
    it('should correctly convert roleActions response array to map', () => {
      let roleActions = [{
        "action": "unlock",
        "role": "CONTRIBUTOR"
      }, {
        "action": "unlock",
        "role": "COLLABORATOR"
      }, {
        "action": "approve",
        "role": "CONSUMER"
      }, {
        "action": "lock",
        "role": "CONTRIBUTOR"
      }];

      let converted = roleActionsTable.convertToMap(roleActions);
      expect(converted['CONTRIBUTOR']['lock']).to.exist;
      expect(converted['CONTRIBUTOR']['unlock']).to.exist;
      expect(converted['COLLABORATOR']['unlock']).to.exist;
      expect(converted['CONSUMER']['approve']).to.exist;
    });
  });

  describe('edit', function () {
    it('should change edit mode to true', () => {
      expect(roleActionsTable.editMode).to.be.undefined;
      roleActionsTable.edit();
      expect(roleActionsTable.editMode).to.be.true;
    });
  });

  describe('cancelEdit', function () {
    it('should change edit mode to false and empty the new role actions object', () => {
      roleActionsTable.editMode = true;
      roleActionsTable.cancelEdit();
      expect(roleActionsTable.editMode).to.be.false;
    });
  });

  describe('save', function () {
    it('should not make save request if there are no new role actions', () => {
      roleActionsTable.editMode = true;
      roleActionsTable.save();
      expect(roleManagementService.saveRoleActions.called).to.be.false;
      expect(roleActionsTable.editMode).to.be.false;
    });

    it('should cancel edit mode after successful save', () => {
      roleActionsTable.roleActionsModel = {
        'CONSUMER': {
          'download': {
            'enabled': true,
            'filters': []
          }
        }
      };
      roleActionsTable.editMode = true;

      roleActionsTable.save();
      expect(roleManagementService.saveRoleActions.calledOnce).to.be.true;
      expect(roleActionsTable.editMode).to.be.false;
    });
  });

  describe('extractChanges', function () {
    it('should return empty array if there are no new role actions', () => {
      expect(roleActionsTable.extractChanges()).to.deep.equal([]);
    });

    it('should correctly extract new role actions as request array', () => {
      let expected = [{
        'action': 'print',
        'role': 'CONSUMER',
        'enabled': true,
        'filters': []
      }, {
        'action': 'export',
        'role': 'CONSUMER',
        'enabled': false,
        'filters': ['ASSIGNEE']
      }, {
        'action': 'move',
        'role': 'CONTRIBUTOR',
        'enabled': true,
        'filters': ['CREATEDBY']
      }];
      roleActionsTable.roleActionsModel = {
        'CONSUMER': {
          'print': expected[0],
          'export': expected[1]
        },
        'CONTRIBUTOR': {
          'move': expected[2]
        }
      };

      expect(roleActionsTable.extractChanges()).to.deep.equal(expected);
    });
  });

  describe('hasChanges', function () {
    it('should return false if no new role action created', () => {
      roleActionsTable.roleActionsModel = {
        'CONTRIBUTOR': {
          'unlock': {
            'enabled': true,
            'filters': ['CREATEDBY']
          }
        }
      };

      expect(roleActionsTable.hasChanges('CONTRIBUTOR', 'unlock')).to.be.false;
    });

    it('should return true if new role action created', () => {
      roleActionsTable.roleActionsModel = {
        'MANAGER': {
          'move': {
            'enabled': true,
            'filters': []
          }
        }
      };

      expect(roleActionsTable.hasChanges('MANAGER', 'move')).to.be.true;
    });

    it('should return true when only the filter is changed for existing role action', () => {
      roleActionsTable.roleActionsModel = {
        'CONTRIBUTOR': {
          'unlock': {
            'enabled': true,
            'filters': []
          }
        },
        'CONSUMER': {
          'approve': {
            'enabled': false,
            'filters': ['ASSIGNEE']
          }
        }
      };

      expect(roleActionsTable.hasChanges('CONTRIBUTOR', 'unlock')).to.be.true;
      expect(roleActionsTable.hasChanges('CONSUMER', 'approve')).to.be.true;
    });

    it('should return true when a filter is appended to the existing ones', () => {
      roleActionsTable.roleActionsModel = {
        'CONTRIBUTOR': {
          'unlock': {
            'enabled': true,
            'filters': ['CREATEDBY', 'ASSIGNEE']
          }
        }
      };

      expect(roleActionsTable.hasChanges('CONTRIBUTOR', 'unlock')).to.be.true;
    });

    it('should return true when only the enabled status is changed for existing role action', () => {
      roleActionsTable.roleActionsModel = {
        'CONTRIBUTOR': {
          'unlock': {
            'enabled': false,
            'filters': ['CREATEDBY']
          }
        },
        'CONSUMER': {
          'approve': {
            'enabled': true,
            'filters': []
          }
        }
      };

      expect(roleActionsTable.hasChanges('CONTRIBUTOR', 'unlock')).to.be.true;
      expect(roleActionsTable.hasChanges('CONSUMER', 'approve')).to.be.true;
    });

    it('should return true when all fields are changed for existing role action', () => {
      roleActionsTable.roleActionsModel = {
        'CONTRIBUTOR': {
          'unlock': {
            'enabled': false,
            'filters': ['ASSIGNEE']
          }
        }
      };

      expect(roleActionsTable.hasChanges('CONTRIBUTOR', 'unlock')).to.be.true;
    });
  });

  describe('activateFilter', function () {
    it('should do nothing if not in edit mode', () => {
      roleActionsTable.activateFilter('CONSUMER', 'move');
      expect(roleActionsTable.activatedFilters).to.deep.equal({});
    });

    it('should do nothing if a filter for role action is already created', () => {
      roleActionsTable.activatedFilters = {
        'CONSUMERmove': true
      };
      let expected = {
        'CONSUMERmove': true
      };
      roleActionsTable.editMode = true;

      roleActionsTable.activateFilter('CONSUMER', 'move');
      expect(roleActionsTable.activatedFilters).to.deep.equal(expected);
    });

    it('should mark filter enabled for role action', () => {
      let expected = {
        'CONSUMERmove': true
      };
      roleActionsTable.editMode = true;

      roleActionsTable.activateFilter('CONSUMER', 'move');
      expect(roleActionsTable.activatedFilters).to.deep.equal(expected);
    });
  });

  describe('filterActivated', function () {
    it('should return false if there is no filter for role action', () => {
      expect(roleActionsTable.filterActivated('CONSUMER', 'move')).to.be.false;
    });

    it('should return true when there is activated filter for role action', () => {
      roleActionsTable.activatedFilters = {
        'CONSUMERdownload': true
      };
      expect(roleActionsTable.filterActivated('CONSUMER', 'download')).to.be.true;
    });
  });

  describe('isRoleActiveForAction', function () {
    it('should return false if there is no action for role', () => {
      expect(roleActionsTable.isRoleActiveForAction('CONTRIBUTOR', 'edit')).to.be.false;
    });

    it('should return true if there is action for role and its enabled', () => {
      roleActionsTable.roleActions = {
        'CONTRIBUTOR': {
          'unlock': {
            enabled: true
          }
        }
      };

      expect(roleActionsTable.isRoleActiveForAction('CONTRIBUTOR', 'unlock')).to.be.true;
    });

    it('should return false if there is action for role and its not enabled', () => {
      roleActionsTable.roleActions = {
        'CONTRIBUTOR': {
          'unlock': {
            enabled: false
          }
        }
      };

      expect(roleActionsTable.isRoleActiveForAction('CONTRIBUTOR', 'unlock')).to.be.false;
    });
  });

  describe('getFilters', function () {
    it('should return empty array of filters if no existing role action', () => {
      expect(roleActionsTable.getFilters('COLLABORATOR', 'print')).to.deep.equal([]);

      roleActionsTable.roleActions = {
        'COLLABORATOR': {
          'export': {}
        }
      };

      expect(roleActionsTable.getFilters('COLLABORATOR', 'print')).to.deep.equal([]);
    });

    it('should return array of filters if there is existing role action', () => {
      expect(roleActionsTable.getFilters('CONTRIBUTOR', 'unlock')).to.deep.equal(['CREATEDBY']);
    });
  });

});