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
        config.setDefaultTermDescriptionType(900000000000003001L);
        config.setDefaultTermLangCode("en");
        config.setDefaultTermLanguageRefset(900000000000509007L);
        config.setEditionName("GMDN Edition");
        config.setEffectiveTime("20140725");
        config.setExpirationTime("20150201");
        config.setNormalizeTextIndex(true);
        HashSet<String> baselineFolders = new HashSet<String>();
        baselineFolders.add("/Volumes/Macintosh HD2/Downloads/uk_sct2cl_17/SnomedCT_Release_INT_20140131/RF2Release/Snapshot");
        config.setFoldersBaselineLoad(baselineFolders);
        config.setModulesToIgnoreBaselineLoad(new ArrayList<Long>());
        HashSet<String> extensionFolders = new HashSet<String>();
        extensionFolders.add("/Volumes/Macintosh HD2/Multi-english-data/gmdn-rf2");
        extensionFolders.add("/Volumes/Macintosh HD2/Multi-english-data/RF2TechnologyPreview/Snapshot");config.setFoldersBaselineLoad(extensionFolders);
        config.setFoldersExtensionLoad(extensionFolders);
        ArrayList<Long> modulesToIgnore = new ArrayList<Long>();
        config.setModulesToIgnoreExtensionLoad(modulesToIgnore);

        TransformerDiskBased tr = new TransformerDiskBased();

        tr.convert(config);
    }
}
