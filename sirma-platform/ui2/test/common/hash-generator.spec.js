import {HashGenerator} from 'common/hash-generator';

describe('hash', () => {
  it('should compare equals arrays hash', () => {
    let array1 = [1, 2, 3, '4'];
    let array2 = [1, 2, 3, '4'];

    expect(HashGenerator.getHash(array1, true) === HashGenerator.getHash(array2, true)).to.be.true;
    expect(HashGenerator.getHash(array1, false) === HashGenerator.getHash(array2, false)).to.be.true;
  });

  it('should compare nested arrays hash', () => {
    let array1 = [1, 2, 3, ['4']];
    let array2 = [1, 2, 3, ['4']];

    expect(HashGenerator.getHash(array1, true) === HashGenerator.getHash(array2, true)).to.be.true;
    expect(HashGenerator.getHash(array1, false) === HashGenerator.getHash(array2, false)).to.be.true;
  });

  it('should compare random equals arrays hash', () => {
    let array1 = [1, 2, 3, 4];
    let array2 = [3, 2, 1, 4];

    expect(HashGenerator.getHash(array1, true) === HashGenerator.getHash(array2, true)).to.be.true;
    expect(HashGenerator.getHash(array1, false) === HashGenerator.getHash(array2, false)).to.be.false;
  });

  it('should compare random equals arrays hash with duplicates', () => {
    let array1 = [1, 2, 3, ['4'], ['4']];
    let array2 = [3, 2, 1, ['4'], 1];

    expect(HashGenerator.getHash(array1, true) === HashGenerator.getHash(array2, true)).to.be.true;
    expect(HashGenerator.getHash(array1, false) === HashGenerator.getHash(array2, false)).to.be.false;
  });

  it('should compare random equals arrays hash with objects', () => {
    let array1 = [1, 2, 3, ['4'], {'prop': [3, 3, 1, 2], 'prop2': 'abc'}];
    let array2 = [3, 2, {'prop2': 'abc', 'prop': [1, 2, 3, 1]}, 1, ['4'], 1];

    expect(HashGenerator.getHash(array1, true) === HashGenerator.getHash(array2, true)).to.be.true;
    expect(HashGenerator.getHash(array1, false) === HashGenerator.getHash(array2, false)).to.be.false;
  });

  it('should compare random equals arrays hash with null and undefined', () => {
    let array1 = [1, undefined, 3, ['4'], {'prop': [3, 2, 2, 1, null, 1], 'prop2': 'abc'}];
    let array2 = [3, undefined, {'prop2': 'abc', 'prop': [1, 2, null, 3]}, 1, ['4'], 1];

    expect(HashGenerator.getHash(array1, true) === HashGenerator.getHash(array2, true)).to.be.true;
    expect(HashGenerator.getHash(array1, false) === HashGenerator.getHash(array2, false)).to.be.false;
  });

  it('should compare random equals objects hash with null and undefined', () => {
    let array1 = {'a': [1, undefined, 3, ['4'], {'prop': [3, 2, 2, 1, null, 1], 'prop2': 'abc'}]};
    let array2 = {'a': [3, undefined, {'prop2': 'abc', 'prop': [1, 2, null, 3]}, 1, ['4'], 1]};
    let array3 = {'b': [3, undefined, {'prop2': 'abc', 'prop': [1, 2, null, 3]}, 1, ['4'], 1]};

    expect(HashGenerator.getHash(array1, true) === HashGenerator.getHash(array2, true)).to.be.true;
    expect(HashGenerator.getHash(array2, true) === HashGenerator.getHash(array3, true)).to.be.false;
    expect(HashGenerator.getHash(array1, false) === HashGenerator.getHash(array2, false)).to.be.false;
  });
});