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
package com.sandflow.smpte.tools;

import com.sandflow.smpte.register.ElementsRegister;
import com.sandflow.smpte.register.GroupsRegister;
import com.sandflow.smpte.register.LabelsRegister;
import com.sandflow.smpte.register.TypesRegister;
import com.sandflow.smpte.register.exception.DuplicateEntryException;
import com.sandflow.smpte.register.exception.InvalidEntryException;
import com.sandflow.smpte.register.importer.ExcelElementsRegister;
import com.sandflow.smpte.register.importer.ExcelGroupsRegister;
import com.sandflow.smpte.register.importer.ExcelLabelsRegister;
import com.sandflow.smpte.register.importer.ExcelTypesRegister;
import com.sandflow.smpte.util.ExcelCSVParser;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 *
 * @author Pierre-Anthony Lemieux (pal@sandflow.com)
 */
public class XLSRegistersToXML {

    private final static String USAGE = "Converts SMPTE Elements Register spreadsheet to an XML representation.\n"
            + "  Usage: BuildXMLElementsRegister (-element | -type | -label | -group) -i xlspath -o xmlpath";

    public static void main(String[] args) throws FileNotFoundException, ExcelCSVParser.SyntaxException, JAXBException, IOException, InvalidEntryException, DuplicateEntryException {
        if (args.length != 5
                || "-?".equals(args[0])
                || (!"-i".equals(args[1]))
                || (!"-o".equals(args[3]))) {

            System.out.println(USAGE);

            return;
        }
        
        Logger.getLogger("").setLevel(Level.OFF);

        FileInputStream f = new FileInputStream(args[2]);

        BufferedWriter writer = new BufferedWriter(new FileWriter(args[4]));

        Object reg;
        JAXBContext ctx;

        if ("-element".equals(args[0])) {

            reg = ExcelElementsRegister.fromXLS(f);
            ctx = JAXBContext.newInstance(ElementsRegister.class);

        } else if ("-type".equals(args[0])) {

            reg = ExcelTypesRegister.fromXLS(f);
            ctx = JAXBContext.newInstance(TypesRegister.class);

        } else if ("-label".equals(args[0])) {

            reg = ExcelLabelsRegister.fromXLS(f);
            ctx = JAXBContext.newInstance(LabelsRegister.class);

        } else if ("-group".equals(args[0])) {

            reg = ExcelGroupsRegister.fromXLS(f);
            ctx = JAXBContext.newInstance(GroupsRegister.class);

        } else {
            System.err.println("Error: bad argument.");

            System.out.println(USAGE);

            return;
        }

        Marshaller m = ctx.createMarshaller();

        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        m.marshal(reg, writer);

        writer.close();
    }
}
