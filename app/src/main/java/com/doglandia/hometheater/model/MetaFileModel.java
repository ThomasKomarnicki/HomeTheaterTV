package com.doglandia.hometheater.model;

import java.util.Map;

public class MetaFileModel {

    Map<String, FileMeta> fileList;



    class FileMeta{

        private long timeCreated;
        private String locationName;

    }
}
