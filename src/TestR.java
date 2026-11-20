import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Atithi Kumari
 * @date 19/10/26
 * @description
 *
 * Given a strings, find the length of the longest substring without duplicate characters.
 *  
 * Example 1:
 * Input: s = "abcabcbb"
 * Output: 3
 * Explanation: The answer is "abc", with the length of 3. Note that "bca" and "cab" are also correct answers.
 * Example 2:
 * Input: s = "bbbbb"
 * Output: 1
 * Explanation: The answer is "b", with the length of 1.
 * Example 3:
 * Input: s = "pwwkew"
 * Output: 3
 * Explanation: The answer is "wke", with the length of 3.
 * Notice that the answer must be a substring, "pwke" is a subsequence and not a substring.
 *
 * "abcabcbb"
 * i,j =0;
 * traverse j until n , max =
 * cur=substring(i,j)- j
 * char c=s.chaAt(j);
 * cur.contains(c)-> i++, else move j and update max
 * Time Complexity - O(n) ,
 *
 * aaaaaa
 * abca- substring , i++,-> j++
 * ab
 * abc -
 * bca - j++
 *
 * abcadcdd
 *  abc , i=0,j=2
 *  bca , i=1,j=3
 *  bcad , i=1,j=4
 *  adc, i=3,j=5 --> O(maxLengthofthesubtring)
 * */
public class TestR {
    public static void main(String[] args) {
        String input="abcadcdd";
        System.out.println(lengthOfLongSubstring(input));
    }
    public static int lengthOfLongSubstring(String input){
        int i=0,j=0,n=input.length(),max=0;
        if(n<=1) return n;
        Set<Character> seen=new HashSet<>();
        while(j<n){
            char cur=input.charAt(j);
            if(!seen.contains(cur)) {
                seen.add(cur);
                j++;
                max=Math.max(max,j-i);
            }
            //abcabcbb //0,a -1 , 0,a,1,b , 0,a ,1,b 2,c
            else{
                seen.remove(input.charAt(i));
                i++;
            }
        }
        return max;
    }


}
