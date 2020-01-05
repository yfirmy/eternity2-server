
-- Recycling old pending jobs

UPDATE search.tree SET tag = 'GO' WHERE
        path in ( select pending_job from search.pending ) AND tag = 'PENDING';

TRUNCATE search.pending;