package org.library.mapper;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.TypeAdapter;

/**
 * This class provides methods to map objects to maps.
 */
public class MapDataObjects {

	@Inject
	Logger log = LoggerFactory.getLogger(MapDataObjects.class);

	/**
	 * Converts an object to a list of maps.
	 *
	 * @param data The input object.
	 * @return A list of maps representing the data.
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 * @throws JsonProcessingException   If there's an issue processing JSON data.
	 * @throws IllegalArgumentException  If there's an illegal argument.
	 * @throws IllegalAccessException    If there's an illegal access attempt.
	 */
	@SuppressWarnings("unchecked")
	protected List<Map<String, Object>> dataToMapObject(Object data) throws JsonProcessingException,
			IllegalArgumentException, IllegalAccessException, InvocationTargetException, IntrospectionException {
		if (data instanceof List) {
			return prepareMap((List<Object>) data);
		} else if (data instanceof Map) {
			List<Map<String, Object>> mapResult = new ArrayList<>();
			mapResult.add((Map<String, Object>) data);
			return mapResult;
		}

		return new ArrayList<>();
	}

	/**
	 * Prepares a list of maps from the input data.
	 *
	 * @param data The input list of objects.
	 * @return A list of maps representing the data.
	 * @throws IllegalArgumentException  If there's an illegal argument.
	 * @throws IllegalAccessException    If there's an illegal access attempt.
	 * @throws JsonProcessingException   If there's an issue processing JSON data.
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 */
	private List<Map<String, Object>> prepareMap(List<Object> data) throws IllegalArgumentException,
			IllegalAccessException, JsonProcessingException, InvocationTargetException, IntrospectionException {
		List<Map<String, Object>> result = new ArrayList<>();

		for (Object x : data) {
			Map<String, Object> map = new LinkedHashMap<>();

			if (x instanceof String str) {
				if (isValidJson(str)) {
					map = mapStringToJson(str);
				}
			} else if (checkForCast(x)) {
				Field[] fields = x.getClass().getDeclaredFields();
				mapFields(map, fields, x);
			}

			result.add(map);
		}

		return result;
	}

	/**
	 * Maps fields of an object to a map.
	 *
	 * @param map    The map to store field values.
	 * @param fields The fields to map.
	 * @param x      The object containing field values.
	 * @throws IllegalArgumentException  If there's an illegal argument.
	 * @throws IllegalAccessException    If there's an illegal access attempt.
	 * @throws IntrospectionException
	 * @throws InvocationTargetException
	 */
	private void mapFields(Map<String, Object> map, Field[] fields, Object x)
			throws IllegalArgumentException, IllegalAccessException, IntrospectionException, InvocationTargetException {
		for (Field f : fields) {
			PropertyDescriptor pd = new PropertyDescriptor(f.getName(), x.getClass());
			Method getter = pd.getReadMethod();
			map.put(f.getName(), getter.invoke(x));
		}
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> mapStringToJson(String data) throws JsonProcessingException {
		return new ObjectMapper().readValue(data, LinkedHashMap.class);
	}

	/**
	 * Checks if a JSON string is valid.
	 *
	 * @param json The JSON string to validate.
	 * @return True if the JSON is valid, false otherwise.
	 */
	private boolean isValidJson(String json) {
		final TypeAdapter<JsonElement> strictAdapter = new Gson().getAdapter(JsonElement.class);
		try {
			strictAdapter.fromJson(json);
		} catch (JsonSyntaxException | IOException e) {
			String message = "List Contains Invalid JSON Check for Syntax %s";
			log.error(String.format(message, " Error: {}"), e);
			return false;
		}
		return true;
	}

	/**
	 * Checks if an object type should be cast to a class.
	 *
	 * @param x The object to check.
	 * @return True if the object should be cast, false otherwise.
	 */
	private boolean checkForCast(Object x) {
		String str = x.getClass().getName();
		return !("java.lang.String".equals(str) || "java.lang.Integer".equals(str) || "java.lang.Character".equals(str)
				|| "java.lang.Double".equals(str) || "java.lang.Long".equals(str));
	}
}
