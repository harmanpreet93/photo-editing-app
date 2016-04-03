package harman.myinstaapp;

import android.os.AsyncTask;

public class AsyncCallWS extends AsyncTask<String, Void, Void> {
    @Override
    protected Void doInBackground(String... params) {
        //Invoke webservice
//        displayText = WebService.invokeWebService(editText, "hello");

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        //Set response

    }

    @Override
    protected void onPreExecute() {
        //Make ProgressBar invisible
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }

}
