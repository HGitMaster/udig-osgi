package net.refractions.udig.core.filter;

import org.opengis.filter.PropertyIsLessThanOrEqualTo;
import org.opengis.filter.expression.Expression;

/**
 * AdaptingFilter that implements PropertyIsBetween interface.
 * 
 * @author Jody
 * @since 1.1.0
 */
class AdaptingPropertyIsLessThanOrEqualTo extends AdaptingFilter<PropertyIsLessThanOrEqualTo> implements PropertyIsLessThanOrEqualTo{

    AdaptingPropertyIsLessThanOrEqualTo( PropertyIsLessThanOrEqualTo filter ) {
        super(filter);
    }
    public Expression getExpression1() {
        return wrapped.getExpression1();
    }
    public Expression getExpression2() {
        return wrapped.getExpression2();
    }
    public boolean isMatchingCase() {
        return wrapped.isMatchingCase();
    }
    public MatchAction getMatchAction() {
        return wrapped.getMatchAction();
    }
}
