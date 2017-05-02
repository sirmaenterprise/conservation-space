export const KEY_ENTER = 13;

export class Keys {

  static isEnter(code) {
    return Keys.is(KEY_ENTER, code);
  }

  static is(expected, actual) {
    return expected === actual;
  }
}