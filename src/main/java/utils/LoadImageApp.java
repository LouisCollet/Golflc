package utils;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import javax.swing.*;

/**
 * This class demonstrates how to load an Image from an external file
 */
public class LoadImageApp extends Component
{

    BufferedImage img;

    public void paint(Graphics g)
    {
        g.drawImage(img, 0, 0, null);
    }

    public LoadImageApp() {
       try {
           img = ImageIO.read(new File("strawberry.jpg"));
           //BufferedImage image = ImageIO.read( new File( "rabbit.jpg" ) );
       } catch (IOException e) {
       }

    }

    @Override
    public Dimension getPreferredSize() {
        if (img == null) {
             return new Dimension(100,100);
        } else {
           return new Dimension(img.getWidth(null), img.getHeight(null));
       }
    }

    public static void main(String[] args)
    {
        /**
    * Display file formats supported by JAI (Java Advanced Imaging) on your platform.
    * e.g BMP, bmp, GIF, gif, jpeg, JPEG, jpg, JPG, png, PNG, wbmp, WBMP
    * @param args not used
    */


        String[] names = ImageIO.getWriterFormatNames();
      for ( String name: names )
         {
         System.out.print( name + ", " );
         }
///////////
        JFrame f = new JFrame("Load Image Sample");

        f.addWindowListener(new WindowAdapter(){
            @Override
                public void windowClosing(WindowEvent e) {
                    //System.exit(0);
                }
            });
        f.setLocation(200, 200);
        f.setSize(1800, 800); // fonctionne pas !!

        f.add(new LoadImageApp());
        f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         f.setVisible(true);
    }
}

/*/
 * JFrame frame = new JFrame();
     JLabel label = new JLabel(new ImageIcon(image));
     frame.getContentPane().add(label, BorderLayout.CENTER);
     frame.pack();
     frame.setVisible(true);
 *
 *
 *
 * url to BufferedImage
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
...
BufferedImage image = null;
try
   {
   image = ImageIO.read( url );
   }
catch ( IOException e )
   {
   LOG.info( "image missing" );
   }
 * */
