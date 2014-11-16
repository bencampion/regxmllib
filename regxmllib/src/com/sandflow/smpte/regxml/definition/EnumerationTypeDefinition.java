/*
 * Copyright (c) 2014, Pierre-Anthony Lemieux (pal@sandflow.com)
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
package com.sandflow.smpte.regxml.definition;

import com.sandflow.smpte.util.AUID;
import com.sandflow.smpte.util.xml.AUIDAdapter;
import java.util.ArrayList;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
@XmlAccessorType(XmlAccessType.NONE)
public class EnumerationTypeDefinition extends Definition {
    

    @XmlJavaTypeAdapter(value = AUIDAdapter.class)
    @XmlElement(name = "ElementType")
    private AUID elementType;
 

    @XmlElementWrapper(name = "Elements")
    @XmlElement(name = "Element")
    private ArrayList<Element> elements;

    public EnumerationTypeDefinition() {}

    public EnumerationTypeDefinition(Collection<Element> elements) {
        this.elements = new ArrayList<>(elements);
    }

    public AUID getElementType() {
        return elementType;
    }

    public void setElementType(AUID elementType) {
        this.elementType = elementType;
    }

    @Override
    public void accept(DefinitionVisitor visitor) throws DefinitionVisitor.VisitorException {
        visitor.visit(this);
    }

    public Collection<Element> getElements() {
        return elements;
    }

    @XmlType(name = "")
    @XmlAccessorType(value = XmlAccessType.NONE)
    public static class Element {

        @XmlElement(name = "Name")
        private String name;
        @XmlElement(name = "Value")
        private int value;
        @XmlElement(name = "Description")
        private String description;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}
