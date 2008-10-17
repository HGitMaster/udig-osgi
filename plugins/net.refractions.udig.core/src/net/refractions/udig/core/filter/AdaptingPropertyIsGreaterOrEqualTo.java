package net.refractions.udig.core.filter;

import org.opengis.filter.BinaryComparisonOperator;
import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
import org.opengis.filter.expression.Expression;

/**
 * AdaptingFilter that implements PropertyIsGreaterThanOrEqualTo interface.
 * 
 * @author Jody
 * @since 1.1.0
 */
class AdaptingPropertyIsGreaterOrEqualTo extends AdaptingFilter implements PropertyIsGreaterThanOrEqualTo {

    AdaptingPropertyIsGreaterOrEqualTo( PropertyIsGreaterThanOrEqualTo filter ) {
        super(filter);
    }
    public Expression getExpression1() {
        return ((BinaryComparisonOperator)wrapped).getExpression1();
    }

    public Expression getExpression2() {
        return ((BinaryComparisonOperator)wrapped).getExpression2();
    }

    public boolean isMatchingCase() {
        return ((BinaryComparisonOperator)wrapped).isMatchingCase();
    }
}
