import {MomentAdapter} from 'adapters/moment-adapter';
import {Versions} from 'idoc/system-tabs/versions/versions';
import {PromiseStub} from 'test/promise-stub';

describe('Versions', () => {
  let versions;
  beforeEach(() => {
    Versions.prototype.context = {
      currentObjectId: 'emf:111222',
      getCurrentObjectId: function() {
        return this.currentObjectId;
      }
    };

    let instanceRestService = {
      getVersions: () => {
        let versions = [
          {
            id: 'emf:123456',
            properties: {
              modifiedOn: '2016-09-23T21:00:00.000Z'
            }
          },
          {
            id: 'emf:999888',
            properties: {
              modifiedOn: '2016-09-26T21:00:00.000Z'
            }
          }
        ];
        return PromiseStub.resolve({
          data: {
            versionsCount: 2,
            versions: versions
          }
        });
      }
    };
    let configuration = {
      get: () => {
        return 25
      }
    };

    let translateService = {
      translateInstant: function (label) {
        return label;
      }
    };
    versions = new Versions(instanceRestService, configuration, new MomentAdapter(), translateService);
  });

  it('constructor must create versions array', () => {
    expect(versions.versions).to.have.length(2);
    expect(versions.versions[0]).to.have.property('id', 'emf:123456');
    expect(versions.versions[1]).to.have.property('id', 'emf:999888');
  });

  it('constructor must initialize pagination configuration', () => {
    let expectedPaginationConfig = {
      'total': 2,
      'showFirstLastButtons': true,
      'page': 1,
      'pageSize': 25,
      'pageRotationStep': 2
    };
    expect(versions.paginationConfig).to.eql(expectedPaginationConfig);
  });

  it('getCreatedOn should return formatted date', () => {
    versions.datePattern = 'DD.MM.YY HH:mm';
    let version = {
      properties: {
        modifiedOn: new Date()
      }
    };
    expect(versions.getCreatedOn(version)).to.equals(versions.momentAdapter.format(version.properties.modifiedOn, versions.datePattern));
  });

  it('onPageChanged should load new page of versions', () => {
    let pageNumber = 3;
    let loadVersionsSpy = sinon.spy(versions, 'loadVersions');
    versions.onPageChanged({pageNumber: pageNumber});
    expect(loadVersionsSpy.callCount).to.equal(1);
    expect(loadVersionsSpy.getCall(0).args[0]).to.equal((pageNumber - 1) * versions.pageSize);
  });
});
