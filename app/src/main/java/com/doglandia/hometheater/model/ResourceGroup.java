package com.doglandia.hometheater.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tdk10 on 2/21/2016.
 */
public class ResourceGroup implements Parcelable{

//    private String name;
    private String groupName;

    private List<Resource> resourceList;

    private String location;

    public ResourceGroup(String groupName){
        this.groupName = groupName;
        resourceList = new ArrayList<>();
    }

    protected ResourceGroup(Parcel in) {
//        name = in.readString();
        groupName = in.readString();
        resourceList = in.createTypedArrayList(Resource.CREATOR);
    }

    public static final Creator<ResourceGroup> CREATOR = new Creator<ResourceGroup>() {
        @Override
        public ResourceGroup createFromParcel(Parcel in) {
            return new ResourceGroup(in);
        }

        @Override
        public ResourceGroup[] newArray(int size) {
            return new ResourceGroup[size];
        }
    };

    public String getName() {
        return groupName;
    }

    public List<Resource> getResourceList() {
        return resourceList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeString(name);
        dest.writeString(groupName);
        dest.writeTypedList(resourceList);
    }
}
