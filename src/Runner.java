import java.util.*;
import java.io.*;

public class Runner {

    public static void main(String[] args) throws IOException{
        // TODO: write the runner so that it follows the same format as the directions

        //get user input
        System.out.println("Enter input image: ");
        Scanner scan = new Scanner(System.in);
        String path = scan.nextLine();
        System.out.println("How many vertical seams to remove: ");
        int vert_seams = scan.nextInt();
        scan.nextLine();//eat empty line
        System.out.println("How many horizontal seams to remove: ");
        int horz_seams = scan.nextInt();
        scan.nextLine();
        System.out.println("Enter output image: ");
        String newimage = scan.nextLine();
        //get original picture
        Picture old = new Picture(path);

        //instantiate SeamCarver with old picture
        SeamCarver carver = new SeamCarver(old);
        //loop through num of vert and horizontal requests, making calls to both methods the proper amt of times
        for(int i = 0; i < vert_seams; i ++){
            int[] seamPath = carver.findVerticalSeam();
            carver.removeVerticalSeam(seamPath);
        }
        for(int i = 0; i < horz_seams; i ++){
            int[] seamPath = carver.findHorizontalSeam();
            carver.removeHorizontalSeam(seamPath);
        }

        //save new picture and show the two pics side by side
        carver.picture().save(newimage);
        old.show();
        carver.picture().show();

    }
}
