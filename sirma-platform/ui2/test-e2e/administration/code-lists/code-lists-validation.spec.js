'use strict';

let CodeListsSandbox = require('./code-lists.js').CodeListsSandbox;

describe('Controlled vocabularies management - validation:', () => {

  let management;

  beforeEach(() => {
    management = new CodeListsSandbox().open(false).getManagement();
  });

  function generateString(length) {
    return new Array(length + 1).join('s');
  }

  describe('When creating new controlled vocabularies', () => {
    it('should validate the empty form and forbid creating until valid', () => {
      management.addCodeList().then(newCode => {
        let newCodeDetails = newCode.getDetails();

        // Should be empty and invalid at first
        assertInvalidEmptyDetails(newCode, newCodeDetails);

        // Should not allow duplicated identifiers
        newCodeDetails.setId('1');
        assertDuplicatedIdentifiers(newCode, newCodeDetails);

        // Should still be invalid because of the empty name
        newCodeDetails.setId('100');
        expect(newCode.canSave()).to.eventually.be.false;
        expect(newCodeDetails.isIdValid()).to.eventually.be.true;

        // Should disallow long names
        let longName = generateString(300);
        newCodeDetails.setName(longName);
        assertLongNameValidation(newCode, newCodeDetails);

        // Should allow create after ID and name are valid
        newCodeDetails.setName('Valid name for new controlled vocabulary');
        assertValidCodeDetails(newCode, newCodeDetails);
      });
    });

    it('should validate the length of the controlled vocabulary identifier', () => {
      management.addCodeList().then(newCode => {
        let newCodeDetails = newCode.getDetails();

        newCodeDetails.setName('Valid name');

        newCodeDetails.setId(generateString(41));
        assertLongIdentifierValidation(newCode, newCodeDetails);

        // 40 is the max length
        newCodeDetails.setId(generateString(40));
        assertValidCodeDetails(newCode, newCodeDetails);
      });
    });

    it('should validate invalid characters of the controlled vocabulary identifier', () => {
      management.addCodeList().then(newCode => {
        let newCodeDetails = newCode.getDetails();

        newCodeDetails.setId('Нова листа');
        assertInvalidIdentifierCharactersValidation(newCode, newCodeDetails);

        newCodeDetails.setId('!@#$');
        assertInvalidIdentifierCharactersValidation(newCode, newCodeDetails);
      });
    });

    it('should disallow creating until both vocabulary and its value are valid', () => {
      management.addCodeList().then(newCode => {
        let newCodeDetails = newCode.getDetails();
        expect(newCode.canSave()).to.eventually.be.false;

        newCodeDetails.setId('100');
        newCodeDetails.setName('Valid name for new controlled vocabulary');
        expect(newCode.canSave()).to.eventually.be.true;

        newCode.addValue().then(newValue => {
          expect(newCode.canSave()).to.eventually.be.false;
          newValue.setId('INIT');
          newValue.setName('Valid name for new value');
        });

        expect(newCode.canSave()).to.eventually.be.true;
      });
    });
  });

  describe('When editing existing controlled vocabulary', () => {
    it('should validate newly inserted value and forbid saving until valid', () => {
      management.getCodeLists().then(codeLists => {
        let projectState = codeLists[0].open();
        projectState.edit();

        // Should be initially valid after edit & should allow save
        expect(projectState.canSave()).to.eventually.be.true;

        projectState.addValue().then(newValue => {
          // Should be empty and invalid at first
          assertInvalidEmptyDetails(projectState, newValue);

          // Should not allow duplicated identifiers
          newValue.setId('APPROVED');
          assertDuplicatedIdentifiers(projectState, newValue);

          // Uniqueness validation should be case insensitive
          newValue.setId('AppROved');
          assertDuplicatedIdentifiers(projectState, newValue);

          // Should still be invalid because of the empty name
          newValue.setId('DELETED');
          expect(newValue.isIdValid()).to.eventually.be.true;
          expect(projectState.canSave()).to.eventually.be.false;

          // Should disallow long names
          let longName = generateString(300);
          newValue.setName(longName);
          assertLongNameValidation(projectState, newValue);

          // Should not allow duplicated names & be case insensitive
          newValue.setName('approved');
          assertDuplicatedNames(projectState, newValue);

          // Should allow create after ID and name are valid
          newValue.setName('Valid name for Deleted value');
          assertValidCodeDetails(projectState, newValue);
        });
      });
    });

    it('should validate the length of any new value\'s identifier', () => {
      management.getCodeLists().then(codeLists => {
        let projectState = codeLists[0].open();
        projectState.edit();

        projectState.addValue().then(newValue => {
          newValue.setName('Valid name');

          newValue.setId(generateString(150));
          assertLongIdentifierValidation(projectState, newValue);

          // 100 is the max length
          newValue.setId(generateString(100));
          assertValidCodeDetails(projectState, newValue);
        });
      });

      it('should validate the uniqueness of any new value\'s name', () => {
        management.getCodeLists().then(codeLists => {
          let projectState = codeLists[0].open();
          projectState.edit();

          projectState.addValue().then(newValue => {
            newValue.setName('approved');
            assertDuplicatedNames(projectState, newValue);

            newValue.setName('UpdatedValueName');
            assertValidCodeDetails(projectState, newValue);
          });
        });
      });
    });

    it('should validate invalid characters of new controlled vocabulary value identifier', () => {
      management.getCodeLists().then(codeLists => {
        let projectState = codeLists[0].open();
        projectState.edit();

        projectState.addValue().then(newValue => {

          newValue.setId('Нова листа');
          assertInvalidIdentifierCharactersValidation(projectState, newValue);

          newValue.setId('!@#$');
          assertInvalidIdentifierCharactersValidation(projectState, newValue);
        });
      });
    });

    it('should perform validation upon the vocabulary\'s name', () => {
      management.getCodeLists().then(codeLists => {
        let projectState = codeLists[0].open();
        projectState.edit();

        let projectStateDetails = projectState.getDetails();
        // Should be initially valid after edit & should allow save
        assertValidCodeDetails(projectState, projectStateDetails);

        projectStateDetails.setName('');
        assertEmptyNameValidation(projectState, projectStateDetails);

        projectStateDetails.setName('Project type');
        assertDuplicatedNames(projectState, projectStateDetails);

        // Should perform validation after modifying the name through the description dialog
        changeNameInDescriptionsDialog(projectStateDetails, 2, 'New project state name');

        assertValidCodeDetails(projectState, projectStateDetails);
      });
    });

    it('should perform validation upon existing value\'s name', () => {
      // Same as above but for values
      management.getCodeLists().then(codeLists => {
        let projectState = codeLists[0].open();
        projectState.edit();

        projectState.getValues().then(values => {
          let completedState = values[1];

          completedState.setName('');
          assertEmptyNameValidation(projectState, completedState);

          changeNameInDescriptionsDialog(completedState, 2, 'New completed state name');

          assertValidCodeDetails(projectState, completedState);
        });
      });
    });
  });

  function assertInvalidEmptyDetails(code, codeDetails) {
    expect(code.canSave()).to.eventually.be.false;
    expect(codeDetails.isIdValid()).to.eventually.be.false;
    expect(codeDetails.getIdValidationMessages().isEmpty()).to.eventually.be.true;
    expect(codeDetails.isNameValid()).to.eventually.be.false;
    expect(codeDetails.getNameValidationMessages().isEmpty()).to.eventually.be.true;
  }

  function assertDuplicatedIdentifiers(code, codeDetails) {
    expect(code.canSave()).to.eventually.be.false;
    expect(codeDetails.isIdValid()).to.eventually.be.false;
    expect(codeDetails.getIdValidationMessages().isNotUnique()).to.eventually.be.true;
  }

  function assertDuplicatedNames(code, codeDetails) {
    expect(code.canSave()).to.eventually.be.false;
    expect(codeDetails.isNameValid()).to.eventually.be.false;
    expect(codeDetails.getNameValidationMessages().isNotUnique()).to.eventually.be.true;
  }

  function assertLongIdentifierValidation(code, codeDetails) {
    expect(code.canSave()).to.eventually.be.false;
    expect(codeDetails.isIdValid()).to.eventually.be.false;
    expect(codeDetails.getIdValidationMessages().exceedsMaxSize()).to.eventually.be.true;
  }

  function assertInvalidIdentifierCharactersValidation(code, codeDetails) {
    expect(code.canSave()).to.eventually.be.false;
    expect(codeDetails.isIdValid()).to.eventually.be.false;
    expect(codeDetails.getIdValidationMessages().invalidCharacters()).to.eventually.be.true;
  }

  function assertEmptyNameValidation(code, codeDetails) {
    expect(code.canSave()).to.eventually.be.false;
    expect(codeDetails.isNameValid()).to.eventually.be.false;
    expect(codeDetails.getNameValidationMessages().isEmpty()).to.eventually.be.true;
  }

  function assertLongNameValidation(code, codeDetails) {
    expect(code.canSave()).to.eventually.be.false;
    expect(codeDetails.isNameValid()).to.eventually.be.false;
    expect(codeDetails.getNameValidationMessages().exceedsMaxSize()).to.eventually.be.true;
  }

  function assertValidCodeDetails(code, codeDetails) {
    expect(code.canSave()).to.eventually.be.true;
    expect(codeDetails.isIdValid()).to.eventually.be.true;
    expect(codeDetails.isNameValid()).to.eventually.be.true;
  }

  function changeNameInDescriptionsDialog(codeDetails, languageIndex, name) {
    let descriptionsDialog = codeDetails.openDescriptions();
    descriptionsDialog.getDescriptions().then(descriptions => {
      let description = descriptions[languageIndex];
      description.setName(name);
      descriptionsDialog.close();
    });
  }

});