import {CommentContentPanel} from 'idoc/comments/comment-content-panel/comment-content-panel';
import {ReloadCommentsEvent} from 'idoc/comments/events/reload-comments-event';
import {ReloadRepliesEvent} from 'idoc/comments/events/reload-replies-event';
import {CommentContentDialogClosedEvent} from 'idoc/comments/events/comment-content-dialog-closed-event';
import {CommentsHelper} from 'idoc/comments/comments-helper';
import 'common/lib/textcomplete/textcomplete';
import emoji from 'node-emoji';
import _ from 'lodash';

const USER_ICON_NAME = 'user';
const USER_ICON_SIZE = 16;

export class CommentContentDialog {

  constructor(dialogService, eventbus, dataProvider, resourceRestService, iconsService) {
    this.dialogService = dialogService;
    this.eventbus = eventbus;
    this.dataProvider = dataProvider;
    this.resourceRestService = resourceRestService;
    this.iconsService = iconsService;
    this.defaultUserIcon = this.getDefaultUserIcon();
  }

  /**
   * Opens the comment dialog. It is used for replying to an existing comment, editing it or creating a new one.
   *
   * @param config the configuration object consisted of the following:
   *      - comment:  a CommentInstance with the comment that will be edited
   *      - currentObject: the currentObject that will be commented (context.getCurrentObject())
   *      - tabId: the id of the tab that will be commented
   *      - replyTo: the id of the comment that will be replied
   *      - widgetId: the id of the widget on which is the comment
   */
  createDialog(config) {
    let buttons = this.getDialogButtons(config);
    let dialogConfig = this.getDialogConfig(config);
    dialogConfig.buttons = buttons;
    dialogConfig.modeless = true;
    dialogConfig.onShow = () => {
      //The comments dialog should be resized depending on the dimensions of the viewport.
      let width = $(window).width() / 3;
      CommentContentPanel.setFocus();
      //Sets the dimensions of the dialog.
      $('.modal-content').css({width: width});
    };
    this.dialogService.create(CommentContentPanel, this.createPropertyDefinition(config.comment, buttons), dialogConfig);
    this.matchUserMentionOrEmoji();
  }

  matchUserMentionOrEmoji() {
    $('.with-mention-support').textcomplete([
      {
        //mention strategy matches the symbol (@) a.k.a at
        match: /(^|\s)@(.*)$/,
        search: (term, callback) => {
          let searchTerm = {
            includeUsers: true,
            includeGroups: false,
            limit: 10,
            term: term
          };
          this.resourceRestService.getResources(searchTerm).then((response) => {
            callback($.map(response.data.items, function (user) {
              if (user.label) {
                return user.label.toLowerCase().indexOf(term.toLowerCase()) !== -1 ? user : null;
              } else {
                return null;
              }
            }));
          });
        },
        replace: (value) => {
          return ` <a href="#/idoc/${value.id}" class="instance-link" target="_blank"><span data-mention-id="${value.id}">${value.label}</span></a>`;
        },
        template: (value) => {
          return `<img src="${this.defaultUserIcon}"/><b>${value.label}</b>`;
        },
        cache: true
      },

      {
        // emoji strategy matches the symbol (:) a.k.a colon
        match: /(^|\s):(\w*)$/,
        search: function (term, callback) {
          let cache = ['smile', 'laughing', 'smiling_imp', 'wink', 'weary', 'angry', 'cry', 'beer', 'v', 'clock10'];
          callback($.map(cache, function (emo) {
            return emo.indexOf(term) === 0 ? emoji.get(emo) : null;
          }), true);

          callback($.map(Object.keys(emoji.emoji), function (emo) {
            return emo.indexOf(term) === 0 ? emoji.get(emo) : null;
          }));
        },
        replace: function (value) {
          CKEDITOR.instances[CommentContentPanel.EDITOR_SELECTOR].fire('change');
          return value;
        },
        template: function (value) {
          return value + ' ' + emoji.which(value);
        },
        cache: true
      }
    ], {zIndex: 1120, debounce: 500});
  }

  getDefaultUserIcon() {
    return this.iconsService.getIconForInstance(USER_ICON_NAME, USER_ICON_SIZE);
  }

  getDialogButtons(config) {
    let dismissOnSave = true;
    if (config.options && config.options.dialogConfig && !config.options.dialogConfig.dismissOnSave) {
      dismissOnSave = config.options.dialogConfig.dismissOnSave;
    }
    return [
      {
        id: CommentsHelper.SAVE_ACTION_ID,
        label: 'comments.create.dialog.button.save',
        cls: 'btn-primary',
        dismiss: dismissOnSave,
        disabled: true,
        onButtonClick: (buttonId, componentScope) => {
          let commentContentPanel = componentScope.commentContentPanel;

          if (config.comment) {
            config.comment.setMentionedUsers(commentContentPanel.getMentionedUsers());
            config.comment.setMotivation(['oa:editing']);
            config.comment.setDescription(commentContentPanel.getCommentContent());
            config.instanceId = config.comment.getId();
            config.content = config.comment.getComment();
            config.comment.removeActions();
            this.dataProvider.updateComment(config).then(() => {
              CommentsHelper.reloadComments(config.comment.isReply(), config.tabId, config.widgetId, this.eventbus);
              if (typeof config.onClose === 'function') {
                config.onClose(buttonId);
              }
            });
          } else {
            config.content = commentContentPanel.getCommentContent();
            config.mentionedUsers = commentContentPanel.getMentionedUsers();
            config.instanceId = config.currentObject.getId();
            this.dataProvider.createComment(config).then(() => {
              CommentsHelper.reloadComments(config.replyTo, config.tabId, config.widgetId, this.eventbus);
              if (typeof config.onClose === 'function') {
                config.onClose(buttonId);
              }
            });
          }
        }
      },
      {
        id: CommentsHelper.CANCEL_ACTION_ID,
        label: 'comments.create.dialog.button.cancel',
        dismiss: true,
        onButtonClick: (buttonId) => {
          if (typeof config.onClose === 'function') {
            config.onClose(buttonId);
          }
        }
      }
    ];
  }

  getDialogConfig(config) {
    let header = 'comments.create.dialog.header';
    if (config.comment) {
      header = 'comments.edit.dialog.header';
    }
    if (config.replyTo) {
      header = 'comments.action.reply';
    }
    if (config.comment && config.comment.isReply()) {
      header = 'comments.edit.reply.header';
    }


    return {
      header: header,
      showClose: true,
      onClose: () => {
        //Textcomlete Leaking Event Handler #287 https://github.com/yuku-t/jquery-textcomplete/issues/287
        //The event handler carries a reference to dropdownViews which prevents it from being garbage collected by the browser.
        //So we have to remove them manually on CKEditor close.
        //The author @yuku-t added the enhancement label on Sep 12 2016
        $('.dropdown-menu.textcomplete-dropdown').remove();

        if (!config.widgetId || !config.options) {
          return;
        }
        this.eventbus.publish(new CommentContentDialogClosedEvent(config.widgetId, config.options));
      }
    };
  }

  createPropertyDefinition(comment, buttons) {
    return {
      config: {
        dialog: {
          buttons
        }
      },
      comment
    };
  }
}