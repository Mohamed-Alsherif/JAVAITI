package com.mycompany.traindataframe;

import static com.mycompany.traindataframe.utils.Utils.*;

import com.mycompany.traindataframe.utils.CloseableIterator;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

public class Main {

	public static void main(String[] args) throws Exception {

//		long start = System.currentTimeMillis();
//		SparkConf conf = new SparkConf().setAppName("traindataframe");
//
//		String master = System.getProperty("spark.master");
//		if (master == null || master.trim().length() == 0) {
//			System.out.println("No master found ; running locally");
//			conf = conf
//				.setMaster("local[*]")
//				.set("spark.driver.host", "127.0.0.1")
//				;
//		} else {
//			System.out.println("Master found to be " + master);
//		}
//
//		// Tries to determine necessary jar
//		String source = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//		if (source.endsWith(".jar")) {
//			conf.setJars(new String [] {source});
//		}
//
//		try (JavaSparkContext sc = new JavaSparkContext(conf)) {
//
//			// Do your analysis here starting with sc
//		}
//
//		long s = (System.currentTimeMillis() - start) / 1000;
//		String dur = String.format("%d:%02d:%02d", s / 3600, (s % 3600) / 60, (s % 60));
//		System.out.println("Analysis completed in " + dur);
	}
}
