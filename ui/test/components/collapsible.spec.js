import { Collapsible } from 'components/collapsible/collapsible';

describe('Collapsible', () => {
  it('toggleCaret should update icon element class depending on expanded flag', () => {
    let iconElement = {
      addClass: sinon.spy(),
      removeClass: sinon.spy()
    };

    Collapsible.toggleCaret(iconElement, true);
    expect(iconElement.addClass).to.be.calledOnce;
    expect(iconElement.addClass.getCall(0).args[0]).to.equal('fa-caret-right');
    expect(iconElement.removeClass).to.be.calledOnce;
    expect(iconElement.removeClass.getCall(0).args[0]).to.equal('fa-caret-down');

    iconElement.addClass.reset();
    iconElement.removeClass.reset();
    Collapsible.toggleCaret(iconElement, false);
    expect(iconElement.addClass).to.be.calledOnce;
    expect(iconElement.addClass.getCall(0).args[0]).to.equal('fa-caret-down');
    expect(iconElement.removeClass).to.be.calledOnce;
    expect(iconElement.removeClass.getCall(0).args[0]).to.equal('fa-caret-right');
  });

  it('isElementVisible should return boolean depending on element css', () => {
    let anElement = (cssProperties) => {
      return {
        css: (property) => {
          return cssProperties[property];
        }
      }
    };

    let visibleElement = anElement({ });
    expect(Collapsible.isElementVisible(visibleElement)).to.be.true;

    let hiddenElement = anElement({ display: 'none' });
    expect(Collapsible.isElementVisible(hiddenElement)).to.be.false;
  });
});
