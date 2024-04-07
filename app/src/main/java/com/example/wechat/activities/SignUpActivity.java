package com.example.wechat.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import com.example.wechat.databinding.ActivitySignUpBinding;
import com.example.wechat.utilities.Constants;
import com.example.wechat.utilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private PreferenceManager preferenceManager;
    private String encodeImage;

    /*Este es el método que se llama cuando se crea la actividad de registro. Aquí inicializas el ViewBinding,
    estableces la vista de la actividad con setContentView(), inicias PreferenceManager para manejar las preferencias
    de la aplicación y configuras los listeners para los eventos de la interfaz de usuario.*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        setListeners();
    }

    /*Configuras los listeners de los componentes de la UI. Cuando el usuario hace clic en textSignIn, la actividad retrocede
    al estado anterior (probablemente al inicio de sesión). Cuando el usuario hace clic en buttonSignUp, se verifica si los
    detalles de registro son válidos y, si es así, se procede a registrar al usuario. El listener en layoutImage lanza
    una Intent para seleccionar una imagen de la galería.*/
    private void setListeners(){
        binding.textSignIn.setOnClickListener(v -> onBackPressed());
        binding.buttonSignUp.setOnClickListener(v -> {
            if (isValidSignUpDetails()){
                signUp();
            }
        });
        binding.layoutImage.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(intent);
        });
    }

    /*Muestra un mensaje corto al usuario utilizando un Toast, que es una pequeña ventana emergente que muestra información.*/
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /*Es el método que maneja el proceso de registro. Primero muestra la barra de progreso para indicar que el registro está en curso.
    Luego, crea un nuevo usuario en la base de datos de Firestore con los detalles proporcionados. Si el registro es exitoso, guarda
    la información relevante en PreferenceManager, inicia MainActivity y finaliza la actividad actual. Si el registro falla,
    muestra un mensaje de error usando un Toast*/
    private void signUp() {
        loading(true);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_NAME, binding.inputName.getText().toString());
        user.put(Constants.KEY_EMAIL, binding.inputEmail.getText().toString());
        user.put(Constants.KEY_PASSWORD, binding.inputPassword.getText().toString());
        user.put(Constants.KEY_IMAGE, encodeImage);
        database.collection(Constants.KEY_COLLECTION_USERS)
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    loading(false);
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                    preferenceManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferenceManager.putString(Constants.KEY_NAME, binding.inputName.getText().toString());
                    preferenceManager.putString(Constants.KEY_IMAGE, encodeImage);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(exception -> {
                    loading(false);
                    showToast(exception.getMessage());
                });
    }

    /*Toma un objeto Bitmap, lo redimensiona a un ancho fijo manteniendo la proporción de aspecto, y luego lo comprime y
    codifica en Base64, que es un formato de cadena que se puede guardar fácilmente en Firestore o en SharedPreferences.*/
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    /*Es una instancia de ActivityResultLauncher, utilizada para recibir el resultado de la selección de imagen.
    Cuando se selecciona una imagen correctamente, la procesa, actualiza la UI y guarda la codificación de la imagen en Base64.*/
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK) {
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodeImage = encodeImage(bitmap);
                        }
                        catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    /* Realiza la validación de los campos de entrada del formulario de registro. Comprueba que se haya seleccionado una imagen
    de perfil, que los campos de texto no estén vacíos, que el correo electrónico sea válido y que la contraseña y
    su confirmación coincidan. Devuelve true si todos los campos son válidos.*/
    private Boolean isValidSignUpDetails() {
        if (encodeImage == null) {
            showToast("Select profile image");
            return false;
        }
        else if(binding.inputName.getText().toString().trim().isEmpty()) {
            showToast("Enter name");
            return false;
        }
        else if(binding.inputEmail.getText().toString().trim().isEmpty()) {
            showToast("Enter email");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()) {
            showToast("Enter valid email");
            return false;
        }
        else if(binding.inputPassword.getText().toString().trim().isEmpty()) {
            showToast("Enter password");
            return false;
        }
        else if(binding.inputConfirmPassword.getText().toString().trim().isEmpty()) {
            showToast("Confirm your password");
            return false;
        }
        else if (!binding.inputPassword.getText().toString().equals(binding.inputConfirmPassword.getText().toString())) {
            showToast("Password & confirm password must be same");
            return false;
        }
        else {
            return true;
        }
    }

    /*Controla la visibilidad de la barra de progreso y el botón de registro en la UI basándose en el estado de isLoading.
    Cuando isLoading es true, muestra la barra de progreso y oculta el botón de registro. Cuando es false, muestra el botón
    de registro y oculta la barra de progreso.*/
    private void loading(Boolean isLoading) {
        if(isLoading) {
            binding.buttonSignUp.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.progressBar.setVisibility(View.INVISIBLE);
            binding.buttonSignUp.setVisibility(View.VISIBLE);
        }
    }
}