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

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

public class SetsActivity extends AppCompatActivity {

    @SuppressWarnings("FieldCanBeLocal")
    private DbHelper dbhelper;
    private SQLiteDatabase db;
    private Cursor pieceCursor;
    private ListView listview;
    private PieceCursorAdapter pieceadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sets);


        dbhelper = new DbHelper(this);
        db = dbhelper.getReadableDatabase();
        String[] projection = {Contract.PieceEntry._ID, Contract.PieceEntry.COLUMN_NAME_TITEL};
        pieceCursor = db.query(
                Contract.PieceEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        listview = findViewById(R.id.list_view_sets);
        pieceadapter = new PieceCursorAdapter(this, pieceCursor);
        listview.setAdapter(pieceadapter);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                int id = pieceCursor.getInt(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry._ID));
                Intent pieceint = new Intent(view.getContext(), PieceActivity.class);
                pieceint.putExtra("id", id);
                pieceint.putExtra("type", PieceActivity.CHANGE);
                view.getContext().startActivity(pieceint);
            }
        });

        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                int id = pieceCursor.getInt(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry._ID));
                Intent pieceint = new Intent(view.getContext(), PieceActivity.class);
                String selection = Contract.PieceEntry._ID + " LIKE ?";
                String[] selectionArgs = {String.valueOf(id)};
                db.delete(Contract.PieceEntry.TABLE_NAME, selection, selectionArgs);
                updatelist();
                return true;
            }
        });

        Button b = findViewById(R.id.neueranker);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pieceint = new Intent(view.getContext(), PieceActivity.class);
                pieceint.putExtra("id", -1);
                pieceint.putExtra("type", PieceActivity.NEW);
                startActivity(pieceint);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        updatelist();
    }

    private void updatelist() {
        String[] projection = {Contract.PieceEntry._ID, Contract.PieceEntry.COLUMN_NAME_TITEL};
        pieceCursor = db.query(
                Contract.PieceEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);
        pieceadapter.changeCursor(pieceCursor);
        pieceadapter.notifyDataSetChanged();
    }

}
