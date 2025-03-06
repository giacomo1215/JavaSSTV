package src.com.sstv;

import java.util.Scanner;

public class Program {
    public static Scanner in = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Insert file:");
        String file = in.nextLine();
        try {
            SSTVImageEncoder.encodeImage(file);
        }catch (Exception e){}
    }
}