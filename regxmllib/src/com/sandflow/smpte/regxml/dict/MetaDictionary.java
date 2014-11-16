/*
 * Copyright (c) 2014, pal
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
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
package com.sandflow.smpte.regxml.dict;

import com.sandflow.smpte.regxml.definition.OpaqueTypeDefinition;
import com.sandflow.smpte.regxml.definition.VariableArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.IndirectTypeDefinition;
import com.sandflow.smpte.regxml.definition.ClassDefinition;
import com.sandflow.smpte.regxml.definition.RenameTypeDefinition;
import com.sandflow.smpte.regxml.definition.CharacterTypeDefinition;
import com.sandflow.smpte.regxml.definition.StreamTypeDefinition;
import com.sandflow.smpte.regxml.definition.PropertyDefinition;
import com.sandflow.smpte.regxml.definition.StrongReferenceTypeDefinition;
import com.sandflow.smpte.regxml.definition.StringTypeDefinition;
import com.sandflow.smpte.regxml.definition.Definition;
import com.sandflow.smpte.regxml.definition.WeakReferenceTypeDefinition;
import com.sandflow.smpte.regxml.definition.SetTypeDefinition;
import com.sandflow.smpte.regxml.definition.ExtendibleEnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.EnumerationTypeDefinition;
import com.sandflow.smpte.regxml.definition.PropertyAliasDefinition;
import com.sandflow.smpte.regxml.definition.IntegerTypeDefinition;
import com.sandflow.smpte.regxml.definition.FixedArrayTypeDefinition;
import com.sandflow.smpte.regxml.definition.RecordTypeDefinition;
import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.UL;
import com.sandflow.smpte.util.UUID;
import com.sandflow.smpte.util.xml.ULAdapter;
import com.sandflow.smpte.util.xml.UUIDAdapter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author pal
 */
@XmlRootElement(name = "Baseline", namespace = "http://www.smpte-ra.org/schemas/2001-1b/2014/metadict")
@XmlAccessorType(XmlAccessType.NONE)
public class MetaDictionary {

    @XmlAttribute(name = "rootElement", required = true)
    private final static String rootElement = "MXF";

    @XmlAttribute(name = "rootObject", required = true)
    private final static String rootObject = "Preface";
    
    @XmlJavaTypeAdapter(value = UUIDAdapter.class)
    @XmlElement(name = "SchemeID", required = true)
    private UUID schemeID;
    
    @XmlElement(name = "SchemeURI", required = true)
    private URI schemeURI;
    
    @XmlElement(name = "Description")
    private String description;

    private final ArrayList<Definition> definitions = new ArrayList<>();
    private final HashMap<AUID, Definition> definitionsByAUID = new HashMap<>();
    private final HashMap<String, Definition> definitionsBySymbol = new HashMap<>();
    private final HashMap<AUID, Set<Definition>> membersOf = new HashMap<>();

    private MetaDictionary() {
    }

    public MetaDictionary(URI scheme, Collection<Definition> inputdefs) throws DuplicateDefinitionException {
        
         MessageDigest digest;
         
      UUID nsid = UUID.fromURN("urn:uuid:6ba7b810-9dad-11d1-80b4-00c04fd430c8");

         /* BUG: ST 2001-1 does not allow label to be used in multiple enumerations */
        
        try {
            digest = MessageDigest.getInstance("SHA-1");
            digest.update(nsid.getValue());
            digest.update(scheme.toString().getBytes("ASCII"));
        } catch (NoSuchAlgorithmException |UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        
        /* TODO: refactor to UUID class */
        
        byte[] result = digest.digest();
        
        result[6] = (byte) ((result[6] & 0x0f) | 0xaf);
        result[8] = (byte) ((result[8] & 0x3f) | 0x7f);
        
        
       this.schemeID = new UUID(result);
        this.schemeURI = scheme;
       

        for (Definition def : inputdefs) {

            AUID defid = createNormalizedAUID(def.getIdentification());

            if (this.definitionsByAUID.put(defid, def) != null) {
                throw new DuplicateDefinitionException("Duplicate AUID: " + def.getIdentification());
            }

            if (this.definitionsBySymbol.put(def.getSymbol(), def) != null) {
                throw new DuplicateDefinitionException("Duplicate Symbol: " + def.getSymbol());
            }

            if (def instanceof ClassDefinition) {
                this.membersOf.getOrDefault(defid, new HashSet<>()).add(def);
            }
            
            this.definitions.add(def);
        }

    }

    public static String getRootElement() {
        return rootElement;
    }

    public static String getRootObject() {
        return rootObject;
    }

    public UUID getSchemeID() {
        return schemeID;
    }

    public URI getSchemeURI() {
        return schemeURI;
    }

    public String getDescription() {
        return description;
    }
    
    

    public Collection<Definition> getMembersOf(ClassDefinition def) {
        return membersOf.get(def.getIdentification());
    }

    @XmlElementWrapper(name = "MetaDefinitions")
    @XmlElements(value = {
        @XmlElement(name = "ClassDefinition",
                type = ClassDefinition.class),
        @XmlElement(name = "PropertyDefinition",
                type = PropertyDefinition.class),
        @XmlElement(name = "PropertyAliasDefinition",
                type = PropertyAliasDefinition.class),
        @XmlElement(name = "TypeCharacterDefinition",
                type = CharacterTypeDefinition.class),
        @XmlElement(name = "TypeEnumerationDefinition",
                type = EnumerationTypeDefinition.class),
        @XmlElement(name = "TypeExtendibleEnumerationDefinition",
                type = ExtendibleEnumerationTypeDefinition.class),
        @XmlElement(name = "TypeFixedArrayDefinition",
                type = FixedArrayTypeDefinition.class),
        @XmlElement(name = "TypeIndirectDefinition",
                type = IndirectTypeDefinition.class),
        @XmlElement(name = "TypeIntegerDefinition",
                type = IntegerTypeDefinition.class),
        @XmlElement(name = "TypeOpaqueDefinition",
                type = OpaqueTypeDefinition.class),
        @XmlElement(name = "TypeRecordDefinition",
                type = RecordTypeDefinition.class),
        @XmlElement(name = "TypeRenameDefinition",
                type = RenameTypeDefinition.class),
        @XmlElement(name = "TypeSetDefinition",
                type = SetTypeDefinition.class),
        @XmlElement(name = "TypeStreamDefinition",
                type = StreamTypeDefinition.class),
        @XmlElement(name = "TypeStringDefinition",
                type = StringTypeDefinition.class),
        @XmlElement(name = "TypeStrongReferenceDefinition",
                type = StrongReferenceTypeDefinition.class),
        @XmlElement(name = "TypeVariableArrayDefinition",
                type = VariableArrayTypeDefinition.class),
        @XmlElement(name = "TypeWeakReferenceDefinition",
                type = WeakReferenceTypeDefinition.class)
    })
    public ArrayList<Definition> getDefinitions() {
        return definitions;
    }

    public Definition getDefinition(AUID id) {
        return definitionsByAUID.get(createNormalizedAUID(id));
    }

    public Definition getDefinition(UL id) {
        return definitionsByAUID.get(new AUID(createNormalizedUL(id)));
    }

    public Definition getDefinition(String symbol) {
        return definitionsBySymbol.get(symbol);
    }

    public static AUID createNormalizedAUID(AUID auid) {

        if (auid.isUL()) {

            return new AUID(createNormalizedUL(auid.asUL()));
        } else {
            return auid;
        }
    }

    public static UL createNormalizedUL(UL ul) {
        byte[] value = ul.getValue().clone();
        /* set version to 0 */

        value[7] = 0;

        if (ul.isGroup()) {

            /* set byte 6 to 0x7f */
            value[5] = 0x7f;

        }

        return new UL(value);
    }

    public static String createQualifiedSymbol(String namespace, String symbol) {
        if (namespace == null || namespace.length() == 0) {
            return symbol;
        } else {
            return namespace + " " + symbol;
        }
    }
    
    
    public void toXML(Writer writer) throws JAXBException, IOException {
                        
        JAXBContext ctx = JAXBContext.newInstance(MetaDictionary.class);

        Marshaller m = ctx.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        m.marshal(this, writer);
        writer.close();
    }
    
      public static MetaDictionary fromXML(Reader reader) throws JAXBException, IOException, DuplicateDefinitionException {
                        
        JAXBContext ctx = JAXBContext.newInstance(MetaDictionary.class);

        Unmarshaller m = ctx.createUnmarshaller();
        MetaDictionary md = (MetaDictionary) m.unmarshal(reader);
        
         for (Definition def : md.definitions) {

            AUID defid = createNormalizedAUID(def.getIdentification());

            if (md.definitionsByAUID.put(defid, def) != null) {
                throw new DuplicateDefinitionException("Duplicate AUID: " + def.getIdentification());
            }

            if (md.definitionsBySymbol.put(def.getSymbol(), def) != null) {
                throw new DuplicateDefinitionException("Duplicate Symbol: " + def.getSymbol());
            }

            if (def instanceof ClassDefinition) {
                md.membersOf.getOrDefault(defid, new HashSet<>()).add(def);
            }
            
        }
        
        return md;
    }  
}
