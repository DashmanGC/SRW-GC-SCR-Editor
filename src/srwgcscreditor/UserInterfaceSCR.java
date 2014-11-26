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
 * UserInterfaceSCR.java
 *
 * Created on 07-jul-2014, 1:04:40
 */

package srwgcscreditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Jonatan
 */
public class UserInterfaceSCR extends javax.swing.JFrame {

    String lastDirectory = ".";
    String lastDirectorySave = ".";
    ArrayList<IndexColorModel> palettes;
    int lastPalette = -1;
    boolean image_loaded = false;
    boolean scr_loaded = false;
    String title = "SRW GC SCR Editor by Dashman";

    // We use this to manage the contents of the palette list
    DefaultListModel modelListPal = new DefaultListModel();

    // We save the image data as byte arrays
    byte[][][] tilesBMfile;
    //byte[][][] tilesSCR;

    TilePanel lastClicked = null;
    TilePanel lastSCRclicked = null;
    int lastClickedTileIMG = -1;
    int selectedSCR = 0;
    int lastX = -1;
    int lastY = -1;

    boolean flipsAllowed = false;

    private class TileDataSCR{
        int position;   // Position of the tile in the image data
        boolean flipH;  // Flip Horizontally
        boolean flipV;  // Flip Vertically

        public TileDataSCR(){
            position = 0;
            flipH = false;
            flipV = false;
        }

        public TileDataSCR(int pos, boolean fH, boolean fV){
            position = pos;
            flipH = fH;
            flipV = fV;
        }
    }

    TileDataSCR[][] tileDataSCR;

    MouseListener listener = new MouseAdapter(){
        @Override
        public void mouseClicked(MouseEvent e){
            //System.out.println(listSelectionEvent.toString());
            //JList list = (JList) e.getSource();
            //String filename = "";

            TilePanel clicked = (TilePanel) e.getSource();

            //int x = clicked.getX();
            //int y = clicked.getY();

            //System.out.println("Clicked. X: " + x + " Y: " + y);

            if (lastClicked != null)
                lastClicked.setSelected(false);

            // It's possible to determine which tile was clicked with the coordinates
            /*int zoom = comboZoomImage.getSelectedIndex() + 1;
            int tile_x = x / (10*zoom);
            int tile_y = y / (10*zoom);

            int position = tile_x + (tile_y * tilesBM6[0].length);*/

            //lastClickedTileIMG = position;
            lastClickedTileIMG = clicked.getPosition();

            //clicked = (TilePanel) panelTilesIMG.getComponent(position);

            clicked.setSelected(true);
            lastClicked = clicked;

            if (scr_loaded){
                int scr_width = tileDataSCR[0].length;
                int scr_height = tileDataSCR.length;
                int x = selectedSCR % scr_width;
                int y = selectedSCR / scr_width;

                // De-select our previously chosen tile in the SCR edit window
                lastSCRclicked.setSelected(false);

                if (radioPickIndividualTile.isSelected()){  // Pick one tile
                    tileDataSCR[y][x].position = lastClickedTileIMG;

                    selectedSCR++;  // Select the next tile in the SCR edit window

                    if (selectedSCR == scr_width*scr_height)    // Or 0 if we reached the end
                        selectedSCR = 0;

                    lastSCRclicked.setTileImage(clicked.getTileImage());
                }
                else{   // Pick range of tiles
                    int zoom = comboZoomImage.getSelectedIndex() + 1;

                    if (lastX < 0){ // We haven't set a starting point yet
                        lastX = clicked.getX() / (10 * zoom);
                        lastY = clicked.getY() / (10 * zoom);
                    }
                    else{   // There's a starting point. Set the end point.
                        int newX = clicked.getX() / (10 * zoom);
                        int newY = clicked.getY() / (10 * zoom);

                        int offX = newX - lastX;
                        int offY = newY - lastY;

                        //System.out.println("Go from (" + lastX + ", " + lastY + ") to (" + newX + ", " + newY +").");

                        if (offX < 0 || offY < 0){
                            JOptionPane.showMessageDialog(null, "Sorry, we only allow reading a range of tiles\n" +
                                    "from left to right and top to bottom. Try again.",
                                "Whooops!", JOptionPane.WARNING_MESSAGE);
                        }
                        else{
                            // Determine the range of tiles selected between (lastX, lastY) and (newX, newY)
                            int topX = x + offX;
                            if (topX > scr_width)
                                topX = scr_width;

                            int topY = y + offY;
                            if (topY > scr_height)
                                topY = scr_height;

                            //System.out.println("Top X: " + topX + " Top Y: " + topY);
                            //System.out.println("Initial X: " + x + " Initial Y: " + y);

                            // Set the tiles in the SCR edit window to the ones selected in the image
                            int counterX = 0;
                            int counterY = 0;
                            int img_width = tilesBMfile[0].length;

                            // These loops are not very well controlled... but they work :P
                            for (int i = y; i <= topY; i++){
                                counterX = 0;

                                for (int j = x; j <= topX; j++){
                                    selectedSCR = (i*scr_width) + j;
                                    int tileIMG = (lastX + counterX) + (lastY + counterY)*img_width;

                                    counterX++;

                                    //if (selectedSCR < panelTilesSCR.getComponentCount()){
                                    if (j < scr_width && i < scr_height){
                                        ( (TilePanel) panelTilesSCR.getComponent(selectedSCR) ).setTileImage(
                                                ( (TilePanel) panelTilesIMG.getComponent(tileIMG) ).getTileImage() );

                                        tileDataSCR[i][j].position = tileIMG;

                                        //System.out.println("Updated tile X:" + j + " Y: " + i);
                                    }
                                    else
                                        selectedSCR = 0;
                                }

                                counterY++;
                            }
                            //System.out.println("Done.");

                            panelTilesSCR.repaint();
                        }

                        // Set the starting point to "not set"
                        lastX = -1;
                        lastY = -1;
                    }
                }

                lastSCRclicked = (TilePanel) panelTilesSCR.getComponent(selectedSCR);
                lastSCRclicked.setSelected(true);
            }

        }
    };


    MouseListener listenerSCR = new MouseAdapter(){
        @Override
        public void mouseClicked(MouseEvent e){
            TilePanel clicked = (TilePanel) e.getSource();

            if (lastSCRclicked != null)
                lastSCRclicked.setSelected(false);

            // It's possible to determine which tile was clicked with the coordinates
            /*int zoom = comboZoomImage.getSelectedIndex() + 1;
            int tile_x = x / (10*zoom);
            int tile_y = y / (10*zoom);

            int position = tile_x + (tile_y * tilesBM6[0].length);*/

            //lastClickedTileIMG = position;
            selectedSCR = clicked.getPosition();

            //clicked = (TilePanel) panelTilesIMG.getComponent(position);

            clicked.setSelected(true);
            lastSCRclicked = clicked;

            if (flipsAllowed){
                checkFlipH.setSelected(clicked.isFlippedH());
                checkFlipV.setSelected(clicked.isFlippedV());
            }
        }
    };


    /** Creates new form UserInterfaceSCR */
    public UserInterfaceSCR() {
        initComponents();
        
        this.setIconImage(new ImageIcon(getClass().getClassLoader().getResource("resources/icons/logo.png")).getImage());

        listPalettes.setModel(modelListPal);

        initPalettes();
        changePalette();

    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupTiles = new javax.swing.ButtonGroup();
        panelImageData = new javax.swing.JPanel();
        buttonLoad = new javax.swing.JButton();
        buttonSaveBMP = new javax.swing.JButton();
        comboZoomImage = new javax.swing.JComboBox();
        labelZoomImage = new javax.swing.JLabel();
        scrollImage = new javax.swing.JScrollPane();
        panelTilesIMG = new javax.swing.JPanel();
        radioPickIndividualTile = new javax.swing.JRadioButton();
        radioPickTileGroup = new javax.swing.JRadioButton();
        buttonLoadBM9 = new javax.swing.JButton();
        panelPalettes = new javax.swing.JPanel();
        panelColours = new javax.swing.JPanel();
        scrollPalettes = new javax.swing.JScrollPane();
        listPalettes = new javax.swing.JList();
        labelPalettes = new javax.swing.JLabel();
        buttonImportBM7 = new javax.swing.JButton();
        checkClearOnLoad = new javax.swing.JCheckBox();
        buttonImportBM10 = new javax.swing.JButton();
        panelSCRedit = new javax.swing.JPanel();
        labelSCRfile = new javax.swing.JLabel();
        scrollSCR = new javax.swing.JScrollPane();
        panelTilesSCR = new javax.swing.JPanel();
        labelWidth = new javax.swing.JLabel();
        textfieldWidth = new javax.swing.JTextField();
        labelHeight = new javax.swing.JLabel();
        textfieldHeight = new javax.swing.JTextField();
        buttonResize = new javax.swing.JButton();
        checkFlipH = new javax.swing.JCheckBox();
        checkFlipV = new javax.swing.JCheckBox();
        comboZoomSCR = new javax.swing.JComboBox();
        labelZoomSCR = new javax.swing.JLabel();
        buttonLoadSCR = new javax.swing.JButton();
        buttonSaveSCR = new javax.swing.JButton();
        buttonClear = new javax.swing.JButton();
        buttonSCRtoBMP = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SRW GC SCR Editor by Dashman");
        setResizable(false);

        panelImageData.setBorder(javax.swing.BorderFactory.createTitledBorder("Image data"));

        buttonLoad.setText("Load BM6");
        buttonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadActionPerformed(evt);
            }
        });

        buttonSaveBMP.setText("Save as BMP");
        buttonSaveBMP.setEnabled(false);
        buttonSaveBMP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveBMPActionPerformed(evt);
            }
        });

        comboZoomImage.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1x", "2x", "3x" }));
        comboZoomImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboZoomImageActionPerformed(evt);
            }
        });

        labelZoomImage.setFont(new java.awt.Font("Tahoma", 1, 11));
        labelZoomImage.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelZoomImage.setText("Zoom");

        panelTilesIMG.setPreferredSize(new java.awt.Dimension(500, 427));

        javax.swing.GroupLayout panelTilesIMGLayout = new javax.swing.GroupLayout(panelTilesIMG);
        panelTilesIMG.setLayout(panelTilesIMGLayout);
        panelTilesIMGLayout.setHorizontalGroup(
            panelTilesIMGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        panelTilesIMGLayout.setVerticalGroup(
            panelTilesIMGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 427, Short.MAX_VALUE)
        );

        scrollImage.setViewportView(panelTilesIMG);

        buttonGroupTiles.add(radioPickIndividualTile);
        radioPickIndividualTile.setFont(new java.awt.Font("Tahoma", 1, 11));
        radioPickIndividualTile.setSelected(true);
        radioPickIndividualTile.setText("Pick tiles individually");
        radioPickIndividualTile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPickIndividualTileActionPerformed(evt);
            }
        });

        buttonGroupTiles.add(radioPickTileGroup);
        radioPickTileGroup.setFont(new java.awt.Font("Tahoma", 1, 11));
        radioPickTileGroup.setText("Pick range of tiles");

        buttonLoadBM9.setText("Load BM9");
        buttonLoadBM9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadBM9ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelImageDataLayout = new javax.swing.GroupLayout(panelImageData);
        panelImageData.setLayout(panelImageDataLayout);
        panelImageDataLayout.setHorizontalGroup(
            panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelImageDataLayout.createSequentialGroup()
                .addGroup(panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelImageDataLayout.createSequentialGroup()
                        .addGap(113, 113, 113)
                        .addComponent(radioPickIndividualTile)
                        .addGap(18, 18, 18)
                        .addComponent(radioPickTileGroup))
                    .addGroup(panelImageDataLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(scrollImage, javax.swing.GroupLayout.DEFAULT_SIZE, 500, Short.MAX_VALUE)
                            .addGroup(panelImageDataLayout.createSequentialGroup()
                                .addComponent(buttonLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(buttonLoadBM9, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(buttonSaveBMP, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(85, 85, 85)
                                .addComponent(labelZoomImage, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboZoomImage, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap())
        );
        panelImageDataLayout.setVerticalGroup(
            panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelImageDataLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonLoad)
                    .addComponent(comboZoomImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelZoomImage)
                    .addComponent(buttonSaveBMP)
                    .addComponent(buttonLoadBM9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollImage, javax.swing.GroupLayout.DEFAULT_SIZE, 429, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioPickIndividualTile)
                    .addComponent(radioPickTileGroup))
                .addContainerGap())
        );

        panelPalettes.setBorder(javax.swing.BorderFactory.createTitledBorder("Palettes"));

        panelColours.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panelColoursLayout = new javax.swing.GroupLayout(panelColours);
        panelColours.setLayout(panelColoursLayout);
        panelColoursLayout.setHorizontalGroup(
            panelColoursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 443, Short.MAX_VALUE)
        );
        panelColoursLayout.setVerticalGroup(
            panelColoursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 41, Short.MAX_VALUE)
        );

        listPalettes.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        listPalettes.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                listPalettesMouseClicked(evt);
            }
        });
        listPalettes.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                listPalettesKeyReleased(evt);
            }
        });
        scrollPalettes.setViewportView(listPalettes);

        labelPalettes.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelPalettes.setText("Palettes available:");

        buttonImportBM7.setText("Import BM7");
        buttonImportBM7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonImportBM7ActionPerformed(evt);
            }
        });

        checkClearOnLoad.setFont(new java.awt.Font("Tahoma", 1, 11));
        checkClearOnLoad.setSelected(true);
        checkClearOnLoad.setText("Clear on load");

        buttonImportBM10.setText("Import BM10");
        buttonImportBM10.setEnabled(false);
        buttonImportBM10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonImportBM10ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelPalettesLayout = new javax.swing.GroupLayout(panelPalettes);
        panelPalettes.setLayout(panelPalettesLayout);
        panelPalettesLayout.setHorizontalGroup(
            panelPalettesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelPalettesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelPalettesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelColours, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelPalettesLayout.createSequentialGroup()
                        .addComponent(scrollPalettes, javax.swing.GroupLayout.PREFERRED_SIZE, 317, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(panelPalettesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkClearOnLoad, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(buttonImportBM7, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
                            .addComponent(buttonImportBM10, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)))
                    .addComponent(labelPalettes, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        panelPalettesLayout.setVerticalGroup(
            panelPalettesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPalettesLayout.createSequentialGroup()
                .addComponent(panelColours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelPalettes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelPalettesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(scrollPalettes, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelPalettesLayout.createSequentialGroup()
                        .addComponent(buttonImportBM7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonImportBM10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(checkClearOnLoad)))
                .addContainerGap())
        );

        panelSCRedit.setBorder(javax.swing.BorderFactory.createTitledBorder("SCR Edit"));

        labelSCRfile.setFont(new java.awt.Font("Tahoma", 1, 11));
        labelSCRfile.setText("- no file loaded -");
        labelSCRfile.setEnabled(false);

        javax.swing.GroupLayout panelTilesSCRLayout = new javax.swing.GroupLayout(panelTilesSCR);
        panelTilesSCR.setLayout(panelTilesSCRLayout);
        panelTilesSCRLayout.setHorizontalGroup(
            panelTilesSCRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 339, Short.MAX_VALUE)
        );
        panelTilesSCRLayout.setVerticalGroup(
            panelTilesSCRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 187, Short.MAX_VALUE)
        );

        scrollSCR.setViewportView(panelTilesSCR);

        labelWidth.setFont(new java.awt.Font("Tahoma", 1, 11));
        labelWidth.setText("Width:");
        labelWidth.setEnabled(false);

        textfieldWidth.setEnabled(false);
        textfieldWidth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textfieldWidthKeyTyped(evt);
            }
        });

        labelHeight.setFont(new java.awt.Font("Tahoma", 1, 11));
        labelHeight.setText("Height:");
        labelHeight.setEnabled(false);

        textfieldHeight.setEnabled(false);
        textfieldHeight.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textfieldHeightKeyTyped(evt);
            }
        });

        buttonResize.setText("Resize");
        buttonResize.setEnabled(false);
        buttonResize.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonResizeActionPerformed(evt);
            }
        });

        checkFlipH.setFont(new java.awt.Font("Tahoma", 1, 11));
        checkFlipH.setText("Flip Tile Horizontally");
        checkFlipH.setEnabled(false);
        checkFlipH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFlipHActionPerformed(evt);
            }
        });

        checkFlipV.setFont(new java.awt.Font("Tahoma", 1, 11));
        checkFlipV.setText("Flip Tile Vertically");
        checkFlipV.setEnabled(false);
        checkFlipV.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFlipVActionPerformed(evt);
            }
        });

        comboZoomSCR.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "1x", "2x", "3x" }));
        comboZoomSCR.setEnabled(false);
        comboZoomSCR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboZoomSCRActionPerformed(evt);
            }
        });

        labelZoomSCR.setFont(new java.awt.Font("Tahoma", 1, 11));
        labelZoomSCR.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelZoomSCR.setText("Zoom");
        labelZoomSCR.setEnabled(false);

        buttonLoadSCR.setText("Load SCR");
        buttonLoadSCR.setEnabled(false);
        buttonLoadSCR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadSCRActionPerformed(evt);
            }
        });

        buttonSaveSCR.setText("Save SCR");
        buttonSaveSCR.setEnabled(false);
        buttonSaveSCR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSaveSCRActionPerformed(evt);
            }
        });

        buttonClear.setText("Clear Tiles");
        buttonClear.setEnabled(false);
        buttonClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonClearActionPerformed(evt);
            }
        });

        buttonSCRtoBMP.setText("Save as BMP");
        buttonSCRtoBMP.setEnabled(false);
        buttonSCRtoBMP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonSCRtoBMPActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelSCReditLayout = new javax.swing.GroupLayout(panelSCRedit);
        panelSCRedit.setLayout(panelSCReditLayout);
        panelSCReditLayout.setHorizontalGroup(
            panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSCReditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(scrollSCR, javax.swing.GroupLayout.PREFERRED_SIZE, 341, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(panelSCReditLayout.createSequentialGroup()
                        .addComponent(labelSCRfile, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                        .addComponent(labelZoomSCR, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(comboZoomSCR, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(panelSCReditLayout.createSequentialGroup()
                        .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(checkFlipH, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, panelSCReditLayout.createSequentialGroup()
                                .addComponent(labelWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 46, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textfieldWidth, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(labelHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(panelSCReditLayout.createSequentialGroup()
                                .addComponent(textfieldHeight, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(buttonResize, javax.swing.GroupLayout.DEFAULT_SIZE, 103, Short.MAX_VALUE))
                            .addComponent(checkFlipV, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(buttonSCRtoBMP, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonClear, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonSaveSCR, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonLoadSCR, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelSCReditLayout.setVerticalGroup(
            panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSCReditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelSCRfile)
                    .addComponent(comboZoomSCR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelZoomSCR))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(panelSCReditLayout.createSequentialGroup()
                        .addComponent(buttonLoadSCR)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonSaveSCR)
                        .addGap(40, 40, 40)
                        .addComponent(buttonSCRtoBMP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonClear))
                    .addComponent(scrollSCR, javax.swing.GroupLayout.PREFERRED_SIZE, 189, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(checkFlipH)
                    .addComponent(checkFlipV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelWidth)
                    .addComponent(textfieldWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelHeight)
                    .addComponent(textfieldHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonResize))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelImageData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(panelSCRedit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelPalettes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelPalettes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelSCRedit, 0, 308, Short.MAX_VALUE))
                    .addComponent(panelImageData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void buttonImportBM7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonImportBM7ActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Load BM7 file");
        chooser.setFileFilter(new FileNameExtensionFilter("BM7 file", "BM7"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            openPalette(chooser.getSelectedFile().getAbsolutePath());
            listPalettes.setSelectedIndex(modelListPal.size() - 1);
            changePalette();
            
            listPalettes.requestFocusInWindow();

            lastDirectory = chooser.getSelectedFile().getPath();
        }
    }//GEN-LAST:event_buttonImportBM7ActionPerformed

    private void listPalettesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listPalettesMouseClicked
        // TODO add your handling code here:
        changePalette();
    }//GEN-LAST:event_listPalettesMouseClicked

    private void listPalettesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listPalettesKeyReleased
        // TODO add your handling code here:
        changePalette();
    }//GEN-LAST:event_listPalettesKeyReleased

    private void buttonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Load BM6 file");
        chooser.setFileFilter(new FileNameExtensionFilter("BM6 file", "BM6"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            lastClickedTileIMG = -1;
            lastClicked = null;
            
            //openBM6(chooser.getSelectedFile().getAbsolutePath());
            openBMfile(chooser.getSelectedFile().getAbsolutePath(), 6);

            lastDirectory = chooser.getSelectedFile().getPath();

            this.setTitle(chooser.getSelectedFile().getName() + " - " + title);
        }
    }//GEN-LAST:event_buttonLoadActionPerformed

    private void comboZoomImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboZoomImageActionPerformed
        // TODO add your handling code here:
        //displayTiles();
        if (image_loaded)
            changeZoom(comboZoomImage.getSelectedIndex() + 1);
    }//GEN-LAST:event_comboZoomImageActionPerformed

    private void buttonSaveBMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveBMPActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectorySave));
        chooser.setDialogTitle("Save BMP file");
        chooser.setFileFilter(new FileNameExtensionFilter("BMP file", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            saveBMP(chooser.getSelectedFile().getAbsolutePath());

            lastDirectorySave = chooser.getSelectedFile().getPath();
        }
    }//GEN-LAST:event_buttonSaveBMPActionPerformed

    private void buttonLoadSCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadSCRActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Load SCR file");
        chooser.setFileFilter(new FileNameExtensionFilter("SCR file", "SCR"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            selectedSCR = 0;
            lastSCRclicked = null;

            openSCR(chooser.getSelectedFile().getAbsolutePath());

            lastDirectory = chooser.getSelectedFile().getPath();

            labelSCRfile.setText(chooser.getSelectedFile().getName());
        }
    }//GEN-LAST:event_buttonLoadSCRActionPerformed

    private void comboZoomSCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboZoomSCRActionPerformed
        // TODO add your handling code here:
        if(scr_loaded)
            changeZoomSCR(comboZoomSCR.getSelectedIndex() + 1);
    }//GEN-LAST:event_comboZoomSCRActionPerformed

    private void checkFlipHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFlipHActionPerformed
        // TODO add your handling code here:
        lastSCRclicked.flipHorizontally(checkFlipH.isSelected());

        int x = selectedSCR % tileDataSCR[0].length;
        int y = selectedSCR / tileDataSCR[0].length;

        tileDataSCR[y][x].flipH = checkFlipH.isSelected();
    }//GEN-LAST:event_checkFlipHActionPerformed

    private void checkFlipVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFlipVActionPerformed
        // TODO add your handling code here:
        lastSCRclicked.flipVertically(checkFlipV.isSelected());

        int x = selectedSCR % tileDataSCR[0].length;
        int y = selectedSCR / tileDataSCR[0].length;

        tileDataSCR[y][x].flipV = checkFlipV.isSelected();
    }//GEN-LAST:event_checkFlipVActionPerformed

    private void buttonSCRtoBMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSCRtoBMPActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectorySave));
        chooser.setDialogTitle("Save BMP file");
        chooser.setFileFilter(new FileNameExtensionFilter("BMP file", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            saveSCRtoBMP(chooser.getSelectedFile().getAbsolutePath());

            lastDirectorySave = chooser.getSelectedFile().getPath();
        }
    }//GEN-LAST:event_buttonSCRtoBMPActionPerformed

    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearActionPerformed
        // TODO add your handling code here:
        clearTilesSCR();
    }//GEN-LAST:event_buttonClearActionPerformed

    private void textfieldWidthKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textfieldWidthKeyTyped
        // TODO add your handling code here:
        if(evt.getKeyChar()<'0' || evt.getKeyChar()>'9') // only numbers
            evt.consume();
    }//GEN-LAST:event_textfieldWidthKeyTyped

    private void textfieldHeightKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textfieldHeightKeyTyped
        // TODO add your handling code here:
        if(evt.getKeyChar()<'0' || evt.getKeyChar()>'9') // only numbers
            evt.consume();
    }//GEN-LAST:event_textfieldHeightKeyTyped

    private void buttonResizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResizeActionPerformed
        // TODO add your handling code here:
        resizeSCR();
    }//GEN-LAST:event_buttonResizeActionPerformed

    private void buttonSaveSCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveSCRActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectorySave));
        chooser.setDialogTitle("Save SCR file");
        chooser.setFileFilter(new FileNameExtensionFilter("SCR file", "SCR"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            saveSCR(chooser.getSelectedFile().getAbsolutePath());

            lastDirectorySave = chooser.getSelectedFile().getPath();
        }
    }//GEN-LAST:event_buttonSaveSCRActionPerformed

    private void radioPickIndividualTileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioPickIndividualTileActionPerformed
        // TODO add your handling code here:
        if (radioPickIndividualTile.isSelected()){
            lastX = -1; // Reset the starting point for the range of tiles
            lastY = -1;
        }
    }//GEN-LAST:event_radioPickIndividualTileActionPerformed

    private void buttonLoadBM9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadBM9ActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Load BM9 file");
        chooser.setFileFilter(new FileNameExtensionFilter("BM9 file", "BM9"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            lastClickedTileIMG = -1;
            lastClicked = null;

            openBMfile(chooser.getSelectedFile().getAbsolutePath(), 9);

            lastDirectory = chooser.getSelectedFile().getPath();

            this.setTitle(chooser.getSelectedFile().getName() + " - " + title);
        }
    }//GEN-LAST:event_buttonLoadBM9ActionPerformed

    private void buttonImportBM10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonImportBM10ActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Load BM10 file");
        chooser.setFileFilter(new FileNameExtensionFilter("BM10 file", "BM10"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            openPalette(chooser.getSelectedFile().getAbsolutePath());
            listPalettes.setSelectedIndex(modelListPal.size() - 1);
            changePalette();

            listPalettes.requestFocusInWindow();

            lastDirectory = chooser.getSelectedFile().getPath();
        }
    }//GEN-LAST:event_buttonImportBM10ActionPerformed

    /**
    * @param args the command line arguments
    */
    /*public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UserInterfaceSCR().setVisible(true);
            }
        });
    }*/

    public void openPalette(String filename){
        try{
            // Read the contents of the file as bytes
            RandomAccessFile f = new RandomAccessFile(filename, "r");

            boolean valid = true;

            int pal_size = 16;
            int type = 7;
            int bpp = 4;
            if (!buttonImportBM7.isEnabled()){
                pal_size = 256;
                type = 10;
                bpp = 8;
            }

            if (type == 7){
                // Make sure it's a BM7 file
                if (f.length() != 96)
                    valid = false;
            }
            else{
                // Make sure it's a BM10 file
                if (f.length() != 1056)
                    valid = false;
            }

            byte[] header = new byte[32];

            f.read(header);

            if (header[0] != 'B' || header[1] != 'M' || header[2] != 'P' || header[3] != type)
                valid = false;

            if (!valid){
                f.close();

                JOptionPane.showMessageDialog(null, "Wrong format for palette file:\n" + filename,
                    "Error", JOptionPane.ERROR_MESSAGE);

                return;
            }

            byte[] c_data = new byte[pal_size * 4];

            f.read(c_data);

            f.close();

            // Make a ColorModel with the colour data
            byte[] r = new byte[pal_size];
            byte[] g = new byte[pal_size];
            byte[] b = new byte[pal_size];
            byte[] a = new byte[pal_size];

            int counter = 0;

            for (int i = 0; i < pal_size*4; i+= 4){
                r[counter] = c_data[i];
                g[counter] = c_data[i+1];
                b[counter] = c_data[i+2];
                a[counter] = c_data[i+3];
                //a[counter] = (byte) 0x7f;

                counter++;
            }

            // Add the ColorModel to the list
            palettes.add(new IndexColorModel(bpp, pal_size, r, g, b, a));

            // Update the palette list in the UI
            int pos = filename.lastIndexOf('/');
            if (pos < 0)
                pos = filename.lastIndexOf('\\');
            String name = filename.substring(pos + 1);

            modelListPal.addElement(name);

        } catch(IOException ex){
            System.err.println("ERROR: Couldn't read palette file!!");
        }
    }


    // Clear the list of palettes and sets the default palette as the one selected
    public void initPalettes(){
        palettes = new ArrayList<IndexColorModel>();

        int pal_size = 16;
        int extra_colour = 15;
        int bpp = 4;
        int dimX = 20;
        int dimY = 20;
        int offX = 22;
        int offY = 11;
        int extraX = 5;
        int extraY = 2;
        int colPerRow = 16;
        if (!buttonImportBM7.isEnabled()){
            bpp = 8;
            pal_size = 256;
            extra_colour = 0;
            dimX = 5;
            dimY = 5;
            offX = 30;
            offY = 9;
            extraX = 1;
            colPerRow = 64;
        }

        // Prepare the Default palette
        byte[] r = new byte[pal_size];
        byte[] g = new byte[pal_size];
        byte[] b = new byte[pal_size];
        byte[] a = new byte[pal_size];

        int posX = (int) panelColours.getBounds().getX();
        int posY = (int) panelColours.getBounds().getY();
        panelColours.removeAll();
        int x = 0;
        int y = 0;

        for (int i = 0; i < pal_size; i++){
            r[i] = (byte) ( extra_colour + (256 / pal_size) * i );
            g[i] = (byte) ( extra_colour + (256 / pal_size) * i );
            b[i] = (byte) ( extra_colour + (256 / pal_size) * i );
            a[i] = (byte) 0xff;

            JPanel p = new JPanel();

            p.setBounds(x*(dimX + extraX) + offX, y*(dimY + extraY) + offY, dimX, dimY);
            p.setBackground(new Color(r[i] & 0xff, g[i] & 0xff, b[i] & 0xff));
            panelColours.add(p);

            x++;
            if (x == colPerRow){
                x = 0;
                y++;
            }
        }

        //panelColours.setBounds(posX, posY, 447, 50);
        //panelColours.revalidate();
        //panelColours.repaint();

        palettes.add(new IndexColorModel(bpp, pal_size, r, g, b, a));

        modelListPal.clear();
        modelListPal.addElement("<Default palette>");

        listPalettes.setSelectedIndex(0);
    }


    // Changes the palette to the currently selected one in the list
    public void changePalette(){
        // If we're on the same index we were before, do nothing
        if (lastPalette == listPalettes.getSelectedIndex())
            return;

        int pal_size = 16;
        if (!buttonImportBM7.isEnabled())
            pal_size = 256;

        //System.out.println("Changing the palette");
        
        lastPalette = listPalettes.getSelectedIndex();

        IndexColorModel cm = palettes.get(lastPalette);

        byte[] r = new byte[pal_size];
        byte[] g = new byte[pal_size];
        byte[] b = new byte[pal_size];
        //byte[] a = new byte[16];

        cm.getReds(r);
        cm.getBlues(b);
        cm.getGreens(g);
        //cm.getAlphas(a);

        // ***** REWRITE THIS TO ALLOW 256 COLOURS!!!!!!!
        //
        // Set the colours of the panels in the UI
        /*panelColour1.setBackground(new Color(r[0] & 0xff, g[0] & 0xff, b[0] & 0xff));
        panelColour2.setBackground(new Color(r[1] & 0xff, g[1] & 0xff, b[1] & 0xff));
        panelColour3.setBackground(new Color(r[2] & 0xff, g[2] & 0xff, b[2] & 0xff));
        panelColour4.setBackground(new Color(r[3] & 0xff, g[3] & 0xff, b[3] & 0xff));
        panelColour5.setBackground(new Color(r[4] & 0xff, g[4] & 0xff, b[4] & 0xff));
        panelColour6.setBackground(new Color(r[5] & 0xff, g[5] & 0xff, b[5] & 0xff));
        panelColour7.setBackground(new Color(r[6] & 0xff, g[6] & 0xff, b[6] & 0xff));
        panelColour8.setBackground(new Color(r[7] & 0xff, g[7] & 0xff, b[7] & 0xff));
        panelColour9.setBackground(new Color(r[8] & 0xff, g[8] & 0xff, b[8] & 0xff));
        panelColour10.setBackground(new Color(r[9] & 0xff, g[9] & 0xff, b[9] & 0xff));
        panelColour11.setBackground(new Color(r[10] & 0xff, g[10] & 0xff, b[10] & 0xff));
        panelColour12.setBackground(new Color(r[11] & 0xff, g[11] & 0xff, b[11] & 0xff));
        panelColour13.setBackground(new Color(r[12] & 0xff, g[12] & 0xff, b[12] & 0xff));
        panelColour14.setBackground(new Color(r[13] & 0xff, g[13] & 0xff, b[13] & 0xff));
        panelColour15.setBackground(new Color(r[14] & 0xff, g[14] & 0xff, b[14] & 0xff));
        panelColour16.setBackground(new Color(r[15] & 0xff, g[15] & 0xff, b[15] & 0xff));*/

        Component[] colPanels = panelColours.getComponents();
        for (int i = 0; i < colPanels.length; i++){
            colPanels[i].setBackground(new Color(r[i] & 0xff, g[i] & 0xff, b[i] & 0xff));
        }

        panelColours.repaint();

        // Re-draw the loaded image (if it's loaded) with the selected ColorModel
        if (image_loaded){
            //displayTiles();
            Component[] tiles = panelTilesIMG.getComponents();
            TilePanel tp;

            for (int i = 0; i < tiles.length; i++){
                tp = (TilePanel) tiles[i];

                tp.setCModel(cm);
            }

            // Do the same for the SCR tiles, if we loaded an SCR
            if (scr_loaded){
                tiles = panelTilesSCR.getComponents();

                for (int i = 0; i < tiles.length; i++){
                    tp = (TilePanel) tiles[i];

                    tp.setCModel(cm);
                }
                panelTilesSCR.repaint();
            }
            panelTilesIMG.repaint();
        }
    }


    public void openBMfile(String filename, int type){
        try{
            // Read the contents of the file as bytes
            RandomAccessFile f = new RandomAccessFile(filename, "r");

            boolean valid = true;

            // Make sure it's a BM6 / BM9 file
            byte[] header = new byte[32];

            f.read(header);

            if (header[0] != 'B' || header[1] != 'M' || header[2] != 'P' || header[3] != type)
                valid = false;

            if (!valid){
                f.close();

                JOptionPane.showMessageDialog(null, "Wrong format for image file:\n" + filename,
                    "Error", JOptionPane.ERROR_MESSAGE);

                return;
            }

            // Get the image data
            byte[] img_data = new byte[ (int) (f.length() - 32)];   // Full file minus the header

            f.read(img_data);

            f.close();

            // We ignore the bytes indicating the number of colours. We know it's always 16.
            int width = 0;
            int height = 0;

            int tile_size = 32; // 8 bytes high * 4 bytes wide (4bpp)
            if (type == 9)
                tile_size = 64; // 8 bytes high * 8 bytes wide  (8bpp)

            width = header[8] << 24 | (header[9] & 0xFF) << 16 | (header[10] & 0xFF) << 8 | (header[11] & 0xFF);
            height = header[12] << 24 | (header[13] & 0xFF) << 16 | (header[14] & 0xFF) << 8 | (header[15] & 0xFF);

            tilesBMfile = new byte[height/8][][];

            int counter = 0;

            for (int i = 0; i < height/8; i++){ // Height in tiles
                tilesBMfile[i] = new byte[width/8][];  // Width in tiles

                for (int j = 0; j < width/8; j++){
                    tilesBMfile[i][j] = new byte[tile_size];

                    for (int k = 0; k < tile_size; k++){
                        tilesBMfile[i][j][k] = img_data[counter];
                        counter++;
                    }
                }
            }

            //System.out.println("Read " + counter + " bytes");

            if (tile_size == 32){
                buttonImportBM7.setEnabled(true);
                buttonImportBM10.setEnabled(false);
            }
            else{
                buttonImportBM7.setEnabled(false);
                buttonImportBM10.setEnabled(true);
            }

            image_loaded = true;

            if (checkClearOnLoad.isSelected())
                initPalettes();

            findPalettes(filename);

            displayTiles();

            buttonSaveBMP.setEnabled(true);

            resetSCRsection();
            scr_loaded = false;

        } catch (IOException ex){
            System.err.println("ERROR: Couldn't read BM6 / BM9 file!!");
        }
    }


    public void resetSCRsection(){
        // Enable the SCR section
        labelSCRfile.setEnabled(true);
        buttonLoadSCR.setEnabled(true);

        // Discard previous SCR tiles
        panelTilesSCR.removeAll();
        labelSCRfile.setText("- no file loaded -");

        // Disable SCR options
        buttonSaveSCR.setEnabled(false);
        buttonSCRtoBMP.setEnabled(false);
        buttonClear.setEnabled(false);
        buttonResize.setEnabled(false);

        checkFlipH.setSelected(false);
        checkFlipH.setEnabled(false);
        checkFlipV.setSelected(false);
        checkFlipV.setEnabled(false);

        labelWidth.setEnabled(false);
        labelHeight.setEnabled(false);
        textfieldWidth.setText("");
        textfieldWidth.setEnabled(false);
        textfieldHeight.setText("");
        textfieldHeight.setEnabled(false);

        labelZoomSCR.setEnabled(true);
        comboZoomSCR.setEnabled(true);
    }


    public void displayTiles(){
        panelTilesIMG.removeAll();

        int zoom = comboZoomImage.getSelectedIndex() + 1;
        int counter = 0;

        for (int i = 0; i < tilesBMfile.length; i++){
            for (int j = 0; j < tilesBMfile[i].length; j++){
                TilePanel tp = new TilePanel(tilesBMfile[i][j], palettes.get(listPalettes.getSelectedIndex()), counter, zoom);
                tp.addMouseListener(listener);

                tp.setBounds(j*10*zoom, i*10*zoom, 10*zoom, 10*zoom);
                //tp.repaint();

                panelTilesIMG.add(tp);
                counter++;
            }
        }

        panelTilesIMG.repaint();

        int newWidth = (tilesBMfile[0].length * 10 * zoom);
        int newHeight = (tilesBMfile.length * 10 * zoom);

        if (newWidth < scrollImage.getWidth()){
            newWidth = scrollImage.getWidth();
        }
        if (newHeight < scrollImage.getHeight()){
            newHeight = scrollImage.getHeight();
        }
        panelTilesIMG.setPreferredSize(new Dimension(newWidth, newHeight));

        scrollImage.revalidate();

        /*if (lastClickedTileIMG > 0){
            TilePanel clicked = (TilePanel) panelTilesIMG.getComponent(lastClickedTileIMG);

            clicked.setSelected(true);
            lastClicked = clicked;
        }*/
    }


    public void changeZoom(int zoom){
        Component[] tiles = panelTilesIMG.getComponents();
        TilePanel tp;
        int x = 0;
        int y = 0;

        for (int i = 0; i < tiles.length; i++){
            tp = (TilePanel) tiles[i];

            tp.setBounds(x*10*zoom, y*10*zoom, 10*zoom, 10*zoom);
            tp.setZoom(zoom);

            x++;
            if (x == tilesBMfile[0].length){
                x = 0;
                y++;
            }
        }

        int newWidth = (tilesBMfile[0].length * 10 * zoom);
        int newHeight = (tilesBMfile.length * 10 * zoom);

        if (newWidth < scrollImage.getWidth()){
            newWidth = scrollImage.getWidth();
        }
        if (newHeight < scrollImage.getHeight()){
            newHeight = scrollImage.getHeight();
        }
        panelTilesIMG.setPreferredSize(new Dimension(newWidth, newHeight));

        scrollImage.revalidate();
    }


    public void changeZoomSCR(int zoom){
        Component[] tiles = panelTilesSCR.getComponents();
        TilePanel tp;
        int x = 0;
        int y = 0;

        for (int i = 0; i < tiles.length; i++){
            tp = (TilePanel) tiles[i];

            tp.setBounds(x*10*zoom, y*10*zoom, 10*zoom, 10*zoom);
            tp.setZoom(zoom);

            x++;
            if (x == tileDataSCR[0].length){
                x = 0;
                y++;
            }
        }

        int newWidth = (tileDataSCR[0].length * 10 * zoom);
        int newHeight = (tileDataSCR.length * 10 * zoom);

        if (newWidth < scrollSCR.getWidth()){
            newWidth = scrollSCR.getWidth();
        }
        if (newHeight < scrollSCR.getHeight()){
            newHeight = scrollSCR.getHeight();
        }
        panelTilesSCR.setPreferredSize(new Dimension(newWidth, newHeight));

        scrollSCR.revalidate();
    }


    // Find and load the palettes that affect the BM6 we just loaded.
    public void findPalettes(String filename){
        // Get the folder and the name of the BM6 we're loading palettes for
        int pos = filename.lastIndexOf('/');
        if (pos < 0)
            pos = filename.lastIndexOf('\\');
        String folder = filename.substring(0, pos + 1);
        String name = filename.substring(pos + 1, pos + 5);

        int number = Integer.parseInt(name);
        String new_name = "";

        //System.out.println("Number: " + number);
        boolean go_on = true;
        int counter = 1;
        File f;

        int type = 7;
        if (!buttonImportBM7.isEnabled())
            type = 10;

        // See if there are palettes after the BM6 file
        while (go_on){
            new_name = getBMname(number + counter, type);

            f = new File(folder + new_name);

            if (!f.exists())
                go_on = false;
            else{
                openPalette(folder + new_name);
                counter++;
            }
        }

        // If we didn't find any palette immediately after the BM6 file, we have to look back
        // and find the last known BM7
        if (counter == 1){
            boolean found = false;

            for (int i = 1; i <= number && !found; i++){
                new_name = getBMname(number - i, type);

                f = new File(folder + new_name);

                if (f.exists()){
                    openPalette(folder + new_name);
                    found = true;   // Stop
                }
            }
        }

        // If we found palettes, choose the first one
        if (palettes.size() > 1){
            listPalettes.setSelectedIndex(1);
            lastPalette = 0;
            changePalette();
        }
        
        //END
    }


    public String getBMname(int number, int type){
        String name = "";

        if (number < 10)
            name += "000";
        else if (number < 100)
            name += "00";
        else if (number < 1000)
            name += "0";

        name += number + ".BM" + type;

        return name;
    }


    public void openSCR(String filename){
        try{
            // Read the contents of the file as bytes
            RandomAccessFile f = new RandomAccessFile(filename, "r");

            boolean valid = true;

            // Make sure it's a BM6 file
            byte[] header = new byte[32];

            f.read(header);

            if (header[0] != 'S' || header[1] != 'C' || header[2] != 'R')
                valid = false;

            if (!valid){
                f.close();

                JOptionPane.showMessageDialog(null, "Wrong format for SCR file:\n" + filename,
                    "Error", JOptionPane.ERROR_MESSAGE);

                return;
            }

            // Get the image data
            byte[] tile_data = new byte[ (int) (f.length() - 32)];   // Full file minus the header

            f.read(tile_data);

            f.close();

            int width = header[4] << 24 | (header[5] & 0xFF) << 16 | (header[6] & 0xFF) << 8 | (header[7] & 0xFF);
            int height = header[8] << 24 | (header[9] & 0xFF) << 16 | (header[10] & 0xFF) << 8 | (header[11] & 0xFF);

            int num_tiles = width * height; // We need to determine the number of tiles because SCR files usually have padding

            if (header[15] == 0)
                flipsAllowed = true;
            else
                flipsAllowed = false;

            // Read the tile data and store it in an internal structure
            tileDataSCR = new TileDataSCR[height][];
            for (int i = 0; i < height; i++)
                tileDataSCR[i] = new TileDataSCR[width];

            int x = 0;
            int y = 0;
            int counter = 0;

            while (counter < num_tiles){
                if (flipsAllowed){
                    int flips = tile_data[ 2*counter ] & 0xfc;  // Bytes for flips are the ones for 4 and 8, rest is still used for addressing tiles
                    //int pos = tile_data[ (2*counter) + 1 ] & 0xff;
                    int pos = (tile_data[ 2*counter ] & 0x03) << 8 | (tile_data[ (2*counter) + 1 ] & 0xFF);

                    switch (flips){
                        case 4: // flip horizontally
                            tileDataSCR[y][x] = new TileDataSCR(pos, true, false);
                            break;
                        case 8: // flip vertically
                            tileDataSCR[y][x] = new TileDataSCR(pos, false, true);
                            break;
                        case 12:    // flip both
                            tileDataSCR[y][x] = new TileDataSCR(pos, true, true);
                            break;
                        default:    // no flips
                            tileDataSCR[y][x] = new TileDataSCR(pos, false, false);
                    }
                }
                else{
                    int pos = (tile_data[ 2*counter ] & 0xFF) << 8 | (tile_data[ (2*counter) + 1 ] & 0xFF);
                    tileDataSCR[y][x] = new TileDataSCR(pos, false, false);
                }

                x++;
                if (x == width){
                    x = 0;
                    y++;
                }
                counter ++;
            }


            // Fill the SCR panel with the indicated tiles taken from the image
            displaySCR();

            // Enable the rest of the SCR features
            buttonSaveSCR.setEnabled(true);
            buttonSCRtoBMP.setEnabled(true);
            buttonClear.setEnabled(true);
            buttonResize.setEnabled(true);

            checkFlipH.setEnabled(flipsAllowed);
            checkFlipV.setEnabled(flipsAllowed);

            labelWidth.setEnabled(true);
            labelHeight.setEnabled(true);
            textfieldWidth.setText("" + width);
            textfieldWidth.setEnabled(true);
            textfieldHeight.setText("" + height);
            textfieldHeight.setEnabled(true);

            // Set the first tile as selected
            selectedSCR = 0;
            TilePanel tp = (TilePanel) panelTilesSCR.getComponent(0);
            tp.setSelected(true);
            lastSCRclicked = tp;

            checkFlipH.setSelected(tileDataSCR[0][0].flipH);
            checkFlipV.setSelected(tileDataSCR[0][0].flipV);

            scr_loaded = true;

        } catch (IOException ex){
            System.err.println("ERROR: Couldn't read BM6 file!!");
        }
    }


    public void displaySCR(){
        panelTilesSCR.removeAll();

        int zoom = comboZoomSCR.getSelectedIndex() + 1;
        int counter = 0;
        int x = 0;
        int y = 0;
        int position = 0;

        // Width and Height in tiles
        int height = tileDataSCR.length;
        int width = tileDataSCR[0].length;

        //int height_img = tilesBM6.length;
        int width_img = tilesBMfile[0].length;

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                position = tileDataSCR[i][j].position;

                x = position % width_img;
                y = position / width_img;

                //System.out.println("Position: " + position + " X: " + x + " Y: " + y);

                TilePanel tp = new TilePanel(tilesBMfile[y][x], palettes.get(listPalettes.getSelectedIndex()), counter, zoom);
                tp.addMouseListener(listenerSCR);

                tp.setFlips(tileDataSCR[i][j].flipH, tileDataSCR[i][j].flipV);

                tp.setBounds(j*10*zoom, i*10*zoom, 10*zoom, 10*zoom);

                panelTilesSCR.add(tp);
                counter++;
            }
        }

        panelTilesSCR.repaint();

        int newWidth = (width * 10 * zoom);
        int newHeight = (height * 10 * zoom);

        if (newWidth < scrollSCR.getWidth()){
            newWidth = scrollSCR.getWidth();
        }
        if (newHeight < scrollSCR.getHeight()){
            newHeight = scrollSCR.getHeight();
        }
        panelTilesSCR.setPreferredSize(new Dimension(newWidth, newHeight));

        scrollSCR.revalidate();
    }


    public void clearTilesSCR(){
        int pos = 0;

        for (int i = 0; i < tileDataSCR.length; i++){
            for (int j= 0; j < tileDataSCR[0].length; j++){
                tileDataSCR[i][j].flipH = false;
                tileDataSCR[i][j].flipV = false;
                tileDataSCR[i][j].position = 0;

                //((TilePanel) panelTilesSCR.getComponent(pos)).clearTile();
                ((TilePanel) panelTilesSCR.getComponent(pos)).setTileImage(
                        ((TilePanel) panelTilesIMG.getComponent(0)).getTileImage() );
                ((TilePanel) panelTilesSCR.getComponent(pos)).setFlips(false, false);
                pos++;
            }
        }
        
        panelTilesSCR.repaint();
    }


    public void resizeSCR(){
        try{
            int newWidth = Integer.parseInt(textfieldWidth.getText());
            int newHeight = Integer.parseInt(textfieldHeight.getText());
            
            int width = tileDataSCR[0].length;
            int height = tileDataSCR.length;

            if (newWidth == width && newHeight == height){
                System.out.println("Dimensions are the same. We don't resize.");
                return;
            }

            if (newWidth == 0 || newHeight == 0){
                System.out.println("Having 0 rows / columns is not allowed.");
                return;
            }

            TileDataSCR[][] newTiles = new TileDataSCR[newHeight][];

            for (int i = 0; i < newHeight; i++){
                newTiles[i] = new TileDataSCR[newWidth];

                for (int j = 0; j < newWidth; j++){
                    if ( i < height && j < width)
                        newTiles[i][j] = tileDataSCR[i][j];
                    else
                        newTiles[i][j] = new TileDataSCR();
                }
            }

            tileDataSCR = newTiles;
            
            displaySCR();

            if (selectedSCR > panelTilesSCR.getComponentCount())
                selectedSCR = 0;

            lastSCRclicked = (TilePanel) panelTilesSCR.getComponent(selectedSCR);
            lastSCRclicked.setSelected(true);

            //panelTilesSCR.repaint();
            
        }catch(NumberFormatException ex){
            System.err.println("ERROR: Width or height is not a number!");
        }
    }


    public void saveSCR(String path){
        byte[] header = new byte[32];

        int tilesX = tileDataSCR[0].length;
        int tilesY = tileDataSCR.length;

        int num_tiles = tilesX * tilesY;

        // Prepare the header
        header[0] = 'S';
        header[1] = 'C';
        header[2] = 'R';

        header[4] = (byte) ( (tilesX >> 24) & 0xff );
        header[5] = (byte) ( (tilesX >> 16) & 0xff );
        header[6] = (byte) ( (tilesX >> 8) & 0xff );
        header[7] = (byte) ( tilesX  & 0xff );

        header[8] = (byte) ( (tilesY >> 24) & 0xff );
        header[9] = (byte) ( (tilesY >> 16) & 0xff );
        header[10] = (byte) ( (tilesY >> 8) & 0xff );
        header[11] = (byte) ( tilesY  & 0xff );

        if (!flipsAllowed)
            header[15] = 1;

        // Prepare the tile data
        int size = num_tiles * 2;   // 2 bytes per tile
        int extra_bytes = size % 32;

        if (extra_bytes != 0)   // The tile data has to be 32-byte aligned
            size += 32 - extra_bytes;

        byte[] data = new byte[size];

        /*Component[] tiles = panelTilesSCR.getComponents();

        TilePanel tp;*/

        //for (int i = 0; i < tiles.length; i++){
            //tp = (TilePanel) tiles[i];
        int counter = 0;

        for (int i = 0; i < tilesY; i++){
            for (int j = 0; j < tilesX; j++){
                int pos = tileDataSCR[i][j].position;
                int flips = 0;

                if (tileDataSCR[i][j].flipH)
                    flips += 4;
                if (tileDataSCR[i][j].flipV)
                    flips += 8;

                data[2*counter] = (byte) ( flips | ( (pos >> 8) & 0xff) );
                data[2*counter + 1] = (byte) (pos & 0xff);

                counter++;
            }
        }

        // Write the file
        if (!path.endsWith(".SCR") && !path.endsWith(".scr"))
            path += ".SCR";
        
        try {
            RandomAccessFile scr = new RandomAccessFile(path, "rw");

            scr.write(header);
            scr.write(data);

            scr.close();

            System.out.println(path + " saved successfully.");
        } catch (IOException ex) {
            System.err.println("ERROR: Couldn't write " + path);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    public void saveBMP(String path){
        byte[] CLUT = getCLUT();
        byte[] imageData = getImageData(tilesBMfile);
        int width = tilesBMfile[0].length * 8; // Each tile is 8 pixels wide
        int height = tilesBMfile.length * 8;   // and 8 pixels high
        byte depth = 0x04;  // 4bpp
        if (!buttonImportBM7.isEnabled())
            depth = 0x08;   // 8bpp

        writeBMP(path, CLUT, imageData, width, height, depth);
    }


    public void saveSCRtoBMP(String path){
        int width = tileDataSCR[0].length * 8; // Each tile is 8 pixels wide
        int height = tileDataSCR.length * 8;   // and 8 pixels high

        int x = 0;
        int y = 0;
        int position = 0;

        // Prepare a 3D array with the tile data of the selected tiles
        byte[][][] selectedTiles = new byte[tileDataSCR.length][][];

        for (int i = 0; i < tileDataSCR.length; i++){
            selectedTiles[i] = new byte[tileDataSCR[0].length][];
            for (int j = 0; j < tileDataSCR[0].length; j++){
                position = tileDataSCR[i][j].position;

                x = position % tilesBMfile[0].length;
                y = position / tilesBMfile[0].length;

                selectedTiles[i][j] = tilesBMfile[y][x];   // This works, but doesn't export flips

                if (tileDataSCR[i][j].flipH){
                    //System.out.println("Tile X: " + j + " Y: " + i + " flipped horizontally");
                    selectedTiles[i][j] = flipTileH(selectedTiles[i][j]);
                }

                if (tileDataSCR[i][j].flipV){
                    //System.out.println("Tile X: " + j + " Y: " + i + " flipped vertically");
                    selectedTiles[i][j] = flipTileV(selectedTiles[i][j]);
                }

                /* This doesn't work
                BufferedImage aux_img = ((TilePanel) panelTilesSCR.getComponent(position)).getTileImage();
                selectedTiles[i][j] = ((DataBufferByte) aux_img.getData().getDataBuffer()).getData();
                position++;*/
            }
        }

        byte[] CLUT = getCLUT();
        byte[] imageData = getImageData(selectedTiles);
        byte depth = 0x04;  // 4bpp
        if (!buttonImportBM7.isEnabled())
            depth = 0x08;   // 8bpp

        writeBMP(path, CLUT, imageData, width, height, depth);
    }


    public byte[] flipTileV(byte[] tile){
        byte[] pixels_R = tile.clone();
        //int dimX = tiles[0].length * tile_width;
        int dimX = 4;

        for (int i = 0, j = tile.length - dimX; i < tile.length; i+=dimX, j-=dimX){
            for (int k = 0; k < dimX; ++k){
                //System.out.println("Length: " + pixels.length + " i: " + i + " j: " + j + " k: " + k);
                pixels_R[i + k] = tile[j + k];
            }
        }

        return pixels_R;
    }


    public byte[] flipTileH(byte[] tile){
        byte[] pixels_R = tile.clone();

        for (int i = 0; i < 8; i++){
            for (int j = 0; j < 4; j++){
                int pos = i*4 + j;

                int k = -(j - 3);
                
                int newPos = i*4 + k;

                byte aux = tile[pos];

                if (buttonImportBM7.isEnabled()){   // We're dealing with BM6 - 4bpp
                    byte aux2 = (byte) ( ( (aux & 0xf0) >> 4 ) | ( (aux & 0x0f) << 4 ) );   // Reverse the nibbles

                    //System.out.println("Original: " + pos + " New: " + newPos);

                    pixels_R[newPos] = aux2;
                }
                else{   // We're dealing with BM9 - 8bpp (no need to deal with nibbles, just bytes)
                    pixels_R[newPos] = aux;
                }
            }
        }

        return pixels_R;
    }


    // Returns the selected palette as a byte array
    public byte[] getCLUT(){
        int clut_size = 64;
        if (!buttonImportBM7.isEnabled())
            clut_size = 1024;

        byte[] clut = new byte[clut_size]; // 16 colours * 4 bytes

        byte[] r = new byte[clut_size / 4];
        byte[] g = new byte[clut_size / 4];
        byte[] b = new byte[clut_size / 4];
        byte[] a = new byte[clut_size / 4];

        int sel = listPalettes.getSelectedIndex();

        palettes.get(sel).getReds(r);
        palettes.get(sel).getGreens(g);
        palettes.get(sel).getBlues(b);
        palettes.get(sel).getAlphas(a);

        int counter = 0;

        for (int i = 0; i < clut_size; i += 4){
            clut[i] = b[counter];   // It seems like Windows BMP files use BGRA colours
            clut[i+1] = g[counter];
            clut[i+2] = r[counter]; // So we have to switch their positions when saving to BMP
            clut[i+3] = a[counter];

            //System.out.println("R: " + r[counter] + " G: " + g[counter] + " B: " + b[counter] + " A: " + a[counter]);

            counter++;
        }

        return clut;
    }


    // Takes a tiled image structure and returns it as a byte array
    public byte[] getImageData(byte[][][] tiles){
        byte[] image = new byte[tiles.length * tiles[0].length * tiles[0][0].length];

        int tile_width = 4; // Width of a tile in bytes
        if (tiles[0][0].length == 64)
            tile_width = 8;

        // We have to write the lines of each row of tiles one after the other
        // That is, first we write line 0 of every tile in row 0, then line one of that same row, and so on
        for (int i = 0; i < tiles.length; i++){ // For every row of tiles
            for (int j = 0; j < tiles[0].length; j++){  // For every tile in a row (each row has the same amount of tiles)
                int line_count = -1; // line inside a tile - each line has 8 pixels (4 bytes)

                for (int k = 0; k < tiles[0][0].length; k++){   // For every byte inside a tile (32 or 64)
                    int row_pos = k % tile_width;
                    if (row_pos == 0)
                        line_count++;

                    // The position is:
                    // displacement inside the row of tiles
                    // + displacemen in rows (a row holds N tiles of 32 or 64 bytes each)
                    // + displacement in rows inside a tile (each line has 4 or 8 bytes)
                    // + displacement insde a row in the tile
                    int pos = (j * tile_width)
                            + (i * tiles[0].length * tiles[0][0].length)
                            + line_count * (tiles[0].length * tile_width)
                            + row_pos;

                    //System.out.println("Row: " + i + " Col: " + j + " Pix: " + k + " Pos: " + pos);

                    image[pos] = tiles[i][j][k];
                }
            }
        }

        // The texture data is stored upside-down. We can fix that.
        byte[] pixels_R = image.clone();
        int dimX = tiles[0].length * tile_width;
        for (int i = 0, j = image.length - dimX; i < image.length; i+=dimX, j-=dimX){
            for (int k = 0; k < dimX; ++k){
                //System.out.println("Length: " + pixels.length + " i: " + i + " j: " + j + " k: " + k);
                image[i + k] = pixels_R[j + k];
            }
        }

        return image;
    }


    public void writeBMP(String filename, byte[] CLUT, byte[] imageData, int width, int height, byte depth){
        if (!filename.endsWith(".bmp") && !filename.endsWith(".BMP"))
            filename += ".bmp";

        byte[] header = new byte[54];

        // Prepare the header
        // * All sizes are big endian

        // Byte 0: '42' (B) Byte 1: '4d' (M)
        header[0] = 0x42;
        header[1] = 0x4d;

        // Next 4 bytes: file size (header + CLUT + pixels)
        int file_size = 54 + CLUT.length + imageData.length;

        header[2] = (byte) (file_size & 0xff);
        header[3] = (byte) ((file_size >> 8) & 0xff);
        header[4] = (byte) ((file_size >> 16) & 0xff);
        header[5] = (byte) ((file_size >> 24) & 0xff);

        // Next 4 bytes: all 0
        header[6] = 0;
        header[7] = 0;
        header[8] = 0;
        header[9] = 0;

        // Next 4 bytes: offset to start of image (header + CLUT)
        int offset = file_size - imageData.length;
        header[10] = (byte) (offset & 0xff);
        header[11] = (byte) ((offset >> 8) & 0xff);
        header[12] = (byte) ((offset >> 16) & 0xff);
        header[13] = (byte) ((offset >> 24) & 0xff);

        // Next 4 bytes: 28 00 00 00
        header[14] = 40;
        header[15] = 0;
        header[16] = 0;
        header[17] = 0;

        // Next 4 bytes: Width
        header[18] = (byte) (width & 0xff);
        header[19] = (byte) ((width >> 8) & 0xff);
        header[20] = (byte) ((width >> 16) & 0xff);
        header[21] = (byte) ((width >> 24) & 0xff);

        // Next 4 bytes: Height
        header[22] = (byte) (height & 0xff);
        header[23] = (byte) ((height >> 8) & 0xff);
        header[24] = (byte) ((height >> 16) & 0xff);
        header[25] = (byte) ((height >> 24) & 0xff);

        // Next 2 bytes: 01 00 (number of planes in the image)
        header[26] = 1;
        header[27] = 0;

        // Next 2 bytes: bits per pixel ( 04 00 or 08 00 )
        header[28] = depth;
        header[29] = 0;

        // Next 4 bytes: 00 00 00 00 (compression)
        header[30] = 0;
        header[31] = 0;
        header[32] = 0;
        header[33] = 0;

        // Next 4 bytes: image size in bytes (pixels)
        header[34] = (byte) (imageData.length & 0xff);
        header[35] = (byte) ((imageData.length >> 8) & 0xff);
        header[36] = (byte) ((imageData.length >> 16) & 0xff);
        header[37] = (byte) ((imageData.length >> 24) & 0xff);

        // Next 12 bytes: all 0 (horizontal and vertical resolution, number of colours)
        header[38] = 0;
        header[39] = 0;
        header[40] = 0;
        header[41] = 0;
        header[42] = 0;
        header[43] = 0;
        header[44] = 0;
        header[45] = 0;
        header[46] = 0;
        header[47] = 0;
        header[48] = 0;
        header[49] = 0;

        int num_colours = (CLUT.length / 4);

        // Next 4 bytes: important colours (= number of colours)
        header[50] = (byte) (num_colours & 0xff);
        header[51] = (byte) ((num_colours >> 8) & 0xff);
        header[52] = 0;
        header[53] = 0;

        // Check if folder with the name of the pak_file exists. If not, create it.
        /*String path = pak_file + "_extract";
        File folder = new File(path);
        if (!folder.exists()){
            boolean success = folder.mkdir();
            if (!success){
                System.err.println("ERROR: Couldn't create folder.");
                return;
            }
        }*/

        // Create the bmp file inside said folder
        //String file_path = filename + "_" + number + ".bmp";
        //path += "/" + file_path;
        try {
            //RandomAccessFile bmp = new RandomAccessFile(path, "rw");
            RandomAccessFile bmp = new RandomAccessFile(filename, "rw");

            bmp.write(header);
            bmp.write(CLUT);
            bmp.write(imageData);
            
            bmp.close();

            //System.out.println(file_path + " saved successfully.");
            System.out.println(filename + " saved successfully.");
            //tex_counter++;
        } catch (IOException ex) {
            //System.err.println("ERROR: Couldn't write " + file_path);
            System.err.println("ERROR: Couldn't write " + filename);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonClear;
    private javax.swing.ButtonGroup buttonGroupTiles;
    private javax.swing.JButton buttonImportBM10;
    private javax.swing.JButton buttonImportBM7;
    private javax.swing.JButton buttonLoad;
    private javax.swing.JButton buttonLoadBM9;
    private javax.swing.JButton buttonLoadSCR;
    private javax.swing.JButton buttonResize;
    private javax.swing.JButton buttonSCRtoBMP;
    private javax.swing.JButton buttonSaveBMP;
    private javax.swing.JButton buttonSaveSCR;
    private javax.swing.JCheckBox checkClearOnLoad;
    private javax.swing.JCheckBox checkFlipH;
    private javax.swing.JCheckBox checkFlipV;
    private javax.swing.JComboBox comboZoomImage;
    private javax.swing.JComboBox comboZoomSCR;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelPalettes;
    private javax.swing.JLabel labelSCRfile;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JLabel labelZoomImage;
    private javax.swing.JLabel labelZoomSCR;
    private javax.swing.JList listPalettes;
    private javax.swing.JPanel panelColours;
    private javax.swing.JPanel panelImageData;
    private javax.swing.JPanel panelPalettes;
    private javax.swing.JPanel panelSCRedit;
    private javax.swing.JPanel panelTilesIMG;
    private javax.swing.JPanel panelTilesSCR;
    private javax.swing.JRadioButton radioPickIndividualTile;
    private javax.swing.JRadioButton radioPickTileGroup;
    private javax.swing.JScrollPane scrollImage;
    private javax.swing.JScrollPane scrollPalettes;
    private javax.swing.JScrollPane scrollSCR;
    private javax.swing.JTextField textfieldHeight;
    private javax.swing.JTextField textfieldWidth;
    // End of variables declaration//GEN-END:variables

}
