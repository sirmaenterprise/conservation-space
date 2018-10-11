import {ArrayToCSV} from 'filters/array-to-csv';

describe('ArrayToCSV', () => {
  let filter = new ArrayToCSV();

  it('should return array values converted to csv', () => {
    expect(filter.filter(['This', 'is', 'an', 'Array'])).to.equal('This, is, an, Array');
  });

  it('should return number array values converted to csv', () => {
    expect(filter.filter([1, 2, 3, 4])).to.equal('1, 2, 3, 4');
  });

  it('should skip empty array values when converting to csv', () => {
    expect(filter.filter([1, , 'test', , ,])).to.equal('1, test');
  });

  it('should return an empty string if an empty array is passed', () => {
    expect(filter.filter([])).to.equal('');
  });

  it('should return an empty string if no argument is passed', () => {
    expect(filter.filter()).to.equal('');
  });

  it('should return string values intact', () => {
    expect(filter.filter('This is a plain string')).to.equal('This is a plain string');
  });
});
