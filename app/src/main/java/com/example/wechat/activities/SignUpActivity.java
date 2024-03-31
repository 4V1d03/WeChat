package com.example.wechat.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.wechat.R;
import com.example.wechat.databinding.ActivitySignUpBinding;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    private void setListeners(){
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
    }

}