# RF2 to json conversion utility [![Build Status](https://travis-ci.org/IHTSDO/rf2-to-json-conversion.svg?branch=master)](https://travis-ci.org/IHTSDO/rf2-to-json-conversion) [![Code Climate](https://codeclimate.com/github/IHTSDO/rf2-to-json-conversion/badges/gpa.svg)](https://codeclimate.com/github/IHTSDO/rf2-to-json-conversion)
Conversion of SNOMED CT RF2 files to a JSON format, including pre-computed indexes for common search strategies

Please see the bottom of this readme file for instructions for users who are developing within the IHTSDO.

## Building (manual instructions)
Build the project using Maven, it will generate an executable jar with embedded dependencies (i.e. "target/rf2-to-json-conversion-1.0-SNAPSHOT-jar-with-dependencies.jar").

## Conversion configuration
Create a config.xml for the conversion execution, it requires to define the location of the source RF2 Snapshot files.

```
<?xml version="1.0" encoding="UTF-8"?>
<config>
    <processInMemory>true</processInMemory>
    <defaultTermLangCode>en</defaultTermLangCode>
    <defaultTermDescriptionType>900000000000003001</defaultTermDescriptionType>
    <defaultTermLanguageRefset>900000000000509007</defaultTermLanguageRefset>
    <normalizeTextIndex>true</normalizeTextIndex>
    <createCompleteConceptsFile>false</createCompleteConceptsFile>
    <editionName>International Edition</editionName>
    <databaseName>en-edition</databaseName>
    <effectiveTime>20140131</effectiveTime>
    <expirationTime>20150201</expirationTime>
    <outputFolder>/Users/alo/Downloads/Releases/en-json</outputFolder>
    <foldersBaselineLoad>
        <folder>/Users/alo/Downloads/Releases/SnomedCT_Release_INT_20140131/RF2Release/Snapshot</folder>
    </foldersBaselineLoad>
    <modulesToIgnoreBaselineLoad>
    </modulesToIgnoreBaselineLoad>
    <foldersExtensionLoad>
    </foldersExtensionLoad>
    <modulesToIgnoreExtensionLoad>
    </modulesToIgnoreExtensionLoad>
</config>
```

Variables:
- _processInMemory_: true for processing with memory maps, really fast, but it requires 8gb of RAM for simple proyects (International Edition + 1 extension). Set to false for using disk based maps, slower but runs with less memory.
- _defaultTermLangCode_: Language code for default term selection.
- _defaultTermDescriptionType_: description type SCTID for the default term selection, some prefer to use the FSN as default term, other prefer to use a Synonym (the preferred synonym will be picked).
- _defaultTermLanguageRefset_: language reference set used to identify the preferred acceptability.
- _normalizeTextIndex_: setting this to True will normalize the text index with diacritics removal.
- _createCompleteConceptsFile_: Always set to false, true provides an additional level of denormalization not implemented in any APIS today and increases significantly the size of the resulting model.
- _editionName_: name for this edition.
- _databaseName_: short name for the edition, used for the ongodb database.
- _effectiveTime_: release date/effective time for the finl package, if it combines the International Edition and an extension use the later date, usually the extension one.
- _expirationTime_: date when a warning needs to appear in the browser to announce that data may be deprecated.
- _outputFolder_: folder where the resulting .json files will be stored.
- _foldersBaselineLoad_: list of folders with baseline edition snapshots, usually the international edition.
- _modulesToIgnoreBaselineLoad_: list of Modules SCTIDs in the baseline files to ignore during conversion.
- _foldersExtensionLoad_: list of folders with extension edition files.
- _modulesToIgnoreExtensionLoad_: list of Modules SCTIDs in the extension files to ignore during conversion.

If the base edition and the extension are available in a single RF2 snapshot package, this folder can be used as a single baseline folder. All folders will be recursively expored and RF2 files will be indentified base on file names and headers (firs row of the file).

You can find sample config files in the `config` folder.

## Executing conversion
Run the executable jar file after building the repository, alternatively it can be found in the [releases]( https://github.com/IHTSDO/rf2-to-json-conversion/releases):

```
java -Xmx8g -jar rf2-to-json-conversion-<version>-SNAPSHOT-jar-with-dependencies.jar config.xml
```

The results will be a set of .json files in the output folder.

## Importing data
The snapshot API requires a MongoDB database to store the denormalized representation contained in the .json files. MongoDb 2.6.x is required to leverage its latest adavances in full text search.

This project includes a script file for importing the resulting .json files into MongoDB, and creating the necessary indexes. This sentence needs to be executed from the folder that contains the .json files. The script will use the `mongoimport` command provided by mongoDB, it is expected to be in the path, as well as `mongo` runtime.

`./import.sh localhost en-edition 20140731`

The first parameter is the hostname, the second is the edition short name, and the third is the effective time. The edition and effective time will be used to configure the Rest API that runs with this data.

