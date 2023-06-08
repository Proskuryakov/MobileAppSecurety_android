package ru.vsu.cs.proskuryakov.mas.ui.lab;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ru.vsu.cs.proskuryakov.mas.databinding.ActivityLab4Binding;
import ru.vsu.cs.proskuryakov.mas.ui.login.LoggedInUserView;
import ru.vsu.cs.proskuryakov.mas.ui.login.LoginActivity;

public class Lab4 extends AppCompatActivity {

    private ActivityLab4Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLab4Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Button butBack = binding.btnBack;
        Button butLogout = binding.logout;

        butBack.setOnClickListener((v) -> finish());
        butLogout.setOnClickListener((v) -> logout());

        // Вызов формы авторизации
        Intent intent = new Intent(this, LoginActivity.class);
        mStartForResult.launch(intent);
    }

    ActivityResultLauncher<Intent> mStartForResult = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                // Ссылки на текст-боксы
                TextView tvLogin = binding.tvName;
                TextView tvData = binding.tvData;
                TextView tvError = binding.tvError;
                Button butLogout = binding.logout;

                // Проверка результата входа
                if (result.getResultCode() == Activity.RESULT_OK) {
                    butLogout.setVisibility(View.VISIBLE);
                    // В случае успеха, получение данных пользователя
                    Intent intent = result.getData();
                    if (intent.hasExtra("UserData")) {
                        // Получение экземпляра структуры с данными
                        LoggedInUserView userData = (LoggedInUserView) intent.getSerializableExtra("UserData");
                        runOnUiThread(() -> {
                            tvLogin.setText(userData.getDisplayName());
                        });
                        runOnUiThread(() -> {
                            tvData.setText(userData.getDisplayData().toString());
                        });
                    }
                } else {
                    butLogout.setVisibility(View.INVISIBLE);
                    // В случае неудачи - сообщение об ошибке
                    Intent intent = result.getData();
                    if (intent.hasExtra("Error")) {
                        String errorString = intent.getStringExtra("Error");
                        runOnUiThread(() -> {
                            tvError.setText(errorString);
                        });
                    } else
                        runOnUiThread(() -> {
                            tvError.setText("Ошибка доступа");
                        });
                }
            }
        });

    private void logout(){
        Intent intent  = new Intent(this, LoginActivity.class);
        intent.putExtra("logout",true);
        mStartForResult.launch(intent);
    }

}