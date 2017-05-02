import {SimpleDateToMomentFormat} from 'common/simple-date-to-moment-format';

describe('SimpleDateToMomentFormat',()=>{
  it('should format Java SimpleDateFormat strings to moment equivalent',()=>{
    let simpleFormatString = 'YY-yyy.MM/ww, D-ddd-d/dd-EE"EEEE"-uu(kk:mm:ss:S) :KK z Z X';
    let formattedString = SimpleDateToMomentFormat.convertToMomentFormat(simpleFormatString);
    expect(formattedString).to.equal('gg-YYY.MM/ww, DDD-DD-DD/DD-ddd"dddd"-ee(HH:mm:ss:SSS) :hh Z Z Z');
  });
});