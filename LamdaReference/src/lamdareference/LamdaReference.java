package lamdareference;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
public class LamdaReference 
{
    public static void main(String[] args) {
        String s1 = "Hello World Hello Everyone";
        String s2 = "new Hello World";
        String s3="P@ssw0rd";
 
        String betterS = betterS(s1, s2, LamdaReference::isBetter);
        System.out.println(betterS);
        boolean containAlphabet = containAlphabet(s3, LamdaReference::isL);
        System.out.println(containAlphabet);
    }

    public static boolean isBetter(String s1, String s2) 
    {
        return (s1.length() > s2.length());
    }

    public static String betterS(String s1, String s2, BiPredicate<String, String> p) 
    {
        if (isBetter(s1, s2)) {
            return s1;
        } else {
            return s2;
        }

    }

    public static boolean isL(String s1) 
    {
        char[] s1_char_arr = s1.toCharArray();
        for (char c : s1_char_arr) {
            if (Character.isDigit(c)) {
                return false;
            }
        }
        return true;
    }

    public static Boolean containAlphabet(String s1, Consumer<String>c) 
    {
        return isL(s1);
    }
}


