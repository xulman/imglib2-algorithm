/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (c) 2009 - 2016, Tobias Pietzsch, Stephan Preibisch, Stephan Saalfeld,
 * John Bogovic, Albert Cardona, Barry DeZonia, Christian Dietz, Jan Funke,
 * Aivar Grislis, Jonathan Hale, Philipp Hanslovsky, Grant Harris, Stefan Helfrich,
 * Mark Hiner, Martin Horn, Steffen Jaensch, Lee Kamentsky, Larry Lindsey,
 * Melissa Linkert, Mark Longair, Brian Northan, Nick Perry, Dimiter Prodanov,
 * Curtis Rueden, Johannes Schindelin, Jean-Yves Tinevez and Michael Zinsmaier.
 * All rights reserved.       
 *                            
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *                            
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *                            
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *                            
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package net.imglib2.algorithm.fill;

import net.imglib2.Cursor;
import net.imglib2.Point;
import net.imglib2.algorithm.neighborhood.DiamondShape;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.integer.LongType;
import net.imglib2.util.Pair;
import net.imglib2.view.ExtendedRandomAccessibleInterval;
import net.imglib2.view.Views;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Philipp Hanslovsky &lt;hanslovskyp@janelia.hhmi.org&gt;
 */
public class FloodFillTest {

    private static final long START_LABEL = 1;
    private static final long FILL_LABEL = 2;
    private static final int[] N_DIMS = { 1, 2, 3, 4 };
    private static final int SIZE_OF_EACH_DIM = 60;


    private static < T extends IntegerType < T > > void runTest(
            int nDim,
            int sizeOfEachDim,
            ImgFactory< T > imageFactory,
            T t )
    {
        long[] dim = new long[nDim];
        long[] c = new long[nDim];
        long r = sizeOfEachDim / 4;
        for ( int d = 0; d < nDim; ++d ) {
            dim[d] = sizeOfEachDim;
            c[d] = sizeOfEachDim / 3;
        }

        long divisionLine = r / 3;

        Img<T> img = imageFactory.create(dim, t.copy() );
        Img<T> refImg = imageFactory.create(dim, t.copy());

        for (Cursor<T> i = img.cursor(), ref = refImg.cursor(); i.hasNext(); )
        {
            i.fwd();
            ref.fwd();
            long diffSum = 0;
            for ( int d = 0; d < nDim; ++d )
            {
                long diff = i.getLongPosition( d ) - c[d];
                diffSum += diff * diff;

            }

            if ( ( diffSum < r * r ) ) {
                if ((i.getLongPosition(0) - c[0] < divisionLine)) {
                    i.get().setInteger(START_LABEL);
                    ref.get().setInteger( FILL_LABEL );
                } else if (i.getLongPosition(0) - c[0] > divisionLine) {
                    i.get().setInteger( START_LABEL );
                    ref.get().setInteger( START_LABEL );
                }
            }

        }

        T fillLabel = t.createVariable();
        fillLabel.setInteger( FILL_LABEL );

        ExtendedRandomAccessibleInterval<T, Img<T>> extendedImg = Views.extendValue(img, fillLabel);

        Filter<Pair<T, T>, Pair<T, T>> filter = new Filter< Pair< T, T >, Pair< T, T > >() {
            @Override
            public boolean accept(Pair< T, T > p1, Pair< T, T > p2) {
                return ( p1.getB().getIntegerLong() != p2.getB().getIntegerLong() ) &&
                        ( p1.getA().getIntegerLong() == p2.getA().getIntegerLong() );
            }
        };

        FloodFill.fill( extendedImg, extendedImg, new Point( c ), fillLabel, new DiamondShape( 1 ), filter );

        for ( Cursor< T > imgCursor = img.cursor(), refCursor = refImg.cursor(); imgCursor.hasNext(); )
        {
            Assert.assertEquals( refCursor.next(), imgCursor.next() );
        }


    }

    @Test
    public void runTests()
    {
        for ( int nDim : N_DIMS )
        {
            runTest(
                    nDim,
                    SIZE_OF_EACH_DIM,
                    new ArrayImgFactory<LongType>(),
                    new LongType()
            );
        }
    }

}
