package com.sunnykatiyar.skmanager;

import java.util.List;

interface MyOperations {

    int NO_OPERATION = 0;
    int OPERATION_COPY = 201;
    int OPERATION_MOVE  = 204;
    int OPERATION_RENAME = 209;
    int OPERATION_DELETE = 212;
    int OPERATION_INSTALL_APKS = 215;
    int GET_FOLDER_FILES = 218;
    int GET_SUBFOLDERS_FILES = 221;
    int GET_FILTERED_FILES  = 224;
    int GET_EMPTY_FOLDERS = 227;
    int GET_FOLDER_FILES_BY_SHELL = 230;

//    public void setObjectType();
//    public void copyFiles(List<Object> srcPathList, String dest_path);
void moveFiles(List<Object> srcPathList, String dest_path);
//    public void deleteFiles(List<Object> srcPathList);
//    public void installApk(List<Object> srcPathList);
//    public void createNew(Object targetFolder);
//
//    public void renameFile(Object targetFile);
//    public void renameFromFormat(List<Object> srcPathList);


}
