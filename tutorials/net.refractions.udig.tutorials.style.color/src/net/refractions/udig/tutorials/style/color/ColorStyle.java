package net.refractions.udig.tutorials.style.color;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.project.StyleContent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IMemento;

public class ColorStyle extends StyleContent {
    public static final String ID =
        "net.refractions.udig.tutorials.style.color.colorStyle";
    public ColorStyle() {
        super(ID);
    }
    public Object createDefaultStyle( IGeoResource resource, Color color, IProgressMonitor monitor )
            throws IOException {
        if( resource.canResolve( Color.class )){
            return resource.resolve( Color.class, monitor );
        }
        return null;
    }    
    public Class<Color> getStyleClass() {
        return Color.class;
    }
    public Object load( IMemento memento ) {
        Integer R = memento.getInteger("r");
        Integer G = memento.getInteger("g");
        Integer B = memento.getInteger("b");
        Integer A = memento.getInteger("a");
        int r = R == null ? 0 : (int) R;
        int g = G == null ? 0 : (int) G;
        int b = B == null ? 0 : (int) B;
        int a = A == null ? 255 : (int) A;
        
        return new Color( r, g, b, a );
    }
    
    public Object load( URL url, IProgressMonitor monitor ) throws IOException {
        return null;
    }
    public void save( IMemento memento, Object value ) {
        Color color = (Color) value;
        if (color != null){
            memento.putInteger("r", color.getRed());
            memento.putInteger("g", color.getGreen());
            memento.putInteger("b", color.getBlue());
            memento.putInteger("a", color.getAlpha());
        }
    }
}
