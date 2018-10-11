export class InstanceVersionComparator {
  static REGEX = new RegExp('^((\\d+)\\.)+((\\d+))$');

  /**
   * Two instances' sequence-based versions comparator. Can compare versions matching ^((\d+)\.)+((\d+))$
   *
   * A version number MUST take the form X.Y.Z where X, Y, and Z are non-negative integers and
   * starts and ends with non-negative integers.
   * X is the major version, Y is the minor version, and Z is the patch version.
   * Each element MUST increase numerically, and MUST NOT contain leading zeroes.
   * For instance: 1.9.0 -> 1.10.0 -> 1.11.0 -> 1.11.1
   * More specific groups are allowed if following above rules.
   * @param versionOne first version
   * @param versionTwo second version
   * @returns   1 when versionOne > versionTwo
   *           -1 when versionOne < versionTwo
   *            0 when versionOne = versionTwo
   */
  static compare(versionOne, versionTwo) {
    if (!this.REGEX.test(versionOne) || !this.REGEX.test(versionTwo)) {
      throw new TypeError('Version should match ^((\\d+)\\.)+((\\d+))$');
    }

    if (versionOne === versionTwo) {
      return 0;
    }

    let versionOneSegments = versionOne.split('.');
    let versionTwoSegments = versionTwo.split('.');

    let length = Math.min(versionOneSegments.length, versionTwoSegments.length);

    for (let i = 0; i < length; i++) {
      if (parseInt(versionOneSegments[i]) > parseInt(versionTwoSegments[i])) {
        return 1;
      }

      if (parseInt(versionOneSegments[i]) < parseInt(versionTwoSegments[i])) {
        return -1;
      }
    }

    if (versionOneSegments.length > versionTwoSegments.length) {
      return 1;
    }

    if (versionOneSegments.length < versionTwoSegments.length) {
      return -1;
    }
  }
}