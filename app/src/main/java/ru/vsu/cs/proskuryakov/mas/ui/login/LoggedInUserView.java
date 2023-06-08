package ru.vsu.cs.proskuryakov.mas.ui.login;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import ru.vsu.cs.proskuryakov.mas.data.model.CustomData;

/**
 * Class exposing authenticated user details to the UI.
 */
public class LoggedInUserView implements Serializable {
    private String displayName;
    //... other data fields that may be accessible to the UI
    private CustomData displayData;

    LoggedInUserView(String displayName, CustomData displayData) {
        this.displayName = displayName;
        this.displayData = displayData;
    }

    public String getDisplayName() {
        return displayName;
    }

    public CustomData getDisplayData() {
        return displayData;
    }

    private void writeObject(@NonNull ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(displayName);
        oos.writeObject(displayData);
    }

    private void readObject(@NonNull ObjectInputStream ois)
        throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
        displayName = (String)ois.readObject();
        displayData = (CustomData)ois.readObject();
    }
}