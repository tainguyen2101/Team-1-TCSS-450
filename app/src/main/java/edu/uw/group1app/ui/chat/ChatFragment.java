package edu.uw.group1app.ui.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import edu.uw.group1app.MainActivity;
import edu.uw.group1app.R;
import edu.uw.group1app.databinding.FragmentChatBinding;
import edu.uw.group1app.model.UserInfoViewModel;

/**
 * this class provides a function that a user can chat
 *
 * @author Gyubeom Kim
 * @version 2.0
 */

public class ChatFragment extends Fragment {
    /**
     * a chat id
     */
    private int mChatID;

    /**
     * title of chat
     */
    private String mChatTitle;

    /*
     * chat send view model
     */
    private ChatSendViewModel mSendModel;

    /**
     * chat view model
     */
    private ChatViewModel mChatModel;

    /**
     * user info view model containing user information
     */
    private UserInfoViewModel mUserModel;

    public ChatFragment() {
        // Required empty public constructor
    }

    /**
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewModelProvider provider = new ViewModelProvider(getActivity());
        mUserModel = provider.get(UserInfoViewModel.class);
        mChatModel = provider.get(ChatViewModel.class);
        mSendModel = provider.get(ChatSendViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ChatFragmentArgs args = ChatFragmentArgs.fromBundle(getArguments());
        mChatID = args.getChatid();
        mChatTitle = args.getChattitle();
        ((MainActivity) getActivity()).setActionBarTitle(mChatTitle);
        mChatModel.getFirstMessages(mChatID, mUserModel.getmJwt());
        FragmentChatBinding binding = FragmentChatBinding.bind(getView());

        //SetRefreshing shows the internal Swiper view progress bar. Show this until messages load
        binding.swipeContainer.setRefreshing(true);

        final RecyclerView rv = binding.recyclerMessages;
        //Set the Adapter to hold a reference to the list FOR THIS chat ID that the ViewModel
        //holds.
        rv.setAdapter(new ChatRecyclerViewAdapter(
                mChatModel.getMessageListByChatId(mChatID),
                mUserModel.getEmail()));


        //When the user scrolls to the top of the RV, the swiper list will "refresh"
        //The user is out of messages, go out to the service and get more
        binding.swipeContainer.setOnRefreshListener(() -> {
            mChatModel.getNextMessages(mChatID, mUserModel.getmJwt());
        });

        mChatModel.addMessageObserver(mChatID, getViewLifecycleOwner(),
                list -> {
                    /*
                     * This solution needs work on the scroll position. As a group,
                     * you will need to come up with some solution to manage the
                     * recyclerview scroll position. You also should consider a
                     * solution for when the keyboard is on the screen.
                     */
                    //inform the RV that the underlying list has (possibly) changed
                    rv.getAdapter().notifyDataSetChanged();
                    rv.scrollToPosition(rv.getAdapter().getItemCount() - 1);
                    binding.swipeContainer.setRefreshing(false);
                });

        //Send button was clicked. Send the message via the SendViewModel
        binding.buttonSend.setOnClickListener(button -> {
            mSendModel.sendMessage(mChatID,
                    mUserModel.getmJwt(),
                    binding.editMessage.getText().toString());
        });

        binding.buttonAdd.setOnClickListener(button ->
            Navigation.findNavController(getView()).navigate(
                    ChatFragmentDirections.actionChatFragmentToContactListFragment(mChatID, true)
        ));

        //when we get the response back from the server, clear the edittext
        mSendModel.addResponseObserver(getViewLifecycleOwner(), response ->
                binding.editMessage.setText(""));

    }

    /**
     * returns chat id
     *
     * @return chat id representing chat id
     */
    public int getmChatID() {
        return mChatID;
    }
}
