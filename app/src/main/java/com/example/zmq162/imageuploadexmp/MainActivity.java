package com.example.zmq162.imageuploadexmp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button choose,upload;
    EditText editTextname;
    private int PICK_IMAGE_REQUEST = 1;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    ImageView imageView;
    Bitmap bitmap;
    ProgressDialog progressDialog;
    private String UPLOAD_URL ="http://connect2mfi.org/ReferralPlatform/UniversalRegistrationController/fileUpload1";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextname = (EditText) findViewById(R.id.editText);
        choose = (Button) findViewById(R.id.buttonChoose);
        upload = (Button) findViewById(R.id.buttonUpload);

        imageView = (ImageView) findViewById(R.id.imageView);
        choose.setOnClickListener(this);
        upload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v==choose){
            showFileChooser();
        }
        if(v==upload){
//            uploadImage();
            uploadImage1();
        }
    }

    private void showFileChooser(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"image chooser"),PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageView.setImageBitmap(bitmap);
                System.out.println("image set");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImage(){
        //Showing the progress dialog
//        final ProgressDialog progressDialog = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Uploading, please wait...");
        progressDialog.show();

        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Disimissing the progress dialog
                progressDialog.dismiss();
                //Showing toast message of the response
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();

                //Showing toast
                Toast.makeText(MainActivity.this, error.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                String image = getStringImage(bitmap);
                String name = editTextname.getText().toString().trim();
                Map<String,String> params = new HashMap<String,String>();
                params.put(KEY_IMAGE,image);
                params.put(KEY_NAME,name);
                System.out.println("Params:"+params);
                return params;
            }
        };
//        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        requestQueue.add(stringRequest);
        AppController.getInstance().addToRequestQueue(stringRequest);
    }

    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage1(){
        final ProgressDialog progressDialog = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        File file = new File(bitmap.toString());
        PhotoMultipartRequest photoMultipartRequest = new PhotoMultipartRequest(UPLOAD_URL, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();//
                //Showing toast
                Toast.makeText(MainActivity.this, error.getMessage().toString(),Toast.LENGTH_SHORT).show();
            }
        }, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                //Showing toast message of the response
                Toast.makeText(MainActivity.this, response, Toast.LENGTH_LONG).show();
            }
        },file);

        AppController.getInstance().addToRequestQueue(photoMultipartRequest);
    }
}
