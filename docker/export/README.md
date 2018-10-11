# Export to PDF service

The export service uses [express](https://expressjs.com/) to listen for export requests and [puppeteer](https://github.com/GoogleChrome/puppeteer) to launch a headless chromium instance and to control it to export the given page to PDF. After a successful export the pdf file is streamed back to the caller.

## Configuration

The service is configured using environment variables w/ defaults provided by the [config](lib/config.js).

### Service Variables

`EXPORT_HOST` the network interface on which the service listens, default is empty string, meaning - listen on all interfaces
`EXPORT_PORT` the port on which the service listens on, default 8080
`CHROMIUM_PATH` path to chromium/chrome executable
`EXPORT_VIEWPORT_WIDTH` and `EXPORT_VIEWPORT_HEIGHT` control the viewport size, default 1360x1020
`EXPORT_OUTPUT_DIR` where chromium user data directories and exported pdf files are placed, default `\<service work dir>/output`

### Docker specific variables

`EXPORT_OUTPUT_CLEANER_AGE` max age of file in output directory in minutes, files and directories older than that are deleted by a cron job, default 60

## Export request payload

The service expects post requests at `/export/pdf`.
The body of the request must be a json object which includes the `url` to the page that has to be exported and optionally a `timeout` in milliseconds.
If the request exceeds the specified timeout the export request is aborted and a 503 status code is returned. Default timeout is `120000`.

Example:
```json
{
	"url": "https://sirma.bg",
	"timeout": 30000
}
```

Export `https://sirma.bg`, but wait at most 30 seconds (30000 milliseconds) for the export to finish successfully.

## Health check

Simply responds with status 200 at `/export/health`.
