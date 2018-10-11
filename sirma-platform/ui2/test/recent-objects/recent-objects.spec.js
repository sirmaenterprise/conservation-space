import {RecentObjects} from 'recent-objects/recent-objects';

describe('RecentObjects', function () {

  it('should have recent object list properly configured', function () {
    var recentObjects = new RecentObjects();
    expect(recentObjects.recentObjectsListConfig).to.deep.equal({
      renderMenu: true,
      placeholder: 'recent-objects'
    });
  });
});