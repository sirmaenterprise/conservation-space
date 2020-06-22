function showhide(id) {
    var e = document.getElementById(id);
    e.style.display = (e.style.display == 'block') ? 'none' : 'block';
}

/**
 * Get an url parameter from the current page's url.
 *
 * @param param
 *            the param to look for
 */
function getUrlParameter(param) {
    var url = decodeURIComponent(window.location.search.substring(1));
    var variables = url.split('&');
    var paramName;
    var i;

    for (i = 0; i < variables.length; i++) {
        paramName = variables[i].split('=');

        if (paramName[0] === param) {
            return paramName[1] === undefined ? true : paramName[1];
        }
    }
}

function Init(controllerName) {

    /**
     * Authentication specific for Keycloak.
     */
    function KeycloakAuth() {

        let keycloakAdapter = new Keycloak({
            url: '/auth',
            realm: 'master',
            clientId: 'security-admin-console'
        });

        function init() {
            return keycloakAdapter.init({onLoad: 'login-required'}).then(() => {
                var intervalId = setInterval(() => {
                    keycloakAdapter.updateToken(20);
                }, 45000);
                window.addEventListener('beforeunload', () => clearInterval(intervalId));

                $.ajaxPrefilter(function (options, originalOptions, jqXHR) {
                    jqXHR.setRequestHeader('Authorization', 'Bearer ' + keycloakAdapter.token);
                });

                return true;
            });
        }

        function logout() {
            keycloakAdapter.logout();
        }

        return {
            init: init,
            logout: logout
        };

    }

    let authentication = new KeycloakAuth();
    authentication.init().then(() => {
        // after successfully authenticated, init logic for the pages
        switch (controllerName) {
            case 'create':
                new CreateController();
                break;
            case 'update':
                new UpdateController();
                break;
            case 'manage':
                new ManageController();
                break;
            default:
                throw new Error('Received unsupported controller name: ' + controllerName);
        }
    }).catch(console.error);

    $("#menu-create-tenant").click(function () {
        openPage("./index.html");
    });

    $("#menu-update-tenant").click(function () {
        openPage("./update.html");
    });

    $("#menu-manage-tenant").click(function () {
        openPage("./manage.html");
    });

    $("#logout").click(function () {
        authentication.logout();
    });

    /**
     * Opens the given page address in the same window but before that appends the security token to the address.
     * Used for menu actions
     *
     * @param page
     *            the page to open
     */
    function openPage(page) {
        window.open(page, '_self');
    }

}