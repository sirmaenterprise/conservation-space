import {HelpService, HELP_INSTANCE_TYPE} from 'services/help/help-service';
import {InstanceObject} from 'models/instance-object';
import {IdocPageTestHelper} from './idoc-page-test-helper';
import {stub} from 'test/test-utils';

describe('Idoc Contextual Help', () => {

  describe('configureContextualHelp', () => {
    it('should configure the contextual help if the instance is not a help one', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.helpTarget = undefined;
      let helpService = stub(HelpService);
      helpService.getHelpInstanceId.returns('instance-id');

      let models = {
        definitionId: 'image',
        instanceType: 'image-type'
      };
      let currentObject = new InstanceObject('some-id', models);
      idocPage.configureContextualHelp(currentObject, helpService);

      expect(idocPage.helpTarget).to.equal('object.image');
    });

    it('should not configure the contextual help if the instance is a help one', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.helpTarget = undefined;

      let models = {
        definitionId: 'help',
        instanceType: HELP_INSTANCE_TYPE
      };
      let currentObject = new InstanceObject('some-id', models);

      idocPage.configureContextualHelp(currentObject);

      expect(idocPage.helpTarget).to.not.exist;
    });

    it('should not configure the contextual help if there is no instance for the target', () => {
      let idocPage = IdocPageTestHelper.instantiateIdocPage();
      idocPage.helpTarget = undefined;

      let helpService = stub(HelpService);
      helpService.getHelpInstanceId.returns(undefined);

      let models = {
        definitionId: 'image',
        instanceType: 'image-type'
      };
      let currentObject = new InstanceObject('some-id', models);

      idocPage.configureContextualHelp(currentObject, helpService);

      expect(idocPage.helpTarget).to.not.exist;
    });
  });

});