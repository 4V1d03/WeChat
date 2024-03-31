package com.example.wechat.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    //como se habilito viewBinding para el proyecto, la clase binding para cada XML layout se va a generar automaticamente
    private ActivitySignInBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    //una instancia de una clase Binding contiene referencia directa a todos los views que tienen un ID que corresponde a un layout
    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));

    }



}