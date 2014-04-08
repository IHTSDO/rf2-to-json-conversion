/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import org.ihtsdo.json.model.versioned.Concept;
import org.ihtsdo.json.model.versioned.Concept4F;
import org.ihtsdo.json.model.versioned.ConceptAncestor;
import org.ihtsdo.json.model.versioned.ConceptDescriptor;
import org.ihtsdo.json.model.versioned.Description;
import org.ihtsdo.json.model.versioned.LangMembership;
import org.ihtsdo.json.model.versioned.LightDescription;
import org.ihtsdo.json.model.versioned.LightLangMembership;
import org.ihtsdo.json.model.versioned.LightRefsetMembership;
import org.ihtsdo.json.model.versioned.LightRelationship;
import org.ihtsdo.json.model.versioned.RefsetMembership;
import org.ihtsdo.json.model.versioned.Relationship;
import org.ihtsdo.json.model.versioned.TextIndexDescription;
import org.ihtsdo.json.utils.FileSorter;

/**
 *
 * @author Alejandro Rodriguez
 */
public class Transformer4F {

    private static final String MAXEFFTIME = "99991231";
	private String MODIFIER = "Existential restriction";
    private String sep = System.getProperty("line.separator");

    private Map<Long, List<ConceptDescriptor>> concepts;
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
	private HashMap<Long, List<LightDescription>> tdefMembers;
	private HashMap<Long, List<LightRefsetMembership>> attrMembers;
	private HashMap<Long, List<LightRefsetMembership>> assocMembers;
	private ArrayList<Long> listA;
	File tmpFolder;
	File tmpOutputFileSortedFolder;

    public Transformer4F() {
        concepts = new HashMap<Long, List<ConceptDescriptor>>();
        descriptions = new HashMap<Long, List<LightDescription>>();
        relationships = new HashMap<Long, List<LightRelationship>>();
        simpleMembers = new HashMap<Long, List<LightRefsetMembership>>();
        assocMembers = new HashMap<Long, List<LightRefsetMembership>>();
        attrMembers = new HashMap<Long, List<LightRefsetMembership>>();
        tdefMembers = new HashMap<Long, List<LightDescription>>();
        simpleMapMembers = new HashMap<Long, List<LightRefsetMembership>>();
        languageMembers = new HashMap<Long, List<LightLangMembership>>();

        tmpFolder=new File("target/tmp");
        tmpOutputFileSortedFolder=new File("target/SortedFiles");
        langCodes = new HashMap<String, String>();
        langCodes.put("en", "english");
        langCodes.put("es", "spanish");
        langCodes.put("da", "danish");
        langCodes.put("sv", "swedish");
        langCodes.put("fr", "french");
        langCodes.put("nl", "dutch");
    }

    public static void main(String[] args) throws Exception {
        Transformer4F tr = new Transformer4F();
//        tr.setDefaultLangCode("da");
//        tr.setDefaultTermType(tr.synType);

//        tr.loadConceptsFile(new File("/Volumes/Macintosh HD2/tmp/destination/Snapshot/sct2_Concept_Snapshot_INT_20140131.txt"));
//        tr.loadConceptsFile(new File("/Volumes/Macintosh HD2/tmp/content-processing-1.17-release-files/SnomedCT_Release_DK1000005_20131018/RF2Release/Snapshot/Terminology/sct2_Concept_Snapshot_DK1000005_20131018.txt"));

//        tr.loadDescriptionsFile(new File("/Volumes/Macintosh HD2/tmp/destination/Snapshot/sct2_Description_Snapshot-en_INT_20140131.txt"));
//        tr.loadDescriptionsFile(new File("/Volumes/Macintosh HD2/tmp/content-processing-1.17-release-files/SnomedCT_Release_DK1000005_20131018/RF2Release/Snapshot/Terminology/sct2_Description_Snapshot_DK1000005_20131018.txt"));

//        tr.loadRelationshipsFile(new File("/Volumes/Macintosh HD2/tmp/destination/Snapshot/sct2_StatedRelationship_Snapshot_INT_20140131.txt"));
//        tr.loadRelationshipsFile(new File("/Volumes/Macintosh HD2/tmp/content-processing-1.17-release-files/SnomedCT_Release_DK1000005_20131018/RF2Release/Snapshot/Terminology/sct2_StatedRelationship_Snapshot_DK1000005_20131018.txt"));

//        tr.loadRelationshipsFile(new File("/Volumes/Macintosh HD2/tmp/destination/Snapshot/sct2_Relationship_Snapshot_INT_20140131.txt"));
//        tr.loadRelationshipsFile(new File("/Volumes/Macintosh HD2/tmp/content-processing-1.17-release-files/SnomedCT_Release_DK1000005_20131018/RF2Release/Snapshot/Terminology/sct2_Relationship_Snapshot_DK1000005_20131018.txt"));

//        tr.loadSimpleRefsetFile(new File("/Volumes/Macintosh HD2/tmp/destination/Snapshot/der2_Refset_SimpleSnapshot_INT_20140131.txt"));
//        tr.loadSimpleMapRefsetFile(new File("/Volumes/Macintosh HD2/tmp/destination/Snapshot/der2_sRefset_SimpleMapSnapshot_INT_20140131.txt"));

//        tr.loadLanguageRefsetFile(new File("/Volumes/Macintosh HD2/tmp/destination/Snapshot/der2_cRefset_LanguageSnapshot-en_INT_20140131.txt"));
//        tr.loadLanguageRefsetFile(new File("/Volumes/Macintosh HD2/tmp/content-processing-1.17-release-files/SnomedCT_Release_DK1000005_20131018/RF2Release/Snapshot/Refset/Language/der2_cRefset_LanguageSnapshot-da_DK1000005_20131018.txt"));
//        tr.loadLanguageRefsetFile(new File("/Volumes/Macintosh HD2/tmp/content-processing-1.17-release-files/SnomedCT_Release_DK1000005_20131018/RF2Release/Snapshot/Refset/Language/der2_cRefset_LanguageSnapshot-en_DK1000005_20131018.txt"));

//        tr.createConceptsJsonFile("target/concepts.json");
//        tr.createTextIndexFile("target/text-index.json");
        

      tr.setDefaultLangCode("en");
      tr.setDefaultTermType(tr.fsnType);

      tr.loadConceptsFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Terminology/sct2_Concept_Full_INT_20140131.txt"));

      tr.loadDescriptionsFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Terminology/sct2_Description_Full-en_INT_20140131.txt"));
      tr.loadTextDefinitionFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Terminology/sct2_TextDefinition_Full-en_INT_20140131.txt"));

      tr.loadRelationshipsFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Terminology/sct2_StatedRelationship_Full_INT_20140131.txt"));

      tr.loadRelationshipsFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Terminology/sct2_Relationship_Full_INT_20140131.txt"));

      tr.loadSimpleRefsetFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Refset/Content/der2_Refset_SimpleFull_INT_20140131.txt"));
      tr.loadAssociationFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Refset/Content/der2_cRefset_AssociationReferenceFull_INT_20140131.txt"));
      tr.loadAttributeFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Refset/Content/der2_cRefset_AttributeValueFull_INT_20140131.txt"));
      tr.loadSimpleMapRefsetFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Refset/Map/der2_sRefset_SimpleMapFull_INT_20140131.txt"));

      tr.loadLanguageRefsetFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Full/Refset/Language/der2_cRefset_LanguageFull-en_INT_20140131.txt"));

      tr.createConceptsJsonFile("target/concepts.json");
    }

	public void createTClosures() throws IOException {
		loadConceptsFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Snapshot/Terminology/sct2_Concept_Snapshot_INT_20140131.txt"));
		loadRelationshipsFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Snapshot/Terminology//sct2_Relationship_Snapshot_INT_20140131.txt"));
		createTClosure("target/inferred_tc.txt",inferred);
        relationships = new HashMap<Long, List<LightRelationship>>();
		loadRelationshipsFile(new File("/Volumes/Macintosh HD2/SnomedCT_Release_INT_20140131/RF2Release/Snapshot/Terminology/sct2_StatedRelationship_Snapshot_INT_20140131.txt"));
		createTClosure("target/stated_tc.txt",stated);


	}
	public File sortFile(File inputFile, File tempSortFolder,File outputFolder, int[] sortColumns){

		if (!outputFolder.exists()){
			outputFolder.mkdirs();
		}
		if (!tempSortFolder.exists()){
			tempSortFolder.mkdirs();
		}
		File outputFile=new File(outputFolder,"tmp_" + inputFile.getName());
        FileSorter fs=new FileSorter(inputFile, outputFile, tempSortFolder, sortColumns);
        fs.execute();
        fs=null;
        System.gc();
        return outputFile;
	}
	public void loadConceptsFile(File conceptsFile) throws FileNotFoundException, IOException {
        System.out.println("Starting Concepts: " + conceptsFile.getName());
        
        File output=sortFile(conceptsFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{0,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
        try {
        	List<ConceptDescriptor>conceptVer=new ArrayList<ConceptDescriptor>();
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
                loopConcept.setScTime(MAXEFFTIME);
                loopConcept.setModule(Long.parseLong(columns[3]));
                loopConcept.setDefinitionStatus(columns[4].equals("900000000000074008") ? "Primitive" : "Fully defined");
                if (concepts.containsKey(conceptId)){
                	conceptVer=concepts.get(conceptId);
                	for (ConceptDescriptor concDesc:conceptVer){
                		if (concDesc.getScTime().equals(MAXEFFTIME)){
                			concDesc.setScTime(columns[1]);
                		}
                	}
                }else{
                	conceptVer=new ArrayList<ConceptDescriptor>();
                }
                conceptVer.add(loopConcept);
                concepts.put(conceptId, conceptVer);
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
        File output=sortFile(descriptionsFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{0,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
        int descriptionsCount = 0;
        try {
            String line = br.readLine();
            line = br.readLine(); // Skip header
            boolean act;
            List<ConceptDescriptor> lcdesc=new ArrayList<ConceptDescriptor>();
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
                loopDescription.setScTime(MAXEFFTIME);
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
                }else{
                	
                	for (LightDescription item:list){
                		if (item.getDescriptionId().equals(loopDescription.getDescriptionId()) && item.getScTime().equals(MAXEFFTIME)){
                			item.setScTime(columns[1]);
                		}
                	}
                }
                list.add(loopDescription);
                descriptions.put(sourceId, list);

                if (act && columns[6].equals("900000000000003001") && columns[5].equals("en")) {
                    lcdesc = concepts.get(sourceId);
                    for (ConceptDescriptor concDesc:lcdesc){
                		if (concDesc.getEffectiveTime().compareTo(columns[1])<=0 && concDesc.getScTime().compareTo(columns[1])>0){
                			concDesc.setDefaultTerm(columns[7]);
                		}
                	}
                } else if (act && columns[6].equals(defaultTermType) && columns[5].equals(defaultLangCode)) {
                	lcdesc = concepts.get(sourceId);
                    for (ConceptDescriptor concDesc:lcdesc){
                		if (concDesc.getEffectiveTime().compareTo(columns[1])<=0 && concDesc.getScTime().compareTo(columns[1])>0){
                			concDesc.setDefaultTerm(columns[7]);
                		}
                	}
                }
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

    public void loadTextDefinitionFile(File textDefinitionFile) throws FileNotFoundException, IOException {
        System.out.println("Starting Text Definitions: " + textDefinitionFile.getName());
        File output=sortFile(textDefinitionFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{0,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
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
                loopDescription.setScTime(MAXEFFTIME);
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
                }else{
                	
                	for (LightDescription item:list){
                		if (item.getDescriptionId().equals(loopDescription.getDescriptionId()) && item.getScTime().equals(MAXEFFTIME)){
                			item.setScTime(columns[1]);
                		}
                	}
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
            System.out.println("Text Definitions loaded = " + tdefMembers.size());
        } finally {
            br.close();
        }
    }
    public void loadRelationshipsFile(File relationshipsFile) throws FileNotFoundException, IOException {
        System.out.println("Starting Relationships: " + relationshipsFile.getName());
        File output=sortFile(relationshipsFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{4,5,7,6,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
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
                loopRelationship.setScTime(MAXEFFTIME);
                loopRelationship.setTarget(Long.parseLong(columns[5]));
                loopRelationship.setType(Long.parseLong(columns[7]));
                loopRelationship.setModifier(Long.parseLong(columns[9]));
                loopRelationship.setGroupId(Integer.parseInt(columns[6]));
                Long sourceId = Long.parseLong(columns[4]);
                loopRelationship.setSourceId(sourceId);
                loopRelationship.setCharType(Long.parseLong(columns[8]));

                List<LightRelationship> relList = relationships.get(sourceId);
                if (relList == null) {
                    relList = new ArrayList<LightRelationship>();
                }else{
                	
                	for (LightRelationship item:relList){
                		if (item.getType().equals(loopRelationship.getType()) &&
                				item.getTarget().equals(loopRelationship.getTarget()) &&
                				item.getGroupId().equals(loopRelationship.getGroupId()) &&
                				item.getScTime().equals(MAXEFFTIME)){
                			item.setScTime(columns[1]);
                		}
                	}
                }
                relList.add(loopRelationship);
                relationships.put(sourceId, relList);
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
        File output=sortFile(simpleRefsetFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{5,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
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
                    loopMember.setScTime(MAXEFFTIME);
                    loopMember.setModule(Long.parseLong(columns[3]));

                    Long sourceId = Long.parseLong(columns[5]);
                    loopMember.setReferencedComponentId(sourceId);
                    loopMember.setRefset(Long.parseLong(columns[4]));

                    List<LightRefsetMembership> list = simpleMembers.get(sourceId);
                    if (list == null) {
                        list = new ArrayList<LightRefsetMembership>();
                    }else{
                    	
                    	for (LightRefsetMembership item:list){
                    		if (item.getScTime().equals(MAXEFFTIME) && item.getRefset().equals(Long.parseLong(columns[4]))){
                    			item.setScTime(columns[1]);
                    		}
                    	}
                    }
                    list.add(loopMember);
                    simpleMembers.put(Long.parseLong(columns[5]), list);
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
        File output=sortFile(associationsFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{5,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
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
                    loopMember.setScTime(MAXEFFTIME);
                    loopMember.setModule(Long.parseLong(columns[3]));

                    Long sourceId = Long.parseLong(columns[5]);
                    loopMember.setReferencedComponentId(sourceId);
                    loopMember.setRefset(Long.parseLong(columns[4]));
                    loopMember.setCidValue(Long.parseLong(columns[6]));

                    List<LightRefsetMembership> list = assocMembers.get(sourceId);
                    if (list == null) {
                        list = new ArrayList<LightRefsetMembership>();
                    }else{
                    	for (LightRefsetMembership item:list){
	                		if (item.getScTime().equals(MAXEFFTIME) && item.getRefset().equals(Long.parseLong(columns[4]))){
	                			item.setScTime(columns[1]);
	                		}
                    	}
                	}
                    list.add(loopMember);
                    assocMembers.put(Long.parseLong(columns[5]), list);
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
        File output=sortFile(attributeFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{5,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
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
                    loopMember.setScTime(MAXEFFTIME);
                    loopMember.setModule(Long.parseLong(columns[3]));

                    Long sourceId = Long.parseLong(columns[5]);
                    loopMember.setReferencedComponentId(sourceId);
                    loopMember.setRefset(Long.parseLong(columns[4]));
                    loopMember.setCidValue(Long.parseLong(columns[6]));

                    List<LightRefsetMembership> list = attrMembers.get(sourceId);
                    if (list == null) {
                        list = new ArrayList<LightRefsetMembership>();
                    }else{
                    	for (LightRefsetMembership item:list){
	                		if (item.getScTime().equals(MAXEFFTIME) && item.getRefset().equals(Long.parseLong(columns[4]))){
	                			item.setScTime(columns[1]);
	                		}
                    	}
                	}
                    list.add(loopMember);
                    attrMembers.put(Long.parseLong(columns[5]), list);
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
        File output=sortFile(simpleMapRefsetFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{5,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
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
                    loopMember.setScTime(MAXEFFTIME);
                    loopMember.setModule(Long.parseLong(columns[3]));

                    Long sourceId = Long.parseLong(columns[5]);
                    loopMember.setReferencedComponentId(sourceId);
                    loopMember.setRefset(Long.parseLong(columns[4]));
                    loopMember.setOtherValue(columns[6]);

                    List<LightRefsetMembership> list = simpleMapMembers.get(sourceId);
                    if (list == null) {
                        list = new ArrayList<LightRefsetMembership>();
                    }else{
                    	for (LightRefsetMembership item:list){
	                		if (item.getScTime().equals(MAXEFFTIME) && item.getRefset().equals(Long.parseLong(columns[4]))
	                				&& item.getOtherValue().equals(columns[6])){
	                			item.setScTime(columns[1]);
	                		}
                    	}
                	}
                    list.add(loopMember);
                    simpleMapMembers.put(sourceId, list);
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
        File output=sortFile(languageRefsetFile, tmpFolder, tmpOutputFileSortedFolder, new int[]{5,1});
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(output), "UTF8"));
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
                    loopMember.setScTime(MAXEFFTIME);
                    loopMember.setModule(Long.parseLong(columns[3]));
                    Long sourceId = Long.parseLong(columns[5]);
                    loopMember.setDescriptionId(sourceId);
                    loopMember.setRefset(Long.parseLong(columns[4]));
                    loopMember.setAcceptability(Long.parseLong(columns[6]));
                    List<LightLangMembership> list = languageMembers.get(sourceId);
                    if (list == null) {
                        list = new ArrayList<LightLangMembership>();
                    }else{
                    	for (LightLangMembership item:list){
	                		if (item.getScTime().equals(MAXEFFTIME) && item.getRefset().equals(Long.parseLong(columns[4]))){
	                			item.setScTime(columns[1]);
	                		}
                    	}
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
            Concept4F cpt = new Concept4F();
            cpt.setConceptId(cptId);
            List<ConceptDescriptor> cptdescs = concepts.get(cptId);
            cpt.setVersions(cptdescs);

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
                    d.setScTime(ldesc.getScTime());
                    d.setIcs(getCptVersion(concepts.get(ldesc.getIcs()),ldesc.getEffectiveTime()));
                    d.setTerm(ldesc.getTerm());
                    d.setLength(ldesc.getTerm().length());
                    d.setModule(ldesc.getModule());
                    d.setType(getCptVersion(concepts.get(ldesc.getType()),ldesc.getEffectiveTime()));
                    d.setLang(ldesc.getLang());

                    listLLM = languageMembers.get(descId);
                    listLM = new ArrayList<LangMembership>();

                    if (listLLM != null) {
                        for (LightLangMembership llm : listLLM) {
                            LangMembership lm = new LangMembership();

                            lm.setActive(llm.getActive());
                            lm.setDescriptionId(descId);
                            lm.setEffectiveTime(llm.getEffectiveTime());
                            lm.setScTime(llm.getScTime());
                            lm.setModule(llm.getModule());
                            lm.setAcceptability(getCptVersion(concepts.get(llm.getAcceptability()),llm.getEffectiveTime()));
                            lm.setRefset(getCptVersion(concepts.get(llm.getRefset()),llm.getEffectiveTime()));
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
                            rm.setScTime(lrm.getScTime());
                            rm.setActive(lrm.getActive());
                            rm.setModule(lrm.getModule());
                            rm.setUuid(lrm.getUuid());

                            rm.setReferencedComponentId(descId);
                            rm.setRefset(getCptVersion(concepts.get(lrm.getRefset()),lrm.getEffectiveTime()));
                            rm.setType(lrm.getType());
                            rm.setCidValue(getCptVersion(concepts.get(lrm.getCidValue()),lrm.getEffectiveTime()));

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
                    d.setScTime(ldesc.getScTime());
                    d.setIcs(getCptVersion(concepts.get(ldesc.getIcs()),ldesc.getEffectiveTime()));
                    d.setTerm(ldesc.getTerm());
                    d.setLength(ldesc.getTerm().length());
                    d.setModule(ldesc.getModule());
                    d.setType(getCptVersion(concepts.get(ldesc.getType()),ldesc.getEffectiveTime()));
                    d.setLang(ldesc.getLang());

                    listLLM = languageMembers.get(descId);
                    listLM = new ArrayList<LangMembership>();

                    if (listLLM != null) {
                        for (LightLangMembership llm : listLLM) {
                            LangMembership lm = new LangMembership();

                            lm.setActive(llm.getActive());
                            lm.setDescriptionId(descId);
                            lm.setEffectiveTime(llm.getEffectiveTime());
                            lm.setScTime(llm.getScTime());
                            lm.setModule(llm.getModule());
                            lm.setAcceptability(getCptVersion(concepts.get(llm.getAcceptability()),llm.getEffectiveTime()));
                            lm.setRefset(getCptVersion(concepts.get(llm.getRefset()),llm.getEffectiveTime()));
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
                        d.setScTime(lrel.getScTime());
                        d.setActive(lrel.getActive());
                        d.setModule(lrel.getModule());
                        d.setGroupId(lrel.getGroupId());
                        d.setModifier(MODIFIER);
                        d.setSourceId(cptId);
                        d.setTarget(getCptVersion(concepts.get(lrel.getTarget()),lrel.getEffectiveTime()));
                        d.setType(getCptVersion(concepts.get(lrel.getType()),lrel.getEffectiveTime()));
                        d.setCharType(getCptVersion(concepts.get(lrel.getCharType()),lrel.getEffectiveTime()));
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
                        d.setScTime(lrel.getScTime());
                        d.setActive(lrel.getActive());
                        d.setModule(lrel.getModule());
                        d.setGroupId(lrel.getGroupId());
                        d.setModifier(MODIFIER);
                        d.setSourceId(cptId);
                        d.setTarget(getCptVersion(concepts.get(lrel.getTarget()),lrel.getEffectiveTime()));
                        d.setType(getCptVersion(concepts.get(lrel.getType()),lrel.getEffectiveTime()));
                        d.setCharType(getCptVersion(concepts.get(lrel.getCharType()),lrel.getEffectiveTime()));
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
                    d.setScTime(lrm.getScTime());
                    d.setActive(lrm.getActive());
                    d.setModule(lrm.getModule());
                    d.setUuid(lrm.getUuid());

                    d.setReferencedComponentId(cptId);
                    d.setRefset(getCptVersion(concepts.get(lrm.getRefset()),lrm.getEffectiveTime()));
                    d.setType(lrm.getType());

                    listRM.add(d);
                }
            }

            listLRM = simpleMapMembers.get(cptId);
            if (listLRM != null) {
                for (LightRefsetMembership lrm : listLRM) {
                    RefsetMembership d = new RefsetMembership();
                    d.setEffectiveTime(lrm.getEffectiveTime());
                    d.setScTime(lrm.getScTime());
                    d.setActive(lrm.getActive());
                    d.setModule(lrm.getModule());
                    d.setUuid(lrm.getUuid());

                    d.setReferencedComponentId(cptId);
                    d.setRefset(getCptVersion(concepts.get(lrm.getRefset()),lrm.getEffectiveTime()));
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
                    d.setScTime(lrm.getScTime());
                    d.setActive(lrm.getActive());
                    d.setModule(lrm.getModule());
                    d.setUuid(lrm.getUuid());

                    d.setReferencedComponentId(cptId);
                    d.setRefset(getCptVersion(concepts.get(lrm.getRefset()),lrm.getEffectiveTime()));
                    d.setType(lrm.getType());
                    d.setCidValue(getCptVersion(concepts.get(lrm.getCidValue()),lrm.getEffectiveTime()));

                    listRM.add(d);
                }
            }
            listLRM = attrMembers.get(cptId);
            if (listLRM != null) {
                for (LightRefsetMembership lrm : listLRM) {
                    RefsetMembership d = new RefsetMembership();
                    d.setEffectiveTime(lrm.getEffectiveTime());
                    d.setScTime(lrm.getScTime());
                    d.setActive(lrm.getActive());
                    d.setModule(lrm.getModule());
                    d.setUuid(lrm.getUuid());

                    d.setReferencedComponentId(cptId);
                    d.setRefset(getCptVersion(concepts.get(lrm.getRefset()),lrm.getEffectiveTime()));
                    d.setType(lrm.getType());
                    d.setCidValue(getCptVersion(concepts.get(lrm.getCidValue()),lrm.getEffectiveTime()));

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

    private ConceptDescriptor getCptVersion(List<ConceptDescriptor> list,
			String effectiveTime) {

        for (ConceptDescriptor concDesc:list){
    		if (concDesc.getEffectiveTime().compareTo(effectiveTime)<=0 && concDesc.getScTime().compareTo(effectiveTime)>0){
    			return concDesc;
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

    private void createTClosure(String fileName,Long charType) throws IOException {
    	
    	 System.out.println("Starting creation of " + fileName);
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
                // using long lang names for Mongo 2.4.x text indexes
                d.setLang(langCodes.get(ldesc.getLang()));
                ConceptDescriptor concept = getCptVersion(concepts.get(ldesc.getConceptId()),ldesc.getEffectiveTime());
                d.setConceptActive(concept.getActive());
                d.setFsn(concept.getDefaultTerm());
                if (d.getFsn() == null) {
                    System.out.println("FSN Issue..." + d.getConceptId());
                    d.setFsn(d.getTerm());
                }
                d.setSemanticTag("");
                if (d.getFsn().contains("(")) {
                    d.setSemanticTag(d.getFsn().substring(d.getFsn().indexOf("(") + 1, d.getFsn().length() - 1));
                }
                String cleanTerm = d.getTerm().replace("(", "").replace(")", "").trim().toLowerCase();
                String[] tokens = cleanTerm.split("\\s+");
                d.setWords(Arrays.asList(tokens));
                bw.append(gson.toJson(d).toString());
                bw.append(sep);
            }
        }

        bw.close();
        System.out.println(fileName + " Done");
    }

    public String getDefaultTermType() {
        return defaultTermType;
    }

    public void setDefaultTermType(String defaultTermType) {
        this.defaultTermType = defaultTermType;
    }
    
    

}
