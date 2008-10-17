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
package net.refractions.udig.tools.edit.support;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Wraps a Coordinate and calculates its position only requested. This allows the point it maps to
 * to be moved around without the more time consuming process of calculating the coordinate
 * location.
 * 
 * @author jones
 * @since 1.1.0
 */
public class LazyCoord extends Coordinate {
    
    /** long serialVersionUID field */
    private static final long serialVersionUID = 8814031966871200006L;

    Point start;
    private EditBlackboard blackboard;
    Coordinate coord;
    PointCoordCalculator pointCoordCalculator;
    private double differenceX;
    private double differenceY;
    
    private final Object obj=new Object();

    public LazyCoord( Point point2, Coordinate coord2, EditBlackboard bb2 ) {
        this.start = point2;
        pointCoordCalculator = new PointCoordCalculator(bb2.pointCoordCalculator);
        this.blackboard = bb2;
        this.coord = coord2;
        Coordinate calculated = pointCoordCalculator.toCoord(start);
        this.differenceX=calculated.x-coord.x;
        this.differenceY=calculated.y-coord.y;
        super.x=coord.x;
        super.y=coord.y;
        super.z=coord.z;
    }

    public LazyCoord( LazyCoord coord2 ) {
        this( coord2.start, coord2.coord, coord2.blackboard );
    }

    public Coordinate get( Point p ) {

        boolean sameTransform = pointCoordCalculator.toScreen
                .equals(blackboard.pointCoordCalculator.toScreen);

        if (p.equals(start) && sameTransform){
            super.x=coord.x;
            super.y=coord.y;
            super.z=coord.z;
            return new Coordinate(this);
        }

        double[] startEndDelta = new double[2];
        Coordinate endTransform = blackboard.pointCoordCalculator.toCoord(p);

        startEndDelta[0] = endTransform.x - coord.x;
        startEndDelta[1] = endTransform.y - coord.y;

        coord.x = coord.x + startEndDelta[0]-differenceX;
        coord.y = coord.y + startEndDelta[1]-differenceY;
        super.x=coord.x;
        super.y=coord.y;
        pointCoordCalculator = new PointCoordCalculator(blackboard.pointCoordCalculator);
        start = p;

        return new Coordinate(this);
    }

    public void set( Coordinate o, Point point ) {
        coord = o;
        super.x=o.x;
        super.y=o.y;
        super.z=o.z;
        start = point;
    }

    @Override
    public String toString() {
        return start.toString();
    }
    
    @Override
    public boolean equals( Object obj ) {
        return (this == obj);
    }
    
    @Override
    public int hashCode() {
        return obj.hashCode();
    }

}
