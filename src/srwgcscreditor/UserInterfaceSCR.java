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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
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
    ArrayList<IndexColorModel> palettes;        // Originals - can restore to these
    ArrayList<IndexColorModel> palettes_mod;    // with modifications
    int lastPalette = -1;
    boolean image_loaded = false;
    boolean scr_loaded = false;
    String title = "SRW GC SCR Editor by Dashman";
    
    String[] color16 = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                         "10", "11", "12", "13", "14", "15"};
    
    String[] color256 = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                         "10", "11", "12", "13", "14", "15", "16", "17", "18", "19",
                         "20", "21", "22", "23", "24", "25", "26", "27", "28", "29",
                         "30", "31", "32", "33", "34", "35", "36", "37", "38", "39",
                         "40", "41", "42", "43", "44", "45", "46", "47", "48", "49",
                         "50", "51", "52", "53", "54", "55", "56", "57", "58", "59",
                         "60", "61", "62", "63", "64", "65", "66", "67", "68", "69",
                         "70", "71", "72", "73", "74", "75", "76", "77", "78", "79",
                         "80", "81", "82", "83", "84", "85", "86", "87", "88", "89",
                         "90", "91", "92", "93", "94", "95", "96", "97", "98", "99",
                         "100", "101", "102", "103", "104", "105", "106", "107", "108", "109",
                         "110", "111", "112", "113", "114", "115", "116", "117", "118", "119",
                         "120", "121", "122", "123", "124", "125", "126", "127", "128", "129",
                         "130", "131", "132", "133", "134", "135", "136", "137", "138", "139",
                         "140", "141", "142", "143", "144", "145", "146", "147", "148", "149",
                         "150", "151", "152", "153", "154", "155", "156", "157", "158", "159",
                         "160", "161", "162", "163", "164", "165", "166", "167", "168", "169",
                         "170", "171", "172", "173", "174", "175", "176", "177", "178", "179",
                         "180", "181", "182", "183", "184", "185", "186", "187", "188", "189",
                         "190", "191", "192", "193", "194", "195", "196", "197", "198", "199",
                         "200", "201", "202", "203", "204", "205", "206", "207", "208", "209",
                         "210", "211", "212", "213", "214", "215", "216", "217", "218", "219",
                         "220", "221", "222", "223", "224", "225", "226", "227", "228", "229",
                         "230", "231", "232", "233", "234", "235", "236", "237", "238", "239",
                         "240", "241", "242", "243", "244", "245", "246", "247", "248", "249",
                         "250", "251", "252", "253", "254", "255"};

    // We use this to manage the contents of the palette list
    DefaultListModel modelListPal = new DefaultListModel();
        
    // Variables used for Mass BMP exports
    boolean palette_after = true;   // Indicates if the palette is before or after the BM file
    String lastPaletteFound = "";   // Name of the last palette found
                                    // This is useful to start looking for SCR files after it
                                    // when the palette is after the BM file

    // We save the image data as byte arrays
    byte[][][] tilesBMfile;
    //byte[][][] tilesSCR;

    TilePanel lastClicked = null;
    TilePanel lastSCRclicked = null;
    int lastClickedTileIMG = -1;
    int selectedSCR = 0;
    int lastX = -1;
    int lastY = -1;
    
    int[] highlights;

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
            
            highlightTiles();

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
        selectPalette();

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
        checkTransparencyBM = new javax.swing.JCheckBox();
        labelDimensions = new javax.swing.JLabel();
        panelPalettes = new javax.swing.JPanel();
        panelColours = new javax.swing.JPanel();
        scrollPalettes = new javax.swing.JScrollPane();
        listPalettes = new javax.swing.JList();
        labelPalettes = new javax.swing.JLabel();
        buttonImportBM7 = new javax.swing.JButton();
        checkClearOnLoad = new javax.swing.JCheckBox();
        buttonImportBM10 = new javax.swing.JButton();
        buttonExportPalette = new javax.swing.JButton();
        checkXOpaddingPalette = new javax.swing.JCheckBox();
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
        checkTransparencySCR = new javax.swing.JCheckBox();
        checkXOpaddingSCR = new javax.swing.JCheckBox();
        textfieldAlign = new javax.swing.JTextField();
        labelAlign = new javax.swing.JLabel();
        panelEditPalette = new javax.swing.JPanel();
        comboColor = new javax.swing.JComboBox<>();
        labelColorNmbr = new javax.swing.JLabel();
        fieldColorR = new javax.swing.JTextField();
        labelColorR = new javax.swing.JLabel();
        fieldColorG = new javax.swing.JTextField();
        labelColorG = new javax.swing.JLabel();
        fieldColorB = new javax.swing.JTextField();
        labelColorB = new javax.swing.JLabel();
        fieldColorA = new javax.swing.JTextField();
        labelColorA = new javax.swing.JLabel();
        buttonRestoreColor = new javax.swing.JButton();
        buttonRestoreAllColors = new javax.swing.JButton();
        menubarMain = new javax.swing.JMenuBar();
        menuBatch = new javax.swing.JMenu();
        menuitemBM6FolderToBMP = new javax.swing.JMenuItem();
        menuitemBM9FolderToBMP = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        menuItemSCRFolderToBMP = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        checkmenuAllPalettes = new javax.swing.JCheckBoxMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SRW GC SCR Editor by Dashman");
        setResizable(false);

        panelImageData.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Image Data", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(51, 51, 255))); // NOI18N

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

        labelZoomImage.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelZoomImage.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        labelZoomImage.setText("Zoom");

        panelTilesIMG.setPreferredSize(new java.awt.Dimension(500, 427));

        javax.swing.GroupLayout panelTilesIMGLayout = new javax.swing.GroupLayout(panelTilesIMG);
        panelTilesIMG.setLayout(panelTilesIMGLayout);
        panelTilesIMGLayout.setHorizontalGroup(
            panelTilesIMGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 531, Short.MAX_VALUE)
        );
        panelTilesIMGLayout.setVerticalGroup(
            panelTilesIMGLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 482, Short.MAX_VALUE)
        );

        scrollImage.setViewportView(panelTilesIMG);

        buttonGroupTiles.add(radioPickIndividualTile);
        radioPickIndividualTile.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        radioPickIndividualTile.setSelected(true);
        radioPickIndividualTile.setText("Pick tiles individually");
        radioPickIndividualTile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioPickIndividualTileActionPerformed(evt);
            }
        });

        buttonGroupTiles.add(radioPickTileGroup);
        radioPickTileGroup.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        radioPickTileGroup.setText("Pick range of tiles");

        buttonLoadBM9.setText("Load BM9");
        buttonLoadBM9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonLoadBM9ActionPerformed(evt);
            }
        });

        checkTransparencyBM.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        checkTransparencyBM.setSelected(true);
        checkTransparencyBM.setText("Alt. transparency");
        checkTransparencyBM.setEnabled(false);

        labelDimensions.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelDimensions.setText("Dimensions (in tiles): 0 x 0");

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
                            .addComponent(scrollImage, javax.swing.GroupLayout.PREFERRED_SIZE, 533, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(panelImageDataLayout.createSequentialGroup()
                                .addComponent(buttonLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(buttonLoadBM9, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(buttonSaveBMP, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(checkTransparencyBM)
                                .addGap(4, 4, 4)
                                .addComponent(labelZoomImage, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboZoomImage, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(panelImageDataLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelDimensions, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        panelImageDataLayout.setVerticalGroup(
            panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelImageDataLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(labelDimensions)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(buttonLoad)
                    .addComponent(comboZoomImage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelZoomImage)
                    .addComponent(buttonSaveBMP)
                    .addComponent(buttonLoadBM9)
                    .addComponent(checkTransparencyBM))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scrollImage)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelImageDataLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(radioPickIndividualTile)
                    .addComponent(radioPickTileGroup))
                .addContainerGap())
        );

        panelPalettes.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Palettes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(51, 51, 255))); // NOI18N

        panelColours.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout panelColoursLayout = new javax.swing.GroupLayout(panelColours);
        panelColours.setLayout(panelColoursLayout);
        panelColoursLayout.setHorizontalGroup(
            panelColoursLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
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

        checkClearOnLoad.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        checkClearOnLoad.setSelected(true);
        checkClearOnLoad.setText("Clear on load");

        buttonImportBM10.setText("Import BM10");
        buttonImportBM10.setEnabled(false);
        buttonImportBM10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonImportBM10ActionPerformed(evt);
            }
        });

        buttonExportPalette.setText("Export Palette");
        buttonExportPalette.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonExportPaletteActionPerformed(evt);
            }
        });

        checkXOpaddingPalette.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        checkXOpaddingPalette.setText("XO padding");

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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(panelPalettesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(checkClearOnLoad, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonImportBM7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(buttonImportBM10, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE)
                            .addComponent(buttonExportPalette, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(panelPalettesLayout.createSequentialGroup()
                                .addComponent(checkXOpaddingPalette)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(labelPalettes, javax.swing.GroupLayout.Alignment.LEADING))
                .addContainerGap())
        );
        panelPalettesLayout.setVerticalGroup(
            panelPalettesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelPalettesLayout.createSequentialGroup()
                .addComponent(panelColours, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelPalettes)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelPalettesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelPalettesLayout.createSequentialGroup()
                        .addComponent(buttonImportBM7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(buttonImportBM10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(checkClearOnLoad)
                        .addGap(18, 18, 18)
                        .addComponent(buttonExportPalette)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(checkXOpaddingPalette))
                    .addComponent(scrollPalettes))
                .addContainerGap())
        );

        panelSCRedit.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "SCR Edit", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(51, 51, 255))); // NOI18N

        labelSCRfile.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelSCRfile.setText("- no file loaded -");
        labelSCRfile.setEnabled(false);

        javax.swing.GroupLayout panelTilesSCRLayout = new javax.swing.GroupLayout(panelTilesSCR);
        panelTilesSCR.setLayout(panelTilesSCRLayout);
        panelTilesSCRLayout.setHorizontalGroup(
            panelTilesSCRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 496, Short.MAX_VALUE)
        );
        panelTilesSCRLayout.setVerticalGroup(
            panelTilesSCRLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 187, Short.MAX_VALUE)
        );

        scrollSCR.setViewportView(panelTilesSCR);

        labelWidth.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        labelWidth.setText("Width:");
        labelWidth.setEnabled(false);

        textfieldWidth.setEnabled(false);
        textfieldWidth.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textfieldWidthKeyTyped(evt);
            }
        });

        labelHeight.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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

        checkFlipH.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        checkFlipH.setText("Flip Tile Horizontally");
        checkFlipH.setEnabled(false);
        checkFlipH.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                checkFlipHActionPerformed(evt);
            }
        });

        checkFlipV.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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

        labelZoomSCR.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
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

        buttonClear.setText("New / Clear");
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

        checkTransparencySCR.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        checkTransparencySCR.setSelected(true);
        checkTransparencySCR.setText("Alt. transparency");
        checkTransparencySCR.setEnabled(false);

        checkXOpaddingSCR.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        checkXOpaddingSCR.setText("XO padding");
        checkXOpaddingSCR.setEnabled(false);

        textfieldAlign.setEnabled(false);
        textfieldAlign.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textfieldAlignKeyTyped(evt);
            }
        });

        labelAlign.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        labelAlign.setText("Align (XO):");
        labelAlign.setEnabled(false);

        javax.swing.GroupLayout panelSCReditLayout = new javax.swing.GroupLayout(panelSCRedit);
        panelSCRedit.setLayout(panelSCReditLayout);
        panelSCReditLayout.setHorizontalGroup(
            panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelSCReditLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
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
                                .addComponent(buttonResize)
                                .addGap(57, 57, 57))
                            .addComponent(checkFlipV, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(22, 22, 22)
                        .addComponent(labelAlign)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textfieldAlign, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelSCReditLayout.createSequentialGroup()
                        .addComponent(labelSCRfile, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelZoomSCR, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(comboZoomSCR, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(scrollSCR, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(panelSCReditLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(checkXOpaddingSCR)
                        .addComponent(checkTransparencySCR)
                        .addComponent(buttonSaveSCR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonLoadSCR, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonClear, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
                    .addComponent(buttonSCRtoBMP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkXOpaddingSCR)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(buttonSCRtoBMP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(checkTransparencySCR, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                    .addComponent(buttonResize)
                    .addComponent(textfieldAlign, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelAlign))
                .addContainerGap())
        );

        panelEditPalette.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Color Edit", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 0, 12), new java.awt.Color(51, 51, 255))); // NOI18N

        comboColor.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }));
        comboColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                comboColorActionPerformed(evt);
            }
        });

        labelColorNmbr.setText("Color:");

        fieldColorR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldColorRActionPerformed(evt);
            }
        });

        labelColorR.setText("R:");

        fieldColorG.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldColorGActionPerformed(evt);
            }
        });

        labelColorG.setText("G:");

        fieldColorB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldColorBActionPerformed(evt);
            }
        });

        labelColorB.setText("B:");

        fieldColorA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fieldColorAActionPerformed(evt);
            }
        });

        labelColorA.setText("A:");

        buttonRestoreColor.setText("Restore Color");
        buttonRestoreColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRestoreColorActionPerformed(evt);
            }
        });

        buttonRestoreAllColors.setText("Restore ALL");
        buttonRestoreAllColors.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonRestoreAllColorsActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout panelEditPaletteLayout = new javax.swing.GroupLayout(panelEditPalette);
        panelEditPalette.setLayout(panelEditPaletteLayout);
        panelEditPaletteLayout.setHorizontalGroup(
            panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEditPaletteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(buttonRestoreColor, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditPaletteLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditPaletteLayout.createSequentialGroup()
                                .addComponent(labelColorNmbr)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(comboColor, javax.swing.GroupLayout.PREFERRED_SIZE, 61, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditPaletteLayout.createSequentialGroup()
                                .addComponent(labelColorR, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fieldColorR, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditPaletteLayout.createSequentialGroup()
                                .addComponent(labelColorG, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fieldColorG, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditPaletteLayout.createSequentialGroup()
                                .addComponent(labelColorB, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fieldColorB, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, panelEditPaletteLayout.createSequentialGroup()
                                .addComponent(labelColorA, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(fieldColorA, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(buttonRestoreAllColors, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        panelEditPaletteLayout.setVerticalGroup(
            panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(panelEditPaletteLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(comboColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelColorNmbr))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldColorR, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelColorR))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldColorG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelColorG))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldColorB, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelColorB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(panelEditPaletteLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fieldColorA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelColorA))
                .addGap(18, 18, 18)
                .addComponent(buttonRestoreColor)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonRestoreAllColors)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        menuBatch.setText("Batch operations");

        menuitemBM6FolderToBMP.setText("Folder (BM6) --> BMP");
        menuitemBM6FolderToBMP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuitemBM6FolderToBMPActionPerformed(evt);
            }
        });
        menuBatch.add(menuitemBM6FolderToBMP);

        menuitemBM9FolderToBMP.setText("Folder (BM9) --> BMP");
        menuitemBM9FolderToBMP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuitemBM9FolderToBMPActionPerformed(evt);
            }
        });
        menuBatch.add(menuitemBM9FolderToBMP);
        menuBatch.add(jSeparator1);

        menuItemSCRFolderToBMP.setText("Folder (SCRs) --> BMP");
        menuItemSCRFolderToBMP.setEnabled(false);
        menuItemSCRFolderToBMP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuItemSCRFolderToBMPActionPerformed(evt);
            }
        });
        menuBatch.add(menuItemSCRFolderToBMP);
        menuBatch.add(jSeparator2);

        checkmenuAllPalettes.setText("Export using all palettes");
        menuBatch.add(checkmenuAllPalettes);

        menubarMain.add(menuBatch);

        setJMenuBar(menubarMain);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(panelImageData, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(panelPalettes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelEditPalette, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(panelSCRedit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(panelImageData, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(panelPalettes, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(panelEditPalette, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(panelSCRedit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
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
            selectPalette();
            
            listPalettes.requestFocusInWindow();

            lastDirectory = chooser.getSelectedFile().getPath();
        }
    }//GEN-LAST:event_buttonImportBM7ActionPerformed

    private void listPalettesMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_listPalettesMouseClicked
        // TODO add your handling code here:
        selectPalette();
    }//GEN-LAST:event_listPalettesMouseClicked

    private void listPalettesKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_listPalettesKeyReleased
        // TODO add your handling code here:
        selectPalette();
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
        if (image_loaded){
            changeZoom(comboZoomImage.getSelectedIndex() + 1);
            
            highlightTiles();
        }
    }//GEN-LAST:event_comboZoomImageActionPerformed

    private void buttonSaveBMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveBMPActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectorySave));
        chooser.setDialogTitle("Save BMP file");
        chooser.setSelectedFile(new File(this.getTitle().split(" ")[0] + ".bmp"));
        chooser.setFileFilter(new FileNameExtensionFilter("BMP file", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            saveBMP(chooser.getSelectedFile().getAbsolutePath(), checkTransparencyBM.isSelected());
            
            lastDirectorySave = chooser.getSelectedFile().getPath();
            
            JOptionPane.showMessageDialog(null, "File created:\n" + chooser.getSelectedFile().getAbsolutePath(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_buttonSaveBMPActionPerformed

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
            selectPalette();

            listPalettes.requestFocusInWindow();

            lastDirectory = chooser.getSelectedFile().getPath();
        }
    }//GEN-LAST:event_buttonImportBM10ActionPerformed

    private void menuitemBM6FolderToBMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitemBM6FolderToBMPActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Choose directory containing BM6 files");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // disable the "All files" option.
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            lastClickedTileIMG = -1;
            lastClicked = null;
            
            batchExportBMP(chooser.getSelectedFile().getAbsolutePath(), 6);

            lastDirectory = chooser.getSelectedFile().getPath();

            //this.setTitle(chooser.getSelectedFile().getName() + " - " + title);
        }
    }//GEN-LAST:event_menuitemBM6FolderToBMPActionPerformed

    private void menuitemBM9FolderToBMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuitemBM9FolderToBMPActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Choose directory containing BM6 files");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // disable the "All files" option.
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            lastClickedTileIMG = -1;
            lastClicked = null;
            
            batchExportBMP(chooser.getSelectedFile().getAbsolutePath(), 9);

            lastDirectory = chooser.getSelectedFile().getPath();

            //this.setTitle(chooser.getSelectedFile().getName() + " - " + title);
        }
    }//GEN-LAST:event_menuitemBM9FolderToBMPActionPerformed

    private void menuItemSCRFolderToBMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuItemSCRFolderToBMPActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Choose directory containing SCR files - The current BM file will be used.");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

        // disable the "All files" option.
        chooser.setAcceptAllFileFilterUsed(false);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            lastClickedTileIMG = -1;
            lastClicked = null;
            
            batchExportSCR(chooser.getSelectedFile().getAbsolutePath());

            lastDirectory = chooser.getSelectedFile().getPath();

            //this.setTitle(chooser.getSelectedFile().getName() + " - " + title);
        }
    }//GEN-LAST:event_menuItemSCRFolderToBMPActionPerformed

    private void comboColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboColorActionPerformed
        // TODO add your handling code here:
        selectColor();
    }//GEN-LAST:event_comboColorActionPerformed

    private void fieldColorRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldColorRActionPerformed
        // TODO add your handling code here:
        if (checkNumericFields()){
            modifyColor();
        }
    }//GEN-LAST:event_fieldColorRActionPerformed

    private void fieldColorGActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldColorGActionPerformed
        // TODO add your handling code here:
        if (checkNumericFields()){
            modifyColor();
        }
    }//GEN-LAST:event_fieldColorGActionPerformed

    private void fieldColorBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldColorBActionPerformed
        // TODO add your handling code here:
        if (checkNumericFields()){
            modifyColor();
        }
    }//GEN-LAST:event_fieldColorBActionPerformed

    private void fieldColorAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fieldColorAActionPerformed
        // TODO add your handling code here:
        if (checkNumericFields()){
            modifyColor();
        }
    }//GEN-LAST:event_fieldColorAActionPerformed

    private void buttonRestoreColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRestoreColorActionPerformed
        // TODO add your handling code here:
        restoreColor();
    }//GEN-LAST:event_buttonRestoreColorActionPerformed

    private void buttonRestoreAllColorsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonRestoreAllColorsActionPerformed
        // TODO add your handling code here:
        restoreAllColors();
    }//GEN-LAST:event_buttonRestoreAllColorsActionPerformed

    private void buttonExportPaletteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonExportPaletteActionPerformed
        // TODO add your handling code here:
        IndexColorModel cm = palettes.get(lastPalette);
        int pal_size = cm.getMapSize();        
        
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectorySave));
        if (pal_size == 16){
            chooser.setDialogTitle("Save BM7 file");
            chooser.setFileFilter(new FileNameExtensionFilter("BM7 file", "BM7"));
        }
        else{
            chooser.setDialogTitle("Save BM10 file");
            chooser.setFileFilter(new FileNameExtensionFilter("BM10 file", "BM10"));
        }

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            String path = chooser.getSelectedFile().getAbsolutePath();
            
            if ( pal_size == 16 && !path.endsWith(".BM7") )
                path += ".BM7";
            else if ( pal_size == 256 &&  !path.endsWith(".BM10") )
                path += ".BM10";
            
            savePalette(path);

            lastDirectorySave = path;
        }
    }//GEN-LAST:event_buttonExportPaletteActionPerformed

    private void buttonSCRtoBMPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSCRtoBMPActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectorySave));
        chooser.setDialogTitle("Save BMP file");
        chooser.setSelectedFile(new File(labelSCRfile.getText() + ".bmp"));
        chooser.setFileFilter(new FileNameExtensionFilter("BMP file", "bmp"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            saveSCRtoBMP(chooser.getSelectedFile().getAbsolutePath(), checkTransparencySCR.isSelected());

            lastDirectorySave = chooser.getSelectedFile().getPath();

            JOptionPane.showMessageDialog(null, "File created:\n" + chooser.getSelectedFile().getAbsolutePath(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        }
    }//GEN-LAST:event_buttonSCRtoBMPActionPerformed

    private void buttonClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonClearActionPerformed
        // TODO add your handling code here:
        clearTilesSCR();
    }//GEN-LAST:event_buttonClearActionPerformed

    private void buttonSaveSCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonSaveSCRActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectorySave));
        chooser.setDialogTitle("Save SCR file");
        chooser.setFileFilter(new FileNameExtensionFilter("SCR file", "SCR"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            String path = chooser.getSelectedFile().getAbsolutePath();

            if (!path.endsWith(".SCR") && !path.endsWith(".scr"))
            path += ".SCR";

            saveSCR(path);

            lastDirectorySave = path;
        }
    }//GEN-LAST:event_buttonSaveSCRActionPerformed

    private void buttonLoadSCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonLoadSCRActionPerformed
        // TODO add your handling code here:
        JFileChooser chooser = new JFileChooser();
        chooser.setCurrentDirectory(new java.io.File(lastDirectory));
        chooser.setDialogTitle("Load SCR file");
        chooser.setFileFilter(new FileNameExtensionFilter("SCR file", "SCR"));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION){
            selectedSCR = 0;
            lastSCRclicked = null;

            openSCR(chooser.getSelectedFile().getAbsolutePath(), true);

            lastDirectory = chooser.getSelectedFile().getPath();

            labelSCRfile.setText(chooser.getSelectedFile().getName());
        }
    }//GEN-LAST:event_buttonLoadSCRActionPerformed

    private void comboZoomSCRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_comboZoomSCRActionPerformed
        // TODO add your handling code here:
        if(scr_loaded)
        changeZoomSCR(comboZoomSCR.getSelectedIndex() + 1);
    }//GEN-LAST:event_comboZoomSCRActionPerformed

    private void checkFlipVActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFlipVActionPerformed
        // TODO add your handling code here:
        lastSCRclicked.flipVertically(checkFlipV.isSelected());

        int x = selectedSCR % tileDataSCR[0].length;
        int y = selectedSCR / tileDataSCR[0].length;

        tileDataSCR[y][x].flipV = checkFlipV.isSelected();
    }//GEN-LAST:event_checkFlipVActionPerformed

    private void checkFlipHActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_checkFlipHActionPerformed
        // TODO add your handling code here:
        lastSCRclicked.flipHorizontally(checkFlipH.isSelected());

        int x = selectedSCR % tileDataSCR[0].length;
        int y = selectedSCR / tileDataSCR[0].length;

        tileDataSCR[y][x].flipH = checkFlipH.isSelected();
    }//GEN-LAST:event_checkFlipHActionPerformed

    private void buttonResizeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_buttonResizeActionPerformed
        // TODO add your handling code here:
        resizeSCR();
    }//GEN-LAST:event_buttonResizeActionPerformed

    private void textfieldHeightKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textfieldHeightKeyTyped
        // TODO add your handling code here:
        if(evt.getKeyChar()<'0' || evt.getKeyChar()>'9') // only numbers
        evt.consume();
    }//GEN-LAST:event_textfieldHeightKeyTyped

    private void textfieldWidthKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textfieldWidthKeyTyped
        // TODO add your handling code here:
        if(evt.getKeyChar()<'0' || evt.getKeyChar()>'9') // only numbers
        evt.consume();
    }//GEN-LAST:event_textfieldWidthKeyTyped

    private void textfieldAlignKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textfieldAlignKeyTyped
        // TODO add your handling code here:
        if(evt.getKeyChar()<'0' || evt.getKeyChar()>'9') // only numbers
        evt.consume();
    }//GEN-LAST:event_textfieldAlignKeyTyped

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
    
    /*
    Parses either BM7 or BM10 file, containing 4bpp (16 colors) and 8bpp (256 colors) palettes.
    Format is the same in both GC and XO, although in XO, files have extra bytes to be 2048 byte aligned.
    
    In GC, there are 2 BM10 palettes that have a different format: 0007.BM10 and 0016.BM10.
    In these files, height and width data is included in the header, and colors seem to
    be defined with 2 bytes instead of 4... which I can't make any sense of.
    
    The BM9 files that use these palettes don't seem to be very important.
    In fact, they have no SCR files associated to them and most of them
    seem to be the exact same file, so probably early tests.
    
    I'll have to live with just seeing these files in black & white.
    */
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
                if (f.length() != 96 && f.length() != 2048) // XO format is always 2048 bytes long
                    valid = false;
            }
            else{
                // Make sure it's a BM10 file
                if (f.length() != 1056 && f.length() != 2048) // XO format is always 2048 bytes long
                    valid = false;
            }
            
            if (f.length() == 2048){
                checkXOpaddingSCR.setSelected(true);
                checkXOpaddingPalette.setSelected(true);
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
            palettes_mod.add(new IndexColorModel(bpp, pal_size, r, g, b, a));

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
        palettes_mod = new ArrayList<IndexColorModel>();

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
        palettes_mod.add(new IndexColorModel(bpp, pal_size, r, g, b, a));

        modelListPal.clear();
        modelListPal.addElement("<Default palette>");

        listPalettes.setSelectedIndex(0);
    }
    
    // Sets the Palette Edit panel to point at color 0 of the selected palette
    public void initPalEditPanel(){
        // Prepare contents of the combobox with the colors
        // It can be a palette with 16 or 256 colors
        ComboBoxModel m;
        int pal_size = 16;
        if (!buttonImportBM7.isEnabled()){
            pal_size = 256;       
            m = new DefaultComboBoxModel(color256);
            comboColor.setModel(m);
        }
        else{
            m = new DefaultComboBoxModel(color16);
            comboColor.setModel(m);
        }
        
        // Get palette's Rs, Gs, Bs and As
        IndexColorModel cm = palettes_mod.get(lastPalette);

        byte[] r = new byte[pal_size];
        byte[] g = new byte[pal_size];
        byte[] b = new byte[pal_size];
        byte[] a = new byte[pal_size];

        cm.getReds(r);
        cm.getBlues(b);
        cm.getGreens(g);
        cm.getAlphas(a);
        
        // Select entry 0 in combobox
        comboColor.setSelectedIndex(0);
        
        // Fill up the fields with values from color 0
        fieldColorR.setText("" + ( r[0] & 0xff ) );
        fieldColorG.setText("" + ( g[0] & 0xff ) );
        fieldColorB.setText("" + ( b[0] & 0xff ) );
        fieldColorA.setText("" + ( a[0] & 0xff ) );
    }

    // Changes the palette to the currently selected one in the list
    public void selectPalette(){
        // If we're on the same index we were before, do nothing
        if (lastPalette == listPalettes.getSelectedIndex())
            return;

        int pal_size = 16;
        if (!buttonImportBM7.isEnabled())
            pal_size = 256;

        //System.out.println("Changing the palette");
        
        lastPalette = listPalettes.getSelectedIndex();

        IndexColorModel cm = palettes_mod.get(lastPalette);

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

        /*// Re-draw the loaded image (if it's loaded) with the selected ColorModel
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
        }*/        
        
        repaintAll(cm);
        
        initPalEditPanel();
    }

    // Displays RGBA values of selected color in palette
    public void selectColor(){
        // Get palette's Rs, Gs, Bs and As
        IndexColorModel cm = palettes_mod.get(lastPalette);

        byte[] r = new byte[palettes_mod.get(lastPalette).getMapSize()];
        byte[] g = new byte[palettes_mod.get(lastPalette).getMapSize()];
        byte[] b = new byte[palettes_mod.get(lastPalette).getMapSize()];
        byte[] a = new byte[palettes_mod.get(lastPalette).getMapSize()];

        cm.getReds(r);
        cm.getBlues(b);
        cm.getGreens(g);
        cm.getAlphas(a);
        
        // Fill up the fields with values from color 0
        fieldColorR.setText("" + ( r[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorG.setText("" + ( g[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorB.setText("" + ( b[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorA.setText("" + ( a[comboColor.getSelectedIndex()] & 0xff ) );
    }

    // Ensures numeric fields contain numbers between 0 and 255
    public boolean checkNumericFields(){
        boolean ok = true;
        int number = 0;
        
        try{
            number = Integer.parseInt(fieldColorR.getText());

            if (number > 255)
                number = 255;
            else if (number < 0)
                number = 0;
            fieldColorR.setText("" + number);

            fieldColorR.setBackground(new Color(255, 255, 255));
        }
        catch(NumberFormatException e){
            fieldColorR.setBackground(new Color(255, 204, 204));
            ok = false;
        }
        
        try{
            number = Integer.parseInt(fieldColorG.getText());

            if (number > 255)
                number = 255;
            else if (number < 0)
                number = 0;
            fieldColorG.setText("" + number);

            fieldColorG.setBackground(new Color(255, 255, 255));
        }
        catch(NumberFormatException e){
            fieldColorG.setBackground(new Color(255, 204, 204));
            ok = false;
        }
        
        try{
            number = Integer.parseInt(fieldColorB.getText());

            if (number > 255)
                number = 255;
            else if (number < 0)
                number = 0;
            fieldColorB.setText("" + number);

            fieldColorB.setBackground(new Color(255, 255, 255));
        }
        catch(NumberFormatException e){
            fieldColorB.setBackground(new Color(255, 204, 204));
            ok = false;
        }
        
        try{
            number = Integer.parseInt(fieldColorA.getText());

            if (number > 255)
                number = 255;
            else if (number < 0)
                number = 0;
            fieldColorA.setText("" + number);

            fieldColorA.setBackground(new Color(255, 255, 255));
        }
        catch(NumberFormatException e){
            fieldColorA.setBackground(new Color(255, 204, 204));
            ok = false;
        }
        
        return ok;
    }
    
    // Saves changes to color into palette
    public void modifyColor(){
        int bpp = 4;
        int pal_size;
        
        // Get palette's Rs, Gs, Bs and As
        IndexColorModel cm = palettes_mod.get(lastPalette);
        
        pal_size = cm.getMapSize();

        byte[] r = new byte[pal_size];
        byte[] g = new byte[pal_size];
        byte[] b = new byte[pal_size];
        byte[] a = new byte[pal_size];

        cm.getReds(r);
        cm.getBlues(b);
        cm.getGreens(g);
        cm.getAlphas(a);
        
        r[comboColor.getSelectedIndex()] = (byte) ( Integer.parseInt(fieldColorR.getText()) & 0xff );
        g[comboColor.getSelectedIndex()] = (byte) ( Integer.parseInt(fieldColorG.getText()) & 0xff );
        b[comboColor.getSelectedIndex()] = (byte) ( Integer.parseInt(fieldColorB.getText()) & 0xff );
        a[comboColor.getSelectedIndex()] = (byte) ( Integer.parseInt(fieldColorA.getText()) & 0xff );
        
        if (pal_size == 256)
            bpp = 8;
        
        IndexColorModel cm_new = new IndexColorModel(bpp, pal_size, r, g, b, a);
        
        palettes_mod.set(lastPalette, cm_new);
        
        
        Component[] colPanels = panelColours.getComponents();
        
        colPanels[comboColor.getSelectedIndex()].setBackground(new Color(
                                                        r[comboColor.getSelectedIndex()] & 0xff, 
                                                        g[comboColor.getSelectedIndex()] & 0xff, 
                                                        b[comboColor.getSelectedIndex()] & 0xff));
        
        
        panelColours.repaint();
        
        repaintAll(cm_new);
    }
    
    // Restores values for selected color in palette
    public void restoreColor(){
        // Get original palette's Rs, Gs, Bs and As
        IndexColorModel cm = palettes.get(lastPalette);
        IndexColorModel cm_mod = palettes_mod.get(lastPalette);
        int bpp = 4;
        int pal_size = cm.getMapSize();
        if (pal_size == 256)
            bpp = 8;

        byte[] r = new byte[pal_size];
        byte[] g = new byte[pal_size];
        byte[] b = new byte[pal_size];
        byte[] a = new byte[pal_size];

        cm.getReds(r);
        cm.getBlues(b);
        cm.getGreens(g);
        cm.getAlphas(a);
        
        byte[] r2 = new byte[pal_size];
        byte[] g2 = new byte[pal_size];
        byte[] b2 = new byte[pal_size];
        byte[] a2 = new byte[pal_size];

        cm_mod.getReds(r2);
        cm_mod.getBlues(b2);
        cm_mod.getGreens(g2);
        cm_mod.getAlphas(a2);
        
        // Copy values from original to mod
        r2[comboColor.getSelectedIndex()] = r[comboColor.getSelectedIndex()];
        g2[comboColor.getSelectedIndex()] = g[comboColor.getSelectedIndex()];
        b2[comboColor.getSelectedIndex()] = b[comboColor.getSelectedIndex()];
        a2[comboColor.getSelectedIndex()] = a[comboColor.getSelectedIndex()];
                
        // Fill up the fields with values from color 0
        fieldColorR.setText("" + ( r[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorG.setText("" + ( g[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorB.setText("" + ( b[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorA.setText("" + ( a[comboColor.getSelectedIndex()] & 0xff ) );
                
        IndexColorModel cm_new = new IndexColorModel(bpp, pal_size, r2, g2, b2, a2);
        palettes_mod.set(lastPalette, cm_new);
        
        Component[] colPanels = panelColours.getComponents();
        
        colPanels[comboColor.getSelectedIndex()].setBackground(new Color(
                                                        r[comboColor.getSelectedIndex()] & 0xff, 
                                                        g[comboColor.getSelectedIndex()] & 0xff, 
                                                        b[comboColor.getSelectedIndex()] & 0xff));
        
        
        panelColours.repaint();
        
        repaintAll(cm_new);
    }
    
    
    // Restores values for selected color in palette
    public void restoreAllColors(){
        // Get original palette's Rs, Gs, Bs and As
        IndexColorModel cm = palettes.get(lastPalette);
        int pal_size = cm.getMapSize();

        byte[] r = new byte[pal_size];
        byte[] g = new byte[pal_size];
        byte[] b = new byte[pal_size];
        byte[] a = new byte[pal_size];

        cm.getReds(r);
        cm.getBlues(b);
        cm.getGreens(g);
        cm.getAlphas(a);
        
        // Select entry 0 in combobox
        comboColor.getSelectedIndex();
        
        // Fill up the fields with values from color 0
        fieldColorR.setText("" + ( r[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorG.setText("" + ( g[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorB.setText("" + ( b[comboColor.getSelectedIndex()] & 0xff ) );
        fieldColorA.setText("" + ( a[comboColor.getSelectedIndex()] & 0xff ) );
                
        palettes_mod.set(lastPalette, cm);
        
        Component[] colPanels = panelColours.getComponents();
        for (int i = 0; i < colPanels.length; i++){
            colPanels[i].setBackground(new Color(r[i] & 0xff, g[i] & 0xff, b[i] & 0xff));
        }
        
        panelColours.repaint();
        
        repaintAll(cm);
    }
    
    
    public void repaintAll(IndexColorModel cm){
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
            
            labelDimensions.setText("Dimensions (in tiles): " + tilesBMfile[0].length + " x " + tilesBMfile.length);

            if (checkClearOnLoad.isSelected())
                initPalettes();

            findPalettes(filename);

            displayTiles();

            buttonSaveBMP.setEnabled(true);
            checkTransparencyBM.setEnabled(true);
            checkXOpaddingSCR.setEnabled(true);
            buttonClear.setEnabled(true);

            resetSCRsection();
            scr_loaded = false;
            
            tileDataSCR = null;
            highlights = null;
            
            menuItemSCRFolderToBMP.setEnabled(true);

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
        //buttonClear.setEnabled(false);
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
        
        labelAlign.setEnabled(false);
        textfieldAlign.setText("");
        textfieldAlign.setEnabled(false);

        labelZoomSCR.setEnabled(true);
        comboZoomSCR.setEnabled(true);
        
        checkTransparencySCR.setEnabled(false);
        checkXOpaddingSCR.setEnabled(false);
    }


    public void displayTiles(){
        panelTilesIMG.removeAll();

        int zoom = comboZoomImage.getSelectedIndex() + 1;
        int counter = 0;

        for (int i = 0; i < tilesBMfile.length; i++){
            for (int j = 0; j < tilesBMfile[i].length; j++){
                TilePanel tp = new TilePanel(tilesBMfile[i][j], palettes_mod.get(listPalettes.getSelectedIndex()), counter, zoom);
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

        try{
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
                palette_after = true;
                lastPaletteFound = folder + new_name;
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
                    palette_after = false;
                    lastPaletteFound = folder + new_name;   // Serves no purpose in this situation though
                }
            }
        }
        } catch (NumberFormatException ex){
            //Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(null, "Loaded BMx file doesn't start with 4 digits.",
                "Error - Palette files not loaded", JOptionPane.ERROR_MESSAGE);
        }

        // If we found palettes, choose the first one
        if (palettes.size() > 1){
            listPalettes.setSelectedIndex(1);
            lastPalette = 0;
            selectPalette();
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

    
    public String getSCRname(int number){
        String name = "";

        if (number < 10)
            name += "000";
        else if (number < 100)
            name += "00";
        else if (number < 1000)
            name += "0";

        name += number + ".SCR";

        return name;
    }


    public void openSCR(String filename, boolean report){
        try{
            // Read the contents of the file as bytes
            RandomAccessFile f = new RandomAccessFile(filename, "r");

            boolean valid = true;

            // Make sure it's an SCR file
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
            int align = header[16] << 24 | (header[17] & 0xFF) << 16 | (header[18] & 0xFF) << 8 | (header[19] & 0xFF);

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
            displaySCR(report);

            // Enable the rest of the SCR features
            buttonSaveSCR.setEnabled(true);
            buttonSCRtoBMP.setEnabled(true);
            checkTransparencySCR.setEnabled(true);
            checkXOpaddingSCR.setEnabled(true);
            //buttonClear.setEnabled(true);
            buttonResize.setEnabled(true);

            checkFlipH.setEnabled(flipsAllowed);
            checkFlipV.setEnabled(flipsAllowed);

            labelWidth.setEnabled(true);
            labelHeight.setEnabled(true);
            textfieldWidth.setText("" + width);
            textfieldWidth.setEnabled(true);
            textfieldHeight.setText("" + height);
            textfieldHeight.setEnabled(true);
            
            labelAlign.setEnabled(true);
            textfieldAlign.setText("" + align);
            textfieldAlign.setEnabled(true);

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


    public void displaySCR(boolean report){
        panelTilesSCR.removeAll();
        //highlights = null;

        int zoom = comboZoomSCR.getSelectedIndex() + 1;
        int counter = 0;
        int x = 0;
        int y = 0;
        int position = 0;

        // Width and Height in tiles
        int height = tileDataSCR.length;
        int width = tileDataSCR[0].length;
        
        // Last tile
        int last_tile = tilesBMfile.length * tilesBMfile[0].length;
        boolean flag_out_of_bounds = false;

        //int height_img = tilesBM6.length;
        int width_img = tilesBMfile[0].length;

        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                position = tileDataSCR[i][j].position;
                
                if (position >= last_tile){ // tile requested out of the boundaries of the BM6
                    flag_out_of_bounds = true;
                    x = 0;
                    y = 0;
                    tileDataSCR[i][j].position = 0;
                    tileDataSCR[i][j].flipH = false;
                    tileDataSCR[i][j].flipV = false;
                    
                    //System.out.println("Max: " + last_tile + " - Position: " + position);
                }
                else{
                    x = position % width_img;
                    y = position / width_img;
                }

                //System.out.println("Position: " + position + " X: " + x + " Y: " + y);

                TilePanel tp = new TilePanel(tilesBMfile[y][x], palettes_mod.get(listPalettes.getSelectedIndex()), counter, zoom);
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
        
        if (!flag_out_of_bounds)
            highlightTiles();
        
        if (flag_out_of_bounds && report){    // Display a message saying that some tiles have been set to the first one for being out of bounds
            JOptionPane.showMessageDialog(null, "Some tiles were out of bounds and have been set to tile 0.",
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }


    public void clearTilesSCR(){
        int pos = 0;
        
        // If no SCR is loaded, create one with the dimensions of the BM6 / BM9
        if (tileDataSCR == null){
            tileDataSCR = new TileDataSCR[tilesBMfile.length][];
            for (int i = 0; i < tileDataSCR.length; i++){
                tileDataSCR[i] = new TileDataSCR[tilesBMfile[0].length];
                
                for (int j = 0; j < tileDataSCR[0].length; j++)
                    tileDataSCR[i][j] = new TileDataSCR();
            }
            
            
            flipsAllowed = true;    // NEEDS REVISION - There are some cases in which flips are not allowed.
                                    // This could be size-dependant, must investigate cases.
            
            // Fill the SCR panel with the indicated tiles taken from the image
            displaySCR(false);

            // Enable the rest of the SCR features
            buttonSaveSCR.setEnabled(true);
            buttonSCRtoBMP.setEnabled(true);
            checkTransparencySCR.setEnabled(true);
            checkXOpaddingSCR.setEnabled(true);
            //buttonClear.setEnabled(true);
            buttonResize.setEnabled(true);

            checkFlipH.setEnabled(flipsAllowed);
            checkFlipV.setEnabled(flipsAllowed);

            labelWidth.setEnabled(true);
            labelHeight.setEnabled(true);
            textfieldWidth.setText("" + tileDataSCR[0].length);
            textfieldWidth.setEnabled(true);
            textfieldHeight.setText("" + tileDataSCR.length);
            textfieldHeight.setEnabled(true);

            // Set the first tile as selected
            selectedSCR = 0;
            TilePanel tp = (TilePanel) panelTilesSCR.getComponent(0);
            tp.setSelected(true);
            lastSCRclicked = tp;

            checkFlipH.setSelected(tileDataSCR[0][0].flipH);
            checkFlipV.setSelected(tileDataSCR[0][0].flipV);

            scr_loaded = true;
        }

        else{
            //System.out.println("Number of SCR tiles: " + panelTilesSCR.getComponentCount());
            
            if (panelTilesSCR.getComponentCount() == 0) // When creating a new SCR, the tiles could be empty. This generates the tiles
                displaySCR(false);                           // The normal way to reproduce the error: load BM6, load SCR, load BM6, click on New / Load
            
            // Set all tiles to the top-left one (position 0)
            for (int i = 0; i < tileDataSCR.length; i++){
                for (int j= 0; j < tileDataSCR[0].length; j++){
                    tileDataSCR[i][j].flipH = false;
                    tileDataSCR[i][j].flipV = false;
                    tileDataSCR[i][j].position = 0;
                    
                    //System.out.println("Accessing SCR tile: " + pos);
                    
                    //((TilePanel) panelTilesSCR.getComponent(pos)).clearTile();
                    ((TilePanel) panelTilesSCR.getComponent(pos)).setTileImage(
                            ((TilePanel) panelTilesIMG.getComponent(0)).getTileImage() );
                    ((TilePanel) panelTilesSCR.getComponent(pos)).setFlips(false, false);
                    pos++;
                }
            }

            panelTilesSCR.repaint();
        }
        
        //highlights = null;
        highlightTiles();
    }


    public void resizeSCR(){
        try{
            int newWidth = Integer.parseInt(textfieldWidth.getText());
            int newHeight = Integer.parseInt(textfieldHeight.getText());
            
            int width = tileDataSCR[0].length;
            int height = tileDataSCR.length;

            if (newWidth == width && newHeight == height){
                //System.out.println("Dimensions are the same. We don't resize.");
                                
                JOptionPane.showMessageDialog(null, "Dimensions are the same. We don't resize.",
                "Warning", JOptionPane.WARNING_MESSAGE);
                
                return;
            }

            if (newWidth == 0 || newHeight == 0){
                //System.out.println("Having 0 rows / columns is not allowed.");
                
                JOptionPane.showMessageDialog(null, "Having 0 rows / columns is not allowed.",
                "Warning", JOptionPane.WARNING_MESSAGE);
                
                if (newWidth == 0){
                    textfieldWidth.setText("1");
                    newWidth = 1;
                }
                
                if (newHeight == 0){
                    textfieldHeight.setText("1");
                    newHeight = 1;
                }
                //return;
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
            
            displaySCR(false);

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
        int alignXO = 0;
        
        if (!textfieldAlign.getText().isBlank())
            alignXO = Integer.parseInt(textfieldAlign.getText());

        int num_tiles = tilesX * tilesY;
        
        int alignment = 32;
        
        if (checkXOpaddingSCR.isSelected())
            alignment = 2048;

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

        header[16] = (byte) ( (alignXO >> 24) & 0xff );
        header[17] = (byte) ( (alignXO >> 16) & 0xff );
        header[18] = (byte) ( (alignXO >> 8) & 0xff );
        header[19] = (byte) ( alignXO & 0xff );
        
        // Prepare the tile data
        int size = num_tiles * 2;   // 2 bytes per tile
        int extra_bytes = size % alignment;

        if (extra_bytes != 0)   // The tile data has to be 32-byte aligned or 2048-byte aligned in the case of XO files
            size += alignment - extra_bytes - header.length;

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
            
            // Truncate the file (in case we're overwriting)
            scr.setLength(0);

            scr.write(header);
            scr.write(data);

            scr.close();

            //System.out.println(path + " saved successfully.");
            
            JOptionPane.showMessageDialog(null, "File created:\n" + path,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException ex) {
            System.err.println("ERROR: Couldn't write " + path);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    
    public void savePalette(String path){
        IndexColorModel cm_mod = palettes_mod.get(lastPalette);
        int pal_size = cm_mod.getMapSize();
        
        byte[] r = new byte[pal_size];
        byte[] g = new byte[pal_size];
        byte[] b = new byte[pal_size];
        byte[] a = new byte[pal_size];

        cm_mod.getReds(r);
        cm_mod.getBlues(b);
        cm_mod.getGreens(g);
        cm_mod.getAlphas(a);
                
        // Prepare the header
        byte[] header = new byte[32];
        
        header[0] = 'B';
        header[1] = 'M';
        header[2] = 'P';
        
        if (pal_size == 16){
            header[3] = 0x07;
            header[7] = 0x10;
        }
        else{
            header[3] = 0x0a;
            header[6] = 0x01;
        }
        
        // Prepare color data
        byte[] data = new byte[pal_size*4];
        
        int counter = 0;
        
        for (int i = 0; i < data.length; i+=4){
            data[i] = r[counter];
            data[i+1] = g[counter];
            data[i+2] = b[counter];
            data[i+3] = a[counter];
            
            counter++;
        }        
        
        // Prepare padding
        byte[] padding = new byte[2048 - header.length - data.length];
        
        
        try {
            RandomAccessFile pal = new RandomAccessFile(path, "rw");
            
            // Truncate the file (in case we're overwriting)
            pal.setLength(0);

            pal.write(header);
            pal.write(data);
            if (checkXOpaddingPalette.isSelected())
                pal.write(padding);

            pal.close();

            //System.out.println(path + " saved successfully.");
            
            JOptionPane.showMessageDialog(null, "File created:\n" + path,
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (IOException ex) {
            System.err.println("ERROR: Couldn't write " + path);
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public void saveBMP(String path, boolean alt_transparency){
        byte[] CLUT = getCLUT(alt_transparency);
        byte[] imageData = getImageData(tilesBMfile);
        int width = tilesBMfile[0].length * 8; // Each tile is 8 pixels wide
        int height = tilesBMfile.length * 8;   // and 8 pixels high
        byte depth = 0x04;  // 4bpp
        if (!buttonImportBM7.isEnabled())
            depth = 0x08;   // 8bpp

        writeBMP(path, CLUT, imageData, width, height, depth);
    }


    public void saveSCRtoBMP(String path, boolean alt_transparency){
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

        byte[] CLUT = getCLUT(alt_transparency);
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
    public byte[] getCLUT(boolean transparency){
        int clut_size = 64;
        if (!buttonImportBM7.isEnabled())
            clut_size = 1024;

        byte[] clut = new byte[clut_size]; // 16 colours * 4 bytes

        byte[] r = new byte[clut_size / 4];
        byte[] g = new byte[clut_size / 4];
        byte[] b = new byte[clut_size / 4];
        byte[] a = new byte[clut_size / 4];

        int sel = listPalettes.getSelectedIndex();

        palettes_mod.get(sel).getReds(r);
        palettes_mod.get(sel).getGreens(g);
        palettes_mod.get(sel).getBlues(b);
        palettes_mod.get(sel).getAlphas(a);

        int counter = 0;

        for (int i = 0; i < clut_size; i += 4){
            clut[i] = b[counter];   // It seems like Windows BMP files use BGRA colours
            clut[i+1] = g[counter];
            clut[i+2] = r[counter]; // So we have to switch their positions when saving to BMP
            clut[i+3] = a[counter];

            //System.out.println("R: " + r[counter] + " G: " + g[counter] + " B: " + b[counter] + " A: " + a[counter]);

            counter++;
        }
        
        // If we choose to use alternate transparency colour, we'll set the first colour
        // in the palette to violet, a colour very unlikely to be used in the game files.
        // We do this in order to avoid indexed images that only use gray to be
        // recognized as images in grayscale format -> leads to trouble editing them.
        if (transparency){
            clut[0] = (byte) 224;
            clut[1] = (byte) 64;
            clut[2] = (byte) 240;
            
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
            
            // Truncate the file (in case we're overwriting)
            bmp.setLength(0);

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

    
    // Takes all BM6 / BM9 files in a directory and exports them to BMP files, as well as the SCR files associated to them
    // type determines if we're looking for BM6 or BM9 files
    public void batchExportBMP(String directory, int type){
        // Get a list of all BM6 / BM9 files in the folder
        System.out.println("Folder: " + directory);
        File bmpFolder = new File(directory);
        
        File[] listOfFiles;
        
        if (type == 6)
            listOfFiles = bmpFolder.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".BM6")); }
            });
        else
            listOfFiles = bmpFolder.listFiles(new FilenameFilter(){
            public boolean accept(File dir, String filename) {
                return (filename.endsWith(".BM9")); }
            });
            
        
        System.out.println("BMx files found: " + listOfFiles.length);
        
        
        int write_counter = 0;
        
        // For each file found.
        for (int i = 0; i < listOfFiles.length; i++){
            // Load the file using function openBMfile
            // This loads the palettes as well
            openBMfile(listOfFiles[i].getAbsolutePath(), type);
        
            // Save the BM file as BMP (use function saveBMP)
            // This can be done for the first palette or one for each
            if (checkmenuAllPalettes.isSelected()){
                for (int j = 0; j < palettes.size(); j++){
                    listPalettes.setSelectedIndex(j);
                    selectPalette();
                    saveBMP(listOfFiles[i].getAbsolutePath() + "-" + j + ".bmp", checkTransparencyBM.isSelected());
                    write_counter++;
                }
            }
            else{
                saveBMP(listOfFiles[i].getAbsolutePath() + ".bmp", checkTransparencyBM.isSelected());
                write_counter++;
            }
        
            // We determine if we have to skip the palettes to start looking for SCR
            // files or not
            String filename;
            
            if (palette_after && !lastPaletteFound.isEmpty())
                filename = lastPaletteFound;
            else
                filename = listOfFiles[i].getAbsolutePath();
            
            // Get the folder and the name of the BM6 we're loading palettes for
            int pos = filename.lastIndexOf('/');
            if (pos < 0)
                pos = filename.lastIndexOf('\\');
            
            //System.out.println("Filename: " + filename + " - Length: " + filename.length() );
            
            String folder = filename.substring(0, pos + 1);
            String name = filename.substring(pos + 1, pos + 5); // We take the number of the file
            
            int number = Integer.parseInt(name);
            String new_name = "";

            //System.out.println("Number: " + number);
            boolean go_on = true;
            int counter = 1;
            File f;
            
            boolean flag_stop = false;
            
            // While there are SCR files following the BM6 / BM9 file (or its palettes)
            while (go_on && !flag_stop){
                new_name = getSCRname(number + counter);

                f = new File(folder + new_name);

                if (!f.exists()){
                    // Normally, this means to stop.
                    // However, there's a special case with mission numbers:
                    // SCR files are spaced with a distance of 3 (2715, 2718, 2721, ...)
                    // We make sure we're not in that case
                    new_name = getSCRname(number + counter + 2);
                    f = new File(folder + new_name);
                    
                    if (!f.exists())
                        go_on = false;
                    else{
                        counter += 2;
                        flag_stop = true;   // We only do this once
                    }
                }                
                else{
                    // Load the file using function openSCR
                    openSCR(folder + new_name, false);
                    counter++;

                    // Save the SCR file to BMP (use function saveSCRtoBMP)
                    // This can be done for the first palette or one for each
                    if (checkmenuAllPalettes.isSelected()){
                        for (int j = 0; j < palettes.size(); j++){
                            listPalettes.setSelectedIndex(j);
                            selectPalette();
                            saveSCRtoBMP(folder + new_name + "-" + j + ".bmp", checkTransparencySCR.isSelected());
                            write_counter++;
                        }
                    }
                    else{
                        saveSCRtoBMP(folder + new_name + ".bmp", checkTransparencySCR.isSelected());
                        write_counter++;
                    }
                }
            }
        }
            
        JOptionPane.showMessageDialog(null, "Batch finished.\nProcessed " + write_counter + " files.",
            "Finished", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    // Highlights tiles in use in the SCR
    public void highlightTiles(){
        if (highlights != null){
            for (int i = 0; i < highlights.length; i++){
                try{
                TilePanel tp = (TilePanel) panelTilesIMG.getComponent(highlights[i]);
                tp.setHighlighted(false);
                } catch(ArrayIndexOutOfBoundsException ex){
                    System.out.println("Tried to highlight unexistant tile: " + highlights[i]);
                    System.out.println("Position in highlight list: " + i);
                }
            }
        }
        
        if (tileDataSCR != null){
            highlights = new int[tileDataSCR.length * tileDataSCR[0].length];
            int counter = 0;

            for (int i = 0; i < tileDataSCR.length; i++)
                for (int j = 0; j < tileDataSCR[0].length; j++){

                    TilePanel tp = (TilePanel) panelTilesIMG.getComponent(tileDataSCR[i][j].position);
                    tp.setHighlighted(true);

                    highlights[counter] = tileDataSCR[i][j].position;
                    counter++;
                }
        }
    }
    
    
    public void batchExportSCR(String directory){
        // Get a list of all SCRs files in the folder
        System.out.println("Folder: " + directory);
        File bmpFolder = new File(directory);
        
        File[] listOfFiles;
        
        listOfFiles = bmpFolder.listFiles(new FilenameFilter(){
        public boolean accept(File dir, String filename) {
            return (filename.endsWith(".SCR")); }
        });
            
        
        System.out.println("SCR files found: " + listOfFiles.length);
        
        
        int write_counter = 0;
        
        // For each file found.
        for (int i = 0; i < listOfFiles.length; i++){
            // Load the file using function openSCR
            openSCR(listOfFiles[i].getAbsolutePath(), false);

            // Save the SCR file to BMP (use function saveSCRtoBMP)
            // This can be done for the first palette or one for each
            saveSCRtoBMP(listOfFiles[i].getAbsolutePath() + ".bmp", checkTransparencySCR.isSelected());

            write_counter++;
            
        }
            
        JOptionPane.showMessageDialog(null, "Batch finished.\nProcessed " + write_counter + " files.",
            "Finished", JOptionPane.INFORMATION_MESSAGE);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton buttonClear;
    private javax.swing.JButton buttonExportPalette;
    private javax.swing.ButtonGroup buttonGroupTiles;
    private javax.swing.JButton buttonImportBM10;
    private javax.swing.JButton buttonImportBM7;
    private javax.swing.JButton buttonLoad;
    private javax.swing.JButton buttonLoadBM9;
    private javax.swing.JButton buttonLoadSCR;
    private javax.swing.JButton buttonResize;
    private javax.swing.JButton buttonRestoreAllColors;
    private javax.swing.JButton buttonRestoreColor;
    private javax.swing.JButton buttonSCRtoBMP;
    private javax.swing.JButton buttonSaveBMP;
    private javax.swing.JButton buttonSaveSCR;
    private javax.swing.JCheckBox checkClearOnLoad;
    private javax.swing.JCheckBox checkFlipH;
    private javax.swing.JCheckBox checkFlipV;
    private javax.swing.JCheckBox checkTransparencyBM;
    private javax.swing.JCheckBox checkTransparencySCR;
    private javax.swing.JCheckBox checkXOpaddingPalette;
    private javax.swing.JCheckBox checkXOpaddingSCR;
    private javax.swing.JCheckBoxMenuItem checkmenuAllPalettes;
    private javax.swing.JComboBox<String> comboColor;
    private javax.swing.JComboBox comboZoomImage;
    private javax.swing.JComboBox comboZoomSCR;
    private javax.swing.JTextField fieldColorA;
    private javax.swing.JTextField fieldColorB;
    private javax.swing.JTextField fieldColorG;
    private javax.swing.JTextField fieldColorR;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JLabel labelAlign;
    private javax.swing.JLabel labelColorA;
    private javax.swing.JLabel labelColorB;
    private javax.swing.JLabel labelColorG;
    private javax.swing.JLabel labelColorNmbr;
    private javax.swing.JLabel labelColorR;
    private javax.swing.JLabel labelDimensions;
    private javax.swing.JLabel labelHeight;
    private javax.swing.JLabel labelPalettes;
    private javax.swing.JLabel labelSCRfile;
    private javax.swing.JLabel labelWidth;
    private javax.swing.JLabel labelZoomImage;
    private javax.swing.JLabel labelZoomSCR;
    private javax.swing.JList listPalettes;
    private javax.swing.JMenu menuBatch;
    private javax.swing.JMenuItem menuItemSCRFolderToBMP;
    private javax.swing.JMenuBar menubarMain;
    private javax.swing.JMenuItem menuitemBM6FolderToBMP;
    private javax.swing.JMenuItem menuitemBM9FolderToBMP;
    private javax.swing.JPanel panelColours;
    private javax.swing.JPanel panelEditPalette;
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
    private javax.swing.JTextField textfieldAlign;
    private javax.swing.JTextField textfieldHeight;
    private javax.swing.JTextField textfieldWidth;
    // End of variables declaration//GEN-END:variables

}
