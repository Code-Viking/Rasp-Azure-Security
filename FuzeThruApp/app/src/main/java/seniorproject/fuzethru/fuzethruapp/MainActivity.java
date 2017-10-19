package seniorproject.fuzethru.fuzethruapp;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final IotDevice device = new IotDevice(); // Creates IoT Device

        final Button lightOn = (Button)findViewById(R.id.lightOn);
        lightOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPostMessage(device, "lightOn", "");
            }
        });

        final Button lightOff = (Button)findViewById(R.id.lightOff);
        lightOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendPostMessage(device, "lightOff", "");
            }
        });
    }

    private void sendPostMessage(final IotDevice device, final String command, final String payload) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // Create URL
                    URL iotHubEndPoint = new URL(device.getUri() + "methods/" + device.getApiVersion());
                    // Create connection
                    HttpsURLConnection connection = (HttpsURLConnection) iotHubEndPoint.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestProperty("Authorization", device.getSasToken());
                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-Type", "application/json");
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());
                    outputStream.writeBytes("{\"methodName\":\"" + command + "\",\"responseTimeoutInSeconds\":200,\"payload\":{" + payload + "}}");
                    outputStream.flush();
                    outputStream.close();
                    final int connectionCode = connection.getResponseCode();
                    if (connectionCode == 200) {
                        // Success
                        Log.d("Connection", "Successful: " + String.valueOf(connectionCode));
                        InputStream responseBody = connection.getInputStream();
                        InputStreamReader responseBodyReader = new InputStreamReader(responseBody, "UTF-8");
                        JsonReader jsonReader = new JsonReader(responseBodyReader);
                        final String response = getResponse(jsonReader);
                        jsonReader.close();
                        Log.d("Connection", "Response: " + response);
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                Toast.makeText(MainActivity.this, response, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Log.e("Connection", "Failed: " + String.valueOf(connectionCode));runOnUiThread(new Runnable(){
                            @Override
                            public void run(){
                                Toast.makeText(MainActivity.this, "Error: " + String.valueOf(connectionCode), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    connection.disconnect();

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public String getResponse(JsonReader reader) throws IOException {
        reader.beginObject();
        String payload = "No Response From Device";
        while (reader.hasNext()) {
            String key = reader.nextName();
            if (key.equals("payload")) {
                payload = reader.nextString();
                break;
            } else {
                reader.skipValue();
            }
        }
        return payload;
    }
}
