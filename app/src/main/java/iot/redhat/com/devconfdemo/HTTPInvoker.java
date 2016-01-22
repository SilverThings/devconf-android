package iot.redhat.com.devconfdemo;

import android.os.AsyncTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by jpechane on 19.1.16.
 */
public class HTTPInvoker {
    private static final String CONTENT_TYPE = "application/json";
    private static final Logger LOG = LoggerFactory.getLogger(HTTPInvoker.class.getSimpleName());

    private final String serverAddress;

    public HTTPInvoker(final String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public String post(String path, String body) {
        return doHttpRequest(path, "POST", body);
    }

    public String get(String path) {
        return doHttpRequest(path, "GET", null);
    }

    public String delete(String path, String method) {
        return doHttpRequest(path, "DELETE", null);
    }

    private String doHttpRequest(String path, String method, String body) {
        AsyncTask<String, Void, String> net = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                LOG.info("doInBackground");
                String result;
                String path = serverAddress + params[0];
                String method = params[1];
                String body = params[2];
                try {
                    final URL url = new URL(path);
                    final HttpURLConnection http = (HttpURLConnection) url.openConnection();
                    http.setRequestMethod(method);
                    if (body != null) {
                        http.setDoOutput(true);
                        http.setRequestProperty( "Content-Type", CONTENT_TYPE);
                    }
                    http.connect();
                    if (body != null) {
                        final OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream());
                        writer.append(body);
                        writer.flush();
                    }
                    final InputStreamReader reader = new InputStreamReader(http.getInputStream());
                    final StringWriter sw = new StringWriter();
                    char[] buffer = new char[8192];
                    int cnt = reader.read(buffer);
                    while(cnt != -1) {
                        sw.write(buffer, 0, cnt);
                        cnt = reader.read(buffer);
                    }
                    sw.close();
                    reader.close();
                    result = sw.toString();
                }
                catch (Exception e) {
                    LOG.error("Error when doing HTTP request", e);
                    result = e.toString();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                LOG.debug("onPostExecute: {}", s);
                super.onPostExecute(s);

            }
        };
        try {
            net.execute(path, method, body);
            return "";
        }
        catch (Exception e) {
            return e.toString();
        }
    }
}
