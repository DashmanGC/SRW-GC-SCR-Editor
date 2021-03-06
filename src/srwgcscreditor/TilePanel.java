/*
 * Copyright (C) 2014 Dashman
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * PreviewPanelMT.java
 *
 * Created on 22-mar-2013, 2:18:12
 */

package srwgcscreditor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import javax.swing.BorderFactory;

/**
 *
 * @author Jonatan
 */
public class TilePanel extends javax.swing.JPanel {
    private BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_INDEXED);
    private int zoom = 1;
    private boolean selected = false;
    private boolean flipH = false;
    private boolean flipV = false;
    private int position = 0;
    private IndexColorModel cm;

    /** Creates new form PreviewPanelMT */
    public TilePanel() {
        initComponents();
    }

    public TilePanel(byte[] pixels, IndexColorModel colorModel, int pos, int z){
        initComponents();
        zoom = z;
        position = pos;

        // Tiles are 8x8 pixels, but each pixel uses 4 bits (4bpp), so 1 byte is 2 pixels
        int width = 4;
        if (pixels.length == 64)
            width = 8;

        int height = 8;

        image = new BufferedImage(8, height, BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        int counter = 0;
        cm = colorModel;

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                int pixel = 0;

                if (width == 4){
                    pixel = (pixels[counter] >> 4) & 0xf;

                    image.setRGB(2*j, i, colorModel.getRGB(pixel));

                    pixel = pixels[counter] & 0xf;

                    image.setRGB(2*j + 1, i, colorModel.getRGB(pixel));
                }
                else{
                    pixel = (pixels[counter]) & 0xff;

                    image.setRGB(j, i, colorModel.getRGB(pixel));
                }

                counter++;
            }
        }

        this.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1 * zoom) );
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int width = 8*zoom;
        int height = 8*zoom;

        if(flipH && flipV)
            g.drawImage(image, 1*zoom + width, 1*zoom + height, -width, -height, this);
        else if (flipH)
            g.drawImage(image, 1*zoom + width, 1*zoom, -width, height, this);
        else if (flipV)
            g.drawImage(image, 1*zoom, 1*zoom + height, width, -height, this);
        else
            g.drawImage(image, 1*zoom, 1*zoom , width, height, this);
    }

    public void setSelected(boolean sel){
        selected = sel;
        
        if (selected)
            this.setBorder( BorderFactory.createLineBorder(Color.GREEN, 1 * zoom) );
        else
            this.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1 * zoom) );

        this.repaint();
    }

    public void setFlips(boolean fH, boolean fV){
        flipH = fH;
        flipV = fV;
    }

    public void flipVertically(boolean flip){
        flipV = flip;
        this.repaint();
    }

    public void flipHorizontally(boolean flip){
        flipH = flip;
        this.repaint();
    }

    public boolean isFlippedH(){
        return flipH;
    }

    public boolean isFlippedV(){
        return flipV;
    }

    public void setZoom(int z){
        zoom = z;

        if (selected)
            this.setBorder( BorderFactory.createLineBorder(Color.GREEN, 1 * zoom) );
        else
            this.setBorder( BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1 * zoom) );

        this.repaint();
    }

    public void setCModel(IndexColorModel colorModel){
        cm = colorModel;
        image = new BufferedImage(cm, image.getRaster(), false, null);
        //repaint();
    }

    public IndexColorModel getCModel(){
        return cm;
    }

    public BufferedImage getTileImage(){
        return image;
    }

    public void setTileImage(BufferedImage newImg){
        image = newImg;
    }

    public void clearTile(){
        BufferedImage clear_image = new BufferedImage(8, 8, BufferedImage.TYPE_BYTE_INDEXED, cm);

        setTileImage(clear_image);
    }

    public int getPosition(){
        return position;
    }

    public void setPosition(int newPos){
        position = newPos;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
