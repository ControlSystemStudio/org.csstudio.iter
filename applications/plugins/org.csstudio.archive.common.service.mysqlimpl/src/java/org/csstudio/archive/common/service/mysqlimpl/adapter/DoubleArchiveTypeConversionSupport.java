/*
 * Copyright (c) 2010 Stiftung Deutsches Elektronen-Synchrotron,
 * Member of the Helmholtz Association, (DESY), HAMBURG, GERMANY.
 *
 * THIS SOFTWARE IS PROVIDED UNDER THIS LICENSE ON AN "../AS IS" BASIS.
 * WITHOUT WARRANTY OF ANY KIND, EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE. SHOULD THE SOFTWARE PROVE DEFECTIVE
 * IN ANY RESPECT, THE USER ASSUMES THE COST OF ANY NECESSARY SERVICING, REPAIR OR
 * CORRECTION. THIS DISCLAIMER OF WARRANTY CONSTITUTES AN ESSENTIAL PART OF THIS LICENSE.
 * NO USE OF ANY SOFTWARE IS AUTHORIZED HEREUNDER EXCEPT UNDER THIS DISCLAIMER.
 * DESY HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS,
 * OR MODIFICATIONS.
 * THE FULL LICENSE SPECIFYING FOR THE SOFTWARE THE REDISTRIBUTION, MODIFICATION,
 * USAGE AND OTHER RIGHTS AND OBLIGATIONS IS INCLUDED WITH THE DISTRIBUTION OF THIS
 * PROJECT IN THE FILE LICENSE.HTML. IF THE LICENSE IS NOT INCLUDED YOU MAY FIND A COPY
 * AT HTTP://WWW.DESY.DE/LEGAL/LICENSE.HTM
 */
package org.csstudio.archive.common.service.mysqlimpl.adapter;

import javax.annotation.Nonnull;

/**
 * Type conversions for {@link Double}.
 *
 * @author bknerr
 * @since 10.12.2010
 */
public class DoubleArchiveTypeConversionSupport extends AbstractNumberArchiveTypeConversionSupport<Double> {
    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Double convertFromArchiveString(@Nonnull final String value) {
        return Double.parseDouble(value);
    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    @Nonnull
//    public Collection<Double> convertMultiScalarFromArchiveString(@Nonnull final String values) throws TypeSupportException {
//        final Iterable<String> strings = Splitter.on(ARCHIVE_COLLECTION_ELEM_SEP).split(values);
//        final Iterable<Double> doubles = Iterables.transform(strings, new Function<String, Double>() {
//            @Override
//            @CheckForNull
//            public Double apply(@Nonnull final String from) {
//                return convertFromArchiveString(from);
//            }
//        });
//        int size;
//        try {
//            size = Iterables.size(doubles);
//        } catch (final NumberFormatException e) {
//            throw new TypeSupportException("Values representation is not convertible to Double.", e);
//        }
//        if (Iterables.size(strings) != size) {
//            throw new TypeSupportException("Number of values in string representation does not match the size of the result collection..", null);
//        }
//        return Lists.newArrayList(doubles);
//    }
}
