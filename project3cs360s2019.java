package project3cs360s2019;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import java.util.Scanner;
import java.io.BufferedWriter;
import java.util.NoSuchElementException;

public class project3cs360s2019 {
	// set true to display expected utility values
	public static boolean showValues = false;
	// set true to display number of iterations and differences from soln
	public static boolean compareFile = true;
	
	public static int gridSize = 0;
	public static int numObstacles = 0;
	public static int iterations = 0;
	public static double probCorrect = 0.7;
	public static double probIncorrect = 0.1;
	public static double gamma = 0.9;
	public static double epsilon = 0.1;
	
	public static int[][] grid;
	public static char[][] policyGrid;
	public static double[][] euGrid;
	public static double[][] meuGrid;
	public static Vector<Integer> obstaclesX = new Vector<>();
	public static Vector<Integer> obstaclesY = new Vector<>();
	
	public static void main(String[] args) {
		parse("input-5.txt");
		simulate();
		
		BufferedWriter bw;
		try {
			bw = new BufferedWriter(new FileWriter("output.txt"));		
			for (int i=0; i<gridSize; i++) {
				for (int j = 0; j<gridSize; j++) {
					char policy = policyGrid[j][i];
					bw.write(policy);
				}
				bw.newLine();
			}			
			bw.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println("fnfe in output: " + fnfe.getMessage());
		} catch (IOException ioe) {
			System.out.println("ioe in output: " + ioe.getMessage());
		}
		
		if (compareFile) {
			compareFiles("output-5.txt");
		}
	}
	
	public static void parse(String filename) {
		File file = new File(filename);
		try {
			Scanner scan = new Scanner(file);
			gridSize = scan.nextInt();
			numObstacles = scan.nextInt();
			
			grid = new int[gridSize][gridSize];
			policyGrid = new char[gridSize][gridSize];
			euGrid = new double[gridSize][gridSize];
			meuGrid = new double[gridSize][gridSize];

			// initialize grids' values
			for (int[] i : grid) {
				Arrays.fill(i, -1);
			}
			for (double[] d : euGrid) {
				Arrays.fill(d, 0);
			}
			for (double[] d : meuGrid) {
				Arrays.fill(d, 0);
			}
			for (char[] c : policyGrid) {
				Arrays.fill(c, '*');
			}
			
			// set obstacles
			for (int i=0; i<numObstacles; ++i) {
				String line = scan.next();
				String[] coord = line.split(",");
				int x = Integer.parseInt(coord[0]);
				int y = Integer.parseInt(coord[1]);
				
				 // obstacle 'o' is present at this square
				grid[x][y] = -101;
				obstaclesX.add(x);
				obstaclesY.add(y);
			}
			String line = scan.next();
			String[] coord = line.split(",");
			int x = Integer.parseInt(coord[0]);
			int y = Integer.parseInt(coord[1]);
			
			// destination point '.' is present at this square
			grid[x][y] = 99;
			meuGrid[x][y] = 99;
			policyGrid[x][y] = '.';
			
			scan.close();			
			
		} catch (FileNotFoundException fnfe) {
			System.out.println("fnfe in input: " + fnfe.getMessage());
		} catch (NumberFormatException nfe) {
			System.out.println("nfe in input: " + nfe.getMessage());
		} catch (NoSuchElementException nsee) {
			System.out.println("nsee in input: " + nsee.getMessage());
		} catch (IllegalStateException ise) {
			System.out.print("ise in input: " + ise.getMessage());
		}
	}

	public static void simulate() {
		boolean isConverged = true;
		while (true) {
			isConverged = true;
			iterations++;
			for (int i=0; i<gridSize; i++) {
				for (int j=0; j<gridSize; j++) {
					if (grid[j][i] != 99) {
						policyGrid[j][i] = calcEU(j, i);
						double delta = Math.abs(meuGrid[j][i] - euGrid[j][i]);
						if ((epsilon * (1.0 - gamma) / gamma) < delta) {
							isConverged = false;
						}
					}
				}
			}
			
			if (isConverged == true) {
				break;
			}

			for (int i=0; i<gridSize; i++) {
				for (int j=0; j<gridSize; j++) {
					euGrid[j][i] = meuGrid[j][i];
				}
			}
		}	
		
		for (int i=0; i<numObstacles; i++) {
			policyGrid[obstaclesX.elementAt(i)][obstaclesY.elementAt(i)] = 'o';
		}
		
		if (showValues) {
			displayValues();
		}
	}
	
	public static char calcEU(int x, int y) {
		double west = 0.0;
		double east = 0.0;
		double north = 0.0;
		double south = 0.0;
		
		// move WEST
		if (x-1 < 0) {
			west = euGrid[x][y];
		}
		else {
			west = euGrid[x-1][y];
		}

		// move EAST
		if (x+1 >= gridSize) {
			east = euGrid[x][y];
		}
		else {
			east = euGrid[x+1][y];
		}
		
		// move NORTH
		if (y-1 < 0) {
			north = euGrid[x][y];
		}
		else {
			north = euGrid[x][y-1];
		}
		
		// move SOUTH
		if (y+1 >= gridSize) {
			south = euGrid[x][y];
		}
		else {
			south = euGrid[x][y+1];
		}
		
		// calc each action's expected utility
		double westEU = probCorrect * west + probIncorrect * (east + north + south);
		double eastEU = probCorrect * east + probIncorrect * (west + north + south);
		double northEU = probCorrect * north + probIncorrect * (west + east + south);
		double southEU = probCorrect * south + probIncorrect * (west + east + north);
		
		// calc meu
		double meu = Math.max(Math.max(Math.max(Math.max(Double.NEGATIVE_INFINITY, westEU), eastEU), northEU), southEU);
		
		// get optimal action
		char action; 
		if (westEU == meu) {
			action = '<';
		}
		else if (eastEU == meu) {
			action = '>';
		}
		else if (northEU == meu) {
			action = '^';
		}
		else {
			action = 'v';	
		}
		
		meu = (meu * gamma) - 1;
		
		// if there is an obstacle at the square
		if (grid[x][y] == -101) {
			meu -= 100;
		}
		
		meuGrid[x][y] = meu;		
		return action;
	}
	
	public static void compareFiles(String filename) {
		File file = new File(filename);
		String soln = "";
		String mySoln = "";
		try {
			Scanner sc = new Scanner(file);
			while (sc.hasNextLine() ) {
				soln += sc.nextLine();
			}		
			sc.close();
		} catch (FileNotFoundException e) {
			System.out.println("Output File Not found");
		}
		
		for(int i = 0; i<gridSize; i++) {
			for (int j = 0; j<gridSize; j++) {
				mySoln += policyGrid[j][i];
			}
		}
		
		int diff = 0;
		if (soln.length() != mySoln.length()) {
			System.out.println("Compared solns have diff lengths!");
		}
		else {
			for (int i=0; i<soln.length(); i++) {
				if (soln.charAt(i) != mySoln.charAt(i)) {
					diff++;
				}
			}
		}
		System.out.println("Num of iterations: " + iterations);
		System.out.println("Num of differences: " + diff);
	}
	
	// display values rounded to 2 decimal places
	public static void displayValues() {
		for(int i=0; i<gridSize; i++) {
			for (int j=0; j<gridSize; j++) {
				double d = (double) Math.round(euGrid[j][i] * 100) / 100;
				System.out.print(d + "  ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void printGrid() {
		for (int i=0; i<gridSize; i++) {
			for (int j=0; j<gridSize; j++) {
				System.out.print(grid[i][j] + " ");
			}
			System.out.println();
		}
	}
}

