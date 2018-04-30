package science.credo.credomobiledetektor.info;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.ContextThemeWrapper;

import org.apache.http.util.ByteArrayBuffer;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import science.credo.credomobiledetektor.BuildConfig;
import science.credo.credomobiledetektor.R;

public class VersionInfo extends AsyncTask<Void, Void, Boolean> {

    private int latestVersion;
    private Context context;

    public VersionInfo(Context context){
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Void... b) {
        try {
            // Url to lowest acceptable version
            URL updateURL = new URL("http://wklej.org/id/3429348/txt/");

            //Read from URL
            URLConnection conn = updateURL.openConnection();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayBuffer baf = new ByteArrayBuffer(50);

            int current = 0;
            while ((current = bis.read()) != -1) {
                baf.append((byte) current);
            }

            // Comvert Bytes to String
            final String s = new String(baf.toByteArray());

            // Convert String to Integer
            int newVersion = Integer.valueOf(s);

            // Set lowest acceptable version to latestVersion variable
            setVersion(newVersion);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        // Current version
        int curVersion = BuildConfig.VERSION_CODE;

        if(!result) {
            // Compare current with minimal acceptable version
            if (curVersion < latestVersion) {
                showUpdateDialog();
            }
            else{}

        }
    }

    /* Create and show update dialog */
    public void showUpdateDialog(){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                R.style.Theme_AppCompat_DayNight_Dialog));

        alertDialogBuilder.setTitle(context.getString(R.string.updateTitle));
        alertDialogBuilder.setMessage(context.getString(R.string.updateMessage) + " " + latestVersion + context.getString(R.string.updateMeggage1));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setPositiveButton(R.string.update, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Url to Google Play
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=science.credo.credomobiledetektor&hl=pl")));
                dialog.cancel();
            }
        });
        alertDialogBuilder.show();
    }

    /* Set lowest acceptable version to latestVersion variable */
    public void setVersion(int version){
        latestVersion = version;
    }
}

