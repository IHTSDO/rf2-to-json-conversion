package org.ihtsdo.json.utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.ihtsdo.json.model.LightRelationship;

public class TClosure {
	
//	HashMap<Long,HashSet<Long>>parentHier;
	HashMap<Long,HashSet<Long>>childrenHier;
//	private long ISARELATIONSHIPTYPEID=116680003l;
	private String ISA_SCTID="116680003";
	private String ROOT_CONCEPT = "138875005";
	String rf2Rels;
	private HashSet<Long> hControl;
	public TClosure(String rf2Rels) throws FileNotFoundException, IOException{
//		parentHier=new HashMap<Long,HashSet<Long>>();
		childrenHier=new HashMap<Long,HashSet<Long>>();
		this.rf2Rels=rf2Rels;
		loadIsas();
	}
	public TClosure(Map<String, List<LightRelationship>> relationships,String charType) throws FileNotFoundException, IOException {

//		parentHier=new HashMap<Long,HashSet<Long>>();
		childrenHier=new HashMap<Long,HashSet<Long>>();
		loadIsasFromMap(relationships, charType);
	}
	private void loadIsasFromMap(Map<String, List<LightRelationship>> relationships,String charType) {

		List<LightRelationship> listLR = new ArrayList<LightRelationship>();
		
		for (String cptId:relationships.keySet()){
			listLR = relationships.get(cptId);
			if (listLR != null) {
				for (LightRelationship lrel : listLR) {
					if (lrel.getCharType().equals(charType) &&
							lrel.getType().equals(ISA_SCTID) &&
							lrel.isActive()) {
	
						addRel(Long.parseLong(lrel.getTarget()),Long.parseLong(cptId));
					}
				}
			}
		}
	}
		
	private void loadIsas() throws IOException, FileNotFoundException {
		System.out.println("Starting Isas Relationships from: " + rf2Rels);
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(rf2Rels), "UTF8"));
		try {
			String line = br.readLine();
			line = br.readLine(); // Skip header
			int count = 0;
			while (line != null) {
				if (line.isEmpty()) {
					continue;
				}
				String[] columns = line.split("\\t");
				if (columns[7].equals(ISA_SCTID) 
						&& columns[2].equals("1") 
						&& !columns[4].equals(ROOT_CONCEPT)){
					addRel(Long.parseLong(columns[5]),Long.parseLong(columns[4]));
					
					count++;
					if (count % 100000 == 0) {
						System.out.print(".");
					}
				}
				line = br.readLine();
			}
			System.out.println(".");
//			System.out.println("Parent isas Relationships loaded = " + parentHier.size());
			System.out.println("Children isas Relationships loaded = " + childrenHier.size());
		} finally {
			br.close();
		}		
	}
	public void addRel(Long parent, Long child){
		if (parent==child){
			System.out.println("same child and parent: " + child);
			return;
		}
//		HashSet<Long> parentList=parentHier.get(child);
//		if (parentList==null){
//			parentList=new HashSet<Long>();
//		}
//		parentList.add(parent);
//		parentHier.put(child, parentList);
		
		HashSet<Long> childrenList=childrenHier.get(parent);
		if (childrenList==null){
			childrenList=new HashSet<Long>();
		}
		childrenList.add(child);
		childrenHier.put(parent, childrenList);
	}
	
//	public boolean isAncestorOf(Long ancestor,Long descendant){
//		
//		HashSet<Long>parent=parentHier.get(descendant);
//		if (parent==null){
//			return false;
//		}
//		if (parent.contains(ancestor)){
//			return true;
//		}
//		for(Long par:parent){
//			if (isAncestorOf(ancestor,par)){
//				return true;
//			}
//		}
//		return false;
//	}
	
//	public HashSet<Long> getParent(Long conceptId) {
//		return parentHier.get(conceptId);
//	}

	public HashSet<Long> getChildren(Long conceptId) {
		return childrenHier.get(conceptId);
	}
	public int getDescendantsCount(Long conceptId){

		hControl = new HashSet<Long>();
		int ret=getDistinctChildrenCount(conceptId);
		hControl=null;
		return ret;
	}
	private int getDistinctChildrenCount(Long conceptId){
		HashSet<Long> children=childrenHier.get(conceptId);
		if (children==null){
			return 0;
		}
		int ret=0;
		for(Long child:children){
			if (!hControl.contains(child)){
				hControl.add(child);
				ret++;
			
				ret+=getDistinctChildrenCount(child);
			}
		}		
		return ret;
	}
}
