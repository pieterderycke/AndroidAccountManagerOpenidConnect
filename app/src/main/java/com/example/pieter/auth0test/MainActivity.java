package com.example.pieter.auth0test;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    static final int CHOOSE_ACCOUNT_IDTOKEN_REQUEST = 0;
    static final int CHOOSE_ACCOUNT_ACCESSTOKEN_REQUEST = 1;
    static final int CHOOSE_ACCOUNT_REFRESHTOKEN_REQUEST = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void login(View view) {
        AccountManager accountManager = AccountManager.get(view.getContext());
        accountManager.addAccount(SampleAccountAuthenticator.ACCOUNT_TYPE, null, null, null, this, null, null);
    }

    public void displayAccessToken(View view) {
        Intent chooseAccountIntent = AccountManager.newChooseAccountIntent(null, null,
                new String[]{SampleAccountAuthenticator.ACCOUNT_TYPE},
                false, null, null, null, null);
        startActivityForResult(chooseAccountIntent, CHOOSE_ACCOUNT_ACCESSTOKEN_REQUEST);
    }

    public void displayRefreshToken(View view) {
        Intent chooseAccountIntent = AccountManager.newChooseAccountIntent(null, null,
                new String[]{SampleAccountAuthenticator.ACCOUNT_TYPE},
                false, null, null, null, null);
        startActivityForResult(chooseAccountIntent, CHOOSE_ACCOUNT_REFRESHTOKEN_REQUEST);
    }

    public void displayIdToken(View view) {
        Intent chooseAccountIntent = AccountManager.newChooseAccountIntent(null, null,
                new String[]{SampleAccountAuthenticator.ACCOUNT_TYPE},
                false, null, null, null, null);
        startActivityForResult(chooseAccountIntent, CHOOSE_ACCOUNT_IDTOKEN_REQUEST);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CHOOSE_ACCOUNT_IDTOKEN_REQUEST || requestCode == CHOOSE_ACCOUNT_ACCESSTOKEN_REQUEST
                || requestCode == CHOOSE_ACCOUNT_REFRESHTOKEN_REQUEST) {
            if (resultCode == RESULT_OK) {
                String tokeType = GetTokenType(requestCode);

                String accountName= data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                String accountType = data.getExtras().getString(AccountManager.KEY_ACCOUNT_TYPE);
                Account account = new Account(accountName, accountType);

                AccountManager accountManager = AccountManager.get(this);
                accountManager.getAuthToken(account, tokeType, null, false, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle bundle = future.getResult();

                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(bundle.getString("authAccount"))
                                    .setMessage(bundle.getString("authtoken"))
                                    .show();
                        }
                        catch(Exception ex) {
                            ex.getMessage();
                        }
                    }
                }, null);
            }
        }
    }

    private String GetTokenType(int requestCode) {
        switch(requestCode) {
            case CHOOSE_ACCOUNT_IDTOKEN_REQUEST:
                return "idToken";
            case CHOOSE_ACCOUNT_ACCESSTOKEN_REQUEST:
                return "accessToken";
            case CHOOSE_ACCOUNT_REFRESHTOKEN_REQUEST:
                return "refreshToken";
            default:
                return null;
        }
    }
}
