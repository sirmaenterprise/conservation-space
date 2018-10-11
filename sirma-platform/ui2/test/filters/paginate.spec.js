import {Paginate} from 'filters/paginate';

describe('Paginate Filter', () => {

  const ITEMS = ['1', '2', '3', '4', '5', '6', '7', '8', '9', '10'];

  let paginate;
  beforeEach(() => {
    paginate = new Paginate();
  });

  it('should filter to a subset of items based on the configuration', () => {
    let filtered = paginate.filter(ITEMS, {page: 1, pageSize: 3});
    expect(filtered).to.deep.equal(['1', '2', '3']);

    filtered = paginate.filter(ITEMS, {page: 2, pageSize: 3});
    expect(filtered).to.deep.equal(['4', '5', '6']);

    filtered = paginate.filter(ITEMS, {page: 3, pageSize: 3});
    expect(filtered).to.deep.equal(['7', '8', '9']);

    filtered = paginate.filter(ITEMS, {page: 4, pageSize: 3});
    expect(filtered).to.deep.equal(['10']);

    filtered = paginate.filter(ITEMS, {page: 5, pageSize: 3});
    expect(filtered).to.deep.equal([]);

    filtered = paginate.filter(ITEMS, {page: 1, pageSize: 25});
    expect(filtered).to.deep.equal(ITEMS);
  });

  it('should handle undefined collections', () => {
    expect(paginate.filter(undefined, {})).to.deep.equal([]);
  });

});