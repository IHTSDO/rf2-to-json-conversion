package org.ihtsdo.json.runners;

import org.ihtsdo.json.TransformerConfig;
import org.ihtsdo.json.TransformerDiskBased;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by alo on 7/31/14.
 */
public class GMDNRunner {

    public static void main(String[] args) throws Exception {
        TransformerConfig config = new TransformerConfig();
        config.setDatabaseName("gmdn-edition");
        config.setDefaultTermDescriptionType("900000000000003001");
        config.setDefaultTermLangCode("en");
        config.setDefaultTermLanguageRefset("900000000000509007");
        config.setEditionName("GMDN Edition");
        config.setEffectiveTime("20140725");
        config.setExpirationTime("20150201");
        config.setNormalizeTextIndex(true);
        HashSet<String> baselineFolders = new HashSet<String>();
        baselineFolders.add("/Users/alo/Downloads/Releases/SnomedCT_Release_INT_20140131/RF2Release/Snapshot");
        config.setFoldersBaselineLoad(baselineFolders);
        config.setModulesToIgnoreBaselineLoad(new ArrayList<String>());
        HashSet<String> extensionFolders = new HashSet<String>();
        extensionFolders.add("/Users/alo/NetBeansProjects/gmdn-analyzer/output/rf2");
        extensionFolders.add("/Users/alo/NetBeansProjects/gmdn-analyzer/output/SnomedCT_MedicalDevicesTechnologyPreview_INT_20140131/RF2TechnologyPreview/Snapshot");
        config.setFoldersExtensionLoad(extensionFolders);
        ArrayList<String> modulesToIgnore = new ArrayList<String>();
        config.setModulesToIgnoreExtensionLoad(modulesToIgnore);

        config.setOutputFolder("/Users/alo/Downloads/Releases/gmdn-json");

        TransformerDiskBased tr = new TransformerDiskBased();

        tr.convert(config);
    }
}
