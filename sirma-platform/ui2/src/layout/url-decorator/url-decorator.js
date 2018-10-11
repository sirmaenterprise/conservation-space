import {Injectable, Inject} from 'app/app';
import {Router} from 'adapters/router/router';
import {WindowAdapter} from 'adapters/angular/window-adapter';
import {StateParamsAdapter} from 'adapters/router/state-params-adapter';

/**
 * Decorates URLs of objects created in the old web to be accessible from this one.
 */
@Injectable()
@Inject(Router, StateParamsAdapter, WindowAdapter, '$state')
export class UrlDecorator {

  constructor(router, stateParamsAdapter, windowAdapter, $state) {
    this.router = router;
    this.$state = $state;
    this.windowAdapter = windowAdapter;
    this.stateParamsAdapter = stateParamsAdapter;
  }

  decorate(event) {
    if (!event.target) {
      return;
    }
    let linkNode = $(event.target).closest(UrlDecorator.INSTANCE_LINK_SELECTOR);
    if (linkNode[0]) {
      UrlDecorator.decorateUrl(linkNode);
      // allow page reload if the link that was triggered is for the same view as for the currently loaded one
      if (this.shouldReload(linkNode)) {
        event.stopPropagation();
        event.preventDefault();
        let currentState = this.router.getCurrentState();
        this.router.navigate(currentState, this.stateParamsAdapter.getStateParams(), {reload: true});
      }
    }
  }

  shouldReload(linkNode) {
    if (linkNode.hasClass(UrlDecorator.SUPPRESSED_LINK_CLASS)) {
      return false;
    }
    var currentStateUrl = this.router.getStateUrl(this.router.getCurrentState());
    currentStateUrl = decodeURIComponent(currentStateUrl);
    let currentUrl = UrlDecorator.stripSearch(this.windowAdapter.location.href);
    let linkHref = UrlDecorator.stripSearch(UrlDecorator.getHref(linkNode));
    return currentUrl === linkHref && currentUrl === currentStateUrl;
  }

  static getHref(linkNode) {
    return linkNode.attr('href');
  }

  static stripSearch(url) {
    if (url.indexOf('?') !== -1) {
      url = url.substring(0, url.indexOf('?'));
    }
    if (url.indexOf('#') !== -1) {
      url = url.substring(url.indexOf('#'));
    }
    return url;
  }

  static decorateUrl(linkNode) {
    let href = UrlDecorator.getHref(linkNode);
    if (href.length > 0 && href[0] === '/') {
      linkNode.attr('href', href.replace('emf', 'remote'));
    }
  }
}

UrlDecorator.INSTANCE_LINK_SELECTOR = '.instance-link';
UrlDecorator.SUPPRESSED_LINK_CLASS = 'suppressed-link';