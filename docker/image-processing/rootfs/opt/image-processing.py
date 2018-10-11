#!/usr/bin/env python

import os
import time
import sys
import subprocess
import shutil

SUPPORTED_TYPES = ['image/tiff', 'image/tif', 'image/jpeg', 'image/png', 'image/gif', 'image/bmp','image/x-ms-bmp', 'application/pdf', 'image/x-adobe-dng', 'image/vnd.adobe.photoshop', 'image/jp2', 'image/x-canon-crw','image/x-adobe-dng','image/x-canon-cr2','image/x-nikon-nef','image/x-pentax-raw','image/x-pentax-pef','image/x-sony-sr2','image/x-sony-arw','image/x-fuji-raf','image/x-panasonic-raw2','application/octet-stream', 'image/x-portable-pixmap'];
RAW_TYPES = ['crw', 'dng', 'cr2', 'nef', 'raw', 'pef', 'sr2', 'arw', 'raf', 'raw2', '3fr', 'crf', 'dcr', 'fff', 'iiq', 'kdc', 'mef','mos', 'mrw', 'nrw', 'orf', 'rwl', 'rwz', 'srf', 'srw'];

UPLOADING = ".uploading";
TIMEOUT='300';
ADDITIONAL_PROFILE='/opt/profile.icc';
FAILURE_IMAGE='failed.ptif';
SLEEP_TIME = 1

def main():
	inputDir = defaultString(sys.argv,1,'/data/input/');
	outputDir = defaultString(sys.argv,2,'/var/www/localhost/images/');
	processedDir = defaultString(sys.argv,3,'/data/processed/');
	iccProfilePath = os.path.join(inputDir, 'source') + '.icc'

	# create dirs if necessary
	if not os.path.exists(inputDir):
		os.makedirs(inputDir)

	if not os.path.exists(outputDir):
		os.makedirs(outputDir)

	if not os.path.exists(processedDir):
		os.makedirs(processedDir)

	failedConversions = {};

	print 'Image processing started. Waiting for images';

	while True:
		imageData = getImageForProcessing(inputDir);

		if imageData != None:
			filePath = imageData['path'];
			extension = imageData['extension'];
			mimetype = imageData['mimetype'];
			name = imageData['name'];
			relativePath = imageData['relativePath'];
			# used to store the data whe multi-step processing is needed
			tempFile = os.path.join(inputDir, name + '.temp.' + extension);
			relativeOutputDir = os.path.join(outputDir,imageData['relativePath'])
			outputFilePath = os.path.join(relativeOutputDir, name) + '.ptif';

			# check if the folder of the file exists because the processing libs does not support folder creation
			if not os.path.exists(relativeOutputDir):
				os.makedirs(relativeOutputDir)
				# copy nocontent and inprogress default images
				copy("nocontent.ptif", relativeOutputDir)
				copy("inprogress.ptif", relativeOutputDir)



			processedDirForImage = os.path.join(processedDir, imageData['relativePath'],'' )
			if not os.path.exists(processedDirForImage):
				os.makedirs(processedDirForImage)

			start = time.time();

			print '---------- \nProcessing file "{0}" of type "{1}"'.format(filePath, imageData['mimetype']);
			print('Output file location ' + outputFilePath);

			if mimetype in ['image/tiff', 'image/tif'] and isTilledTiff(filePath):
				print 'The image is tiled tiff - copy as is without processing';
				copy(filePath, outputFilePath);
				move(filePath, processedDirForImage);
				continue;

			if filePath not in failedConversions:
				failedConversions[filePath] = 0;

			# Process profiles only if the additional profile is present
			if additionalProfileExist():
				try:
					extractImageProfile(filePath, iccProfilePath);
					profileExtracted = True;
				except:
					print 'Profile cannot be extracted'
					profileExtracted = False;

				try:
					if profileExtracted:
						applyProfilesToImage(filePath, tempFile, iccProfilePath, ADDITIONAL_PROFILE);
					else:
						applyProfilesToImage(filePath, tempFile, ADDITIONAL_PROFILE);
				except Exception, e:
					print 'ICC profile cannot be applied ' + e.message;
					delete(tempFile);
					tempFile = filePath;

			else:
				tempFile = filePath;

			quality = determineQuality(tempFile);

			try:
				if extension in ['bmp','png','jpg','jpeg','gif'] or mimetype in ['image/jpeg', 'image/png', 'image/bmp', 'image/gif']:
					print '1'
					convertToPTIF(tempFile, outputFilePath, quality);

				elif extension == 'ppm':
					print '2'
					call('convert', '-depth', '8', '-define', 'tiff:tile-geometry=512x512', '-compress', 'LZW', tempFile, outputFilePath);

				elif extension == 'pdf' or mimetype == 'application/pdf':
					print '3'
					call('convert', '-depth', '8', '-define', 'tiff:tile-geometry=512x512', '-compress', 'LZW', tempFile + '[0]', outputFilePath);

				elif extension == 'jp2' or mimetype == 'image/jp2':
					print '4'
					tifImage = os.path.join(inputDir, name) + '.tif';
					try:
						call('j2k_to_image', '-i', tempFile, '-o', tifImage);
						convertToPTIF(tifImage, outputFilePath, quality);
					finally:
						delete(tifImage);

				elif extension in RAW_TYPES or mimetype == 'application/octet-stream':
					print '5'
					tifImage = os.path.join(inputDir, name) + '.tif';
					try:
						call('rawtherapee-cli', '-o', tifImage, '-p', '/opt/default.pp3', '-js3', '-j100', '-t', '-b8', '-Y', '-c', tempFile);
						convertToPTIF(tifImage, outputFilePath, quality);
					finally:
						delete(tifImage);

				else:
					print '6'

					imageInfo = identify(filePath);
					print 'Image Info: ' + imageInfo

					if '16-bit' in imageInfo or '8-bit' in imageInfo:
						try:
							print 'Converting 8/16-bit image "{0}"'.format(filePath);
							# first convert to tif and then to ptif
							convertedTif = outputFilePath.replace('.ptif', '.tif');
							call('convert', tempFile, '-limit', 'memory', '16mb', '-depth', '8', '-define', 'quantum:format=unsigned', convertedTif);
							convertToPTIF(convertedTif, outputFilePath, quality);
						finally:
							delete(convertedTif);
					else:
						convertToPTIF(tempFile, outputFilePath, quality);

			except:
				print 'Error during processing of "{0}"'.format(filePath);
				failedConversions[filePath] = failedConversions[filePath] + 1;

				if failedConversions[filePath] == 3:
					print 'Convertions of "{0}" failed 3 times - using the fallback image';
					if  os.path.isfile(FAILURE_IMAGE):
						copy(os.path.join(outputDir, FAILURE_IMAGE), outputFilePath);
					else:
						print 'WARN: Fallback image {0} not provided'.format(FAILURE_IMAGE);

				else:
					#Retry the processing
					print failedConversions[filePath]
					continue;

			finally:
				delete(iccProfilePath);
				if tempFile != filePath:
					delete(tempFile);

			print 'Finished processing of "{0}" to processed in {1} secs'.format(filePath, round(time.time() - start));

			move(filePath, processedDirForImage);
			del failedConversions[filePath];

		time.sleep(SLEEP_TIME);

def extractImageProfile(filePath, destination):
	#[0] after the file name tells imagemagick to extract only for the first layer
	print 'Extracting image profile'
	call('convert', filePath + '[0]', destination);

	#Sometimes the icc file appears after the process has finished
	while os.path.isfile(destination) == False:
		print 'Waiting for profile to appear';
		time.sleep(0.5);


def call(*parameters):
	return subprocess.check_call(['timeout', TIMEOUT] + list(parameters));

def additionalProfileExist():
	return os.path.isfile(ADDITIONAL_PROFILE);

# Adds a predefined profile (additional profile) to the current set of profiles
# The interesting here is that new profiles are added using -profile and profiles are removed using +profile
def applyProfilesToImage(originalImage, destination, *profiles):
	profilesToAdd = [];
	for profile in profiles:
		if os.path.isfile(profile):
			profilesToAdd.append('-profile');
			profilesToAdd.append(profile);

	if len(profilesToAdd) > 0:
		print 'Applying profiles {0}'.format(profilesToAdd);
		params = ['timeout', TIMEOUT] + ['convert', originalImage, '+profile', '"*"'] + profilesToAdd + [destination];
		return subprocess.check_call(params);

def getImageForProcessing(inputDir):
	for root,folders,files in os.walk(inputDir, topdown=False):
		for fileName in files:
			relativePath = os.path.relpath(root, inputDir);
			filePath = os.path.join(root, fileName);
			extension = getFileExtension(fileName);
			mimetype = getMimeType(filePath);
			typeSupported = (mimetype in SUPPORTED_TYPES) or (extension in RAW_TYPES);
			if isUploaded(filePath) and typeSupported:
				return {
					'path': filePath,
					'relativePath': relativePath,
					'extension': extension,
					'name': getFileWithoutExtension(fileName),
					'mimetype': mimetype
				}

	return None;

def getMimeType(filePath):
	try:
		return subprocess.check_output(['file', '-b', '--mime-type', filePath], stderr=open(os.devnull, 'wb')).strip()
	except Exception, e:
		print 'Cannot get mimetype. Reason: {0}'.format(e.message);
		return '';

def identify(filePath):
	try:
		# [0] means to identify only the first layer (or page in case of pdf)
		imageInfo = subprocess.check_output(['identify', filePath + '[0]'], stderr=subprocess.STDOUT);
	except subprocess.CalledProcessError as exception:
		# sometimes imagemagick says it can't identify image info, but it actually outputs the data in error stream, so we'll try to read it as well
		imageInfo = exception.output;

	# Fallback to 'file' command
	if not imageInfo:
		imageInfo = subprocess.check_output(['file', filePath], stderr=open(os.devnull, 'wb')).strip()

	return imageInfo;

def isTilledTiff(filePath):
	try:
		result = subprocess.check_call(['/opt/tiff-is-tiled', filePath]);
		return result == 0;
	except subprocess.CalledProcessError as exception:
		return False;

def convertToPTIF(source, destination, quality):
	try:
		call('vips', 'tiffsave', source, destination, '--compression', 'jpeg', '--background', '255', '-Q', str(quality), '--tile', '--tile-width', '512', '--tile-height', '512', '--pyramid');
	except Exception, e:
		print 'Error in conversion to PTIF. Reason: {0}'.format(e.message);
		raise e;

def isUploaded(fileName):
	return not fileName.endswith(UPLOADING)

def getFileExtension(fileName):
	lastDotIndex = fileName.rfind('.');
	if lastDotIndex != -1:
		return fileName[lastDotIndex+1:].lower();
	return '';

def getFileWithoutExtension(fileName):
	lastDotIndex = fileName.rfind('.');
	if lastDotIndex != -1:
		return fileName[:lastDotIndex];
	return fileName;

def copy(source, target):
	subprocess.check_call(['cp', source, target]);

def move(source, target):
	subprocess.check_call(['mv', source, target]);

def delete(source):
	subprocess.check_call(['rm', '-f', source]);

def defaultString(array,index,defaultValue):
	if len(array) > index:
  		return array[index];
	return defaultValue;

def getFileSize(path):
	return os.path.getsize(path);

def determineQuality(path):
	if getFileSize(path) > 2097152:
		return 90;
	else:
		return 100;

main()
