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

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.DOWNLOAD_SERVICE;

/**
 * Created by manuel on 29.10.17.
 */

class Interest {
    private final Context context;

    private final String Vorlesung;
    private final String Typ;
    private final int Mode;
    private final String Holder;
    @SuppressWarnings("FieldCanBeLocal")
    private final int maxId = 20;
    private final int prote;
    private final String encCred;

    Interest(Context context, String Vorlesung, String Typ, String Holder, int Mode, int prote, String encCred) {
        this.context = context;
        this.Vorlesung = Vorlesung;
        this.Typ = Typ;
        this.Holder = Holder;
        this.Mode = Mode;
        this.prote = prote;
        this.encCred = encCred;
    }

    void lookup() {
        //TODO Generalize this
        if (Mode == 0) {
            for (int id = 0; id < maxId; id++) {
                String UrlName = Holder.replaceAll("####", String.format("%02d", id))
                        .replaceAll("%%%%", String.format("%d", id));
                new LookUpTask().execute(UrlName, String.valueOf(id));
            }
        } else if (Mode == 1) {
            String UrlName = Holder;
            new LookUpTask().execute(UrlName, "");
        }
    }

    private class LookUpTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... S) {
            String UrlName = S[0];
            String id = S[1];
            String Title = Vorlesung + Typ + id;
            File Path = new File(Environment.getExternalStorageDirectory() + "/Unidown", Title + ".pdf");
            if (exists(UrlName, Path)) {
                Uri uri = Uri.parse(UrlName);
                try {
                    //noinspection ResultOfMethodCallIgnored
                    Path.getCanonicalFile().delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                MainActivity.addtolistofjobs(download(uri, Title, "", encCred), Title);
            }
            return null;
        }
    }

    private boolean exists(String UrlName, File Path) {
        try {
            HttpURLConnection.setFollowRedirects(false);
            HttpURLConnection con = (HttpURLConnection) new URL(UrlName).openConnection();
            con.setRequestMethod("HEAD");
            if (prote == 1)
                con.setRequestProperty("Authorization", "Basic " + encCred);
            con.setIfModifiedSince(Path.lastModified());
            return con.getResponseCode() == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    private long download(Uri uri, String Title, String Description, String encCred) {

        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        if (prote == 1)
            request.addRequestHeader("Authorization", "Basic " + encCred);

        request.setTitle(Title);
        request.setDescription(Description);

        request.setDestinationInExternalPublicDir("/Unidown", Title + ".pdf");

        assert downloadManager != null;
        return downloadManager.enqueue(request);
    }

}