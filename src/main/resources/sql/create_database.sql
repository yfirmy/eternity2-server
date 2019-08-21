
-- Database: eternity2_prod

-- DROP DATABASE eternity2_prod;

CREATE DATABASE eternity2_prod
    WITH 
    OWNER = e2server
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE eternity2_prod TO e2server;

-- Database: eternity2_test

-- DROP DATABASE eternity2_test;

CREATE DATABASE eternity2_test
    WITH
    OWNER = e2server_test
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE eternity2_test TO e2server_test;