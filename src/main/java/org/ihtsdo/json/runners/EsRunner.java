package org.ihtsdo.json.runners;

import org.ihtsdo.json.TransformerConfig;
import org.ihtsdo.json.TransformerDiskBased;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by alo on 7/31/14.
 */
public class EsRunner {

    public static void main(String[] args) throws Exception {
        TransformerConfig config = new TransformerConfig();
        config.setDatabaseName("es-edition");
        config.setDefaultTermDescriptionType(900000000000003001L);
        config.setDefaultTermLangCode("es");
        config.setDefaultTermLanguageRefset(900000000000509007L);
        config.setEditionName("Spanish Edition");
        config.setEffectiveTime("20140430");
        config.setExpirationTime("20141231");
        config.setNormalizeTextIndex(true);
        HashSet<String> baselineFolders = new HashSet<String>();
        baselineFolders.add("/Users/alo/Downloads/Releases/SnomedCT_Release_INT_20140131/RF2Release/Snapshot");
        config.setFoldersBaselineLoad(baselineFolders);
        config.setModulesToIgnoreBaselineLoad(new ArrayList<Long>());
        HashSet<String> extensionFolders = new HashSet<String>();
        extensionFolders.add("/Users/alo/Downloads/Releases/SnomedCT_Release-es_INT_20140430/RF2Release/Snapshot");
        config.setFoldersExtensionLoad(extensionFolders);
        ArrayList<Long> modulesToIgnore = new ArrayList<Long>();
        config.setModulesToIgnoreExtensionLoad(modulesToIgnore);

        config.setOutputFolder("/Users/alo/Downloads/Releases/es-json");

        TransformerDiskBased tr = new TransformerDiskBased();

        tr.convert(config);
    }
}
