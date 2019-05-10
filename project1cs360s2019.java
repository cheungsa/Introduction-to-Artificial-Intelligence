import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Vector;

public class project1cs360s2019 {
	public static int n; // size of grid
	public static int c; // number of cameras
	public static int a; // number of animals
	public static String algorithm = "";
	public static int[][] grid;

	public static void main(String[] args) {
		parse("input.txt"); // change filename here

		if (algorithm.contains("dfs")) {
			Dfs dfs = new project1cs360s2019().new Dfs(c, grid);
			dfs.solveCamProb();
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter("output.txt"));
				bw.write(Integer.toString(dfs.getMaxCameras()));
				bw.close();
			} catch (FileNotFoundException fnfe) {
				System.out.println("fnfe in Main: " + fnfe.getMessage());
			} catch (IOException ioe) {
				System.out.println("ioe in Main: " + ioe.getMessage());
			}
		}
		else if (algorithm.contains("astar")) {
			Astar astar = new project1cs360s2019().new Astar(c, a, grid);
			astar.astarSolve();
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new FileWriter("output.txt"));
				bw.write(Integer.toString(astar.getMaxCameras()));
				bw.close();
			} catch (FileNotFoundException fnfe) {
				System.out.println("fnfe in Main: " + fnfe.getMessage());
			} catch (IOException ioe) {
				System.out.println("ioe in Main: " + ioe.getMessage());
			}
		}
	}
	
	public static void parse(String filename) {
		File file = new File(filename);
		try {
			Scanner scan = new Scanner(file);
			n = scan.nextInt();
			c = scan.nextInt();
			a = scan.nextInt();
			algorithm = scan.next();
			
			grid = new int[n][n];
			while (scan.hasNext()) {
				String line = scan.next();
				String[] coord = line.split(",");
				int x = Integer.parseInt(coord[0]);
				int y = Integer.parseInt(coord[1]);
				grid[x][y]++; // animal is present at this square
			}
			scan.close();
		} catch (FileNotFoundException fnfe) {
			System.out.println("fnfe in Parser: " + fnfe.getMessage());
		} catch (NumberFormatException nfe) {
			System.out.println("nfe in Parser: " + nfe.getMessage());
		} catch (IllegalStateException ise) {
			System.out.print("ise in Parser: " + ise.getMessage());
		}
	}
	
	public static void checkParse() {
		System.out.println("~~~~Checking parser~~~~" +
							"\nSize of grid: " + n +
							"\nNum of cameras: " + c +
							"\nNum of animals: " + a +
							"\nAlgorithm: " + algorithm);
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				System.out.print(grid[i][j]);
			}
			System.out.println();
		}
		System.out.println("~~~~~~~~~~~~~~~~~~~~~~~");
	}

/***********************************Point***********************************/	

	public class Point {
		public int x, y;
		
		public Point (int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		// Check whether in same row, column, or diagonal
		public boolean isIntersecting (int x, int y) {
			return ((this.x == x) || (this.y == y) || (Math.abs(this.x - x) == Math.abs(this.y - y)));
		}
	}	
	
/***********************************Dfs***********************************/
	public class Dfs {
		private int numCameras;
		private int maxWeight;
		private int[][] grid;
		Vector<Point> points;
		
		public Dfs(int numCameras, int[][] grid) {
			this.numCameras = numCameras;
			this.grid = grid;
			this.maxWeight = 0;
			points = new Vector<Point>();
		}
		
		public int getMaxCameras() {
			return maxWeight;
		}
		
		public boolean isValid(Vector<Point> points2, int x, int y) {
			for (Point p : points2) {
				if (p.isIntersecting(x, y)) {
					return false;
				}
			}
			return true;
		}
		
		public void solveCamProb() {
			// perform dfs on all the rows to make sure we get the best
			// out of all possible configurations
			for (int i = 0; i < grid.length; i++) {
				solveCamProbUtil(numCameras, 0, i);
			}
		}
		
		public void solveCamProbUtil(int numCameras, int currWeight, int currRow) {
			// BC: If all cameras are placed, find the max score among the configurations and return
			if (numCameras == 0) {
				if (currWeight > maxWeight) {
					maxWeight = currWeight;
				}
				return;
			}
			// BC: If the camera can't be placed in any row in this column, return
			if (currRow == grid.length) {
				return;
			}
			// Check whether the camera can be placed in all the columns in this row
			for (int i = 0; i < grid[currRow].length; ++i) {
				int x = currRow;
				int y = i;
				// Check whether the camera can be placed in grid[currRow][i]
				if (isValid(points, x, y)) {
					Point currPoint = new Point(currRow, i);
					
					// Keep track of camera placements with ArrayList
					points.add(currPoint);
					
					// Recur to place the rest of the cameras
					solveCamProbUtil(numCameras-1, currWeight + grid[currRow][i], currRow+1);
					
					// If placing the camera in grid[currRow][i] doesn't lead to a soln,
					// then remove the camera from the ArrayList
					points.remove(points.lastElement());
				}
				
				// Recur to place the current camera and the rest, if there are still rows and cols available 
				if (i == (grid.length - 1) && numCameras <= (grid.length - currRow - 1)) {
					solveCamProbUtil(numCameras, currWeight, currRow+1);
				}
			}
		}
	}
	
/***********************************Configuration (Astar)***********************************/	
	
	public class Astar {
		private int numCameras;
		private int maxCameras;
		private int numAnimals;
		private int[][] grid;
		PriorityQueue<Configuration> pq;
		Vector<Point> points;
		
		public class Configuration {
			Vector<Point> points;
			int f, g, h;	// for A* (may change later): g is the summed score of the configuration
							// 		   					  h is the score at current tile	
			int numCameras;
			
			public Configuration(Vector<Point> points, int f, int g, int h) {
				this.points = points;
				this.f = f;
				this.g = g;
				this.h = h;
				numCameras = points.size();
			}
		}

		public class CompareConfigs implements Comparator<Configuration> {
			@Override
			public int compare(Configuration c1, Configuration c2) {
				if (c1.f < c2.f) {
					return 1;
				}
				if (c1.f > c2.f) {
					return -1;
				}
				return 0;
			}
		}
		
		public Astar(int numCameras, int numAnimals, int[][] grid) {
			this.numCameras = numCameras;
			this.numAnimals = numAnimals;
			this.grid = grid;
			this.maxCameras = 0;
			
			// initialize the open pq and closed vector
			pq = new PriorityQueue<Configuration>(new CompareConfigs());
			points = new Vector<Point>();
			
			// place the starting point into the open pq
			Configuration config = new Configuration(points, 0, 0, 0);
			pq.add(config);
		}
		
		public int calcG(Vector<Point> points, int g) {
			for (int i = 0; i < points.size(); i++) {
				g += grid[points.get(i).x][points.get(i).y];
			}
			return g;
		}
		
		public int calcH(Point p) {
			return grid[p.x][p.y];
		}

		public int getMaxCameras() {
			return maxCameras;
		}
		
		public boolean isValidPoint(Vector<Point> points, Point p1) {
			for (Point p : points) {
				if (p1.isIntersecting(p.x, p.y)) {
					return false;
				}
			}
			return true;
		}

		public boolean isValidCoordinates(Vector<Point> points, int x, int y) {
			for (Point p : points) {
				if (p.isIntersecting(x, y)) {
					return false;
				}
			}
			return true;
		}
		
		public Vector<Point> createSuccessors(Vector<Point> points) {
			Vector<Point> successors = new Vector<Point>();
			for (int i = 0; i < grid.length; i++) {
				for (int j = 0; j < grid.length; j++) {
					if (isValidCoordinates(points, i, j)) {
						Point currP = new Point(i, j);
						successors.add(currP);
					}
				}
			}
			return successors;
		}
		
		public void astarSolve() {	
			// while the open pq is not empty
			while (!pq.isEmpty()) {
				// get the configuration with the largest f
				Configuration currConfig = pq.peek();
				// remove it from the open pq
				if (currConfig != null) {
					pq.remove();
				}
				points = currConfig.points;
				
				// the current configuration has placed the max number of cameras available
				if (currConfig.numCameras == numCameras) {
					maxCameras = calcG(points, 0);
					return;
				}
				
				// create the successors (aka points that do not intersect)
				Vector<Point> successors = createSuccessors(points);
				// calculate the total cost of each valid successor
				for (int i = 0; i < successors.size(); i++) {
					int g = calcG(points, 0) + 1; // number of animals detected by a configuration
					int h = calcH(successors.get(i)); // the score at the current tile in the grid
					int f = g + h;
					
					// if the successor is valid
					if (isValidPoint(points, successors.get(i))) {
						// add it to a new configuration
						Point currPoint = successors.get(i);
						Vector<Point> newPoints = new Vector<Point>(points);
						newPoints.add(currPoint);
						Configuration newConfig = new Configuration(newPoints, f, g, h);
						
						// add the new config to the closed pq
						pq.add(newConfig);
					}
				}
			}
		}
	}
	
}
