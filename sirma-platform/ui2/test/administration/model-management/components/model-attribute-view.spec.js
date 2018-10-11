import {PluginsService} from 'services/plugin/plugins-service';
import {ModelAttribute} from 'administration/model-management/model/model-attribute';
import {ModelAttributeView} from 'administration/model-management/components/model-attribute-view';

import {PromiseStub} from 'test/promise-stub';
import {stub} from 'test/test-utils';

describe('ModelAttributeView', () => {

  let attribute;
  let modelAttributeView;

  let element;
  let scopeMock;
  let elementMock;
  let compileMock;
  let pluginsServiceStub;

  beforeEach(() => {
    attribute = new ModelAttribute('label');
    element = {
      append: sinon.spy(() => {
      }),
      empty: sinon.spy(() => {
      })
    };

    scopeMock = mockScope();
    compileMock = mockCompile();
    elementMock = mockElement(element);
    pluginsServiceStub = stub(PluginsService);

    pluginsServiceStub.loadComponentModules.returns(PromiseStub.resolve(getExtensions()));
    modelAttributeView = new ModelAttributeView(pluginsServiceStub, PromiseStub, scopeMock, elementMock, compileMock);
    modelAttributeView.attribute = attribute;
  });

  it('should compile a given extension as a component', () => {
    modelAttributeView.compileAttributeExtension(getExtensions()['string']);

    expect(element.empty.calledOnce).to.be.true;
    expect(element.append.calledWith('extension')).to.be.true;

    expect(scopeMock.$new.calledOnce).to.be.true;
    expect(elementMock.find.calledWith('.attribute-extension')).to.be.true;
    expect(compileMock.calledWith('<model-string-attribute attribute="modelAttributeView.attribute"></model-string-attribute>')).to.be.true;
  });

  it('should cache extension modules after the first load', () => {
    expect(modelAttributeView.modules).to.not.exist;

    modelAttributeView.ngOnInit();
    expect(modelAttributeView.modules).to.deep.eq(getExtensions());
  });

  it('should get the proper attribute extension or a default one', () => {
    attribute.setType('label');
    modelAttributeView.getAttributeExtension(attribute).then(extension => {
      expect(extension.component).to.eq('model-label-attribute');
    });
  });

  function getExtensions() {
    return {
      string: {
        'type': 'string',
        'component': 'model-string-attribute',
      },
      label: {
        'type': 'label',
        'component': 'model-label-attribute',
      },
      boolean: {
        'type': 'boolean',
        'component': 'model-boolean-attribute',
      }
    };
  }

  function mockElement(element) {
    return {
      find: sinon.spy(() => element)
    };
  }

  function mockScope() {
    return {
      $new: sinon.spy(() => {
      })
    };
  }

  function mockCompile() {
    return sinon.spy(() => {
      return sinon.spy(() => {
        return ['extension'];
      });
    });
  }
});