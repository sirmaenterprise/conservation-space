/**
 * Reusable assertion functions for code lists and values.
 */

function assertCodeListHeader(codeList, id, name) {
  let header = codeList.getHeader();
  expect(header.getId()).to.eventually.equal(id);
  expect(header.getName()).to.eventually.equal(name);
}

function assertCodeDetails(details, id, name, comment, extra1, extra2, extra3) {
  expect(details.getId()).to.eventually.equal(id);
  expect(details.getName()).to.eventually.equal(name);
  expect(details.getComment()).to.eventually.equal(comment);
  expect(details.getExtra('1')).to.eventually.equal(extra1);
  expect(details.getExtra('2')).to.eventually.equal(extra2);
  expect(details.getExtra('3')).to.eventually.equal(extra3);
}

function assertCodeValueActiveState(details, active) {
  expect(details.isActive()).to.eventually.equal(active);
}

function assertCodeDescriptions(codeDescription, language, name, comment) {
  expect(codeDescription.getLanguage()).to.eventually.equal(language);
  expect(codeDescription.getName()).to.eventually.equal(name);
  expect(codeDescription.getComment()).to.eventually.equal(comment);
}

function assertCodeValuesPage(values, expectedIds) {
  expect(values.length).to.equal(expectedIds.length);
  values.forEach((value, index) => {
    expect(value.getId()).to.eventually.equal(expectedIds[index]);
  });
}

function assertPreviewMode(code) {
  expect(code.isIdReadonly()).to.eventually.be.true;
  assertCodeEditMode(code, true);
}

function assertEditMode(code) {
  expect(code.isIdReadonly()).to.eventually.be.true;
  assertCodeEditMode(code, false);
}

function assertCreateMode(code) {
  expect(code.isIdReadonly()).to.eventually.be.false;
  assertCodeEditMode(code, false);
}

function assertCodeEditMode(code, readonly) {
  expect(code.isNameReadonly()).to.eventually.equal(readonly);
  expect(code.isCommentReadonly()).to.eventually.equal(readonly);
  expect(code.isExtraReadonly('1')).to.eventually.equal(readonly);
  expect(code.isExtraReadonly('2')).to.eventually.equal(readonly);
  expect(code.isExtraReadonly('3')).to.eventually.equal(readonly);
}

function assertDescriptionEditMode(description, readonly) {
  expect(description.isLanguageReadonly()).to.eventually.be.true;
  expect(description.isNameReadonly()).to.eventually.equal(readonly);
  expect(description.isCommentReadonly()).to.eventually.equal(readonly);
}

module.exports = {
  assertCodeListHeader,
  assertCodeDetails,
  assertCodeDescriptions,
  assertCodeValuesPage,
  assertPreviewMode,
  assertEditMode,
  assertCreateMode,
  assertCodeValueActiveState,
  assertDescriptionEditMode
};