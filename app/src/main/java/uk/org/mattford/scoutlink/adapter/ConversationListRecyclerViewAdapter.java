package uk.org.mattford.scoutlink.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import uk.org.mattford.scoutlink.R;
import uk.org.mattford.scoutlink.fragment.ConversationListFragment;
import uk.org.mattford.scoutlink.model.Conversation;

public class ConversationListRecyclerViewAdapter extends RecyclerView.Adapter<ConversationListRecyclerViewAdapter.ViewHolder> {

    private final ConversationListFragment mListener;
    private ArrayList<Conversation> conversationList;
    private Conversation activeConversation;

    public ConversationListRecyclerViewAdapter(
            ConversationListFragment listener
    ) {
        mListener = listener;
        conversationList = new ArrayList<>();
        activeConversation = null;
    }

    public void setConversationList(ArrayList<Conversation> conversationList) {
        this.conversationList = conversationList;
        notifyDataSetChanged();
    }

    public void setActiveConversation(Conversation activeConversation) {
        this.activeConversation = activeConversation;
        notifyDataSetChanged();
    }

    public void notifyConversationChanged(Conversation conversation) {
        int idx = conversationList.indexOf(conversation);
        if (idx > -1) {
            notifyItemChanged(idx);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.conversation = conversationList.get(position);
        holder.mConversationNameView.setText(holder.conversation.getName());

        if (holder.conversation.getUnreadMessagesCount().getValue() != null) {
            int unreadMessagesCount = holder.conversation.getUnreadMessagesCount().getValue();
            holder.mUnreadMessagesView.setText(Integer.toString(unreadMessagesCount));
            holder.mUnreadMessagesView.setVisibility(unreadMessagesCount == 0 ? View.GONE : View.VISIBLE);
        } else {
            holder.mUnreadMessagesView.setVisibility(View.GONE);
        }
        holder.mConversationNameView
                .setTextColor(
                        holder.mConversationNameView
                                .getContext()
                                .getResources()
                                .getColor(holder.conversation == this.activeConversation ? R.color.scoutlink_orange : R.color.white)
                );

        holder.mView.setOnClickListener(v -> {
            if (null != mListener) {
                mListener.onItemClicked(holder.conversation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mConversationNameView;
        final TextView mUnreadMessagesView;
        Conversation conversation;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mConversationNameView = view.findViewById(R.id.conversation_name);
            mUnreadMessagesView = view.findViewById(R.id.unread_message_count);
        }
    }
}
