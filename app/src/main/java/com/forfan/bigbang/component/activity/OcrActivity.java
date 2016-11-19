package com.forfan.bigbang.component.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.forfan.bigbang.R;
import com.forfan.bigbang.component.base.BaseActivity;
import com.forfan.bigbang.cropper.BitmapUtil;
import com.forfan.bigbang.cropper.CropHandler;
import com.forfan.bigbang.cropper.CropHelper;
import com.forfan.bigbang.cropper.CropParams;
import com.forfan.bigbang.cropper.ImageUriUtil;
import com.forfan.bigbang.util.OcrAnalsyser;
import com.forfan.bigbang.util.SnackBarUtil;
import com.forfan.bigbang.util.StatusBarCompat;
import com.microsoft.projectoxford.vision.contract.Line;
import com.microsoft.projectoxford.vision.contract.OCR;
import com.microsoft.projectoxford.vision.contract.Region;
import com.microsoft.projectoxford.vision.contract.Word;

/**
 * Created by wangyan-pd on 2016/11/9.
 */

public class OcrActivity extends BaseActivity implements View.OnClickListener, CropHandler {
    private static final String TAG = OcrActivity.class.getName();
    private CropParams mCropParams;
    private ImageView mImageView;
    private TextView mResultTextView;
    private Button mPicReOcr;
    private Uri mCurrentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orc);
        StatusBarCompat.setupStatusBarView(this, (ViewGroup) getWindow().getDecorView(), true, R.color.colorPrimary);
        mCropParams = new CropParams(this);
        mImageView = (ImageView) findViewById(R.id.image);
        mResultTextView = (TextView) findViewById(R.id.result);
        mPicReOcr = (Button) findViewById(R.id.re_ocr);
        findViewById(R.id.take_pic).setOnClickListener(this);
        findViewById(R.id.select_pic).setOnClickListener(this);
        findViewById(R.id.re_ocr).setOnClickListener(this);
        parseIntent(getIntent());

        mResultTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OcrActivity.this, BigBangActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(BigBangActivity.TO_SPLIT_STR, mResultTextView.getText());
                startActivity(intent);
            }
        });
    }

    private void parseIntent(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            if (intent.getClipData() != null && intent.getClipData().getItemAt(0) != null && intent.getClipData().getItemAt(0).getUri() != null) {
                Uri uri = intent.getClipData().getItemAt(0).getUri();
                showBitmapandOcr(uri);
            }
        } else {
            Uri uri = intent.getData();
            showBitmapandOcr(uri);
        }

    }

    private void showBitmapandOcr(Uri uri) {
        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageBitmap(BitmapUtil.decodeUriAsBitmap(this, uri));
        uploadImage4Ocr(uri);
        mCurrentUri = uri;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        parseIntent(intent);

    }

    @Override
    public void onClick(View v) {
        mCropParams.refreshUri();

        switch (v.getId()) {
            case R.id.take_pic:
                mCropParams.enable = true;
                mCropParams.compress = false;
                Intent intent = CropHelper.buildCameraIntent(mCropParams);
                startActivityForResult(intent, CropHelper.REQUEST_CAMERA);
                mPicReOcr.setVisibility(View.GONE);

                break;
            case R.id.select_pic:
                mCropParams.enable = true;
                mCropParams.compress = false;
                Intent intent1 = CropHelper.buildGalleryIntent(mCropParams);
                startActivityForResult(intent1, CropHelper.REQUEST_CROP);
                mPicReOcr.setVisibility(View.GONE);
                break;
            case R.id.re_ocr:
                if (mCurrentUri != null)
                    uploadImage4Ocr(mCurrentUri);
                break;

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        CropHelper.handleResult(this, requestCode, resultCode, data);
        if (requestCode == 1) {
            Log.e(TAG, "");
        }
    }

    @Override
    protected void onDestroy() {
        CropHelper.clearCacheDir();
        super.onDestroy();
    }

    @Override
    public CropParams getCropParams() {
        return mCropParams;
    }

    @Override
    public void onPhotoCropped(Uri uri) {
        // Original or Cropped uri
        Log.d(TAG, "Crop Uri in path: " + uri.getPath());

        if (!mCropParams.compress) {
            showBitmapandOcr(uri);

        }

    }


    private void uploadImage4Ocr(Uri uri) {
        String img_path = ImageUriUtil.getImageAbsolutePath(this, uri);
        // VisionServiceRestClient client = new VisionServiceRestClient("00b0e581e4124a2583ea7dba57aaf281");
        OcrAnalsyser.getInstance().analyse(this, img_path, new OcrAnalsyser.CallBack() {
            @Override
            public void onSucess(OCR ocr) {
                mResultTextView.setText(OcrAnalsyser.getInstance().getPasedMiscSoftText(ocr));
            }

            @Override
            public void onFail() {
                mResultTextView.setText(R.string.sorry_for_parse_fail);
                mPicReOcr.setVisibility(View.VISIBLE);
            }
        });
//        RequestBody requestBody =
//                RequestBody.create(MediaType.parse("multipart/form-data"), file);
//        RetrofitHelper.getOcrService()
//                .uploadImage("e02e6b613488957", descriptionString, requestBody)
//                .compose(this.bindToLifecycle())
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(recommendInfo -> {
//                    LogUtil.d(recommendInfo.toString());
//                    mResultTextView.setText(getPasedText(recommendInfo));
//                }, throwable -> {
//                    LogUtil.d(throwable.toString());
//                    mResultTextView.setText(R.string.sorry_for_parse_fail);
//                    mPicReOcr.setVisibility(View.VISIBLE);
//                });

    }

    private String getPasedMiscSoftText(OCR r) {
        String result = "";
        for (Region reg : r.regions) {
            for (Line line : reg.lines) {
                for (Word word : line.words) {
                    result += word.text + " ";
                }
                result += "\n";
            }
            result += "\n\n";
        }
        return result;
    }

    private void showBigBang(String result) {
        Intent intent = new Intent(this, BigBangActivity.class);
        intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BigBangActivity.TO_SPLIT_STR, result);
        startActivity(intent);
    }

    @Override
    public void onCompressed(Uri uri) {
        // Compressed uri
        mImageView.setImageBitmap(BitmapUtil.decodeUriAsBitmap(this, uri));

    }

    @Override
    public void onCancel() {
        SnackBarUtil.show(mResultTextView, "Crop canceled!");
    }

    @Override
    public void onFailed(String message) {
        SnackBarUtil.show(mResultTextView, "Crop failed: " + message);
    }

    @Override
    public void handleIntent(Intent intent, int requestCode) {
        startActivityForResult(intent, requestCode);
    }


}
