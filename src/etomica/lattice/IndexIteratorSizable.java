/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package etomica.lattice;

/**
 * Interface for an iterator in which the number of indices
 * in each dimension may be specified.
 */

public interface IndexIteratorSizable extends IndexIterator {

    /**
     * Specifies maximum index in each dimension (for dimension
     * i, maximum index is size[i]-1).
     */
    public void setSize(int[] size);
}
