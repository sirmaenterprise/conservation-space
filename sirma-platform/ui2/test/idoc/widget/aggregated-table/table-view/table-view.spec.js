import {TableView} from 'idoc/widget/aggregated-table/table-view/table-view';

describe('TableView', function () {

  it('should return concatenated comma separated string', () => {
    let tableView = new TableView();
    let tableHeader = {name: 'property3', labels: ['GEP111111 Property 3', 'GEP100002 Property 3']};
    expect(tableView.getColumnHeaderLabel(tableHeader)).to.equal('GEP111111 Property 3, GEP100002 Property 3');
  });

});