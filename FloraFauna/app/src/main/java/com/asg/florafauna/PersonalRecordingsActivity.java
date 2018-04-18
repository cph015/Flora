package com.asg.florafauna;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import android.widget.AdapterView.OnItemClickListener;

public class PersonalRecordingsActivity extends AppCompatActivity {

    private Bitmap currentImage;
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE = 0;

    private Intent shareddata;
    private String dirName;// = Environment.getExternalStorageDirectory().toString() + "/FloraFauna/Recordings/";
    private File recordings;// = new File(dirName);
    private String[] themeArray = new String[1];

    // List Files
    GridView imagegrid;
    ArrayList<String> f = new ArrayList<String>();// list of file paths
    File[] listFile;

    //fullscreen
    private ArrayList<String> FilePathStrings = new ArrayList<String>();
    private ArrayList<String> FileNameStrings = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //setTheme(R.style.AppTheme);
        try {
            //opens the file to read its contents
            FileInputStream fis = this.openFileInput("theme");
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);

            themeArray[0] = reader.readLine(); //adds the line to the temp array
            reader.close();
            isr.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (themeArray[0].equals("Green")) {
            setTheme(R.style.AppTheme);
        } else if (themeArray[0].equals("Blue")) {
            setTheme(R.style.AppThemeBlue);
        } else if (themeArray[0].equals("Mono")) {
            setTheme(R.style.AppThemeMono);
        } else if (themeArray[0].equals("Cherry")) {
            setTheme(R.style.AppThemeCherry);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_recordings);

        FloraFaunaActionBar.createActionBar(getSupportActionBar(), R.layout.ab_recordings);

        //testing to traverse directories-------------------------------------------------------
        shareddata = getIntent();
        //-------------------------------------
        if (shareddata.getStringExtra("RDIR") == null) {
            dirName = Environment.getExternalStorageDirectory().toString() + "/FloraFauna/Recordings/";
        }
        else{
            dirName = shareddata.getStringExtra("RDIR");
        }

        //--------------------------------------------------------------------------------------

        FloatingActionButton openGallery = (FloatingActionButton) findViewById(R.id.floatingUpload);

        openGallery.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, 1);
            }
        });

        // open the default camera app to take a picture
        ImageButton openCamera = (ImageButton) findViewById(R.id.floatingCameraButton);
        openCamera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, 1);
                }
            }
        });


        //Create dir for recordings
        //request for permission to write to storage
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE);
        }

        recordings = new File(dirName);

        //checks if the recordings dir exists
        if (!recordings.exists()) {
            //if directory creation fails, tell the user
            if (!recordings.mkdirs()) {
                Log.d("error", "failed to make dir");
                Toast.makeText(this, "Failed to create directory", Toast.LENGTH_LONG).show();
            }
        }
        //if the directory exists, make a log
        else {
            Log.d("error", "dir. already exists");
        }

        imagegrid = (GridView) findViewById(R.id.FileList);

        //Check if write storage has permission
        PackageManager pm = this.getPackageManager();
        int hasPerm = pm.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                this.getPackageName());
        if (hasPerm == PackageManager.PERMISSION_GRANTED) {
            //list files
            GetFiles();
            //imagegrid = (GridView) findViewById(R.id.FileList); //gridview on recordings page
            //imageAdapter = new ImageAdapter();
            imagegrid.setAdapter(new ImageAdapter(this, FilePathStrings, FileNameStrings));
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.recordings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_home:
                Intent search_intent = new Intent(PersonalRecordingsActivity.this, SearchActivity.class);
                startActivity(search_intent);
                return true;
            case R.id.action_settings:
                Intent settings_intent = new Intent(PersonalRecordingsActivity.this, SettingsActivity.class);
                startActivity(settings_intent);
                return true;
            case R.id.action_help:
                Intent help_intent = new Intent(PersonalRecordingsActivity.this, HelpActivity.class);
                startActivity(help_intent);
                return true;
            case R.id.action_map:
                Intent intent = new Intent(PersonalRecordingsActivity.this, MapActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Uri photoUri = data.getData();
            if (photoUri != null) {
                //code to mess with images will be here
                try {
                    currentImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
                    //selectedImage.setImageBitmap(currentImage); //set the image view to the current image
                    FileOutputStream output = new FileOutputStream(recordings + "/image1.png");
                    currentImage.compress(Bitmap.CompressFormat.PNG, 100, output); //save file
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent refresh = new Intent(PersonalRecordingsActivity.this, PersonalRecordingsActivity.class);
                startActivity(refresh);
                finish();

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_ACCESS_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
                }
                else {
                    // Permission denied, Unable to create directory
                }
            }
        }
    }

    public void goBack(View view){
        /* closes the activity */
        setResult(RESULT_OK, null);
        finish();
    }

    //List Files
    public void GetFiles()
    {
        if (recordings.isDirectory())
        {
            listFile = recordings.listFiles();


            for (int i = 0; i < listFile.length; i++)
            {

                f.add(listFile[i].getAbsolutePath());

                // Get the path of the image file
                FilePathStrings.add(listFile[i].getAbsolutePath());
                // Get the name image file
                FileNameStrings.add(listFile[i].getName());

            }
        }
    }

    public class ImageAdapter extends BaseAdapter {

        // Declare variables
        private Activity activity;
        private ArrayList<String> filepath;
        private ArrayList<String> filename;

        private LayoutInflater mInflater;

        public ImageAdapter(Activity a, ArrayList<String> fpath, ArrayList<String> fname) {
            activity = a;
            filepath = fpath;
            filename = fname;

            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return f.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            File testfile = new File(FilePathStrings.get(position));
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.galleryitem, null);

                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage); //thumbnail
                holder.fileName = (TextView) convertView.findViewById(R.id.fileName); //name of text

                convertView.setTag(holder);

                //set onclicklistener for each item
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //if image, do this
                        File testfileclicked = new File(FilePathStrings.get(position));
                        if(!testfileclicked.isDirectory()) {
                            Intent i = new Intent(getApplicationContext(), FullScreenImage.class);
                            // Pass String arrays FilePathStrings
                            i.putExtra("filepath", FilePathStrings);
                            // Pass String arrays FileNameStrings
                            i.putExtra("filename", FileNameStrings);
                            // Pass click position
                            i.putExtra("position", position);
                            startActivity(i);
                        }

                        //if folder, set new directory and open new instance
                        else if(testfileclicked.isDirectory()){
                            //do stuff
                        }

                    }
                });
            }
            else {
                holder = (ViewHolder) convertView.getTag();
            }

            //set thumbnail
            final Bitmap myBitmap = BitmapFactory.decodeFile(f.get(position));
            if(!testfile.isDirectory()) {
                holder.imageview.setImageBitmap(myBitmap);
            }
            else if(testfile.isDirectory()){
                holder.imageview.setImageResource(R.drawable.folder);
            }
            //breakdown file path to get only file name
            String filepath = f.get(position);
            ArrayList<String> list = new ArrayList<String>(Arrays.asList(filepath.split("/")));

            //set text name
            holder.fileName.setText(list.get(list.size()-1));

            //set description
            //if not folder
            if(!testfile.isDirectory()) {

            }
            //if folder
            else if(testfile.isDirectory()){

            }

            return convertView;
        }
    }
    class ViewHolder {
        ImageView imageview;
        TextView fileName;
    }

}

