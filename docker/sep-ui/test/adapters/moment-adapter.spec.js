import {MomentAdapter} from 'adapters/moment-adapter';

describe('MomentAdapter', () => {

  it('should format given date using provided pattern', () => {
    var moment = new MomentAdapter();
    var formatted = moment.format(new Date('2015/12/22').toISOString(), 'MMMM/DD/YYYY HH:mm');
    expect(formatted).to.equal('December/22/2015 00:00');
  });
});