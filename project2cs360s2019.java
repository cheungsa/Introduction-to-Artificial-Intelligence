package project2cs360s2019;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Scanner;
import java.util.Vector;

public class project2cs360s2019 {
	
	public static void main(String[] args) {
		int numHeroes = 0;
		int nextHeroId = 0;
		String algorithm = "";
		File file = new File("input3.txt"); //2, 3(null), 6, 7, 8, 9(null)
		try {
			Scanner scan = new Scanner(file);
			numHeroes = scan.nextInt();
			algorithm = scan.next();
			Vector<Hero> heroes = new Vector<Hero>(numHeroes);
			Team radiant = new Team(1);
			Team dire = new Team(2);
			
			// note to self: should i do for loop instead?
			while (scan.hasNext()) {
				String line = scan.next();
				String[] heroInfo = line.split(",");
				int id = Integer.parseInt(heroInfo[0]);
				double power = Double.parseDouble(heroInfo[1]);
				double rMastery = Double.parseDouble(heroInfo[2]);
				double dMastery = Double.parseDouble(heroInfo[3]);
				int membership = Integer.parseInt(heroInfo[4]);
				
				Hero hero = new Hero(id, power, rMastery, dMastery, membership);
				
				// hero hasn't been picked yet
				if (membership == 0) {
					heroes.add(hero);
				}
				// hero has been picked by me
				else if (membership == 1) {
					radiant.add(hero, 1);
				}
				// hero has been picked by opponent
				else if (membership == 2) {
					dire.add(hero, 2);
				}
			}	
			
			Collections.sort(heroes, new HeroCmp());
			//print(radiant, dire, heroes);
			Tree tree = new Tree(heroes, radiant, dire);
			
			if (algorithm.contains("minimax")) {
				//tree.minimax(true, heroes, radiant, dire, null);
				tree.Minmax(true, heroes, radiant, dire, null);
			}
			else if (algorithm.contains("ab")) {
				//tree.abPruning(true, heroes, radiant, dire, null, Double.MIN_VALUE, Double.MAX_VALUE);
				tree.alphaBeta(true, heroes, radiant, dire, null, Double.MIN_VALUE, Double.MAX_VALUE);
			}
			nextHeroId = tree.getNextHeroId();
			
		} catch (FileNotFoundException fnfe) {
			System.out.println("fnfe in Main - input: " + fnfe.getMessage());
		} catch (NumberFormatException nfe) {
			System.out.println("nfe in Main - input: " + nfe.getMessage());
		}
		
		System.out.println("1) Next hero: " + nextHeroId);
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("output.txt"));
			bw.write(Integer.toString(nextHeroId));
			bw.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println("fnfe in Main - output: " + fnfe.getMessage());
		} catch (IOException ioe) {
			System.out.println("ioe in Main - output: " + ioe.getMessage());
		}
	}
	
	public static int minimax(Vector<Hero> heroes) {
		return 0;
	}
	
	public static int abPruning(Vector<Hero> heroes) {
		return 0;
	}
	
	public static void print(Team radiant, Team dire, Vector<Hero> heroes) {
		System.out.println("Team Radiant: ");
		radiant.printMembers();
		System.out.println("Team Dire: ");
		dire.printMembers();
		System.out.println("Hero Pool: ");
		for (Hero h : heroes) {
			System.out.println(h.getId());
		}
	}
	
	//Comparator for heroes
	public static class HeroCmp implements Comparator<Hero>{
		@Override
		public int compare(Hero h1, Hero h2) {
			if (h1.getId() > h2.getId()) {
				return 1;
			}
			return -1;
		}
		
	}
	
	/********************Hero Class********************/
	
	
	/********************Tree Class********************/
	
	
	/********************Team Class********************/
	
}
