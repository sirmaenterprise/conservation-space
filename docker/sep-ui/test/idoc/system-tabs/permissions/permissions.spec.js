import {Permissions, NO_PERMISSION, NO_PERMISSIONS_LABEL} from 'idoc/system-tabs/permissions/permissions';
import {PromiseStub} from 'test/promise-stub';

describe('Permissions', () => {
  let permissions = {};

  Permissions.prototype.context = {
    currentObjectId: 'sadf',
    getCurrentObjectId: function() {
      return this.currentObjectId;
    },
    getCurrentObject: ()=> {
      return Promise.resolve({instanceType: 'classinstance'});
    }
  };

  let permissionsService = {
    getRoles: ()=> {
      return Promise.resolve({data: []});
    },
    load: ()=> {
      return Promise.resolve({
        data: {
          editAllowed: true,
          permissions: []
        }
      });
    },
    save: ()=> {

    }
  };

  let stateProvider = {
    getStateParam: sinon.spy()
  };

  let iconsService = {
    getIconForInstance: sinon.spy()
  };

  let eventbus = {
    publish: sinon.spy()
  };


  beforeEach(() => {
    eventbus.publish.reset();
    permissions = new Permissions(permissionsService, {}, {}, {}, eventbus, iconsService, stateProvider);
  });

  it('should get the user icon', ()=> {
    permissions.getAuthorityIcon('user');
    expect(permissions.iconsService.getIconForInstance.callCount).to.equal(1);
    expect(permissions.iconsService.getIconForInstance.args[0][0]).to.equal('user');
  });

  it('should get the group icon', ()=> {
    permissions.getAuthorityIcon('group');
    expect(permissions.iconsService.getIconForInstance.callCount).to.equal(2);
    expect(permissions.iconsService.getIconForInstance.args[1][0]).to.equal('group');
  });

  it('should get the roles label by its value', ()=> {
    permissions.roles = [{value: 'value', label: 'label'}];
    var value = permissions.getRoleValueByLabel('label');
    expect(value).to.equal('value');
  });

  it('should test the correct calculations of the permissions', ()=> {
    var authority = {
      newSpecialPermission: 'newSpecial'
    };
    var authorityActive = permissions.calculateActive(authority);
    expect(authorityActive).to.equal('newSpecial');
  });

  it('should hide authority with no active permissions', ()=> {
    permissions.addAuthorityToHidden = sinon.spy();
    permissions.authorities = [{
      isManager: false,
      library: '-'
    }];
    permissions.sort = sinon.spy();
    permissions.showAuthorities();
    expect(permissions.addAuthorityToHidden.callCount).to.equal(1);
    expect(permissions.authorities[0].show).to.be.false;
  });

  it('should show authority with active library permissions', ()=> {
    permissions.removeAuthorityFromHidden = sinon.spy();
    permissions.authorities = [{
      isManager: false,
      library: 'library'
    }];
    permissions.inheritedLibraryPermissions = true;
    permissions.sort = sinon.spy();
    permissions.showAuthorities();
    expect(permissions.removeAuthorityFromHidden.callCount).to.equal(1);
    expect(permissions.authorities[0].show).to.be.true;
  });

  it('should sort the permissions alphabetically by their names', ()=> {
    permissions.authorities = [{
      name: 'system',
      isManager: false,
      library: 'library'
    }, {
      name: 'name2',
      isManager: false,
      library: 'library'
    }, {
      name: 'name1',
      isManager: false,
      library: 'library'
    }];

    permissions.sort();
    expect(permissions.authorities[0].name).to.equal('name1');
    expect(permissions.authorities[2].name).to.equal('system');
  });

  it('should reverse the permissions', ()=> {
    permissions.authorities = [{
      name: 'system',
      isManager: false,
      library: 'library'
    }, {
      name: 'name2',
      isManager: false,
      library: 'library'
    }, {
      name: 'name1',
      isManager: false,
      library: 'library'
    }];
    permissions.reverse = true;
    permissions.sort();
    expect(permissions.authorities[0].name).to.equal('system');
    expect(permissions.authorities[2].name).to.equal('name1');
  });

  it('should save the edited permissions', (done)=> {
    permissions.authorities = [{
      id: 'authorityId',
      newSpecial: 'newSpecial'
    }];
    permissions.getRoleValueByLabel = ()=> {
      return 'newSpecial';
    };
    permissions.showAuthorities = sinon.spy();
    permissions.extractPermissions = sinon.spy();
    permissions.permissionsService.save = function () {
      return Promise.resolve({data: {editAllowed: true}});
    };
    permissions.savePermissions().then(()=> {
      expect(permissions.extractPermissions.callCount).to.equal(1);
      expect(permissions.editAllowed).to.be.true;
      expect(eventbus.publish.calledOnce).to.be.true;
      expect(eventbus.publish.getCall(0).args[0].getData()).to.be.true;
      done();
    }).catch(()=> {
      done(new Error('Should save the permissions.'));
    });
  });

  it('should hide authority from authorities select', ()=> {
    var response = {
      data: {
        items: [{id: 'first'}, {id: 'second'}]
      }
    };
    permissions.authoritiesMap.set('first', true);
    sinon.stub(permissions, 'transformAuthorityFromRest').returns({id: 'first'});
    var hidden = permissions.hideAuthorities(response);
    expect(permissions.transformAuthorityFromRest.callCount).to.equal(1);
    expect(hidden.length).to.equal(1);
  });

  it('should catch the error and show a notification', (done)=> {
    permissions.authorities = [];
    permissions.newAuthorities = [];
    permissions.notificationService = {
      error: sinon.spy(),
      remove: sinon.spy()
    };

    permissions.permissionsService.save = function () {
      return Promise.reject();
    };
    permissions.translateService = {};
    permissions.translateService.translate = function () {
      return Promise.resolve('data');
    };
    permissions.savePermissions().then(()=> {
      expect(permissions.editMode).to.equal(true);
      expect(eventbus.publish.calledOnce).to.be.false;
      expect(permissions.notificationService.error.callCount).to.equal(1);
      expect(permissions.notificationService.remove.callCount).to.equal(1);
      done();
    }).catch(()=> {
      done(new Error('Should save the permissions.'));
    });

  });

  it('should not hide authorities that had inherited parent permissions after disabled inheritance', ()=> {
    let objectPermissions = [{
      id: 'user1',
      calculated: 'MANAGER',
      inherited: 'MANAGER',
      isManager: true,
      label: 'user1'
    }, {
      id: 'admin',
      calculated: 'MANAGER',
      isManager: true,
      label: 'admin'
    }];
    let response = {
      data: {
        items: [{
          'id': 'group1'
        }, {
          'id': 'user1'
        }, {
          'id': 'user2'
        }, {
          'id': 'admin'
        }]
      }
    };
    permissions.authorities = [{
      name: 'user2',
      isManager: false
    }];
    permissions.authoritiesMap.set('user2', true);
    permissionsService.load = () => {
      return PromiseStub.resolve({
        data: {
          editAllowed: true,
          inheritedPermissionsEnabled: false,
          inheritedLibraryPermissions: false,
          permissions: objectPermissions
        }
      });
    };
    permissions.roles = [];
    let expected = [{
      id: 'group1',
      text: undefined,
      type: undefined,
      value: undefined
    }, {
      id: 'user2',
      text: undefined,
      type: undefined,
      value: undefined
    }];

    permissions.loadAuthorities();
    expect(permissions.hideAuthorities(response)).to.deep.equal(expected);
  });

});