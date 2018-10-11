import {RemoveWhitespaces} from 'filters/remove-whitespaces';

describe('Tests for RemoveWhitespaces filter', () => {
  it('Test that filter() removes all whitespaces from input string', () => {
    let removeWhitespaces = new RemoveWhitespaces();
    let result = removeWhitespaces.filter('Test string   with      multiple      different whitespaces.');
    expect(result).to.equal('Teststringwithmultipledifferentwhitespaces.');
  });
});
