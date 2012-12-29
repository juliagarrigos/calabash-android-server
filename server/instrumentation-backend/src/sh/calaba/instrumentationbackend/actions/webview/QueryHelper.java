package sh.calaba.instrumentationbackend.actions.webview;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sh.calaba.instrumentationbackend.InstrumentationBackend;
import sh.calaba.org.codehaus.jackson.map.ObjectMapper;
import android.webkit.WebView;

public class QueryHelper {

	public static String executeJavascriptInWebviews(WebView webViewOrNull, String scriptPath, String... args) {
		
		String script = readJavascriptFromAsset(scriptPath);

		for (String arg : args) {
			script = script.replaceFirst("%@", arg);
		}

		final String myScript = script;
    	List<CalabashChromeClient> webViews = null;
    	if (webViewOrNull == null)
    	{
    		webViews = CalabashChromeClient.findAndPrepareWebViews();
    	}
    	else 
    	{
    		webViews = Collections.singletonList(CalabashChromeClient.prepareWebView(webViewOrNull));	
    	}
    	

    	for (CalabashChromeClient ccc : webViews) {
    	    WebView w = ccc.getWebView();
            w.loadUrl("javascript:calabash_result = " + myScript + ";prompt('calabash:' + calabash_result);");
			return ccc.getResult();
		}
    	throw new RuntimeException("No webviews found");
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Object> findFirstVisibleRectangle(List<HashMap<String,Object>> elements) {
		return (Map<String, Object>)findFirstVisibleElement(elements).get("rect");	
	}
	
	public static Map<String, Object> findFirstVisibleElement(List<HashMap<String,Object>> elements) {
		//TODO: Should do something more intelligent
		return (Map<String, Object>)elements.get(0);	
	}
	
	public static float[] getScreenCoordinatesForCenter(WebView webView, Map<String, Object> rectangle) {
		try {
			
            float scale = webView.getScale();


			System.out.println("scale: " + scale);
			int[] webviewLocation = new int[2];
			webView.getLocationOnScreen(webviewLocation);
			
			//TODO: Exception if center_x or center_y are not numbers
			float x = webviewLocation[0] + ((Number)rectangle.get("center_x")).floatValue() * scale;
			float y = webviewLocation[1] + ((Number)rectangle.get("center_y")).floatValue() * scale;
			return new float[]{x, y};
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static float[] getScreenCoordinatesForCenter(Map<String, Object> rectangle) {
		WebView webView = CalabashChromeClient.findAndPrepareWebViews().get(0).getWebView();
		return getScreenCoordinatesForCenter(webView, rectangle);
	}
	
	
	public static String toJsonString(Object o) {
		//http://www.mkyong.com/java/how-to-convert-java-map-to-from-json-jackson/
		try {
			return new ObjectMapper().writeValueAsString(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	

    private static String readJavascriptFromAsset(String scriptPath) {
    	StringBuffer script = new StringBuffer();
		try {
			InputStream is = InstrumentationBackend.instrumentation.getContext().getResources().getAssets().open(scriptPath);
			BufferedReader input =  new BufferedReader(new InputStreamReader(is));
			String line = null;
			while (( line = input.readLine()) != null){
				script.append(line);
				script.append("\n");
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return script.toString();
    }
}
