#!/bin/sh

# the following parameters need to be collected and passed from a start script
EDITION='US Edition'
DATABASE='us-edition'
EFF_TIME='20150901'
EXP_TIME='20160301'
LOGIN=''
PASSW=''

UMLS="http://${LOGIN}:${PASSW}@download.nlm.nih.gov/mlb/utsauth/USExt"  # FIX-ME
RELEASEFILE="SnomedCT_RF2Release_US1000124_${EFF_TIME}.zip"             # FIX-ME
HOSTDIR='/nfs/host'
WORKDIR="${HOSTDIR}/work"
OUTDIR="${WORKDIR}/output"
INPDIR="${WORKDIR}/input"

echo Provisioning Basic State Linux, Maven and Java Environment
set -e # exit script on first error
set -x # print commands and their arguments as executed

sudo apt-get update
sudo apt-get upgrade -y
sudo apt-get install -y maven2 openjdk-7-jdk wget bindfs

if [ ! -s ${INPDIR}/${RELEASEFILE} ]; then
    mkdir -p ${INPDIR} && wget ${UMLS}/${RELEASEFILE} -o ${INPDIR}/${RELEASEFILE}
else
    cd ${INPDIR} && unzip -o ${RELEASEFILE}
fi

test -d ${OUTDIR} ] || mkdir -p ${OUTDIR}

cd ${HOSTDIR} && mvn package

cat > /tmp/config.xml <<EOF
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <processInMemory>false</processInMemory>
    <defaultTermLangCode>en</defaultTermLangCode>
    <defaultTermDescriptionType>900000000000003001</defaultTermDescriptionType>
    <defaultTermLanguageRefset>900000000000509007</defaultTermLanguageRefset>
    <normalizeTextIndex>true</normalizeTextIndex>
    <createCompleteConceptsFile>false</createCompleteConceptsFile>
    <editionName>${EDITION}</editionName>
    <databaseName>${DATABASE}</databaseName>
    <effectiveTime>${EFF_TIME}</effectiveTime>
    <expirationTime>${EXP_TIME}</expirationTime>
    <outputFolder>${OUTDIR}/${DATABASE}</outputFolder>
    <foldersBaselineLoad>
      <folder>${INPDIR}/SnomedCT_RF2Release_US1000124_${EFF_TIME}/Snapshot</folder>
    </foldersBaselineLoad>
    <modulesToIgnoreBaselineLoad>
    </modulesToIgnoreBaselineLoad>
    <foldersExtensionLoad>
    </foldersExtensionLoad>
    <modulesToIgnoreExtensionLoad>
    </modulesToIgnoreExtensionLoad>
</config>
EOF

java -Xmx8g -jar ${HOSTDIR}/target/rf2-to-json-conversion-1.0-jar-with-dependencies.jar /tmp/config.xml

if [ $? -eq 0 ]; then
    cd ${OUTDIR} && tar cvjf snomed-ct-json-${DATABASE}-${EFF_TIME}.tar.bz2 ${DATABASE}/
    cd ${OUTDIR} && sha1sum snomed-ct-json-${DATABASE}-${EFF_TIME}.tar.bz2 | cut -f1 -d' ' > snomed-ct-json-${DATABASE}-${EFF_TIME}.tar.bz2.sha1sum
fi
