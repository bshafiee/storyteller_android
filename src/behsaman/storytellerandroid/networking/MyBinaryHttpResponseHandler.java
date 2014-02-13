package behsaman.storytellerandroid.networking;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;

import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
* Used to intercept and handle the responses from requests made using {@link AsyncHttpClient}.
* Receives response body as byte array with a content-type whitelist. (e.g. checks Content-Type
* against allowed list, Content-length). <p>&nbsp;</p> For example: <p>&nbsp;</p>
* <pre>
* AsyncHttpClient client = new AsyncHttpClient();
* String[] allowedTypes = new String[] { "image/png" };
* client.get("http://www.example.com/image.png", new BinaryHttpResponseHandler(allowedTypes) {
* &#064;Override
* public void onSuccess(byte[] imageData) {
* // Successfully got a response
* }
*
* &#064;Override
* public void onFailure(Throwable e, byte[] imageData) {
* // Response failed :(
* }
* });
* </pre>
*/
public abstract class MyBinaryHttpResponseHandler extends AsyncHttpResponseHandler {

    private static final String LOG_TAG = "BinaryHttpResponseHandler";

    private String[] mAllowedContentTypes = new String[]{
            "image/jpeg",
            "image/png"
    };

    /**
* Method can be overriden to return allowed content types, can be sometimes better than passing
* data in constructor
*
* @return array of content-types or Pattern string templates (eg. '.*' to match every response)
*/
    public String[] getAllowedContentTypes() {
        return mAllowedContentTypes;
    }

    /**
* Creates a new BinaryHttpResponseHandler
*/
    public MyBinaryHttpResponseHandler() {
        super();
    }

    /**
* Creates a new BinaryHttpResponseHandler, and overrides the default allowed content types with
* passed String array (hopefully) of content types.
*
* @param allowedContentTypes content types array, eg. 'image/jpeg' or pattern '.*'
*/
    public MyBinaryHttpResponseHandler(String[] allowedContentTypes) {
        super();
        if (allowedContentTypes != null)
            mAllowedContentTypes = allowedContentTypes;
        else
            Log.e(LOG_TAG, "Constructor passed allowedContentTypes was null !");
    }

    @Override
    public abstract void onSuccess(int statusCode, Header[] headers, byte[] binaryData);

    @Override
    public abstract void onFailure(int statusCode, Header[] headers, byte[] binaryData, Throwable error);

    @Override
    public final void sendResponseMessage(HttpResponse response) throws IOException {
        StatusLine status = response.getStatusLine();
        Header[] contentTypeHeaders = response.getHeaders("Content-Type");
        if (contentTypeHeaders.length >= 1) {
	        Header contentTypeHeader = contentTypeHeaders[0];
	        boolean foundAllowedContentType = false;
	        for (String anAllowedContentType : getAllowedContentTypes()) {
	            try {
	                if (Pattern.matches(anAllowedContentType, contentTypeHeader.getValue())) {
	                    foundAllowedContentType = true;
	                }
	            } catch (PatternSyntaxException e) {
	                Log.e("BinaryHttpResponseHandler", "Given pattern is not valid: " + anAllowedContentType, e);
	            }
	        }
        }
        /*if (!foundAllowedContentType) {
            //Content-Type not in allowed list, ABORT!
            sendFailureMessage(status.getStatusCode(), response.getAllHeaders(), null, new HttpResponseException(status.getStatusCode(), "Content-Type not allowed!"));
            return;
        }*/
        super.sendResponseMessage(response);
    }
}

