/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.json;

import com.google.gson.Gson;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import org.ihtsdo.json.model.*;
import org.ihtsdo.json.utils.FileHelper;

import java.io.*;
import java.util.*;

/**
 *
 * @author Alejandro Rodriguez
 */
public class TransformerOnePass {

	private String MODIFIER = "Existential restriction";
	private String sep = System.getProperty("line.separator");

	private Map<Long, ConceptDescriptor> concepts;
	private Map<Long, List<LightDescription>> descriptions;
	private Map<Long, List<LightRelationship>> relationships;
	private Map<Long, List<LightRefsetMembership>> simpleMembers;
	private Map<Long, List<LightRefsetMembership>> simpleMapMembers;
	private Map<Long, List<LightLangMembership>> languageMembers;
	private Map<String, String> langCodes;

	private String defaultLangCode = "en";
	public String fsnType = "900000000000003001";
	public String synType = "900000000000013009";
	private Long inferred = 900000000000011006l;
	private Long stated = 900000000000010007l;
	private Long isaSCTId=116680003l;
	private String defaultTermType = fsnType;
	private Map<Long, List<LightDescription>> tdefMembers;
	private Map<Long, List<LightRefsetMembership>> attrMembers;
	private Map<Long, List<LightRefsetMembership>> assocMembers;
	private ArrayList<Long> listA;
	private Map<String, String> charConv;
	private Map<Long, String> cptFSN;
	private HashSet<Long> notLeafInferred;
	private HashSet<Long> notLeafStated;

    private RecordManager recMan;

	public TransformerOnePass() throws IOException {
        String fileName = "/Volumes/Macintosh HD2/conversiondb/conversiondb";
        deleteDir(new File("/Volumes/Macintosh HD2/conversiondb"));

        recMan = RecordManagerFactory.createRecordManager(fileName);
		concepts = recMan.hashMap("concepts");
		descriptions = recMan.hashMap("descriptions");
		relationships = recMan.hashMap("relationships");
		simpleMembers = recMan.hashMap("simpleMembers");
		assocMembers = recMan.hashMap("assocMembers");
		attrMembers = recMan.hashMap("attrMembers");
		tdefMembers = recMan.hashMap("tdefMembers");
		simpleMapMembers = recMan.hashMap("simpleMapMembers");
		languageMembers = recMan.hashMap("languageMembers");
		notLeafInferred=new HashSet<Long>();
		notLeafStated=new HashSet<Long>();
		cptFSN = recMan.hashMap("cptFSN");;

		langCodes = new HashMap<String, String>();
		langCodes.put("en", "english");
		langCodes.put("es", "spanish");
		langCodes.put("da", "danish");
		langCodes.put("sv", "swedish");
		langCodes.put("fr", "french");
		langCodes.put("nl", "dutch");
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

	public static void main(String[] args) throws Exception {
		TransformerOnePass tr = new TransformerOnePass();
		tr.setDefaultLangCode("en");
		tr.setDefaultTermType(tr.fsnType);

		HashSet<String> folders=new HashSet<String>();
		folders.add("/Volumes/Macintosh HD2/uk_sct2cl_17/SnomedCT_Release_INT_20140131/RF2Release/Snapshot");
		folders.add("/Volumes/Macintosh HD2/uk_sct2cl_17/SnomedCT2_GB1000000_20140401/RF2Release/Snapshot");
		folders.add("/Users/termmed/Downloads/SnomedCT_Release_US1000124_20140301/RF2Release/Snapshot");
		//folders.add("/Users/termmed/Downloads/SnomedCT_Release_AU1000036_20140531/RF2 Release/Snapshot");
		String valConfig= "config/validation-rules.xml";
        HashSet<String> files = tr.getFilesFromFolders(folders);
        tr.processFiles(files, valConfig);
		tr.createConceptsJsonFile("/Volumes/Macintosh HD2/Multi-english-data/concepts.json");
		tr.createTextIndexFile("/Volumes/Macintosh HD2/Multi-english-data/text-index.json");
		//tr.freeStep1();
		//tr.createTClosures(folders, valConfig, "/Volumes/Macintosh HD2/Multi-english-data/tclosure-inferred.json", "/Volumes/Macintosh HD2/tclosure-stated.json");
        tr.recMan.close();
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

    private void processFiles(HashSet<String> files, String validationConfig) throws IOException, Exception {
        File config=new File(validationConfig);
        for (String file:files){
            String pattern=FileHelper.getFileTypeByHeader(new File(file), config);

            if (pattern.equals("rf2-relationships")){
                loadRelationshipsFile(new File(file));
            }else if(pattern.equals("rf2-textDefinition")){
                loadTextDefinitionFile(new File(file));
            }else if(pattern.equals("rf2-association")){
                loadAssociationFile(new File(file));
            }else if(pattern.equals("rf2-association-2")){
                loadAssociationFile(new File(file));
            }else if(pattern.equals("rf2-attributevalue")){
                loadAttributeFile(new File(file));
            }else if(pattern.equals("rf2-language")){
                loadLanguageRefsetFile(new File(file));
            }else if(pattern.equals("rf2-simple")){
                loadSimpleRefsetFile(new File(file));
            }else if(pattern.equals("rf2-orderRefset")){
                // TODO: add process to order refset
                loadSimpleRefsetFile(new File(file));
            }else if(pattern.equals("rf2-simplemaps")){
                loadSimpleMapRefsetFile(new File(file));
            }else if(pattern.equals("rf2-descriptions")){
                loadDescriptionsFile(new File(file));
            }else if(pattern.equals("rf2-concepts")){
                loadConceptsFile(new File(file));
            }else{}
            recMan.clearCache();
        }
        completeDefaultTerm();
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

	public void createTClosures(HashSet<String> folders, String valConfig, String transitiveClosureInferredFile,String transitiveClosureStatedFile) throws Exception {
		if (relationships==null || relationships.size()==0){
			getFilesForTransClosureProcess(folders,valConfig);
		}
		createTClosure(transitiveClosureInferredFile,inferred);
		createTClosure(transitiveClosureStatedFile,stated);

	}
	
	private void getFilesForTransClosureProcess(HashSet<String> folders, String validationConfig) throws IOException, Exception {

		concepts = new HashMap<Long, ConceptDescriptor>();
		relationships = new HashMap<Long, List<LightRelationship>>();
		File config=new File(validationConfig);
		FileHelper fHelper=new FileHelper();
		for (String folder:folders){
			File dir=new File(folder);
			HashSet<String> files=new HashSet<String>();
			fHelper.findAllFiles(dir, files);

			for (String file:files){
				String pattern=FileHelper.getFileTypeByHeader(new File(file), config);

				if (pattern.equals("rf2-relationships")){
					loadRelationshipsFile(new File(file));
				}else if(pattern.equals("rf2-concepts")){
					loadConceptsFile(new File(file));
				}else{}
			}
		}

	}

	public void loadConceptsFile(File conceptsFile) throws FileNotFoundException, IOException {
		System.out.println("Starting Concepts: " + conceptsFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(conceptsFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				ConceptDescriptor loopConcept = new ConceptDescriptor();
				Long conceptId = Long.parseLong(columns[0]);
				loopConcept.setConceptId(conceptId);
				loopConcept.setActive(columns[2].equals("1"));
				loopConcept.setEffectiveTime(columns[1]);
				loopConcept.setModule(Long.parseLong(columns[3]));
				loopConcept.setDefinitionStatus(columns[4].equals("900000000000074008") ? "Primitive" : "Fully defined");
				concepts.put(conceptId, loopConcept);
                recMan.commit();
				line = br.readLine();
				count++;
				if (count % 100000 == 0) {
					System.out.print(".");
				}
			}
			System.out.println(".");
			System.out.println("Concepts loaded = " + concepts.size());
		} finally {
			br.close();
		}
	}

	public void loadDescriptionsFile(File descriptionsFile) throws FileNotFoundException, IOException {
		System.out.println("Starting Descriptions: " + descriptionsFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(descriptionsFile), "UTF8"));
		int descriptionsCount = 0;
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			boolean act;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				LightDescription loopDescription = new LightDescription();
				loopDescription.setDescriptionId(Long.parseLong(columns[0]));
				act = columns[2].equals("1");
				loopDescription.setActive(act);
				loopDescription.setEffectiveTime(columns[1]);
				Long sourceId = Long.parseLong(columns[4]);
				loopDescription.setConceptId(sourceId);
				loopDescription.setType(Long.parseLong(columns[6]));
				loopDescription.setTerm(columns[7]);
				loopDescription.setIcs(Long.parseLong(columns[8]));
				loopDescription.setModule(Long.parseLong(columns[3]));
				loopDescription.setLang(columns[5]);
				List<LightDescription> list = descriptions.get(sourceId);
				if (list == null) {
					list = new ArrayList<LightDescription>();
				}
				list.add(loopDescription);
				descriptions.put(sourceId, list);
                recMan.commit();
				line = br.readLine();
				descriptionsCount++;
				if (descriptionsCount % 100000 == 0) {
					System.out.print(".");
				}
			}
			System.out.println(".");
			System.out.println("Descriptions loaded = " + descriptions.size());
		} finally {
			br.close();
		}
	}

	public void completeDefaultTerm() throws IOException {
		boolean act;
		String type;
		String lang;
		ConceptDescriptor cdesc;
		for (Long sourceId:concepts.keySet()){
			List<LightDescription> lDescriptions = descriptions.get(sourceId);
			if (lDescriptions!=null){
				for (LightDescription desc:lDescriptions){
					
					act=desc.getActive();
					type=String.valueOf(desc.getType());
					lang=desc.getLang();
					if (act && type.equals("900000000000003001") && lang.equals("en")) {
						cdesc = concepts.get(sourceId);
						if (cdesc != null && (cdesc.getDefaultTerm() == null || cdesc.getDefaultTerm().isEmpty())) {
							cdesc.setDefaultTerm(desc.getTerm());
						}
						
						if (getDefaultTermType()!=fsnType){
							if (!cptFSN.containsKey(sourceId)){
								cptFSN.put(sourceId,desc.getTerm());
                                recMan.commit();
							}
						}
					} else if (act && type.equals(defaultTermType) && lang.equals(defaultLangCode)) {
						cdesc = concepts.get(sourceId);
						if (cdesc != null) {
							cdesc.setDefaultTerm(desc.getTerm());
						}
					}
					if (getDefaultTermType()!=fsnType && act && type.equals("900000000000003001") && lang.equals(defaultLangCode)){
						cptFSN.put(sourceId,desc.getTerm());
                        recMan.commit();
					}
				}
			}
		}
	}
	public void loadTextDefinitionFile(File textDefinitionFile) throws FileNotFoundException, IOException {
		System.out.println("Starting Text Definitions: " + textDefinitionFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(textDefinitionFile), "UTF8"));
		int descriptionsCount = 0;
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			boolean act;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				LightDescription loopDescription = new LightDescription();
				loopDescription.setDescriptionId(Long.parseLong(columns[0]));
				act = columns[2].equals("1");
				loopDescription.setActive(act);
				loopDescription.setEffectiveTime(columns[1]);
				Long sourceId = Long.parseLong(columns[4]);
				loopDescription.setConceptId(sourceId);
				loopDescription.setType(Long.parseLong(columns[6]));
				loopDescription.setTerm(columns[7]);
				loopDescription.setIcs(Long.parseLong(columns[8]));
				loopDescription.setModule(Long.parseLong(columns[3]));
				loopDescription.setLang(columns[5]);
				List<LightDescription> list = tdefMembers.get(sourceId);
				if (list == null) {
					list = new ArrayList<LightDescription>();
				}
				list.add(loopDescription);
				tdefMembers.put(sourceId, list);
                recMan.commit();

				line = br.readLine();
				descriptionsCount++;
				if (descriptionsCount % 100000 == 0) {
					System.out.print(".");
				}
			}
			System.out.println(".");
			System.out.println("Text Definitions loaded = " + tdefMembers.size());
		} finally {
			br.close();
		}
	}
	public void loadRelationshipsFile(File relationshipsFile) throws FileNotFoundException, IOException {
		System.out.println("Starting Relationships: " + relationshipsFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(relationshipsFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				LightRelationship loopRelationship = new LightRelationship();

				loopRelationship.setActive(columns[2].equals("1"));
				loopRelationship.setEffectiveTime(columns[1]);
				loopRelationship.setModule(Long.parseLong(columns[3]));
				Long targetId=Long.parseLong(columns[5]);
				loopRelationship.setTarget(targetId);
				Long type=Long.parseLong(columns[7]);
				loopRelationship.setType(type);
				loopRelationship.setModifier(Long.parseLong(columns[9]));
				loopRelationship.setGroupId(Integer.parseInt(columns[6]));
				Long sourceId = Long.parseLong(columns[4]);
				loopRelationship.setSourceId(sourceId);
				Long charType=Long.parseLong(columns[8]);
				loopRelationship.setCharType(charType);

				List<LightRelationship> relList = relationships.get(sourceId);
				if (relList == null) {
					relList = new ArrayList<LightRelationship>();
				}
				relList.add(loopRelationship);
				relationships.put(sourceId, relList);
                recMan.commit();
				if (columns[2].equals("1") 
						&& type==isaSCTId){
					if ( charType==inferred){
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
			System.out.println("Relationships loaded = " + relationships.size());
		} finally {
			br.close();
		}
	}

	public void loadSimpleRefsetFile(File simpleRefsetFile) throws FileNotFoundException, IOException {
		System.out.println("Starting Simple Refset Members: " + simpleRefsetFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(simpleRefsetFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				if (columns[2].equals("1")) {
					LightRefsetMembership loopMember = new LightRefsetMembership();
					loopMember.setType(LightRefsetMembership.RefsetMembershipType.SIMPLE_REFSET.name());
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setModule(Long.parseLong(columns[3]));

					Long sourceId = Long.parseLong(columns[5]);
					loopMember.setReferencedComponentId(sourceId);
					loopMember.setRefset(Long.parseLong(columns[4]));

					List<LightRefsetMembership> list = simpleMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightRefsetMembership>();
					}
					list.add(loopMember);
					simpleMembers.put(Long.parseLong(columns[5]), list);
                    recMan.commit();
					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("SimpleRefsetMember loaded = " + simpleMembers.size());
		} finally {
			br.close();
		}
	}

	public void loadAssociationFile(File associationsFile) throws FileNotFoundException, IOException {
		System.out.println("Starting Association Refset Members: " + associationsFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(associationsFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				if (columns[2].equals("1")) {
					LightRefsetMembership loopMember = new LightRefsetMembership();
					loopMember.setType(LightRefsetMembership.RefsetMembershipType.ASSOCIATION.name());
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setModule(Long.parseLong(columns[3]));

					Long sourceId = Long.parseLong(columns[5]);
					loopMember.setReferencedComponentId(sourceId);
					loopMember.setRefset(Long.parseLong(columns[4]));
					loopMember.setCidValue(Long.parseLong(columns[6]));

					List<LightRefsetMembership> list = assocMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightRefsetMembership>();
					}
					list.add(loopMember);
					assocMembers.put(Long.parseLong(columns[5]), list);
                    recMan.commit();
					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("AssociationMember loaded = " + assocMembers.size());
		} finally {
			br.close();
		}
	}

	public void loadAttributeFile(File attributeFile) throws FileNotFoundException, IOException {
		System.out.println("Starting Attribute Refset Members: " + attributeFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(attributeFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				if (columns[2].equals("1")) {
					LightRefsetMembership loopMember = new LightRefsetMembership();
					loopMember.setType(LightRefsetMembership.RefsetMembershipType.ATTRIBUTE_VALUE.name());
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setModule(Long.parseLong(columns[3]));

					Long sourceId = Long.parseLong(columns[5]);
					loopMember.setReferencedComponentId(sourceId);
					loopMember.setRefset(Long.parseLong(columns[4]));
					loopMember.setCidValue(Long.parseLong(columns[6]));

					List<LightRefsetMembership> list = attrMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightRefsetMembership>();
					}
					list.add(loopMember);
					attrMembers.put(Long.parseLong(columns[5]), list);
                    recMan.commit();
					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("AttributeMember loaded = " + attrMembers.size());
		} finally {
			br.close();
		}
	}
	public void loadSimpleMapRefsetFile(File simpleMapRefsetFile) throws FileNotFoundException, IOException {
		System.out.println("Starting SimpleMap Refset Members: " + simpleMapRefsetFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(simpleMapRefsetFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				if (columns[2].equals("1")) {
					LightRefsetMembership loopMember = new LightRefsetMembership();
					loopMember.setType(LightRefsetMembership.RefsetMembershipType.SIMPLEMAP.name());
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setModule(Long.parseLong(columns[3]));

					Long sourceId = Long.parseLong(columns[5]);
					loopMember.setReferencedComponentId(sourceId);
					loopMember.setRefset(Long.parseLong(columns[4]));
					loopMember.setOtherValue(columns[6]);

					List<LightRefsetMembership> list = simpleMapMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightRefsetMembership>();
					}
					list.add(loopMember);
					simpleMapMembers.put(sourceId, list);
                    recMan.commit();
					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("SimpleMap RefsetMember loaded = " + simpleMapMembers.size());
		} finally {
			br.close();
		}
	}

	public void loadLanguageRefsetFile(File languageRefsetFile) throws FileNotFoundException, IOException {
		System.out.println("Starting Language Refset Members: " + languageRefsetFile.getName());
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(languageRefsetFile), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				if (columns[2].equals("1")) {
					LightLangMembership loopMember = new LightLangMembership();
					loopMember.setUuid(UUID.fromString(columns[0]));

					loopMember.setActive(columns[2].equals("1"));
					loopMember.setEffectiveTime(columns[1]);
					loopMember.setModule(Long.parseLong(columns[3]));
					Long sourceId = Long.parseLong(columns[5]);
					loopMember.setDescriptionId(sourceId);
					loopMember.setRefset(Long.parseLong(columns[4]));
					loopMember.setAcceptability(Long.parseLong(columns[6]));
					List<LightLangMembership> list = languageMembers.get(sourceId);
					if (list == null) {
						list = new ArrayList<LightLangMembership>();
					}
					list.add(loopMember);
					languageMembers.put(sourceId, list);
                    recMan.commit();
					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
			System.out.println("LanguageMembers loaded = " + languageMembers.size());
		} finally {
			br.close();
		}
	}

	public void createConceptsJsonFile(String fileName) throws FileNotFoundException, UnsupportedEncodingException, IOException {
		System.out.println("Starting creation of " + fileName);
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

		//        int count = 0;
		for (Long cptId : concepts.keySet()) {
			//            count++;
			//if (count > 10) break;
			Concept cpt = new Concept();
			ConceptDescriptor cptdesc = concepts.get(cptId);

			cpt.setConceptId(cptId);
			cpt.setActive(cptdesc.getActive());
			cpt.setDefaultTerm(cptdesc.getDefaultTerm());
			cpt.setEffectiveTime(cptdesc.getEffectiveTime());
			cpt.setModule(cptdesc.getModule());
			cpt.setDefinitionStatus(cptdesc.getDefinitionStatus());
			cpt.setIsLeafInferred( (notLeafInferred.contains(cptId)? 0:1));
			cpt.setIsLeafStated( (notLeafStated.contains(cptId)? 0:1));
			listLD = descriptions.get(cptId);
			listD = new ArrayList<Description>();

			if (listLD != null) {
				Long descId;
				for (LightDescription ldesc : listLD) {
					Description d = new Description();
					d.setActive(ldesc.getActive());
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

							lm.setActive(llm.getActive());
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
							rm.setActive(lrm.getActive());
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
				Long descId;
				for (LightDescription ldesc : listLD) {
					Description d = new Description();
					d.setActive(ldesc.getActive());
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

							lm.setActive(llm.getActive());
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
					if (lrel.getCharType().equals(900000000000010007L)) {
						Relationship d = new Relationship();
						d.setEffectiveTime(lrel.getEffectiveTime());
						d.setActive(lrel.getActive());
						d.setModule(lrel.getModule());
						d.setGroupId(lrel.getGroupId());
						d.setModifier(MODIFIER);
						d.setSourceId(cptId);
						d.setTarget(concepts.get(lrel.getTarget()));
						d.setType(concepts.get(lrel.getType()));
						d.setCharType(concepts.get(lrel.getCharType()));
						listR.add(d);
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
					if (lrel.getCharType().equals(900000000000011006L)) {
						Relationship d = new Relationship();
						d.setEffectiveTime(lrel.getEffectiveTime());
						d.setActive(lrel.getActive());
						d.setModule(lrel.getModule());
						d.setGroupId(lrel.getGroupId());
						d.setModifier(MODIFIER);
						d.setSourceId(cptId);
						d.setTarget(concepts.get(lrel.getTarget()));
						d.setType(concepts.get(lrel.getType()));
						d.setCharType(concepts.get(lrel.getCharType()));
						listR.add(d);
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
					d.setActive(lrm.getActive());
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
					d.setActive(lrm.getActive());
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
					d.setActive(lrm.getActive());
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
					d.setActive(lrm.getActive());
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
		}
		bw.close();
		System.out.println(fileName + " Done");
	}

	public String getDefaultLangCode() {
		return defaultLangCode;
	}

	public void setDefaultLangCode(String defaultLangCode) {
		this.defaultLangCode = defaultLangCode;
	}

	private void createTClosure(String fileName,Long charType) throws IOException {

		System.out.println("Transitive Closure creation from " + fileName);
		FileOutputStream fos = new FileOutputStream(fileName);
		OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
		BufferedWriter bw = new BufferedWriter(osw);
		Gson gson = new Gson();


		//         int count = 0;
		for (Long cptId : concepts.keySet()) {		

			listA = new ArrayList<Long>();
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

	private void getAncestors(Long cptId,Long charType) {

		List<LightRelationship> listLR = new ArrayList<LightRelationship>();

		listLR = relationships.get(cptId);
		if (listLR != null) {
			for (LightRelationship lrel : listLR) {
				if (lrel.getCharType().equals(charType) &&
						lrel.getType().equals(isaSCTId) &&
						lrel.getActive()) {
					Long tgt=lrel.getTarget();
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
		//        int count = 0;
		for (long conceptId : descriptions.keySet()) {
			//            count++;
			//if (count > 10) break;
			for (LightDescription ldesc : descriptions.get(conceptId)) {
				TextIndexDescription d = new TextIndexDescription();
				d.setActive(ldesc.getActive());
				d.setTerm(ldesc.getTerm());
				d.setLength(ldesc.getTerm().length());
				d.setTypeId(ldesc.getType());
				d.setConceptId(ldesc.getConceptId());
				d.setDescriptionId(ldesc.getDescriptionId());
				d.setModule(ldesc.getModule());
				// using long lang names for Mongo 2.4.x text indexes
				d.setLang(langCodes.get(ldesc.getLang()));
				ConceptDescriptor concept = concepts.get(ldesc.getConceptId());
				d.setConceptModule(concept.getModule());
				d.setConceptActive(concept.getActive());
				if (getDefaultTermType()!=fsnType){
					String fsn= cptFSN.get(ldesc.getConceptId());
					if (fsn!=null){
						d.setFsn(fsn);
					}
				}else{
					d.setFsn(concept.getDefaultTerm());
				}
				if (d.getFsn() == null) {
					System.out.println("FSN Issue..." + d.getConceptId());
					d.setFsn(d.getTerm());
				}
				d.setSemanticTag("");
				if (d.getFsn().endsWith(")")) {
					d.setSemanticTag(d.getFsn().substring(d.getFsn().lastIndexOf("(") + 1, d.getFsn().length() - 1));
				}
				String cleanTerm = d.getTerm().replace("(", "").replace(")", "").trim().toLowerCase();
				String convertedTerm=convertTerm(cleanTerm);
				String[] tokens = convertedTerm.toLowerCase().split("\\s+");
				d.setWords(Arrays.asList(tokens));
				bw.append(gson.toJson(d).toString());
				bw.append(sep);
			}
		}

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

		String charconvtable="src/main/resources/org/ihtsdo/util/char_conversion_table.txt";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(charconvtable), "UTF8"));	
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



}
