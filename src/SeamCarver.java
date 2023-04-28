public class SeamCarver {
    private int width;
    private int height;
    private Picture pictureCopy;

    // create a seam carver object based on the given picture
    public SeamCarver(Picture picture) {
        if (picture == null) {
            throw new IllegalArgumentException("argument to SeamCarver() is null\n");
        }

        pictureCopy = new Picture(picture);
        width = picture.width();
        height = picture.height();
    }

    // current picture
    public Picture picture() {
        return new Picture(pictureCopy);
    }

    // width of current picture
    public int width() {
        return width;
    }

    // height of current picture
    public int height() {
        return height;
    }

    // energy of pixel at column x and row y
    public double energy(int x, int y) {
        validateColumnIndex(x);
        validateRowIndex(y);

        // border pixels
        if (x == 0 || x == width -1 || y == 0 || y == height -1) {
            return 1000;
        }

        int up, down, left, right;
        up = pictureCopy.getRGB(x, y - 1);
        down = pictureCopy.getRGB(x, y + 1);
        left = pictureCopy.getRGB(x - 1, y);
        right = pictureCopy.getRGB(x + 1, y);
        double gradientY = gradient(up, down);
        double gradientX = gradient(left, right);

        return Math.sqrt(gradientX + gradientY);
    }

    private double gradient(int rgb1, int rgb2) {
        int r1 = (rgb1 >> 16) & 0xFF;
        int g1 = (rgb1 >>  8) & 0xFF;
        int b1 = (rgb1 >>  0) & 0xFF;
        int r2 = (rgb2 >> 16) & 0xFF;
        int g2 = (rgb2 >>  8) & 0xFF;
        int b2 = (rgb2 >>  0) & 0xFF;

        return Math.pow(r1 - r2, 2) + Math.pow(g1 - g2, 2)
                + Math.pow(b1 - b2, 2);
    }

    // sequence of indices for vertical seam
    public int[] findVerticalSeam() {
        double[][] energy = new double[height][width];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                energy[row][col] = energy(col, row);
            }
        }

        //TODO: Use Dynamic Programming to find the vertical seam
        int[] vSeam = new int[height];
        //instantiate arrays for tracking weights and paths as we loop thru
        double[][] tracker = new double[height][width];
        int[][] path = new int[height][width];



        for(int i = 0; i < width; i ++){//fill tracker with top rows because you have to start from one
            tracker[0][i] = energy[0][i];
            path[0][i] = -1;
        }
        //now fill it
        for(int i = 1; i < height; i ++){
            for(int j = 0; j < width; j ++){
                double left = Double.MAX_VALUE;//left and right have to be given dummy values until we check what index we're at
                double center = tracker[i-1][j];
                double right = Double.MAX_VALUE;

                if(j > 0) {//make sure we don't go out of bounds
                    left = tracker[i - 1][j - 1];
                }
                if(j < width -1){
                    right = tracker[i-1][j+1];
                }
                //get min of them
                double min = Math.min(Math.min(left, center), Math.min(center, right));
                //update tracker
                tracker[i][j] = energy[i][j] + min;
                //update path tracker
                if(min == left){
                    path[i][j] = j-1;
                }else if(min  == center){
                    path[i][j] = j;
                }else{
                    path[i][j] = j +1;
                }
            }
        }
        //now sort through the tracker to get the lowest end point
        double min = Double.MAX_VALUE;
        int minI = -1;
        for(int i = 0 ; i < width; i ++){
            if(tracker[height -1][i] < min){
                min = tracker[height -1][i];
                minI = i;
            }
        }
        //set last path
        vSeam[height - 1] = minI;
        //loop backwards through the path array and fill the seam
        for(int i = height - 2; i >= 0; i --){
            vSeam[i] = path[i+1][vSeam[i+1]];
        }

        return vSeam;
    }

    // sequence of indices for horizontal seam
    public int[] findHorizontalSeam() {
        double[][] energy = new double[width][height];
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                energy[col][row] = energy(col, row);
            }
        }
        
        int pic_width = energy.length;
        int pic_height = energy[0].length;
        double[][] tracker = new double[pic_width][pic_height];
        int[][] path = new int[pic_width][pic_height];

        //set up first col of values
        for (int j = 0; j < pic_height; j++) {
            tracker[0][j] = energy[0][j];
            path[0][j] = -1;
        }

        //loop through rows -> cols starting with an offset (already filled)
        for (int i = 1; i < pic_width; i++) {
            for (int j = 0; j < pic_height; j++) {
                double minEnergy = tracker[i - 1][j];
                path[i][j] = j;

                if (j > 0 && tracker[i - 1][j - 1] < minEnergy) {
                    minEnergy = tracker[i - 1][j - 1];
                    path[i][j] = j - 1;
                }

                if (j < pic_height - 1 && tracker[i - 1][j + 1] < minEnergy) {
                    minEnergy = tracker[i - 1][j + 1];
                    path[i][j] = j + 1;
                }

                tracker[i][j] = energy[i][j] + minEnergy;
            }
        }

        // Find the pixel with the minimum energy in the last row
        double minEnergy = Double.MAX_VALUE;
        int minIndex = -1;
        for (int j = 0; j < pic_height; j++) {
            if (tracker[pic_width - 1][j] < minEnergy) {
                minEnergy = tracker[pic_width - 1][j];
                minIndex = j;
            }
        }

        // Build the seam by following the edgeTo array
        int[] seam = new int[width];
        seam[width - 1] = minIndex;
        for (int i = width - 2; i >= 0; i--) {
            seam[i] = path[i + 1][seam[i + 1]];
        }

        return seam;


    }

    // remove vertical seam from current picture
    public void removeVerticalSeam(int[] seam) {
        if (seam == null) {
            throw new IllegalArgumentException("the argument to removeVerticalSeam() is null\n");
        }
        if (seam.length != height) {
            System.out.println("Seam length: " + seam.length);
            System.out.println("Supposed to be: " + height);
            throw new IllegalArgumentException("the length of seam not equal height\n");
        }
        validateSeam(seam);
        if (width <= 1) {
            throw new IllegalArgumentException("the width of the picture is less than or equal to 1\n");
        }

        Picture tmpPicture = new Picture(width - 1, height);
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width - 1; col++) {
                validateColumnIndex(seam[row]);
                if (col < seam[row]) {
                    tmpPicture.setRGB(col, row, pictureCopy.getRGB(col, row));
                } else {
                    tmpPicture.setRGB(col, row, pictureCopy.getRGB(col + 1, row));
                }
            }
        }
        pictureCopy = tmpPicture;
        width--;
    }

    // remove horizontal seam from current picture
    public void removeHorizontalSeam(int[] seam) {
        
        //I struggled with this implementation, and ended up using the provided "removeVerticalSeam" method
        // then stepped through the method with the debugger and figured out what the values needed to be
        if (seam == null) {//check seam isnt null
            throw new IllegalArgumentException("the argument to removeVerticalSeam() is null\n");
        }
        if (seam.length != width) {// make sure its the same length as the width bc we're removing a horizontal seam
            System.out.println("Seam length: " + seam.length);
            System.out.println("Supposed to be: " + height);
            throw new IllegalArgumentException("the length of seam not equal height\n");
        }
        validateSeam(seam);
        if (height <= 1) {
            throw new IllegalArgumentException("the width of the picture is less than or equal to 1\n");
        }
        //instantiate new picture to hold the corrections we make with one less height
        Picture tmpPicture = new Picture(width, height - 1);
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height - 1; row++) {
                validateRowIndex(seam[col]);
                if (row < seam[col]) {
                    tmpPicture.setRGB(col, row, pictureCopy.getRGB(col, row));
                } else {
                    tmpPicture.setRGB(col, row, pictureCopy.getRGB(col, row + 1));
                }
            }
        }
        pictureCopy = tmpPicture;
        height--;
    }

    // transpose the current pictureCopy
    private void transpose() {
        Picture tmpPicture = new Picture(height, width);
        for (int row = 0; row < width; row++) {
            for (int col = 0; col < height; col++) {
                tmpPicture.setRGB(col, row, pictureCopy.getRGB(row, col));
            }
        }
        pictureCopy = tmpPicture;
        int tmp = height;
        height = width;
        width = tmp;
    }

    // make sure column index is bewteen 0 and width - 1
    private void validateColumnIndex(int col) {
        if (col < 0 || col > width -1) {
            throw new IllegalArgumentException("colmun index is outside its prescribed range\n");
        }
    }

    // make sure row index is bewteen 0 and height - 1
    private void validateRowIndex(int row) {
        if (row < 0 || row > height -1) {
            throw new IllegalArgumentException("row index is outside its prescribed range\n");
        }
    }

    // make sure two adjacent entries differ within 1
    private void validateSeam(int[] seam) {
        for (int i = 0; i < seam.length - 1; i++) {
            if (Math.abs(seam[i] - seam[i + 1]) > 1) {
                throw new IllegalArgumentException("two adjacent entries differ by more than 1 in seam\n");
            }
        }
    }
}