/******************************************************************************
 *  Compilation:  javac Picture.java
 *  Execution:    java Picture filename.jpg
 *  Dependencies: none
 *
 ******************************************************************************/

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;

import java.net.URL;

import javax.imageio.ImageIO;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;



 *
 *  @author Robert Sedgewick
 *  @author Kevin Wayne
 */
public final class Picture implements ActionListener {
    private BufferedImage image;               // the rasterized image
    private JFrame frame;                      // on-screen view
    private String title;                      // window title (typically the name of the file)
    private boolean isOriginUpperLeft = true;  // location of origin
    private boolean isVisible = false;         // is the frame visible?
    private final int width, height;           // width and height

    /**
     * Creates a {@code width}-by-{@code height} picture, with {@code width} columns
     * and {@code height} rows, where each pixel is black.
     *
     * @param width the width of the picture
     * @param height the height of the picture
     * @throws IllegalArgumentException if {@code width} is negative or zero
     * @throws IllegalArgumentException if {@code height} is negative or zero
     */
    public Picture(int width, int height) {
        if (width  <= 0) throw new IllegalArgumentException("width must be positive");
        if (height <= 0) throw new IllegalArgumentException("height must be positive");
        this.width  = width;
        this.height = height;
        this.title = width + "-by-" + height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * Creates a new picture that is a deep copy of the argument picture.
     *
     * @param  picture the picture to copy
     * @throws IllegalArgumentException if {@code picture} is {@code null}
     */
    public Picture(Picture picture) {
        if (picture == null) throw new IllegalArgumentException("constructor argument is null");

        width  = picture.width();
        height = picture.height();
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        title = picture.title;
        isOriginUpperLeft = picture.isOriginUpperLeft;
        for (int col = 0; col < width(); col++)
            for (int row = 0; row < height(); row++)
                image.setRGB(col, row, picture.image.getRGB(col, row));
    }

    /**
     * Creates a picture by reading a JPEG, PNG, or GIF image from a file or URL.
     * The filetype extension must be {@code .jpg}, {@code .png}, or {@code .gif}.
     *
     * @param  filename the name of the file or URL
     * @throws IllegalArgumentException if cannot read image
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public Picture(String filename) {
        if (filename == null) throw new IllegalArgumentException("constructor argument is null");
        if (filename.length() == 0) throw new IllegalArgumentException("constructor argument is the empty string");

        title = filename;
        try {
            // try to read from file in working directory
            File file = new File(filename);
            if (file.isFile()) {
                title = file.getName();
                image = ImageIO.read(file);
            }

            else {

                // resource relative to .class file
                URL url = getClass().getResource(filename);

                // resource relative to classloader root
                if (url == null) {
                    url = getClass().getClassLoader().getResource(filename);
                }

                // or URL from web or jar
                if (url == null) {
                    url = new URL(filename);
                }

                image = ImageIO.read(url);
            }

            if (image == null) {
                throw new IllegalArgumentException("could not read image: " + filename);
            }

            width  = image.getWidth(null);
            height = image.getHeight(null);

            // convert to ARGB if necessary
            if (image.getType() != BufferedImage.TYPE_INT_ARGB) {
                BufferedImage imageARGB = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                imageARGB.createGraphics().drawImage(image, 0, 0, null);
                image = imageARGB;
            }
        }
        catch (IOException ioe) {
            throw new IllegalArgumentException("could not open image: " + filename, ioe);
        }
    }

    /**
     * Creates a picture by reading the image from a JPEG, PNG, or GIF file.
     *
     * @param file the file
     * @throws IllegalArgumentException if cannot read image
     * @throws IllegalArgumentException if {@code file} is {@code null}
     */
    public Picture(File file) {
        if (file == null) throw new IllegalArgumentException("constructor argument is null");

        try {
            BufferedImage image = ImageIO.read(file);

            width  = image.getWidth(null);
            height = image.getHeight(null);
            title = file.getName();

            // convert to ARGB
            if (image.getType() != BufferedImage.TYPE_INT_RGB) {
                BufferedImage imageARGB = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                imageARGB.createGraphics().drawImage(image, 0, 0, null);
                image = imageARGB;
            }
        }
        catch (IOException ioe) {
            throw new IllegalArgumentException("could not open file: " + file, ioe);
        }
    }

    /**
     * Returns a {@link JLabel} containing this picture, for embedding in a {@link JPanel},
     * {@link JFrame} or other GUI widget.
     *
     * @return the {@code JLabel}
     */
    public JLabel getJLabel() {
        if (image == null) return null;         // no image available
        ImageIcon icon = new ImageIcon(image);
        return new JLabel(icon);
    }

    /**
     * Sets the origin to be the upper left pixel. This is the default.
     */
    public void setOriginUpperLeft() {
        isOriginUpperLeft = true;
    }

    /**
     * Sets the origin to be the lower left pixel.
     */
    public void setOriginLowerLeft() {
        isOriginUpperLeft = false;
    }

    /**
     * Displays the picture in a window on the screen.
     */

    // getMenuShortcutKeyMask() deprecated in Java 10 but its replacement
    // getMenuShortcutKeyMaskEx() is not available in Java 8
    @SuppressWarnings("deprecation")
    public void show() {

        // create the GUI for viewing the image if needed
        if (frame == null) {
            frame = new JFrame();

            JMenuBar menuBar = new JMenuBar();
            JMenu menu = new JMenu("File");
            menuBar.add(menu);
            JMenuItem menuItem1 = new JMenuItem(" Save...   ");
            menuItem1.addActionListener(this);
            menuItem1.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
                    Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
            menu.add(menuItem1);
            frame.setJMenuBar(menuBar);



            frame.setContentPane(getJLabel());
            // f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setTitle(title);
            frame.setResizable(false);
            frame.pack();
        }

        // draw
        frame.setVisible(true);
        isVisible = true;
        frame.repaint();
    }

    /**
     * Hides the window on the screen.
     */
    public void hide() {
        if (frame != null) {
            isVisible = false;
            frame.setVisible(false);
        }
    }

    /**
     * Is the window containing the picture visible?
     * @return {@code true} if the picture is visible, and {@code false} otherwise
     */
    public boolean isVisible() {
        return isVisible;
    }

    /**
     * Returns the height of the picture.
     *
     * @return the height of the picture (in pixels)
     */
    public int height() {
        return height;
    }

    /**
     * Returns the width of the picture.
     *
     * @return the width of the picture (in pixels)
     */
    public int width() {
        return width;
    }

    private void validateRowIndex(int row) {
        if (row < 0 || row >= height())
            throw new IllegalArgumentException("row index must be between 0 and " + (height() - 1) + ": " + row);
    }

    private void validateColumnIndex(int col) {
        if (col < 0 || col >= width())
            throw new IllegalArgumentException("column index must be between 0 and " + (width() - 1) + ": " + col);
    }

    /**
     * Returns the color of pixel ({@code col}, {@code row}) as a {@link java.awt.Color}.
     *
     * @param col the column index
     * @param row the row index
     * @return the color of pixel ({@code col}, {@code row})
     * @throws IllegalArgumentException unless both {@code 0 <= col < width} and {@code 0 <= row < height}
     */
    public Color get(int col, int row) {
        validateColumnIndex(col);
        validateRowIndex(row);
        int argb = getRGB(col, row);
        return new Color(argb, true);
    }

    /**
     * Returns the color of pixel ({@code col}, {@code row}) as an {@code int}.
     * Using this method can be more efficient than {@link #get(int, int)} because
     * it does not create a {@code Color} object.
     *
     * @param col the column index
     * @param row the row index
     * @return the integer representation of the color of pixel ({@code col}, {@code row})
     * @throws IllegalArgumentException unless both {@code 0 <= col < width} and {@code 0 <= row < height}
     */
    public int getRGB(int col, int row) {
        validateColumnIndex(col);
        validateRowIndex(row);
        if (isOriginUpperLeft) return image.getRGB(col, row);
        else                   return image.getRGB(col, height - row - 1);
    }

    /**
     * Sets the color of pixel ({@code col}, {@code row}) to given color.
     *
     * @param col the column index
     * @param row the row index
     * @param color the color
     * @throws IllegalArgumentException unless both {@code 0 <= col < width} and {@code 0 <= row < height}
     * @throws IllegalArgumentException if {@code color} is {@code null}
     */
    public void set(int col, int row, Color color) {
        validateColumnIndex(col);
        validateRowIndex(row);
        if (color == null) throw new IllegalArgumentException("color argument is null");
        int rgb = color.getRGB();
        setRGB(col, row, rgb);
    }

    /**
     * Sets the color of pixel ({@code col}, {@code row}) to given color.
     *
     * @param col the column index
     * @param row the row index
     * @param rgb the integer representation of the color
     * @throws IllegalArgumentException unless both {@code 0 <= col < width} and {@code 0 <= row < height}
     */
    public void setRGB(int col, int row, int rgb) {
        validateColumnIndex(col);
        validateRowIndex(row);
        if (isOriginUpperLeft) image.setRGB(col, row, rgb);
        else                   image.setRGB(col, height - row - 1, rgb);
    }

    /**
     * Returns true if this picture is equal to the argument picture.
     *
     * @param other the other picture
     * @return {@code true} if this picture is the same dimension as {@code other}
     *         and if all pixels have the same color; {@code false} otherwise
     */
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != this.getClass()) return false;
        Picture that = (Picture) other;
        if (this.width()  != that.width())  return false;
        if (this.height() != that.height()) return false;
        for (int col = 0; col < width(); col++)
            for (int row = 0; row < height(); row++)
                if (this.getRGB(col, row) != that.getRGB(col, row)) return false;
        return true;
    }

    /**
     * Returns a string representation of this picture.
     * The result is a <code>width</code>-by-<code>height</code> matrix of pixels,
     * where the color of a pixel is represented using 6 hex digits to encode
     * the red, green, and blue components.
     *
     * @return a string representation of this picture
     */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(width +"-by-" + height + " picture (RGB values given in hex)\n");
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                int rgb;
                if (isOriginUpperLeft) rgb = image.getRGB(col, row);
                else                   rgb = image.getRGB(col, height - row - 1);
                sb.append(String.format("#%06X ", rgb & 0xFFFFFF));
            }
            sb.append("\n");
        }
        return sb.toString().trim();
    }

    /**
     * This operation is not supported because pictures are mutable.
     *
     * @return does not return a value
     * @throws UnsupportedOperationException if called
     */
    public int hashCode() {
        throw new UnsupportedOperationException("hashCode() is not supported because pictures are mutable");
    }

    // does this picture use transparency (i.e., alpha < 255 for some pixel)?
    private boolean hasAlpha() {
        for (int col = 0; col < width; col++) {
            for (int row = 0; row < height; row++) {
                int argb = image.getRGB(col, row);
                int alpha =  (argb >> 24) & 0xFF;
                if (alpha != 255) return true;
            }
        }
        return false;
    }

    /**
     * Saves the picture to a file in a supported file format
     * (typically JPEG, PNG, GIF TIFF, and BMP).
     * If the file format does not support transparency (such as JPEG
     * or BMP), it will be converted to be opaque (with purely
     * transparent pixels converted to black).
     *
     * @param filename the name of the file
     * @throws IllegalArgumentException if {@code filename} is {@code null}
     * @throws IllegalArgumentException if {@code filename} is the empty string
     */
    public void save(String filename) {
        if (filename == null) throw new IllegalArgumentException("argument to save() is null");
        if (filename.length() == 0) throw new IllegalArgumentException("argument to save() is the empty string");
        File file = new File(filename);
        save(file);
    }

    /**
     * Saves the picture to a file in a supported format
     * (typically JPEG, PNG, GIF TIFF, and BMP).
     *
     * @param  file the file
     * @throws IllegalArgumentException if {@code file} is {@code null}
     */
    public void save(File file) {
        if (file == null) throw new IllegalArgumentException("argument to save() is null");
        title = file.getName();

        String suffix = title.substring(title.lastIndexOf('.') + 1);
        if (!title.contains(".")) suffix = "";

        try {
            // for formats that support transparency (e.g., PNG and GIF)
            if (ImageIO.write(image, suffix, file)) return;

            // for formats that don't support transparency (e.g., JPG and BMP)
            // create BufferedImage in RGB format and use white background
            BufferedImage imageRGB = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            imageRGB.createGraphics().drawImage(image, 0, 0, Color.WHITE, null);
            if (ImageIO.write(imageRGB, suffix, file)) return;

            System.out.printf("Error: the filetype '%s' is not supported\n", suffix);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens a save dialog box when the user selects "Save As" from the menu.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        FileDialog chooser = new FileDialog(frame,
                "The filetype extension must be either .jpg or .png", FileDialog.SAVE);
        chooser.setVisible(true);
        if (chooser.getFile() != null) {
            save(chooser.getDirectory() + File.separator + chooser.getFile());
        }
    }

    /**
     * Unit tests this {@code Picture} data type.
     * Reads a picture specified by the command-line argument,
     * and shows it in a window on the screen.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        Picture picture = new Picture(args[0]);
        System.out.printf("%d-by-%d\n", picture.width(), picture.height());
        picture.show();
    }

}
