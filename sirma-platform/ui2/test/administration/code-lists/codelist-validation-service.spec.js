import {
  CodelistValidationService,
  MAX_CL_LENGTH,
  MAX_CV_LENGTH,
  MAX_NAME_LENGTH
} from 'administration/code-lists/services/codelist-validation-service';
import {getCodeLists} from './code-lists.stub';

describe('CodelistValidationService', () => {

  let codeLists;
  let validationService;

  beforeEach(() => {
    codeLists = getCodeLists();
    validationService = new CodelistValidationService();
  });

  it('should validate valid code list', () => {
    let toValidate = codeLists[0];

    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.true;
    expect(toValidate.validationModel['id'].valid).to.be.true;
    expect(toValidate.description.validationModel['name'].valid).to.be.true;
  });

  it('should validate invalid code list that has no identifier', () => {
    let toValidate = codeLists[0];

    toValidate.id = '';
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.false;
    expect(toValidate.validationModel['id'].valid).to.be.false;
    expect(toValidate.validationModel['id'].empty).to.be.true;
    expect(toValidate.description.validationModel['name'].valid).to.be.true;
  });

  it('should validate invalid code list that has duplicated identifier', () => {
    let toValidate = codeLists[0];

    toValidate.id = codeLists[1].id;
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.false;
    expect(toValidate.validationModel['id'].valid).to.be.false;
    expect(toValidate.validationModel['id'].exists).to.be.true;
    expect(toValidate.description.validationModel['name'].valid).to.be.true;
  });

  it('should validate invalid code list that has very long identifier', () => {
    let toValidate = codeLists[0];

    toValidate.id = generateString(50);
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.false;
    expect(toValidate.validationModel['id'].valid).to.be.false;
    expect(toValidate.validationModel['id'].exceedsSize).to.be.true;
    expect(toValidate.validationModel['id'].maxSize).to.equal(MAX_CL_LENGTH);

    // 40 is the max
    toValidate.id = generateString(40);
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.true;
  });

  it('should validate invalid code list that has empty name', () => {
    let toValidate = codeLists[0];

    toValidate.description.name = '';
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.false;
    expect(toValidate.validationModel['id'].valid).to.be.true;
    expect(toValidate.description.validationModel['name'].valid).to.be.false;
    expect(toValidate.description.validationModel['name'].empty).to.be.true;
  });

  it('should validate invalid code list that has very long name', () => {
    let toValidate = codeLists[0];

    toValidate.description.name = generateString(300);
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.false;
    expect(toValidate.validationModel['id'].valid).to.be.true;
    expect(toValidate.description.validationModel['name'].valid).to.be.false;
    expect(toValidate.description.validationModel['name'].exceedsSize).to.be.true;
    expect(toValidate.description.validationModel['name'].maxSize).to.equal(MAX_NAME_LENGTH);
  });

  it('should validate only new or modified code values', () => {
    let toValidate = codeLists[0];

    validationService.validate(codeLists, toValidate);
    expect(toValidate.values[0].validationModel).to.not.exist;
    expect(toValidate.values[1].validationModel).to.not.exist;

    toValidate.values[0].isNew = true;
    validationService.validate(codeLists, toValidate);
    expect(toValidate.values[0].validationModel.valid).to.be.true;
    expect(toValidate.values[1].validationModel).to.not.exist;

    toValidate.values[1].isModified = true;
    validationService.validate(codeLists, toValidate);
    expect(toValidate.values[0].validationModel.valid).to.be.true;
    expect(toValidate.values[1].validationModel.valid).to.be.true;
  });

  it('should perform the same validation on code values as on code lists', () => {
    let toValidate = codeLists[0];
    // Test uniqueness + empty
    toValidate.values[0].isModified = true;
    toValidate.values[0].id = 'val12';
    toValidate.values[0].description.name = '';

    validationService.validate(codeLists, toValidate);
    expect(toValidate.values[0].validationModel.valid).to.be.false;
    expect(toValidate.values[0].validationModel['id'].valid).to.be.false;
    expect(toValidate.values[0].validationModel['id'].exists).to.be.true;
    expect(toValidate.values[0].description.validationModel['name'].valid).to.be.false;
    expect(toValidate.values[0].description.validationModel['name'].empty).to.be.true;

    // The code list should be marked as invalid if even one of it's values are invalid
    expect(toValidate.validationModel.valid).to.be.false;
  });

  it('should perform the same length validation on code values identifiers', () => {
    let toValidate = codeLists[0];
    toValidate.values[0].isModified = true;
    toValidate.values[0].id = generateString(150);

    validationService.validate(codeLists, toValidate);
    expect(toValidate.values[0].validationModel.valid).to.be.false;
    expect(toValidate.values[0].validationModel['id'].valid).to.be.false;
    expect(toValidate.values[0].validationModel['id'].exceedsSize).to.be.true;
    expect(toValidate.values[0].validationModel['id'].maxSize).to.equal(MAX_CV_LENGTH);
  });

  it('should validate invalid code list that has duplicated name on base language', () => {
    let toValidate = codeLists[0];

    toValidate.description.name = codeLists[1].description.name;
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.false;
    expect(toValidate.validationModel['id'].valid).to.be.true;
    expect(toValidate.description.validationModel['name'].exists).to.be.true;
    expect(toValidate.description.validationModel['name'].valid).to.be.false;
  });

  it('should validate invalid code list that has duplicated name on any language', () => {
    let toValidate = codeLists[0];

    toValidate.descriptions['BG'].name = codeLists[1].descriptions['BG'].name;
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.false;
    expect(toValidate.descriptions['BG'].validationModel.valid).to.be.false;
    expect(toValidate.descriptions['BG'].validationModel['name'].exists).to.be.true;
    expect(toValidate.descriptions['BG'].validationModel['name'].valid).to.be.false;
  });

  it('should perform the same uniqueness validation on code values names for base language', () => {
    let toValidate = codeLists[0];
    toValidate.values[0].isModified = true;
    toValidate.values[0].description.name = codeLists[0].values[1].description.name;

    validationService.validate(codeLists, toValidate);
    expect(toValidate.values[0].validationModel.valid).to.be.false;
    expect(toValidate.values[0].validationModel['id'].valid).to.be.true;
    expect(toValidate.values[0].description.validationModel['name'].exists).to.be.true;
    expect(toValidate.values[0].description.validationModel['name'].valid).to.be.false;
  });

  it('should perform the same uniqueness validation on code values names for any language', () => {
    let toValidate = codeLists[0];
    toValidate.values[0].isModified = true;
    toValidate.values[0].descriptions['BG'].name = codeLists[0].values[1].descriptions['BG'].name;

    validationService.validate(codeLists, toValidate);
    expect(toValidate.values[0].validationModel.valid).to.be.false;
    expect(toValidate.values[0].descriptions['BG'].validationModel.valid).to.be.false;
    expect(toValidate.values[0].descriptions['BG'].validationModel['name'].exists).to.be.true;
    expect(toValidate.values[0].descriptions['BG'].validationModel['name'].valid).to.be.false;
  });

  it('should validate identifiers uniqueness with case insensitivity', () => {
    let toValidate = codeLists[0];
    toValidate.values[0].isModified = true;
    toValidate.values[0].id = 'VAL12';

    validationService.validate(codeLists, toValidate);
    expect(toValidate.values[0].validationModel.valid).to.be.false;
    expect(toValidate.values[0].validationModel['id'].valid).to.be.false;
    expect(toValidate.values[0].validationModel['id'].exists).to.be.true;
  });

  it('should disallow special characters for code list identifiers', () => {
    let toValidate = codeLists[0];
    toValidate.isModified = true;
    toValidate.id = 'листа1';

    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.false;
    expect(toValidate.validationModel['id'].valid).to.be.false;
    expect(toValidate.validationModel['id'].invalidCharacters).to.be.true;
  });

  it('should disallow special characters for code value identifiers', () => {
    let toValidate = codeLists[0];
    toValidate.values[0].isModified = true;
    toValidate.values[0].id = '!@#';

    validationService.validate(codeLists, toValidate);
    assertInvalidCharactersInIdentifier(toValidate.values[0]);

    toValidate.values[0].id = 'кирилицаLatin2';
    validationService.validate(codeLists, toValidate);
    assertInvalidCharactersInIdentifier(toValidate.values[0]);

    toValidate.values[0].id = 'Latin_2*';
    validationService.validate(codeLists, toValidate);
    assertInvalidCharactersInIdentifier(toValidate.values[0]);

    toValidate.values[0].id = 'in^vali=d';
    validationService.validate(codeLists, toValidate);
    assertInvalidCharactersInIdentifier(toValidate.values[0]);

    // And finally allowed chars
    toValidate.values[0].id = '_v.Al/-id+';
    validationService.validate(codeLists, toValidate);
    expect(toValidate.validationModel.valid).to.be.true;
  });

  function assertInvalidCharactersInIdentifier(code) {
    expect(code.validationModel.valid).to.be.false;
    expect(code.validationModel['id'].valid).to.be.false;
    expect(code.validationModel['id'].invalidCharacters).to.be.true;
  }

  function generateString(length) {
    return new Array(length).join('t');
  }
});
