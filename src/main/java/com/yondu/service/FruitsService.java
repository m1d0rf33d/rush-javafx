package com.yondu.service;

import com.yondu.utils.Java2JavascriptUtils;

import static java.lang.Thread.sleep;
import static java.util.Arrays.asList;
import static java.util.Collections.shuffle;
import static javafx.application.Platform.runLater;
import static org.json.simple.JSONValue.toJSONString;

import java.util.List;

public class FruitsService {

	// async function
	public void loadFruits(final Object callbackfunction){
		
		// a database...
		final List<String> fruits = asList(
				new String[] { "orange", "apple", "banana", "strawberry" });
		
		// launch a background thread (async)
		new Thread( () -> {
				try {
					shuffle(fruits);
					sleep(1000); //add some processing simulation...
					runLater( () ->
							Java2JavascriptUtils.call(callbackfunction, toJSONString(fruits))
					);
				} catch (InterruptedException e) {	}
			}
		).start();
	}
}
