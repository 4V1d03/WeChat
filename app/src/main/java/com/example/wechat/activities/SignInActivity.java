package com.example.wechat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.wechat.databinding.ActivitySignInBinding;
import com.example.wechat.utilities.Constants;
import com.example.wechat.utilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    //como se habilito viewBinding para el proyecto, la clase binding para cada XML layout se va a generar automaticamente
    private ActivitySignInBinding binding;
    private PreferenceManager preferenceManager;   // Sirve para facilitar la gestión del estado de la sesión del usuario y otros datos importantes de la aplicación sin tener que manejar directamente SharedPreferences cada vez.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferenceManager = new PreferenceManager(getApplicationContext());
        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
        binding = ActivitySignInBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
    }

    //una instancia de una clase Binding contiene referencia directa a todos los views que tienen un ID que corresponde a un layout
    /*Estableces los listeners para dos elementos de la interfaz de usuario:
    textCreateNewAccount y buttonSignIn. El primero abre SignUpActivity cuando se hace clic en él, permitiendo al usuario registrarse.
    El segundo llama al método isValidSignInDetails() para validar las entradas del usuario y,
    si son válidas, procede a llamar a signIn() para intentar iniciar sesión.*/
    private void setListeners(){
        binding.textCreateNewAccount.setOnClickListener(v ->
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class)));
        binding.buttonSignIn.setOnClickListener(v -> {
            if (isValidSignInDetails()) {
                signIn();
            }
        });
    }

    /* Este método realiza la operación de inicio de sesión. Muestra la barra de progreso para indicar que el proceso está en curso, y luego realiza
    una consulta a la base de datos de Firestore buscando un usuario que coincida con el correo electrónico y la contraseña proporcionados.*/
    private void signIn() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    }
                    else {
                        loading(false);
                        showToast("Unable to sign in");
                    }
                });
    }

    /* Controla la visibilidad de la interfaz de usuario basándose en el estado de la operación de inicio de sesión.
    Cuando isLoading es true, ocultas el botón de inicio de sesión y muestras la barra de progreso. Cuando es false, hace lo contrario.*/
    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.buttonSignIn.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignIn.setVisibility(View.VISIBLE);
        }
    }

    /*Muestra un Toast con el mensaje proporcionado. Este es un mensaje flotante breve que proporciona información al usuario, como errores o confirmaciones.*/
    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /*Verifica que los campos de correo electrónico y contraseña no estén vacíos y que el correo electrónico tenga un formato válido.
    Si algún campo no cumple con las validaciones, muestra un Toast con un mensaje apropiado y devuelve false. Si todo es valido devuelve true*/
    private Boolean isValidSignInDetails() {
        if (binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        }
        else if (binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        }
        else {
            return true;
        }
    }
}