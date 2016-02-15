package org.ihtsdo.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ihtsdo.json.model.Concept;
import org.ihtsdo.json.model.ConceptDescriptor;
import org.ihtsdo.json.model.Description;
import org.ihtsdo.json.model.LangMembership;
import org.ihtsdo.json.model.LightDescription;
import org.ihtsdo.json.model.LightLangMembership;
import org.ihtsdo.json.model.LightRefsetMembership;
import org.ihtsdo.json.model.LightRelationship;
import org.ihtsdo.json.model.RefsetDescriptor;
import org.ihtsdo.json.model.RefsetMembership;
import org.ihtsdo.json.model.Relationship;
import org.ihtsdo.json.model.ResourceSetManifest;
import org.ihtsdo.json.model.TextIndexDescription;
import org.ihtsdo.json.utils.FileHelper;
import org.mapdb.DBMaker;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.apache.log4j.Logger;
import org.ihtsdo.json.model.LightRefsetMembership.RefsetMembershipType;
import org.mapdb.DB;
import org.mapdb.Serializer;

/**
 * @author Adrian Walker
 */
public class MultiMapTransformer {

    private final static Logger LOGGER = Logger.getLogger(MultiMapTransformer.class);

    private static final int PROCESSED_LOG = 100_000;
    private static final int ASYNC_WRITE_FLUSH_DELAY = 100;
    /**
     * Intern strings to reduce memory usage.
     *
     * Only enable INTERN_STRINGS with Java versions 7+
     *
     * Increase the interned string pool with JVM parameter as required, e.g.
     * -XX:StringTableSize=5875187
     */
    private static final boolean INTERN_STRINGS = true;
    /**
     * Enable serialization to reduce memory usage.
     */
    private static final boolean ENABLE_SERIALIZATION = false;
    /**
     * Enable compression to reduce memory usage of serialized objects.
     */
    private static final boolean ENABLE_COMPRESSION = false;

    private static final String MODIFIER = "Existential restriction";
    private static final String SEP = System.getProperty("line.separator");
    private static final Charset UTF8 = Charset.forName("UTF8");

    private static final Pattern LINE_SPLIT_PATTERN = Pattern.compile("\t");
    private static final Pattern TERM_SPLIT_PATTERN = Pattern.compile("\\s+");
    private static final String EMPTY = "";
    private static final String L_PAREN = "(";
    private static final String R_PAREN = ")";

    private static final String FSN_TYPE = "900000000000003001";
    private static final String INFERRED = "900000000000011006";
    private static final String STATED = "900000000000010007";
    private static final String SCT_ID = "116680003";
    private static final String PRIMITIVE = "900000000000074008";
    private static final String PREFERRED = "900000000000548007";
    private static final String ACTIVE = "1";

    private static final String FULLY_DEFINED_DEFINITION_STATUS = "Fully defined";
    private static final String PRIMITIVE_DEFINITION_STATUS = "Primitive";

    private String defaultLangRefset = "900000000000509007";
    private String defaultTermType = FSN_TYPE;
    private String defaultLangCode = "en";

    private DB db;

    // data
    private Map<String, ConceptDescriptor> concepts;
    private Map<String, Iterable<LightDescription>> descriptions;
    private Map<String, Iterable<LightRelationship>> relationships;
    private Map<String, Iterable<LightRelationship>> targetRelationships;
    private Map<String, Iterable<LightRefsetMembership>> simpleMembers;
    private Map<String, Iterable<LightRefsetMembership>> assocMembers;
    private Map<String, Iterable<LightRefsetMembership>> attrMembers;
    private Map<String, Iterable<LightDescription>> tdefMembers;
    private Map<String, Iterable<LightRefsetMembership>> simpleMapMembers;
    private Map<String, Iterable<LightLangMembership>> languageMembers;
    private Map<String, String> cptFSN;

    // lookups
    private Set<String> notLeafInferred;
    private Set<String> notLeafStated;

    private Map<String, Integer> refsetsCount;
    private Map<String, String> refsetsTypes;

    private Set<String> refsetsSet;
    private Set<String> langRefsetsSet;
    private Set<String> modulesSet;

    // other
    private Map<String, String> langCodes;
    private Map<String, String> charConv;
    private ResourceSetManifest manifest;

    public MultiMapTransformer() throws IOException {
        langCodes = getLangCodesTable();
        charConv = getCharConvTable();
    }

    private Map<String, String> getLangCodesTable() {

        Map<String, String> langCodes = new HashMap<>();

        langCodes.put("en", "english");
        langCodes.put("es", "spanish");
        langCodes.put("da", "danish");
        langCodes.put("sv", "swedish");
        langCodes.put("fr", "french");
        langCodes.put("nl", "dutch");

        return langCodes;
    }

    private static Map<String, String> getCharConvTable() throws IOException {

        Map<String, String> charConv = new HashMap<>();

        BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("char_conversion_table.txt"), UTF8));
        br.readLine();

        String line;
        while ((line = br.readLine()) != null) {

            String[] columns = LINE_SPLIT_PATTERN.split(line, -1);
            String[] codes = TERM_SPLIT_PATTERN.split(columns[2]);

            for (String code : codes) {
                charConv.put(code, columns[0]);
            }
        }

        br.close();

        return charConv;
    }

    public void convert(final TransformerConfig config) throws Exception {

        db = makeDatabase(config);
        makeDataStructures();

        setDefaultLangCode(config.getDefaultTermLangCode());
        setDefaultTermType(config.getDefaultTermDescriptionType());
        setDefaultLangRefset(config.getDefaultTermLanguageRefset());

        manifest = new ResourceSetManifest();
        manifest.setDatabaseName(config.getDatabaseName());
        manifest.setTextIndexNormalized(config.isNormalizeTextIndex());
        manifest.setEffectiveTime(config.getEffectiveTime());
        manifest.setDefaultTermLangCode(config.getDefaultTermLangCode());
        manifest.setCollectionName(config.getEffectiveTime());
        manifest.setDefaultTermLangRefset(config.getDefaultTermLanguageRefset());
        manifest.setExpirationDate(config.getExpirationTime());
        manifest.setDefaultTermType(config.getDefaultTermDescriptionType());
        manifest.setResourceSetName(config.getEditionName());

        LOGGER.info("######## Processing Baseline ########");
        Set<String> files = getFilesFromFolders(config.getFoldersBaselineLoad());
        LOGGER.info("Files: " + files.size());
        processFiles(files, config.getModulesToIgnoreBaselineLoad());

        if (config.getFoldersExtensionLoad() != null && config.getModulesToIgnoreExtensionLoad() != null) {
            LOGGER.info("######## Processing Extensions ########");
            files = getFilesFromFolders(config.getFoldersExtensionLoad());
            LOGGER.info("Files: " + files.size());
            processFiles(files, config.getModulesToIgnoreExtensionLoad());
        } else {
            LOGGER.info("######## No Extensions options configured ########");
        }

        completeDefaultTerm();

        File output = new File(config.getOutputFolder());
        output.mkdirs();
        createConceptsJsonFile(config.getOutputFolder() + "/concepts.json", config.isCreateCompleteConceptsFile());
        createTextIndexFile(config.getOutputFolder() + "/text-index.json");
        createManifestFile(config.getOutputFolder() + "/manifest.json");

        db.close();
    }

    private DB makeDatabase(final TransformerConfig config) throws IOException {

        DBMaker dbMaker;

        if (config.isProcessInMemory()) {

            if (ENABLE_SERIALIZATION) {
                dbMaker = DBMaker.newMemoryDB();

                if (ENABLE_COMPRESSION) {
                    dbMaker.compressionEnable();
                }
            } else {
                dbMaker = DBMaker.newHeapDB();
            }
        } else {
            File file = File.createTempFile("mapdb-", ".tmp");
            LOGGER.info("creating temp file: " + file.getAbsolutePath());
            dbMaker = DBMaker.newFileDB(file).commitFileSyncDisable();
        }

        dbMaker = dbMaker.transactionDisable()
                .asyncWriteEnable()
                .asyncWriteFlushDelay(ASYNC_WRITE_FLUSH_DELAY);

        return dbMaker.make();
    }

    private void makeDataStructures() throws IOException {

        Serializer<String> stringSerializer = INTERN_STRINGS ? Serializer.STRING_INTERN : Serializer.STRING;

        concepts = db.createHashMap("concepts")
                .keySerializer(stringSerializer)
                .make();
        descriptions = new StringMultiMap<>(db, "descriptions", stringSerializer);
        relationships = new StringMultiMap<>(db, "relationships", stringSerializer);
        targetRelationships = new StringMultiMap<>(db, "targetRelationships", stringSerializer);
        simpleMembers = new StringMultiMap<>(db, "simpleMembers", stringSerializer);
        assocMembers = new StringMultiMap<>(db, "assocMembers", stringSerializer);
        attrMembers = new StringMultiMap<>(db, "attrMembers", stringSerializer);
        tdefMembers = new StringMultiMap<>(db, "tdefMembers", stringSerializer);
        simpleMapMembers = new StringMultiMap<>(db, "simpleMapMembers", stringSerializer);
        languageMembers = new StringMultiMap<>(db, "languageMembers", stringSerializer);
        cptFSN = db.createHashMap("cptFSN")
                .keySerializer(stringSerializer)
                .valueSerializer(stringSerializer)
                .make();

        notLeafInferred = db.createHashSet("notLeafInferred")
                .serializer(stringSerializer)
                .make();
        notLeafStated = db.createHashSet("notLeafStated")
                .serializer(stringSerializer)
                .make();

        refsetsCount = db.createHashMap("refsetsCount")
                .keySerializer(stringSerializer)
                .valueSerializer(Serializer.INTEGER)
                .make();
        refsetsTypes = db.createHashMap("refsetsTypes")
                .keySerializer(stringSerializer)
                .valueSerializer(stringSerializer)
                .make();

        refsetsSet = db.createHashSet("refsetsSet")
                .serializer(stringSerializer)
                .make();
        langRefsetsSet = db.createHashSet("langRefsetsSet")
                .serializer(stringSerializer)
                .make();
        modulesSet = db.createHashSet("modulesSet")
                .serializer(stringSerializer)
                .make();
    }

    public static void deleteDir(final File dir) {
        
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String child : children) {
                new File(dir, child).delete();
            }
        }
    }

    private void processFiles(final Set<String> files, final List<String> modulesToIgnore) throws IOException, Exception {

        for (String file : files) {
            String pattern = FileHelper.getFileTypeByHeader(new File(file));

            if (pattern.equals("rf2-relationships")) {
                loadRelationshipsFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-textDefinition")) {
                loadTextDefinitionFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-association")) {
                loadAssociationFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-association-2")) {
                loadAssociationFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-attributevalue")) {
                loadAttributeFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-language")) {
                loadLanguageRefsetFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-simple")) {
                loadSimpleRefsetFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-orderRefset")) {
                // TODO: add process to order refset
                loadSimpleRefsetFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-simplemaps")) {
                loadSimpleMapRefsetFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-descriptions")) {
                loadDescriptionsFile(new File(file), modulesToIgnore);
            } else if (pattern.equals("rf2-concepts")) {
                loadConceptsFile(new File(file), modulesToIgnore);
            } else {
            }
        }
    }

    private Set<String> getFilesFromFolders(final Set<String> folders) throws IOException, Exception {

        Set<String> result = new HashSet<>();
        FileHelper fileHelper = new FileHelper();

        for (String folder : folders) {
            File dir = new File(folder);
            HashSet<String> files = new HashSet<>();
            fileHelper.findAllFiles(dir, files);
            result.addAll(files);
        }

        return result;
    }

    private static Stream<String[]> streamFileData(final File file, final List<String> modulesToIgnore) throws IOException {

        Path path = file.toPath();
        Stream<String[]> stream = Files.lines(path, UTF8)
                .skip(1)
                .filter(line -> !line.isEmpty())
                .map(line -> split(line))
                .filter(columns -> !modulesToIgnore.contains(columns[3]));

        if (INTERN_STRINGS) {
            stream = stream.map(columns -> intern(columns));
        }

        return stream;
    }

    private static String[] intern(String[] columns) {

        for (int i = 0; i < columns.length; i++) {
            columns[i] = columns[i].intern();
        }

        return columns;
    }

    private static String[] split(final String line) {

        return LINE_SPLIT_PATTERN.split(line);
    }

    private void logProcessed(final long processed) {

        if (processed % PROCESSED_LOG == 0) {
            LOGGER.info("Processed: " + processed);
        }
    }

    private void commit() {

        LOGGER.debug("Committing...");
        db.commit();
        LOGGER.debug("...done");
    }

    private void clearCache() {

        LOGGER.debug("Clearing cache...");
        db.getEngine().clearCache();
        LOGGER.debug("...done");
    }

    public void loadConceptsFile(final File conceptsFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting Concepts: " + conceptsFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(conceptsFile, modulesToIgnore);
        data.map(columns -> newConceptDescriptor(columns)).forEach(cd -> {

            modulesSet.add(cd.getModule());
            concepts.put(cd.getConceptId(), cd);

            long processed = count.incrementAndGet();
            logProcessed(processed);

        });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("Concepts loaded = " + concepts.size() + " (" + count + ")");
    }

    public void loadDescriptionsFile(final File descriptionsFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting Descriptions: " + descriptionsFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(descriptionsFile, modulesToIgnore);
        data.map(columns -> newLightDescription(columns)).forEach(ld -> {

            modulesSet.add(ld.getModule());
            descriptions.put(ld.getConceptId(), Arrays.asList(new LightDescription[]{ld}));

            long processed = count.incrementAndGet();
            logProcessed(processed);

        });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("Descriptions loaded = " + descriptions.size() + " (" + count + ")");
    }

    public void completeDefaultTerm() {

        LOGGER.info("Starting Default Terms computation");

        AtomicLong count = new AtomicLong();

        concepts.entrySet().forEach(entry -> {

            String sourceId = entry.getKey();

            Iterable<LightDescription> lds = descriptions.get(sourceId);
            if (lds != null) {
                String lastTerm = "No descriptions";
                String enFsn = null;
                String configFsn = null;
                String userSelectedDefaultTermByLangCode = null;
                String userSelectedDefaultTermByRefset = null;

                for (LightDescription ld : lds) {

                    boolean act = ld.isActive();

                    String type = ld.getType();
                    String lang = ld.getLang();

                    if (act && type.equals(FSN_TYPE) && lang.equals(defaultLangCode)) {
                        enFsn = ld.getTerm();
                    }

                    if (act && type.equals(defaultTermType) && lang.equals(defaultLangCode)) {
                        userSelectedDefaultTermByLangCode = ld.getTerm();
                    }

                    if (act && type.equals(defaultTermType)) {
                        Iterable<LightLangMembership> llms = languageMembers.get(ld.getDescriptionId());
                        if (llms != null) {
                            for (LightLangMembership llm : llms) {

                                if (llm.getAcceptability().equals(PREFERRED) && llm.getRefset().equals(defaultLangRefset)) {
                                    if (ld.getType().equals(FSN_TYPE)) {
                                        configFsn = ld.getTerm();
                                    }
                                    userSelectedDefaultTermByRefset = ld.getTerm();
                                }
                            }
                        }
                    }

                    lastTerm = ld.getTerm();

                }

                ConceptDescriptor cd = entry.getValue();

                if (userSelectedDefaultTermByRefset != null) {
                    cd.setDefaultTerm(userSelectedDefaultTermByRefset);
                } else if (userSelectedDefaultTermByLangCode != null) {
                    cd.setDefaultTerm(userSelectedDefaultTermByLangCode);
                } else if (enFsn != null) {
                    cd.setDefaultTerm(enFsn);
                } else {
                    cd.setDefaultTerm(lastTerm);
                }

                concepts.put(sourceId, cd);

                if (configFsn != null) {
                    cptFSN.put(sourceId, configFsn);
                } else if (enFsn != null) {
                    cptFSN.put(sourceId, enFsn);
                } else {
                    cptFSN.put(sourceId, lastTerm);
                }

                long processed = count.incrementAndGet();
                logProcessed(processed);
            }
        });

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("Default Terms computation completed");
    }

    public void loadTextDefinitionFile(final File textDefinitionFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting Text Definitions: " + textDefinitionFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(textDefinitionFile, modulesToIgnore);
        data.map(columns -> newLightDescription(columns)).forEach(ld -> {

            modulesSet.add(ld.getModule());
            tdefMembers.put(ld.getConceptId(), Arrays.asList(new LightDescription[]{ld}));

            long processed = count.incrementAndGet();
            logProcessed(processed);

        });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("Text Definitions loaded = " + tdefMembers.size() + " (" + count + ")");
    }

    public void loadRelationshipsFile(final File relationshipsFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting Relationships: " + relationshipsFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(relationshipsFile, modulesToIgnore);
        data.map(columns -> newLightRelationship(columns)).forEach(lr -> {

            modulesSet.add(lr.getModule());
            relationships.put(lr.getSourceId(), Arrays.asList(new LightRelationship[]{lr}));

            if (lr.getType().equals(SCT_ID) && lr.isActive()) {
                targetRelationships.put(lr.getTarget(), Arrays.asList(new LightRelationship[]{lr}));
            }

            if (lr.isActive() && lr.getType().equals(SCT_ID)) {
                if (lr.getCharType().equals(INFERRED)) {
                    notLeafInferred.add(lr.getTarget());
                } else {
                    notLeafStated.add(lr.getTarget());
                }
            }

            long processed = count.incrementAndGet();
            logProcessed(processed);

        });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("Relationships loaded = " + relationships.size() + " (" + count + ")");
    }

    public void loadSimpleRefsetFile(final File simpleRefsetFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting Simple Refset Members: " + simpleRefsetFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(simpleRefsetFile, modulesToIgnore);
        data.map(columns -> newLightRefsetMembership(columns, RefsetMembershipType.SIMPLE_REFSET.name()))
                .filter(lrm -> lrm.isActive())
                .forEach(lrm -> {

                    modulesSet.add(lrm.getModule());
                    refsetsSet.add(lrm.getRefset());
                    refsetsTypes.put(lrm.getRefset(), lrm.getType());
                    simpleMembers.put(lrm.getReferencedComponentId(), Arrays.asList(new LightRefsetMembership[]{lrm}));
                    refsetsCount.put(lrm.getRefset(), refsetsCount.getOrDefault(lrm.getRefset(), 0) + 1);

                    long processed = count.incrementAndGet();
                    logProcessed(processed);

                });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("SimpleRefsetMember loaded = " + simpleMembers.size() + " (" + count + ")");
    }

    public void loadAssociationFile(final File associationsFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting Association Refset Members: " + associationsFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(associationsFile, modulesToIgnore);
        data.map(columns -> newLightRefsetMembership(columns, RefsetMembershipType.ASSOCIATION.name()))
                .filter(lrm -> lrm.isActive())
                .forEach(lrm -> {

                    modulesSet.add(lrm.getModule());
                    refsetsSet.add(lrm.getRefset());
                    refsetsTypes.put(lrm.getRefset(), lrm.getType());
                    assocMembers.put(lrm.getReferencedComponentId(), Arrays.asList(new LightRefsetMembership[]{lrm}));
                    refsetsCount.put(lrm.getRefset(), refsetsCount.getOrDefault(lrm.getRefset(), 0) + 1);

                    long processed = count.incrementAndGet();
                    logProcessed(processed);

                });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("AssociationMember loaded = " + assocMembers.size() + " (" + count + ")");
    }

    public void loadAttributeFile(final File attributeFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting Attribute Refset Members: " + attributeFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(attributeFile, modulesToIgnore);
        data.map(columns -> newLightRefsetMembership(columns, RefsetMembershipType.ATTRIBUTE_VALUE.name()))
                .filter(lrm -> lrm.isActive())
                .forEach(lrm -> {

                    modulesSet.add(lrm.getModule());
                    refsetsSet.add(lrm.getRefset());
                    refsetsTypes.put(lrm.getRefset(), lrm.getType());
                    attrMembers.put(lrm.getReferencedComponentId(), Arrays.asList(new LightRefsetMembership[]{lrm}));
                    refsetsCount.put(lrm.getRefset(), refsetsCount.getOrDefault(lrm.getRefset(), 0) + 1);

                    long processed = count.incrementAndGet();
                    logProcessed(processed);

                });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("AttributeMember loaded = " + attrMembers.size() + " (" + count + ")");
    }

    public void loadSimpleMapRefsetFile(final File simpleMapRefsetFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting SimpleMap Refset Members: " + simpleMapRefsetFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(simpleMapRefsetFile, modulesToIgnore);
        data.map(columns -> newLightRefsetMembership(columns, RefsetMembershipType.SIMPLEMAP.name()))
                .filter(lrm -> lrm.isActive())
                .forEach(lrm -> {

                    modulesSet.add(lrm.getModule());
                    refsetsSet.add(lrm.getRefset());
                    refsetsTypes.put(lrm.getRefset(), lrm.getType());
                    simpleMembers.put(lrm.getReferencedComponentId(), Arrays.asList(new LightRefsetMembership[]{lrm}));
                    refsetsCount.put(lrm.getRefset(), refsetsCount.getOrDefault(lrm.getRefset(), 0) + 1);

                    long processed = count.incrementAndGet();
                    logProcessed(processed);

                });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("SimpleMap RefsetMember loaded = " + simpleMapMembers.size() + " (" + count + ")");
    }

    public void loadLanguageRefsetFile(final File languageRefsetFile, final List<String> modulesToIgnore) throws FileNotFoundException, IOException {

        LOGGER.info("Starting Language Refset Members: " + languageRefsetFile.getName());

        clearCache();

        AtomicLong count = new AtomicLong();

        Stream<String[]> data = streamFileData(languageRefsetFile, modulesToIgnore);
        data.map(columns -> newLightLangMembership(columns))
                .filter(llm -> llm.isActive())
                .forEach(llm -> {

                    modulesSet.add(llm.getModule());
                    langRefsetsSet.add(llm.getRefset());
                    languageMembers.put(llm.getDescriptionId(), Arrays.asList(new LightLangMembership[]{llm}));

                    long processed = count.incrementAndGet();
                    logProcessed(processed);

                });
        data.close();

        LOGGER.info("Processed: " + count);

        commit();

        LOGGER.info("LanguageMembers loaded = " + languageMembers.size() + " (" + count + ")");
    }

    public void createConceptsJsonFile(final String fileName, final boolean createCompleteVersion) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        LOGGER.info("Starting creation of " + fileName);

        BufferedWriter bw = Files.newBufferedWriter(Paths.get(fileName), UTF8);

        Gson gson = new Gson();

        AtomicLong count = new AtomicLong();

        concepts.entrySet().forEach(entry -> {

            long processed = count.incrementAndGet();
            logProcessed(processed);

            Concept c = newConcept(entry.getKey(), entry.getValue(), createCompleteVersion);

            Iterable<LightDescription> lds = descriptions.get(c.getConceptId());
            List<Description> ds = new ArrayList();

            if (lds != null) {

                lds.forEach(ld -> {

                    Description d = newDescription(ld, c, createCompleteVersion);

                    Iterable<LightLangMembership> llms = languageMembers.get(d.getDescriptionId());
                    List<LangMembership> lms = new ArrayList();

                    if (llms != null) {
                        llms.forEach(llm -> {
                            LangMembership lm = newLangMembership(llm);
                            lms.add(lm);
                        });

                        if (lms.isEmpty()) {
                            d.setLangMemberships(null);
                        } else {
                            d.setLangMemberships(lms);
                        }
                    }

                    Iterable<LightRefsetMembership> lrms = attrMembers.get(d.getDescriptionId());
                    List<RefsetMembership> rms = new ArrayList();

                    if (lrms != null) {
                        lrms.forEach(lrm -> {
                            RefsetMembership rm = newRefsetMembership(lrm);
                            rms.add(rm);
                        });

                        if (rms.isEmpty()) {
                            d.setRefsetMemberships(null);
                        } else {
                            d.setRefsetMemberships(rms);
                        }
                    } else {
                        d.setRefsetMemberships(null);
                    }

                    ds.add(d);
                });
            }

            lds = tdefMembers.get(c.getConceptId());

            if (lds != null) {
                lds.forEach(ld -> {
                    Description d = newDescription(ld);

                    Iterable<LightLangMembership> llms = languageMembers.get(ld.getDescriptionId());
                    List<LangMembership> lms = new ArrayList();

                    if (llms != null) {
                        llms.forEach(llm -> {
                            LangMembership lm = newLangMembership(llm);
                            lms.add(lm);
                        });

                        if (lms.isEmpty()) {
                            d.setLangMemberships(null);
                        } else {
                            d.setLangMemberships(lms);
                        }
                    }

                    ds.add(d);
                });
            }

            if (!ds.isEmpty()) {
                c.setDescriptions(ds);
            } else {
                c.setDescriptions(null);
            }

            Iterable<LightRelationship> lrs = relationships.get(c.getConceptId());
            List<Relationship> statedRs = new ArrayList();
            List<Relationship> inferredRs = new ArrayList();

            if (lrs != null) {
                lrs.forEach(lr -> {
                    Relationship r = newRelationship(lr, createCompleteVersion);

                    if (lr.getCharType().equals(STATED)) {
                        statedRs.add(r);
                    } else if (lr.getCharType().equals(INFERRED)) {
                        inferredRs.add(r);
                    }
                });

                if (statedRs.isEmpty()) {
                    c.setStatedRelationships(null);
                } else {
                    c.setStatedRelationships(statedRs);
                }

                if (inferredRs.isEmpty()) {
                    c.setRelationships(null);
                } else {
                    c.setRelationships(inferredRs);
                }
            } else {
                c.setStatedRelationships(null);
                c.setRelationships(null);
            }

            List<RefsetMembership> rms = new ArrayList();

            Iterable<LightRefsetMembership> lrms = simpleMembers.get(c.getConceptId());
            if (lrms != null) {
                lrms.forEach(lrm -> {
                    RefsetMembership rm = newRefsetMembership(lrm);
                    rms.add(rm);
                });
            }

            lrms = simpleMapMembers.get(c.getConceptId());
            if (lrms != null) {
                lrms.forEach(lrm -> {
                    RefsetMembership rm = newRefsetMembership(lrm);
                    rms.add(rm);
                });
            }

            lrms = assocMembers.get(c.getConceptId());
            if (lrms != null) {
                lrms.forEach(lrm -> {
                    RefsetMembership rm = newRefsetMembership(lrm);
                    rms.add(rm);
                });
            }

            lrms = attrMembers.get(c.getConceptId());
            if (lrms != null) {
                lrms.forEach(lrm -> {
                    RefsetMembership rm = newRefsetMembership(lrm);
                    rms.add(rm);
                });
            }

            if (rms.isEmpty()) {
                c.setMemberships(null);
            } else {
                c.setMemberships(rms);
            }

            try {
                bw.append(gson.toJson(c));
                bw.append(SEP);
            } catch (final IOException ioe) {
                LOGGER.error(ioe);
                throw new RuntimeException(ioe);
            }
        });

        bw.close();
        LOGGER.info("Processed: " + count);
        LOGGER.info(fileName + " Done");
    }

    private List<String> getDescendants(final String cptId, final String charType) {
        return getDescendants(new ArrayList<>(), cptId, charType);
    }

    private List<String> getDescendants(final List<String> descendants, final String cptId, final String charType) {

        Iterable<LightRelationship> lrs = targetRelationships.get(cptId);

        if (lrs != null) {
            for (LightRelationship lr : lrs) {

                if (lr.getCharType().equals(charType)) {
                    String sourceId = lr.getSourceId();
                    if (!descendants.contains(sourceId)) {
                        descendants.add(sourceId);
                        getDescendants(descendants, sourceId, charType);
                    }
                }
            }
        }

        return descendants;
    }

    public String getDefaultLangCode() {
        return defaultLangCode;
    }

    public void setDefaultLangCode(String defaultLangCode) {
        this.defaultLangCode = defaultLangCode;
    }

    private List<String> getAncestors(final String cptId, final String charType) {
        return getAncestors(new ArrayList<>(), cptId, charType);
    }

    private List<String> getAncestors(final List<String> ancestors, final String cptId, final String charType) {

        Iterable<LightRelationship> lrs = relationships.get(cptId);

        if (lrs != null) {
            for (LightRelationship lr : lrs) {

                if (lr.getCharType().equals(charType)
                        && lr.getType().equals(SCT_ID)
                        && lr.isActive()) {

                    String tgt = lr.getTarget();

                    if (!ancestors.contains(tgt)) {
                        ancestors.add(tgt);
                        getAncestors(ancestors, tgt, charType);
                    }
                }
            }
        }

        return ancestors;
    }

    public void createTextIndexFile(final String fileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        LOGGER.info("Starting creation of " + fileName);

        BufferedWriter bw = Files.newBufferedWriter(Paths.get(fileName), UTF8);
        Gson gson = new Gson();

        AtomicLong count = new AtomicLong();

        descriptions.entrySet().forEach(entry -> {

            long processed = count.incrementAndGet();
            logProcessed(processed);

            entry.getValue().forEach(ld -> {

                TextIndexDescription tid = newTextIndexDescription(ld);

                try {
                    bw.append(gson.toJson(tid));
                    bw.append(SEP);
                } catch (final IOException ioe) {
                    LOGGER.error(ioe);
                    throw new RuntimeException(ioe);
                }
            });
        });

        bw.close();
        LOGGER.info("Processed: " + count);
        LOGGER.info(fileName + " Done");
    }

    public void createManifestFile(final String fileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {

        LOGGER.info("Starting creation of " + fileName);

        BufferedWriter bw = Files.newBufferedWriter(Paths.get(fileName), UTF8);
        Gson gson = new Gson();

        for (String moduleId : modulesSet) {
            manifest.getModules().add(concepts.get(moduleId));
        }

        for (String langRefsetId : langRefsetsSet) {
            manifest.getLanguageRefsets().add(concepts.get(langRefsetId));
        }

        for (String refsetId : refsetsSet) {
            ConceptDescriptor concept = concepts.get(refsetId);
            manifest.getRefsets().add(new RefsetDescriptor(concept, refsetsCount.get(refsetId)));
        }

        bw.append(gson.toJson(manifest));
        bw.close();

        LOGGER.info(fileName + " Done");
    }

    private String convertTerm(final String cleanTerm) {

        String convertedTerm = cleanTerm;

        for (String code : charConv.keySet()) {
            String test = "\\u" + code;
            String repl = charConv.get(code);
            convertedTerm = convertedTerm.replaceAll(test, repl);
        }

        return convertedTerm;
    }

    public String getDefaultTermType() {
        return defaultTermType;
    }

    public void setDefaultTermType(final String defaultTermType) {
        this.defaultTermType = defaultTermType;
    }

    public String getDefaultLangRefset() {
        return defaultLangRefset;
    }

    public void setDefaultLangRefset(final String defaultLangRefset) {
        this.defaultLangRefset = defaultLangRefset;
    }

    private Concept newConcept(final String conceptId, final ConceptDescriptor conceptDescriptor, final boolean createCompleteVersion) {

        Concept c = new Concept();
        c.setConceptId(conceptId);
        c.setActive(conceptDescriptor.isActive());
        c.setDefaultTerm(conceptDescriptor.getDefaultTerm());
        c.setEffectiveTime(conceptDescriptor.getEffectiveTime());
        c.setModule(conceptDescriptor.getModule());
        c.setDefinitionStatus(conceptDescriptor.getDefinitionStatus());
        c.setLeafInferred(!notLeafInferred.contains(conceptId));
        c.setLeafStated(!notLeafStated.contains(conceptId));
        c.setFsn(cptFSN.get(conceptId));

        if (createCompleteVersion) {
            c.setInferredAncestors(getAncestors(conceptId, INFERRED));
            c.setStatedAncestors(getAncestors(conceptId, STATED));
            c.setInferredDescendants(getDescendants(conceptId, INFERRED));
            c.setStatedDescendants(getDescendants(conceptId, STATED));
        }

        return c;
    }

    private Description newDescription(final LightDescription lightDescription) {

        Description d = new Description();
        d.setActive(lightDescription.isActive());
        d.setConceptId(lightDescription.getConceptId());
        d.setDescriptionId(lightDescription.getDescriptionId());
        d.setEffectiveTime(lightDescription.getEffectiveTime());
        d.setIcs(concepts.get(lightDescription.getIcs()));
        d.setTerm(lightDescription.getTerm());
        d.setLength(lightDescription.getTerm().length());
        d.setModule(lightDescription.getModule());
        d.setType(concepts.get(lightDescription.getType()));
        d.setLang(lightDescription.getLang());

        return d;
    }

    private Description newDescription(final LightDescription lightDescription, final Concept concept, final boolean createCompleteVersion) {

        Description d = newDescription(lightDescription);

        if (createCompleteVersion) {

            String fsn = concept.getFsn();

            if (fsn.endsWith(R_PAREN)) {
                concept.setSemtag(fsn.substring(fsn.lastIndexOf(L_PAREN) + 1, fsn.length() - 1));
            }

            String cleanTerm = d.getTerm().replace(L_PAREN, EMPTY).replace(R_PAREN, EMPTY).trim().toLowerCase();

            if (manifest.isTextIndexNormalized()) {

                String convertedTerm = convertTerm(cleanTerm);
                String[] tokens = TERM_SPLIT_PATTERN.split(convertedTerm.toLowerCase());
                d.setWords(Arrays.asList(tokens));

            } else {

                String[] tokens = TERM_SPLIT_PATTERN.split(cleanTerm.toLowerCase());
                d.setWords(Arrays.asList(tokens));
            }
        }

        return d;
    }

    private RefsetMembership newRefsetMembership(final LightRefsetMembership lightRefsetMember) {

        RefsetMembership rm = new RefsetMembership();
        rm.setEffectiveTime(lightRefsetMember.getEffectiveTime());
        rm.setActive(lightRefsetMember.isActive());
        rm.setModule(lightRefsetMember.getModule());
        rm.setUuid(lightRefsetMember.getUuid());
        rm.setReferencedComponentId(lightRefsetMember.getReferencedComponentId());
        rm.setRefset(concepts.get(lightRefsetMember.getRefset()));
        rm.setType(lightRefsetMember.getType());
        rm.setCidValue(concepts.get(lightRefsetMember.getCidValue()));

        return rm;
    }

    private Relationship newRelationship(final LightRelationship lightRelationship, final boolean createCompleteVersion) {

        Relationship r = new Relationship();
        r.setEffectiveTime(lightRelationship.getEffectiveTime());
        r.setActive(lightRelationship.isActive());
        r.setModule(lightRelationship.getModule());
        r.setGroupId(lightRelationship.getGroupId());
        r.setModifier(MODIFIER);
        r.setSourceId(lightRelationship.getSourceId());
        r.setTarget(concepts.get(lightRelationship.getTarget()));
        r.setType(concepts.get(lightRelationship.getType()));
        r.setCharType(concepts.get(lightRelationship.getCharType()));

        if (createCompleteVersion) {
            r.setTypeInferredAncestors(getAncestors(lightRelationship.getType(), INFERRED));
            r.setTypeStatedAncestors(getAncestors(lightRelationship.getType(), STATED));
            r.setTargetInferredAncestors(getAncestors(lightRelationship.getTarget(), INFERRED));
            r.setTargetStatedAncestors(getAncestors(lightRelationship.getTarget(), STATED));
        }

        return r;
    }

    private LangMembership newLangMembership(final LightLangMembership lightLanguageMember) {

        LangMembership lm = new LangMembership();
        lm.setActive(lightLanguageMember.isActive());
        lm.setDescriptionId(lightLanguageMember.getDescriptionId());
        lm.setEffectiveTime(lightLanguageMember.getEffectiveTime());
        lm.setModule(lightLanguageMember.getModule());
        lm.setAcceptability(concepts.get(lightLanguageMember.getAcceptability()));
        lm.setRefset(concepts.get(lightLanguageMember.getRefset()));
        lm.setUuid(lightLanguageMember.getUuid());

        return lm;
    }

    private TextIndexDescription newTextIndexDescription(final LightDescription ld) {

        TextIndexDescription tid = new TextIndexDescription();
        tid.setActive(ld.isActive());
        tid.setTerm(ld.getTerm());
        tid.setLength(ld.getTerm().length());
        tid.setTypeId(ld.getType());
        tid.setConceptId(ld.getConceptId());
        tid.setDescriptionId(ld.getDescriptionId());
        tid.setModule(ld.getModule());
        //TODO: using String lang names to support compatibility with Mongo 2.4.x text indexes
        tid.setLang(langCodes.get(ld.getLang()));
        tid.setConceptModule(ld.getModule());
        tid.setConceptActive(ld.isActive());
        tid.setDefinitionStatus(concepts.get(ld.getConceptId()).getDefinitionStatus());
        tid.setFsn(cptFSN.get(ld.getConceptId()));
        tid.setSemanticTag(EMPTY);

        if (tid.getFsn().endsWith(R_PAREN)) {
            tid.setSemanticTag(tid.getFsn().substring(tid.getFsn().lastIndexOf(L_PAREN) + 1, tid.getFsn().length() - 1));
        }

        String cleanTerm = tid.getTerm().replace(L_PAREN, EMPTY).replace(R_PAREN, EMPTY).trim().toLowerCase();
        if (manifest.isTextIndexNormalized()) {
            String convertedTerm = convertTerm(cleanTerm);
            String[] tokens = TERM_SPLIT_PATTERN.split(convertedTerm.toLowerCase());
            tid.setWords(Arrays.asList(tokens));
        } else {
            String[] tokens = TERM_SPLIT_PATTERN.split(cleanTerm.toLowerCase());
            tid.setWords(Arrays.asList(tokens));
        }

        tid.setRefsetIds(new ArrayList());

        // Refset index assumes that only active members are included in the db.
        Iterable<LightRefsetMembership> lrms = simpleMembers.get(ld.getConceptId());
        if (lrms != null) {
            for (LightRefsetMembership lrm : lrms) {
                tid.getRefsetIds().add(lrm.getRefset());
            }
        }

        lrms = simpleMapMembers.get(ld.getConceptId());
        if (lrms != null) {
            for (LightRefsetMembership lrm : lrms) {
                tid.getRefsetIds().add(lrm.getRefset());
            }
        }

        lrms = assocMembers.get(ld.getConceptId());
        if (lrms != null) {
            for (LightRefsetMembership lrm : lrms) {
                tid.getRefsetIds().add(lrm.getRefset());
            }
        }

        lrms = attrMembers.get(ld.getConceptId());
        if (lrms != null) {
            for (LightRefsetMembership lrm : lrms) {
                tid.getRefsetIds().add(lrm.getRefset());
            }
        }

        return tid;
    }

    private ConceptDescriptor newConceptDescriptor(final String[] columns) {

        ConceptDescriptor cd = new ConceptDescriptor();
        cd.setConceptId(columns[0]);
        cd.setEffectiveTime(columns[1]);
        cd.setActive(columns[2].equals(ACTIVE));
        cd.setModule(columns[3]);
        cd.setDefinitionStatus(columns[4].equals(PRIMITIVE) ? PRIMITIVE_DEFINITION_STATUS : FULLY_DEFINED_DEFINITION_STATUS);

        return cd;
    }

    private LightDescription newLightDescription(final String[] columns) {

        LightDescription ld = new LightDescription();
        ld.setDescriptionId(columns[0]);
        ld.setEffectiveTime(columns[1]);
        ld.setActive(columns[2].equals(ACTIVE));
        ld.setModule(columns[3]);
        ld.setConceptId(columns[4]);
        ld.setLang(columns[5]);
        ld.setType(columns[6]);
        ld.setTerm(columns[7]);
        ld.setIcs(columns[8]);

        return ld;
    }

    private LightLangMembership newLightLangMembership(final String[] columns) {

        LightLangMembership llm = new LightLangMembership();
        llm.setUuid(UUID.fromString(columns[0]));
        llm.setEffectiveTime(columns[1]);
        llm.setActive(columns[2].equals(ACTIVE));
        llm.setModule(columns[3]);
        llm.setRefset(columns[4]);
        llm.setDescriptionId(columns[5]);
        llm.setAcceptability(columns[6]);

        return llm;
    }

    private LightRelationship newLightRelationship(final String[] columns) throws NumberFormatException {

        LightRelationship lr = new LightRelationship();
        lr.setEffectiveTime(columns[1]);
        lr.setActive(columns[2].equals(ACTIVE));
        lr.setModule(columns[3]);
        lr.setSourceId(columns[4]);
        lr.setTarget(columns[5]);
        lr.setGroupId(Integer.valueOf(columns[6]));
        lr.setType(columns[7]);
        lr.setModifier(columns[9]);
        lr.setCharType(columns[8]);

        return lr;
    }

    private LightRefsetMembership newLightRefsetMembership(final String[] columns, final String type) {

        LightRefsetMembership lrm = new LightRefsetMembership();
        lrm.setType(type);
        lrm.setUuid(UUID.fromString(columns[0]));
        lrm.setEffectiveTime(columns[1]);
        lrm.setActive(columns[2].equals(ACTIVE));
        lrm.setModule(columns[3]);
        lrm.setRefset(columns[4]);
        lrm.setReferencedComponentId(columns[5]);

        if (columns.length > 6) {
            lrm.setCidValue(columns[6]);
        }

        return lrm;
    }
}
