package com.obbswapper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.Settings;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    EditText etPackageName, etObbName, etSrcName;
    Button btnPermission, btnDelete, btnCopyReal, btnCopyTest;
    ProgressBar progressBar;
    TextView tvLog;

    Uri obbDirUri = null;
    String pendingSrcFolder = null;

    ActivityResultLauncher<Intent> dirPicker = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                obbDirUri = result.getData().getData();
                getContentResolver().takePersistableUriPermission(obbDirUri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                tvLog.append("Papka tanlandi!\n");
                if (pendingSrcFolder != null) {
                    copyObb(pendingSrcFolder);
                    pendingSrcFolder = null;
                }
            }
        });

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
            String pkg = etPackageName.getText().toString().trim();
            if (pkg.isEmpty()) { tvLog.append("Package name kiriting!\n"); return; }
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            Uri uri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb%2F" + pkg);
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
            dirPicker.launch(intent);
        });

        btnDelete.setOnClickListener(v -> {
            if (obbDirUri == null) { tvLog.append("Avval PERMISSION bosing!\n"); return; }
            String obbName = etObbName.getText().toString().trim();
            if (obbName.isEmpty()) obbName = "1.obb";
            DocumentFile dir = DocumentFile.fromTreeUri(this, obbDirUri);
            if (dir != null) {
                DocumentFile file = dir.findFile(obbName);
                if (file != null) {
                    file.delete();
                    tvLog.append("O'chirildi: " + obbName + "\n");
                } else {
                    tvLog.append("Fayl topilmadi!\n");
                }
            }
        });

        btnCopyReal.setOnClickListener(v -> {
            if (obbDirUri == null) { pendingSrcFolder = "Testobb/real/"; btnPermission.performClick(); return; }
            copyObb("Testobb/real/");
        });

        btnCopyTest.setOnClickListener(v -> {
            if (obbDirUri == null) { pendingSrcFolder = "Testobb/test/"; btnPermission.performClick(); return; }
            copyObb("Testobb/test/");
        });
    }

    void copyObb(String srcFolder) {
        String obbName = etObbName.getText().toString().trim();
        String srcName = etSrcName.getText().toString().trim();
        if (obbName.isEmpty()) obbName = "1.obb";
        if (srcName.isEmpty()) srcName = "1.obb";

        File source = new File(Environment.getExternalStorageDirectory(), srcFolder + srcName);
        if (!source.exists()) { tvLog.append("Manba topilmadi: " + source.getAbsolutePath() + "\n"); return; }

        DocumentFile dir = DocumentFile.fromTreeUri(this, obbDirUri);
        if (dir == null) { tvLog.append("Papka xatosi!\n"); return; }

        DocumentFile existing = dir.findFile(obbName);
        if (existing != null) existing.delete();

        DocumentFile newFile = dir.createFile("application/octet-stream", obbName);
        if (newFile == null) { tvLog.append("Fayl yaratib bo'lmadi!\n"); return; }

        progressBar.setProgress(0);
        try {
            InputStream in   = new FileInputStream(source);
            OutputStream out = getContentResolver().openOutputStream(newFile.getUri());
            byte[] buf = new byte[1048576];
            long total = source.length(), done = 0;
            int read;
            while ((read = in.read(buf)) != -1) {
                out.write(buf, 0, read);
                done += read;
                progressBar.setProgress((int)(done * 100 / total));
            }
            in.close();
            out.close();
            tvLog.append("Tayyor! Nusxalandi: " + obbName + "\n");
        } catch (Exception e) {
            tvLog.append("Xato: " + e.getMessage() + "\n");
        }
    }
                 }
