package lerignoux.droid_control;


import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class ServersSettingsActivity extends AppCompatActivity {
    public static final String SRVPrefKey = "ServerPreferences" ;
    final int ACTIVITY_CHOOSE_FILE = 1;
    final Context context = this;
    private ServersAdapter serverListAdapter;
    private ListView serverListView;
    private ArrayList<Server> serverList = new ArrayList<Server>();


    public class ServersAdapter extends ArrayAdapter<Server> {
        ServersAdapter(Context context, ArrayList<Server> servers) {
            super(context, 0, servers);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            Server server = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.server_list_item, parent, false);
            }
            // Lookup view for data population
            TextView name = (TextView) convertView.findViewById(R.id.srv_name);
            TextView address = (TextView) convertView.findViewById(R.id.srv_address);
            TextView port = (TextView) convertView.findViewById(R.id.srv_port);
            TextView username = (TextView) convertView.findViewById(R.id.srv_username);
            // Populate the data into the template view using the data object
            name.setText(server.name);
            address.setText(server.address);
            port.setText(String.valueOf(server.port));
            username.setText(String.valueOf(server.username));
            // Return the completed view to render on screen

            ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.srv_delete);
            deleteButton.setTag(position);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = (Integer) view.getTag();
                    // Access the row position here to get the correct data item
                    Server server = getItem(position);
                    serverList.remove(position);
                    serverListAdapter.remove(server);
                    saveServerList();
                    // Do what you want here...
                }
            });

            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers_settings);
        setupActionBar();
        setupPrivateKeyFinder();
        setupPassphrase();
        setupServerList();
        setupAddServerFab();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupPrivateKeyFinder() {

        TextView pkview = (TextView) this.findViewById(R.id.privateKeyName);
        SharedPreferences srvPref = getSharedPreferences(SRVPrefKey, MODE_PRIVATE);
        pkview.setText(srvPref.getString("private_key_file", "No private key set"));

        Button btn = (Button) this.findViewById(R.id.FindPrivKeyBtn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent chooseFile;
                Intent intent;
                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
                chooseFile.setType("file/*");
                intent = Intent.createChooser(chooseFile, "Load private key");
                startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);
            }
        });
    }

    public void setupPassphrase() {
        final SharedPreferences srvPref = getSharedPreferences(SRVPrefKey, MODE_PRIVATE);
        final EditText passphraseInput = (EditText) findViewById(R.id.privKeyPassphrase);
        passphraseInput.setText(srvPref.getString("PrivateKeyPassphrase", "No passphrase"));
        passphraseInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                // We save the passphrase
                srvPref.edit().putString("PrivateKeyPassphrase", passphraseInput.getText().toString()).commit();
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

        });
    }

    public void setupServerList() {
        // Create the adapter to convert the array to views
        serverListAdapter = new ServersAdapter(this, serverList);
        // Attach the adapter to a ListView
        serverListView = (ListView) findViewById(R.id.ServerList);
        serverListView.setAdapter(serverListAdapter);
        loadServerList();
        serverListAdapter.addAll(serverList);
    }

    public void setupAddServerFab() {
        FloatingActionButton button = (FloatingActionButton) findViewById(R.id.addServerButton);

        // add button listener
        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {

                // get prompts.xml view
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.popup_add_server, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText serverNameInput = (EditText) promptsView
                        .findViewById(R.id.addServerName);
                final EditText serverAddressInput = (EditText) promptsView
                        .findViewById(R.id.addServerAddress);
                final EditText serverPortInput = (EditText) promptsView
                        .findViewById(R.id.addServerPort);
                final EditText serverUsernameInput = (EditText) promptsView
                        .findViewById(R.id.addServerUsername);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("Add",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // get user input and set it to result
                                    // edit text
                                    serverList.add(new Server(
                                            serverNameInput.getText().toString(),
                                            serverAddressInput.getText().toString(),
                                            Integer.parseInt(serverPortInput.getText().toString()),
                                            serverUsernameInput.getText().toString())
                                    );
                                    serverListAdapter.add(new Server(
                                            serverNameInput.getText().toString(),
                                            serverAddressInput.getText().toString(),
                                            Integer.parseInt(serverPortInput.getText().toString()),
                                            serverUsernameInput.getText().toString())
                                    );
                                    saveServerList();
                                    loadServerList();
                                }
                        })
                        .setNegativeButton("Cancel",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    dialog.cancel();
                                }
                        });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

            }
        });
    }

    private void loadServerList() {
        SharedPreferences mPrefs = getSharedPreferences(SRVPrefKey, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = mPrefs.getString("ServerList", "[]");
        Log.v("DEBUG", "Server list: " + json);
        serverList = gson.fromJson(json, new TypeToken<ArrayList<Server>>() {}.getType());
    }

    private void saveServerList() {
        SharedPreferences sharedPref = getSharedPreferences(SRVPrefKey, MODE_PRIVATE);
        Gson gson = new Gson();
        String serverListString = gson.toJson(serverList);
        Log.v("DEBUG", "Saved server list: " + serverListString);
        sharedPref.edit().putString("ServerList", serverListString).commit();

        String json = sharedPref.getString("ServerList", "[]");
        Log.v("DEBUG", "Read server list: " + json);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ACTIVITY_CHOOSE_FILE: {
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    String filePath = uri.getPath();

                    SharedPreferences srvPref = getSharedPreferences(SRVPrefKey, MODE_PRIVATE);
                    srvPref.edit().putString("private_key_file", filePath).apply();

                    // We update the key name
                    TextView pkview = (TextView) this.findViewById(R.id.privateKeyName);
                    pkview.setText(srvPref.getString("private_key_file", "No private key set"));
                }
            }
        }
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(
                        index >= 0
                                ? listPreference.getEntries()[index]
                                : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ServersPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ServersPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            bindPreferenceSummaryToValue(findPreference("srv_1"));
            bindPreferenceSummaryToValue(findPreference("srv_2"));
            bindPreferenceSummaryToValue(findPreference("srv_3"));
            bindPreferenceSummaryToValue(findPreference("srv_4"));
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), ServersSettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }
    }
}
