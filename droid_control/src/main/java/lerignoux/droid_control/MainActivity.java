package lerignoux.droid_control;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RatingBar;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import lerignoux.droid_control.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Nothing linked yet", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        File ssh_dir = getFilesDir();
        savePrivateKey(ssh_dir);
        addListenerOnRatingBar();
    }

    public void addListenerOnRatingBar() {

        RatingBar ratingBar = (RatingBar) findViewById(R.id.ratingBar);

        //if rating value is changed,
        //display the current rating value in the result (textview) automatically
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                String script = "bash control/rating.sh " + String.valueOf(rating);
                scriptExec(script);
            }
        });
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    protected void scriptExec(String script) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String host = sharedPref.getString("host", "192.168.0.1");
        String port = sharedPref.getString("port", "22");
        Log.i("/lerx", "executing " + script + " on " + host);
        Executor task = new Executor(script, sharedPref);
        Thread t = new Thread(task);
        t.start();

        //ConnectionTask().execute(script, host, port);
    }

    public void startScript(View view) {
        // Handle user click
        String script;
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        switch (view.getId()) {
            case R.id.power:
                script = sharedPref.getString("cmd_power", "");
                break;
            case R.id.play:
                script = sharedPref.getString("cmd_play_pause", "");
                break;
            case R.id.stop:
                script = sharedPref.getString("cmd_stop", "");
                break;
            case R.id.next:
                script = sharedPref.getString("cmd_next", "");
                break;
            case R.id.ratingBar:
                script = sharedPref.getString("cmd_rating", "");
                break;
            case R.id.custom0:
                script = sharedPref.getString("script_0", "");
                break;
            case R.id.custom1:
                script = sharedPref.getString("script_1", "");
                break;
            case R.id.custom2:
                script = sharedPref.getString("script_2", "");
                break;
            case R.id.custom3:
                script = sharedPref.getString("script_3", "");
                break;
            case R.id.custom4:
                script = sharedPref.getString("script_4", "");
                break;
            case R.id.custom5:
                script = sharedPref.getString("script_5", "");
                break;
            default:
                script = "";
        }
        scriptExec(script);
        Snackbar.make(view, "Done " + script, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public static class MyUserInfo implements UserInfo {

        private SharedPreferences pref;

        protected void onCreate(SharedPreferences pref) {
            this.pref = pref;
        }
        public String getPassword(){
            return this.pref.getString("password", ""); }
        public boolean promptYesNo(String str){ return false; }
        public String getPassphrase(){
            return this.pref.getString("passphrase", ""); }
        public boolean promptPassphrase(String message){ return false; }
        public boolean promptPassword(String message){ return false; }
        public void showMessage(String message){ }
    }

    protected Void savePrivateKey(File ssh_dir) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String filename = sharedPref.getString("private_key", "id_rsa");
        String private_key="";
        try {
            String abs_filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filename;
            FileInputStream inputStream = new FileInputStream(new File(abs_filename));

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append("\n");
                }

                inputStream.close();
                private_key = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        FileOutputStream outputStream;
        try {
            outputStream = openFileOutput("id_rsa", Context.MODE_PRIVATE);
            outputStream.write(private_key.getBytes());
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class Executor implements Runnable {

        private String script;
        private SharedPreferences sharedPref;

        public Executor(String script, SharedPreferences sharedPref) {
            this.sharedPref = sharedPref;
            this.script = script;
        }

        @Override
        public void run() {
            try {
                String passphrase = sharedPref.getString("passphrase", "");

                JSch jsch = new JSch();

                String host = sharedPref.getString("host", "localhost");
                Integer port = 22;
                String user = host.substring(0, host.indexOf('@'));
                host = host.substring(host.indexOf('@') + 1);

                Session session = jsch.getSession(user, host, port);
                Log.d("/droid_control", "connection to: " + user + "@" + host + ":" + port);
                // username and password will be given via UserInfo interface.
                UserInfo ui = new MyUserInfo();
                String ssh_dir = getFilesDir().getPath();
                jsch.addIdentity(ssh_dir + "/id_rsa", passphrase);
                // jsch.setKnownHosts(ssh_dir + "/known_hosts");

                Log.i("/droid_control", ssh_dir + "/id_rsa");
                session.setUserInfo(ui);
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand(this.script);

                //channel.setInputStream(System.in);
                channel.setInputStream(null);

                //channel.setOutputStream(System.out);

                //FileOutputStream fos=new FileOutputStream("/tmp/stderr");
                //((ChannelExec)channel).setErrStream(fos);
                ((ChannelExec) channel).setErrStream(System.err);

                InputStream in = channel.getInputStream();

                channel.connect();

                byte[] tmp = new byte[1024];
                while (true) {
                    while (in.available() > 0) {
                        int i = in.read(tmp, 0, 1024);
                        if (i < 0) break;
                        System.out.print(new String(tmp, 0, i));
                    }
                    if (channel.isClosed()) {
                        if (in.available() > 0) continue;
                        System.out.println("exit-status: " + channel.getExitStatus());
                        break;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (Exception ee) {
                    }
                }
                channel.disconnect();
                session.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
