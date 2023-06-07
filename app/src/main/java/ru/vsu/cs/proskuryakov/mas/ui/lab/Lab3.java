package ru.vsu.cs.proskuryakov.mas.ui.lab;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import ru.vsu.cs.proskuryakov.mas.data.service.AESFactory;
import ru.vsu.cs.proskuryakov.mas.databinding.ActivityLab3Binding;

public class Lab3 extends AppCompatActivity {

    private static final String server_name = "http://10.0.2.2:80";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityLab3Binding binding = ActivityLab3Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        TextView labOut = binding.labOut;
        TextView encryptTV = binding.encrypt;
        EditText tbData = binding.tbData;
        Button butSend = binding.butSend;
        Button butBack = binding.btnBack;

        butBack.setOnClickListener((v) -> finish());

        // Устанавливаем обработчик нажатия кнопки
        butSend.setOnClickListener(new View.OnClickListener() {
            // Обработчик нажатия кнопки
            @Override
            public void onClick(View v) {
                // Читаем введенные данные
                String plainText = tbData.getText().toString();
                String encryptMessage = AESFactory.aesInstant().encrypt(plainText);
                encryptTV.setText(encryptMessage);

                // Создаем поток, в котором будет осуществляться обмен данными
                Thread thread = new Thread(new Runnable() {
                    // Обработчик, выполняемый в потоке
                    @Override
                    public void run() {
                        // Открываем соединение и отправляем запрос серверу
                        HttpURLConnection conn = null;

                        try
                        {
                            URL url = new URL(server_name + "/echo?data=" + URLEncoder.encode(encryptMessage, "UTF-8"));
                            conn = (HttpURLConnection) url.openConnection();
                            conn.setReadTimeout(10000);
                            conn.setConnectTimeout(15000);
                            conn.setRequestMethod("GET");
                            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                            conn.setDoInput(true);
                            conn.connect();
                        } catch (Exception e) {
                            runOnUiThread( () -> { labOut.setText("ОШИБКА: " + e.getMessage()); });
                        }

                        // Получаем ответ сервера и закрываем соединение
                        try {
                            InputStream is = conn.getInputStream();
                            BufferedReader br = new BufferedReader(
                                    new InputStreamReader(is, "UTF-8"));
                            StringBuilder sb = new StringBuilder();
                            String bfr_st = null;
                            while ((bfr_st = br.readLine()) != null) {
                                sb.append(bfr_st);
                            }
                            runOnUiThread( ()->{labOut.setText(sb.toString()); } );

                            is.close(); // закроем поток
                            br.close(); // закроем буфер

                        } catch (Exception e) {
                            runOnUiThread( ()-> { labOut.setText("ОШИБКА: " + e.getMessage()); } );
                        }
                        finally {
                            conn.disconnect();
                        }
                    }
                });

                try {
                    thread.start();
                } catch (Exception e) {
                    runOnUiThread( () -> { labOut.setText("Запуск потока ОШИБКА: " + e.getMessage()); } );
                }
            }
        });

    }
}