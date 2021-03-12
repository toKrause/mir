/*
 * $Id$
 * $Revision$ $Date$
 *
 * This file is part of ***  M y C o R e  ***
 * See http://www.mycore.de/ for details.
 *
 * This program is free software; you can use it, redistribute it
 * and / or modify it under the terms of the GNU General Public License
 * (GPL) as published by the Free Software Foundation; either version 2
 * of the License or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program, in a file called gpl.txt or license.txt.
 * If not, write to the Free Software Foundation Inc.,
 * 59 Temple Place - Suite 330, Boston, MA  02111-1307 USA
 */

package org.mycore.mir.authorization.accesskeys;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jdom2.Element;
import org.jdom2.transform.JDOMSource;

import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mir.authorization.accesskeys.backend.MIRAccessKey;

public class MIRAccessKeyResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        final MCRObjectID objectId = MCRObjectID.getInstance(href.substring(href.indexOf(":") + 1));

        final List<MIRAccessKey> accessKeys = MIRAccessKeyManager.getAccessKeys(objectId);
        
        if (accessKeys.size() == 0) {
            return new JDOMSource(new Element("null"));
        }
        
        try {
            final String json = MIRAccessKeyTransformer.accessKeysToJson(accessKeys);
            final Element servFlag = MIRAccessKeyTransformer.accessKeysJsonToServFlag(json);
            return new JDOMSource(servFlag);
        } catch (JsonProcessingException e) {
            LOGGER.error("Access keys could not be converted.");
            return new JDOMSource(new Element("null"));
        }
    }
}
