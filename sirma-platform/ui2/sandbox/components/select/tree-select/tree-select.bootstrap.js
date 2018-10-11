import {Component, View, Inject} from 'app/app';
import 'components/select/tree-select/tree-select';

import template from './tree-select.bootstrap.html!text';

@Component({
  selector: 'seip-tree-select-bootstrap'
})
@View({
  template: template
})
export class TreeSelectBootstrap {
  
  constructor() {
    let data = [{
      id: 'concrete', text: 'Concrete', children: [{
        id: 'reinforced', text: 'Reinforced', children: [{
          id: 'fiber', text: 'Fiber'
        },
        {
          id: 'rebar', text: 'Rebar'
        }]
      },
      {
        id: 'prestressed', text: 'Prestressed'
      }]
    }, 
    {
      id: 'metal', text: 'Metal', children: [{
        id: 'steel', text: 'Steel', children: [{
          id: 'hot_rolled', text: 'Hot rolled'
        },
        {
          id: 'cold_formed', text: 'Cold formed'
        }]
      },
      {
        id: 'aluminium', text: 'Aluminium'
      }]
    }];

    this.treeSelectConfig = {
      data: data,
      multiple: true
    };
  }
  
}