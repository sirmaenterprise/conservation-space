# Compare PDFs service
Service for comparing PDF files.
It uses [diffpdf](https://github.com/taurus-forever/diffpdf-console/) which is a fork before the point the original tool was made commercial.
The fork also adds CLI support, although it still requires an X server or xvfb.

## Configuration
Most configurations are retrieved from environment variables, but also have default values.

* `COMPARE_OUTPUT_DIR` where temporary files and logs are stored. Default `/opt/compare-pdf/output`
* `COMPARE_HOST` the network interface the service listens on. Default `all`
* `COMPARE_PORT` the port on which the service listens on. Default `8080`
* `COMPARE_TIMEOUT` the maximum time in milliseconds the comparison must complete. Default 120000

### Docker specific variables
* `COMPARE_OUTPUT_CLEANER_AGE` the output directory is cleaned using a cron job. The job runs every 15 minutes and looks for files order than `COMPARE_OUTPUT_CLEANER_AGE` minutes. Default `60`

## Usage
The service accepts `POST` requests to `/compare` with the `application/vnd.seip.v2+json` content type header.
The request payload must contain the URLs to the files that need to be compared, e.g.
```json
{
	"first": "http://example.com/file-1.pdf",
	"second": "http://example.com/file-2.pdf"
}
```

After a successful comparison the resulting PDF file is streamed back to the client using the `application/pdf` content type and a `200` status code.
If the comparison takes more than `COMPARE_TIMEOUT` milliseconds a `503` response is sent to the client.
If any other error occur a `500` response is sent.

## Health check
There is a `GET` endpoint at `/health` which checks the health of the service. It's used in the docker container for the health  status of the container.
