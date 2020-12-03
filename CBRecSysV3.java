package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.Set;

public class CBRecSysV3 { // Update

	static int list_length = 10;
	static double train_ratio = 0.8; // 80% train 20% test
	static double r = 0;
	static double f = 0;
	static double p = 0;
	static double ap = 0;
	static double meanAp = 0;
	static double kFold = 5; 
	static double fsaEval = 0; //FSA total score
	static double recCount = 0; //Number of FSA ratings

	// FilePaths ***Update these to local filepaths
	static String itemProfile1 = "C:\\\\javafiles\\\\item-profiles1.csv";
	static String itemProfile2 = "C:\\\\javafiles\\\\item-profiles2.csv";
	static String itemProfile3 = "C:\\\\javafiles\\\\item-profiles3.csv";
	static String userRating = "C:\\\\javafiles\\\\user-item-rating.csv";
	static String userGroupRating = "C:\\\\javafiles\\\\user-item-rating-group.csv";

	public static void main(String[] args) {

		/*
		 * [0]Recipe ID;[1]Name;[2]Fiber (g);[3]Sodium (g);[4]Carbohydrates (g);[5]Fat
		 * (g);[6]Protein (g);[7]Sugar (g);[8]Saturated Fat (g);[9]Size (g);
		 * [10]Servings;[11]Calories (kCal);[12]Average Rating;[13]Average
		 * Sentiment;[14]Number of Ratings;[15]Number of Bookmarks;[16]Year of
		 * Publishing double grams, double d_sodium, double d_sugar, double d_fat,
		 * double d_saturated_fat
		 */
		// k is Key that defines what parameter to add.
		HashMap<String, String> grams = readInItemProfileMulti(itemProfile2, 9);
		HashMap<String, String> sodium = readInItemProfileMulti(itemProfile2, 3);
		HashMap<String, String> sugar = readInItemProfileMulti(itemProfile2, 7);
		HashMap<String, String> fat = readInItemProfileMulti(itemProfile2, 5);
		HashMap<String, String> s_fat = readInItemProfileMulti(itemProfile2, 8);

		// item profiles
		HashMap<String, String> items = readInItemProfile(itemProfile2);
		// user profiles
		HashMap<String, ArrayList<String>> users = readInUserProfile(userRating);

		// Define train and test sets
		for (int k = 0; k < kFold; k++) {
			HashMap<String, ArrayList<String>> train = new HashMap<String, ArrayList<String>>();
			HashMap<String, ArrayList<String>> test = new HashMap<String, ArrayList<String>>();
			// generate train/test sets based on the given ratio on user level
			for (String user : users.keySet()) {

				ArrayList<String> user_items = users.get(user);
				Collections.shuffle(user_items); // shuffle items
				ArrayList<String> train_items = new ArrayList<String>();
				ArrayList<String> test_items = new ArrayList<String>();

				for (int i = 0; i < user_items.size(); i++) {

					String item = user_items.get(i);

					if (i < user_items.size() * train_ratio) {
						train_items.add(item);
					} else {
						test_items.add(item);
					}
				}
				train.put(user, train_items);
				test.put(user, test_items);
			}

			for (String user : train.keySet()) {

				ArrayList<String> user_items = train.get(user);

				StringBuilder user_profile = new StringBuilder();
				for (int i = 0; i < user_items.size(); i++) {
					String item = items.get(user_items.get(i));
					user_profile.append(item);
					user_profile.append(" ");
				}

				HashMap<String, Double> map = new HashMap<String, Double>();

				

				for (String item : items.keySet()) {
					String item_profile = items.get(item);

					// double ui_score1 = jaccard_sim(user_profile.toString(),item_profile);
					double ui_score2 = cosineSimilarity(string2map(user_profile.toString()), string2map(item_profile));
					// double ui_score3 = dice_sim(user_profile.toString(),item_profile);
					
					// double grams, double d_sodium, double d_sugar, double d_fat, double
					// d_saturated_fat*/

					double FSA_Score = (16 - FSA(Double.valueOf(grams.get(item)), Double.valueOf(sodium.get(item)),
							Double.valueOf(sugar.get(item)), Double.valueOf(fat.get(item)),
							Double.valueOf(s_fat.get(item)) - 4 + 1));
					double hybrid = ((2 * ui_score2)) + 0.1 * FSA_Score;
					//Random generator = new Random();
					//double baseline = generator.nextDouble();
					map.put(item, hybrid);

				}
				// map lowest to highest & sort list and cut at predefined list length
				//following section is an ugly hack implemented to calculate AP for MAP
				Map<String, Double> sortedMap = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(list_length)
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
								LinkedHashMap::new));
				Map<String, Double> sortedMap1 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(1).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				Map<String, Double> sortedMap2 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(2).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				Map<String, Double> sortedMap3 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(3).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				Map<String, Double> sortedMap4 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(4).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				Map<String, Double> sortedMap5 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(5).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				Map<String, Double> sortedMap6 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(6).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				Map<String, Double> sortedMap7 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(7).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				Map<String, Double> sortedMap8 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(8).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				Map<String, Double> sortedMap9 = map.entrySet().stream()
						.sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(9).collect(Collectors
								.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
				ArrayList<Map> maps = new ArrayList<Map>();
				maps.add(sortedMap1);
				maps.add(sortedMap2);
				maps.add(sortedMap3);
				maps.add(sortedMap4);
				maps.add(sortedMap5);
				maps.add(sortedMap6);
				maps.add(sortedMap7);
				maps.add(sortedMap8);
				maps.add(sortedMap9);
				maps.add(sortedMap);
				double apTemp = 0;
				double mapCounter = 1;
				for (Map currentMap : maps) {
					HashSet<String> results_ = new HashSet<String>();
					HashSet<String> test_ = new HashSet<String>();

					int i = 0;

					// output results
					for (Object j : currentMap.keySet()) {

						results_.add((String) j);
						test_.add(test.get(user).get(i));

						i++;

						if (i == test.get(user).size())
							break;
					}
					results_.retainAll(test_);
					apTemp += (double) results_.size() / (double) mapCounter;
					mapCounter++;

				}

				ap += ((double) 1 / (double) list_length) * (double) apTemp;

				// ************* Result printing for iteration **********//
				// item id //similarity values //test
				// find many from train in test data //goal
				// usual with low values
				// 0.0 or 0.1 normal
				System.out.println("--\n" + user + "\n--");
				System.out.println("Train,Sim,Test:");

				HashSet<String> results_ = new HashSet<String>();
				HashSet<String> test_ = new HashSet<String>();

				int i = 0;

				// output results
				for (String j : sortedMap.keySet()) {

					results_.add(j);
					test_.add(test.get(user).get(i));

					System.out.println(j + "," + sortedMap.get(j) + "," + test.get(user).get(i));
					i++;

					if (i == test.get(user).size())
						break;
				}

				results_.retainAll(test_);
				// calculate precision @k
				System.out.println("P@" + list_length + ": " + (double) results_.size() / list_length);
				p += (double) results_.size() / (double) list_length;
				// Calc Recall
				r += (double) results_.size() / (double) test_.size();
				// Calc F1
				double tempR = (double) results_.size() / (double) list_length;
				double tempP = (double) results_.size() / (double) test_.size();
				if (tempP == 0 && tempR == 0) {
				} else {
					f += (double) 2 * ((tempP * tempR) / (tempP + tempR));
				}

				if (!results_.isEmpty()) {
					for (String id : results_) {
						fsaEval += FSA(Double.valueOf(grams.get(id)), Double.valueOf(sodium.get(id)),
								Double.valueOf(sugar.get(id)), Double.valueOf(fat.get(id)),
								Double.valueOf(s_fat.get(id)));
						recCount++;
					}
				}
			}
		}
		// Addjust numbers for Kfold
		System.out.println("*******************************************");
		p = (p / (double) users.size()) / kFold;
		System.out.println("P@" + list_length + " overall: " + p);

		r = (r / (double) users.size()) / kFold;
		System.out.println("R@" + list_length + " overall: " + r);

		f = ((double) f / (double) kFold) / users.size();
		System.out.println("F1 overall: " + f);

		meanAp = (((double) 1 / (double) users.size()) * (double) ap) / kFold;
		System.out.println("MAP@" + list_length + ": " + meanAp);

		fsaEval = fsaEval / recCount;
		System.out.println("Average FSA value: " + fsaEval);

	} // END MAIN

	public static double userProfile2Num(String str) {

		double num = 0.0;

		String l[] = str.split("\\s+");

		for (int i = 0; i < l.length; i++) {
			num += Double.valueOf(l[i]);
		}
		num = num / (double) l.length;
		return num;
	}

	public static double numDist(double a, double b) {
		double num = 0.0;
		num = Math.abs(a - b);
		return num;
	}

	public static Double cosineSimilarity(final Map<String, Integer> leftVector,
			final Map<String, Integer> rightVector) {
		if (leftVector == null || rightVector == null) {
			throw new IllegalArgumentException("Vectors must not be null");
		}

		final Set<CharSequence> intersection = getIntersection(leftVector, rightVector);

		final double dotProduct = dot(leftVector, rightVector, intersection);
		double d1 = 0.0d;
		for (final Integer value : leftVector.values()) {
			d1 += Math.pow(value, 2);
		}
		double d2 = 0.0d;
		for (final Integer value : rightVector.values()) {
			d2 += Math.pow(value, 2);
		}
		double cosineSimilarity;
		if (d1 <= 0.0 || d2 <= 0.0) {
			cosineSimilarity = 0.0;
		} else {
			cosineSimilarity = (double) (dotProduct / (double) (Math.sqrt(d1) * Math.sqrt(d2)));
		}
		return cosineSimilarity;
	}

	private static Set<CharSequence> getIntersection(final Map<String, Integer> leftVector,
			final Map<String, Integer> rightVector) {
		final Set<CharSequence> intersection = new HashSet<>(leftVector.keySet());
		intersection.retainAll(rightVector.keySet());
		return intersection;
	}

	private static double dot(final Map<String, Integer> leftVector, final Map<String, Integer> rightVector,
			final Set<CharSequence> intersection) {
		long dotProduct = 0;
		for (final CharSequence key : intersection) {
			dotProduct += leftVector.get(key) * rightVector.get(key);
		}
		return dotProduct;
	}
	// ***********************************COSINE SIMILARTY END
	// ********************************''

	public static Map<String, Integer> string2map(String str) {
		Map<String, Integer> map = new HashMap<>();
		String l[] = str.split("\\s+");
		for (int i = 0; i < l.length; i++) {
			if (map.containsKey(l[i])) {
				Integer c = map.get(l[i]);
				c = c + 1;
				map.put(l[i], c);
			} else {
				map.put(l[i], 1);
			}
		}
		return map;
	}

	// *************DICE SIM ****************************'
	public static double dice_sim(String str1, String str2) {
		double val = 0.0;

		// split on white spaces & tokanize strings
		String l1[] = str1.split("\\s+");
		String l2[] = str2.split("\\s+");

		// add to hashsets - remove redundant entries
		HashSet<String> set1 = new HashSet<String>();
		HashSet<String> set2 = new HashSet<String>();

		// generate sets
		for (int i = 0; i < l1.length; i++) {
			set1.add(l1[i]);
		}
		for (int i = 0; i < l2.length; i++) {
			set2.add(l2[i]);
		}

		HashSet<String> union = new HashSet<String>();
		union.addAll(set1);
		union.addAll(set2);

		HashSet<String> intersection = new HashSet<String>();
		intersection.addAll(set1);
		intersection.retainAll(set2);

		// compute dice sim sim
		double aboveline = 2 * (double) intersection.size();
		double belowline = ((double) set1.size() + (double) set2.size());
		val = (double) aboveline / belowline;

		return val;

	}

	// *********************** JACCARD SIM **************************
	public static double jaccard_sim(String str1, String str2) {
		double val = 0.0;

		// split on white spaces & tokanize strings
		String l1[] = str1.split("\\s+");
		String l2[] = str2.split("\\s+");

		// add to hashsets - remove redundant entries
		HashSet<String> set1 = new HashSet<String>();
		HashSet<String> set2 = new HashSet<String>();

		// generate sets
		for (int i = 0; i < l1.length; i++) {
			set1.add(l1[i]);
		}
		for (int i = 0; i < l2.length; i++) {
			set2.add(l2[i]);
		}

		HashSet<String> union = new HashSet<String>();
		union.addAll(set1);
		union.addAll(set2);

		HashSet<String> intersection = new HashSet<String>();
		intersection.addAll(set1);
		intersection.retainAll(set2);

		// compute jaccard sim
		val = (double) intersection.size() / (double) union.size();

		return val;
	}

	// ************************************ Food value
	// calc***************************
	static double FSA(double grams, double d_sodium, double d_sugar, double d_fat, double d_saturated_fat) {
		int FSA_fat_low_tmp = 0;
		int FSA_fat_medium_tmp = 0;
		int FSA_fat_high_tmp = 0;
		int FSA_fat_sat_low_tmp = 0;
		int FSA_fat_sat_medium_tmp = 0;
		int FSA_fat_sat_high_tmp = 0;
		int FSA_sugar_low_tmp = 0;
		int FSA_sugar_medium_tmp = 0;
		int FSA_sugar_high_tmp = 0;
		int FSA_salt_low_tmp = 0;
		int FSA_salt_medium_tmp = 0;
		int FSA_salt_high_tmp = 0;

		int fat = 0;
		int sat_fat = 0;
		int sugar = 0;
		int salt = 0;

		// (d_cal_fat/1000.0)/grams*100.0;

		double salt_fsa = (d_sodium * 2.5); // 10-15
		double sugar_fsa = (d_sugar); // <10
		double fat_fsa = (d_fat); // 15-30
		double sat_fat_fsa = (d_saturated_fat); // <10

		if (fat_fsa > 21.0) { // red
			// FSA_fat_high++;
			FSA_fat_high_tmp++;
			fat = 3;
		} else {
			fat_fsa = fat_fsa / grams * 100.0;
			if (fat_fsa <= 3.0) { // green
				// FSA_fat_low++;
				fat = 1;
				FSA_fat_low_tmp++;
			} else if (fat_fsa > 3.0 && fat_fsa <= 20.0) { // yellow
				FSA_fat_medium_tmp++;
				fat = 2;
			} else { // red
				// FSA_fat_high++;
				FSA_fat_high_tmp++;
				fat = 3;
			}
		}

		if (sat_fat_fsa > 6.0) { // red
			// FSA_fat_sat_high++;
			FSA_fat_sat_high_tmp++;
			sat_fat = 3;
		} else {
			sat_fat_fsa = sat_fat_fsa / grams * 100.0;
			if (sat_fat_fsa <= 1.5) { // green
				// FSA_fat_sat_low++;
				sat_fat = 1;
				FSA_fat_sat_low_tmp++;
			} else if (sat_fat_fsa > 1.5 && sat_fat_fsa <= 5.0) { // yellow
				// FSA_fat_sat_medium++;
				FSA_fat_sat_medium_tmp++;
				sat_fat = 2;
			} else { // red
				// FSA_fat_sat_high++;
				FSA_fat_sat_high_tmp++;
				sat_fat = 3;
			}
		}

		if (sugar_fsa > 15.0) { // red
			// FSA_sugar_high++;
			FSA_sugar_high_tmp++;
			sugar = 3;
		} else {
			sugar_fsa = sugar_fsa / grams * 100.0;
			if (sugar_fsa <= 5.0) { // green
				// FSA_sugar_low++;
				FSA_sugar_low_tmp++;
				sugar = 1;
			} else if (sugar_fsa > 5.0 && sugar_fsa <= 12.5) { // yellow
				// FSA_sugar_medium++;
				FSA_sugar_medium_tmp++;
				sugar = 2;
			} else { // red
				// FSA_sugar_high++;
				FSA_sugar_high_tmp++;
				sugar = 3;
			}
		}

		if (salt_fsa > 2.4) { // red
			// FSA_salt_high++;
			FSA_salt_high_tmp++;
			salt = 3;
		} else {
			salt_fsa = salt_fsa / grams * 100.0;
			if (salt_fsa <= 0.3) { // green
				// FSA_salt_low++;
				FSA_salt_low_tmp++;
				salt = 1;
			} else if (salt_fsa > 0.3 && salt_fsa <= 1.5) { // yellow
				// FSA_salt_medium++;
				FSA_salt_medium_tmp++;
				salt = 2;
			} else { // red
				// FSA_salt_high++;
				FSA_salt_high_tmp++;
				salt = 3;
			}
		}

		int FSA_val = FSA_fat_low_tmp + FSA_fat_medium_tmp * 2 + FSA_fat_high_tmp * 3 + FSA_fat_sat_low_tmp
				+ FSA_fat_sat_medium_tmp * 2 + FSA_fat_sat_high_tmp * 3 + FSA_sugar_low_tmp + FSA_sugar_medium_tmp * 2
				+ FSA_sugar_high_tmp * 3 + FSA_salt_low_tmp + FSA_salt_medium_tmp * 2 + FSA_salt_high_tmp * 3;

		return FSA_val;

	}

	// *************************** FILE READING FUNCTIONS START
	// *************************************
	public static HashMap<String, ArrayList<String>> readInUserProfile(String file) {

		HashMap<String, ArrayList<String>> data = new HashMap<String, ArrayList<String>>();

		File fin = new File(file);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fin), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (line != null) {

			String l[] = line.split("\t");

			ArrayList<String> items = new ArrayList<String>();
			if (data.containsKey(l[0])) {
				items = data.get(l[0]);
			}
			items.add(l[1]);

			data.put(l[0], items);

			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	public static HashMap<String, String> readInItemProfile(String file) {

		HashMap<String, String> data = new HashMap<String, String>();

		File fin = new File(file);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fin), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (line != null) {
			String l[] = line.split(";");

			data.put(l[0], l[1]); // configured for title
			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return data;
	}

	public static HashMap<String, String> readInItemProfileMulti(String file, int k) {

		/*
		 * [0]Recipe ID;[1]Name;[2]Fiber (g);[3]Sodium (g);[4]Carbohydrates (g);[5]Fat
		 * (g);[6]Protein (g);[7]Sugar (g);[8]Saturated Fat (g);[9]Size (g);
		 * [10]Servings;[11]Calories (kCal);[12]Average Rating;[13]Average
		 * Sentiment;[14]Number of Ratings;[15]Number of Bookmarks;[16]Year of
		 * Publishing
		 */
		// k is Key that defines what parameter to add.

		HashMap<String, String> data = new HashMap<String, String>();

		File fin = new File(file);
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(fin), "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		String line = null;
		try {
			line = reader.readLine();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		while (line != null) {
			String l[] = line.split(";");

			data.put(l[0], l[k]);

			try {
				line = reader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return data;
	}
	// ************************** FILE READING ENDS *******************************
}
