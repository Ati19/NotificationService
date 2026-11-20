import java.util.HashMap;
import java.util.Map;

/**
 * @author Atithi Kumari
 * @date 05/11/26
 * @description
 *
 * First Missing Positive
 *
 * Input: nums = [1,2,0]
 *
 * Output: 3
 *
 * Input: nums = [7,8,9,11,12]
 *
 * Output: 1
 *
 *
Input: nums = [3,4,-1,1]

Output: 2

 * Array may contain -ve nos, not necessarily increasing
 * Sort the nos and iterate
 *  0 1 2 - 3
 *
 */
public class TestHyre {
    public static void main(String[] args) {
        int[] nums= {1,2,0};
        System.out.println(firstPositiveNo(nums));
    }

    //1,2,0  , //7,8,9,11,12  , //3,4,-1,1
    private static int firstPositiveNo(int[] nums){
        Map<Integer,Integer> map=new HashMap<>();
        int n= nums.length;
        for(int i=0;i<n;i++){
            if(nums[i]>0) {
                if (map.containsKey(nums[i])) {
                    map.put(nums[i], map.getOrDefault(nums[i], 0) + 1);
                } else {
                    map.put(nums[i], 0);
                }
            }
        }
        for(int i=1;i<=n;i++){
            if(!map.containsKey(i))
                return i;
        }
        return -1;
    }
}
