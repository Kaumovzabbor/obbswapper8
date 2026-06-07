package com.obbswapper;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity {

    EditText etPackageName, etObbName, etSrcName;
    Button btnPermission, btnDelete, btnCopyReal, btnCopyTest;
    ProgressBar progressBar;
    TextView tvLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etPackageName = findViewById(R.id.etPackageName);
        etObbName     = findViewById(R.id.etObbName);
        etSrcName     = findViewById(R.id.etSrcName);
        btnPermission = findViewById(R.id.btnPermission);
        btnDelete     = findViewById(R.id.btnDelete);
        btnCopyReal   = findViewById(R.id.btnCopyReal);
        btnCopyTest   = findViewById(R.id.btnCopyTest);
        progressBar   = findViewById(R.id.progressBar);
        tvLog         = findViewById(R.id.tvLog);

        btnPermission.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= 30) {
                if (!Environment.isExternalStorageManager()) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    intent.setData(Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    tvLog.append("Ruxsat allaqachon berilgan!\n");
                }
            }
        });

        btnDelete.setOnClickListener(v -> {
            String pkg = etPackageName.getText().toString().trim();
            String obbName = etObbName.getText().toString().trim();
            if (obbName.isEmpty()) obbName = "1.obb";
            File target = new File(Environment.getExternalStorageDirectory(),
                    "Android/obb/" + pkg + "/" + obbName);
            if (target.exists()) {
                target.delete();
                tvLog.append("O'chirildi: " + target.getAbsolutePath() + "\n");
            } else {
                tvLog.append("Fayl topilmadi!\n");
            }
        });

        btnCopyReal.setOnClickListener(v -> copyObb("Testobb/real/"));
        btnCopyTest.setOnClickListener(v -> copyObb("Testobb/test/"));
    }

    void copyObb(String srcFolder) {
        String pkg     = etPackageName.getText().toString().trim();
        String obbName = etObbName.getText().toString().trim();
        String srcName = etSrcName.getText().toString().trim();
        if (obbName.isEmpty()) obbName = "1.obb";
        if (srcName.isEmpty()) srcName = "1.obb";

        File ext     = Environment.getExternalStorageDirectory();
        File source  = new File(ext, srcFolder + srcName);
        File destDir = new File(ext, "Android/obb/" + pkg);
        File dest    = new File(destDir, obbName);

        if (!source.exists()) { tvLog.append("Manba topilmadi: " + source.getAbsolutePath() + "\n"); return; }
        if (!destDir.exists()) destDir.mkdirs();
        if (dest.exists()) dest.delete();

        progressBar.setProgress(0);
        try {
            FileInputStream fis  = new FileInputStream(source);
            FileOutputStream fos = new FileOutputStream(dest);
            FileChannel in       = fis.getChannel();
            FileChannel out      = fos.getChannel();
            long total = in.size(), done = 0, chunk = 1048576L;
            while (done < total) {
                done += in.transferTo(done, chunk, out);
                int pct = (int)(done * 100 / total);
                progressBar.setProgress(pct);
            }
            in.close(); out.close();
            fis.close(); fos.close();
            tvLog.append("Tayyor! Nusxalandi: " + dest.getAbsolutePath() + "\n");
        } catch (Exception e) {
            tvLog.append("Xato: " + e.getMessage() + "\n");
        }
    }
          }
