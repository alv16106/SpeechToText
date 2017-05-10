package info.androidhive.speechtotext;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.NaturalLanguageUnderstanding;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalysisResults;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.AnalyzeOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.EntitiesOptions;
import com.ibm.watson.developer_cloud.natural_language_understanding.v1.model.Features;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
	ArrayList<String> result;
	NaturalLanguageUnderstanding service = new NaturalLanguageUnderstanding(
			NaturalLanguageUnderstanding.VERSION_DATE_2017_02_27,
			"e47f7720-a590-4c09-a8bd-b4d8f1edc7e7",
			"Xqo4KfXFnutA"
	);
	JsonParser par = new JsonParser();
	private EditText nombre;
	private EditText apellido;
	private EditText zona;
	private EditText municipio;
	private EditText edad;
	private TextView txtSpeechInput;
	private ImageButton btnSpeak;
	private final int REQ_CODE_SPEECH_INPUT = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		nombre = (EditText) findViewById(R.id.nombre);
		apellido = (EditText)findViewById(R.id.apellido);
		zona = (EditText)findViewById(R.id.zona);
		municipio = (EditText)findViewById(R.id.municipio);
		edad = (EditText)findViewById(R.id.edad);
		txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
		btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);
		btnSpeak.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				promptSpeechInput();
			}
		});

	}

	/**
	 * Showing google speech input dialog
	 * */

    private class CallWatson extends AsyncTask<Void,Void,String> {

        @Override
        protected String doInBackground(Void... params) {
            //Autenticacion de Watson:
            service.setEndPoint("https://gateway.watsonplatform.net/natural-language-understanding/api");
            EntitiesOptions entities = new EntitiesOptions.Builder().sentiment(true).limit(1).model("20:cb17bd1e-76ba-45c5-8482-f73646aec596").build();
            Features features = new Features.Builder().entities(entities).build();
            AnalyzeOptions parameters = new AnalyzeOptions.Builder().text(result.get(0)).features(features).build();
            AnalysisResults results = service.analyze(parameters).execute();
			String x = results.toString();
			JSONObject c;
			JSONArray consulta=null;
			try {
				c = new JSONObject(x);
				consulta = c.getJSONArray("entities");
				String resultadosFinales="";
				for (int i =0;i<consulta.length();i++){
					if (i ==0){
						resultadosFinales = consulta.getJSONObject(i).getString("text");
					}else if (i==(consulta.length()-1)){
						resultadosFinales = resultadosFinales+" y " + consulta.getJSONObject(i).getString("text");
					}else{
						resultadosFinales = resultadosFinales+", " + consulta.getJSONObject(i).getString("text");
					}
				}
				return x;
			} catch (JSONException e) {
				e.printStackTrace();
				return null;
			}

        }

        @Override
        protected void onPostExecute(String s) {
            txtSpeechInput.setText("El diagnostico es: "+s);
        }
    }
	private void promptSpeechInput() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				getString(R.string.speech_prompt));
		try {
			startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
		} catch (ActivityNotFoundException a) {
			Toast.makeText(getApplicationContext(),
					getString(R.string.speech_not_supported),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Receiving speech input
	 * */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case REQ_CODE_SPEECH_INPUT: {
			if (resultCode == RESULT_OK && null != data) {

				result = data
						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				txtSpeechInput.setText(result.get(0));
				new CallWatson().execute();
			}
			break;
		}

		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
