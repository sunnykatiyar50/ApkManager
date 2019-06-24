package com.sunnykatiyar.AppManager;

/**
 * Created by Sunny Katiyar on 16-04-2017.
 */

public class TestAppDatabase {

    private static final String DATABASE_NAME = "AppsDatabase.db" ;
    private static final String DATABASE_VERSION= "1.0";
    private static final String TABLE_NAME = "AllAppsDetails";
    private static final String COLUMN_PACKAGENAME = "PackageName";
    private static final String COLUMN_APPNAME = "AppNames";
    private static final String COLUMN_INSTALLDATE ="InstallDate";
    private static final String COLUMN_APKSIZE = "ApkSize";
    private static final String COLUMN_LASTUPDATE = "LastUpdated";
    private static final String COLUMN_SR_ID ="Serial_ID";

    private final String Create_Database ="";
    private final String Create_Table = "Create Table "+ TABLE_NAME+"("+
                                            COLUMN_SR_ID+ "Integer not null primary key,"+
                                            COLUMN_PACKAGENAME+"text not null,"+
                                             COLUMN_APPNAME+"text not null,"+
                                                COLUMN_INSTALLDATE+"long ,"+
                                                COLUMN_LASTUPDATE+"long ,"+
                                                COLUMN_APKSIZE+"double ,"+")";

}
