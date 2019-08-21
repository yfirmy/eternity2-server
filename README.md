#  ** Eternity II Server **

## Description

The "Eternity II Server" is a hierarchical database service, dedicated to help solving the ["Eternity II" puzzle challenge](https://en.wikipedia.org/wiki/Eternity_II_puzzle):
  - by storing the intermediate results received from the solvers, based on the [Eternity II solver](https://github.com/yfirmy/eternity2-solver), then avoiding to explore again the same blocks;
  - by providing the next "chunks" of the challenge to the available solvers

## The 5 main features

 - Representing the huge search space as a tree-like structure
 - Dividing the search space into smaller size challenges for the solvers to explore, by creating branches in the tree
 - Providing the next "smaller sized" challenges to the available solvers
 - Storing de results received from the solvers 
 - Pruning the explored branches, to avoid a hyper massive tree in the database
 
 ## Technical details
 
 ### Protocol
  - HTTP REST API
  
 ### Backend
  - PostGreSQL database, with LTREE extension
 
 ## How to build the project
 
 This is a Java Spring Boot project 
 
 ## Dependencies
  
 The "Eternity II Server" requires  :
  - a HTTP access to the [Eternity II solver](https://github.com/yfirmy/eternity2-solver) REST API (in order to divide the search space into branches)
  - a JDBC access to a PostGreSQL database
  
 Note: the actual PostGreSQL database is also required for the non-regression Tests Suite.
  
 ## REST API
 
 | GET | PUT |  Path                              | Parameters          | Description                    |
 |-----|-----|------------------------------------|---------------------|--------------------------------|
 | X   |     | /api/eternity2-server/v1/jobs      | size, limit, offset | get the next jobs to solve     |
 |     |  X  | /api/eternity2-server/v1/result    | (JSON Body expected) | store a result                |
 | X   |     | /api/eternity2-server/v1/solutions | limit, offset | get the found solutions       |
 |     |  X  | /api/eternity2-server/v1/status    | (JSON Body expected) | set a PENDING status to a job |
 


