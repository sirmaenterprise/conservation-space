import {InstanceHeaderTooltip} from 'instance-header/instance-header-tooltip/instance-header-tooltip';
import {PromiseAdapterMock} from 'test/adapters/angular/promise-adapter-mock';
import {PromiseStub} from 'test/promise-stub';

describe('InstanceHeaderTooltip', () => {

  it('should bind tooltip events if instance link is present', () => {
    var headerLink = {
      click: sinon.spy(),
      hover: sinon.spy(),
      length: 1
    };
    var instanceHeaderTooltip = getInstanceHeaderTooltip(headerLink);
    expect(headerLink.click.called).to.be.true;
    expect(headerLink.hover.called).to.be.true;
  });

  it('should display tooltip content', () => {
    var headerLink = $('<a href="path/emf:123"></a>');
    var instanceHeaderTooltip = getInstanceHeaderTooltip(headerLink);
    instanceHeaderTooltip.loadTooltip(headerLink);
    expect(instanceHeaderTooltip.tooltipAdapter.tooltip.called).to.be.true
    expect(instanceHeaderTooltip.tooltipAdapter.tooltip.getCall(0).args[1].title).to.be.equal('<p>Tooltip</p>');
  });

  it('should not display tooltip content if instance identifier is not present', () => {
    var headerLink = $('<a href="path/123"></a>');
    var instanceHeaderTooltip = getInstanceHeaderTooltip(headerLink);
    instanceHeaderTooltip.loadTooltip(headerLink);
    expect(instanceHeaderTooltip.tooltipAdapter.tooltip.called).to.be.false
  });

  it('should cancel tooltip properly', () => {
    var headerLink = $('<a href="path/emf:123"></a>');
    var instanceHeaderTooltip = getInstanceHeaderTooltip(headerLink);

    instanceHeaderTooltip.tooltipSkipRequest = {
      resolve: sinon.spy()
    };
    var tooltipLoader = {
      cancel: sinon.spy()
    };
    instanceHeaderTooltip.cancelTooltip(headerLink, tooltipLoader);
    expect(instanceHeaderTooltip.tooltipSkipRequest.resolve.called).to.be.true
    expect(tooltipLoader.cancel.called).to.be.true
    expect(instanceHeaderTooltip.tooltipAdapter.destroy.called).to.be.true
  });
});

function getInstanceHeaderTooltip(element) {
  return new InstanceHeaderTooltip(element, getTooltipAdapterMock(), PromiseAdapterMock.mockAdapter(), getContextualEscapeAdapterMock(), getInstanceServiceMock());
}

function getInstanceServiceMock() {
  return {
    getTooltip: () => {
      return PromiseStub.resolve({
        data: '<p>Tooltip</p>'
      });
    }
  };
}

function getContextualEscapeAdapterMock() {
  return {
    trustAsHtml: (element)=> {
      return element;
    }
  };
}

function getTooltipAdapterMock() {
  return {
    destroy: sinon.spy(),
    tooltip: sinon.spy()
  }
}