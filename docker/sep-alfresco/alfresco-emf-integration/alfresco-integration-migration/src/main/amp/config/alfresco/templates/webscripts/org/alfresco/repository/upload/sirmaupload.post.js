function extractMetadata(file)
{
   // Extract metadata - via repository action for now.
   // This should use the MetadataExtracter API to fetch properties, allowing for possible failures.
   var emAction = actions.create("extract-metadata");
   if (emAction != null)
   {
      // Call using readOnly = false, newTransaction = false
      emAction.execute(file, false, false);
   }
}

function exitUpload(statusCode, statusMsg)
{
   status.code = statusCode;
   status.message = statusMsg;
   status.redirect = true;
   formdata.cleanup();
}

function main()
{
   try
   {
      var filename = null,
         content = null,
         mimetype = null,
         siteId = null, site = null,
         containerId = null, container = null,
         destination = null,
         destNode = null,
         thumbnailNames = null,
         summaryText = null,
         fileauthor = null,
         //format must be "Jan 10, 94"
         filecreateddate = null,
         titlename = null,
         //indicate, that file is from SoftSolution
         softSolutionFlag = false,
         isvssfile = false,
         i;

      // Upload specific
      var uploadDirectory = null,
         title = "",
         contentType = null,
         aspects = [],
         overwrite = true; // If a filename clashes for a versionable file

      // Update specific
      var updateNodeRef = null,
         majorVersion = false,
         description = "";
      	 version = "";
      	 modifiedDate = "";
      	 modifier = "";
      
      // Sirma specific
      var revision = null, 
      	status = null,
      	convert = null;
      
      // Prevents Flash- and IE8-sourced "null" values being set for those parameters where they are invalid.
      // Note: DON'T use a "!==" comparison for "null" here.
      var fnFieldValue = function(p_field)
      {
         return field.value.length() > 0 && field.value != "null" ? field.value : null;
      };

      // allow the locale to be set via an argument
      if (args["lang"] != null)
      {
         utils.setLocale(args["lang"]);
      }

      // Parse file attributes
      for each (field in formdata.fields)
      {
         switch (String(field.name).toLowerCase())
         {
         case "modifier":
        	 modifier = fnFieldValue(field);
        	 break;
         
         case "modifieddate":
        	 modifiedDate = fnFieldValue(field);
        	 break;
         
         case "version":
        	 version = fnFieldValue(field);
        	 break;
         
         case "softsolutionflag":
        	 softSolutionFlag = field.value == "true";
        	 break;
        	 
         case "isvssfile":
        	 isvssfile = field.value == "true";
        	 break;
         
         case "titlename":
        	 titlename = fnFieldValue(field);
        	 break;
         
         case "filecreateddate":
        	 filecreateddate = fnFieldValue(field);
        	 break;
         
         case "fileauthor":
        	 fileauthor = fnFieldValue(field);
        	 break;
         
         case "summarytext":
        	 summaryText = fnFieldValue(field);
        	 break;
         
            case "filedata":
               if (field.isFile)
               {
                  filename = field.filename;
                  content = field.content;
                  mimetype = field.mimetype;
               }
               break;

            case "siteid":
               siteId = fnFieldValue(field);
               break;

            case "containerid":
               containerId = fnFieldValue(field);
               break;

            case "destination":
                 destination = fnFieldValue(field);
               break;

            case "uploaddirectory":
               uploadDirectory = fnFieldValue(field);
               if (uploadDirectory !== null)
               {
                  // Remove any leading "/" from the uploadDirectory
                  if (uploadDirectory.substr(0, 1) == "/")
                  {
                     uploadDirectory = uploadDirectory.substr(1);
                  }
                  // Ensure uploadDirectory ends with "/" if not the root folder
                  if ((uploadDirectory.length > 0) && (uploadDirectory.substring(uploadDirectory.length - 1) != "/"))
                  {
                     uploadDirectory = uploadDirectory + "/";
                  }
               }
               break;

            case "updatenoderef":
               updateNodeRef = fnFieldValue(field);
               break;

            case "filename":
               title = fnFieldValue(field);
               break;

            case "description":
               description = field.value;
               break;

            case "contenttype":
               contentType = field.value;
               break;

            case "aspects":
               aspects = field.value != "-" ? field.value.split(",") : [];
               break;

            case "majorversion":
               majorVersion = field.value == "true";
               break;

            case "overwrite":
               overwrite = field.value == "true";
               break;

            case "thumbnails":
               thumbnailNames = field.value;
               break;
               
            case "revision":
            	revision = fnFieldValue(field);
            	break;
            
            case "status":
            	status = fnFieldValue(field);
            	break;
            	
            case "convert":
            	convert = fnFieldValue(field);
            	break;
         }
      }

      // Ensure mandatory file attributes have been located. Need either destination, or site + container or updateNodeRef
      if ((filename === null || content === null) || (destination === null && (siteId === null || containerId === null) && updateNodeRef === null))
      {
         exitUpload(400, "Required parameters are missing");
         return;
      }

      /**
       * Site or Non-site?
       */
      if (siteId !== null && siteId.length() > 0)
      {
         /**
          * Site mode.
          * Need valid site and container. Try to create container if it doesn't exist.
          */
         site = siteService.getSite(siteId);
         if (site === null)
         {
            exitUpload(404, "Site (" + siteId + ") not found.");
            return;
         }

         container = site.getContainer(containerId);
         if (container === null)
         {
            try
            {
               // Create container since it didn't exist
               container = site.createContainer(containerId);
            }
            catch(e)
            {
               // Error could be that it already exists (was created exactly after our previous check) but also something else
               container = site.getContainer(containerId);
               if (container === null)
               {
                  // Container still doesn't exist, then re-throw error
                  throw e;
               }
               // Since the container now exists we can proceed as usual
            }
         }

         if (container === null)
         {
            exitUpload(404, "Component container (" + containerId + ") not found.");
            return;
         }
         
         destNode = container;
      }
      else if (destination !== null)
      {
         /**
          * Non-Site mode.
          * Need valid destination nodeRef.
          */
         destNode = search.findNode(destination);
         if (destNode === null)
         {
            exitUpload(404, "Destination (" + destination + ") not found.");
            return;
         }
      }

      /**
       * Update existing or Upload new?
       */
      if (updateNodeRef !== null)
      {
         /**
          * Update existing file specified in updateNodeRef
          */
         var updateNode = search.findNode(updateNodeRef);
         if (updateNode === null)
         {
            exitUpload(404, "Node specified by updateNodeRef (" + updateNodeRef + ") not found.");
            return;
         }
         
         if (updateNode.isLocked)
         {
            // We cannot update a locked document
            exitUpload(404, "Cannot update locked document '" + updateNodeRef + "', supply a reference to its working copy instead.");
            return;
         }
         
         if (softSolutionFlag) {      
	           	//upload SoSo file
	      	 	if (titlename == null) {
	      	 		updateNode.properties.title = " "; 
	      	 	} else {
	      	 		updateNode.properties.title = titlename;
	      	 	} 
	      	 	updateNode.properties.comment = summaryText; 
	      	 	updateNode.properties.description = summaryText;
	      	 	/*updateNode.properties["eiso:lastMidifier"] = fileauthor;          		
	      	 	updateNode.properties["eiso:lastModifiedDate"] = new Date(filecreateddate);*/
	         	updateNode.save();
	           } else if (isvssfile) {
	         	//upload vss file
	         	 if (titlename == null) {
	         		 updateNode.properties.title = " "; 
	      	 	} else {
	      	 		updateNode.properties.title = titlename;
	      	 	}   
//	         	 updateNode.properties["eiso:lastModifiedDate"] = new Date(modifiedDate);
//	         	 updateNode.properties["eiso:lastMidifier"] = modifier;
	         	 updateNode.save();
	          }

     
	         if (!updateNode.hasAspect("cm:workingcopy"))
	         {
	            // Ensure the file is versionable (autoVersion = true, autoVersionProps = false)
            	updateNode.ensureVersioningEnabled(true, false);
	
	            if (updateNode.versionHistory == null)
	            {
	               // Create the first version manually so we have 1.0 before checkout
	            	if (isvssfile) {
	            		updateNode.createVersion(description, true);
	            	} else if (softSolutionFlag) {
	            		updateNode.createVersion(summaryText, true);
	            	} else {
	            		updateNode.createVersion("", true);
	            	}
	            }
	
	            // It's not a working copy, do a check out to get the actual working copy           
	            
	            updateNode = updateNode.checkoutForUpload();
	         }
	
	         // Update the working copy content
	         updateNode.properties.content.write(content);
	         // Reset working copy mimetype and encoding
	         updateNode.properties.content.guessMimetype(filename);
	         updateNode.properties.content.guessEncoding();	         
	         // check it in again, with supplied version history note
	         updateNode = updateNode.checkin(description, majorVersion);
	         
	          // Extract the metadata
              // (The overwrite policy controls which if any parts of
         	  //  the document's properties are updated from this)
	          extractMetadata(updateNode);
         
        	 // Record the file details ready for generating the response
	         model.document = updateNode;
   }
      else
      {
         /**
          * Upload new file to destNode (calculated earlier) + optional subdirectory
          */
    	  // Sirma: fixed but when checking for sub directoriess
         if (uploadDirectory !== null && uploadDirectory != "/")
         {
            var temp = destNode.childByNamePath(uploadDirectory);
            if (temp === null)
            {
                var folders = uploadDirectory.split("/");
                for ( var i = 0; i < folders.length; i++) {
					var folderName = folders[i];
					// first check if the folder exists
					var tempNode = destNode.childByNamePath(folderName);
					// if not create new ones
					if (tempNode === null) {
						// creates a sub folders as Sirma folder 
						destNode = destNode.createNode(folderName, "cm:folder");
						// inherit parent permissions
						destNode.setInheritsPermissions(true);
                	} else {
                		// otherwise move to that folder
                		destNode = tempNode;
                	}
				}
            	if (destNode === null){
	              exitUpload(404, "Cannot upload file since upload directory '" + uploadDirectory + "' does not exist.");
	               return;
            	}
            } else {
            	// the folder exists then move reference to that folder
            	destNode = temp;
            }
         }

         /**
          * Existing file handling.
          */
         var existingFile = destNode.childByNamePath(filename);
         if (existingFile !== null)
         {
            // File already exists, decide what to do
            if (existingFile.hasAspect("cm:versionable") && overwrite)
            {
               // Upload component was configured to overwrite files if name clashes
               existingFile.properties.content.write(content);

               // Reapply mimetype as upload may have been via Flash - which always sends binary mimetype
               existingFile.properties.content.guessMimetype(filename);
               existingFile.properties.content.guessEncoding();
               existingFile.save();

               // Extract the metadata
               // (The overwrite policy controls which if any parts of
               //  the document's properties are updated from this)
               extractMetadata(existingFile);

               // Record the file details ready for generating the response
               model.document = existingFile;
               // We're finished - bail out here
               formdata.cleanup();
               return;
            }
            else
            {
               // Upload component was configured to find a new unique name for clashing filenames
               var counter = 1,
                  tmpFilename,
                  dotIndex;

               while (existingFile !== null)
               {
                  dotIndex = filename.lastIndexOf(".");
                  if (dotIndex == 0)
                  {
                     // File didn't have a proper 'name' instead it had just a suffix and started with a ".", create "1.txt"
                     tmpFilename = counter + filename;
                  }
                  else if (dotIndex > 0)
                  {
                     // Filename contained ".", create "filename-1.txt"
                     tmpFilename = filename.substring(0, dotIndex) + "-" + counter + filename.substring(dotIndex);
                  }
                  else
                  {
                     // Filename didn't contain a dot at all, create "filename-1"
                     tmpFilename = filename + "-" + counter;
                  }
                  existingFile = destNode.childByNamePath(tmpFilename);
                  counter++;
               }
               filename = tmpFilename;
            }
         }

         /**
          * Create a new file.
          */
       //  var newFile = destNode.createFile(filename); 
         var newFile;

 		//upload vss file
         if (isvssfile) {
        	 var propaties = {"cm%3Amodified":" ",
        			 		  "cm%3Amodifier":" ",
        			 		  "cm%3Acreated": " ",
        			 		  "cm%3Aauthor":" "}; 
        		propaties["cm%3Amodified"] = new Date(modifiedDate);
        		propaties["cm%3Amodifier"] = modifier;
        		propaties["cm%3Acreated"] = new Date(filecreateddate);
        		propaties["cm%3Aauthor"] = fileauthor;
        		
        		newFile = destNode.createFile(filename, propaties);
        	   	var props = new Array(1);
        	       props["cm:autoVersionOnUpdateProps"] = false;
        	              newFile.addAspect("cm:versionable", props);
        		        		
        		 if (!titlename == null) {
        			newFile.properties.title = titlename;
         	 	} 
//        		newFile.properties["eiso:lastModifiedDate"] = new Date(modifiedDate);
//        		newFile.properties["eiso:lastMidifier"] = modifier;
        	    newFile.properties.comment = description;
        		newFile.save();
         } else if (softSolutionFlag) {
        	  	//upload SoSo file
        	 var propaties = {"cm%3Amodified":" ",
			 		  "cm%3Amodifier":" ",
			 		  "cm%3Acreated": " ",
			 		  "cm%3Aauthor":" "}; 
				propaties["cm%3Amodified"] = new Date(filecreateddate);
				propaties["cm%3Amodifier"] = fileauthor;
				propaties["cm%3Acreated"] = new Date(filecreateddate);
				propaties["cm%3Aauthor"] = fileauthor;
				
				newFile = destNode.createFile(filename, propaties);
			   	var props = new Array(1);
			       props["cm:autoVersionOnUpdateProps"] = false;
			              newFile.addAspect("cm:versionable", props);
				        		
				 if (titlename == null) {
					 newFile.properties.title = " ";
				 } else {
					newFile.properties.title = titlename;
		 	 	} 
//				newFile.properties["eiso:lastMidifier"] = fileauthor;          		
//				newFile.properties["eiso:lastModifiedDate"] = new Date(filecreateddate);
				newFile.properties.comment = summaryText;
		       	newFile.properties.description = summaryText;       		
 	            newFile.save();
        	 
         }else {
        	 newFile = destNode.createFile(filename); 
         }
       
         if (contentType !== null)
         {
            newFile.specializeType(contentType);
         }
         newFile.properties.content.write(content);
                  
         // Reapply mimetype as upload may have been via Flash - which always sends binary mimetype
         newFile.properties.content.guessMimetype(filename);
         newFile.properties.content.guessEncoding();       
         if (convert !== null) {
	         if (convert == "PDF") {
	        	  try {
		        	  var oldFile = newFile;
		        	  newFile = newFile.transformDocument("application/pdf");
		        	  destNode.removeNode(oldFile);
	        	  } catch (e) {
	        		  
	        	  }
	         }
         }
         newFile.save();

         // Create thumbnail?
         if (thumbnailNames != null)
         {
            var thumbnails = thumbnailNames.split(","),
               thumbnailName = "";

            for (i = 0; i < thumbnails.length; i++)
            {
               thumbnailName = thumbnails[i];
               if (thumbnailName != "" && thumbnailService.isThumbnailNameRegistered(thumbnailName))
               {
                  newFile.createThumbnail(thumbnailName, true);
               }
            }
         }

         // Extract metadata - via repository action for now.
         // This should use the MetadataExtracter API to fetch properties, allowing for possible failures.
      	 // Extract the metadata
         extractMetadata(newFile);

         // Set the title if none set during meta-data extract
         newFile.reset();
         // always set the title
         
         if (!softSolutionFlag && !isvssfile) {
		        	 if (title != null && title != "") {
		          		//  && (newFile.properties.title !== null || newFile.properties.title != "")
		          		var temp = newFile.properties.title;
		 	            newFile.properties.title = title;
		 	            // override the description with the file's original title
		 	            // if title exists
		 	            if (temp != null && temp != "") {
		 	            	newFile.properties.description = temp;
		 	            }
		 	            newFile.save();
		       		}          	
         }
         
         if (String(newFile.properties.description).toUpperCase() == "ISO 9001:2008 DEVELOPMENT AND IMPLEMENTATION PLAN")  {
         	newFile.properties.description = " ";
         }
         newFile.save();
     	if (status == "Declared" || siteId == "rm") {
     		// do nothing here.
     	} else {
         	var mldAction = actions.create("add-to-mld");
         	if (mldAction != null){
         		// Call using readOnly = false, newTransaction = false
         		// pass the title to extract the document identifier
         		mldAction.parameters.docid = filename;
     			mldAction.parameters.revision = revision;
     			mldAction.parameters.mldstatus = status;
         		mldAction.execute(newFile, false, false);
         		newFile.save();
         	}
     	}
         	
         // Additional aspects?
         if (aspects.length > 0)
         {
            for (i = 0; i < aspects.length; i++)
            {
            	if (!newFile.hasAspect(aspects[i])){
            		if ("imap:flaggable" == aspects[i]) {
                		// Ensure the file is flagged as seen
            			 var props = new Array(6);
                         props["imap:flagSeen"] = true;
                         props["imap:flagAnswered"] = false;
                         props["imap:flagDeleted"] = false;
                         props["imap:flagDraft"] = false;
                         props["imap:flagRecent"] = false;
                         props["imap:flagFlagged"] = false;
                		newFile.addAspect(aspects[i], props);
                	} else {
                		newFile.addAspect(aspects[i]);
                	}
            	}
            }
         }

         // Record the file details ready for generating the response
         model.document = newFile;
      }
      
      // final cleanup of temporary resources created during request processing
 	 	formdata.cleanup();
   }
   catch (e)
   {
      try
      {
         formdata.cleanup();
      }
      catch (ce)
      {
         // NOTE: ignore
      }

      // capture exception, annotate it accordingly and re-throw
      if (e.message && e.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         e.code = 413;
      }
      else
      {
         e.code = 500;
         e.message = "Unexpected error occurred during upload of new content.";      
      }
      throw e;
   }
}

main();