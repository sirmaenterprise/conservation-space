import {UserActivityEntry} from 'user/activity/user-activity-entry';
import {IdocMocks} from 'test/idoc/idoc-mocks';

describe('UserActivityEntry', () => {
  describe('getFormattedTimestamp', () => {
    let userActivityEntry, filterFuncSpy;
    before(() => {
      filterFuncSpy = sinon.spy();
      let filterMock = () => {
        return filterFuncSpy;
      };
      let momentAdapterMock = {
        format: sinon.spy()
      };
      userActivityEntry = new UserActivityEntry(filterMock, IdocMocks.mockConfiguration(), momentAdapterMock);
      userActivityEntry.datePattern = 'DD.MM.YY HH.mm';
    });

    afterEach(() => {
      filterFuncSpy.reset();
    });

    it('should use timeAgo filter if activity timestamp is less than an hour ago', () => {
      let timestamp = new Date();
      timestamp.setTime(timestamp.getTime() - 30 * 60 * 1000);
      userActivityEntry.activity = {
        timestamp
      };
      userActivityEntry.getFormattedTimestamp();
      expect(filterFuncSpy.callCount).to.equals(1);
    });

    it('should format date if activity timestamp is more than an hour ago', () => {
      let timestamp = new Date();
      timestamp.setTime(timestamp.getTime() - 90 * 60 * 1000);
      userActivityEntry.activity = {
        timestamp
      };
      userActivityEntry.getFormattedTimestamp();
      expect(filterFuncSpy.callCount).to.equals(0);
      expect(userActivityEntry.momentAdapter.format.callCount).to.equals(1);
    });
  });
});
