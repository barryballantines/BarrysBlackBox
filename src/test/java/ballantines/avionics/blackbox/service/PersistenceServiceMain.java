package ballantines.avionics.blackbox.service;

import org.json.JSONArray;

/**
 *
 * @author mbuse
 */
public class PersistenceServiceMain {
    
    public static void main(String... args) {
        PreferencesPersistenceServiceImpl service = new PreferencesPersistenceServiceImpl();
        
        JSONArray export = service.exportPreferences();
        System.out.println(export.toString(2));
        
        System.out.println("=== DELETE PREFERENCES ===");
        
        service.deletePreferences();
        JSONArray test = service.exportPreferences();
        System.out.println(export.toString(2));
        
        System.out.println("=== REIMPORT PREFERENCES ===");
        
        service.importPreferences(export);
        test = service.exportPreferences();
        System.out.println(export.toString(2));
        
        System.out.println(test.toString(2).equals(export.toString(2)) ? "SUCCESS" : "FAILURE");
        
    }
}
