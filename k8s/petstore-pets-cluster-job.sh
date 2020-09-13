#!/bin/sh -

set -o errexit

export PGHOST=$PETSTORE_PETS_CLUSTER_SERVICE_HOST
export PGPORT=$PETSTORE_PETS_CLUSTER_SERVICE_PORT_POSTGRESQL
export PGDATABASE="pets"
export PGUSER=$DATABASE_USERNAME
export PGPASSWORD=$DATABASE_PASSWORD

echo "running init script.."
psql < /usr/src/schema.sql
echo "init script completed"

echo "job completed"

return 0
