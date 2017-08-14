package com.box.sdk.utilities;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.box.sdk.BatchAPIRequest;
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxJSONRequest;
import com.box.sdk.BoxUser;
import com.box.sdk.IAccessTokenCache;
import com.box.sdk.InMemoryLRUAccessTokenCache;
import com.box.sdk.http.HttpMethod;
import com.eclipsesource.json.JsonObject;

/**
 *
 */
public class BatchRequestExample {

    private static final int MAX_CACHE_ENTRIES = 100;
        private static final String APP_USER_NAME = "BATCH-TEST-APP-USER-NAME";


    public static void main(String[] args) throws IOException {
        // Turn off logging to prevent polluting the output.
        Logger.getLogger("com.box.sdk").setLevel(Level.OFF);


        //It is a best practice to use an access token cache to prevent unneeded requests to Box for access tokens.
        //For production applications it is recommended to use a distributed cache like Memcached or Redis, and to
        //implement IAccessTokenCache to store and retrieve access tokens appropriately for your environment.
        IAccessTokenCache accessTokenCache = new InMemoryLRUAccessTokenCache(MAX_CACHE_ENTRIES);

        Reader reader = new FileReader("src/example/config/config2.json");
        BoxConfig boxConfig = BoxConfig.readFrom(reader);

        BoxDeveloperEditionAPIConnection api = BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(
                boxConfig, accessTokenCache);

        List<BoxAPIRequest> requests = new ArrayList<BoxAPIRequest>();

        //Get current user request
        URL getMeURL = BoxUser.GET_ME_URL.build(api.getBaseURL());
        BoxAPIRequest getMeRequest = new BoxAPIRequest(getMeURL, HttpMethod.GET);
        requests.add(getMeRequest);

        //Create App User Request
        URL createUserURL = BoxUser.USERS_URL_TEMPLATE.build(api.getBaseURL());
        BoxJSONRequest createAppUserRequest = new BoxJSONRequest(createUserURL, HttpMethod.POST);
        JsonObject requestJSON = new JsonObject();
        requestJSON.add("name", APP_USER_NAME);
        requestJSON.add("is_platform_access_only", true);
        createAppUserRequest.setBody(requestJSON.toString());
        requests.add(createAppUserRequest);

        //Get Root Folder Request
        URL getRootFolderURL = BoxFolder.FOLDER_INFO_URL_TEMPLATE.build(api.getBaseURL(), 0);
        BoxAPIRequest getRootFolderRequest = new BoxAPIRequest(getRootFolderURL, HttpMethod.GET);
        requests.add(getRootFolderRequest);

        BatchAPIRequest batchRequest = new BatchAPIRequest(api);
        List<BoxAPIResponse> responses = batchRequest.execute(requests);

        System.out.println("GET ME RESPONSE:");
        System.out.println("Response Code: " + responses.get(0).getResponseCode());
        System.out.println("Response: " + responses.get(0).getBodyAsString());

        System.out.println("CREATE APP USER RESPONSE:");
        System.out.println("Response Code: " + responses.get(1).getResponseCode());
        System.out.println("Response: " + responses.get(1).getBodyAsString());

        System.out.println("GET ROOT FOLDER RESPONSE:");
        System.out.println("Response Code: " + responses.get(2).getResponseCode());
        System.out.println("Response: " + responses.get(2).getBodyAsString());
    }
}
