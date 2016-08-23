/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package ballantines.avionics.flightgear.connect;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.javalite.http.Get;
import org.javalite.http.Http;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * An implementation of PropertyService which uses the internal HTTPD service of
 * Flightgear. In order to be able to connect to Flightgear, you need to start
 * your flight simulator with the option {@code --httpd=5500} where "5500" will
 * be the port, used to configure this service.
 * 
 * @author mbuse
 */
public class HttpPropertyServiceImpl implements PropertyService {

    public HttpPropertyServiceImpl(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public HttpPropertyServiceImpl() {
        this(new ServerConfig());
    }
    
    
    @Override
    public Map<String, Object> readProperties(Map<String, Object> properties) {
        RootAndDepth rd = findRootAndDepth(properties.keySet());
        JSONObject json = readPropertyNode(rd.root, rd.depth);
        fillPropertyMap(properties, json);
        return properties;
    }

    @Override
    public Map<String, Object> readProperties(String... properties) {
        Map<String, Object> results = new HashMap<>();
        for (String p : properties) {
            results.put(p, null);
        }
        return readProperties(results);
    }

    @Override
    public <T> T readProperty(String property) {
        JSONObject json = readPropertyNode(property, 0);
        return (T) parseValue(json);
    }

    @Override
    public void writeProperties(Map<String, Object> properties) {
        Map<String, Map<String,Object>> requestParamsByParentPath =
                getRequestParamsByParentPath(properties);
        for (String path : requestParamsByParentPath.keySet()) {
            submitParams(path, requestParamsByParentPath.get(path));
        }
    }

    @Override
    public void writeProperty(String property, Object value) {
        writeProperties(Collections.singletonMap(property, value));
    }
    
    // === HELPER METHODS ===
    
    protected void submitParams(String path, Map<String, Object> params) {
        try {
            StringBuilder url = new StringBuilder();
            url.append(getWriteUrl());
            url.append(path);
            url.append("?");
            for (String key : params.keySet()) {
                url.append(key);
                url.append("=");
                url.append(URLEncoder.encode( params.get(key).toString() , "UTF-8"));
                url.append("&");
            }
            url.append("submit=set");
            submitUrl(url.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Failed to Submit Params.", ex);
        }
    }
    
    protected void submitUrl(String url) {
        Get response = Http.get(url);
        String txt = response.text();
        //System.out.println(txt);
    }
    
    protected Map<String, Map<String, Object>> getRequestParamsByParentPath(Map<String, Object> properties) {
        Map<String, Map<String, Object>> requestParamsByParentPath = new HashMap<>();
        
        for (String property : properties.keySet()) {
            
            int idx = property.lastIndexOf('/');
            String path = property.substring(0, idx);
            String key = property.substring(idx+1);
            
            Map<String, Object> values = requestParamsByParentPath.get(path);
            if (values==null) {
                values = new HashMap<>();
                requestParamsByParentPath.put(path, values);
            }
            values.put(key, properties.get(property));
            
        }
        
        return requestParamsByParentPath;
    }
    
    protected JSONObject readPropertyNode(String property, int depth) {
        if (!property.startsWith("/")) {
            property = "/" + property;
        }
        String url = getReadBaseURL() + property + "?d=" + depth;
        Get response = Http.get(url);
        String txt = response.text();
        //System.out.println("property '" + property + "' = " + txt);
        JSONObject json = new JSONObject(txt);
        return json;
    }
    
    protected String getReadBaseURL() {
        return serverConfig.fullAddress()+ "/json";
    }
    
    protected String getWriteUrl() {
        return serverConfig.fullAddress() + "/props";
    }
    
    protected void fillPropertyMap(Map<String,Object> properties, JSONObject node) {
        String path = node.getString("path");
        String type = node.getString("type");
        if (properties.containsKey(path)) {
            properties.put(path, parseValue(node));
            
        }
        if (node.has("children")) {
            JSONArray children = node.getJSONArray("children");
            for (int i=0; i<children.length(); i++) {
                JSONObject o = children.getJSONObject(i);
                fillPropertyMap(properties, o);
            }
        }
    }
    
    protected Object parseValue(JSONObject node) {
        String type = node.getString("type");
        if (node.has("value")) {
            String value = "" + node.get("value"); // BUGFIX
            //System.out.println("parseValue: '" + value + "' as " + type);
            if ("string".equals(type)) {
                return value;
            }
            else if ("double".equals(type)) {
                return Double.parseDouble(value);
            }
            else if ("int".equals(type)) {
                return Integer.parseInt(value);
            }
            else if ("bool".equals(type)) {
                return "1".equals(value);
            }
            return value;
        } else {
            return null;
        }
    }
    
    protected RootAndDepth findRootAndDepth(Set<String> properties) {
        RootAndDepth result = null;
        for (String p : properties) {
            if (result==null) {
                // this is the first property...
                result = new RootAndDepth();
                result.root = p;
                result.depth = 0;
            }
            else {
                result = findRootAndDepth(result, p);
            }
        }
        return result;
    }
    
    protected RootAndDepth findRootAndDepth(RootAndDepth probe, String property) {
        RootAndDepth other = findRootAndDepth(probe.root, property);
        return probe.merge(other);
    }
    
    protected RootAndDepth findRootAndDepth(String p1, String p2) {
        RootAndDepth result = null;
        String[] segments1 = segmentize(p1);
        String[] segments2 = segmentize(p2);
        int minLength = Math.min(segments1.length, segments2.length);
        int maxLength = Math.max(segments1.length, segments2.length);
        for (int i=0; i<minLength; i++) {
            if (!segments1[i].equals(segments2[i])) {
                // first difference detected.
                result = new RootAndDepth();
                result.root = desegmentize(segments1, i);
                result.depth = maxLength - i;
                break;
            }
        }
        if (result==null) {
            result = new RootAndDepth();
            result.root = desegmentize(segments1, minLength);
            result.depth = maxLength - minLength;
        }
        return result;
    }
    
    protected String[] segmentize(String path) {
        return path.split("/");
    }
    protected String desegmentize(String[] segments, int length) {
        int l = Math.min(length, segments.length);
        if (l>0) {
            StringBuilder b = new StringBuilder(segments[0]);
            for (int i=1; i<l; i++) {
               b.append("/").append(segments[i]);
            }
            return b.toString();
        }
        else {
            return "";
        }
    }
    
    protected static class RootAndDepth {
        String root = "";
        int depth = 0;
        
        public RootAndDepth merge(RootAndDepth other) {
            RootAndDepth result = new RootAndDepth();
            result.root = (root.length() > other.root.length()) 
                    ? other.root
                    : root;
            result.depth = Math.max(this.depth, other.depth);
            return result;
        }
    }
    
    // === ACCESSORS ===

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    
    // === VARIABLES ===
    
    private ServerConfig serverConfig;
}
