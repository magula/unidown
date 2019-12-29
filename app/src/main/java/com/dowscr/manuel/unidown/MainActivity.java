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

import android.Manifest;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListView listview;
    private BroadcastReceiver downloadReceiver;

    private final Random Rand = new Random();

    private static final Map<Long, String> running_jobs_ids = new HashMap<Long, String>();

    private final String Path = Environment.getExternalStorageDirectory().toString() + "/Unidown";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }


        mSwipeRefreshLayout = findViewById(R.id.swiperefresh);

        mSwipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        mSwipeRefreshLayout.setRefreshing(true);
                        sweep();
                        new UpdateFileList().execute();
                    }
                }
        );

        listview = findViewById(R.id.list_view);

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {
                int toprowVertpos = (listview == null || listview.getChildCount() == 0) ? 0 : listview.getChildAt(0).getTop();
                mSwipeRefreshLayout.setEnabled(i == 0 && toprowVertpos >= 0);
            }
        });


        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        downloadReceiver = new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onReceive(Context context, Intent intent) {
                long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                int NotificationID = Rand.nextInt();
                Notificate(running_jobs_ids.get(referenceId), getString(R.string.newdocument) + " " + running_jobs_ids.get(referenceId), NotificationID);
                running_jobs_ids.remove(referenceId);
                new UpdateFileList().execute();
            }
        };
        registerReceiver(downloadReceiver, filter);

        mSwipeRefreshLayout.setRefreshing(true);
        sweep();
        new UpdateFileList().execute();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
            finish();
        } else {
            mSwipeRefreshLayout.setRefreshing(true);
            sweep();
            new UpdateFileList().execute();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_sets) {
            final Intent intent = new Intent(this, SetsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_impressum) {
            final Intent intent = new Intent(this, ImpressumActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class UpdateFileList extends AsyncTask<Void, Void, Void> {
        File[] files;
        String[] Names;

        protected Void doInBackground(Void... s) {

            final File[] Q = (new File(Path)).listFiles();
            files = Q == null ? new File[0] : Q;
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File t0, File t1) {
                    return Long.compare(t0.lastModified(), t1.lastModified());
                }
            });

            Names = new String[files.length];
            for (int i = 0; i < files.length; i++) Names[i] = files[i].getName();
            return null;
        }

        @Override
        protected void onPostExecute(Void s) {
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.rowlayout, R.id.filename, Names);
            listview.setAdapter(adapter);
            listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    File f = files[i];
                    openfile(f);
                }
            });
            listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                    File f = files[i];
                    intenttosendfile(f);
                    return true;
                }
            });
            if (running_jobs_ids.size() == 0)
                mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    private void intenttosendfile(File f) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        shareIntent.setType("application/pdf");
        startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));
    }

    private Intent intenttoopenfile(File f) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(f), "application/pdf");
        return intent;
    }

    private void openfile(File f) {
        startActivity(intenttoopenfile(f));
    }

    private Intent intenttoprintfile(File f) {
        String PrintAppCode = "dowscr";

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(f));
        intent.setType("application/pdf");
        List<ResolveInfo> resInfo = getPackageManager().queryIntentActivities(intent, 0);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo)
                if (info.activityInfo.packageName.toLowerCase().contains(PrintAppCode) ||
                        info.activityInfo.name.toLowerCase().contains(PrintAppCode)) {
                    intent.setPackage(info.activityInfo.packageName);
                    return intent;
                }
        }
        return null;
    }

    public static void addtolistofjobs(long id, String Title) {
        running_jobs_ids.put(id, Title);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void Notificate(String title, String text, int NotiID) {
        String Path = Environment.getExternalStorageDirectory().toString() + "/Unidown";
        final File f = new File(Path, title + ".pdf");
        Intent intent = intenttoopenfile(f);
        PendingIntent pIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, 0);

        Notification.Builder notibuilder =
                new Notification.Builder(this)
                        .setContentTitle(title)
                        .setSmallIcon(android.R.drawable.stat_sys_download_done)
                        .setContentText(text)
                        .setContentIntent(pIntent);

        Intent jntent = intenttoprintfile(f);
        if (jntent != null) {
            PendingIntent qIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), jntent, 0);
            try {
                Resources res = getPackageManager().getResourcesForApplication("com.dowscr.manuel.dowscr");
                notibuilder.addAction(R.drawable.ic_white, "Print", qIntent);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }

        NotificationManager NotificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        assert NotificationManager != null;
        NotificationManager.notify(NotiID, notibuilder.build());
    }

    @Override
    protected void onDestroy() {
        try {
            unregisterReceiver(downloadReceiver);
        } catch (Exception e) {
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSwipeRefreshLayout.setRefreshing(true);
        sweep();
        new UpdateFileList().execute();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void sweep() {
        DbHelper dbhelper = new DbHelper(getApplicationContext());
        SQLiteDatabase db = dbhelper.getReadableDatabase();
        String[] projection = {Contract.PieceEntry.COLUMN_NAME_TITEL, Contract.PieceEntry.COLUMN_NAME_url, Contract.PieceEntry.COLUMN_NAME_MODE, Contract.PieceEntry.COLUMN_NAME_PROTE, Contract.PieceEntry.COLUMN_NAME_USER, Contract.PieceEntry.COLUMN_NAME_PASS};
        Cursor pieceCursor = db.query(
                Contract.PieceEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null);

        while (pieceCursor.moveToNext()) {
            String title = pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_TITEL));
            String url = pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_url));
            int mode = pieceCursor.getInt(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_MODE));
            int prote = Objects.equals(pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_PROTE)), "true") ? 1 : 0;
            String user = pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_USER));
            String pass = pieceCursor.getString(pieceCursor.getColumnIndexOrThrow(Contract.PieceEntry.COLUMN_NAME_PASS));
            String encCred = Base64.encodeToString((user + ":" + pass).getBytes(), Base64.DEFAULT);
            new Interest(MainActivity.this, title, "", url, mode, prote, encCred).lookup();
        }
        pieceCursor.close();
    }
}
