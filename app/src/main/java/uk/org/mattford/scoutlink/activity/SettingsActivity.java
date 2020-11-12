package uk.org.mattford.scoutlink.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import java.util.ArrayList;

import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.databinding.ActivitySettingsBinding;
import uk.org.mattford.scoutlink.irc.IRCService;
import uk.org.mattford.scoutlink.model.Settings;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private Settings settings;

    private final int AUTOJOIN_REQUEST_CODE = 0;
    private final int CONNECT_COMMANDS_REQUEST_CODE = 1;
    private final int NOTIFY_LIST_REQUEST_CODE = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        settings = new Settings(this);

        binding.settingsNickname.setText(settings.getString("nickname", ""));
        binding.settingsIdent.setText(settings.getString("ident", "androidirc"));
        binding.settingsGecos.setText(settings.getString("gecos", "ScoutLink IRC for Android!"));
        binding.settingsNickservUser.setText(settings.getString("nickserv_user", ""));
        binding.settingsNickservPassword.setText(settings.getString("nickserv_password", ""));
        binding.settingsQuitMessage.setText(settings.getString("quit_message", getString(R.string.default_quit_message)));

        binding.settingsEnableLogging.setChecked(settings.getBoolean("logging_enabled", true));
        binding.settingsLoadPreviousMessagesOnJoin.setChecked(settings.getBoolean("load_previous_messages_on_join", true));
        binding.settingsVibrateOnMention.setChecked(settings.getBoolean("vibrate_on_mention", true));
    }

    @Override
    public void onPause() {
        super.onPause();
        settings.putString("nickname", binding.settingsNickname.getText().toString());
        settings.putString("ident", binding.settingsIdent.getText().toString());
        settings.putString("gecos", binding.settingsGecos.getText().toString());
        settings.putString("nickserv_user", binding.settingsNickservUser.getText().toString());
        settings.putString("nickserv_password", binding.settingsNickservPassword.getText().toString());
        settings.putString("quit_message", binding.settingsQuitMessage.getText().toString());

        settings.putBoolean("logging_enabled", binding.settingsEnableLogging.isChecked());
        settings.putBoolean("load_previous_messages_on_join", binding.settingsLoadPreviousMessagesOnJoin.isChecked());
        settings.putBoolean("vibrate_on_mention", binding.settingsVibrateOnMention.isChecked());
    }

    public void openAutojoinSettings(View v) {
        Intent intent = new Intent(this, ListEditActivity.class);

        intent.putExtra("title", getString(R.string.settings_autojoin_channels_label));
        intent.putExtra("firstChar", "#");
        intent.putStringArrayListExtra("items", settings.getStringArrayList("autojoin_channels"));
        startActivityForResult(intent, AUTOJOIN_REQUEST_CODE);
    }

    public void openCommandOnConnectSettings(View v) {
        Intent intent = new Intent(this, ListEditActivity.class);

        intent.putStringArrayListExtra("items", settings.getStringArrayList("command_on_connect"));
        intent.putExtra("title", getString(R.string.settings_command_on_connect_label));
        intent.putExtra("firstChar", "/");
        startActivityForResult(intent, CONNECT_COMMANDS_REQUEST_CODE);
    }

    public void openNotifyListSettings(View v) {
        Intent intent = new Intent(this, ListEditActivity.class);

        intent.putStringArrayListExtra("items", settings.getStringArrayList("notify_list"));
        intent.putExtra("title", getString(R.string.settings_notify_list_label));
        intent.putExtra("firstChar", "");
        startActivityForResult(intent, NOTIFY_LIST_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case AUTOJOIN_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    settings.putStringArrayList("autojoin_channels", data.getStringArrayListExtra("items"));
                }
                break;
            case CONNECT_COMMANDS_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    settings.putStringArrayList("command_on_connect", data.getStringArrayListExtra("items"));
                }
                break;
            case NOTIFY_LIST_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    settings.putStringArrayList("notify_list", data.getStringArrayListExtra("items"));
                    ArrayList<String> newItems = data.getStringArrayListExtra("newItems");
                    ArrayList<String> removedItems = data.getStringArrayListExtra("removedItems");
                    Intent addNotify = new Intent(this, IRCService.class);
                    addNotify.setAction(IRCService.ACTION_ADD_NOTIFY);
                    addNotify.putStringArrayListExtra("items", newItems);
                    startService(addNotify);
                    Intent removeNotify = new Intent(this, IRCService.class);
                    removeNotify.setAction(IRCService.ACTION_REMOVE_NOTIFY);
                    removeNotify.putStringArrayListExtra("items", removedItems);
                    startService(removeNotify);
                }
                break;
        }
    }
}
