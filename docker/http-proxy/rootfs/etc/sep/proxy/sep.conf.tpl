server {
    listen 80 default_server;
    listen [::]:80 default_server;

    # Redirect all HTTP requests to HTTPS with a 301 Moved Permanently response.
    return 301 https://$host$request_uri;
}

server {
	listen 443 ssl http2;
	listen [::]:443 ssl http2;

	server_name ${NGINX_SERVER_NAME};

	ssl_certificate /etc/nginx/ssl/nginx.crt;
	ssl_certificate_key /etc/nginx/ssl/nginx.key;

	ssl_session_timeout 1d;
	ssl_session_cache shared:SSL:50m;
	ssl_session_tickets off;
	ssl_dhparam /etc/nginx/ssl/dhparam.pem;
	ssl_protocols ${NGINX_SSL_PROTOCOLS};
	ssl_ciphers '${NGINX_SSL_CIPHERS}';
	ssl_prefer_server_ciphers on;

	client_max_body_size ${NGINX_MAX_REQUEST_SIZE};
	client_body_buffer_size 1M;
	client_body_temp_path /tmp/nginx 1 2;

	proxy_connect_timeout 160s;
	proxy_send_timeout 600s;
	proxy_read_timeout 600s;
	proxy_buffering off;

	add_header Strict-Transport-Security max-age=15768000;
	add_header X-Frame-Options "SAMEORIGIN";

	charset utf-8;

{{if index . "ui.enabled"}}
	location / {
		proxy_redirect {{index . "idp.address"}}/ https://$http_host/sso/;
		proxy_redirect {{index . "ui.address"}}/ https://$http_host/;

		proxy_pass {{index . "ui.address"}};
	}

	{{if index . "ui.browserSync.enabled"}}
	location /browser-sync/ {
		proxy_pass {{index . "ui.address"}};
		proxy_http_version 1.1;
		proxy_set_header Upgrade $http_upgrade;
		proxy_set_header Connection "Upgrade";
	}
	{{end}}
{{end}}

{{if index . "sep.enabled"}}
	location /remote {
		client_max_body_size ${NGINX_MAX_REQUEST_SIZE};
		client_body_buffer_size 1M;

		proxy_buffering off;
		proxy_ignore_client_abort on;

		proxy_connect_timeout ${NGINX_PROXY_TIMEOUT};
		proxy_send_timeout ${NGINX_PROXY_TIMEOUT};
		proxy_read_timeout ${NGINX_PROXY_TIMEOUT};

		proxy_redirect {{index . "idp.address"}}/ https://$http_host/sso/;
		proxy_redirect {{index . "sep.address"}}/emf https://$http_host/remote;
		proxy_redirect {{index . "sep.address"}} https://$http_host;

		proxy_cookie_path /emf /remote;

		sub_filter_types application/x-java-jnlp-file application/json;
		sub_filter '{{index . "sep.address"}}/emf' 'https://$http_host/remote';
		sub_filter '{{index . "idp.address"}}/' 'https://$http_host/sso/';
		sub_filter_once off;

		proxy_pass {{index . "sep.address"}}/emf;
	}
{{end}}

{{if index . "idp.enabled"}}
	location /sso {
		# this is needed for sub_filter directive to work
		proxy_set_header Accept-Encoding "";

		proxy_redirect {{index . "sep.address"}}/emf https://$http_host/remote;
		proxy_redirect {{index . "idp.address"}}/ https://$http_host/sso/;

		proxy_cookie_path / /sso/;

		# login page resource and form action rewrites
		sub_filter 'href="/' 'href="/sso/';
		sub_filter 'src="/' 'src="/sso/';
		sub_filter '<form action="../../commonauth"' '<form action="/sso/commonauth"';

		# we submit a form ...fix the action
		sub_filter '{{index . "sep.address"}}/emf' 'https://$http_host/remote';
		sub_filter '{{index . "alfresco.address"}}' '{{index . "hosts.internal.address"}}';
		sub_filter_once off;

		# the trailing slash here is !important - it means /sso from the location above will not be included
		proxy_pass {{index . "idp.address"}}/;
	}
{{end}}

{{if index . "iiif.enabled"}}
	location /iiif {                                     
		sub_filter_types application/ld+json;
		sub_filter '{{index . "iiif.address"}}/fcgi-bin/' 'https://$http_host/iiif/fcgi-bin/';
		sub_filter_once off;                                     
		                                              
		proxy_pass {{index . "iiif.address"}}/;                                 
	}
{{end}}

	gzip ${GZIP_ENABLED};
	gzip_min_length 1000;
	gzip_comp_level 5;
	gzip_proxied any;
	gzip_vary on;
	gzip_types text/plain
	  text/css
	  application/json
	  application/javascript
	  text/javascript
	  font/opentype
	  application/x-font-ttf
	  application/vnd.seip.v2+json;

	server_tokens off;
	proxy_hide_header X-Powered-By;
}

server {
	listen 8080;

	server_name _;

{{if index . "alfresco.enabled"}}
	location /alfresco {
		proxy_redirect {{index . "idp.address"}}/ {{index . "hosts.external.address"}}/sso/;
		proxy_redirect {{index . "alfresco.address"}} {{index . "hosts.internal.address"}};

		proxy_pass {{index . "alfresco.address"}};
	}
{{end}}

{{if index . "graphdb.enabled"}}
	location /graphdb/ {
		proxy_set_header Accept-Encoding "";
		sub_filter '<base href="/" target="_blank"/>' '<base href="/graphdb/" target="_blank"/>';
		sub_filter_once off;

		proxy_pass {{index . "graphdb.address"}}/;
	}
{{end}}

{{if index . "solr.core.enabled"}}
	location /solr/core/ {
		proxy_pass {{index . "solr.core.address"}}/solr/;
	}
{{end}}

{{if index . "solr.audit.enabled"}}
	location /solr/audit/ {
		proxy_set_header Accept-Encoding "";

		proxy_pass {{index . "solr.audit.address"}}/solr/;

		sub_filter "app_config.solr_path = '\/solr';" "app_config.solr_path = '\/solr\/audit';";
	}
{{end}}
}
