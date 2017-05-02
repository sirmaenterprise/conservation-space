import {StaticInstanceHeader} from 'instance-header/static-instance-header/static-instance-header';
import {HEADER_DEFAULT, HEADER_COMPACT} from 'instance-header/header-constants';
import {mock$scope} from 'test/idoc/widget/object-data-widget/scope.mock';

describe('StaticInstanceHeader', () => {

  it('should configure the header to be enabled by default', () => {
    expect(new StaticInstanceHeader(mockElement(), undefined, mock$scope()).config.disabled).to.be.false;
  });

  it('should not trigger compile if header is not present', () => {
    var compiler = mockCompile();
    var scope = mock$scope();
    var header = new StaticInstanceHeader(mockElement(), compiler, scope);
    scope.$digest();
    expect(compiler.called).to.be.false;
  });

  it('should trigger compile if header is present', () => {
    var compiler = mockCompile();
    StaticInstanceHeader.prototype.header = "<span>Header</span>";
    var scope = mock$scope();
    var header = new StaticInstanceHeader(mockElement(), compiler, scope);
    scope.$digest();
    expect(compiler.called).to.be.true;
  });

  it('should destroy inner scope and clear header wrapper element', () => {
    var header = new StaticInstanceHeader(mockElement(), undefined, mock$scope());
    header.innerScope = {
      $destroy: sinon.spy()
    };
    header.instanceDataElement = {
      empty: sinon.spy()
    };
    header.clearData();

    expect(header.innerScope.$destroy.callCount).to.equals(1);
    expect(header.instanceDataElement.empty.callCount).to.equals(1);
  });
});

function mockElement() {
  return {
    find: ()=> {
      return {
        append: () => {},
        empty: () => {}
      }
    }
  };
}

function mockCompile() {
  var compile = () => {
    var returnFunc = () => {
      return 'compile';
    };
    return sinon.spy(returnFunc);
  };
  return sinon.spy(compile);
}
