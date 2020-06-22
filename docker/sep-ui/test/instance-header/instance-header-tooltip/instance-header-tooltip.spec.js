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
    getInstanceHeaderTooltip(headerLink);
    expect(headerLink.click.called).to.be.true;
    expect(headerLink.hover.called).to.be.true;
  });

  it('should display tooltip content', () => {
    var headerLink = $('<a href="path/emf:123"></a>');
    var instanceHeaderTooltip = getInstanceHeaderTooltip(headerLink);
    instanceHeaderTooltip.loadTooltip(headerLink);
    expect(instanceHeaderTooltip.tooltipAdapter.tooltip.called).to.be.true;
    expect(instanceHeaderTooltip.tooltipAdapter.tooltip.getCall(0).args[1].title).to.be.equal('<p>Tooltip</p>');
  });

  it('should not display tooltip content if instance identifier is not present', () => {
    var headerLink = $('<a href="path/123"></a>');
    var instanceHeaderTooltip = getInstanceHeaderTooltip(headerLink);
    instanceHeaderTooltip.loadTooltip(headerLink);
    expect(instanceHeaderTooltip.tooltipAdapter.tooltip.called).to.be.false;
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
    expect(instanceHeaderTooltip.tooltipSkipRequest.resolve.called).to.be.true;
    expect(tooltipLoader.cancel.called).to.be.true;
    expect(instanceHeaderTooltip.tooltipAdapter.destroy.called).to.be.true;
  });

  it('should cut data properties values if they are longer than 150 characters', () => {
    var headerLink = $('<a href="path/emf:123"></a>');
    var tooltipData = '<div data-property="description">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis mollis ligula vitae urna hendrerit, a consequat sapien feugiat. Suspendisse potenti. Vestibulum eu malesuada arcu, at malesuada ante.</div>';
    var instanceHeaderTooltip = getInstanceHeaderTooltip(headerLink, tooltipData);
    instanceHeaderTooltip.loadTooltip(headerLink);
    expect(instanceHeaderTooltip.tooltipAdapter.tooltip.called).to.be.true;
    expect(instanceHeaderTooltip.tooltipAdapter.tooltip.getCall(0).args[1].title).to.be.equal('<div data-property="description">Lorem ipsum dolor sit amet, consectetur adipiscing elit. Duis mollis ligula vitae urna hendrerit, a consequat sapien feugiat. Suspendisse potenti. Ves...</div>');
  });
});

function getInstanceHeaderTooltip(element, tooltipData) {
  return new InstanceHeaderTooltip(element, getTooltipAdapterMock(), PromiseAdapterMock.mockAdapter(), getContextualEscapeAdapterMock(), getInstanceServiceMock(tooltipData));
}

function getInstanceServiceMock(data) {
  data = data || '<p>Tooltip</p>';
  return {
    getTooltip: () => {
      return PromiseStub.resolve({
        data
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
  };
}