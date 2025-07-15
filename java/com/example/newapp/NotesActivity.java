package com.example.newapp;
// NotesActivity.java

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class NotesActivity extends AppCompatActivity {

    private ListView listView;
    private ImageButton btnAdd;
    private EditText etSearch;
    private DatabaseHelper dbHelper;
    private SimpleCursorAdapter adapter;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        username = getIntent().getStringExtra("USERNAME");
        if (username == null) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        dbHelper = new DatabaseHelper(this);

        listView = findViewById(R.id.listView);
        btnAdd = findViewById(R.id.btnAdd);
        etSearch = findViewById(R.id.etSearch);

        // 设置空视图
        TextView emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);

        // 添加新笔记按钮
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(NotesActivity.this, NoteEditActivity.class);
                intent.putExtra("USERNAME", username);
                startActivity(intent);
            }
        });

        // 加载笔记列表
        loadNotes();

        // 列表项点击事件
        // NotesActivity.java
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取正确的笔记ID
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    // 从Cursor中直接获取_id值
                    long noteId = cursor.getLong(cursor.getColumnIndexOrThrow("_id"));
                    Log.d("NotesActivity", "点击笔记ID: " + noteId);

                    Intent intent = new Intent(NotesActivity.this, NoteEditActivity.class);
                    intent.putExtra("NOTE_ID", noteId);
                    intent.putExtra("USERNAME", username);
                    startActivity(intent);
                } else {
                    Toast.makeText(NotesActivity.this, "无法获取笔记信息", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // 列表项长按事件（删除）
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(id);
                return true;
            }
        });
    }

    private void loadNotes() {
        Cursor cursor = dbHelper.getAllNotes(username);

        String[] from = new String[] { DatabaseHelper.COLUMN_TITLE, DatabaseHelper.COLUMN_DATE };
        int[] to = new int[] { R.id.tvNoteTitle, R.id.tvNoteDate };

        adapter = new SimpleCursorAdapter(this, R.layout.note_item, cursor, from, to, 0);
        listView.setAdapter(adapter);
    }

    private void showDeleteDialog(final long id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除笔记");
        builder.setMessage("确定要删除这条笔记吗？");
        builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dbHelper.deleteNote(id);
                loadNotes(); // 刷新列表
            }
        });
        builder.setNegativeButton("否", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(); // 每次返回时刷新列表
    }
}