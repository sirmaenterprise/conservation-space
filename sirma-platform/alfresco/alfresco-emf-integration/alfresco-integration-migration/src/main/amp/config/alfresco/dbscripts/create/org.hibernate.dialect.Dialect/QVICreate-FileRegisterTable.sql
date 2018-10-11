--
-- Title:      Upgrade to v4.2 - Add File Register table.
-- Database:   Generic
-- Since:      v4.2 schema 5025
-- Author:     BBonev
--

CREATE TABLE sirma_file_register
(
   content_crc VARCHAR(40) NOT NULL,
   source_path TEXT NOT NULL,
   destination_path TEXT,
   file_name VARCHAR(100) NOT NULL,
   dest_file_name VARCHAR(100),
   status SMALLINT NOT NULL,
   modified_by VARCHAR(50),
   modified_date DATETIME NOT NULL,
   node_id VARCHAR(500),
   UNIQUE INDEX idx_sirma_fregister_crc (content_crc),
   UNIQUE INDEX idx_sirma_fregister_node_id (node_id),
   PRIMARY KEY (content_crc)
);

--
-- Record script finish
--
DELETE FROM alf_applied_patch WHERE id = 'patch.db-v4.2-create-file-register';
INSERT INTO alf_applied_patch
  (id, description, fixes_from_schema, fixes_to_schema, applied_to_schema, target_schema, applied_on_date, applied_to_server, was_executed, succeeded, report)
  VALUES
  (
    'patch.db-v4.2-create-file-register', 'Manually executed script upgrade v4.2',
     0, 5024, -1, 10000, null, 'UNKNOWN', ${TRUE}, ${TRUE}, 'Script completed'
   );
