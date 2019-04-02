package com.example.kimjeonghwan.fixyou.signup;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.kimjeonghwan.fixyou.MainActivity;
import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.retrofit.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import retrofit2.adapter.rxjava.Result;

public class SignUpActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_CAMERA = 1111;
    private static final int REQUEST_TAKE_PHOTO = 2222;
    private static final int REQUEST_TAKE_ALBUM = 3333;
    private static final int REQUEST_IMAGE_CROP = 4444;

    String imageFileName;
    String mCurrentPhotoPath;
    String nationStr;

    Uri imageUri;
    Uri photoURI, albumURI;

    ImageView profile;
    AutoCompleteTextView email;
    AutoCompleteTextView password;
    AutoCompleteTextView name;
    AutoCompleteTextView phone;
    ArrayList<String> interest;
    Spinner nation;
    CheckBox sign_up_check;
    Button sign_up_button;

    LinearLayout birth_button;
    RadioGroup radioGroup;

    BottomSheetDialog bottomSheetDialog;

    ImageView select_gallery;
    ImageView select_photo;
    Drawable icon_gallery;
    Drawable icon_photo;

    private Date currentDate;
    private int iYear,iMonth,iDay;
    TextView birth;
    String interestStr;

    List<String> arrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        profile = findViewById(R.id.profile);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        name = findViewById(R.id.name);
        birth = findViewById(R.id.birth);
        phone = findViewById(R.id.phone);
        nation = findViewById(R.id.nation);
        sign_up_check = findViewById(R.id.sign_up_check);
        sign_up_button = findViewById(R.id.sign_up_button);
        birth_button = findViewById(R.id.birth_button);
        radioGroup = findViewById(R.id.radioGroup);

        getIcon();  // 갤러리, 카메라 이미지 적용

        // 원형 프로필이미지 적용
        Glide.with(getApplicationContext())
                .load(R.drawable.kakao_default_profile_image)
                .apply(RequestOptions.circleCropTransform())
                .into(profile);

        // 이전 액티비티에서 선택한 관심사 받아옴
        Intent intent = getIntent();
        interest = intent.getStringArrayListExtra("interest_text");
        for(int i = 0; i < interest.size(); i++){
            interest.get(i);
            if(i == 0){
                interestStr = interest.get(i);
            } else {
                interestStr += ", "+interest.get(i);
            }
        }

        //오늘 날짜 받아옴
        getDateToday();

        //국가 선택
        nation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                nationStr = parent.getItemAtPosition(position).toString();
                Log.e("nationStr", nationStr);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        birth_button.setOnClickListener(v -> {
            String strDate = birth.getText().toString();
            strDate=strDate.replace("년","/").replace("월","/").replace("일","/");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd/");

            try{
                Date pickDate = new Date(strDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(pickDate);
                Dialog dia = null;
                //strDate값을 기본값으로 날짜 선택 다이얼로그 생성
                dia =new DatePickerDialog(SignUpActivity.this, dateSetListener,cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
                dia.show();
            }catch(Exception e){
                e.printStackTrace();
            }
        });

        profile.setOnClickListener(v -> {
            checkPermission();

            // 밑에서 올라오는 다이어로그 적용
            bottomSheetDialog = new BottomSheetDialog(SignUpActivity.this);

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
        });

        sign_up_button.setOnClickListener(v -> attemptSignUp());
    }

    protected void updateEditText(){
        StringBuffer sb = new StringBuffer();
        birth.setText(sb.append(iYear).append("년").append(iMonth + 1).append("월").append(iDay).append("일")
        );
    }

    @SuppressLint("SetTextI18n")
    protected void getDateToday(){
        currentDate = new Date();
        SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdfMon = new SimpleDateFormat("MM");
        SimpleDateFormat sdfDay = new SimpleDateFormat("dd");

        birth.setText(sdfYear.format(currentDate)+"년"+sdfMon.format(currentDate)+"월"+sdfDay.format(currentDate)+"일");
    }

    DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener(){
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            iYear = year;
            iMonth = monthOfYear;
            iDay = dayOfMonth;
            updateEditText();
        }
    };

    private void attemptSignUp() {

        // 입력된 이메일, 비밀번호, 이름, 생일, 성별을 변수에 넣는다.
        String emailStr = email.getText().toString();
        String passwordStr = password.getText().toString();
        String nameStr = name.getText().toString();
        String birthStr = birth.getText().toString();
        String phoneStr = phone.getText().toString();

        // 약관이 체크되어있는지 확인하고 다음단계로 넘어간다.
        if(sign_up_check.isChecked()){
            int id = radioGroup.getCheckedRadioButtonId();
            //getCheckedRadioButtonId() 의 리턴값은 선택된 RadioButton 의 id 값.
            RadioButton rb = findViewById(id);
            String genderStr = rb.getText().toString();

            signUp(emailStr, passwordStr, nameStr, birthStr, genderStr, phoneStr, nationStr, interestStr);
            //signUp2();
        }
    }

    //회원가입완료 했을때
    private void signUp(String email, String password, String name, String birth, String gender, String phone, String nation, String interest){
        // 서버에 저장할 이미지파일을 리사이즈한다.
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        Bitmap b= BitmapFactory.decodeFile(mCurrentPhotoPath);
        Bitmap out = Bitmap.createScaledBitmap(b, 200, 200, false);

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

        Call<ResponseBody> call = RetrofitClient.getInstance().getService().SignUp(body, email, password, name, birth, gender, phone, nation, interest);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    String res = response.body().string();
                    Log.e("res", res);
                    // success값을 받아오면 회원가입 성공
                    if(Objects.equals(res, "success")){
                        // SharedPreferences를 사용해 로그인정보를 저장한다.
                        SharedPreferences pref = getSharedPreferences("login_info", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("email", email);
                        editor.apply();

                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        startActivity(intent);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Toast.makeText(SignUpActivity.this, "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
                                .apply(RequestOptions.circleCropTransform())
                                .into(profile);
                    } catch (Exception e) {
                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
                    }
                } else {
                    Toast.makeText(SignUpActivity.this, "사진찍기를 취소하였습니다.", Toast.LENGTH_SHORT).show();
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
                            .apply(RequestOptions.circleCropTransform())
                            .into(profile);
                }
                break;
        }
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
