import {ClickHandler} from 'layout/url-decorator/click-handler';

describe('ClickHandler', () => {

  it('should assign onclick handler on document body when created', () => {
    var _$ = $;

    let urlDecorator = {
      decorate: sinon.spy()
    };

    $ = function () {
      return {
        click: (f) => {
          urlDecorator.decorate();
        }
      }
    };

    let handler = new ClickHandler(urlDecorator, null);
    handler.ngAfterViewInit();

    $ = _$;

    expect(urlDecorator.decorate.calledOnce).to.be.true;
  });

});