package com.example.myapplication;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ListView accountListView;
    private TextView tfaCodeTextView;
    private ProgressBar timerBar;
    private Button addAccountButton, deleteAccountButton;

    private List<String> accounts = new ArrayList<>();
    private String selectedAccount = null;
    private int timerProgress = 0;

    private final Handler handler = new Handler();
    private final Random random = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        accountListView = findViewById(R.id.account_list);
        tfaCodeTextView = findViewById(R.id.tfa_code);
        timerBar = findViewById(R.id.timer_bar);
        addAccountButton = findViewById(R.id.add_account_button);
        deleteAccountButton = findViewById(R.id.delete_account_button);
        Button readButton = findViewById(R.id.read_button);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accounts);
        accountListView.setAdapter(adapter);

        accountListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedAccount = accounts.get(position);
            deleteAccountButton.setEnabled(true);
            updateTfaCode();
        });

        addAccountButton.setOnClickListener(v -> showAddAccountDialog(adapter));

        readButton.setOnClickListener(v -> readButtonDialog(adapter));

        deleteAccountButton.setOnClickListener(v -> showDeleteAccountDialog(adapter));

        startTimer();
    }
    private String readButtonDialog(ArrayAdapter<String> adapter) {
        String text = "";

        try{
            FileInputStream fis = openFileInput("file.txt");
            int size = fis.available();
            byte[] buffer = new byte[size];
            fis.read(buffer);
            fis.close();
            text = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "Error reading file!", Toast.LENGTH_SHORT).show();
        }
        ArrayAdapter<String> text1 = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, accounts);
        accountListView.setAdapter(text1);
        return text;
    }

    private void showAddAccountDialog(ArrayAdapter<String> adapter) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.activity_add_account, null);
        EditText loginField = dialogView.findViewById(R.id.login_field);
        EditText passwordField = dialogView.findViewById(R.id.password_field);
        EditText emailField = dialogView.findViewById(R.id.email_field);

        new AlertDialog.Builder(this)
                .setTitle("Add Account")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String login = loginField.getText().toString().trim();
                    String password = passwordField.getText().toString().trim();
                    String email = emailField.getText().toString().trim();
                    if (!login.isEmpty() && !password.isEmpty() && !email.isEmpty()) {
                        writeToFile("file.txt", login, password);
                        accounts.add(login);
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void writeToFile(String fileName, String login, String password){
        File path = getApplicationContext().getFilesDir();
        try{
            FileOutputStream writer = new FileOutputStream(new File(path, fileName));
            writer.write(login.getBytes());
            writer.close();
            Toast.makeText(getApplicationContext(), "Wrote to file " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showDeleteAccountDialog(ArrayAdapter<String> adapter) {
        if (selectedAccount == null) return;

        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete this account?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    accounts.remove(selectedAccount);
                    selectedAccount = null;
                    deleteAccountButton.setEnabled(false);
                    tfaCodeTextView.setText("000000");
                    adapter.notifyDataSetChanged();
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void startTimer() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                timerProgress += 5;
                timerBar.setProgress(timerProgress);

                if (timerProgress >= 100) {
                    timerProgress = 0;
                    updateTfaCode();
                }

                handler.postDelayed(this, 500);
            }
        }, 500);
    }

    private void updateTfaCode() {
        if (selectedAccount != null) {
            tfaCodeTextView.setText(String.format("%06d", random.nextInt(1000000)));
        }
    }
}
