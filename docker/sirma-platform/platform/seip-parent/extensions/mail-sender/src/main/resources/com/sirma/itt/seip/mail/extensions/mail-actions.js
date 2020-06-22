// Should contain functions related to mail sending operations from server side JavaScript
/**
 * Create mail sending context associated with the current instance in the scripts.
 * @returns {*}
 */
function createMail() {
    return mail.createNew(root);
}
