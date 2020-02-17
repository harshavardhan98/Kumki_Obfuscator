package refactor.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class dictionary{

    public int charIndex;
    char[] characterSet;
    public String lastString;
    HashSet<String> set;
    HashMap<String,String> map;



    public String swap(String s,int l,int r){

        if(l<0||r>=s.length())
            return s;

        char[] ch=s.toCharArray();
        char temp = ch[l];
        ch[l] = ch[r];
        ch[r] = temp;


        s=new String(ch);
        return s;
    }

    public String reverseString(String s,int l,int r){

        if(l<0||r>=s.length())
            return s;

        char ch[]=s.toCharArray();

        while(l<r){
            char temp=ch[l];
            ch[l]=ch[r];
            ch[r]=temp;
            l++;
            r--;
        }

        for(int i=0;i<ch.length;i++)
            ;//System.out.print(ch[i]);


        String temp=new String(ch);
        return temp;
    }

    public int bsearch(String str, int l, int r, int key)
    {
        if(l<0||r>=str.length())
            return -1;

        char[] s=str.toCharArray();
        int index = -1;

        while (l <= r) {
            int mid = l + (r - l) / 2;
            //System.out.println(s[mid]+" "+l+" "+mid+" "+r);
            if (s[mid] <= key)
                r = mid - 1;
            else {
                l = mid + 1;
                if (index == -1 || s[index] >= s[mid])
                    index = mid;
            }
        }

        return index;
    }



    public String nextPermutation(String str)
    {
        char[] s=str.toCharArray();
        int len = s.length,i = len - 2;

        while (i >= 0 && i+1<s.length)
            if(s[i] >= s[i + 1])
                --i;
            else
                break;


        if (i < 0) {
            // next permutation is the first lexicographical permutation
            Arrays.sort(s);
            str=new String(s);
            //System.out.print(str);
            if(!checkIdentifier(str))
                System.out.println("");
            return str;
        }
        else {
            int index = bsearch(str, i + 1, len - 1, s[i]);
            str=swap(str,i, index);
            str=reverseString(str, i + 1,len - 1);
            if(!checkIdentifier(str))
                System.out.println("");
            return str;
        }
    }


    public dictionary(){
        charIndex=0;
        //characterSet=new char[]{'\u0B86','\u0B87'};
        characterSet=new char[]{'a','b','c'};
        lastString=new String(new char[]{characterSet[charIndex]});
        set=new HashSet<>();
        map=new HashMap<>();
    }



    public String getIdentifier(String identifierName){

        if(map.containsKey(identifierName))
            return map.get(identifierName);
        else{
            String temp=nextPermutation(lastString);
            if(set.contains(temp))
            {
                charIndex=(charIndex+1)%(characterSet.length);
                temp=temp+characterSet[charIndex];

                if(!checkIdentifier(temp))
                    System.out.println("");

                set.add(temp);
                map.put(identifierName,temp);
                lastString=temp;
                return temp;
            }
            else
            {
                map.put(identifierName,temp);
                set.add(temp);
                lastString=temp;
                return temp;
            }
        }
    }

    public void printDictionary(){
        System.out.println("Size of dictionary "+set.size());
        for(String s:set)
            System.out.println(s);
    }

    boolean checkIdentifier(String a){

        for(int i=0;i<a.length();i++)
            if(!Character.isJavaIdentifierPart(a.charAt(i)))
                return false;

        return true;
    }



}
