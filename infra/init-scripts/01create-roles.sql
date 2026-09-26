CREATE ROLE create_url_owner WITH LOGIN PASSWORD 'ownerpass';
GRANT ALL PRIVILEGES ON url_mapping TO create_url_owner;
GRANT USAGE, SELECT ON SEQUENCE url_mapping_id_seq TO create_url_owner;

CREATE ROLE redirect_reader WITH LOGIN PASSWORD 'readerpass';
GRANT SELECT ON url_mapping TO redirect_reader;