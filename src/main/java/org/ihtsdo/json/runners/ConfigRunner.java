package org.ihtsdo.json.runners;

import org.apache.commons.configuration.XMLConfiguration;
import org.ihtsdo.json.TransformerConfig;
import org.ihtsdo.json.TransformerDiskBased;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by alo on 8/4/14.
 */
public class ConfigRunner {

    public static void main(String[] args) throws Exception {
        String config = null;
        if (args.length > 0) {
            config = args[0];
        }
        if (config == null) {
            //config = "config/sampleConfig.xml";
            System.err.println("Error: no config provided. Usage: rf2-to-json-conversion [path/to/config.xml]");
            System.exit(-1);
        }
        System.out.println("Running with config: " + config);
        XMLConfiguration xmlConfig = new XMLConfiguration(config);

        TransformerConfig runnableConfig = new TransformerConfig();

        runnableConfig.setDefaultTermLangCode(xmlConfig.getString("defaultTermLangCode"));
        runnableConfig.setDefaultTermDescriptionType(xmlConfig.getString("defaultTermDescriptionType"));
        runnableConfig.setDefaultTermLanguageRefset(xmlConfig.getString("defaultTermLanguageRefset"));
        runnableConfig.setNormalizeTextIndex(xmlConfig.getString("normalizeTextIndex").equals("true"));
        runnableConfig.setCreateCompleteConceptsFile(xmlConfig.getString("createCompleteConceptsFile").equals("true"));
        runnableConfig.setProcessInMemory(xmlConfig.getString("processInMemory").equals("true"));
        runnableConfig.setEditionName(xmlConfig.getString("editionName"));
        runnableConfig.setDatabaseName(xmlConfig.getString("databaseName"));
        runnableConfig.setEffectiveTime(xmlConfig.getString("effectiveTime"));
        runnableConfig.setExpirationTime(xmlConfig.getString("expirationTime"));
        runnableConfig.setOutputFolder(xmlConfig.getString("outputFolder"));

        Object prop = xmlConfig.getProperty("foldersBaselineLoad.folder");
        if (prop instanceof Collection) {
            for (String loopProp : (Collection<String>)prop) {
                runnableConfig.getFoldersBaselineLoad().add(loopProp);
                System.out.println(loopProp);
            }
        } else if (prop instanceof String) {
            runnableConfig.getFoldersBaselineLoad().add((String)prop);
            System.out.println(prop);
        }

        prop = xmlConfig.getProperty("modulesToIgnoreBaselineLoad.folder");
        if (prop instanceof Collection) {
            for (String loopProp : (Collection<String>)prop) {
                runnableConfig.getModulesToIgnoreBaselineLoad().add(loopProp);
                System.out.println(loopProp);
            }
        } else if (prop instanceof String) {
            runnableConfig.getModulesToIgnoreBaselineLoad().add((String)prop);
            System.out.println(prop);
        }

        prop = xmlConfig.getProperty("foldersExtensionLoad.folder");
        if (prop instanceof Collection) {
            for (String loopProp : (Collection<String>)prop) {
                runnableConfig.getFoldersExtensionLoad().add(loopProp);
                System.out.println(loopProp);
            }
        } else if (prop instanceof String) {
            runnableConfig.getFoldersExtensionLoad().add((String) prop);
            System.out.println(prop);
        }

        prop = xmlConfig.getProperty("modulesToIgnoreExtensionLoad.folder");
        if (prop instanceof Collection) {
            for (String loopProp : (Collection<String>)prop) {
                runnableConfig.getModulesToIgnoreExtensionLoad().add(loopProp);
                System.out.println(loopProp);
            }
        } else if (prop instanceof String) {
            runnableConfig.getModulesToIgnoreExtensionLoad().add((String)prop);
            System.out.println(prop);
        }

        TransformerDiskBased tr = new TransformerDiskBased();

        tr.convert(runnableConfig);

        System.out.println("Done...");

    }
}
