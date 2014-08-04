package org.ihtsdo.json;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by alo on 7/31/14.
 */
public class TransformerConfig {

    private String defaultTermLangCode;
    private Long defaultTermDescriptionType;
    private Long defaultTermLanguageRefset;
    private boolean normalizeTextIndex;
    private String editionName;
    private String databaseName;
    private String effectiveTime;
    private String expirationTime;
    private String outputFolder;

    private HashSet<String> foldersBaselineLoad;
    private ArrayList<Long> modulesToIgnoreBaselineLoad;

    private HashSet<String> foldersExtensionLoad;
    private ArrayList<Long> modulesToIgnoreExtensionLoad;

    public TransformerConfig() {
        foldersBaselineLoad = new HashSet<String>();
        modulesToIgnoreBaselineLoad = new ArrayList<Long>();
        foldersExtensionLoad = new HashSet<String>();
        modulesToIgnoreExtensionLoad = new ArrayList<Long>();
    }

    public String getDefaultTermLangCode() {
        return defaultTermLangCode;
    }

    public void setDefaultTermLangCode(String defaultTermLangCode) {
        this.defaultTermLangCode = defaultTermLangCode;
    }

    public Long getDefaultTermDescriptionType() {
        return defaultTermDescriptionType;
    }

    public void setDefaultTermDescriptionType(Long defaultTermDescriptionType) {
        this.defaultTermDescriptionType = defaultTermDescriptionType;
    }

    public Long getDefaultTermLanguageRefset() {
        return defaultTermLanguageRefset;
    }

    public void setDefaultTermLanguageRefset(Long defaultTermLanguageRefset) {
        this.defaultTermLanguageRefset = defaultTermLanguageRefset;
    }

    public boolean isNormalizeTextIndex() {
        return normalizeTextIndex;
    }

    public void setNormalizeTextIndex(boolean normalizeTextIndex) {
        this.normalizeTextIndex = normalizeTextIndex;
    }

    public String getEditionName() {
        return editionName;
    }

    public void setEditionName(String editionName) {
        this.editionName = editionName;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getEffectiveTime() {
        return effectiveTime;
    }

    public void setEffectiveTime(String effectiveTime) {
        this.effectiveTime = effectiveTime;
    }

    public String getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(String expirationTime) {
        this.expirationTime = expirationTime;
    }

    public HashSet<String> getFoldersBaselineLoad() {
        return foldersBaselineLoad;
    }

    public void setFoldersBaselineLoad(HashSet<String> foldersBaselineLoad) {
        this.foldersBaselineLoad = foldersBaselineLoad;
    }

    public ArrayList<Long> getModulesToIgnoreBaselineLoad() {
        return modulesToIgnoreBaselineLoad;
    }

    public void setModulesToIgnoreBaselineLoad(ArrayList<Long> modulesToIgnoreBaselineLoad) {
        this.modulesToIgnoreBaselineLoad = modulesToIgnoreBaselineLoad;
    }

    public HashSet<String> getFoldersExtensionLoad() {
        return foldersExtensionLoad;
    }

    public void setFoldersExtensionLoad(HashSet<String> foldersExtensionLoad) {
        this.foldersExtensionLoad = foldersExtensionLoad;
    }

    public ArrayList<Long> getModulesToIgnoreExtensionLoad() {
        return modulesToIgnoreExtensionLoad;
    }

    public void setModulesToIgnoreExtensionLoad(ArrayList<Long> modulesToIgnoreExtensionLoad) {
        this.modulesToIgnoreExtensionLoad = modulesToIgnoreExtensionLoad;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(String outputFolder) {
        this.outputFolder = outputFolder;
    }
}
