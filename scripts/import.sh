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

+mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'relationships.target.conceptId' : 1,'relationships.type.conceptId' : 1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'statedRelationships.target.conceptId' : 1,'statedRelationships.type.conceptId' : 1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'additionalRelationships.target.conceptId' : 1,'additionalRelationships.type.conceptId' : 1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'memberships.refset.conceptId' :1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({inferredAncestors:1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({statedAncestors:1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'statedRelationships.typeInferredAncestors':1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'statedRelationships.typeStatedAncestors':1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'relationships.typeInferredAncestors':1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'relationships.typeStatedAncestors':1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'statedRelationships.targetInferredAncestors':1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'statedRelationships.targetStatedAncestors':1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'relationships.targetInferredAncestors':1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3.ensureIndex({'relationships.targetStatedAncestors':1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3tx.ensureIndex({'descriptionId' : 1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3tx.ensureIndex({term: 'text'});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3tx.ensureIndex({term: 1});"
 +mongo --host $1 --port 27017 --eval "db = db.getSiblingDB('$2');db.v$3tx.ensureIndex({words: 1});"

echo "Import Completed"
exit 0
