import {Component, Inject, NgElement} from 'app/app';
import {UrlDecorator} from 'layout/url-decorator/url-decorator';

@Component({
  selector: 'click-handler'
})
@Inject(UrlDecorator, NgElement)
export class ClickHandler {
  constructor(urlDecorator) {
    this.urlDecorator = urlDecorator;
  }

  ngAfterViewInit() {
    $(document.body).click((event) => {
      this.urlDecorator.decorate(event);
    });
  }
}