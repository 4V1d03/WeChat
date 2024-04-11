package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.wechat.R;
import com.example.wechat.databinding.ActivityChatBinding;
import com.example.wechat.models.User;
import com.example.wechat.utilities.Constants;

public class ChatActivity extends AppCompatActivity {
    private ActivityChatBinding binding;
    private User receiverUSer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
    }

    private void loadReceiverDetails(){
        receiverUSer = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.textname.setText(receiverUSer.name);
    }

    private void setListeners(){
        binding.imageBack.setOnClickListener(v -> onBackPressed());
    }
}