package com.example.newapp;

// NoteEditActivity.java


import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class NoteEditActivity extends AppCompatActivity {

    private static final String TAG = null;
    private EditText etTitle, etContent;
    private Button btnSave;
    private DatabaseHelper dbHelper;
    private String username;
    private long noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_edit);

        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("USERNAME");

        etTitle = findViewById(R.id.etTitle);
        etContent = findViewById(R.id.etContent);
        btnSave = findViewById(R.id.btnSave);

        // 检查是否编辑现有笔记
        if (getIntent().hasExtra("NOTE_ID")) {
            noteId = getIntent().getLongExtra("NOTE_ID", -1);
            loadNotes();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });
    }

    private void loadNotes() {

            Cursor cursor = dbHelper.getAllNotes(username);

            // 列名保持不变
            String[] from = new String[] {
                    DatabaseHelper.COLUMN_TITLE,
                    DatabaseHelper.COLUMN_DATE
            };

            int[] to = new int[] {
                    R.id.tvNoteTitle,
                    R.id.tvNoteDate
            };

            // 现在 Cursor 包含 _id 列，适配器可以正常工作
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(
                    this,
                    R.layout.note_item,
                    cursor,
                    from,
                    to,
                    0
            );



    }

    private void saveNote() {
        String title = etTitle.getText().toString().trim();
        String content = etContent.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
            return;
        }

        if (noteId == -1) {
            // 新建笔记
            long newId = dbHelper.addNote(username, title, content);
            if (newId != -1) {
                Toast.makeText(this, "笔记保存成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
            }
        } else {
            // 更新现有笔记
            if (dbHelper.updateNote(noteId, title, content)) {
                Toast.makeText(this, "笔记更新成功", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
