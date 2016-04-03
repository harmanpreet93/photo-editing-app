package harman.myinstaapp;

import android.os.AsyncTask;

public class AsyncCallWS extends AsyncTask<Void, Void, Void> {

    private String effect;
    private byte[] byteArray;


    public AsyncCallWS(String effect,byte[] byteArray) {
        this.effect = effect;
        this.byteArray = byteArray;
    }
    @Override
    protected Void doInBackground(Void... params) {
        //Invoke webservice
        WebService.invokeWebService(effect,byteArray);
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
