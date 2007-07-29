/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.configuration;

/**
 * Comparator for configurations interface.
 *
 * @since 1.0
 *
 * @author <a href="mailto:herve.quiroz@esil.univ-mrs.fr">Herve Quiroz</a>
 * @version $Revision: 1.4 $, $Date: 2004/06/24 12:35:15 $
 */
public interface ConfigurationComparator
{
    /**
     * Compare two configuration objects.
     *
     * @param a the first configuration
     * @param b the second configuration
     * @return true if the two configurations are identical according to
     *         the implemented rules
     */
    boolean compare(Configuration a, Configuration b);
}
