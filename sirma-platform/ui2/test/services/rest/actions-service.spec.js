import {ActionsService} from 'services/rest/actions-service';
import {HEADER_V2_JSON, BASE_PATH} from 'services/rest-client';
import {LOCK, UNLOCK} from 'idoc/actions/action-constants';

describe('ActionsService', () => {

  let restClient;
  let actionsService;

  beforeEach(() => {
    restClient = {
      get: sinon.spy((response) => {
        return Promise.resolve(response);
      }),
      post: sinon.spy(),
      patch: sinon.spy(),
      getUrl: (url)=>{
        return BASE_PATH + url;
      }
    };
    restClient.get.reset();
    restClient.post.reset();
    restClient.patch.reset();
    var promiseAdapter = {
      reject: sinon.spy((msg) => {
        return Promise.reject(msg);
      })
    };
    var translateService = {
      translateInstant: sinon.spy(() => {
        return 'translated';
      })
    };
    let authenticationService = {
      getToken: sinon.spy(() => {
        return 'jwtToken';
      })
    };

    let windowAdapter = {
      location: {
        origin: 'http://localhost:5000'
      }
    };

    actionsService = new ActionsService(restClient, promiseAdapter, translateService, authenticationService, windowAdapter);
  });

  it('getActions() should invoke rest client with proper arguments', () => {
    let params = {
      'context-id': 'emf:999999',
      placeholder: 'placeholder',
      path: ['emf:123456', 'emf:999999']
    };

    actionsService.getActions('emf:123456', params);
    expect(restClient.get.calledWithExactly('/instances/emf:123456/actions', {
      params: params,
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('executeTransition() should call rest client with proper arguments to execute the transition', () => {
    let data = {
      operation: 'transition',
      userOperation: 'approve',
      contextPath: ['emf:123456', 'emf:999999'],
      targetInstance: {
        definitionId: 'emf:123456',
        properties: {}
      }
    };
    actionsService.executeTransition('emf:123456', data);
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/transition', data, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('addIcons() should call the rest client with proper arguments', ()=> {
    var icons = [{size: 16, image: 'firstIcon'}];
    actionsService.addIcons('emf:1', icons);
    expect(restClient.post.called).to.be.true;
    expect(restClient.post.args[0][0]).to.equal('/instances/emf:1/actions/addicons');
    expect(restClient.post.args[0][1].icons).to.equal(icons);
  });

  it('download() should call rest client with proper arguments in order to call download rest', () => {
    actionsService.download('emf:123456');
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/download', null, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('move() should call rest client with proper base arguments', () => {
    actionsService.move('emf:123456');
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/move', undefined, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('move() should call rest client with proper additional arguments', () => {
    actionsService.move('emf:123456', undefined, {
      skipInterceptor: true
    });
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/move', undefined, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      },
      skipInterceptor: true
    })).to.be.true;
  });

  it('uploadNewVersion() should call rest client with proper arguments', () => {
    actionsService.createOrUpdate('emf:123456');
    expect(restClient.patch.calledWithExactly('/instances/emf:123456/actions/createOrUpdate', undefined, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('lock() should call rest client with proper arguments', () => {
    let data = {
      operation: LOCK,
      userOperation: LOCK,
      contextPath: ['emf:123456', 'emf:999999'],
      targetInstance: {
        definitionId: 'emf:123456',
        properties: {}
      }
    };
    actionsService.lock('emf:123456', data);
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/lock', data, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('unlock() should call rest client with proper arguments', () => {
    let data = {
      operation: UNLOCK,
      userOperation: UNLOCK,
      contextPath: ['emf:123456', 'emf:999999'],
      targetInstance: {
        definitionId: 'emf:123456',
        properties: {}
      }
    };
    actionsService.unlock('emf:123456', data);
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/unlock', data, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('exportPDF should perform post with proper arguments', () => {
    restClient.post = sinon.spy();
    let id = 'emf:123456';
    let data = {
      operation: 'exportPDF'
    };
    let expected = {operation: 'exportPDF', url: 'http://localhost:5000/#/idoc/emf:123456?mode=print&jwt=jwtToken'};
    actionsService.exportPDF(id, null, data);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/export/pdf');
    expect(restClient.post.getCall(0).args[1]).to.eql(expected);
  });

  it('exportWord should perform post with proper arguments', () => {
    restClient.post = sinon.spy();
    let id = 'emf:123456';
    let data = {
      operation: 'exportWord'
    };
    let expected = {operation: 'exportWord', url: '/#/idoc/emf:123456?mode=print&jwt=jwtToken&tab=tab2'};
    actionsService.exportWord(id, 'tab2', data);
    expect(restClient.post.calledOnce);
    expect(restClient.post.getCall(0).args[0]).to.equal('/instances/emf:123456/actions/export-word');
    expect(restClient.post.getCall(0).args[1]).to.eql(expected);
  });

  describe('#addThumbnail()', () => {
    it('should not make http request if there is no request provided', (done) => {
      actionsService.addThumbnail().then(done).catch((err) => {
        expect(err).to.exist;
        done();
      });
    });

    it('should not make http request if there are no IDs provided', (done) => {
      var request = {};
      actionsService.addThumbnail(request).then(done).catch((err) => {
        expect(err).to.exist;
        done();
      });
    });

    it('should not make http request if there is no thumbnail ID', (done) => {
      var request = {instanceId: ''};
      actionsService.addThumbnail(request).then(done).catch((err) => {
        expect(err).to.exist;
        done();
      });
    });

    it('should not make http request if there is no instance ID', (done) => {
      var request = {thumbnailObjectId: ''};
      actionsService.addThumbnail(request).then(done).catch((err) => {
        expect(err).to.exist;
        done();
      });
    });

    it('should make http request with the correct data', () => {
      var request = {
        instanceId: 'instance-id',
        thumbnailObjectId: 'document-id'
      };

      actionsService.addThumbnail(request);

      var expected = {
        thumbnailObjectId: 'document-id'
      };
      expect(restClient.post.called).to.be.true;
      expect(restClient.post.getCall(0).args[0]).to.equal('/instances/instance-id/actions/thumbnail');
      expect(restClient.post.getCall(0).args[1]).to.deep.equal(expected);
    });

    it('should make http request with the correct headers', () => {
      var request = {
        instanceId: 'instance-id',
        thumbnailObjectId: 'document-id'
      };

      actionsService.addThumbnail(request);

      var expectedHeaders = {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      };
      expect(restClient.post.getCall(0).args[2]).to.exist;
      expect(restClient.post.getCall(0).args[2].headers).to.exist;
      expect(restClient.post.getCall(0).args[2].headers).to.deep.equal(expectedHeaders);
    });
  });

  it('addRelation() should call rest client with proper arguments', () => {
    let data = {
      "userOperation": "addRelation",
      "removeExisting": false,
      "relations": {
        "emf:relation": [
          "emf:123456",
          "emf:123457"
        ]
      }
    };
    actionsService.addRelation('emf:123455', data);
    expect(restClient.post.calledWithExactly('/instances/emf:123455/actions/addRelation', data, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('revertVersion() should call the rest service with proper arguments', ()=> {
    let payload = {userOperation: 'revertVersion'};
    actionsService.revertVersion('emf:123456', payload);
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/revert-version', payload, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('activateTemplate() should call the rest service with proper arguments', ()=> {
    let payload = {userOperation: 'activateTemplate'};
    actionsService.activateTemplate('emf:123456', payload);
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/activate-template', payload, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });

  it('should return properly combined edit offline check out rest url', ()=> {
    let expected = '/remote/api/instances/emf:123456/actions/edit-offline-check-out?jwt=jwtToken';
    expect(actionsService.downloadForEditOffline('emf:123456')).to.equal(expected);
  });
  
  it('should call the actions endpoint to delete and instance providing the user operation', () => {
    let payload = { userOperation: 'delete' };
    actionsService.delete('emf:123456', payload.userOperation);
    
    expect(restClient.post.calledWithExactly('/instances/emf:123456/actions/delete', payload, {
      headers: {
        'Accept': HEADER_V2_JSON,
        'Content-Type': HEADER_V2_JSON
      }
    })).to.be.true;
  });
});