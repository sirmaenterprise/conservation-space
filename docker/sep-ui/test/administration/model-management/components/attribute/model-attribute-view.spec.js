import {PluginsService} from 'services/plugin/plugins-service';
import {ModelAttribute} from 'administration/model-management/model/attributes/model-attribute';
import {ModelAttributeMetaData} from 'administration/model-management/meta/model-attribute-meta';
import {ModelAttributeView} from 'administration/model-management/components/attributes/model-attribute-view';
import {ModelDescription} from 'administration/model-management/model/model-value';

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
    attribute.setMetaData(new ModelAttributeMetaData());

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
    modelAttributeView.onAttributeLoad = sinon.spy();
    modelAttributeView.attribute = attribute;
  });

  it('should compile a given extension as a component', () => {
    modelAttributeView.compileAttributeExtension(getExtensions()['string']);

    expect(element.empty.calledOnce).to.be.true;
    expect(element.append.calledWith('extension')).to.be.true;

    expect(scopeMock.$new.calledOnce).to.be.true;
    expect(elementMock.find.calledWith('.attribute-extension')).to.be.true;
    expect(compileMock.calledWith('<model-string-attribute'
      + ' config="::modelAttributeView.config"'
      + ' attribute="modelAttributeView.attribute"'
      + ' on-change="modelAttributeView.onChange()"'
      + ' editable="modelAttributeView.isEditable()"'
      + ' context="modelAttributeView.getContext()"'
      + '></model-string-attribute>')).to.be.true;
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

  it('should decide if the attribute is valid or invalid based on its validation model',() => {
    attribute.getValidation().addError('error');
    expect(modelAttributeView.isInvalid()).to.be.true;

    attribute.getValidation().clearErrors();
    expect(modelAttributeView.isInvalid()).to.be.false;
  });

  it('should decide if the attribute is editable or not',() => {
    // enforce attribute as editable
    modelAttributeView.editable = true;
    attribute.getRestrictions().setUpdateable(false);
    expect(modelAttributeView.isEditable()).to.be.true;

    // enforce attribute as not editable
    modelAttributeView.editable = false;
    attribute.getRestrictions().setUpdateable(true);
    expect(modelAttributeView.isEditable()).to.be.false;

    // editable enforced only by restrictions
    modelAttributeView.editable = undefined;
    attribute.getRestrictions().setUpdateable(true);
    expect(modelAttributeView.isEditable()).to.be.true;

    // editable enforced only by restrictions
    modelAttributeView.editable = undefined;
    attribute.getRestrictions().setUpdateable(false);
    expect(modelAttributeView.isEditable()).to.be.false;
  });

  it('should decide if attribute has tooltip value or not', () => {
    expect(modelAttributeView.hasTooltipValue()).to.be.false;
    attribute.getMetaData().setTooltip(new ModelDescription('en', 'tooltip value'));
    expect(modelAttributeView.getTooltipValue()).to.eq('tooltip value');
  });

  function getExtensions() {
    return {
      string: {
        'type': 'string',
        'component': 'model-string-attribute'
      },
      label: {
        'type': 'label',
        'component': 'model-label-attribute'
      },
      boolean: {
        'type': 'boolean',
        'component': 'model-boolean-attribute'
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