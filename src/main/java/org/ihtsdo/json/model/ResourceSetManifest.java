package org.ihtsdo.json.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by alo on 8/4/14.
 */
public class ResourceSetManifest implements Serializable {

    private String resourceSetName;
    private String effectiveTime;
    private String databaseName;
    private String collectionName;
    private String expirationDate;
    private List<ConceptDescriptor> modules;
    private List<ConceptDescriptor> languageRefsets;
    private List<RefsetDescriptor> refsets;
    private Map<String, String> languageRefsetsAbbrev;
    private String defaultTermLangCode;
    private String defaultTermType;
    private String defaultTermLangRefset;
    private boolean textIndexNormalized;

    public ResourceSetManifest() {
        modules = new ArrayList<ConceptDescriptor>();
        languageRefsets = new ArrayList<ConceptDescriptor>();
        refsets = new ArrayList<RefsetDescriptor>();
        languageRefsetsAbbrev = new HashMap<String, String>();
    }

    public String getResourceSetName() {
        return resourceSetName;
    }

    public void setResourceSetName(String resourceSetName) {
        this.resourceSetName = resourceSetName;
    }

    public String getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(String effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public List<ConceptDescriptor> getModules() {
        return modules;
    }

    public void setModules(List<ConceptDescriptor> modules) {
        this.modules = modules;
    }

    public List<ConceptDescriptor> getLanguageRefsets() {
        return languageRefsets;
    }

    public void setLanguageRefsets(List<ConceptDescriptor> languageRefsets) {
        this.languageRefsets = languageRefsets;
    }

    public List<RefsetDescriptor> getRefsets() {
        return refsets;
    }

    public void setRefsets(List<RefsetDescriptor> refsets) {
        this.refsets = refsets;
    }

    public Map<String, String> getLanguageRefsetsAbbrev() {
        return languageRefsetsAbbrev;
    }

    public void setLanguageRefsetsAbbrev(Map<String, String> languageRefsetsAbbrev) {
        this.languageRefsetsAbbrev = languageRefsetsAbbrev;
    }

    public String getDefaultTermLangCode() {
        return defaultTermLangCode;
    }

    public void setDefaultTermLangCode(String defaultTermLangCode) {
        this.defaultTermLangCode = defaultTermLangCode;
    }

    public String getDefaultTermType() {
        return defaultTermType;
    }

    public void setDefaultTermType(String defaultTermType) {
        this.defaultTermType = defaultTermType;
    }

    public String getDefaultTermLangRefset() {
        return defaultTermLangRefset;
    }

    public void setDefaultTermLangRefset(String defaultTermLangRefset) {
        this.defaultTermLangRefset = defaultTermLangRefset;
    }

    public boolean isTextIndexNormalized() {
        return textIndexNormalized;
    }

    public void setTextIndexNormalized(boolean textIndexNormalized) {
        this.textIndexNormalized = textIndexNormalized;
    }
}
