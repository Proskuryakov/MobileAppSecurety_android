package ru.vsu.cs.proskuryakov.mas.data;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import ru.vsu.cs.proskuryakov.mas.data.model.LoggedInUser;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.KeyStore;
import java.util.HashMap;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    private final String FILE_NAME = "passwords.dat";
    private Context context;

    /// Сохраненные зашифрованные данные
    public Result login(String username, String password, Context context) {
        this.context = context;

        // Хеш пароля
        HashMap<String, HashMap<String, byte[]>> heshMap = new HashMap<String, HashMap<String, byte[]>>();
        HashMap<String, byte[]> hesh = new HashMap<>();

        // Генерация данных пользователя (имени)
        LoggedInUser fakeUser =
            new LoggedInUser(
                java.util.UUID.randomUUID().toString(),
                username);

        // Регистрация/авторизация
        try {
            // Загрузка сохранённого хэша
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (file.exists()) {
                ObjectInputStream fis = new ObjectInputStream(context.openFileInput(FILE_NAME));
                heshMap = (HashMap<String, HashMap<String, byte[]>>) fis.readObject();
                hesh = heshMap.get(username);
                fis.close();
            }

            // Псевдоним ключа
            String alias = "MyKeyAlias";

            // Если нет сохраненных данных для этого пользователя, то "регистрируем" его:
            if (hesh == null || hesh.isEmpty()) {
                // Генерирация нового случайного ключа
                final KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(alias,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    //.setUserAuthenticationRequired(true) //requires lock screen, invalidated if lock screen is disabled
//                    .setUserAuthenticationValidityDurationSeconds(-1) //only available x seconds from password authentication. -1 requires finger print - every time
                    .setRandomizedEncryptionRequired(true) //different ciphertext for same plaintext on each call
                    .build();
                keyGenerator.init(keyGenParameterSpec);
                keyGenerator.generateKey();

                // Шифрование пароля
                byte[] passBytes = password.getBytes("UTF-8");
                hesh = encrypt(passBytes, alias);

                heshMap.put(username, hesh);

                // Сохранение хэша в файл
                ObjectOutputStream oos = new ObjectOutputStream(context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
                oos.writeObject(heshMap);
                oos.close();

                // Успешная регистрация
                return new Result.Success<>(fakeUser);
            }

            // Если ключ сохранен, то сравниваем его с паролем
            byte[] passBytes = decrypt(hesh, alias);
            if (passBytes != null) {
                String passString = new String(passBytes, "UTF-8");
                if (passString.compareTo(password) == 0)
                    return new Result.Success<>(fakeUser);
                else
                    return new Result.Error("Wrong password");
            } else
                return new Result.Error("Decrypt error");

        } catch (Exception e) {
            return new Result.Error("Error logging in");
        }
    }

    public void logout(String username) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (file.exists()) {
                ObjectInputStream fis = new ObjectInputStream(context.openFileInput(FILE_NAME));
                HashMap<String, HashMap<String, byte[]>> heshMap = (HashMap<String, HashMap<String, byte[]>>) fis.readObject();
                HashMap<String, byte[]> hesh = heshMap.get(username);
                heshMap.remove(username, hesh);
                fis.close();
                ObjectOutputStream oos = new ObjectOutputStream(context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE));
                oos.writeObject(heshMap);
                oos.close();
                Log.i("LoginDataSource", "LOGOUT");
            }
        } catch (Exception e) {
            Log.e("LoginDataSource", "LOGOUT ERROR");
        }
    }

    private HashMap<String, byte[]> encrypt(final byte[] decryptedBytes, String alias) {
        final HashMap<String, byte[]> map = new HashMap<String, byte[]>();
        try {
            //Get the key
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null);
            final SecretKey secretKey = secretKeyEntry.getSecretKey();
            //Encrypt data
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            final byte[] ivBytes = cipher.getIV();
            final byte[] encryptedBytes = cipher.doFinal(decryptedBytes);
            map.put("iv", ivBytes);
            map.put("encrypted", encryptedBytes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return map;
    }

    private byte[] decrypt(final HashMap<String, byte[]> map, String alias) {
        byte[] decryptedBytes = null;
        try {
            //Get the key
            final KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            final KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(alias, null);
            final SecretKey secretKey = secretKeyEntry.getSecretKey();
            //Extract info from map
            final byte[] encryptedBytes = map.get("encrypted");
            final byte[] ivBytes = map.get("iv");
            //Decrypt data
            final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            final GCMParameterSpec spec = new GCMParameterSpec(128, ivBytes);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);
            decryptedBytes = cipher.doFinal(encryptedBytes);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return decryptedBytes;
    }
}