TRUNCATE search.tree;

COMMIT;

INSERT INTO search.tree (creation_time, path, tag) VALUES (DEFAULT, '', 'GO');

COMMIT;