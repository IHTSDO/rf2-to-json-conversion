/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ihtsdo.json;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author alo
 */
public class TextUtil {
    
    public static Map<String,String> rules = new HashMap<String,String>();
    
    public static void main(String[] args) {
        System.out.println("\u00E1");
        
        String a = "\\u" + "00E1";
        
        System.out.println("áéí".replaceAll(a, "a"));
        
    }
    
    
    
    
    
}
