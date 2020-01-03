/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package packagesystem;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.*;

/**
 *
 * @author frsy
 */
public class Utils {
    public static String formPackageCode(){
        StringBuilder sb = new StringBuilder();
        StringBuilder codesb = new StringBuilder();
        String code;
        String codeChars;
        for(int i='A';i<='Z';i++){
            sb.append((char)i);
        }
        for(int i=0;i<=9;i++){
            sb.append(i);
        }
        codeChars = sb.toString();
        for(int i=0;i<5;i++){
            Random rm = new Random();
            codesb.append(codeChars.charAt(rm.nextInt(codeChars.length())));
        }
        return codesb.toString();
    }
    
    public static String md5Encode(String str){
        String md5Str;
        try{
            MessageDigest md5=MessageDigest.getInstance("MD5");
            byte[] md5Bs=md5.digest(str.getBytes("utf-8"));
            md5Str=new BigInteger(1,md5Bs).toString(16).toUpperCase();
            return md5Str;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }
    }
    
    public static String fromPackagePosition(){
        int col,row;
        Random rm = new Random();
        col = rm.nextInt(10)+1;
        row = rm.nextInt(10)+1;
        return String.format("(%d,%d)",row,col);
    }
}
