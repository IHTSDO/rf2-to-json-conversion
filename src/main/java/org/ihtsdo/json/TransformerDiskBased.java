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
import org.ihtsdo.json.model.ConceptAncestor;
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

/**
 *
 * @author Alejandro Rodriguez
 */
public class TransformerDiskBased {

	private String MODIFIER = "Existential restriction";
	private String sep = System.getProperty("line.separator");

	private Map<String, ConceptDescriptor> concepts;
	private Map<String, List<LightDescription>> descriptions;
	private Map<String, List<LightRelationship>> relationships;
	private Map<String, List<LightRelationship>> targetRelationships;
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
	private ArrayList<String> listA;
	private Map<String, String> charConv;
	private Map<String, String> cptFSN;
	private HashSet<String> notLeafInferred;
	private HashSet<String> notLeafStated;
    private String valConfig;
    private ResourceSetManifest manifest;
    private Set<String> refsetsSet;
    private Set<String> langRefsetsSet;
    private Set<String> modulesSet;



    public TransformerDiskBased() throws IOException {
		langCodes = new HashMap<String, String>();
		langCodes.put("en", "english");
		langCodes.put("es", "spanish");
		langCodes.put("da", "danish");
		langCodes.put("sv", "swedish");
		langCodes.put("fr", "french");
		langCodes.put("nl", "dutch");

        valConfig= "config/validation-rules.xml";
	}

    public void convert(TransformerConfig config) throws Exception {
        if (config.isProcessInMemory()) {
            concepts = new HashMap<String, ConceptDescriptor>();
            descriptions = new HashMap<String, List<LightDescription>>();
            relationships = new HashMap<String, List<LightRelationship>>();
            targetRelationships = new HashMap<String, List<LightRelationship>>();
            simpleMembers = new HashMap<String, List<LightRefsetMembership>>();
            assocMembers = new HashMap<String, List<LightRefsetMembership>>();
            attrMembers = new HashMap<String, List<LightRefsetMembership>>();
            tdefMembers = new HashMap<String, List<LightDescription>>();
            simpleMapMembers = new HashMap<String, List<LightRefsetMembership>>();
            languageMembers = new HashMap<String, List<LightLangMembership>>();
            cptFSN = new HashMap<String, String>();
        } else {
            concepts = DBMaker.newTempHashMap();
            descriptions = DBMaker.newTempHashMap();
            relationships = DBMaker.newTempHashMap();
            targetRelationships = DBMaker.newTempHashMap();
            simpleMembers = DBMaker.newTempHashMap();
            assocMembers = DBMaker.newTempHashMap();
            attrMembers = DBMaker.newTempHashMap();
            tdefMembers = DBMaker.newTempHashMap();
            simpleMapMembers = DBMaker.newTempHashMap();
            languageMembers = DBMaker.newTempHashMap();
            cptFSN = DBMaker.newTempHashMap();
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
        manifest.setExpirationDate(config.getExpirationTime());
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

        completeDefaultTerm();
        File output = new File(config.getOutputFolder());
        output.mkdirs();
        createConceptsJsonFile(config.getOutputFolder() + "/concepts.json", config.isCreateCompleteConceptsFile());
        createTextIndexFile(config.getOutputFolder() + "/text-index.json");
        createManifestFile(config.getOutputFolder() + "/manifest.json");
//        createTClosures(files, config.getOutputFolder() + "/inferredTransitiveClosure.json", config.getOutputFolder() + "/statedTransitiveClosure.json");

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

	public void createTClosures(HashSet<String> folders, String transitiveClosureInferredFile,String transitiveClosureStatedFile) throws Exception {
		if (relationships==null || relationships.size()==0){
			getFilesForTransClosureProcess(folders);
		}
		createTClosure(transitiveClosureInferredFile,inferred);
		createTClosure(transitiveClosureStatedFile,stated);

	}
	
	private void getFilesForTransClosureProcess(HashSet<String> folders) throws IOException, Exception {

		concepts = new HashMap<String, ConceptDescriptor>();
		relationships = new HashMap<String, List<LightRelationship>>();
		FileHelper fHelper=new FileHelper();
		for (String folder:folders){
			File dir=new File(folder);
			HashSet<String> files=new HashSet<String>();
			fHelper.findAllFiles(dir, files);

			for (String file:files){
				String pattern=FileHelper.getFileTypeByHeader(new File(file));

				if (pattern.equals("rf2-relationships")){
					loadRelationshipsFile(new File(file), null);
				}else if(pattern.equals("rf2-concepts")){
					loadConceptsFile(new File(file), null);
				}else{}
			}
		}

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
				String[] columns = line.split("\\t");
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				ConceptDescriptor loopConcept = new ConceptDescriptor();
				String conceptId = columns[0];
				loopConcept.setConceptId(conceptId);
				loopConcept.setActive(columns[2].equals("1"));
				loopConcept.setEffectiveTime(columns[1]);
				loopConcept.setModule(columns[3]);
                modulesSet.add(loopConcept.getModule());
				loopConcept.setDefinitionStatus(columns[4].equals("900000000000074008") ? "Primitive" : "Fully defined");
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
				String[] columns = line.split("\\t");
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
				loopDescription.setModule(columns[3]);
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
					loopConcept.setDefaultTerm(userSelectedDefaultTermByRefset);
				} else if (userSelectedDefaultTermByLangCode != null) {
					loopConcept.setDefaultTerm(userSelectedDefaultTermByLangCode);
				} else if (enFsn != null) {
					loopConcept.setDefaultTerm(enFsn);
				} else {
					loopConcept.setDefaultTerm(lastTerm);
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
				String[] columns = line.split("\\t");
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
				loopDescription.setModule(columns[3]);
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
				String[] columns = line.split("\\t");
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				LightRelationship loopRelationship = new LightRelationship();

				loopRelationship.setActive(columns[2].equals("1"));
				loopRelationship.setEffectiveTime(columns[1]);
				loopRelationship.setModule(columns[3]);
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

				if ( type.equals(isaSCTId) &&
						columns[2].equals("1")){
					List<LightRelationship> targetRelList = targetRelationships.get(targetId);
					if (targetRelList == null) {
						targetRelList = new ArrayList<LightRelationship>();
					}
					targetRelList.add(loopRelationship);
					targetRelationships.put(targetId, targetRelList);
				}
				if (loopRelationship.isActive() && type.equals(isaSCTId)){
					if ( charType.equals(inferred)){
						notLeafInferred.add(targetId);
					}else{
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
				String[] columns = line.split("\\t");
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
					loopMember.setModule(columns[3]);
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
                    refsetsCount.put(loopMember.getRefset(), refsetsCount.get(loopMember.getRefset()) + 1);

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
				String[] columns = line.split("\\t");
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
					loopMember.setModule(columns[3]);
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
                    refsetsCount.put(loopMember.getRefset(), refsetsCount.get(loopMember.getRefset()) + 1);

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
				String[] columns = line.split("\\t");
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
					loopMember.setModule(columns[3]);
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
                    refsetsCount.put(loopMember.getRefset(), refsetsCount.get(loopMember.getRefset()) + 1);

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
				String[] columns = line.split("\\t");
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
					loopMember.setModule(columns[3]);
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
                    refsetsCount.put(loopMember.getRefset(), refsetsCount.get(loopMember.getRefset()) + 1);

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
				String[] columns = line.split("\\t");
                if (modulesToIgnore.contains(columns[3])) {
                    line = br.readLine();
                    continue;
                }
				if (columns[2].equals("1")) {
					LightLangMembership loopMember = new LightLangMembership();
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setModule(columns[3]);
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

			cpt.setConceptId(cptId);
			cpt.setActive(cptdesc.isActive());
			cpt.setDefaultTerm(cptdesc.getDefaultTerm());
			cpt.setEffectiveTime(cptdesc.getEffectiveTime());
			cpt.setModule(cptdesc.getModule());
			cpt.setDefinitionStatus(cptdesc.getDefinitionStatus());
			cpt.setLeafInferred(!notLeafInferred.contains(cptId));
			cpt.setLeafStated(!notLeafStated.contains(cptId));
            cpt.setFsn(cptFSN.get(cptId));

            if (createCompleteVersion) {
                listA = new ArrayList<String>();
                getAncestors(cptId,inferred);
                cpt.setInferredAncestors(listA);
                listA = new ArrayList<String>();
                getAncestors(cptId,stated);
                cpt.setStatedAncestors(listA);
                listA = new ArrayList<String>();
                getDescendants(cptId,inferred);
                cpt.setInferredDescendants(listA);
                listA = new ArrayList<String>();
                getDescendants(cptId,stated);
                cpt.setStatedDescendants(listA);
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
					d.setIcs(concepts.get(ldesc.getIcs()));
					d.setTerm(ldesc.getTerm());
					d.setLength(ldesc.getTerm().length());
					d.setModule(ldesc.getModule());
					d.setType(concepts.get(ldesc.getType()));
					d.setLang(ldesc.getLang());

                    if (createCompleteVersion) {
                        cpt.setFsn(cptFSN.get(cptId));
                        if (cptFSN.get(cptId).endsWith(")")) {
                            cpt.setSemtag(cpt.getFsn().substring(cpt.getFsn().lastIndexOf("(") + 1, cpt.getFsn().length() - 1));
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
							lm.setModule(llm.getModule());
							lm.setAcceptability(concepts.get(llm.getAcceptability()));
							lm.setRefset(concepts.get(llm.getRefset()));
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
							rm.setModule(lrm.getModule());
							rm.setUuid(lrm.getUuid());

							rm.setReferencedComponentId(descId);
							rm.setRefset(concepts.get(lrm.getRefset()));
							rm.setType(lrm.getType());
							rm.setCidValue(concepts.get(lrm.getCidValue()));

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
					d.setIcs(concepts.get(ldesc.getIcs()));
					d.setTerm(ldesc.getTerm());
					d.setLength(ldesc.getTerm().length());
					d.setModule(ldesc.getModule());
					d.setType(concepts.get(ldesc.getType()));
					d.setLang(ldesc.getLang());

					listLLM = languageMembers.get(descId);
					listLM = new ArrayList<LangMembership>();

					if (listLLM != null) {
						for (LightLangMembership llm : listLLM) {
							LangMembership lm = new LangMembership();

							lm.setActive(llm.isActive());
							lm.setDescriptionId(descId);
							lm.setEffectiveTime(llm.getEffectiveTime());
							lm.setModule(llm.getModule());
							lm.setAcceptability(concepts.get(llm.getAcceptability()));
							lm.setRefset(concepts.get(llm.getRefset()));
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
					if (lrel.getCharType().equals("900000000000010007")) {
						Relationship r = new Relationship();
						r.setEffectiveTime(lrel.getEffectiveTime());
						r.setActive(lrel.isActive());
						r.setModule(lrel.getModule());
						r.setGroupId(lrel.getGroupId());
						r.setModifier(MODIFIER);
						r.setSourceId(cptId);
						r.setTarget(concepts.get(lrel.getTarget()));
						r.setType(concepts.get(lrel.getType()));
						r.setCharType(concepts.get(lrel.getCharType()));

                        if (createCompleteVersion) {
                            listA = new ArrayList<String>();
                            getAncestors(lrel.getType(),inferred);
                            r.setTypeInferredAncestors(listA);
                            listA = new ArrayList<String>();
                            getAncestors(lrel.getType(),stated);
                            r.setTypeStatedAncestors(listA);
                            listA = new ArrayList<String>();
                            getAncestors(lrel.getTarget(),inferred);
                            r.setTargetInferredAncestors(listA);
                            listA = new ArrayList<String>();
                            getAncestors(lrel.getTarget(),stated);
                            r.setTargetStatedAncestors(listA);
                        }

						listR.add(r);
					}
				}

				if (listR.isEmpty()) {
					cpt.setStatedRelationships(null);
				} else {
					cpt.setStatedRelationships(listR);
				}
			} else {
				cpt.setStatedRelationships(null);
			}

			listLR = relationships.get(cptId);
			listR = new ArrayList<Relationship>();
			if (listLR != null) {
				for (LightRelationship lrel : listLR) {
					if (lrel.getCharType().equals("900000000000011006")) {
						Relationship r = new Relationship();
						r.setEffectiveTime(lrel.getEffectiveTime());
						r.setActive(lrel.isActive());
						r.setModule(lrel.getModule());
						r.setGroupId(lrel.getGroupId());
						r.setModifier(MODIFIER);
						r.setSourceId(cptId);
						r.setTarget(concepts.get(lrel.getTarget()));
						r.setType(concepts.get(lrel.getType()));
						r.setCharType(concepts.get(lrel.getCharType()));

                        if (createCompleteVersion) {
                            listA = new ArrayList<String>();
                            getAncestors(lrel.getType(),inferred);
                            r.setTypeInferredAncestors(listA);
                            listA = new ArrayList<String>();
                            getAncestors(lrel.getType(),stated);
                            r.setTypeStatedAncestors(listA);
                            listA = new ArrayList<String>();
                            getAncestors(lrel.getTarget(),inferred);
                            r.setTargetInferredAncestors(listA);
                            listA = new ArrayList<String>();
                            getAncestors(lrel.getTarget(),stated);
                            r.setTargetStatedAncestors(listA);
                        }

						listR.add(r);
					}
				}

				if (listR.isEmpty()) {
					cpt.setRelationships(null);
				} else {
					cpt.setRelationships(listR);
				}
			} else {
				cpt.setRelationships(null);
			}

			listLRM = simpleMembers.get(cptId);
			listRM = new ArrayList<RefsetMembership>();
			if (listLRM != null) {
				for (LightRefsetMembership lrm : listLRM) {
					RefsetMembership d = new RefsetMembership();
					d.setEffectiveTime(lrm.getEffectiveTime());
					d.setActive(lrm.isActive());
					d.setModule(lrm.getModule());
					d.setUuid(lrm.getUuid());

					d.setReferencedComponentId(cptId);
					d.setRefset(concepts.get(lrm.getRefset()));
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
					d.setModule(lrm.getModule());
					d.setUuid(lrm.getUuid());

					d.setReferencedComponentId(cptId);
					d.setRefset(concepts.get(lrm.getRefset()));
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
					d.setModule(lrm.getModule());
					d.setUuid(lrm.getUuid());

					d.setReferencedComponentId(cptId);
					d.setRefset(concepts.get(lrm.getRefset()));
					d.setType(lrm.getType());
					d.setCidValue(concepts.get(lrm.getCidValue()));

					listRM.add(d);
				}
			}
			listLRM = attrMembers.get(cptId);
			if (listLRM != null) {
				for (LightRefsetMembership lrm : listLRM) {
					RefsetMembership d = new RefsetMembership();
					d.setEffectiveTime(lrm.getEffectiveTime());
					d.setActive(lrm.isActive());
					d.setModule(lrm.getModule());
					d.setUuid(lrm.getUuid());

					d.setReferencedComponentId(cptId);
					d.setRefset(concepts.get(lrm.getRefset()));
					d.setType(lrm.getType());
					d.setCidValue(concepts.get(lrm.getCidValue()));

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
        System.out.println(".");
		System.out.println(fileName + " Done");
	}

	private void getDescendants(String cptId, String charType) {

		List<LightRelationship> listLR = new ArrayList<LightRelationship>();

		listLR = targetRelationships.get(cptId);
		if (listLR != null) {
			for (LightRelationship lrel : listLR) {
				if (lrel.getCharType().equals(charType)) {
					String sourceId=lrel.getSourceId();
					if (!listA.contains(sourceId)){
						listA.add(sourceId);
						getDescendants(sourceId,charType);
					}
				}
			}
		}
		return ;		
	}

	public String getDefaultLangCode() {
		return defaultLangCode;
	}

	public void setDefaultLangCode(String defaultLangCode) {
		this.defaultLangCode = defaultLangCode;
	}

	private void createTClosure(String fileName,String charType) throws IOException {

		System.out.println("Transitive Closure creation from " + fileName);
		FileOutputStream fos = new FileOutputStream(fileName);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		Gson gson = new Gson();


//		int count = 0;
		for (String cptId : concepts.keySet()) {		

			listA = new ArrayList<String>();
			getAncestors(cptId,charType);
			if (!listA.isEmpty()){
				ConceptAncestor ca=new ConceptAncestor();

				ca.setConceptId(cptId);
				ca.setAncestor(listA);
				bw.append(gson.toJson(ca).toString());
				bw.append(sep);
			}
		}
		bw.close();
		System.out.println(fileName + " Done");
	}

	private void getAncestors(String cptId,String charType) {

		List<LightRelationship> listLR = new ArrayList<LightRelationship>();

		listLR = relationships.get(cptId);
		if (listLR != null) {
			for (LightRelationship lrel : listLR) {
				if (lrel.getCharType().equals(charType) &&
						lrel.getType().equals(isaSCTId) &&
						lrel.isActive()) {
					String tgt=lrel.getTarget();
					if (!listA.contains(tgt)){
						listA.add(tgt);
						getAncestors(tgt,charType);
					}
				}
			}
		}
		return ;
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
				d.setTerm(ldesc.getTerm());
				d.setLength(ldesc.getTerm().length());
				d.setTypeId(ldesc.getType());
				d.setConceptId(ldesc.getConceptId());
				d.setDescriptionId(ldesc.getDescriptionId());
				d.setModule(ldesc.getModule());
				//TODO: using String lang names to support compatibility with Mongo 2.4.x text indexes
				d.setLang(langCodes.get(ldesc.getLang()));
				ConceptDescriptor concept = concepts.get(ldesc.getConceptId());
				d.setConceptModule(concept.getModule());
				d.setConceptActive(concept.isActive());
                d.setDefinitionStatus(concept.getDefinitionStatus());
                d.setFsn(cptFSN.get(conceptId));
				d.setSemanticTag("");
				if (d.getFsn().endsWith(")")) {
					d.setSemanticTag(d.getFsn().substring(d.getFsn().lastIndexOf("(") + 1, d.getFsn().length() - 1));
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

    public void createManifestFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {
        System.out.println("Starting creation of " + fileName);
        FileOutputStream fos = new FileOutputStream(fileName);
        OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
        BufferedWriter bw = new BufferedWriter(osw);
        Gson gson = new Gson();

        for (String moduleId : modulesSet) {
            manifest.getModules().add(concepts.get(moduleId));
        }
        for (String langRefsetId : langRefsetsSet) {
            manifest.getLanguageRefsets().add(concepts.get(langRefsetId));
        }
        for (String refsetId : refsetsSet) {
            manifest.getRefsets().add(new RefsetDescriptor(concepts.get(refsetId), refsetsCount.get(refsetId)));
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
