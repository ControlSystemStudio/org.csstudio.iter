package org.csstudio.apputil.macros;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/** JUnit test/demo of the MacroUtil
 *  @author Xihui Chen, Kay Kasemir
 */
@SuppressWarnings("nls")
public class MacroUtilTest
{
	@Test
	public void testReplacemacros() throws Exception
	{
	    // The there-are-no-macros case
        final IMacroTableProvider nothing = new MacroTable("");
        assertEquals("No Change", MacroUtil.replaceMacros("No Change", nothing));

        assertEquals("$(undefined)", MacroUtil.replaceMacros("$(undefined)", nothing));
        
	    // Actual macros
	    final IMacroTableProvider macros =
	        new MacroTable("ABC=DEF, 123=456, abc_456_def=789, A=$(B), B=C, C=D, 1=$(2), 2=$(1)");
	    
	    System.out.println("Macros: " + macros);
	    
		//simple test
		String input = "$(ABC)";		
		String result = MacroUtil.replaceMacros(input, macros);
		assertEquals("DEF", result);

		//Both type of braces
        assertEquals("DEF 456", MacroUtil.replaceMacros("${ABC} $(123)", macros));
		
		//nested macro string test
		input = "$($(abc_$(123)_def))";		
		result = MacroUtil.replaceMacros(input, macros);
		assertEquals("$(789)", result);
		
		//nested macro table test
		input = "$(A)";		
		result = MacroUtil.replaceMacros(input, macros);
		assertEquals("C", result);
		
		//throw exception when infinite loop detected
		try 
		{
			input = "$(1)";		
			result = MacroUtil.replaceMacros(input, macros);			
		}
		catch (InfiniteLoopException e)
		{			
			result = "InfiniteLoopException";
		}
		assertEquals("InfiniteLoopException", result);
		
		//special character and boundary test
		input = "($(B))";
		result = MacroUtil.replaceMacros(input, macros);
		assertEquals("(C)", result);
		
		//robust parsing test
		input = "$($($(abc_$(123)_def)))Hello $($($(A)))Best OPI $(ABC)D) Yet ${ABC}))!";
		result = MacroUtil.replaceMacros(input, macros);
		assertEquals("$($(789))Hello $(D)Best OPI DEFD) Yet DEF))!", result);
	}	
}