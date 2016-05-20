package com.doglandia.hometheater.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class ResourcesResponse {

    private List<ResourceGroup> resourceGroups;

    public List<ResourceGroup> getResourceGroups() {
        return resourceGroups;
    }

    public void expandResourceGroups(){
        List<ResourceGroup> expanded = new ArrayList<>();

        for(ResourceGroup resourceGroup : resourceGroups){
            Map<String, ResourceGroup> map = new HashMap<>();
            for(Resource resource : resourceGroup.getResourceList()){
                String key = resource.getLocation().substring(0,resource.getLocation().length() - resource.getName().length());
                ResourceGroup group;
                if(map.containsKey(key)){
                    group = map.get(key);
                }else{
                    String[] nameSections = key.split("/");
                    group = new ResourceGroup(nameSections[nameSections.length-1]);
                    map.put(key, group);
                }
                group.getResourceList().add(resource);
            }

            for(String key : map.keySet()){
                expanded.add(map.get(key));
            }
        }

        this.resourceGroups = expanded;
        Collections.sort(this.resourceGroups, new Comparator<ResourceGroup>() {
            @Override
            public int compare(ResourceGroup lhs, ResourceGroup rhs) {
                return lhs.getResourceList().get(0).getLocation().compareTo(rhs.getResourceList().get(0).getLocation());
            }
        });

    }
}
