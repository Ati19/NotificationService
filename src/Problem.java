import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Queue;

/**
 * @author Atithi Kumari
 * @date 06/08/26
 * @description
 *
 * A 2D grid map of m rows and n columns is initially filled with water. 0
 * You may perform an addLand operation which turns the water at position (row, col) into land.
 * Given a list of positions to operate, count the number of islands after each addLand operation.
 * An island is surrounded by water and is formed by connecting adjacent lands horizontally or vertically.
 * You may assume all four edges of the grid are surrounded by water.
 *
 *
 *  1 0 0 0
 *  0 0 0 0
 *  0 0 1 0
 *
 */
public class Problem {

    static int[][] dir={{1,0},{-1,0},{0,1},{0,-1}};
   // static int[][] grid;


    //add and return the number of islands
    //m - rows, n -columns, 0-water , 1- islands
    public static void main(String[] args) {
        int m=3;
        int n=4;
        int[][] grid=new int[m][n];
        for(int[] r:grid){
            Arrays.fill(r,0);
        }
        System.out.println(addIslandBfs(grid,0,0));
        System.out.println(addIslandBfs(grid,2,3));
//        System.out.println(addIsland(grid,1,2));
    }

    //Approach 2
    //BFS - Implement using queue - save recursion stack
    public static int addIslandBfs(int[][] gridMap,int r, int c){
        gridMap[r][c]=1;
        int countOfIslands=0;
        boolean[][] vis=new boolean[gridMap.length][gridMap[0].length];
        for(int i=0;i<gridMap.length;i++){
            for(int j=0;j<gridMap[0].length;j++){
                if(gridMap[i][j]==1){
                    bfs(i,j,gridMap,vis);
                    countOfIslands++;
                }
            }
        }
        return countOfIslands;
    }
    private static void bfs(int r,int c,int[][] gridMap,boolean[][] vis){
        Queue<int[]> q=new ArrayDeque<>();
        q.add(new int[]{r,c});
        vis[r][c]=true;
        while(!q.isEmpty()){
            int[] cur=q.poll();
            for(int[] d:dir){
                int r0=cur[0]+d[0];
                int c0=cur[1]+d[1];
                if(r0>=0 && r0< gridMap.length && c0>=0 && c0<gridMap[0].length && !vis[r0][c0]){
                    q.add(new int[]{r0,c0});
                    vis[r0][c0]=true;
                }
            }

        }
    }

    //Approach 3
    //Union-keep doing union and Find- find the parent

    //Approach 1
    public static int addIsland(int[][] gridMap, int r, int c){
        gridMap[r][c]=1;
        int count=0;
        boolean[][] vis=new boolean[gridMap.length][gridMap[0].length];
        for(int i=0;i<gridMap.length;i++){
            for(int j=0;j<gridMap[0].length;j++){
                if(gridMap[i][j]==1){
                    dfs(i,j,gridMap,vis);
                    count++;
                }
            }
        }
        return count;
    }
    private static void dfs(int r,int c,int[][] gridMap,boolean[][] vis){
        if(r<0 || r>=gridMap.length ||c<0 || c>=gridMap[0].length || gridMap[r][c]==0 || vis[r][c]==true)
            return;
        vis[r][c]=true;
        for(int[] d:dir){
            dfs(r+d[0],c+d[1],gridMap,vis);
        }

    }
}
