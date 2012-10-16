package test;

import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONTest {

	public static void main(String[] args) throws JSONException {
		
		Map<String,String> map = new TreeMap<String,String>();
		
		map.put("uri1", "foo");
		map.put("uri2", "bar");
		map.put("uri3", "jee");
		map.put("uri4", "huu");
		
		JSONArray array = new JSONArray();
		array.put(new JSONObject().put("uri", "foo").put("label","y"));
		array.put(new JSONObject().put("uri", "moo"));
		
		JSONObject result = new JSONObject();
		result.put("identifier","uri");
		result.put("items",array);
		
		System.out.println(result);
		
		test((String)null);
		
	}
	
	public static void test(String... arr) {
		System.out.println(arr.length);
		for (String s : arr)
			System.out.println(s);
		System.out.println(arr);
	}
	
}
