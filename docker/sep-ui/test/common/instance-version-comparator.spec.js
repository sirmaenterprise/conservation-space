import {InstanceVersionComparator} from 'instance/instance-version-comparator';

describe('versionComparator', () => {
  it('should throw TypeError when compare not matching pattern versions', () => {
    let version1 = 'aaa';
    let version2 = '1.19';

    expect(function () {
      InstanceVersionComparator.compare(version1, version2);
    }).to.throw(TypeError);
  });

  it('should return 0 when compare equals versions', () => {
    let version1 = '1.19';
    let version2 = '1.19';
    expect(InstanceVersionComparator.compare(version1, version2)).to.equal(0);

    version1 = '1.19.0.15';
    version2 = '1.19.0.15';
    expect(InstanceVersionComparator.compare(version1, version2)).to.equal(0);
  });

  it('should return 1 when first versions is greater', () => {
    let version1 = '1.19';
    let version2 = '1.9';
    expect(InstanceVersionComparator.compare(version1, version2)).to.equal(1);

    version1 = '1.19.1';
    version2 = '1.19';
    expect(InstanceVersionComparator.compare(version1, version2)).to.equal(1);
  });

  it('should return -1 when first versions is smaller', () => {
    let version1 = '1.9';
    let version2 = '1.29';
    expect(InstanceVersionComparator.compare(version1, version2)).to.equal(-1);

    version1 = '1.29.14';
    version2 = '1.29.14.1';
    expect(InstanceVersionComparator.compare(version1, version2)).to.equal(-1);
  });
});