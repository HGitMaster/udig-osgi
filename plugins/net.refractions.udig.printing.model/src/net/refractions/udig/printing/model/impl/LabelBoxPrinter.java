/* uDig - User Friendly Desktop Internet GIS client
 * http://udig.refractions.net
 * (C) 2004, Refractions Research Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 */
package net.refractions.udig.printing.model.impl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.printing.model.AbstractBoxPrinter;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IMemento;

/**
 * Box printer for map labels.
 * 
 * @author Jesse
 * @since 1.1.0
 */
public class LabelBoxPrinter extends AbstractBoxPrinter {

    /**
     * The amount the Text is inset from the edges
     */
    public static final int INSET = 10;
    
    private static final String SIZE_KEY = "size"; //$NON-NLS-1$
    private static final String STYLE_KEY = "style"; //$NON-NLS-1$
    private static final String FONT_NAME_KEY = "fontName"; //$NON-NLS-1$
    private static final String LABEL_KEY = "label"; //$NON-NLS-1$
    private static final String HORIZ_ALIGN_KEY = "horizontalAlignment"; //$NON-NLS-1$

    private String text = "Set Text"; //$NON-NLS-1$
    private Font font;
    private int padding;
    String preview;
    private int horizontalAlignment = SWT.LEFT;
    private int verticalAlignment = SWT.TOP;   
    private boolean wrap;
    private Color fontColor;

    

    public LabelBoxPrinter() {
        super();
        this.padding = 0;
        fontColor = Color.BLACK;
    }
    
    public LabelBoxPrinter(int padding) {
        super();
        this.padding = padding;
        fontColor = Color.BLACK;
    }

    
    

    
    public int getPadding() {
        return this.padding;
    }
    
    /**
     * Gets the text displayed in the box
     *
     * @return the box text, or null if no text
     */
    public String getText() {
        return text;
    }

    /**
     * Set the text to display in the box.  Null represents
     * no text.
     *
     * @param text the box text.
     */    
    public void setText( String text ) {
        this.text = text;
        setDirty(true);
    }

    public void draw( Graphics2D graphics, IProgressMonitor monitor ) {
        super.draw(graphics, monitor);
        
        int boxWidth = getBox().getSize().width;
        int boxHeight = getBox().getSize().height;
        int availableWidth = boxWidth - 2*padding;
        int availableHeight = boxHeight - 2*padding;
        
        //draw text
        if (font != null && text != null) {
            graphics.setFont(font);
            graphics.setColor(fontColor);            
            int spaceBetweenLines = (int)((float)font.getSize() / 4f);
            
            //calculate vertical position of the first line         
            int y;
            List<String> lines = splitIntoLines(text, availableWidth, graphics, getWrap());   
            
            switch (verticalAlignment) {
            case SWT.CENTER:
                int textHeight = textHeight(lines, spaceBetweenLines, graphics);
                y = padding + (int) ((availableHeight-textHeight)/2);  
                break;
            default:
                y = padding;
                break;
            } //switch
            
            //draw each line
            int x;
            for( int i = 0; i < lines.size(); i++) {
                
                String line = lines.get(i);
                Rectangle2D lineBounds = graphics.getFontMetrics().getStringBounds(line, graphics);
                
                //compute the horizontal alignment of each line
                switch( horizontalAlignment ) {
                case SWT.CENTER:
                    x = (int) ((boxWidth-lineBounds.getWidth())/2);
                    break;
                case SWT.RIGHT:
                    x = (int) (boxWidth-lineBounds.getWidth()) - padding;
                    break;        
                default:
                    // default is left
                    x=padding;
                    break;
                
                } //switch
                
                if (i == 0) {
                    y += font.getSize(); //add "ascent"
                }
                else {
                    y += lineBounds.getHeight(); //add "ascent" + "descent"   
                }                                
                graphics.drawString(line, x, y);
                y += spaceBetweenLines; //add "leading"
                
            }//for
        } //if        
    }

    /**
     * Calculates the height of all lines together, including whitespace 
     * between lines
     *
     * @return height of text block
     */
    private int textHeight(List<String> lines, int spaceBetweenLines, Graphics2D graphics) {
        int height = 0;
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                height += spaceBetweenLines;
            }
            String line = lines.get(i);
            Rectangle2D lineBounds = graphics.getFontMetrics().getStringBounds(line, graphics);
            height += lineBounds.getHeight(); 
        }
        return height;
    }
    
    /**
     * A line wrap algorithm, which splits the given line of text into
     * multiple lines based on the current font size and the available line
     * width
     *
     * @param text
     * @param availableWidth
     * @param graphics
     * @param wrap
     * @return a list of strings, representing the lines
     */
    private List<String> splitIntoLines(String text, int availableWidth, Graphics2D graphics, boolean wrap) {
        
        
        List<String> lines = new ArrayList<String>();
        if (!wrap) {
            lines.add(text);
            return lines;
        }
        
        String[] words = text.split(" ");
        String currentLine = "";
        for (int i = 0; i < words.length; i++) {
            
            String tryLine = (currentLine.equals("")) ? words[i] : (currentLine+" "+words[i]);
            Rectangle2D lineBounds = graphics.getFontMetrics().getStringBounds(tryLine, graphics);
            if (lineBounds.getWidth() > availableWidth) {
                lines.add(currentLine);
                currentLine = words[i]; 
            } 
            else {
                currentLine = tryLine;
            }
        }
        if (!currentLine.equals("")) {
            lines.add(currentLine);
        }
        return lines;
    }
    
    public void createPreview( Graphics2D graphics, IProgressMonitor monitor ) {
        draw(graphics, monitor);
        preview = getText();
        setDirty(false);
    }

    public void save( IMemento memento ) {
        memento.putString(LABEL_KEY, text);
        memento.putInteger(HORIZ_ALIGN_KEY, horizontalAlignment);
        if (font != null) {
            memento.putString(FONT_NAME_KEY, font.getFamily());
            memento.putInteger(STYLE_KEY, font.getStyle());
            memento.putInteger(SIZE_KEY, font.getSize());
        }
    }

    public void load( IMemento memento ) {
        text = memento.getString(LABEL_KEY);
        horizontalAlignment = memento.getInteger(HORIZ_ALIGN_KEY);
        String family = memento.getString(FONT_NAME_KEY);
        if (family != null) {
            int size = memento.getInteger(SIZE_KEY);
            int style = memento.getInteger(STYLE_KEY);
            font = new Font(family, style, size);
        }
    }

    public String getExtensionPointID() {
        return "net.refractions.udig.printing.ui.standardBoxes"; //$NON-NLS-1$
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter( Class adapter ) {
        if (adapter.isAssignableFrom(String.class)) {
            return text;
        }
        return null;
    }

    /**
     * Set the font of the label
     *
     * @param newFont the new font
     */
    public void setFont( Font newFont ) {
        this.font = newFont;
        setDirty(true);
    }

    /**
     * Get the font of the label
     *
     * @return the label font
     */
    public Font getFont() {
        return font;
    }

    /**
     * Sets the horizontal alignment of the label.  Options are: {@link SWT#CENTER}, {@link SWT#RIGHT}, {@link SWT#LEFT}
     *
     * @param newAlignment the new alignment.  One of {@link SWT#CENTER}, {@link SWT#RIGHT}, {@link SWT#LEFT}
     */
    public void setHorizontalAlignment( int newAlignment ) {
        if( newAlignment!=SWT.LEFT && newAlignment!=SWT.CENTER && newAlignment!=SWT.RIGHT){
            throw new IllegalArgumentException("An illegal option was provided"); //$NON-NLS-1$
        }
        this.horizontalAlignment = newAlignment;
        setDirty(true);
    }

    /**
     * Sets the vertical alignment of the label.  Options are: {@link SWT#CENTER}, {@link SWT#TOP}
     *
     * @param newAlignment the new alignment.  One of {@link SWT#CENTER}, {@link SWT#TOP}
     */
    public void setVerticalAlignment( int newAlignment ) {
        if( newAlignment!=SWT.TOP && newAlignment!=SWT.CENTER){
            throw new IllegalArgumentException("An illegal option was provided"); //$NON-NLS-1$
        }
        this.verticalAlignment = newAlignment;
        setDirty(true);
    }
    
    public Color getFontColor() {
        return fontColor;
    }

    public void setFontColor( Color fontColor ) {
        this.fontColor = fontColor;
    }

    public boolean getWrap() {
        return wrap;
    }

    public void setWrap( boolean wrap ) {
        this.wrap = wrap;
    }

    public static String getHORIZ_ALIGN_KEY() {
        return HORIZ_ALIGN_KEY;
    }

}
