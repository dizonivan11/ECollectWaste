package com.ecw.ecollectwaste;

import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

public class InputValidator {
    public static boolean IfEmpty(FragmentActivity fa, EditText input_view, String toast_message) {
        String value = input_view.getText().toString();
        if (value.equals("")) {
            Toast.makeText(fa, toast_message, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public static boolean IfPhoneEmpty(FragmentActivity fa, EditText input_view, int limit, String toast_message) {
        String value = input_view.getText().toString();
        try {
            Long.parseLong(value);
            if (value.length() != limit) throw new NumberFormatException("Phone number length not met");

        } catch (NumberFormatException ex) {
            Toast.makeText(fa, toast_message, Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
