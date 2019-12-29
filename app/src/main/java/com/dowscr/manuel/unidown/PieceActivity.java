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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.Objects;

public class PieceActivity extends AppCompatActivity {

    static final String CHANGE = "change";
    static final String NEW = "new";

    private int Id;
    private String type;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piece);

        Bundle b = getIntent().getExtras();
        assert b != null;
        Id = b.getInt("id");
        type = b.getString("type");

        final Button button = findViewById(R.id.pieceok);
        final EditText edittexttitel = findViewById(R.id.edittexttitel);
        final EditText edittexturl = findViewById(R.id.edittexturl);
        final RadioGroup radiogroupmode = findViewById(R.id.radiogroupmode);
        final CheckBox checkboxprote = findViewById(R.id.prote);
        final EditText edittextuser = findViewById(R.id.edittextuser);
        final EditText edittextpass = findViewById(R.id.edittextpass);

        checkboxprote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean on = ((CheckBox) view).isChecked();
                if (!on) {
                    edittextuser.setEnabled(false);
                    edittextpass.setEnabled(false);
                } else {
                    edittextuser.setEnabled(true);
                    edittextpass.setEnabled(true);
                }
            }
        });

        if (Objects.equals(type, CHANGE)) {
            DbHelper dbhelper = new DbHelper(getApplicationContext());
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            String selection = Contract.PieceEntry._ID + " LIKE ?";
            String[] selectionArgs = {String.valueOf(Id)};
            String[] projection = {Contract.PieceEntry.COLUMN_NAME_TITEL, Contract.PieceEntry.COLUMN_NAME_url, Contract.PieceEntry.COLUMN_NAME_MODE, Contract.PieceEntry.COLUMN_NAME_PROTE, Contract.PieceEntry.COLUMN_NAME_USER, Contract.PieceEntry.COLUMN_NAME_PASS};
            Cursor pieceCursor = db.query(
                    Contract.PieceEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    null);
            pieceCursor.moveToNext();

            edittexttitel.setText(pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_TITEL)));
            edittexturl.setText(pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_url)));
            String mode = (pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_MODE)));
            String prote = (pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_PROTE)));
            if (Objects.equals(mode, "0"))
                ((RadioButton) findViewById(R.id.mode0)).setChecked(true);
            else
                ((RadioButton) findViewById(R.id.mode1)).setChecked(true);
            if (Objects.equals(prote, "true")) {
                checkboxprote.setChecked(true);
                edittextuser.setEnabled(true);
                edittextpass.setEnabled(true);
            } else {
                checkboxprote.setChecked(false);
                edittextuser.setEnabled(false);
                edittextpass.setEnabled(false);
            }
            edittextuser.setText(pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_USER)));
            edittextpass.setText(pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_PASS)));
            pieceCursor.close();
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int mode;
                if (radiogroupmode.getCheckedRadioButtonId() == R.id.mode0)
                    mode = 0;
                else
                    mode = 1;
                new UpdateData().execute(edittexttitel.getText().toString(), edittexturl.getText().toString(), String.valueOf(mode), String.valueOf(checkboxprote.isChecked()), edittextuser.getText().toString(), edittextpass.getText().toString());
                finish();
            }
        });
    }

    private class UpdateData extends AsyncTask<String, Void, Void> {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        @Override
        protected Void doInBackground(String... values) {
            DbHelper dbhelper = new DbHelper(getApplicationContext());
            SQLiteDatabase db = dbhelper.getWritableDatabase();
            ContentValues val = new ContentValues();
            val.put(Contract.PieceEntry.COLUMN_NAME_TITEL, values[0]);
            val.put(Contract.PieceEntry.COLUMN_NAME_url, values[1]);
            val.put(Contract.PieceEntry.COLUMN_NAME_MODE, values[2]);
            val.put(Contract.PieceEntry.COLUMN_NAME_PROTE, values[3]);
            val.put(Contract.PieceEntry.COLUMN_NAME_USER, values[4]);
            val.put(Contract.PieceEntry.COLUMN_NAME_PASS, values[5]);
            if (Objects.equals(type, NEW))
                db.insert(Contract.PieceEntry.TABLE_NAME, null, val);
            else {
                String selection = Contract.PieceEntry._ID + " LIKE ?";
                String[] selectionArgs = {String.valueOf(Id)};
                db.update(
                        Contract.PieceEntry.TABLE_NAME,
                        val,
                        selection,
                        selectionArgs);
            }
            return null;
        }
    }
}
