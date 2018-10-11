'use strict';

const express = require('express');
const bodyParser = require('body-parser');
const path = require('path');
const timeout = require('connect-timeout');
const http = require('http');
const https = require('https');
const fs = require('fs');
const exec = require('child_process');
const url = require('url');
const mime = require('mime-types');
const FileCleaner = require('cron-file-cleaner').FileCleaner;
const uuid = require('uuid/v4')
const config = require('./config.json');

// Setup the console to append timestamp automatically.
require('console-stamp')(console, {pattern: "dd/mm/yyyy HH:MM:ss.l"});

let resilientMode = true;
// RegExp that matches a SEP instance id including revision and version identifiers
let emfIdRegexp = 'emf:[a-zA-Z0-9-\.]{36,60}';

const expressApp = express();
// Create a router to which to attach the services.
const compareRouter = express.Router();

expressApp.use(timeout(config.timeout, {
    // skip automatic processing of timeout error and handle it manually in the application
    respond: false
}));

// Do automatic body parsing.
expressApp.use(bodyParser.json({type: 'application/vnd.seip.v2+json'}));

// Add static path to serve the empty pdf used for healthcheck
expressApp.use('/static', express.static(path.join(__dirname, 'public')));

/**
 * Starts listening on a given host and port as configured in config.json.
 */
expressApp.listen(config.port, config.host, () => {
    console.info("Compare pdf server listening at %s:%s", config.host, config.port);
    expressApp.use('/', compareRouter);

    // Ignore ssl errors.
	// TODO: If it's possible download and accept the certificate.
	process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";
});

/**
 * Compares two pdf files. The service expects two downloadable URLs. This service returns as a result only the fileName
 * of the generated compare. The corresponding API is then responsible for calling the download service with this file
 * name.
 */
compareRouter.post('/compare', (req, res) => {
    console.info("POST was sent to pdf compare service with body: %s", JSON.stringify(req.body));
    // the request has two URLs in the payload which we use to download the actual files we want to compare.

    let firstFilePromise = downloadFileFromSep(req, req.body.firstURL);
    let secondFilePromise = downloadFileFromSep(req, req.body.secondURL);

    Promise.all([firstFilePromise, secondFilePromise]).then(files => {
        let resultFileName = config.tempFolder + extractEmfId(req.body.firstURL)
            + extractEmfId(req.body.secondURL) + '.pdf';
        return compare(files[0], files[1], resultFileName);
    }).then(result => {
        deleteFile(result.firstFile.replace(config.tempFolder, ""));
        deleteFile(result.secondFile.replace(config.tempFolder, ""));

        let fileInfo = fs.statSync(result.resultFile);
        let response = {
            "fileName": result.resultFile.replace(config.tempFolder, ""),
            "fileSize": fileInfo.size,
            "mimetype": mime.lookup(result.resultFile)
        };
        res.status(200).send(JSON.stringify(response));
    }).catch(err => {
        res.status(500).send(`{"error" : "Could not execute compare", "cause": "${err}"}`);
    });
});

/*
 * Writes a file in the temp folder given a downloadable (SEP) url.
 */
function downloadFileFromSep(req, url) {
    return new Promise((fulfil, reject) => {
        let fileName = config.tempFolder + extractEmfId(url) + '.pdf';

		// Check if the file exists in the current directory.
		// if not it will be downloaded otherwise it will be reused
		fs.access(fileName, fs.constants.F_OK, (err) => {
			if (err) {
				console.info("Starting file download for %s", fileName);

				let writeStream = fs.createWriteStream(fileName);
				writeStream.on('error', (error) => {
					throw error;
				});

				let options = buildOptions(req, url);
				const service = options.protocol === 'https:' ? https : http;
				let request = service.request(options, (res) => {
					res.pipe(writeStream);
				});
				// here we check if the file was actually created.
				writeStream.on('finish', () => {
					if (!fileName || fs.statSync(fileName).size === 0) {
						let error = Error('Could not write file or file was corrupted.');
						reject(error);
						throw error;
					} else {
						fulfil(fileName);
					}
				});
				request.end();
			} else {
				console.info("Reusing already downloaded %s", fileName);
				fulfil(fileName);
			}
		});
    })
}

/**
 * Builds options for sending an http GET to the calling SEP in order to download a file for compare.
 * @param req the original export request, we use it to get the cookie.
 * @param urlLocation the SEP url as a string.
 * @returns {{host: string, port: (*|Function|string), path: (*|string|string|String), method: string,
 *      headers: {Authorization: *}}}
 */
function buildOptions(req, urlLocation) {
    let authorization = req.headers.authorization;
    let cookie = req.headers.cookie;

    console.info("Trying to download file from %s", urlLocation);
    let parsedUrl = url.parse(urlLocation);

    let options = {
    	protocol: parsedUrl.protocol,
        host: parsedUrl.hostname,
        path: parsedUrl.path,
		// ignore ssl error lolz
		rejectUnauthorized: false,
        method: 'GET',
        headers: {
            'Authorization': authorization,
            'Cookie': cookie
        }
    };

    if (parsedUrl.port) {
    	options.port = parsedUrl.port;
	}

    console.info("Constructed request for download: ", JSON.stringify(options));
    return options;
}

function extractEmfId(emfUrl) {
  if (emfUrl.endsWith('healthcheck.pdf')) {
    return uuid();
  }

  let m = emfUrl.match(emfIdRegexp);
  // CMF-29377 - either diffpdf or node doesn't like colons (:) in the filename causing the request to hang
  return m[0].replace(':', '_');
}

/**
 * Compares two pdf files that are saved in the temp folder using the diffpdf tool.
 *
 * @param firstFile the file path to existing file for comparison.
 * @param secondFile the file path to existing file for comparison.
 * @param resultFileName result file name is constructed using the emf IDs of the instances, so that we can schedule it
 *      from SEP for deletion or we can reuse it if same files are compared multiple times withing short interval.
 */
function compare(firstFile, secondFile, resultFileName) {
    let command = `diffpdf ${firstFile} ${secondFile} ${resultFileName}`;
    console.info("Executing %s", command);
    return new Promise((fulfil, reject) => {
        exec.exec(command, function (error, stdout, stderr) {
            if (error !== null) {
                console.error("Could not compare %s and %s", firstFile, secondFile);
                reject(error);
                throw error;
            } else {
                console.info("Successfully compared %s and %s result is saved to %s", firstFile, secondFile, resultFileName);
                let result = {
                    firstFile : firstFile,
                    secondFile : secondFile,
                    resultFile : resultFileName
                };
                fulfil(result);
            }
        });
    })
}

function deleteFile(fileName) {
    return new Promise((fulfil, reject) => {
        fs.unlink(config.tempFolder + fileName, (err) => {
            if (err) {
                console.error("Could not delete %s", fileName);
                reject(err);
            } else {
                fulfil();
                console.info("Successfully deleted %s", fileName);
            }
        })
    });
}

/**
 * Streams the content of a file by its name.
 */
compareRouter.get('/compare', (req, res) => {
    let fileLocation = config.tempFolder + req.query.id;
    if (fs.existsSync(fileLocation)) {
        res.sendFile(fileLocation);
    } else {
        res.status(204).send();
    }
});

/**
 * Deletes a a file by its name.
 */
compareRouter.delete('/compare', (req, res) => {
    deleteFile(req.query.id).then(() => {
        res.status(200).send(`[${new Date().toISOString()}] Successfully deleted ${req.query.id}`).end();
    }).catch(err => {
        res.status(404).send(`{"error" : "Could not delete ${req.query.id}", "cause": ${err}`).end();
        throw err;
    });
});

/**
 * Rest service used to check the health status of the compare service.
 */
compareRouter.get('/health', (req, res) => {
    res.status(200).end();
});

/**
 * Remove files downloaded from SEP which are older than 10 minutes every hour. This is activated if only specified
 * in the config. SEP should handle the deletion of files that are created as a result from the compare. Using the
 * scheduler and DELETE.
 */
let fileWatcher = new FileCleaner(config.tempFolder, 600000, config.tempFolderCleanCron, {
    start: config.cleanTempInternally,
    // If for some reason the deletion of input files for compare fails then delete those.
    whitList: emfIdRegexp
});

process.on('exit', (code) => {
    console.info("Application exit: %s", code);
});

process.on('uncaughtException', (err) => {
    if (resilientMode) {
        console.error(err);
    } else {
        throw err;
    }
});
