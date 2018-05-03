/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.ihtsdo.json.model.Concept;
import org.ihtsdo.json.model.ConceptDescriptor;
import org.ihtsdo.json.model.Description;
import org.ihtsdo.json.model.LangMembership;
import org.ihtsdo.json.model.LightConceptDescriptor;
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
import org.ihtsdo.json.utils.TClosure;
import org.mapdb.DBMaker;

import com.google.gson.Gson;

/**
 *
 * @author Alejandro Rodriguez
 */
public class TransformerDiskBased {

	private static final String CONCEPT_MODEL_ATTRIBUTE = "410662002";
	private static final String ATTRIBUTE = "246061005";
	private String MODIFIER = "Existential restriction";
	private String MODIFIER_ID = "900000000000451002";
	private String sep = System.getProperty("line.separator");

	private Map<String, ConceptDescriptor> concepts;
	private Map<String, List<LightDescription>> descriptions;
	private Map<String, List<LightRelationship>> relationships;
	//private Map<String, List<LightRelationship>> targetRelationships;
	private Map<String, List<LightRefsetMembership>> simpleMembers;
	private Map<String, List<LightRefsetMembership>> simpleMapMembers;
	private Map<String, List<LightLangMembership>> languageMembers;
	private Map<String, String> langCodes;

    private Map<String, Integer> refsetsCount;
    private Map<String, String> refsetsTypes;

	private String defaultLangCode = "en";
	public String fsnType = "900000000000003001";
	public String synType = "900000000000013009";
	private String inferred = "900000000000011006";
	private String stated = "900000000000010007";
	private String isaSCTId = "116680003";
	private String defaultTermType = fsnType;
    private String defaultLangRefset = "900000000000509007";
	private Map<String, List<LightDescription>> tdefMembers;
	private Map<String, List<LightRefsetMembership>> attrMembers;
	private Map<String, List<LightRefsetMembership>> assocMembers;
	private List<String> listA;
	private Map<String, String> charConv;
	private Map<String, String> cptFSN;
	private HashSet<String> notLeafInferred;
	private HashSet<String> notLeafStated;
    private ResourceSetManifest manifest;
    private Set<String> refsetsSet;
    private Set<String> langRefsetsSet;
    private Set<String> modulesSet;
	private Map<String,List<String>> calculatedInferredAncestors;
	private Map<String,List<String>> calculatedStatedAncestors;
	private Map<String,List<String>> calculatedInferredAncestorsForRelType;
	private Map<String,List<String>> calculatedStatedAncestorsForRelType;
	private String[] wordSeparators;
	private boolean processInMemory;
	private List<String> emptyList;
	private String version;

    public TransformerDiskBased() throws IOException {
		langCodes = new HashMap<String, String>();
		langCodes.put("en", "english");
		langCodes.put("es", "spanish");
		langCodes.put("da", "danish");
		langCodes.put("sv", "swedish");
		langCodes.put("fr", "french");
		langCodes.put("nl", "dutch");
		wordSeparators=new String[]{"-"};

	}

    public void convert(TransformerConfig config) throws Exception {
    	processInMemory=config.isProcessInMemory();
    	emptyList=new ArrayList<String>();
    	version=config.getVersion();
    	if (version==null){
    		version="2";
    	}
        if (processInMemory) {
            concepts = new HashMap<String, ConceptDescriptor>();
            descriptions = new HashMap<String, List<LightDescription>>();
            relationships = new HashMap<String, List<LightRelationship>>();
            //targetRelationships = new HashMap<String, List<LightRelationship>>();
            simpleMembers = new HashMap<String, List<LightRefsetMembership>>();
            assocMembers = new HashMap<String, List<LightRefsetMembership>>();
            attrMembers = new HashMap<String, List<LightRefsetMembership>>();
            tdefMembers = new HashMap<String, List<LightDescription>>();
            simpleMapMembers = new HashMap<String, List<LightRefsetMembership>>();
            languageMembers = new HashMap<String, List<LightLangMembership>>();
            cptFSN = new HashMap<String, String>();
			calculatedInferredAncestors=new HashMap<String, List<String>>();
			calculatedStatedAncestors=new HashMap<String, List<String>>();
			calculatedInferredAncestorsForRelType=new HashMap<String, List<String>>();
			calculatedStatedAncestorsForRelType=new HashMap<String, List<String>>();
        } else {
            concepts = new HashMap<String, ConceptDescriptor>();
            descriptions = new HashMap<String, List<LightDescription>>();
            relationships = new HashMap<String, List<LightRelationship>>();
            languageMembers = new HashMap<String, List<LightLangMembership>>();
           /* concepts = DBMaker.newTempHashMap();
            descriptions = DBMaker.newTempHashMap();
            relationships = DBMaker.newTempHashMap();
            languageMembers = DBMaker.newTempHashMap();*/
            //targetRelationships = DBMaker.newTempHashMap();
            simpleMembers = DBMaker.newTempHashMap();
            assocMembers = DBMaker.newTempHashMap();
            attrMembers = DBMaker.newTempHashMap();
            tdefMembers = DBMaker.newTempHashMap();
            simpleMapMembers = DBMaker.newTempHashMap();
            cptFSN = DBMaker.newTempHashMap();
			calculatedInferredAncestors=DBMaker.newTempHashMap();
			calculatedStatedAncestors=DBMaker.newTempHashMap();
			calculatedInferredAncestorsForRelType=DBMaker.newTempHashMap();
			calculatedStatedAncestorsForRelType=DBMaker.newTempHashMap();
        }

        notLeafInferred=new HashSet<String>();
        notLeafStated=new HashSet<String>();

        refsetsCount = new HashMap<String, Integer>();
        refsetsTypes = new HashMap<String, String>();

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
//        manifest.setExpirationDate(config.getExpirationTime());
        manifest.setDefaultTermType(config.getDefaultTermDescriptionType());
        manifest.setResourceSetName(config.getEditionName());

        refsetsSet = new HashSet<String>();
        langRefsetsSet = new HashSet<String>();
        modulesSet = new HashSet<String>();

        System.out.println("######## Processing Baseline ########");
        HashSet<String> files = getFilesFromFolders(config.getFoldersBaselineLoad());
        System.out.println("Files: " + files.size());
        processFiles(files, config.getModulesToIgnoreBaselineLoad());

        if (config.getFoldersExtensionLoad() != null && config.getModulesToIgnoreExtensionLoad() != null) {
            System.out.println("######## Processing Extensions ########");
            files = getFilesFromFolders(config.getFoldersExtensionLoad());
            System.out.println("Files: " + files.size());
            processFiles(files, config.getModulesToIgnoreExtensionLoad());
        } else {
            System.out.println("######## No Extensions options configured ########");
        }

		getDescendantsCount();
        completeDefaultTerm();
        completeModuleAndDefStatus();
        File output = new File(config.getOutputFolder());
        output.mkdirs();
        createConceptsJsonFile(config.getOutputFolder() + "/concepts.json", config.isCreateCompleteConceptsFile());
        createTextIndexFile(config.getOutputFolder() + "/text-index.json");
        createManifestFile(config.getOutputFolder() + "/manifest.json");

    }

	private void getDescendantsCount() throws FileNotFoundException, IOException {
		TClosure tc=new TClosure(relationships, inferred);
		for (String conceptId:concepts.keySet()){
			ConceptDescriptor cd=concepts.get(conceptId);
			int descend=tc.getDescendantsCount(Long.parseLong(conceptId));
			cd.setInferredDescendants(descend);
		}
		tc=null;
		tc=new TClosure(relationships, stated);
		for (String conceptId:concepts.keySet()){
			ConceptDescriptor cd=concepts.get(conceptId);
			int descend=tc.getDescendantsCount(Long.parseLong(conceptId));
			cd.setStatedDescendants(descend);
		}
		
		tc=null;
	}

    public static void deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File child = new File(dir, children[i]);
                child.delete();
            }
        }
    }

	public void freeStep1() {
		descriptions =  null;
		simpleMembers =  null;
		assocMembers =  null;
		attrMembers = null;
		tdefMembers =  null;
		simpleMapMembers =  null;
		languageMembers =  null;
		notLeafInferred= null;
		notLeafStated= null;
		cptFSN =  null;
		langCodes = null;
		System.gc();
	}

    private void processFiles(HashSet<String> files, List<String> modulesToIgnore) throws IOException, Exception {
        for (String file:files){
            String pattern=FileHelper.getFileTypeByHeader(new File(file));

            if (pattern.equals("rf2-relationships")){
                loadRelationshipsFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-textDefinition")){
                loadTextDefinitionFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-association")){
                loadAssociationFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-association-2")){
                loadAssociationFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-attributevalue")){
                loadAttributeFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-language")){
                loadLanguageRefsetFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-simple")){
                loadSimpleRefsetFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-orderRefset")){
                // TODO: add process to order refset
                loadSimpleRefsetFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-simplemaps")){
                loadSimpleMapRefsetFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-descriptions")){
                loadDescriptionsFile(new File(file), modulesToIgnore);
            }else if(pattern.equals("rf2-concepts")){
                loadConceptsFile(new File(file), modulesToIgnore);
            }else{}
        }
    }

	private HashSet<String> getFilesFromFolders(HashSet<String> folders) throws IOException, Exception {
        HashSet<String> result = new HashSet<String>();
		FileHelper fHelper=new FileHelper();
		for (String folder:folders){
			File dir=new File(folder);
			HashSet<String> files=new HashSet<String>();
			fHelper.findAllFiles(dir, files);
            result.addAll(files);
		}
        return result;

	}

	public void loadConceptsFile(File conceptsFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting Concepts: " + conceptsFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(conceptsFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				String conceptId = columns[0];
                ConceptDescriptor loopConcept;
				if (concepts.containsKey(conceptId)){
					loopConcept=concepts.get(conceptId);
					if (columns[1].compareTo(loopConcept.getEffectiveTime())<=0){
						line=br.readLine();
						continue;
					}
				}
				loopConcept = new ConceptDescriptor();
				loopConcept.setConceptId(conceptId);
				loopConcept.setActive(columns[2].equals("1"));
				loopConcept.setEffectiveTime(columns[1]);
				loopConcept.setModule(newLightConceptDescriptor(columns[3]));
                modulesSet.add(loopConcept.getModule().getConceptId());
				loopConcept.setDefinitionStatus(newLightConceptDescriptor(columns[4]));
				concepts.put(conceptId, loopConcept);
				count++;
				if (count % 100000 == 0) {
					System.out.print(".");
				}
                line = br.readLine();
			}
			System.out.println(".");
			System.out.println("Concepts loaded = " + concepts.size() + " (" + count + ")");
		} finally {
			br.close();
		}
	}

	public void loadDescriptionsFile(File descriptionsFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting Descriptions: " + descriptionsFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(descriptionsFile), "UTF8"));
		int descriptionsCount = 0;
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			boolean act;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				LightDescription loopDescription = new LightDescription();
				loopDescription.setDescriptionId(columns[0]);
				act = columns[2].equals("1");
				loopDescription.setActive(act);
				loopDescription.setEffectiveTime(columns[1]);
				String sourceId = columns[4];
				loopDescription.setConceptId(sourceId);
				loopDescription.setType(columns[6]);
				loopDescription.setTerm(columns[7]);
				loopDescription.setIcs(columns[8]);
				loopDescription.setStringModule(columns[3]);
                modulesSet.add(columns[3]);
				loopDescription.setLang(columns[5]);
				List<LightDescription> list = descriptions.get(sourceId);
				if (list == null) {
					list = new ArrayList<LightDescription>();
				}
				list.add(loopDescription);
				descriptions.put(sourceId, list);

				line = br.readLine();
				descriptionsCount++;
				if (descriptionsCount % 100000 == 0) {
					System.out.print(".");
				}
			}
			System.out.println(".");
			System.out.println("Descriptions loaded = " + descriptions.size() + " (" + descriptionsCount + ")");
		} finally {
			br.close();
		}
	}

	private void completeModuleAndDefStatus(){

		for (String sourceId:concepts.keySet()){
			String moduleId= concepts.get(sourceId).getModule().getConceptId();
			concepts.get(sourceId).getModule().setPreferredTerm(concepts.get(moduleId).getPreferredTerm());
			

			String defStatusId= concepts.get(sourceId).getDefinitionStatus().getConceptId();
			concepts.get(sourceId).getDefinitionStatus().setPreferredTerm(concepts.get(defStatusId).getPreferredTerm());
		}
	}
	public void completeDefaultTerm(){
		boolean act;
		String type;
		String lang;
		System.out.println("Starting Default Terms computation");
		int count = 0;
		for (String sourceId:concepts.keySet()){
			List<LightDescription> lDescriptions = descriptions.get(sourceId);
			if (lDescriptions!=null){
				String lastTerm = "No descriptions";
				String enFsn = null;
				String langFsn = null;
				String configFsn = null;
				String userSelectedDefaultTermByLangCode = null;
				String userSelectedDefaultTermByRefset = null;
				for (LightDescription desc:lDescriptions){

					act=desc.isActive();
					type=String.valueOf(desc.getType());
					lang=desc.getLang();


					if (act && type.equals("900000000000003001") && lang.equals("en")) {
						enFsn = desc.getTerm();
					}
					if (act && type.equals("900000000000003001") && lang.equals(defaultLangCode)) {
						langFsn = desc.getTerm();
					}
					if (act && type.equals(defaultTermType.toString()) && lang.equals(defaultLangCode)) {
						userSelectedDefaultTermByLangCode = desc.getTerm();
					}

					if (act && type.equals(defaultTermType.toString())) {
						List<LightLangMembership> listLLM = languageMembers.get(desc.getDescriptionId());
						if (listLLM != null) {
							for (LightLangMembership llm : listLLM) {
								if (llm.getAcceptability().equals("900000000000548007") && llm.getRefset().equals(defaultLangRefset)) {
									if (desc.getType().equals("900000000000003001")) {
										configFsn = desc.getTerm();
									}
									userSelectedDefaultTermByRefset = desc.getTerm();
								}
							}
						}
					}

					lastTerm = desc.getTerm();

				}
				ConceptDescriptor loopConcept = concepts.get(sourceId);

				if (userSelectedDefaultTermByRefset != null) {
					loopConcept.setPreferredTerm(userSelectedDefaultTermByRefset);
				} else if (userSelectedDefaultTermByLangCode != null) {
					loopConcept.setPreferredTerm(userSelectedDefaultTermByLangCode);
				} else if (enFsn != null) {
					loopConcept.setPreferredTerm(enFsn);
				} else {
					loopConcept.setPreferredTerm(lastTerm);
				}
				concepts.put(sourceId, loopConcept);

				if (configFsn != null) {
					cptFSN.put(sourceId, configFsn);
				}else if (langFsn!=null){
					cptFSN.put(sourceId, langFsn);
				} else if (enFsn != null) {
					cptFSN.put(sourceId, enFsn);
				} else {
					cptFSN.put(sourceId, lastTerm);
				}

				if (count % 100000 == 0) {
					System.out.print(".");
				}
				count++;
			}
		}
		System.out.println(".");
		System.out.println("Default Terms computation completed");
	}
	public void loadTextDefinitionFile(File textDefinitionFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting Text Definitions: " + textDefinitionFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(textDefinitionFile), "UTF8"));
		int descriptionsCount = 0;
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			boolean act;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				LightDescription loopDescription = new LightDescription();
				loopDescription.setDescriptionId(columns[0]);
				act = columns[2].equals("1");
				loopDescription.setActive(act);
				loopDescription.setEffectiveTime(columns[1]);
				String sourceId = columns[4];
				loopDescription.setConceptId(sourceId);
				loopDescription.setType(columns[6]);
				loopDescription.setTerm(columns[7]);
				loopDescription.setIcs(columns[8]);
				loopDescription.setStringModule(columns[3]);
                modulesSet.add(columns[3]);
				loopDescription.setLang(columns[5]);
				List<LightDescription> list = tdefMembers.get(sourceId);
				if (list == null) {
					list = new ArrayList<LightDescription>();
				}
				list.add(loopDescription);
				tdefMembers.put(sourceId, list);

				line = br.readLine();
				descriptionsCount++;
				if (descriptionsCount % 100000 == 0) {
					System.out.print(".");
				}
			}
			System.out.println(".");
			System.out.println("Text Definitions loaded = " + tdefMembers.size() + " (" + descriptionsCount + ")");
		} finally {
			br.close();
		}
	}
	public void loadRelationshipsFile(File relationshipsFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting Relationships: " + relationshipsFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(relationshipsFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				LightRelationship loopRelationship = new LightRelationship();

				loopRelationship.setRelationshipId(columns[0]);
				loopRelationship.setActive(columns[2].equals("1"));
				loopRelationship.setEffectiveTime(columns[1]);
				loopRelationship.setStringModule(columns[3]);
                modulesSet.add(columns[3]);
				String targetId=columns[5];
				loopRelationship.setTarget(targetId);
				String type=columns[7];
				loopRelationship.setType(type);
				loopRelationship.setModifier(columns[9]);
				loopRelationship.setGroupId(Integer.parseInt(columns[6]));
				String sourceId = columns[4];
				loopRelationship.setSourceId(sourceId);
				String charType=columns[8];
				loopRelationship.setCharType(charType);

				List<LightRelationship> relList = relationships.get(sourceId);
				if (relList == null) {
					relList = new ArrayList<LightRelationship>();
				}
				relList.add(loopRelationship);
				relationships.put(sourceId, relList);

				/*if ( type.equals(isaSCTId) &&
						columns[2].equals("1")){
					List<LightRelationship> targetRelList = targetRelationships.get(targetId);
					if (targetRelList == null) {
						targetRelList = new ArrayList<LightRelationship>();
					}
					targetRelList.add(loopRelationship);
					targetRelationships.put(targetId, targetRelList);
				}*/
				if (loopRelationship.isActive() && type.equals(isaSCTId)){
					if ( charType.equals(inferred)){
						notLeafInferred.add(targetId);
					}else if ( charType.equals(stated)){
						notLeafStated.add(targetId);
					}
				}
				line = br.readLine();
				count++;
				if (count % 100000 == 0) {
					System.out.print(".");
				}
			}
			System.out.println(".");
			System.out.println("Relationships loaded = " + relationships.size() + " (" + count + ")");
		} finally {
			br.close();
		}
	}

	public void loadSimpleRefsetFile(File simpleRefsetFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting Simple Refset Members: " + simpleRefsetFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(simpleRefsetFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				if (columns[2].equals("1")) {
					LightRefsetMembership loopMember = new LightRefsetMembership();
					loopMember.setType(LightRefsetMembership.RefsetMembershipType.SIMPLE_REFSET.name());
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setStringModule(columns[3]);
                    modulesSet.add(columns[3]);
					String sourceId = columns[5];
					loopMember.setReferencedComponentId(sourceId);
					loopMember.setRefset(columns[4]);
                    refsetsSet.add(columns[4]);
                    refsetsTypes.put(columns[4], LightRefsetMembership.RefsetMembershipType.SIMPLE_REFSET.name());

					List<LightRefsetMembership> list = simpleMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightRefsetMembership>();
					}
					list.add(loopMember);
					simpleMembers.put(columns[5], list);

                    if (!refsetsCount.containsKey(loopMember.getRefset())) {
                        refsetsCount.put(loopMember.getRefset(), 0);
                    }
    				if (columns[2].equals("1")){
    					refsetsCount.put(loopMember.getRefset(), refsetsCount.get(loopMember.getRefset()) + 1);
    				}

					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("SimpleRefsetMember loaded = " + simpleMembers.size() + " (" + count + ")");
		} finally {
			br.close();
		}
	}

	public void loadAssociationFile(File associationsFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting Association Refset Members: " + associationsFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(associationsFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				if (columns[2].equals("1")) {
					LightRefsetMembership loopMember = new LightRefsetMembership();
					loopMember.setType(LightRefsetMembership.RefsetMembershipType.ASSOCIATION.name());
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setStringModule(columns[3]);
                    modulesSet.add(columns[3]);
					String sourceId = columns[5];
					loopMember.setReferencedComponentId(sourceId);
					loopMember.setRefset(columns[4]);
                    refsetsSet.add(columns[4]);
                    refsetsTypes.put(columns[4], LightRefsetMembership.RefsetMembershipType.ASSOCIATION.name());

                    loopMember.setCidValue(columns[6]);

					List<LightRefsetMembership> list = assocMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightRefsetMembership>();
					}
					list.add(loopMember);
					assocMembers.put(columns[5], list);

                    if (!refsetsCount.containsKey(loopMember.getRefset())) {
                        refsetsCount.put(loopMember.getRefset(), 0);
                    }

    				if (columns[2].equals("1")){
    					refsetsCount.put(loopMember.getRefset(), refsetsCount.get(loopMember.getRefset()) + 1);
    				}
                    count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("AssociationMember loaded = " + assocMembers.size() + " (" + count + ")");
		} finally {
			br.close();
		}
	}

	public void loadAttributeFile(File attributeFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting Attribute Refset Members: " + attributeFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(attributeFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				if (columns[2].equals("1")) {
					LightRefsetMembership loopMember = new LightRefsetMembership();
					loopMember.setType(LightRefsetMembership.RefsetMembershipType.ATTRIBUTE_VALUE.name());
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setStringModule(columns[3]);
                    modulesSet.add(columns[3]);
					String sourceId = columns[5];
					loopMember.setReferencedComponentId(sourceId);
					loopMember.setRefset(columns[4]);
                    refsetsSet.add(columns[4]);
                    refsetsTypes.put(columns[4], LightRefsetMembership.RefsetMembershipType.ATTRIBUTE_VALUE.name());

                    loopMember.setCidValue(columns[6]);

					List<LightRefsetMembership> list = attrMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightRefsetMembership>();
					}
					list.add(loopMember);
					attrMembers.put(columns[5], list);

                    if (!refsetsCount.containsKey(loopMember.getRefset())) {
                        refsetsCount.put(loopMember.getRefset(), 0);
                    }

    				if (columns[2].equals("1")){
    					refsetsCount.put(loopMember.getRefset(), refsetsCount.get(loopMember.getRefset()) + 1);
    				}
                    count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("AttributeMember loaded = " + attrMembers.size() + " (" + count + ")");
		} finally {
			br.close();
		}
	}
	public void loadSimpleMapRefsetFile(File simpleMapRefsetFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting SimpleMap Refset Members: " + simpleMapRefsetFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(simpleMapRefsetFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				if (columns[2].equals("1")) {
					LightRefsetMembership loopMember = new LightRefsetMembership();
					loopMember.setType(LightRefsetMembership.RefsetMembershipType.SIMPLEMAP.name());
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setStringModule(columns[3]);
                    modulesSet.add(columns[3]);
					String sourceId = columns[5];
					loopMember.setReferencedComponentId(sourceId);
					loopMember.setRefset(columns[4]);
                    refsetsSet.add(columns[4]);
                    refsetsTypes.put(columns[4], LightRefsetMembership.RefsetMembershipType.SIMPLEMAP.name());

                    loopMember.setOtherValue(columns[6]);

					List<LightRefsetMembership> list = simpleMapMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightRefsetMembership>();
					}
					list.add(loopMember);
					simpleMapMembers.put(sourceId, list);

                    if (!refsetsCount.containsKey(loopMember.getRefset())) {
                        refsetsCount.put(loopMember.getRefset(), 0);
                    }

    				if (columns[2].equals("1")){
    					refsetsCount.put(loopMember.getRefset(), refsetsCount.get(loopMember.getRefset()) + 1);
    				}
                    count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("SimpleMap RefsetMember loaded = " + simpleMapMembers.size() + " (" + count + ")");
		} finally {
			br.close();
		}
	}

	public void loadLanguageRefsetFile(File languageRefsetFile, List<String> modulesToIgnore) throws FileNotFoundException, IOException {
		System.out.println("Starting Language Refset Members: " + languageRefsetFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(languageRefsetFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
                    line = br.readLine();
					continue;
				}
				String[] columns = line.split("\t",-1);
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				if (columns[2].equals("1")) {
					LightLangMembership loopMember = new LightLangMembership();
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setStringModule(columns[3]);
                    modulesSet.add(columns[3]);
					String sourceId = columns[5];
					loopMember.setDescriptionId(sourceId);
					loopMember.setRefset(columns[4]);
                    langRefsetsSet.add(columns[4]);

                    loopMember.setAcceptability(columns[6]);
					List<LightLangMembership> list = languageMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightLangMembership>();
					}
					list.add(loopMember);
					languageMembers.put(sourceId, list);
					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("LanguageMembers loaded = " + languageMembers.size() + " (" + count + ")");
		} finally {
			br.close();
		}
	}

	public void createConceptsJsonFile(String fileName, boolean createCompleteVersion) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		System.out.println("Starting creation of " + fileName);
        getCharConvTable();
		FileOutputStream fos = new FileOutputStream(fileName);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		Gson gson = new Gson();

		List<LightDescription> listLD = new ArrayList<LightDescription>();
		List<Description> listD = new ArrayList<Description>();

		List<LightLangMembership> listLLM = new ArrayList<LightLangMembership>();
		List<LangMembership> listLM = new ArrayList<LangMembership>();

		List<LightRelationship> listLR = new ArrayList<LightRelationship>();
		List<Relationship> listR = new ArrayList<Relationship>();

		List<LightRefsetMembership> listLRM = new ArrayList<LightRefsetMembership>();
		List<RefsetMembership> listRM = new ArrayList<RefsetMembership>();

        int count = 0;
        boolean firstWritten = false;
		for (String cptId : concepts.keySet()) {
            count++;
            if (count % 10000 == 0) {
                System.out.print(".");
            }
			Concept cpt = new Concept();
			ConceptDescriptor cptdesc = concepts.get(cptId);

			cpt.setV(version);
			cpt.setConceptId(cptId);
			cpt.setActive(cptdesc.isActive());
			cpt.setPreferredTerm(cptdesc.getPreferredTerm());
			cpt.setEffectiveTime(cptdesc.getEffectiveTime());
			cpt.setModule(cptdesc.getModule());
//			cpt.getModule().setPreferredTerm(concepts.get(cptdesc.getModule().getConceptId()).getPreferredTerm());
			cpt.setDefinitionStatus(cptdesc.getDefinitionStatus());
//			cpt.getDefinitionStatus().setPreferredTerm(concepts.get(cptdesc.getDefinitionStatus().getConceptId()).getPreferredTerm());
			cpt.setLeafInferred(!notLeafInferred.contains(cptId));
			cpt.setLeafStated(!notLeafStated.contains(cptId));
            cpt.setFullySpecifiedName(cptFSN.get(cptId));
			cpt.setStatedDescendants(cptdesc.getStatedDescendants());
			cpt.setInferredDescendants(cptdesc.getInferredDescendants());

            if (createCompleteVersion) {
				listA = getInferredAncestors(cptId,false);
				cpt.setInferredAncestors(listA);
				listA = getStatedAncestors(cptId,false);
				cpt.setStatedAncestors(listA);
            }


			listLD = descriptions.get(cptId);
			listD = new ArrayList<Description>();

			if (listLD != null) {
				String descId;
				for (LightDescription ldesc : listLD) {
					Description d = new Description();
					d.setActive(ldesc.isActive());
					d.setConceptId(ldesc.getConceptId());
					descId = ldesc.getDescriptionId();
					d.setDescriptionId(descId);
					d.setEffectiveTime(ldesc.getEffectiveTime());
					d.setCaseSignificance(newLightConceptDescriptor(ldesc.getIcs()));
					d.setTerm(ldesc.getTerm());
					d.setLength(ldesc.getTerm().length());
					d.setModule(newLightConceptDescriptor(ldesc.getStringModule()));
					d.setType(newLightConceptDescriptor(ldesc.getType()));
					d.setLanguageCode(ldesc.getLang());

                    if (createCompleteVersion) {
                        cpt.setFullySpecifiedName(cptFSN.get(cptId));
                        if (cptFSN.get(cptId).endsWith(")")) {
                            cpt.setSemtag(cpt.getFullySpecifiedName().substring(cpt.getFullySpecifiedName().lastIndexOf("(") + 1, cpt.getFullySpecifiedName().length() - 1));
                        }
                        String cleanTerm = d.getTerm().replace("(", "").replace(")", "").trim().toLowerCase();
                        if (manifest.isTextIndexNormalized()) {
                            String convertedTerm = convertTerm(cleanTerm);
                            String[] tokens = convertedTerm.toLowerCase().split("\\s+");
                            d.setWords(Arrays.asList(tokens));
                        } else {
                            String[] tokens = cleanTerm.toLowerCase().split("\\s+");
                            d.setWords(Arrays.asList(tokens));
                        }
                    }

					listLLM = languageMembers.get(descId);
					listLM = new ArrayList<LangMembership>();

					if (listLLM != null) {
						for (LightLangMembership llm : listLLM) {
							LangMembership lm = new LangMembership();

							lm.setActive(llm.isActive());
							lm.setDescriptionId(descId);
							lm.setEffectiveTime(llm.getEffectiveTime());
							lm.setModule(newLightConceptDescriptor(llm.getStringModule()));
							lm.setAcceptability(newLightConceptDescriptor(llm.getAcceptability()));
							lm.setLanguageReferenceSet(newLightConceptDescriptor(llm.getRefset()));
							lm.setUuid(llm.getUuid());

							listLM.add(lm);

						}
						if (listLM.isEmpty()) {
							d.setLangMemberships(null);
						} else {
							d.setLangMemberships(listLM);
						}
					}

					listLRM = attrMembers.get(descId);
					listRM = new ArrayList<RefsetMembership>();
					if (listLRM != null) {
						for (LightRefsetMembership lrm : listLRM) {
							RefsetMembership rm = new RefsetMembership();
							rm.setEffectiveTime(lrm.getEffectiveTime());
							rm.setActive(lrm.isActive());
							rm.setModule(newLightConceptDescriptor(lrm.getStringModule()));
							rm.setUuid(lrm.getUuid());

							rm.setReferencedComponentId(descId);
							rm.setRefset(newLightConceptDescriptor(lrm.getRefset()));
							rm.setType(lrm.getType());
							rm.setCidValue(newLightConceptDescriptor(lrm.getCidValue()));

							listRM.add(rm);
						}
						if (listRM.isEmpty()){
							d.setRefsetMemberships(null);
						}else{
							d.setRefsetMemberships(listRM);
						}
					}else{
						d.setRefsetMemberships(null);
					}

					listD.add(d);
				}
			}

			listLD = tdefMembers.get(cptId);
			if (listLD != null) {
				String descId;
				for (LightDescription ldesc : listLD) {
					Description d = new Description();
					d.setActive(ldesc.isActive());
					d.setConceptId(ldesc.getConceptId());
					descId = ldesc.getDescriptionId();
					d.setDescriptionId(descId);
					d.setEffectiveTime(ldesc.getEffectiveTime());
					d.setCaseSignificance(newLightConceptDescriptor(ldesc.getIcs()));
					d.setTerm(ldesc.getTerm());
					d.setLength(ldesc.getTerm().length());
					d.setModule(newLightConceptDescriptor(ldesc.getStringModule()));
					d.setType(newLightConceptDescriptor(ldesc.getType()));
					d.setLanguageCode(ldesc.getLang());

					listLLM = languageMembers.get(descId);
					listLM = new ArrayList<LangMembership>();

					if (listLLM != null) {
						for (LightLangMembership llm : listLLM) {
							LangMembership lm = new LangMembership();

							lm.setActive(llm.isActive());
							lm.setDescriptionId(descId);
							lm.setEffectiveTime(llm.getEffectiveTime());
							lm.setModule(newLightConceptDescriptor(llm.getStringModule()));
							lm.setAcceptability(newLightConceptDescriptor(llm.getAcceptability()));
							lm.setLanguageReferenceSet(newLightConceptDescriptor(llm.getRefset()));
							lm.setUuid(llm.getUuid());

							listLM.add(lm);

						}
						if (listLM.isEmpty()) {
							d.setLangMemberships(null);
						} else {
							d.setLangMemberships(listLM);
						}
					}
					listD.add(d);
				}
			}
			if (listD!=null && !listD.isEmpty()){
				cpt.setDescriptions(listD);
			} else {
				cpt.setDescriptions(null);
			}
			listLR = relationships.get(cptId);
			listR = new ArrayList<Relationship>();
			if (listLR != null) {
				for (LightRelationship lrel : listLR) {
//					if (lrel.getCharType().equals("900000000000010007")) {
						Relationship r = new Relationship();
						r.setRelationshipId(lrel.getRelationshipId());
						r.setEffectiveTime(lrel.getEffectiveTime());
						r.setActive(lrel.isActive());
						r.setModule(newLightConceptDescriptor(lrel.getStringModule()));
						r.setRelationshipGroup(lrel.getGroupId());
						r.setModifier(newLightConceptDescriptor(MODIFIER_ID));
						r.setSourceId(cptId);
						r.setDestination(concepts.get(lrel.getTarget()));
						r.getDestination().setFullySpecifiedName(cptFSN.get(lrel.getTarget()));
						r.setType(newLightConceptDescriptor(lrel.getType()));
						r.setCharacteristicType(newLightConceptDescriptor(lrel.getCharType()));
						r.setTargetMemberships(getMemberships(lrel.getTarget()));
                        if (createCompleteVersion && lrel.isActive()) {
							listA = getInferredAncestors(lrel.getType(), true);
							r.setTypeInferredAncestors(listA);
							listA = getStatedAncestors(lrel.getType(),true);
							r.setTypeStatedAncestors(listA);
							listA = getInferredAncestors(lrel.getTarget(),false);
							r.setTargetInferredAncestors(listA);
							listA = getStatedAncestors(lrel.getTarget(),false);
							r.setTargetStatedAncestors(listA);
						}

						listR.add(r);
//					}
				}

				if (listR.isEmpty()) {
					cpt.setRelationships(null);
				} else {
					cpt.setRelationships(listR);
				}
			} else {
				cpt.setRelationships(null);
			}

//			listLR = relationships.get(cptId);
//			listR = new ArrayList<Relationship>();
//			if (listLR != null) {
//				for (LightRelationship lrel : listLR) {
//					if (lrel.getCharType().equals("900000000000011006")) {
//						Relationship r = new Relationship();
//						r.setEffectiveTime(lrel.getEffectiveTime());
//						r.setActive(lrel.isActive());
//						r.setModule(newLightConceptDescriptor(lrel.getStringModule()));
//						r.setRelationshipGroup(lrel.getGroupId());
//						r.setModifier(newLightConceptDescriptor(MODIFIER_ID));
//						r.setSourceId(cptId);
//						r.setDestination(concepts.get(lrel.getTarget()));
//						r.setType(newLightConceptDescriptor(lrel.getType()));
//						r.setCharacteristicType(newLightConceptDescriptor(lrel.getCharType()));
//						r.setTargetMemberships(getMemberships(lrel.getTarget()));
//                        if (createCompleteVersion) {
//							listA = getInferredAncestors(lrel.getType(), true);
//							r.setTypeInferredAncestors(listA);
//							listA = getStatedAncestors(lrel.getType(),true);
//							r.setTypeStatedAncestors(listA);
//							listA = getInferredAncestors(lrel.getTarget(),false);
//							r.setTargetInferredAncestors(listA);
//							listA = getStatedAncestors(lrel.getTarget(),false);
//							r.setTargetStatedAncestors(listA);
//                        }
//
//						listR.add(r);
//					}
//				}
//
//				if (listR.isEmpty()) {
//					cpt.setRelationships(null);
//				} else {
//					cpt.setRelationships(listR);
//				}
//			} else {
//				cpt.setRelationships(null);
//			}
//
//			listLR = relationships.get(cptId);
//			listR = new ArrayList<Relationship>();
//			if (listLR != null) {
//				for (LightRelationship lrel : listLR) {
//					if (lrel.getCharType().equals("900000000000227009")) {
//						Relationship r = new Relationship();
//						r.setEffectiveTime(lrel.getEffectiveTime());
//						r.setActive(lrel.isActive());
//						r.setModule(newLightConceptDescriptor(lrel.getStringModule()));
//						r.setRelationshipGroup(lrel.getGroupId());
//						r.setModifier(newLightConceptDescriptor(MODIFIER_ID));
//						r.setSourceId(cptId);
//						r.setDestination(concepts.get(lrel.getTarget()));
//						r.setType(newLightConceptDescriptor(lrel.getType()));
//						r.setCharacteristicType(newLightConceptDescriptor(lrel.getCharType()));
//						r.setTargetMemberships(getMemberships(lrel.getTarget()));
//
//                        if (createCompleteVersion) {
//							listA = getInferredAncestors(lrel.getType(), true);
//							r.setTypeInferredAncestors(listA);
//							listA = getStatedAncestors(lrel.getType(),true);
//							r.setTypeStatedAncestors(listA);
//							listA = getInferredAncestors(lrel.getTarget(),false);
//							r.setTargetInferredAncestors(listA);
//							listA = getStatedAncestors(lrel.getTarget(),false);
//							r.setTargetStatedAncestors(listA);
//                        }
//
//						listR.add(r);
//					}
//				}
//
//				if (listR.isEmpty()) {
//					cpt.setAdditionalRelationships(null);
//				} else {
//					cpt.setAdditionalRelationships(listR);
//				}
//			} else {
//				cpt.setAdditionalRelationships(null);
//			}
			listLRM = simpleMembers.get(cptId);
			listRM = new ArrayList<RefsetMembership>();
			if (listLRM != null) {
				for (LightRefsetMembership lrm : listLRM) {
					RefsetMembership d = new RefsetMembership();
					d.setEffectiveTime(lrm.getEffectiveTime());
					d.setActive(lrm.isActive());
					d.setModule(newLightConceptDescriptor(lrm.getStringModule()));
					d.setUuid(lrm.getUuid());

					d.setReferencedComponentId(cptId);
					d.setRefset(newLightConceptDescriptor(lrm.getRefset()));
					d.setType(lrm.getType());

					listRM.add(d);
				}
			}

			listLRM = simpleMapMembers.get(cptId);
			if (listLRM != null) {
				for (LightRefsetMembership lrm : listLRM) {
					RefsetMembership d = new RefsetMembership();
					d.setEffectiveTime(lrm.getEffectiveTime());
					d.setActive(lrm.isActive());
					d.setModule(newLightConceptDescriptor(lrm.getStringModule()));
					d.setUuid(lrm.getUuid());

					d.setReferencedComponentId(cptId);
					d.setRefset(newLightConceptDescriptor(lrm.getRefset()));
					d.setType(lrm.getType());
					d.setOtherValue(lrm.getOtherValue());

					listRM.add(d);
				}
			}
			listLRM = assocMembers.get(cptId);
			if (listLRM != null) {
				for (LightRefsetMembership lrm : listLRM) {
					RefsetMembership d = new RefsetMembership();
					d.setEffectiveTime(lrm.getEffectiveTime());
					d.setActive(lrm.isActive());
					d.setModule(newLightConceptDescriptor(lrm.getStringModule()));
					d.setUuid(lrm.getUuid());

					d.setReferencedComponentId(cptId);
					d.setRefset(newLightConceptDescriptor(lrm.getRefset()));
					d.setType(lrm.getType());
					d.setCidValue(newLightConceptDescriptor(lrm.getCidValue()));

					listRM.add(d);
				}
			}
			listLRM = attrMembers.get(cptId);
			if (listLRM != null) {
				for (LightRefsetMembership lrm : listLRM) {
					RefsetMembership d = new RefsetMembership();
					d.setEffectiveTime(lrm.getEffectiveTime());
					d.setActive(lrm.isActive());
					d.setModule(newLightConceptDescriptor(lrm.getStringModule()));
					d.setUuid(lrm.getUuid());

					d.setReferencedComponentId(cptId);
					d.setRefset(newLightConceptDescriptor(lrm.getRefset()));
					d.setType(lrm.getType());
					d.setCidValue(newLightConceptDescriptor(lrm.getCidValue()));

					listRM.add(d);
				}
			}
			if (listRM.isEmpty()) {
				cpt.setMemberships(null);
			} else {
				cpt.setMemberships(listRM);
			}

			bw.append(gson.toJson(cpt).toString());
			bw.append(sep);

            if (!firstWritten) {
                firstWritten = true;
                //System.out.println(gson.toJson(cpt).toString());
            }
		}
		bw.close();
		calculatedStatedAncestors=null;
		calculatedInferredAncestors=null;
		calculatedStatedAncestorsForRelType=null;
		calculatedInferredAncestorsForRelType=null;
        System.out.println(".");
		System.out.println(fileName + " Done");
	}
	
	private LightConceptDescriptor newLightConceptDescriptor(String id) {
		LightConceptDescriptor lcd=new LightConceptDescriptor();
		if (id==null){
			lcd.setConceptId("NULL");
			lcd.setPreferredTerm("Deleted concept");
		}else{
			lcd.setConceptId(id);
			if (concepts.get(id)==null){
				lcd.setPreferredTerm("Deleted concept");
			}else{
				lcd.setPreferredTerm(concepts.get(id).getPreferredTerm());
			}
		}
		return lcd;
	}
	
	private List<String> getMemberships(String target) {
		List<String>ret;
		List<LightRefsetMembership> listLRM = simpleMembers.get(target);
		if (listLRM != null) {
			ret=new ArrayList<String>();
			for (LightRefsetMembership lrm : listLRM) {
				ret.add(lrm.getRefset());
			}
			if (ret.size()>0){
				return ret;
			}
		}
		return null;
	}

	public String getDefaultLangCode() {
		return defaultLangCode;
	}

	public void setDefaultLangCode(String defaultLangCode) {
		this.defaultLangCode = defaultLangCode;
	}

	private List<String> getInferredAncestors(String cptId,boolean isType) {
		if (isType){
			if (calculatedInferredAncestorsForRelType.containsKey(cptId)){
				return calculatedInferredAncestorsForRelType.get(cptId);
			}
		}else{

			if (calculatedInferredAncestors.containsKey(cptId)){
				return calculatedInferredAncestors.get(cptId);
			}
		}
		List<String> ret=new ArrayList<String>();
		List<LightRelationship> listLR = new ArrayList<LightRelationship>();

		listLR = relationships.get(cptId);
		if (listLR != null) {
			for (LightRelationship lrel : listLR) {
				if (lrel.getCharType().equals(inferred) &&
						lrel.getType().equals(isaSCTId) &&
						lrel.isActive()) {
					String tgt=lrel.getTarget();
					if (isType && (tgt.equals(CONCEPT_MODEL_ATTRIBUTE) || tgt.equals(ATTRIBUTE))){
						continue;
					}
					if (!ret.contains(tgt)){
						List<String> tmpl=getInferredAncestors(tgt, isType);
						if (tmpl!=null){
							for (String id:tmpl){
								if (!ret.contains(id)){
									ret.add(id);
								}
							}
						}
						ret.add(tgt);
					}
				}
			}
		}

//		if (ret.size()==0){
//			ret=null;
//		}
		if(isType ){
			if (ret.size()==0){
				if (processInMemory){
					calculatedInferredAncestorsForRelType.put(cptId, null);
				}else{
					calculatedInferredAncestorsForRelType.put(cptId, emptyList);
				}
			}else{
				calculatedInferredAncestorsForRelType.put(cptId, ret);
				
			}
		}else{

			if (ret.size()==0){
				if (processInMemory){
					calculatedInferredAncestors.put(cptId, null);
				}else{
					calculatedInferredAncestors.put(cptId, emptyList);
				}
			}else{
				calculatedInferredAncestors.put(cptId, ret);
				
			}
		}

		if (ret.size()==0){
			return null;
		}
		return ret;
	}

	private List<String> getStatedAncestors(String cptId,boolean isType) {
		if (isType){
			if (calculatedStatedAncestorsForRelType.containsKey(cptId)){
				return calculatedStatedAncestorsForRelType.get(cptId);
			}
		}else{
			if (calculatedStatedAncestors.containsKey(cptId)){
				return calculatedStatedAncestors.get(cptId);
			}
		}
		List<String> ret=new ArrayList<String>();
		List<LightRelationship> listLR = new ArrayList<LightRelationship>();

		listLR = relationships.get(cptId);
		if (listLR != null) {
			for (LightRelationship lrel : listLR) {
				if (lrel.getCharType().equals(stated) &&
						lrel.getType().equals(isaSCTId) &&
						lrel.isActive()) {
					String tgt=lrel.getTarget();
					if (isType && (tgt.equals(CONCEPT_MODEL_ATTRIBUTE) || tgt.equals(ATTRIBUTE))){
						continue;
					}
					if (!ret.contains(tgt)){
						List<String> tmpl=getStatedAncestors(tgt,isType);
						if (tmpl!=null){
							for (String id:tmpl){
								if (!ret.contains(id)){
									ret.add(id);
								}
							}
						}
						ret.add(tgt);
					}
				}
			}
		}
//		if (ret.size()==0){
//			ret=null;
//		}
		
		if(isType ){
			if (ret.size()==0){
				if (processInMemory){
					calculatedStatedAncestorsForRelType.put(cptId, null);
				}else{
					calculatedStatedAncestorsForRelType.put(cptId, emptyList);
				}
			}else{
				calculatedStatedAncestorsForRelType.put(cptId, ret);
				
			}
		}else{

			if (ret.size()==0){
				if (processInMemory){
					calculatedStatedAncestors.put(cptId, null);
				}else{
					calculatedStatedAncestors.put(cptId, emptyList);
				}
			}else{
				calculatedStatedAncestors.put(cptId, ret);
				
			}
		}

		if (ret.size()==0){
			return null;
		}
		return ret ;
	}

	public void createTextIndexFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		getCharConvTable();
		System.out.println("Starting creation of " + fileName);
		FileOutputStream fos = new FileOutputStream(fileName);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		Gson gson = new Gson();
        int count = 0;
		for (String conceptId : descriptions.keySet()) {
            count++;
            if (count % 10000 == 0) {
                System.out.print(".");
            }
			for (LightDescription ldesc : descriptions.get(conceptId)) {
				TextIndexDescription d = new TextIndexDescription();
				d.setActive(ldesc.isActive());
				if (ldesc.getTerm().length()>1000) {
					d.setTerm(ldesc.getTerm().substring(0, 1000));
				}else {
					d.setTerm(ldesc.getTerm());
				}
				d.setLength(d.getTerm().length());
				d.setTypeId(ldesc.getType());
				d.setConceptId(ldesc.getConceptId());
				d.setDescriptionId(ldesc.getDescriptionId());
				d.setStringModule(ldesc.getStringModule());
				//TODO: using String lang names to support compatibility with Mongo 2.4.x text indexes
				d.setLanguageCode(langCodes.get(ldesc.getLang()));
				ConceptDescriptor concept = concepts.get(ldesc.getConceptId());
				d.setConceptModule(concept.getModule().getConceptId());
				d.setConceptActive(concept.isActive());
                d.setDefinitionStatus(concept.getDefinitionStatus().getConceptId());
                d.setFsn(cptFSN.get(conceptId));
				d.setSemanticTag("");
				if (d.getFsn().endsWith(")")) {
					d.setSemanticTag(d.getFsn().substring(d.getFsn().lastIndexOf("(") + 1, d.getFsn().length() - 1));
				}
				setIndexWords(d);
                d.setRefsetIds(new ArrayList<String>());

                // Refset index assumes that only active members are included in the db.
                List<LightRefsetMembership> listLRM = simpleMembers.get(concept.getConceptId());
                if (listLRM != null) {
                    for (LightRefsetMembership lrm : listLRM) {
                        d.getRefsetIds().add(lrm.getRefset());
                    }
                }
                listLRM = simpleMapMembers.get(concept.getConceptId());
                if (listLRM != null) {
                    for (LightRefsetMembership lrm : listLRM) {
                        d.getRefsetIds().add(lrm.getRefset());
                    }
                }
                listLRM = assocMembers.get(concept.getConceptId());
                if (listLRM != null) {
                    for (LightRefsetMembership lrm : listLRM) {
                        d.getRefsetIds().add(lrm.getRefset());
                    }
                }
                listLRM = attrMembers.get(concept.getConceptId());
                if (listLRM != null) {
                    for (LightRefsetMembership lrm : listLRM) {
                        d.getRefsetIds().add(lrm.getRefset());
                    }
                }

				bw.append(gson.toJson(d).toString());
				bw.append(sep);
			}
		}
		for (String conceptId : tdefMembers.keySet()) {

			count++;
			if (count % 10000 == 0) {
				System.out.print(".");
			}
			for (LightDescription ldesc : tdefMembers.get(conceptId)) {
				TextIndexDescription d = new TextIndexDescription();
				d.setActive(ldesc.getActive());
				if (ldesc.getTerm().length()>1000) {
					d.setTerm(ldesc.getTerm().substring(0, 1000));
				}else {
					d.setTerm(ldesc.getTerm());
				}
				d.setLength(d.getTerm().length());
				d.setTypeId(ldesc.getType());
				d.setConceptId(ldesc.getConceptId());
				d.setDescriptionId(ldesc.getDescriptionId());
				d.setStringModule(ldesc.getStringModule());

				d.setEffectiveTime(ldesc.getEffectiveTime());
				d.setLanguageCode(langCodes.get(ldesc.getLang()));
				ConceptDescriptor concept = concepts.get(ldesc.getConceptId());
				d.setConceptModule(concept.getModule().getConceptId());
				d.setConceptActive(concept.getActive());
				d.setDefinitionStatus(concept.getDefinitionStatus().getConceptId());
				d.setFsn(cptFSN.get(conceptId));
				d.setSemanticTag("");
				if (d.getFsn().endsWith(")")) {
					d.setSemanticTag(d.getFsn().substring(d.getFsn().lastIndexOf("(") + 1, d.getFsn().length() - 1));
				}
				setIndexWords(d);
				d.setRefsetIds(new ArrayList<String>());

				// Refset index assumes that only active members are included in the db.
				List<LightRefsetMembership> listLRM = simpleMembers.get(concept.getConceptId());
				if (listLRM != null) {
					for (LightRefsetMembership lrm : listLRM) {
						d.getRefsetIds().add(lrm.getRefset());
					}
				}
				listLRM = simpleMapMembers.get(concept.getConceptId());
				if (listLRM != null) {
					for (LightRefsetMembership lrm : listLRM) {
						d.getRefsetIds().add(lrm.getRefset());
					}
				}
				listLRM = assocMembers.get(concept.getConceptId());
				if (listLRM != null) {
					for (LightRefsetMembership lrm : listLRM) {
						d.getRefsetIds().add(lrm.getRefset());
					}
				}
				listLRM = attrMembers.get(concept.getConceptId());
				if (listLRM != null) {
					for (LightRefsetMembership lrm : listLRM) {
						d.getRefsetIds().add(lrm.getRefset());
					}
				}

				bw.append(gson.toJson(d).toString());
				bw.append(sep);
			}
		}

		bw.close();
        System.out.println(".");
		System.out.println(fileName + " Done");
	}

	private void setIndexWords(TextIndexDescription d) {
		String cleanTerm = d.getTerm().replace("(", "").replace(")", "").trim().toLowerCase();
		cleanTerm = cleanTerm.replace("[", " ").replace("]", " ").trim();
		String[] tokens;
		if (manifest.isTextIndexNormalized()) {
		    String convertedTerm = convertTerm(cleanTerm);
		    tokens = convertedTerm.toLowerCase().split("\\s+");
		} else {
		    tokens = cleanTerm.toLowerCase().split("\\s+");
		}
		HashSet<String> uniqueToken=getTokens(tokens);
		String[] arrtmp=new String[uniqueToken.size()];
		d.setWords(Arrays.asList(uniqueToken.toArray(arrtmp)));
	}
	
	private HashSet<String> getTokens(String[] token){
		HashSet<String> uniqueToken=new HashSet<String>();
		for (String word:token){
			for (String separator:wordSeparators){
				if (word.indexOf(separator)>-1){
					String[] spl=word.split(separator);
//					if recursive then
//					HashSet<String> tmp=getTokens(spl);
//					uniqueToken.addAll(tmp);
//					else
					for (String w:spl){
						uniqueToken.add(w);
					}
				}
				
			}
			uniqueToken.add(word);
		}
		return uniqueToken;
	}
    public void createManifestFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        System.out.println("Starting creation of " + fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);
        Gson gson = new Gson();

		if (modulesSet!=null){
			for (String moduleId : modulesSet) {

				ConceptDescriptor conceptDesc=concepts.get(moduleId);
				if (conceptDesc==null){
					System.out.println("ModuleId not in concept list:" + moduleId);
					continue;
				}
				manifest.getModules().add(concepts.get(moduleId));
			}
		}
		if (langRefsetsSet!=null){
			for (String langRefsetId : langRefsetsSet) {

				ConceptDescriptor conceptDesc=concepts.get(langRefsetId);
				if (conceptDesc==null){
					System.out.println("Lang RefsetId not in concept list:" + langRefsetId);
					continue;
				}
				manifest.getLanguageRefsets().add(concepts.get(langRefsetId));
			}
		}
		String type="";
		if (refsetsSet!=null){
			for (String refsetId : refsetsSet) {
				type=refsetsTypes.get(refsetId);
				ConceptDescriptor conceptDesc=concepts.get(refsetId);
				if (conceptDesc==null){
					System.out.println("RefsetId not in concept list:" + refsetId);
					continue;
				}
				RefsetDescriptor refsetDescriptor=new RefsetDescriptor(concepts.get(refsetId), refsetsCount.get(refsetId));
				refsetDescriptor.setType(type);
				manifest.getRefsets().add(refsetDescriptor);
			}
		}
        bw.append(gson.toJson(manifest).toString());

        bw.close();
        System.out.println(fileName + " Done");
    }

	private String convertTerm(String cleanTerm) {
		for (String code:charConv.keySet()){
			String test="\\u" + code;
			String repl=charConv.get(code);
			cleanTerm=cleanTerm.replaceAll(test, repl);
		}
		return cleanTerm;
	}

	private void getCharConvTable() throws IOException {

		//String charconvtable="src/main/resources/org/ihtsdo/util/char_conversion_table.txt";

		BufferedReader br = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream("char_conversion_table.txt"), "UTF8"));
		br.readLine();
		String line=null;
		charConv=new HashMap<String,String>();
		while ((line=br.readLine())!=null){
			String[] spl=line.split("\t",-1);
			String[]codes=spl[2].split(" ");
			for (String code:codes){

				charConv.put(code,spl[0]);
			}

		}
		br.close();
		System.gc();
	}

	public String getDefaultTermType() {
		return defaultTermType;
	}

	public void setDefaultTermType(String defaultTermType) {
		this.defaultTermType = defaultTermType;
	}

    public String getDefaultLangRefset() {
        return defaultLangRefset;
    }

    public void setDefaultLangRefset(String defaultLangRefset) {
        this.defaultLangRefset = defaultLangRefset;
    }
}
