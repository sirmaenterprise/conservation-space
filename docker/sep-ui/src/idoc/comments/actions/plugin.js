PluginRegistry.add('actions', [{
  name: 'editCommentAction',
  module: 'idoc/comments/actions/edit-comment'
}, {
  name: 'replyCommentAction',
  module: 'idoc/comments/actions/reply'
},
{
  name: 'deleteCommentAction',
  module: 'idoc/comments/actions/delete-comment'
},
{
  name: 'suspendCommentAction',
  module: 'idoc/comments/actions/suspend-comment'
},
{
  name: 'restartCommentAction',
  module: 'idoc/comments/actions/restart-comment'
}]);
