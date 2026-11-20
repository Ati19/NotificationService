/**
 * @author Atithi Kumari
 * @date 05/11/26
 * @description
 *
 * Longest Common Prefix
 * Input: strs = ["flower","flow","flight"]
 *
 * Output: "fl"
 *
 * Continuos prefix
 *
 *
 */
public class TestHyre2 {
    public static void main(String[] args) {
        String[] strs= {"ab","a"};
        System.out.println(longestCommonPrefix(strs));
    }
    private static String longestCommonPrefix(String[] strs){
        if(strs.length==0)return "";
        String comm=strs[0];
        StringBuilder sb=new StringBuilder();
        for(int i=1;i< strs.length;i++){
            String curr=strs[i]; //flow
            sb.setLength(0);
            int j=0;
            while(j<comm.length() && j<curr.length()) {
                if (comm.charAt(j)==curr.charAt(j)){
                    sb.append(comm.charAt(j));
                    j++;
                }
                else{
                    break;
                }
            }
            comm=sb.toString();
        }
        return comm;
    }
}
