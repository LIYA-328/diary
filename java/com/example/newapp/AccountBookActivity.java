package com.example.newapp;


import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AccountBookActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private SimpleCursorAdapter adapter;
    private String username;

    // UI组件
    private Button btnAddRecord;
    private ListView listView;
    private TextView tvTotalBalance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_book);

        // 获取用户名
        username = getIntent().getStringExtra("USERNAME");
        if (username == null || username.isEmpty()) {
            Toast.makeText(this, "用户信息错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化数据库
        dbHelper = new DatabaseHelper(this);

        // 初始化视图
        btnAddRecord = findViewById(R.id.btnAddRecord);
        listView = findViewById(R.id.listViewRecords);
        tvTotalBalance = findViewById(R.id.tvTotalBalance);

        // 设置添加记录按钮事件
        btnAddRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddRecordDialog();
            }
        });

        // 加载记账数据
        loadRecords();

        // 更新总余额
        updateTotalBalance();

        // 设置列表项长按删除事件
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showDeleteDialog(id);
                return true;
            }
        });
    }

    private void loadRecords() {
        Cursor cursor = dbHelper.getAllRecords(username);

        String[] from = new String[] {
                DatabaseHelper.COLUMN_RECORD_TYPE,
                DatabaseHelper.COLUMN_RECORD_CATEGORY,
                DatabaseHelper.COLUMN_RECORD_AMOUNT,
                DatabaseHelper.COLUMN_RECORD_DATE
        };

        int[] to = new int[] {
                R.id.tvRecordType,
                R.id.tvRecordCategory,
                R.id.tvRecordAmount,
                R.id.tvRecordDate
        };

        adapter = new SimpleCursorAdapter(
                this,
                R.layout.account_item,
                cursor,
                from,
                to,
                0
        );

        listView.setAdapter(adapter);
    }

    private void updateTotalBalance() {
        double balance = dbHelper.getAccountBalance(username);
        tvTotalBalance.setText(String.format("总余额: ¥%.2f", balance));

        // 根据余额正负设置不同颜色
        if (balance < 0) {
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvTotalBalance.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }

    private void showAddRecordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_record, null);
        builder.setView(dialogView);
        builder.setTitle("添加记账记录");

        // 初始化对话框中的组件
        Spinner spinnerType = dialogView.findViewById(R.id.spinnerType);
        Spinner spinnerCategory = dialogView.findViewById(R.id.spinnerCategory);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        EditText etDescription = dialogView.findViewById(R.id.etDescription);

        builder.setPositiveButton("保存", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String type = spinnerType.getSelectedItem().toString();
                String category = spinnerCategory.getSelectedItem().toString();
                String amountStr = etAmount.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

                if (amountStr.isEmpty()) {
                    Toast.makeText(AccountBookActivity.this, "请输入金额", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    double amount = Double.parseDouble(amountStr);
                    if (type.equals("支出")) {
                        amount = -amount; // 支出为负数
                    }

                    // 获取当前日期
                    String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                    // 添加记录到数据库
                    if (dbHelper.addRecord(username, type, category, amount, description, date)) {
                        Toast.makeText(AccountBookActivity.this, "记录添加成功", Toast.LENGTH_SHORT).show();
                        loadRecords();
                        updateTotalBalance();
                    } else {
                        Toast.makeText(AccountBookActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(AccountBookActivity.this, "金额格式错误", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("取消", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showDeleteDialog(final long id) {
        new AlertDialog.Builder(this)
                .setTitle("删除记录")
                .setMessage("确定要删除这条记账记录吗？")
                .setPositiveButton("删除", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dbHelper.deleteRecord(id)) {
                            Toast.makeText(AccountBookActivity.this, "记录已删除", Toast.LENGTH_SHORT).show();
                            loadRecords();
                            updateTotalBalance();
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecords();
        updateTotalBalance();
    }
}
