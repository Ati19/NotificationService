import java.util.LinkedList;
import java.util.Queue;

/**
 * @author Atithi Kumari
 * @date 05/08/26
 * @description
 *
 *
 * A data center contains multiple servers arranged in a grid. Each server can be in one of three states:
 *
 * 0 → empty slot
 *
 * 1 → healthy server
 *
 * 2 → infected server
 *
 * Every minute, an infected server spreads the virus to its adjacent servers:
 *
 * * up
 *
 * * down
 *
 * * left
 *
 * * right
 *
 * Write a function to determine:
 *
 * * the minimum time required to infect all healthy servers
 *
 * * OR return -1 if some servers can never be infected.
 *
 *   Samples:
 *
 * - [[2,1,1],[1,1,0],[0,1,1]] → 4
 *
 *  //  2  1  1
 *      1  1  0
 *      0  1  1
 *
 * - [[2,1,1,0],[1,1,0,1],[0,1,1,1],[2,0,1,1]] → 6
 *
 * - [[2,1,1,0,1],[1,1,0,1,1],[0,1,1,0,1],[2,0,1,1,1],[1,1,0,1,2]] → 4
 *
 * - [[2,1,1],[0,1,1],[1,0,1]] → -1
 */
public class Test {
    static int[][] dir={{-1,0},{1,0},{0,-1},{0,1}};
    public static void main(String[] args) {
        int[][] grid={{2,1,1},{0,1,1},{1,0,1}};
        System.out.println(minimumTimeInfect(grid));

    }
    public static int minimumTimeInfect(int[][] grid){
        //find all the infected servers
        //find all the non-infected servers
        //travers from the infected ones in all directions
        //check at the end if all servers are infected or not, return the minimum time
        Queue<int[]> queue=new LinkedList<>();
        int healthyServers=0,minTime=0;
        for(int i=0;i<grid.length;i++){
            for(int j=0;j<grid[0].length;j++){
                if(grid[i][j]==1)
                    healthyServers++;
                if(grid[i][j]==2)
                    queue.add(new int[]{i,j});
            }
        }
        while(healthyServers>0 && !queue.isEmpty()){
            for(int i=0;i<queue.size();i++){
                int[] cur=queue.poll();
                for(int[] d:dir){
                    int r=cur[0]+d[0];
                    int c=cur[1]+d[1];
                    if(r>=0 && r<grid.length && c>=0 && c<grid[0].length && grid[r][c]==1){
                        queue.add(new int[]{r,c});
                        grid[r][c]=2;
                        healthyServers--;
                    }
                }
            }
            minTime++;

        }

       return healthyServers==0?minTime:-1;
    }



}
