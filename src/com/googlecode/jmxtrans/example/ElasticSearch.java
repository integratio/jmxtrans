/**
 * 
 */
package com.googlecode.jmxtrans.example;

import java.io.File;

import com.googlecode.jmxtrans.JmxTransformer;
import com.googlecode.jmxtrans.model.JmxProcess;
import com.googlecode.jmxtrans.util.JmxUtils;

/**
 * @author Shanny Anoep
 *
 */
public class ElasticSearch {
	/**
    *
    */
	public static void main(String[] args) throws Exception {

		JmxProcess process = JmxUtils.getJmxProcess(new File("elasticsearch-custom.json"));
		JmxUtils.printJson(process);

		JmxTransformer transformer = new JmxTransformer();
		transformer.executeStandalone(process);

		System.out.println("started!");
	}

}
