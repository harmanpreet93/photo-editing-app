package harman.myinstaapp;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainCameraActivity extends AppCompatActivity implements
        View.OnClickListener{

    private FloatingActionButton fabCamera, fabGallery, fabCrop, fabEdit;

    static final int REQUEST_CLICK_IMG = 1;
    static final int RESULT_LOAD_IMG = 2;
    final int RESULT_CROP_IMG = 3;

    private ImageView editingImage;

    private String mCurrentPhotoPath;
    private String imgDecodableString;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory = null;

    private String TAG = "MYINSTAAPP";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_camera);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAlbumStorageDirFactory = new AlbumDirFactory();

        fabCamera = (FloatingActionButton)findViewById(R.id.fab_camera);
        fabGallery = (FloatingActionButton)findViewById(R.id.fab_gallery);
        fabCrop = (FloatingActionButton)findViewById(R.id.fab_crop);
        fabEdit = (FloatingActionButton)findViewById(R.id.fab_edit);

        fabCamera.setOnClickListener(this);
        fabGallery.setOnClickListener(this);
        fabCrop.setOnClickListener(this);
        fabEdit.setOnClickListener(this);
        editingImage = (ImageView)findViewById(R.id.editing_image);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fab_camera:
                dispatchTakePictureIntent();
                Log.d(TAG, "Fab Camera");
                break;
            case R.id.fab_gallery:
                Log.d(TAG, "Fab Gallery");
                openGallery();
                break;
            case R.id.fab_crop:
                if (imgDecodableString != null) {
                    File f = new File(imgDecodableString);
                    Uri picUri = Uri.fromFile(f);
                    performCrop(picUri);
                }
                else {
                    new AlertDialog.Builder(MainCameraActivity.this)
                            .setMessage("No image found")
                            .setCancelable(false)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                }
                break;
            case R.id.fab_edit:
                if (imgDecodableString != null) {
                    openNewActivity(AfterCropActivity.class);
                }
                else {
                    new AlertDialog.Builder(MainCameraActivity.this)
                            .setMessage("No image found")
                            .setCancelable(false)
                            .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            }).show();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CLICK_IMG:
                    handleCameraPhoto();
                    break;
                case RESULT_LOAD_IMG:
                    handleGalleryPhoto(data);
                    break;
                case RESULT_CROP_IMG:
                    //get the returned data
                    Bundle extras = data.getExtras();
                    if(extras != null) {
                        //get the cropped bitmap
                        Bitmap thePic = extras.getParcelable("data");
                        //display the returned cropped image
                        editingImage.setImageBitmap(thePic);
                    }
                    else {
                        Bitmap bitmap = BitmapFactory.decodeFile(imgDecodableString);
                        editingImage.setImageBitmap(bitmap);
                    }
            }
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = setUpPhotoFile();
                mCurrentPhotoPath = photoFile.getAbsolutePath();
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
            }catch (IOException ex) {
                photoFile = null;
                mCurrentPhotoPath = null;
                Log.d(TAG,"Error: " + ex.toString());
                // Error occurred while creating the File
                new AlertDialog.Builder(MainCameraActivity.this)
                        .setMessage("Couldn't create image")
                        .setCancelable(false)
                        .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_CLICK_IMG);
            }
        }

    }

    private void openGallery() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // Start the Intent
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void handleCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            setPic();
            galleryAddPic();
            imgDecodableString = mCurrentPhotoPath;
            mCurrentPhotoPath = null;
        }

    }


    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = editingImage.getWidth();
        int targetH = editingImage.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        editingImage.setImageBitmap(bitmap);
    }


    /* Photo album for this application */
    private String getAlbumName() {
        return getString(R.string.album_name);
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d(TAG, "Failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private void handleGalleryPhoto(Intent data) {
        Uri selectedImage = data.getData();
        String[] filePathColumn = { MediaStore.Images.Media.DATA };

        // Get the cursor
        Cursor cursor = getContentResolver().query(selectedImage,
                filePathColumn, null, null, null);
        // Move to first row
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        imgDecodableString = cursor.getString(columnIndex);
        cursor.close();
        // Set the Image in ImageView after decoding the String
        editingImage.setImageBitmap(BitmapFactory
                .decodeFile(imgDecodableString));
    }

    private void performCrop(Uri picUri) {

        try {
            //call the standard crop action intent (the user device may not support it)
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, RESULT_CROP_IMG);

        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            new AlertDialog.Builder(MainCameraActivity.this)
                    .setMessage("Device doesn't support crop action")
                    .setCancelable(false)
                    .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).show();

        }
    }

    private void openNewActivity(Class activity) {
        Log.d(TAG,"opening new activity");
        Intent intent = new Intent(MainCameraActivity.this,activity);
        intent.putExtra("image_path", imgDecodableString);
        startActivity(intent);
    }

}
