package mapper;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import file_test.DataObject;

public class MapClassObjects {

	public static void main(String[] args) {

		Set<DataObject> data = new HashSet<>();
		data.add(new DataObject("santhosh", Integer.valueOf(23), true, 85.9D, (byte) 7, 5.11F));
		System.out.println(MapClassObjects.dataToMapObject(data));
	}

	@SuppressWarnings("unchecked")
	public static List<Map<String, Object>> dataToMapObject(Object data) {

		if (data instanceof List) {
			return prepareMap((List<Object>) data);
		}
		if (data instanceof Set) {
			return prepareMap((Set<Object>) data);
		}

		return null;
	}

	private static List<Map<String, Object>> prepareMap(List<Object> data) {
		List<Map<String, Object>> result = new ArrayList<>();
		data.stream().forEach(x -> {
			Field[] fields = x.getClass().getDeclaredFields();
			Map<String, Object> map = new HashMap<>();
			mapFields(map, fields, x);
			result.add(map);
		});
		return result;
	}
	
	private static List<Map<String, Object>> prepareMap(Set<Object> data) {
		List<Map<String, Object>> result = new ArrayList<>();
		data.stream().forEach(x -> {
			Field[] fields = x.getClass().getDeclaredFields();
			Map<String, Object> map = new HashMap<>();
			mapFields(map, fields, x);
			result.add(map);
		});
		return result;
	}


	private static void mapFields(Map<String, Object> map, Field[] fields, Object x) {

		for (Field f : fields) {
			f.setAccessible(true);
			try {
				map.put(f.getName(), f.get(x));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}

//Gson gson = new Gson();
//Map map = gson.fromJson(jsonString, Map.class);