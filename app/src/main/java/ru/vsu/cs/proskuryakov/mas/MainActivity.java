package ru.vsu.cs.proskuryakov.mas;

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
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {

    // url to get all products list
    private static String server_name = "http://10.0.2.2:80";
    String data_string;
    // JSON Node names
    private static final String TAG_STATUS = "status";
    private static final String TAG_DATA = "data";

    private static final String KEY = "MegaPa$$word";
    private static final String IV = "InitVector";
    private static final EasyAES EASY_AES = new EasyAES(KEY, 256, IV);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получаем ссылки на визуальные компоненты
        TextView labOut = (TextView)findViewById(R.id.labOut);
        TextView encryptTV = (TextView)findViewById(R.id.encrypt);
        EditText tbData = (EditText)findViewById(R.id.tbData);
        Button butSend = (Button)findViewById(R.id.butSend);

        // Устанавливаем обработчик нажатия кнопки
        butSend.setOnClickListener(new View.OnClickListener() {
            // Обработчик нажатия кнопки
            @Override
            public void onClick(View v) {
                // Читаем введенные данные
                String plainText = tbData.getText().toString();
                String encryptMessage = EASY_AES.encrypt(plainText);
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
                            URL url = new URL(server_name + "/echo?data=" + URLEncoder.encode(encryptMessage, StandardCharsets.UTF_8.toString()));
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