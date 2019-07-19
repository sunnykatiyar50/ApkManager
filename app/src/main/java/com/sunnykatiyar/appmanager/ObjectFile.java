package com.sunnykatiyar.appmanager;

import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class ObjectFile {

    public File file;
    public String path="";
    public String name="";
    public String parent="";
    public String perm="";
    public boolean isDirecrtory;
    public String mod_time="";
//    public boolean isFileSystem;
//    public String octal_perm="";
//    public String mount_point="";
//    public String access_time="";
//    public String change_time="";
//    public String birth_time="";
//    public String group_id="";
//    public String group_name="";
//    public String user_id="";
    public String user_name="";
    public String size="";
    public long long_size;
    public String hard_links="";
    public String inode="";
    public String file_type="";
    public int drawableIcon;
    public boolean isSelected = false;
    final String TAG = "OBJECT FILE : ";

//    String[] lslongHeaders = {"PERM","CHILD_COUNT","OWNER","GROUP","SIZE","TIME","NAME"};

    public ObjectFile(File f){

        if(f.canRead()){
            this.file = f;
            this.perm = getPerm(f);
            this.mod_time = convertTimeToFormat(f.lastModified());
            if(f.isDirectory()){
                this.drawableIcon = R.drawable.ic_folder_white_24dp;
                this.file_type = "Folder";
                if(null == f.listFiles()){
                    size = "";
                }else{
                    this.size = f.list().length+ " files";
                }
            }else{
                this.file_type = file.getName().substring(file.getName().lastIndexOf('.')+1);
                this.drawableIcon = R.drawable.ic_insert_drive_file_white_24dp;
                this.size = convertSizeToFormat(f.length());
            }
        }
        Log.i(TAG,"NEW FILE CREATED BY FILE: "+file.getAbsolutePath());

    }

    public ObjectFile(String path){
        String command;
        Shell.Result result;
        String[] stats;

        path = Paths.get(path).toString();

        command = "stat -c '%A~%U~%h~%.19y~%n~%s' \""+path+"\"";
     //   Log.i(TAG,"STAT COMMAND : "+command);

        try{
            result = Shell.su(command).exec();
         //   Log.i(TAG,"STAT COMMAND OUTPUT : "+result.getOut().get(0));
            stats = result.getOut().get(0).split("~");
            //Log.i(TAG,"STAT ARRAY Size : "+stats.length);
            this.perm = stats[0];
            this.user_name = stats[1];
            this.hard_links = stats[2];
            this.mod_time = stats[3];
            this.path = path;
            this.name = Paths.get(this.path).getFileName().toString();
            this.parent = Paths.get(this.path).getParent().toString();

//          Log.i(TAG,"NEW FILE Name at CREATED TIME: "+name);
            this.size = convertSizeToFormat(Long.parseLong(stats[5]));
            this.long_size = Long.parseLong(stats[5]);

            if(perm.startsWith("d")){
                isDirecrtory = true;
                this.size = hard_links;
                file_type = "Folder";
                this.drawableIcon = R.drawable.ic_folder_white_24dp;
            }else if(perm.startsWith("-")){
                 file_type = name.substring(name.lastIndexOf('.')+1);
                 this.drawableIcon = R.drawable.ic_insert_drive_file_white_24dp;
            }else if(perm.startsWith("l")){
                file_type = "symlink";
                this.drawableIcon = R.drawable.ic_link_white_24dp;
            }else if(perm.startsWith("c")){
                this.drawableIcon = R.drawable.ic_dns_white_24dp;
                file_type = "character device";
            }else if(perm.startsWith("b")){
                this.drawableIcon = R.drawable.ic_sd_storage_white_24dp;
                file_type = "block device";
            }else if(perm.startsWith("n")){
                this.drawableIcon = R.drawable.ic_cloud_upload_white_24dp;
                file_type = "network file";
            }else if(perm.startsWith("p")){
                this.drawableIcon = R.drawable.ic_insert_drive_file_white_24dp;
                file_type = "socket";
            }else if(perm.startsWith("s")){
                this.drawableIcon = R.drawable.ic_settings_input_composite_white_24dp;
                file_type = "FIFO";
            }

        }catch(Exception ex){
            Log.e(TAG,"STAT COMMAND : "+command);
            Log.e(TAG,"STAT COMMAND OUTPUT FAILED : ");
        }
      //  Log.i(TAG,"NEW FILE CREATED BY STAT: "+name);
    }

    public ObjectFile(String[] ls_row){

    }

    public void setAllProperties(){

        String command;
        Shell.Result result;
        String[] stats;

        command = "stat -c '%a~%A~%F~%g~%G~%h~%m~%n~%s~%u~%U~%w~%.19x~%.19y~%.19z' "+"\""+path+"\"";
        Log.i(TAG,"STAT COMMAND : "+command);

        try{
            result = Shell.su(command).exec();
            Log.i(TAG,"STAT COMMAND OUTPUT : "+result.getOut().get(0));
            stats = result.getOut().get(0).split("~");
            Log.i(TAG,"STAT ARRAY Size : "+stats.length);
//            this.octal_perm = stats[0];
//            this.perm = stats[1];
//            this.file_type = stats[2];
//            this.group_id = stats[3];
//            this.group_name= stats[4];
//            this.hard_links = stats[5];
//            this.mount_point = stats[6];
//            this.name = stats[7];
//            this.size = stats[8];
//            this.user_id = stats[9];
//            this.user_name = stats[10];
//            this.birth_time = stats[11];
//            this.access_time = stats[12];
//            this.mod_time = stats[13];
//            this.change_time = stats[14];
        }catch(Exception ex){
            Log.i(TAG,"STAT COMMAND OUTPUT FAILED : ");
        }

        Log.i(TAG,"ALL PRPERTIES SET : "+name);
    }

    public String getPerm(File f){
        String perm = "";
        if(f.canRead()){
            perm = perm+"r";
        }
        if(f.canWrite()){
            perm = perm+"w";
        }
        if(f.canExecute()){
            perm = perm+"x";
        }
        return  perm;
    }

    public String convertSizeToFormat(long length) {
        final long MB = 1024 * 1024;
        final long KB = 1024;
        final long GB = 1024 * 1024 * 1024;
        final DecimalFormat format = new DecimalFormat("#.###");

        if (length > GB) {
            return format.format(length / GB) + " MB";
        }
        if (length > MB) {
            return format.format(length / MB) + " MB";
        }
        if (length > KB) {
            return format.format(length / KB) + " KB";
        }

        return format.format(length) + "Bytes";
    }

    public String convertTimeToFormat(long time) {
        DateFormat dateFormat = new SimpleDateFormat("hh:mm a dd~MM~yy");
        String strDate = dateFormat.format(time);
        return strDate;
    }
}
