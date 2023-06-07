package ru.vsu.cs.proskuryakov.mas.data.service;

public class AESFactory {

    private static final String KEY = "MegaPa$$word";
    private static final String IV = "InitVector";
    private static final EasyAES EASY_AES = new EasyAES(KEY, 256, IV);

    public static EasyAES aesInstant() {
        return EASY_AES;
    }

}
