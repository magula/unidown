/*
 * MIT License
 *
 * Copyright (c) 2019 Manuel Gundlach <manuel.gundlach@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.dowscr.manuel.unidown;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by manuel on 02.11.17.
 */

class DbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Unidown.db";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + Contract.PieceEntry.TABLE_NAME + " (" +
                    Contract.PieceEntry._ID + " INTEGER PRIMARY KEY," +
                    Contract.PieceEntry.COLUMN_NAME_TITEL + " TEXT," +
                    Contract.PieceEntry.COLUMN_NAME_MODE + " TEXT," +
                    Contract.PieceEntry.COLUMN_NAME_url + " TEXT," +
                    Contract.PieceEntry.COLUMN_NAME_PROTE + " TEXT," +
                    Contract.PieceEntry.COLUMN_NAME_USER + " TEXT," +
                    Contract.PieceEntry.COLUMN_NAME_PASS + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + Contract.PieceEntry.TABLE_NAME;

    DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        if (i1 == 2 && i == 1) {
            db.execSQL("ALTER TABLE " + Contract.PieceEntry.TABLE_NAME + " ADD COLUMN " + Contract.PieceEntry.COLUMN_NAME_PROTE + " TEXT DEFAULT 'false'");
            db.execSQL("ALTER TABLE " + Contract.PieceEntry.TABLE_NAME + " ADD COLUMN " + Contract.PieceEntry.COLUMN_NAME_USER + " TEXT;");
            db.execSQL("ALTER TABLE " + Contract.PieceEntry.TABLE_NAME + " ADD COLUMN " + Contract.PieceEntry.COLUMN_NAME_PASS + " TEXT;");
        } else {
            db.execSQL(SQL_DELETE_ENTRIES);
            onCreate(db);
        }
    }
}
