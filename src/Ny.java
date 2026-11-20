/**
 * @author Atithi Kumari
 * @date 16/10/26
 * @description
 */
public class Ny {

    interface A{
        String move();
    }
    interface B{
        String move();
    }
    class C implements A,B{

      public String move(){
        return "Avx";
      }
    }

    public static void main(String[] args) {
        int[] arr={1,2,0,1,0};
        System.out.println(canJump(arr));
    }

    public static boolean canJump(int[] nums){
        int goal = nums.length-1;
        int n=nums.length;
        for(int i=n-2;i>=0;i--){
            if(i+nums[i]>=goal)
                goal=i;
        }
        return goal==0;
    }

}
