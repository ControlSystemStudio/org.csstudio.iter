/*******************************************************************************
* Copyright (c) 2010-2013 ITER Organization.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
******************************************************************************/
package org.csstudio.utility.dbparser.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CharStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.csstudio.pvnames.data.Record;
import org.csstudio.pvnames.data.Template;
import org.csstudio.utility.dbparser.antlr.DbRecordLexer;
import org.csstudio.utility.dbparser.exception.DbParsingException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

public class DbUtil {

	public static List<Record> parseDb(String dbFile)
			throws RecognitionException, DbParsingException {
		CharStream cs = new ANTLRStringStream(dbFile);
		DbRecordLexer lexer = new DbRecordLexer(cs);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		RecordDbParser parser = new RecordDbParser();
		parser.parse(tokens);
		parser.transform();
		Template t = parser.getEpicsDb();
		return t.getEPICSRecords();
		// return parseTokens(tokens);
	}

	public static String readFile(IFile file) throws IOException, CoreException {
		StringBuilder out = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				file.getContents()));
		for (String line = br.readLine(); line != null; line = br.readLine())
			out.append(line);
		br.close();
		out.append("\n"); // to avoid EOF issues
		return out.toString();
	}
}
