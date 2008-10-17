package net.refractions.udig.core.filter;

import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.expression.Expression;

/**
 * AdaptingFilter that implements PropertyIsGreaterThanOrEqualTo interface.
 * 
 * @author Jody
 * @since 1.1.0
 */
class AdaptingPropertyIsGreaterThanOrEqualTo extends AdaptingFilter implements PropertyIsGreaterThanOrEqualTo{

    AdaptingPropertyIsGreaterThanOrEqualTo( PropertyIsGreaterThanOrEqualTo filter ) {
        super(filter);
    }

    public Expression getExpression1() {
        return ((PropertyIsGreaterThanOrEqualTo)wrapped).getExpression1();
    }

    public Expression getExpression2() {
        return ((PropertyIsGreaterThanOrEqualTo)wrapped).getExpression2();
    }

    public boolean isMatchingCase() {
        return ((PropertyIsGreaterThanOrEqualTo)wrapped).isMatchingCase();
    }

}
