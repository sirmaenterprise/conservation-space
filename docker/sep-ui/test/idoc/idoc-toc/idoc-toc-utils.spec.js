import {ToCUtils} from 'idoc/idoc-toc/idoc-toc-utils';

describe('Tests for the idoc table of contents: idoc-toc-utils', function() {

  it('Test if ToCUtils.calcSmallerOrEqualHeadings() get the proper headings', function() {
    assert.equal(ToCUtils.calcSmallerOrEqualHeadings(6),'H1,H2,H3,H4,H5,H6');
    assert.equal(ToCUtils.calcSmallerOrEqualHeadings(2),'H1,H2');
    assert.equal(ToCUtils.calcSmallerOrEqualHeadings(1),'H1');
  });

});