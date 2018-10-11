import _ from 'lodash';
import {SELECT_OBJECT_CURRENT} from 'idoc/widget/object-selector/object-selector';
import {MULTIPLE_SELECTION} from 'search/search-selection-modes';
import {RecentActivitiesConfig} from 'idoc/widget/recent-activities/config';

describe('RecentActivitiesConfig', function() {

  var recentActivitiesConfig;
  beforeEach(function() {
    recentActivitiesConfig = new RecentActivitiesConfig(mockTranslateService());
  });

  afterEach(() => {
    RecentActivitiesConfig.prototype.config = undefined;
  });

  it('should handle criteria change', function() {
    let time = new Date().getTime();
    recentActivitiesConfig.config.onObjectSelectorChanged({searchCriteria: time});
    expect(recentActivitiesConfig.config.criteria).to.deep.eq(time);

    recentActivitiesConfig.config.onObjectSelectorChanged({});
    expect(recentActivitiesConfig.config.criteria).to.be.undefined;
  });

  it('should create default config', function() {
    expect(_.isFunction(recentActivitiesConfig.config.onObjectSelectorChanged)).to.be.true;
    expect(_.omit(recentActivitiesConfig.config, 'onObjectSelectorChanged')).to.deep.eq({
      selection: MULTIPLE_SELECTION,
      selectObjectMode: SELECT_OBJECT_CURRENT,
      showIncludeCurrent: true,
      pageSize: 10,
      triggerSearch: false
    });
  });

  it('should create default tabs config, with select-object as active tab', function() {
    expect(recentActivitiesConfig.tabsConfig.activeTab).to.eq('select-object');
    expect(recentActivitiesConfig.tabsConfig.tabs.length).to.eq(2);
    expect(recentActivitiesConfig.tabsConfig.tabs[0].id).to.eq('select-object');
    expect(recentActivitiesConfig.tabsConfig.tabs[1].id).to.eq('display-options');
  });

  it('should create page size select config', function() {
    expect(recentActivitiesConfig.pageSizeSelectConfig.data.length).to.eq(6);
    expect(recentActivitiesConfig.pageSizeSelectConfig.data[0].id).to.eq('all');
    expect(recentActivitiesConfig.pageSizeSelectConfig.data[0].text).to.eq('ALL');
    expect(recentActivitiesConfig.pageSizeSelectConfig.data[1].id).to.eq(5);
    expect(recentActivitiesConfig.pageSizeSelectConfig.data[2].id).to.eq(10);
    expect(recentActivitiesConfig.pageSizeSelectConfig.data[3].id).to.eq(20);
    expect(recentActivitiesConfig.pageSizeSelectConfig.data[4].id).to.eq(50);
    expect(recentActivitiesConfig.pageSizeSelectConfig.data[5].id).to.eq(100);
  });

  it('should configure to trigger a search if criteria is provided', () => {
    RecentActivitiesConfig.prototype.config = {
      criteria: {
        condition: 'AND_THEN',
        rules: []
      }
    };
    recentActivitiesConfig = new RecentActivitiesConfig(mockTranslateService());
    expect(recentActivitiesConfig.config.triggerSearch).to.be.true;
  });

  function mockTranslateService() {
    return {
      translate: function() {
        return {
          then: function(cb) {
            cb('ALL');
          }
        };
      }
    };
  }

});
