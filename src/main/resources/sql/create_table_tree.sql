-- Eternity II Solution Tree and Results Database Creation Script --

create type search.action as enum ( 'DONE', 'GO', 'PENDING' );

create table search.tree
(
  path ltree PRIMARY KEY,
  tag search.action,
  creation_time TIMESTAMP WITH TIME ZONE default current_timestamp
);

create table search.results
(
  path ltree PRIMARY KEY,
  creation_time TIMESTAMP WITH TIME ZONE default current_timestamp
);

create table search.pending
(
    pending_job ltree NOT NULL,
    solver_name text,
    solver_ip inet NOT NULL,
    solver_version text,
    solver_machine_type text,
    solver_cluster_name text,
    solver_score double precision,
    solving_start_time TIMESTAMP WITH TIME ZONE default current_timestamp
);

create index tree_path_idx on search.tree using gist (path);
