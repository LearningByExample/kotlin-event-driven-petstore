#!/bin/sh -

set -o errexit

export PGHOST="192.168.64.3"
export PGPORT="6432"
export PGDATABASE="pets"
export PGUSER="petdba"
export PGPASSWORD=`kubectl get secret petdba.petstore-pets-cluster.credentials -o 'jsonpath={.data.password}' | base64 -d`

psql < ../pet-sql/schema.sql