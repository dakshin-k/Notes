package com.dakshin.notes;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;


public class MainActivity extends AppCompatActivity {
    //migrate all drive jobs to use the queue, and setup an alarm to execute pending jobs regularly
    private static final int PICKFILE_REQUEST_CODE = 1;
    ArrayList<MenuItem> arrayList;
    ListView listView;
    private CustomAdapter adapter;
    FloatingActionButton fab1, fab2;
    FloatingActionMenu fam;
    String filesjson;
    JSONArray currentjsonarray;
    JSONObject parentobject;
    String currentPath="files.json";
    String tag="tag"; //for logcat
    Stack<String> fileHistory;
    private static final int REQUEST_CODE_SIGN_IN = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        listView = findViewById(R.id.listview);
     //  Log.d(tag,"External storage directory is "+ Environment.getExternalStorageDirectory());
        fileHistory=new Stack<>();
        fileHistory.push("files.json");

        arrayList = getArrayList(currentPath);
        adapter = new CustomAdapter(arrayList, this);
        if(arrayList==null)
        {
            Toast.makeText(this, "Arraylist is null", Toast.LENGTH_SHORT).show();
            Log.e(tag,"Array list is null");
        }
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            MenuItem item = arrayList.get(position);
            fam.close(true);
            if(item.getType().equals("folder"))
            {
                arrayList = getArrayList(item.getPath());
                currentPath = item.getPath();
                fileHistory.push(currentPath);
                adapter.clear();
                adapter.addAll(arrayList);
            }
            else {
                //start activity to view this file
                File file=new File(getExternalFilesDir(null),item.getPath());
               /* File file=new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/Notes/"+item.getPath());
                if(!file.exists()&&!file.mkdirs()){
                    Log.e(tag,"continueAppExecution(): could not create directory "+file.getAbsolutePath());
                }*/
                MimeTypeMap myMime = MimeTypeMap.getSingleton();
                Intent newIntent = new Intent(Intent.ACTION_VIEW);
                String mimeType = myMime.getMimeTypeFromExtension(fileExt(file.getPath()).substring(1));
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                if (Build.VERSION.SDK_INT<24)
                    newIntent.setDataAndType(Uri.fromFile(file),mimeType);
                else {
                    Uri uri= FileProvider.getUriForFile(MainActivity.this,
                            getApplicationContext().getPackageName()+".provider",file);
                    newIntent.setDataAndType(uri,mimeType);
                    newIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                }

                try {
                    startActivity(newIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(MainActivity.this, "No app is installed to open this file.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        fam=findViewById(R.id.menu_yellow);
        fam.setClosedOnTouchOutside(true);
        fab1 = findViewById(R.id.fab12);
        fab1.setOnClickListener(v -> {
            AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this);
            builder.setCancelable(true);
            builder.setTitle("New Folder");
            builder.setMessage("Enter folder name: ");
            final EditText input=new EditText(MainActivity.this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("OKAY", (dialog, which) -> {
                final String folderName=input.getText().toString();
                MenuItem item=new MenuItem(folderName,"folder",getNewPath(".json"));
                addtoJSONArray(currentjsonarray,item);

                //regular arrayList.add() doesn't ssem to be working so this is a workaround
                //todo: find out why!

                arrayList=getArrayList(currentPath);
                adapter.clear();
                adapter.addAll(arrayList);

                fam.close(true);
            });
            builder.show();

        });

        fab2 = findViewById(R.id.fab22);
        fab2.setOnClickListener(v -> {
            //todo:
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            startActivityForResult(intent, PICKFILE_REQUEST_CODE);
        });
    }

    String seed="QWERTYUIOPASDFGHJKLZXCVBNM1234567890qwertyuiopasdfghjklzxcvbnm";

    private String getNewPath(String extension) {
        StringBuilder path=new StringBuilder();
        Random random=new Random();
        do {
            int index=random.nextInt(seed.length());
            path.append(seed.charAt(index));
        }while(new File(getExternalFilesDir(null),path.toString()+extension).exists());
        path.append(extension);
        return path.toString();
    }

    private void addtoJSONArray(JSONArray currentJSONArray, MenuItem item) {
        JSONObject object = new JSONObject();
        try {
            object.put("id", String.valueOf(currentJSONArray.length()));
            object.put("name", item.getName());
            object.put("path", item.getPath());
            object.put("type", item.getType());
            currentJSONArray.put(currentJSONArray.length(), object);
            updatejson(currentPath);
            //todo: the modification needs to be reflected in the original files.json, no matter what the folder tree looks like
            //update: I don't know how, but it's happening automatically!
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void updatejson(String path) {
        PrintWriter writer;
        try {
            writer = new PrintWriter(new File(getExternalFilesDir(null),path));
            writer.println(parentobject.toString());
            writer.close();
        } catch (FileNotFoundException e) {
            Log.e(tag,"updatejson(): FileNotFoundException raised");
        }

    }

    private String readJSONFile(String path) throws IOException {
        //Toast.makeText(this, "In readfunc", Toast.LENGTH_SHORT).show();
        File file=new File(getExternalFilesDir(null),path);
        StringBuffer buffer = new StringBuffer("");
        BufferedReader br=new BufferedReader(new FileReader(file.getAbsolutePath()));
        String msg="";
        while(msg!=null)
        {
            buffer.append(msg);
            msg=br.readLine();
        }
        return buffer.toString();

    }

    private ArrayList<MenuItem> getArrayList(String path) {
        ArrayList<MenuItem> arrayList = new ArrayList<>();
        File file=new File(getExternalFilesDir(null),path);
        //Log.d(tag,"file path: "+file.getAbsolutePath());
        String message = null;
        try {
            message = readJSONFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(tag,"Text stored in "+path+": "+message);
        if (message != null) {
            try {
                filesjson = message;
                JSONObject object = new JSONObject(message);
                parentobject=object;
                JSONArray array;
                array = object.getJSONArray("contents");

                currentjsonarray = array;
                return toArrayList(array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            try {
                if(!file.createNewFile())
                    Log.d(tag,"Error creating file");
            } catch (IOException e) {
                Log.e(tag,"IOException raised: could not create file "+file.getAbsolutePath());
                return null;
            }
            try {
                JSONObject json = new JSONObject();
                json.put("name", "root");
                json.put("no", 0);
                JSONArray array = new JSONArray();
                currentjsonarray = array;
                //empty for now, the plus symbol is to add here
                json.put("contents", array);
                parentobject=json;
                message = json.toString();
                filesjson = message;
                PrintWriter writer = new PrintWriter(file.getAbsolutePath());
                writer.println(message);
                writer.close();


            } catch (JSONException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return arrayList;
    }

    private ArrayList<MenuItem> toArrayList(JSONArray contents) {
        //create a menuitem for each entry in contents
        ArrayList<MenuItem> arrayList = new ArrayList<>();
        try {
            for (int i = 0; i < contents.length(); i++) //arbitrary maximum length of array
            {
                JSONObject object = contents.getJSONObject(i);
                MenuItem item = new MenuItem();
                item.setPath(object.getString("path"));
                item.setType(object.getString("type"));
                item.setName(object.getString("name"));
                arrayList.add(object.getInt("id"), item);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return arrayList;
    }

    @Override
    public void onBackPressed()
    {
        if(fileHistory.size()<=1) //exit if we're in home directory
            super.onBackPressed();
        else {
            fileHistory.pop(); //coz first entry is the current directory, and the next one is where we wanna go
            currentPath=fileHistory.peek();
            arrayList=getArrayList(currentPath);
            adapter.clear();
            adapter.addAll(arrayList);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode==PICKFILE_REQUEST_CODE) {
            //Toast.makeText(this, "Result recieved", Toast.LENGTH_SHORT).show();
            if (resultCode == RESULT_OK) {
                String uripath = data.getDataString();
                assert uripath != null;
                Uri uri = android.net.Uri.parse(uripath);
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    String type=getContentResolver().getType(uri);
                    Log.i(tag,"type of file is "+type);
                    //todo: extend this for other files as well
                    assert type != null;
                    switch (type) {
                        case "application/pdf":
                            type = ".pdf";
                            break;
                        //for images
                        case "image/jpeg":
                            type = ".jpg";
                            break;
                        case "image/png":
                            type = ".png";
                            break;
                        //for ms office formats
                        case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
                            type = ".docx";
                            break;
                        case "application/msword":
                            type = ".doc";
                            break;
                        case "application/vnd.ms-powerpoint":
                            type = ".ppt";
                            break;
                        case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                            type = ".pptx";
                            break;
                        case "text/plain":
                            type = ".txt";
                            break;
                        default:
                            Toast.makeText(getApplicationContext(), "This file type is not yet supported", Toast.LENGTH_SHORT).show();
                            Log.d(tag, "This file type is not yet supported");
                            return;
                    }
                    String path=getNewPath(type);
                    //now need to copy the data in the uri to ./path, then update the arraylist and json files
                    File file = new File(getExternalFilesDir(null),path);
                    FileOutputStream outputStream=new FileOutputStream(file);
                    assert inputStream != null;
                    byte bytes[]=new byte[inputStream.available()];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(bytes)) != -1) {
                        outputStream.write(bytes, 0, bytesRead);
                    }
                    inputStream.close();
                    outputStream.close();
                    Log.d(tag,"Contents written to "+path+" successfully.");

                    //now need to add this file to the json file and to the listview onscreen.
                    String fileName=getFileName(uri);
                    Log.d(tag,"file name is "+fileName);
                    MenuItem item=new MenuItem();
                    item.setName(fileName);
                    item.setType(type.substring(1)); //ex: if type= .pdf then the substr will be the reqd. 'pdf'
                    item.setPath(path);
                    addtoJSONArray(currentjsonarray,item);
                    arrayList=getArrayList(currentPath);
                    adapter.clear();
                    adapter.addAll(arrayList);

                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "File was not found", Toast.LENGTH_SHORT).show();
                    Log.e(tag,e.getLocalizedMessage()+"File was not found!");
                } catch (NullPointerException e) {
                    Log.e(tag,"onActivityResult: The string \"type\" is null");
                } catch (IOException e) {
                    Log.e(tag,"onActivityResult: IOException raised ");
                }

            } else Log.d(tag, "Pick file request failed");
        }

    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            //noinspection TryFinallyCanBeTryWithResources
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
    private String fileExt(String url) {
        if (url.contains("?")) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.contains("%")) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.contains("/")) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    public void closeFam(View view) {
        //when the menu is opened, touching outside should close the menu.
        fam.close(true);
    }


}
