#!/bin/sh
set -eu

COUCH_URL="${COUCH_URL:-http://couchdb:5984}"
COUCH_USER="${COUCH_USER:-admin}"
COUCH_PASS="${COUCH_PASSWORD:-admin}"   # bierzemy z COUCH_PASSWORD jak u Ciebie

AUTH="${COUCH_USER}:${COUCH_PASS}"

echo "Waiting for CouchDB at ${COUCH_URL} ..."
until curl -s -u "$AUTH" "${COUCH_URL}/_up" | grep -q '"status":"ok"'; do
  sleep 1
done
echo "CouchDB is up."

for db in players matches matchdetails championdetails; do
  code="$(curl -s -o /dev/null -w "%{http_code}" -u "$AUTH" -X PUT "${COUCH_URL}/${db}")"
  if [ "$code" = "201" ]; then
    echo "Created db: $db"
  elif [ "$code" = "412" ]; then
    echo "Db already exists: $db"
  else
    echo "Failed to create db $db (HTTP $code)" >&2
    exit 1
  fi
done

echo "Done."
