package com.example.newapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String DATABASE_NAME = "DiaryApp.db";
    private static final int DATABASE_VERSION = 1;

    // 用户表
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "user_id";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    // 笔记表
    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_NOTE_ID = "note_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_USER = "user";

    // 记账表结构
    public static final String TABLE_RECORDS = "records";
    public static final String COLUMN_RECORD_ID = "record_id";
    public static final String COLUMN_RECORD_TYPE = "type"; // 收入/支出
    public static final String COLUMN_RECORD_CATEGORY = "category";
    public static final String COLUMN_RECORD_AMOUNT = "amount";
    public static final String COLUMN_RECORD_DESCRIPTION = "description";
    public static final String COLUMN_RECORD_DATE = "date";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建用户表
        String createUserTable = "CREATE TABLE " + TABLE_USERS + "("
                + COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT UNIQUE, "
                + COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createUserTable);

        // 创建笔记表
        String createNoteTable = "CREATE TABLE " + TABLE_NOTES + "("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_CONTENT + " TEXT, "
                + COLUMN_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP, "
                + COLUMN_USER + " TEXT)";
        db.execSQL(createNoteTable);

        // 创建记账表
        String createRecordsTable = "CREATE TABLE " + TABLE_RECORDS + "("
                + COLUMN_RECORD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER + " TEXT, "
                + COLUMN_RECORD_TYPE + " TEXT, "
                + COLUMN_RECORD_CATEGORY + " TEXT, "
                + COLUMN_RECORD_AMOUNT + " REAL, " // REAL用于存储浮点数
                + COLUMN_RECORD_DESCRIPTION + " TEXT, "
                + COLUMN_RECORD_DATE + " DATETIME DEFAULT CURRENT_TIMESTAMP)";
        db.execSQL(createRecordsTable);

        Log.d(TAG, "Database tables created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECORDS);
        onCreate(db);
    }

    // 添加新用户
    public boolean addUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    // 检查用户名是否存在
    public boolean checkUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username},
                null, null, null);

        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // 验证用户登录
    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USER_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null, null, null);

        int count = cursor.getCount();
        cursor.close();
        return count > 0;
    }

    // 添加记账记录
    public boolean addRecord(String username, String type, String category, double amount,
                             String description, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER, username);
        values.put(COLUMN_RECORD_TYPE, type);
        values.put(COLUMN_RECORD_CATEGORY, category);
        values.put(COLUMN_RECORD_AMOUNT, amount);
        values.put(COLUMN_RECORD_DESCRIPTION, description);
        values.put(COLUMN_RECORD_DATE, date);

        long result = db.insert(TABLE_RECORDS, null, values);
        return result != -1;
    }

    // 获取所有记账记录
    public Cursor getAllRecords(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " +
                COLUMN_RECORD_ID + " AS _id, " +
                COLUMN_RECORD_TYPE + ", " +
                COLUMN_RECORD_CATEGORY + ", " +
                COLUMN_RECORD_AMOUNT + ", " +
                COLUMN_RECORD_DESCRIPTION + ", " +
                COLUMN_RECORD_DATE +
                " FROM " + TABLE_RECORDS +
                " WHERE " + COLUMN_USER + "=?" +
                " ORDER BY " + COLUMN_RECORD_DATE + " DESC";

        return db.rawQuery(query, new String[]{username});
    }

    // 删除记账记录
    public boolean deleteRecord(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_RECORDS, COLUMN_RECORD_ID + "=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }

    // 计算账户总余额
    public double getAccountBalance(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(" + COLUMN_RECORD_AMOUNT + ") FROM " + TABLE_RECORDS +
                        " WHERE " + COLUMN_USER + "=?",
                new String[]{username}
        );

        double balance = 0;
        if (cursor != null && cursor.moveToFirst()) {
            balance = cursor.getDouble(0);
            cursor.close();
        }
        return balance;
    }

    // 添加新笔记
    public long addNote(String username, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER, username);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);

        return db.insert(TABLE_NOTES, null, values);
    }

    // 获取所有笔记 - 关键修改
    public Cursor getAllNotes(String username) {
        SQLiteDatabase db = this.getReadableDatabase();

        // 使用别名 _id 满足 SimpleCursorAdapter 的要求
        String query = "SELECT " +
                COLUMN_NOTE_ID + " AS _id, " +  // 将 note_id 别名为 _id
                COLUMN_TITLE + ", " +
                COLUMN_DATE +
                " FROM " + TABLE_NOTES +
                " WHERE " + COLUMN_USER + "=?" +
                " ORDER BY " + COLUMN_DATE + " DESC";

        Log.d(TAG, "Executing query: " + query);
        return db.rawQuery(query, new String[]{username});
    }

    // 获取单个笔记
    public Note getNote(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // 使用原始查询确保获取所有需要的字段
            String query = "SELECT " +
                    COLUMN_TITLE + ", " +
                    COLUMN_CONTENT +
                    " FROM " + TABLE_NOTES +
                    " WHERE " + COLUMN_NOTE_ID + "=?";

            cursor = db.rawQuery(query, new String[]{String.valueOf(id)});

            if (cursor != null && cursor.moveToFirst()) {
                // 使用列名获取值
                int titleIndex = cursor.getColumnIndex(COLUMN_TITLE);
                int contentIndex = cursor.getColumnIndex(COLUMN_CONTENT);

                if (titleIndex >= 0 && contentIndex >= 0) {
                    String title = cursor.getString(titleIndex);
                    String content = cursor.getString(contentIndex);
                    return new Note(title, content);
                } else {
                    Log.e("DatabaseHelper", "列索引无效: titleIndex=" + titleIndex + ", contentIndex=" + contentIndex);
                }
            }
            return null;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "获取笔记失败", e);
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // 更新笔记
    public boolean updateNote(long id, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_CONTENT, content);

        int result = db.update(TABLE_NOTES, values, COLUMN_NOTE_ID + "=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }

    // 删除笔记
    public boolean deleteNote(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_NOTES, COLUMN_NOTE_ID + "=?",
                new String[]{String.valueOf(id)});
        return result > 0;
    }
}