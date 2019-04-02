package com.example.kimjeonghwan.fixyou.profile;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.kimjeonghwan.fixyou.R;
import com.example.kimjeonghwan.fixyou.RecyclerItemClickListener;
import com.example.kimjeonghwan.fixyou.ethereum.utils.InfoDialog;
import com.example.kimjeonghwan.fixyou.profile.camera.CameraSourcePreview;
import com.example.kimjeonghwan.fixyou.profile.camera.Exif;
import com.example.kimjeonghwan.fixyou.profile.camera.GraphicOverlay;
import com.example.kimjeonghwan.fixyou.profile.camera.MaskAdapter;
import com.example.kimjeonghwan.fixyou.profile.camera.MaskItem;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class ProfileCameraActivity extends AppCompatActivity implements View.OnClickListener  {
    private static final String TAG = "ProfileCameraActivity";

    private static int CAMERA_FACING;
    private CameraSource mCameraSource = null;  // 카메라 소스 객체
    private CameraSourcePreview cameraPreview;  // 카메라 프리뷰 객체
    private GraphicOverlay faceOverlay;  // 마스크를 보여주는 그래픽 오버레이 객체
    ImageView capturePreview, captureOverlay;
    private FrameLayout captureFrame, cameraFrame;
    private InfoDialog mInfoDialog;

    private Boolean mIsFrontFacing = true;   // 카메라 소스가 전면 카메라에서 오는지 확인하는 변수
    private static final int RC_HANDLE_GMS = 9001;
    // permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    String mCurrentPhotoPath;
    MaskAdapter maskAdapter;
    RecyclerView recyclerView;
    ArrayList<MaskItem> maskItems;
    MaskItem maskItem;
    private int maskPosition = 0;    // 마스크 리스트 중 현재 선택한 마스크의 위치를 저장하고 있는 변수

    private FaceDetector detector;

    ImageView cameraCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_camera);

        cameraPreview = findViewById(R.id.cameraPreview);
        faceOverlay = findViewById(R.id.faceOverlay);
        recyclerView = findViewById(R.id.mask_recyclerview);
        cameraCapture = findViewById(R.id.cameraCapture);
        capturePreview = findViewById(R.id.capturePreview);
        captureOverlay = findViewById(R.id.captureOverlay);
        captureFrame = findViewById(R.id.captureFrame);
        cameraFrame = findViewById(R.id.cameraFrame);

        mInfoDialog = new InfoDialog(this);
        maskItems = new ArrayList<>();
        maskAdapter = new MaskAdapter(maskItems, getApplicationContext(), maskPosition);

        setOnClickListeners();
        initMask();

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource();
        } else {
            requestCameraPermission();
        }
    }

    /**
     * 클릭 리스너 설정
     */
    private void setOnClickListeners(){
        cameraCapture.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cameraCapture:
                mInfoDialog.Get("Load Wallet","Please wait few seconds");
                mCameraSource.takePicture(null, new CameraSource.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data) {
                        int orientation = Exif.getOrientation(data);
                        Bitmap frameBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                        switch(orientation) {
                            case 90:
                                frameBitmap = rotateImage(frameBitmap, 90);

                                break;
                            case 180:
                                frameBitmap = rotateImage(frameBitmap, 180);

                                break;
                            case 270:
                                frameBitmap = rotateImage(frameBitmap, 270);

                                break;
                            case 0:
                                // if orientation is zero we don't need to rotate this

                            default:
                                break;
                        }
                        Matrix sideInversion = new Matrix();
                        sideInversion.setScale(-1, 1);  // 좌우반전
                        frameBitmap = Bitmap.createBitmap(frameBitmap, 0, 0,
                                frameBitmap.getWidth(), frameBitmap.getHeight(), sideInversion, false);

                        faceOverlay.setDrawingCacheEnabled(true);    // 그래픽 오버레이의 드로잉 캐시를 가능하게 한다.
                        faceOverlay.buildDrawingCache(false);
                        Bitmap overlayBitmap = faceOverlay.getDrawingCache(true);   // 그래픽 오버레이를 비트맵으로 변경한다.

                        capturePreview.setImageBitmap(frameBitmap);        // 캡쳐할 프리뷰에 이미지를 넣는다.
                        captureOverlay.setImageBitmap(null);
                        captureOverlay.setImageBitmap(overlayBitmap);      // 캡쳐할 오버레이뷰에 이미지를 넣는다.

                        captureFrame.setDrawingCacheEnabled(true);       // 캡쳐할 프레임 레이아웃의 드로잉 캐시를 가능하게 한다.
                        captureFrame.buildDrawingCache(true);    // 캡쳐할 프레임의 오토 스케일을 킨다.

                        cameraFrame.setVisibility(View.GONE);
                        Bitmap captureBitmap = Bitmap.createBitmap(captureFrame.getDrawingCache());

                        FileOutputStream outStream = null;   // 이미지 파일을 작성할 아웃풋 스트림

                        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                        String imageFileName = "JPEG_" + timeStamp + ".jpg";
                        File imageFile = null;
                        File storageDir = new File(Environment.getExternalStorageDirectory() + "/Pictures", "gyeom");

                        if (!storageDir.exists()) {
                            Log.e("mCurrentPhotoPath", storageDir.toString());
                            storageDir.mkdirs();
                        }

                        imageFile = new File(storageDir, imageFileName);
                        mCurrentPhotoPath = imageFile.getAbsolutePath();

                        try {
                            outStream = new FileOutputStream(imageFile);
                            captureBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);     // 파일을 저장한다.
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        refreshGallery(imageFile);
                        mInfoDialog.Dismiss();   // 이미지 저장 다이얼로그 종료
                    }
                });
                break;
        }
    }

    public static Bitmap rotateImage(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(),   source.getHeight(), matrix,
                true);
    }

    /**
     * 갤러리 새로고침
     */
    private void refreshGallery(File file) {
        Intent mediaScanIntent = new Intent( Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(Uri.fromFile(file));
        sendBroadcast(mediaScanIntent);
    }

    /**
     * 마스크 아이템 초기화
     */
    private void initMaskItems() {
        // 임의의 데이터입니다.
        List<String> listName = Arrays.asList("none", "captin", "starwars", "op", "iron", "cat", "dog", "crown");
        List<Boolean> listIsMask = Arrays.asList(false, true, true, true, true, true, true, true);
        List<Boolean> listIsSelected = Arrays.asList(true, false, false, false, false, false, false, false);

        for (int i = 0; i < listName.size(); i++) {
            // 각 List의 값들을 data 객체에 set 해줍니다.
            maskItem = new MaskItem();
            maskItem.setName(listName.get(i));
            maskItem.setIsMask(listIsMask.get(i));
            maskItem.setIsSelected(listIsSelected.get(i));

            // 각 값이 들어간 data를 adapter에 추가합니다.
            maskItems.add(maskItem);
        }
    }

    /**
     * 레이아웃 초기화
     */
    private void initMask(){
        initMaskItems();
        recyclerView.setAdapter(maskAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), LinearLayout.HORIZONTAL, false));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), recyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        runOnUiThread(new Runnable() {
                            public void run() {
                                maskAdapter.setSelect(position); // 마스크 아이템의 선택 상태를 변경한다.
                            }
                        });
                        maskPosition = position;
                        changeMask();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        Toast.makeText(getApplicationContext(),position+"번 째 아이템 롱 클릭",Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    /**
     * 마스크를 변경하는 메소드
     */
    private void changeMask(){
        detector.setProcessor(
                new MultiProcessor.Builder<>(new GraphicFaceTrackerFactory())
                        .build());
    }

    /**
     * 카메라의 권한을 요청
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the barcode detector to detect small barcodes
     * at long distances.
     */
    private void createCameraSource() {
        Context context = getApplicationContext();
        detector = new FaceDetector.Builder(context)
                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                .setTrackingEnabled(true)
                .setMode(FaceDetector.ACCURATE_MODE)
                .setProminentFaceOnly(mIsFrontFacing)
                .setMinFaceSize(0.35f)
                .build();

        detector.setProcessor(
                new MultiProcessor.Builder<Face>(new GraphicFaceTrackerFactory())
                        .build());

        if (!detector.isOperational()) {
            Log.w(TAG, "Face detector dependencies are not yet available.");
        }

        mCameraSource = new CameraSource.Builder(context, detector)
                .setRequestedPreviewSize(640, 480)
                .setFacing(CameraSource.CAMERA_FACING_FRONT)
                .setRequestedFps(60.0f)
                .setAutoFocusEnabled(true)
                .build();
    }

    /**
     * 뒤로가기 버튼 클릭
     */
    @Override
    public void onBackPressed() {
        if(cameraFrame.getVisibility() == View.GONE){
            cameraFrame.setVisibility(View.VISIBLE);

            startCameraSource();
        } else {
            finish();
        }
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.stop();
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            createCameraSource();
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Face Tracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    //==============================================================================================
    // Camera Source Preview
    //==============================================================================================

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (mCameraSource != null) {
            try {
                cameraPreview.start(mCameraSource, faceOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        }
    }

    //==============================================================================================
    // Graphic Face Tracker
    //==============================================================================================

    /**
     * Factory for creating a face tracker to be associated with a new face.  The multiprocessor
     * uses this factory to create face trackers as needed -- one for each individual.
     */
    private class GraphicFaceTrackerFactory implements MultiProcessor.Factory<Face> {
        @Override
        public Tracker<Face> create(Face face) {
            return new GraphicFaceTracker(faceOverlay);
        }
    }

    /**
     * Face tracker for each detected individual. This maintains a face graphic within the app's
     * associated face overlay.
     */
    private class GraphicFaceTracker extends Tracker<Face> {
        private GraphicOverlay mOverlay;
        private FaceGraphic mFaceGraphic;

        GraphicFaceTracker(GraphicOverlay overlay) {
            mOverlay = overlay;
            mFaceGraphic = new FaceGraphic(overlay, maskPosition, getApplicationContext());
        }

        /**
         * Start tracking the detected face instance within the face overlay.
         */
        @Override
        public void onNewItem(int faceId, Face item) {
            mFaceGraphic.setId(faceId);
        }

        /**
         * Update the position/characteristics of the face within the overlay.
         */
        @Override
        public void onUpdate(FaceDetector.Detections<Face> detectionResults, Face face) {
            mOverlay.add(mFaceGraphic);
            mFaceGraphic.updateFace(face);
        }

        /**
         * Hide the graphic when the corresponding face was not detected.  This can happen for
         * intermediate frames temporarily (e.g., if the face was momentarily blocked from
         * view).
         */
        @Override
        public void onMissing(FaceDetector.Detections<Face> detectionResults) {
            mOverlay.remove(mFaceGraphic);
        }

        /**
         * Called when the face is assumed to be gone for good. Remove the graphic annotation from
         * the overlay.
         */
        @Override
        public void onDone() {
            mOverlay.remove(mFaceGraphic);
        }
    }
}
