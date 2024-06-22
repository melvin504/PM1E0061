package com.example.pm2e10471.Configuracion;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteConnection extends SQLiteOpenHelper
{
    public SQLiteConnection(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(Trans.CreateTableContactos);
        sqLiteDatabase.execSQL(Trans.CreateTablePais);
        sqLiteDatabase.execSQL(Trans.InsertPaises);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(Trans.DropTableContactos);
        sqLiteDatabase.execSQL(Trans.DropTablePais);
        onCreate(sqLiteDatabase);
    }
}
