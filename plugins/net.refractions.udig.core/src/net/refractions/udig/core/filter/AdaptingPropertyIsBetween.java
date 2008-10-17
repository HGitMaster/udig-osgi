package net.refractions.udig.core.filter;

import org.opengis.filter.PropertyIsBetween;
import org.opengis.filter.expression.Expression;

/**
 * AdaptingFilter that implements PropertyIsBetween interface.
 * 
 * @author Jody
 * @since 1.1.0
 */
class AdaptingPropertyIsBetween extends AdaptingFilter implements PropertyIsBetween {

    AdaptingPropertyIsBetween( PropertyIsBetween filter ) {
        super(filter);
    }

    public Expression getExpression() {
        return ((PropertyIsBetween)wrapped).getExpression();
    }

    public Expression getLowerBoundary() {
        return ((PropertyIsBetween)wrapped).getLowerBoundary();
    }

    public Expression getUpperBoundary() {
        return ((PropertyIsBetween)wrapped).getUpperBoundary();
    }

}
