import {RecentObjects} from 'recent-objects/recent-objects';

describe('RecentObjects', function() {

  describe('ngOnInit()', function() {

    it('should clear breadcrumb', function() {
      var recentObjects = new RecentObjects({clear: sinon.spy()});
      recentObjects.ngOnInit();
      expect(recentObjects.breadcrumbEntryManager.clear.calledOnce).to.be.true;
    });
  });
});