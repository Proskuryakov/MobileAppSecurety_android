package ru.vsu.cs.proskuryakov.mas;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import ru.vsu.cs.proskuryakov.mas.ui.lab.Lab3;
import ru.vsu.cs.proskuryakov.mas.ui.lab.Lab4;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button butLab3 = findViewById(R.id.butLab3);
        Button butLab4 = findViewById(R.id.butLab4);

        butLab3.setOnClickListener((v) -> startActivity(new Intent(this, Lab3.class)));
        butLab4.setOnClickListener((v) -> startActivity(new Intent(this, Lab4.class)));

    }
}