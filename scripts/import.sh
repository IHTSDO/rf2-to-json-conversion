#!/usr/bin/env bash
# Import Snomed-CT json data
#
# Requires two arguments:
# $ ./import.sh <database> <collection>
#

if [[ $# -ne 2 ]]; then
    echo 'Import script requires two arguments'
    echo ' $ ./import.sh <database> <collection>'
    exit 1
fi

function exitIfError {
    local err=${1}

    if [ $? -ne 0 ]; then
        echo "An error occurred: $err"
        exit 1
    fi
}

ERROR=$(mongo --eval "db = db.getSiblingDB('server');db.resources.remove({'databaseName': '$1', 'collectionName': '$2'});")
exitIfError ${ERROR}

ERROR=$(mongo --eval "db = db.getSiblingDB('$1');db.dropDatabase();")
exitIfError ${ERROR}

ERROR=$(mongoimport --file concepts.json -d $1 -c v$2)
exitIfError ${ERROR}

ERROR=$(mongoimport --file text-index.json -d $1 -c v$2tx)
exitIfError ${ERROR}

ERROR=$(mongoimport --file manifest.json -d server -c resources)
exitIfError ${ERROR}

if [ -s statedTransitiveClosure.json ]; then
    ERROR=$(mongoimport --file statedTransitiveClosure.json -d $1 -c v$2stc)
    exitIfError ${ERROR}
fi

if [ -s inferredTransitiveClosure.json ]; then
    ERROR=$(mongoimport --file inferredTransitiveClosure.json -d $1 -c v$2itc)
    exitIfError ${ERROR}
fi

ERROR=$(mongo --eval "db = db.getSiblingDB('$1');db.v$2.ensureIndex({'conceptId' : 1});db.v$2.ensureIndex({'relationships.target.conceptId' : 1,'relationships.type.conceptId' : 1});db.v$2.ensureIndex({'statedRelationships.target.conceptId' : 1,'statedRelationships.type.conceptId' : 1});db.v$2.ensureIndex({'memberships.refset.conceptId' :1});")
exitIfError ${ERROR}

ERROR=$(mongo --eval "db = db.getSiblingDB('$1');db.v$2tx.ensureIndex({'descriptionId' : 1});db.v$2tx.ensureIndex({term: 'text'});db.v$2tx.ensureIndex({term: 1});db.v$2tx.ensureIndex({words: 1});")
exitIfError ${ERROR}

echo "Import Completed"
exit 0
