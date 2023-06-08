package ru.vsu.cs.proskuryakov.mas.data.model;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class CustomData implements Serializable {
    private String data;

    public CustomData(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    private void writeObject(@NonNull ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(data);
    }

    private void readObject(@NonNull ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        data = (String) ois.readObject();
    }

    @NonNull
    @Override
    public String toString() {
        return "CustomData{" +
            "data='" + data + '\'' +
            '}';
    }
}
