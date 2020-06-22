import {Component, Inject, NgElement} from 'app/app';

/*
 * Provides an alternative src URL if src returns 404.
 */
@Component({
  selector: '[alt-src]'
})
@Inject(NgElement)
export class AltSrc {

  constructor(element) {
    this.element = element;

    this.element.on('error', this.handleErrorSrc.bind(this));
  }

  handleErrorSrc() {
    var src = this.element.attr('src');
    var altSrc = this.element.attr('alt-src');

    // avoid infinite recursion if altSrc also returns 404
    if (src !== altSrc) {
      this.element.attr('src', altSrc);
    }
  }
}