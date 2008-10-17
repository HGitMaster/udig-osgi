package net.refractions.udig.graph.internal;

import java.util.ArrayList;
import java.util.List;

import net.refractions.udig.project.IBlackboard;
import net.refractions.udig.project.ILayer;
import net.refractions.udig.ui.operations.IOp;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.geotools.graph.path.DijkstraShortestPathFinder;
import org.geotools.graph.path.Path;
import org.geotools.graph.structure.Edge;
import org.geotools.graph.structure.Graph;
import org.geotools.graph.structure.Node;
import org.geotools.graph.traverse.standard.DijkstraIterator;

import com.vividsolutions.jts.geom.Point;

public class FindPathOp implements IOp {

    public void op( Display display, Object target, IProgressMonitor monitor ) throws Exception {
        long start = System.nanoTime();

        ILayer layer = (ILayer) target;
        IBlackboard mapboard = layer.getMap().getBlackboard();

        if ( !mapboard.contains( "graph" ) || !mapboard.contains( "waypoints" ) ) {
            return;
        }

        Graph graph = (Graph ) mapboard.get( "graph" );
        ArrayList<Node> list = (ArrayList<Node> ) mapboard.get( "waypoints" );
        if ( list.size() < 2 ) {
            return;
        }

        DijkstraIterator.EdgeWeighter weighter = new DijkstraIterator.EdgeWeighter() {
            public double getWeight( Edge e ) {
                Point a = (Point ) e.getNodeA().getObject();
                Point b = (Point ) e.getNodeB().getObject();
                double dx = a.getX() - b.getX();
                double dy = a.getY() - b.getY();

                return Math.sqrt( dx * dx + dy * dy );
            }
        };

        List<Edge> edgeList = new ArrayList<Edge>();
        for ( int i = 0; i < list.size() - 1; i++ ) {
            DijkstraShortestPathFinder pf = new DijkstraShortestPathFinder( graph, list.get( i ), weighter );
            pf.calculate();
            Path path = pf.getPath( list.get( i + 1 ) );
            if ( path != null ) {
                edgeList.addAll( path.getEdges() );
            }
        }
        mapboard.put( "path", edgeList );
        layer.refresh( null );

        monitor.done();
        long end = System.nanoTime();
        System.out.println( Double.toString( ((end - start) / 1000) / 1000.0 ) + "ms" );
    }
}
