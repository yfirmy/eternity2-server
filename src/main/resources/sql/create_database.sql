
-- Database: eternity2_challenge

-- #
-- # su - postgres
-- # pgsgl
-- #

CREATE DATABASE eternity2_challenge
    WITH
    OWNER = e2server_challenge
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE eternity2_challenge TO e2server_challenge;

---

CREATE DATABASE eternity2_clue1
    WITH
    OWNER = e2server_clue1
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE eternity2_clue1 TO e2server_clue1;

---

CREATE DATABASE eternity2_sample
    WITH
    OWNER = e2server_sample
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE eternity2_sample TO e2server_sample;

--

CREATE DATABASE eternity2_test
    WITH
    OWNER = e2server_test
    ENCODING = 'UTF8'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

GRANT ALL ON DATABASE eternity2_test TO e2server_test;


-- DROP DATABASE eternity2_challenge;