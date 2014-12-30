package com.example.neo.translationxml;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    // Calls for the AsyncTask to execute when the translate button is clicked
    public void onTranslateText(View view) {

        EditText translateEditText = (EditText) findViewById(R.id.words_edit_text);

        // If the user entered words to translate then get the JSON data
        if(!isEmpty(translateEditText)){

            Toast.makeText(this, "Getting Translations",
                    Toast.LENGTH_LONG).show();

            // ---------------------- NEW STUFF ----------------------

            // Calls for the method doInBackground to execute
            new GetXMLData().execute();

            // ---------------------- NEW STUFF ----------------------

        } else {

            // Post an error message if they didn't enter words
            Toast.makeText(this, "Enter Words to Translate",
                    Toast.LENGTH_SHORT).show();

        }

    }

    // Check if the user entered words to translate
    // Returns false if not empty
    protected boolean isEmpty(EditText editText){

        // Get the text in the EditText convert it into a string, delete whitespace
        // and check length
        return editText.getText().toString().trim().length() == 0;

    }

    // ---------------------- NEW STUFF ----------------------

    // Allows you to perform background operations without locking up the user interface
    // until they are finished
    // The void part is stating that it doesn't receive parameters, it doesn't monitor progress
    // and it won't pass a result to onPostExecute
    class GetXMLData extends AsyncTask<Void, Void, Void>{

        // Will hold the final string to display
        String stringToPrint = "";

        @Override
        protected Void doInBackground(Void... voids) {

            // Holds the xml String that is retrieved from the web service
            String xmlString = "";

            // Holds the words the user wants to translate
            String wordsToTranslate = "";

            // Where the user enters the words to translate
            EditText translateEditText = (EditText) findViewById(R.id.words_edit_text);

            // Get the words the user entered in the EditText box
            wordsToTranslate = translateEditText.getText().toString();

            // Replace spaces with plus so they can be passed in the URL to the web service
            wordsToTranslate = wordsToTranslate.replace(" ", "+");

            // Client used to grab data from a provided URL
            DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());

            // Provide the URL for the post request
            HttpPost httpPost = new HttpPost("http://newjustin.com/translateit.php?action=xmltranslations&english_words=" + wordsToTranslate);

            // Define that the data expected is in JSON format
            httpPost.setHeader("Content-type", "text/xml");

            // Allows you to input a stream of bytes from the URL
            InputStream inputStream = null;

            try {

                // The client calls for the post request to execute and sends the results back
                HttpResponse response = httpClient.execute(httpPost);

                // Holds the message sent by the response
                HttpEntity entity = response.getEntity();

                // Get the content sent
                inputStream = entity.getContent();

                // A BufferedReader is used because it is efficient
                // The InputStreamReader converts the bytes into characters
                // My JSON data is UTF-8 so I read that encoding
                // 8 defines the input buffer size
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);

                // Storing each line of data in a StringBuilder
                StringBuilder sb = new StringBuilder();

                String line = null;

                // readLine reads all characters up to a \n and then stores them
                while((line = reader.readLine()) != null){

                    sb.append(line);

                }

                // Save the results to a String
                xmlString = sb.toString();

                // Generates an XML parser
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

                // The XML parser that is generated will support XML namespaces
                factory.setNamespaceAware(true);

                // Gathers XML data and provides information on that data
                XmlPullParser xpp = factory.newPullParser();

                // Input the XML data for parsing
                xpp.setInput(new StringReader(xmlString));

                // The event type is either START_DOCUMENT, END_DOCUMENT, START_TAG,
                // END_TAG, TEXT
                int eventType = xpp.getEventType();

                // Cycle through the XML document until the document ends
                while (eventType != XmlPullParser.END_DOCUMENT) {

                    // Each time you find a new opening tag the event type will be START_TAG
                    // We want to skip the first tag with the name translations
                    if ((eventType == XmlPullParser.START_TAG) && (!xpp.getName().equals("translations"))) {

                        // getName returns the name for the current element with focus
                        stringToPrint = stringToPrint + xpp.getName() + " : ";

                        // getText returns the text for the current event
                    } else if (eventType == XmlPullParser.TEXT) {
                        stringToPrint = stringToPrint + xpp.getText() + "\n";
                    }
                    // next puts focus on the next element in the XML doc
                    eventType = xpp.next();
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        // ---------------------- NEW STUFF ----------------------

        @Override
        protected void onPostExecute(Void aVoid) {

            TextView translateTextView = (TextView) findViewById(R.id.translate_text_view);

            translateTextView.setText(stringToPrint);

        }
    }


}
