import {stub} from 'test/test-utils';
import {MomentAdapter} from 'adapters/moment-adapter';
import {Versions} from 'idoc/system-tabs/versions/versions';
import {PromiseStub} from 'test/promise-stub';
import {Configuration} from 'common/application-config';
import {TranslateService} from 'services/i18n/translate-service';
import {AuthenticationService} from 'services/security/authentication-service';
import {NotificationService} from 'services/notification/notification-service';
import {InstanceRestService} from 'services/rest/instance-service';

describe('Versions', () => {

  const COMPACT_HEADER = 'compact header of user';
  const USER_ID = 'emf:user-id';

  let versions;
  let instanceRestService;
  let configuration;
  let translateService;
  let authenticationService;
  let notificationService;

  beforeEach(()=> {
    Versions.prototype.context = {};
    instanceRestService = decorateInstanceRestService(stub(InstanceRestService));
    configuration = decorateConfiguration(stub(Configuration));
    translateService = stub(TranslateService);
    authenticationService = stub(AuthenticationService);
    let $scope = decorateScope();
    notificationService = stub(NotificationService);
    versions = new Versions(instanceRestService, configuration, new MomentAdapter(), translateService, authenticationService, $scope, notificationService);
  });

  describe('ngOnInit', ()=> {

    it('should load versions', () => {
      decorateVersionsInstance(versions, true, 'documentinstance');
      expect(versions.versions).to.have.length(2);
      expect(versions.versions[0]).to.have.property('id', 'emf:123456');
      expect(versions.versions[1]).to.have.property('id', 'emf:999888');
    });

    it('should initialize pagination configuration', () => {
      decorateVersionsInstance(versions, true, 'documentinstance');
      let expectedPaginationConfig = {
        'total': 2,
        'showFirstLastButtons': true,
        'page': 1,
        'pageSize': 25,
        'pageRotationStep': 2
      };
      expect(versions.paginationConfig).to.eql(expectedPaginationConfig);
    });

    it('onPageChanged should load new page of versions', () => {
      decorateVersionsInstance(versions, true, 'documentinstance');
      let pageNumber = 3;
      let loadVersionsSpy = sinon.spy(versions, 'loadVersions');
      versions.onPageChanged({pageNumber});
      expect(loadVersionsSpy.callCount).to.equal(1);
      expect(loadVersionsSpy.getCall(0).args[0]).to.equal((pageNumber - 1) * versions.pageSize);
    });

  });

  describe('checkIfComparable', ()=> {

    it('should not enable compare when there is not preview for the instance', (done)=> {
      decorateVersionsInstance(versions, false, 'documentinstance');
      versions.checkIfComparable().then((comparable)=> {
        expect(comparable).to.be.false;
        done();
      }).catch(done);
    });

    it('should enable compare when a preview is created for the current instance', (done)=> {
      decorateVersionsInstance(versions, true, 'documentinstance');

      versions.checkIfComparable().then((comparable)=> {
        expect(comparable).to.be.true;
        done();
      });
    });

    it('should not enable compare when a preview is created for the current instance of type image', (done)=> {
      decorateVersionsInstance(versions, true, 'image');
      versions.checkIfComparable().then((comparable)=> {
        expect(comparable).to.be.false;
        done();
      });
    });

    it('should not enable compare when a preview is created for the current instance of type audio', (done)=> {
      decorateVersionsInstance(versions, true, 'audio');
      versions.checkIfComparable().then((comparable)=> {
        expect(comparable).to.be.false;
        done();
      });
    });

    it('should not enable comparison when the version is executable', (done)=> {
      decorateVersionsInstance(versions, true, 'executable');
      versions.checkIfComparable().then((comparable)=> {
        expect(comparable).to.be.false;
        done();
      });
    });

    it('should not enable comparison when the verion is executable(octet-stream)', (done)=> {
      decorateVersionsInstance(versions, true, 'octet-stream');
      versions.checkIfComparable().then((comparable)=> {
        expect(comparable).to.be.false;
        done();
      });
    });
  });

  describe('checkIfComparisonIsEnabled', ()=> {
    it('should comparison enabled if two versions are selected', ()=> {
      let versionOne = 'emf:first';
      let versionTwo = 'emf:second';
      decorateVersionsInstance(versions, true, 'documentinstance');
      versions.selectedVersions.push({'id': versionOne});

      versions.handleSelection({id: versionTwo});

      expect(versions.isComparisonEnabled()).to.be.true;
    });

    it('comparison should be disabled if one versions are selected', ()=> {
      let versionId = 'emf:first';
      decorateVersionsInstance(versions, true, 'documentinstance');
      versions.handleSelection({id: versionId});

      expect(versions.isComparisonEnabled()).to.be.false;
    });

    it('comparison should should be disabled if first version is deselected', ()=> {
      let versionOneId = 'emf:first';
      let versionTwoId = 'emf:second';
      decorateVersionsInstance(versions, true, 'documentinstance');
      versions.selectedVersions.push({'id': versionOneId});
      versions.selectedVersions.push({'id': versionTwoId});

      versions.handleSelection({id: versionOneId, selected: true});

      expect(versions.isComparisonEnabled()).to.be.false;
    });

    it('comparison should be disabled if second version is deselected', ()=> {
      let versionOneId = 'emf:first';
      let versionTwoId = 'emf:second';
      decorateVersionsInstance(versions, true, 'documentinstance');
      versions.selectedVersions.push({'id': versionOneId});
      versions.selectedVersions.push({'id': versionTwoId});

      versions.handleSelection({id: versionTwoId, selected: true});

      expect(versions.isComparisonEnabled()).to.be.false;
    });
  });

  describe('versionsCheckboxesSelection', ()=> {
    it('checkbox of version should be enabled if first checkbox is selected', ()=> {
      let version = {id: 'emf:id', selected: false, config: {comparable: true}};
      decorateVersionsInstance(versions, true, 'documentinstance');
      expect(versions.checkDisabled(version)).to.be.false;
    });

    it('checkbox of version should be enabled if second checkbox is selected', ()=> {
      let versionOneId = 'emf:first';

      let version = {id: 'emf:second', selected: false, config: {comparable: true}};

      decorateVersionsInstance(versions, true, 'documentinstance');
      versions.handleSelection({id: versionOneId, selected: false});

      expect(versions.checkDisabled(version)).to.be.false;
    });

    it('checkbox of version should be disabled for instances of type video', ()=> {
      let version = {
        id: 'emf:id', selected: false, config: {comparable: false}
      };
      decorateVersionsInstance(versions, true, 'video');

      expect(versions.checkDisabled(version)).to.be.true;
    });

    it('checkbox of version should be disable if third checkbox is selected', ()=> {
      let versionOneId = 'emf:first';
      let versionTwoId = 'emf:second';
      decorateVersionsInstance(versions, true, 'documentinstance');
      versions.handleSelection({id: versionOneId, selected: false});
      versions.handleSelection({id: versionTwoId, selected: false});
      let version = {
        id: 'emf:third',
        selected: true,
        config: {comparable: true}
      };
      expect(versions.checkDisabled(version)).to.be.false;
    });
  });

  it('should show warning when the compare fails', (done)=> {
    decorateVersionsInstance(versions, true, 'documentinstance');
    versions.handleSelection({id: 'asdf', selected: false});
    versions.handleSelection({id: 'afsd', selected: false});
    instanceRestService.compareVersions.returns(PromiseStub.reject());
    let spyDecorateDownloadUri = sinon.spy(versions, 'decorateDownloadURI');

    versions.compareVersions().then(()=> {
      expect(versions.notificationService.remove.called).to.be.true;
      expect(versions.notificationService.warning.called).to.be.true;
      expect(spyDecorateDownloadUri.called).to.be.false;

      spyDecorateDownloadUri.restore();
      done();
    });
  });

  it('should call compare versions rest', ()=> {
    let versionOneId = 'emf:first';
    let versionTwoId = 'emf:second';
    let expectedParameters = ['emf:111222', versionOneId, versionTwoId];
    decorateVersionsInstance(versions, true, 'documentinstance');
    versions.selectedVersions = [{id: versionOneId}, {id: versionTwoId}];

    versions.compareVersions();

    expect(instanceRestService.compareVersions.called).to.be.true;
    expect(instanceRestService.compareVersions.args[0]).to.deep.equal(expectedParameters);
  });

  it('should load compact header of person created version', () => {
    let version = {properties: {modifiedBy: {results: [USER_ID]}}};
    versions.versions = [version];


    expect(version.versionModifiedByHeader === undefined).to.be.true;
    versions.loadModifiedByHeader(version);
    expect(version.versionModifiedByHeader).to.equal(COMPACT_HEADER);
  });

  describe('populateVersionsConfiguration', () => {

    const NOT_COMPARABLE_TOOLTIP = 'not comparable tooltip';

    describe('version for comparable type', () => {
      it('should not set not comparable tooltip when mimetype is comparable', () => {
        let version = setVersion(versions, true, true);

        versions.populateVersionsConfiguration();

        expect(version.config.notComparableTooltip === undefined).to.be.true;
        expect(version.config.comparable).to.be.true;
      });

      it('should set not comparable tooltip when mimetype is not comparable', () => {
        let version = setVersion(versions, true, false);

        versions.populateVersionsConfiguration();

        expect(version.config.notComparableTooltip).to.be.equal(NOT_COMPARABLE_TOOLTIP);
        expect(version.config.comparable === undefined).to.be.true;
      });
    });

    describe('version for not comparable type', () => {
      it('should set not comparable tooltip  when mimetype is comparable', () => {
        let version = setVersion(versions, false, true);

        versions.populateVersionsConfiguration();

        expect(version.config.notComparableTooltip).to.be.equal(NOT_COMPARABLE_TOOLTIP);
        expect(version.config.comparable === undefined).to.be.true;
      });

      it('should set not comparable tooltip  when mimetype is not comparable', () => {
        let version = setVersion(versions, false, false);

        versions.populateVersionsConfiguration();

        expect(version.config.notComparableTooltip).to.be.equal(NOT_COMPARABLE_TOOLTIP);
        expect(version.config.comparable === undefined).to.be.true;
      });
    });

    function setVersion(versions, comparableType, comparableMimetype) {
      let version = comparableMimetype ? createComparableTypeVersion() : createNonComparableTypeVersion();
      versions.comparable = comparableType;
      versions.notComparableTooltip = NOT_COMPARABLE_TOOLTIP;
      versions.versions = [version];
      return version;
    }

    function createNonComparableTypeVersion() {
      return createVersion('image');
    }

    function createComparableTypeVersion() {
      return createVersion('comparableType');
    }

    function createVersion(mimetype) {
      return {
        properties: {primaryContentMimetype: mimetype}
      };
    }
  });

  function decorateInstanceRestService(instanceRestService) {
    instanceRestService.compareVersions.returns(PromiseStub.resolve());
    let versions = [
      {
        id: 'emf:123456',
        properties: {
          modifiedOn: '2016-09-23T21:00:00.000Z',
          primaryContentMimetype: 'video',
          modifiedBy: {results: [USER_ID]}
        }
      },
      {
        id: 'emf:999888',
        properties: {
          modifiedOn: '2016-09-26T21:00:00.000Z',
          primaryContentMimetype: 'text/plain',
          modifiedBy: {results: [USER_ID]}
        }
      }
    ];
    instanceRestService.getVersions.returns(PromiseStub.resolve({
      data: {
        versionsCount: 2,
        versions
      }
    }));

    instanceRestService.loadBatch.returns(PromiseStub.resolve({
      data: [{id: USER_ID, headers: {compact_header: COMPACT_HEADER}}]
    }));
    return instanceRestService;
  }

  function decorateConfiguration(configuration) {
    configuration.get.returns(25);
    return configuration;
  }

  function decorateScope() {
    return {
      $evalAsync(callback) {
        callback();
      }
    };
  }

  function decorateVersionsInstance(versions, withPreview, instanceType) {
    versions.context = {
      currentObjectId: 'emf:111222',
      getCurrentObjectId() {
        return this.currentObjectId;
      },
      getCurrentObject() {
        return PromiseStub.resolve({
          id: 'emf:111222',
          instanceType,
          models: {
            validationModel: {
              'emf:contentId': {defaultValue: withPreview ? 'preview' : undefined}
            }
          }
        });
      }
    };
    versions.ngOnInit();
  }
});
