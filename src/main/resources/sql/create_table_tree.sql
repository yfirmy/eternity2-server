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

create index tree_path_idx on search.tree using gist (path);
