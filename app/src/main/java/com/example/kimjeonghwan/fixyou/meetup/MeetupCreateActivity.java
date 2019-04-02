package com.example.kimjeonghwan.fixyou.meetup;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.kimjeonghwan.fixyou.MainActivity;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetupCreateActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;

    String imageFileName;
    String mCurrentPhotoPath;

    Uri imageUri;
    Uri photoURI, albumURI;

    BottomSheetDialog bottomSheetDialog;

    ImageView select_gallery;
    ImageView select_photo;
    Drawable icon_gallery;
    Drawable icon_photo;

    ImageView meetup_image;
    EditText meetup_name;
    EditText meetup_explanation;
    Button meetup_create_button;
    TextView text_location;
    LinearLayout meetup_location;
    LinearLayout meetup_interest;
    ScrollView meetup_form;
    ProgressBar meetup_progress;
    private Intent intent;

    String userEmail;
    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meetup_create);

        meetup_image = findViewById(R.id.meetup_image);  // 그룹 이미지 설정
        meetup_name = findViewById(R.id.meetup_name);  // 그룹 이름 입력
        meetup_explanation = findViewById(R.id.meetup_explanation);  // 그룹 설명 입력
        meetup_interest = findViewById(R.id.meetup_interest);  // 그룹 관심사 입력
        meetup_location = findViewById(R.id.meetup_location);  // 그룹 위치 입력
        meetup_create_button = findViewById(R.id.meetup_create_button);  // 입력 완료 버튼
        text_location = findViewById(R.id.text_location);
        meetup_form = findViewById(R.id.meetup_form);
        meetup_progress = findViewById(R.id.meetup_progress);

        getIcon();  // 갤러리, 카메라 이미지 적용

        meetup_image.setOnClickListener(this);
        meetup_location.setOnClickListener(this);
        meetup_interest.setOnClickListener(this);
        meetup_create_button.setOnClickListener(this);

        userEmail = getSharedPreferences("login_info", MODE_PRIVATE).getString("email","none"); // SharedPreference 에서 이메일 정보를 가져온다.
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.meetup_image:
                checkPermission();
                // 밑에서 올라오는 다이어로그 적용
                bottomSheetDialog = new BottomSheetDialog(MeetupCreateActivity.this);

                @SuppressLint("InflateParams") View view = getLayoutInflater().inflate(R.layout.dialog_bottom_sheet, null);
                bottomSheetDialog.setContentView(view);
                bottomSheetDialog.show();

                LinearLayout dialog_gallery = view.findViewById(R.id.dialog_gallery);
                LinearLayout dialog_photo = view.findViewById(R.id.dialog_photo);
                select_gallery = view.findViewById(R.id.select_gallery);
                select_photo = view.findViewById(R.id.select_photo);

                select_gallery.setImageDrawable(icon_gallery);
                select_photo.setImageDrawable(icon_photo);

                dialog_gallery.setOnClickListener(v13 -> {
                    getAlbum();
                    bottomSheetDialog.dismiss();
                });
                dialog_photo.setOnClickListener(v1 -> {
                    captureCamera();
                    bottomSheetDialog.dismiss();
                });
                break;
            case R.id.meetup_location:
                intent = new Intent(MeetupCreateActivity.this, MeetupLocationActivity.class);
                startActivityForResult(intent, 3000);
                break;
            case R.id.meetup_interest:
                Log.e("meetup_interest", "meetup_interest");
                intent = new Intent(MeetupCreateActivity.this, MeetupInterestActivity.class);
                startActivityForResult(intent, 4000);
                break;
            case R.id.meetup_create_button:
                attemptCreate();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK) // 액티비티가 정상적으로 종료되었을 경우
        {
            if(requestCode==3000) // requestCode==1 로 호출한 경우에만 처리합니다.
            {
                String address = data.getStringExtra("address");
                text_location.setText(address);
            }
        }
        switch (requestCode) {
            case REQUEST_TAKE_PHOTO:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        Log.i("REQUEST_TAKE_PHOTO", "OK");
                        galleryAddPic();

                        //profile.setImageURI(imageUri);
                        // 원형 프로필이미지 적용
                        Glide.with(getApplicationContext())
                                .load(imageUri)
                                .into(meetup_image);
                    } catch (Exception e) {
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                } else {
                    Toast.makeText(MeetupCreateActivity.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_TAKE_ALBUM:
                if (resultCode == Activity.RESULT_OK) {

                    if(data.getData() != null){
                        try {
                            File albumFile = null;
                            albumFile = createImageFile();
                            photoURI = data.getData();
                            albumURI = Uri.fromFile(albumFile);
                            cropImage();
                        }catch (Exception e){
                            Log.e("TAKE_ALBUM_SINGLE ERROR", e.toString());
                        }
                    }
                }
                break;

            case REQUEST_IMAGE_CROP:
                if (resultCode == Activity.RESULT_OK) {

                    galleryAddPic();
                    //profile.setImageURI(albumURI);
                    // 원형 프로필이미지 적용
                    Glide.with(getApplicationContext())
                            .load(albumURI)
                            .into(meetup_image);
                }
                break;
        }
    }

    private void attemptCreate() {
        showProgress(true);
        meetupCreate(meetup_name.getText().toString(), meetup_explanation.getText().toString());
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            meetup_form.setVisibility(show ? View.GONE : View.VISIBLE);
            meetup_form.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    meetup_form.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            meetup_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            meetup_progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    meetup_progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            meetup_progress.setVisibility(show ? View.VISIBLE : View.GONE);
            meetup_form.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    private void meetupCreate(String name, String explanation){
        // 서버에 저장할 이미지파일을 리사이즈한다.
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Bitmap b= BitmapFactory.decodeFile(mCurrentPhotoPath);
        Bitmap out = Bitmap.createScaledBitmap(b, 700, 700, false);

        File file = new File(dir, imageFileName);

        FileOutputStream fOut;
        try {
            fOut = new FileOutputStream(file);
            out.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
        } catch (Exception ignored) {}

        RequestBody reqFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaded_file", file.getName(), reqFile);

        Call<ResponseBody> call = RetrofitClient.getInstance().getService().MeetupCreate(body, name, explanation, userEmail);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.e("res", res);

                    // success값을 받아오면 회원가입 성공
                    if(Objects.equals(res, "error")){
                        AlertDialog.Builder msg = new AlertDialog.Builder(getApplicationContext());
                        msg.setTitle( "알림");
                        msg.setMessage("그룹생성에 실패했습니다.");
                        msg.setPositiveButton("확인",  null);
                        msg.show();
                        finish();
                    }else {
                        SharedPreferences sharedPreferences = getSharedPreferences("login_info",MODE_PRIVATE);
                        //저장을 하기위해 editor를 이용하여 값을 저장시켜준다.
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("meetupId",res); // key, value를 이용하여 저장하는 형태
                        //최종 커밋
                        editor.apply();
                        finish();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MeetupCreateActivity.this, "Error" + t.getMessage().toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void captureCamera(){
        String state = Environment.getExternalStorageState();
        // 외장 메모리 검사
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                } catch (IOException ex) {
                    Log.e("captureCamera Error", ex.toString());
                }
                if (photoFile != null) {
                    // getUriForFile의 두 번째 인자는 Manifest provier의 authorites와 일치해야 함

                    Uri providerURI = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                    imageUri = providerURI;

                    // 인텐트에 전달할 때는 FileProvier의 Return값인 content://로만!!, providerURI의 값에 카메라 데이터를 넣어 보냄
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerURI);

                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
            }
        } else {
            Toast.makeText(this, "저장공간이 접근 불가능한 기기입니다", Toast.LENGTH_SHORT).show();
        }
    }

    public File createImageFile() throws IOException {
        // 이미지파일 이름생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "gyeom");

        if (!storageDir.exists()) {
            Log.i("mCurrentPhotoPath1", storageDir.toString());
            storageDir.mkdirs();
        }

        imageFile = new File(storageDir, imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }

    private void getAlbum(){
        Log.i("getAlbum", "Call");
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, REQUEST_TAKE_ALBUM);
    }

    private void galleryAddPic(){
        Log.i("galleryAddPic", "Call");
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        // 해당 경로에 있는 파일을 객체화(새로 파일을 만든다는 것으로 이해하면 안 됨)
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        sendBroadcast(mediaScanIntent);
        Toast.makeText(this, "사진이 앨범에 저장되었습니다.", Toast.LENGTH_SHORT).show();
    }

    // 카메라 전용 크랍
    public void cropImage(){
        Log.i("cropImage", "Call");
        Log.i("cropImage", "photoURI : " + photoURI + " / albumURI : " + albumURI);

        Intent cropIntent = new Intent("com.android.camera.action.CROP");

        // 50x50픽셀미만은 편집할 수 없다는 문구 처리 + 갤러리, 포토 둘다 호환하는 방법
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        cropIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cropIntent.setDataAndType(photoURI, "image/*");
        //cropIntent.putExtra("outputX", 200); // crop한 이미지의 x축 크기, 결과물의 크기
        //cropIntent.putExtra("outputY", 200); // crop한 이미지의 y축 크기
        cropIntent.putExtra("aspectX", 1); // crop 박스의 x축 비율, 1&1이면 정사각형
        cropIntent.putExtra("aspectY", 1); // crop 박스의 y축 비율
        cropIntent.putExtra("scale", true);
        cropIntent.putExtra("output", albumURI); // 크랍된 이미지를 해당 경로에 저장
        startActivityForResult(cropIntent, REQUEST_IMAGE_CROP);
    }

    private void checkPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 처음 호출시엔 if()안의 부분은 false로 리턴 됨 -> else{..}의 요청으로 넘어감
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
                    (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))) {
                new AlertDialog.Builder(this)
                        .setTitle("알림")
                        .setMessage("저장소 권한이 거부되었습니다. 사용을 원하시면 설정에서 해당 권한을 직접 허용하셔야 합니다.")
                        .setNeutralButton("설정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                intent.setData(Uri.parse("package:" + getPackageName()));
                                startActivity(intent);
                            }
                        })
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setCancelable(false)
                        .create()
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, MY_PERMISSION_CAMERA);
            }
        }
    }

    // 나의 앱 리스트를 불러옴
    @SuppressLint("LongLogTag")
    private void getIcon() {
        PackageManager pkgm = getApplication().getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> AppInfos = pkgm.queryIntentActivities(intent, 0);
        for (ResolveInfo info : AppInfos) {
            ActivityInfo ai = info.activityInfo;
            if(ai.loadLabel(pkgm).toString().equals("갤러리")){
                try {
                    icon_gallery = getPackageManager().getApplicationIcon(ai.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            if(ai.loadLabel(pkgm).toString().equals("카메라")){
                try {
                    icon_photo = getPackageManager().getApplicationIcon(ai.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
