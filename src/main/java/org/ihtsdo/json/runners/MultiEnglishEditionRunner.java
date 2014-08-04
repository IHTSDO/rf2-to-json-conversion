package org.ihtsdo.json.runners;

import org.ihtsdo.json.TransformerConfig;
import org.ihtsdo.json.TransformerDiskBased;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by alo on 7/31/14.
 */
public class MultiEnglishEditionRunner {

    public static void main(String[] args) throws Exception {
        TransformerConfig config = new TransformerConfig();
        config.setDatabaseName("multi-edition");
        config.setDefaultTermDescriptionType(900000000000003001L);
        config.setDefaultTermLangCode("en");
        config.setDefaultTermLanguageRefset(900000000000509007L);
        config.setEditionName("Multi English Edition (Int, AU, US, UK)");
        config.setEffectiveTime("20140701");
        config.setExpirationTime("20150201");
        config.setNormalizeTextIndex(true);

        HashSet<String> baselineFolders = new HashSet<String>();
        baselineFolders.add("/Volumes/Macintosh HD2/Downloads/uk_sct2cl_17/SnomedCT_Release_INT_20140131/RF2Release/Snapshot");
        config.setFoldersBaselineLoad(baselineFolders);
        config.setModulesToIgnoreBaselineLoad(new ArrayList<Long>());

        HashSet<String> extensionFolders = new HashSet<String>();
        extensionFolders.add("/Volumes/Macintosh HD2/Downloads/uk_sct2cl_17/SnomedCT2_GB1000000_20140401/RF2Release/Snapshot");
        extensionFolders.add("/Users/termmed/Downloads/SnomedCT_Release_US1000124_20140301/RF2Release/Snapshot");
        extensionFolders.add("/Users/termmed/Downloads/SnomedCT_Release_AU1000036_20140531/RF2 Release/Snapshot");
        config.setFoldersExtensionLoad(extensionFolders);
        ArrayList<Long> modulesToIgnore = new ArrayList<Long>();
        modulesToIgnore.add(900000000000207008L);
        modulesToIgnore.add(900000000000012004L);
        config.setModulesToIgnoreExtensionLoad(modulesToIgnore);

        TransformerDiskBased tr = new TransformerDiskBased();

        tr.convert(config);
    }
}
