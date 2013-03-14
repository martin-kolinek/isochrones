#!/bin/bash
echo running program
sbt "run $1 $2 martin"
echo visualizing results
psql -f util/output.sql
