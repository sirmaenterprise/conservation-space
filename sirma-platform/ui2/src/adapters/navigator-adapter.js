import {Injectable} from 'app/app';
import bowser from 'bowser';

@Injectable()
export class NavigatorAdapter {

  static getNavigator() {
    var ua = navigator.userAgent, tem;
    var M = ua.match(/(edge(?=\/))\/?\s*(\d+)/i);
    if (!M) {
      M = ua.match(/(opera|chrome|safari|firefox|msie|trident(?=\/))\/?\s*(\d+)/i) || [];
    }
    if (/trident/i.test(M[1])) {
      tem = /\brv[ :]+(\d+)/g.exec(ua) || [];
      return 'IE ' + (tem[1] || '');
    }
    if (M[1] === 'Chrome') {
      tem = ua.match(/\bOPR\/(\d+)/);
      if (tem != null) {
        return 'Opera ' + tem[1];
      }
    }
    M = M[2] ? [M[1], M[2]] : [navigator.appName, navigator.appVersion, '-?'];
    if ((tem = ua.match(/version\/(\d+)/i)) != null) {
      M.splice(1, 1, tem[1]);
    }
    return M.join(' ');
  }

  static getOS() {
    // Win XP: Windows NT 5.1
    // Win8: Windows NT 6.2; WoW64
    // Win7: Windows NT 6.1; WOW64
    // os x: Intel Mac OS X 10.9
    // Linux: Linux i686
    return navigator.platform;
  }

  static mobile() {
    return /Mobile|mini|Fennec|Android|iP(ad|od|hone)/.test(navigator.appVersion);
  }

  static isInternetExplorer() {
    // IE <= 11
    return !!bowser.msie;
  }

  static isEdge() {
    let ua = NavigatorAdapter.getNavigator();
    return ua.indexOf('Edge') > -1;
  }

  static isEdgeOrIE() {
    let ua = NavigatorAdapter.getNavigator();
    return ua.indexOf('IE ') > -1 || ua.indexOf('Edge') > -1;
  }

  static isChrome(){
    return /chrome/i.test( NavigatorAdapter.getNavigator() );
  }

  static isFirefox(){
    return navigator.userAgent.search('Firefox') > -1;
  }

  static isSafari() {
    return !!bowser.safari;
  }
}
