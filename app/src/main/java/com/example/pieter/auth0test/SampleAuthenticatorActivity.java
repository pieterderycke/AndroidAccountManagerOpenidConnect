package com.example.pieter.auth0test;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.TokenRequest;
import com.google.api.client.auth.openidconnect.IdToken;
import com.google.api.client.auth.openidconnect.IdTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Pieter on 22/11/2015.
 */
public class SampleAuthenticatorActivity extends AccountAuthenticatorActivity {
    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    /**
     * Global instance of the HTTP transport.
     */
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    private static final DataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private static final String TOKEN_SERVER_URL = "https://pdr.eu.auth0.com/oauth/token";
    private static final String AUTHORIZATION_SERVER_URL = "https://pdr.eu.auth0.com/authorize";
    private static final String API_KEY = "cT8RfY16qfsR9YR0278ierA9xXMWkAV2";
    private static final String API_SECRET = "YVrfcHMqmDlw98lRczlpgymGtsWrQ_hj49LzouYdIi9CC1a_4tfPuubEGvUBbz2J";
    private static final String[] SCOPES = new String[]{ "openid", "offline_access", "profile" };
    private static final String REDIRECT_URI = "be.deryckepieter.auth0test://login";

    private AccountManager accountManager;
    private AuthorizationCodeFlow flow;

    static {
        DATA_STORE_FACTORY = new MemoryDataStoreFactory();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authenticator);

        accountManager = AccountManager.get(getBaseContext());

        try {
            flow = new AuthorizationCodeFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    HTTP_TRANSPORT,
                    JSON_FACTORY,
                    new GenericUrl(TOKEN_SERVER_URL),
                    new ClientParametersAuthentication(API_KEY, API_SECRET),
                    API_KEY,
                    AUTHORIZATION_SERVER_URL)
                    .setScopes(Arrays.asList(SCOPES))
                    .setDataStoreFactory(DATA_STORE_FACTORY)
                            //.setDataStoreFactory(DATA_STORE_FACTORY)
                    .build();

            if (!isRedirect(getIntent())) {
                String authorizationUrl = flow.newAuthorizationUrl().setRedirectUri(REDIRECT_URI).build();

                // Open the login page in the native browser
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(authorizationUrl));
                startActivity(browserIntent);
            }
        } catch (Exception ex) {
            Log.e("auth0test", ex.getMessage());
        }
    }

    @Override
    protected void onNewIntent (Intent intent) {
        super.onNewIntent(intent);

        if(isRedirect(intent)) {
            String authorizationCode = extractAuthorizationCode(intent);
            new GetTokens(flow).execute(authorizationCode);
        }
    }

    private boolean isRedirect(Intent intent) {
        String data = intent.getDataString();

        return Intent.ACTION_VIEW.equals(intent.getAction()) && data != null;
    }

    private String extractAuthorizationCode(Intent intent){
        String data = intent.getDataString();
        Uri uri = Uri.parse(data);

        return uri.getQueryParameter("code");
    }

    private class GetTokens extends AsyncTask<String, Integer, IdTokenResponse> {

        private final AuthorizationCodeFlow flow;

        public GetTokens(AuthorizationCodeFlow flow) {
            this.flow = flow;
        }

        protected IdTokenResponse doInBackground(String... params) {
            try {
                TokenRequest request = flow.newTokenRequest(params[0])
                        .setRedirectUri(REDIRECT_URI);

                return IdTokenResponse.execute(request);
            }
            catch(IOException ex) {
                Log.e("auth0test", ex.getMessage());
                return null;
            }
            catch (Exception ex) {
                Log.e("auth0test", ex.getMessage());
                return null;
            }
        }

        protected void onPostExecute(IdTokenResponse result) {
            try {
                IdToken idToken = result.parseIdToken();
                String userName = idToken.getPayload().get("name").toString();

                Account account = new Account(userName, SampleAccountAuthenticator.ACCOUNT_TYPE);

                accountManager.addAccountExplicitly(account, null, null);
                accountManager.setAuthToken(account, "accessToken", result.getAccessToken());
                accountManager.setAuthToken(account, "refreshToken", result.getRefreshToken());
                accountManager.setAuthToken(account, "idToken", result.getIdToken());

                Bundle data = new Bundle();
                data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
                data.putString(AccountManager.KEY_ACCOUNT_TYPE, SampleAccountAuthenticator.ACCOUNT_TYPE);
                data.putString(AccountManager.KEY_AUTHTOKEN, result.getAccessToken());

                Intent intent = new Intent();
                intent.putExtras(data);

                setAccountAuthenticatorResult(intent.getExtras());
                setResult(RESULT_OK, intent);
                finish();
            }
            catch(IOException ex) {
                setResult(RESULT_CANCELED);
                finish();
            }
        }

    }
}
