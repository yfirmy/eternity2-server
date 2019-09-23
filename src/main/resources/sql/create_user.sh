#!/bin/bash

su - postgres

createuser --pwprompt e2server_challenge
createuser --pwprompt e2server_clue1
createuser --pwprompt e2server_sample

# in file pg_hba.conf
# add the following lines, in the "IPV4 local connections" section:

host e2server_challenge all     127.0.0.1/32    md5
host e2server_clue1     all     127.0.0.1/32    md5
host e2server_test      all     127.0.0.1/32    md5

# then restart service:
# service postgresql-11 restart

