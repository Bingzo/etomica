/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.compatibility;

import java.io.Serializable;

public class NumericFeature extends Feature implements Serializable
{
	public NumericFeature( String aname, double avalue ) { super(aname); value=avalue; }
	public int compareTo( Feature feat ) 
	{
		if ( feat==null)
			return IS_EMPTY;
		if ( feat instanceof NumericFeature )
		{
			double other = ((NumericFeature)feat).value;
			if ( other<value )
				return -1;
			else if ( other>value )
				return +1;
			return 0;
		}
		return INCOMPATIBLE_TYPES;
	}
	public boolean compareTo( int operator, Feature feat )
	{
		return compareTo( feat )==operator;
	}
	public double value;
}