var RecentActivitiesSandboxPage = require('./recent-activities').RecentActivitiesSandboxPage;
var ObjectSelector = require('../object-selector/object-selector').ObjectSelector;

describe('RecentActivities', function () {
  var page;
  var config;

  beforeEach(function () {
    page = new RecentActivitiesSandboxPage();
    page.open();
  });

  describe('normal mode', () => {
    beforeEach(() => {
      config = page.insert();
    });

    it('should be configured for current object by default', function () {
      expect(config.getObjectSelector().getSelectObjectMode()).to.eventually.eq(ObjectSelector.CURRENT_OBJECT);
    });

    it('should support automatic object selection', function () {
      var selector = config.getObjectSelector();
      selector.selectObjectSelectionMode(ObjectSelector.AUTOMATICALLY);
      expect(selector.getSelectObjectMode()).to.eventually.eq(ObjectSelector.AUTOMATICALLY);
    });

    it('should support manual object selection', function () {
      var selector = config.getObjectSelector();
      selector.selectObjectSelectionMode(ObjectSelector.MANUALLY);
      expect(selector.getSelectObjectMode()).to.eventually.eq(ObjectSelector.MANUALLY);
    });

    it('should be configured for 10 results per page by default', function () {
      config.switchToDisplayOptionsTab();
      expect(config.getPageSizeConfigSelect().getSelectedValue()).to.eventually.eq('10');
    });

    it('should support configurable page size', function () {
      config.switchToDisplayOptionsTab();
      var pageSizeSelect = config.getPageSizeConfigSelect();

      pageSizeSelect.selectFromMenu(null, '5');
      expect(pageSizeSelect.getSelectedValue()).to.eventually.eq('5');

      config.save();

      var widget = page.getWidget();
      widget.waitForPagination();
      widget.waitForActivities();
      expect(widget.getItemsCount()).to.eventually.eq(5);
    });

    it('should hide pagination if all results are shown', function () {
      config.switchToDisplayOptionsTab();
      config.getPageSizeConfigSelect().selectFromMenu(null, 'all');
      config.save();

      var widget = page.getWidget();
      widget.waitStalenessOfPagination();
    });
  });

  describe('modelling mode', () => {
    beforeEach(() => {
      page.toggleModellingMode();
      config = page.insert();
    });

    it('should not show results if is in modelling mode and automatic selection is selected', () => {
      config.selectAutomatically();
      config.save();
      var widget = page.getWidget();
      expect(widget.getItemsCount()).to.eventually.equal(0);
    });
  });
});