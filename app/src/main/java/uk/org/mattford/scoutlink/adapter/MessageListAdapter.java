package uk.org.mattford.scoutlink.adapter;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import androidx.room.Room;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.database.LogDatabase;
import uk.org.mattford.scoutlink.database.entities.LogMessage;
import uk.org.mattford.scoutlink.database.migrations.LogDatabaseMigrations;
import uk.org.mattford.scoutlink.model.Conversation;
import uk.org.mattford.scoutlink.model.Message;
import uk.org.mattford.scoutlink.model.Settings;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageListAdapter extends ArrayAdapter<LinearLayout> {

    private ArrayList<LinearLayout> messages;
    private ArrayList<Message> buffer;
    private boolean initialised = false;
    private Context context;
    private Message previousMessage;
    private Conversation conversation;
    MessageListAdapter(Context context, Conversation conv) {
        super(context, 0);

        this.context = context;
        this.messages = new ArrayList<>();
        this.buffer = new ArrayList<>();
        this.conversation = conv;

        LinkedList<Message> messagesSnapshot = new LinkedList<>(conv.getMessages());
        loadLoggedMessages(messagesSnapshot);
    }

    public MessageListAdapter(Context context, List<Message> messages) {
        super(context, 0);

        this.context = context;
        this.messages = new ArrayList<>();

        loadLoggedMessages(messages);
    }

    private void loadLoggedMessages(List<Message> messages) {
        Settings settings = new Settings(context);
        if (conversation == null ||
                conversation.getType() == Conversation.TYPE_SERVER ||
                !settings.getBoolean("logging_enabled", true) ||
                !settings.getBoolean("load_previous_messages_on_join", true)
        ) {
            for (Message msg : messages) {
                addMessage(msg);
            }
            initialised = true;
            processBuffer();
            return;
        }

        int messagesToLoad = settings.getInteger("previous_messages_to_load", 10);

        LogDatabase logDatabase = Room.databaseBuilder(context.getApplicationContext(), LogDatabase.class, "logs")
                .addMigrations(LogDatabaseMigrations.MIGRATION_0_1)
                .build();

        new Thread(() -> {
            List<LogMessage> logMessages = logDatabase.logMessageDao().findConversationMessagesWithLimit(conversation.getName(), messagesToLoad);
            logDatabase.close();
            (new Handler(Looper.getMainLooper())).post(() -> {
                for (int i = logMessages.size() - 1; i >= 0; i--) {
                    LogMessage msg = logMessages.get(i);
                    Message message = new Message(msg.sender, msg.message, msg.date, null);
                    addMessage(message);
                }
                if (!messages.isEmpty()) {
                    for (Message msg : messages) {
                        addMessage(msg);
                    }
                }
                initialised = true;
                processBuffer();
            });
        }).start();
    }

    private void processBuffer() {
        while (buffer.size() > 0) {
            Message msg = buffer.get(0);
            buffer.remove(0);
            processMessage(msg);
        }
    }

    public void addMessage(Message message) {
        buffer.add(message);
        if (initialised) {
            processBuffer();
        }
    }

    private void processMessage(Message message) {
        if (previousMessage == null || (
                previousMessage.getTimestamp() != null &&
                        message.getTimestamp() != null &&
                        message.getTimestamp().getDay() != previousMessage.getTimestamp().getDay()
        )
        ) {
            LayoutInflater li = LayoutInflater.from(context);
            LinearLayout dateDivider = (LinearLayout)li.inflate(R.layout.message_list_date_divider, null);
            TextView dateString = dateDivider.findViewById(R.id.date);
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(context);
            String formattedDate = dateFormat.format(message.getTimestamp());
            dateString.setText(formattedDate);
            messages.add(dateDivider);
        }

        LinearLayout msgView = message.renderTextView(context);
        if (!this.showMessageMetadata(message, previousMessage)) {
            msgView.findViewById(R.id.message_metadata).setVisibility(View.GONE);
        }
        messages.add(msgView);

        previousMessage = message;
        notifyDataSetChanged();
    }

    private boolean showMessageMetadata(Message message, Message previousMessage) {
        if (previousMessage == null ||
                (message.getSender() != null &&
                        (previousMessage.getSender() == null ||
                                !previousMessage.getSender().equalsIgnoreCase(message.getSender()))) ||
                (message.getTimestamp() != null && previousMessage.getTimestamp() == null)
        ) {
            return true;
        }

        // If more that 30 mins has passed, show the meta regardless
        Date previousTimestamp = previousMessage.getTimestamp();
        Date currentTimestamp = message.getTimestamp();
        return (currentTimestamp.getTime() - previousTimestamp.getTime() > (1000 * 60 * 30));
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public LinearLayout getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position);
    }
}
