package org.openqa.selenium;

import java.io.*;

import org.mozilla.javascript.*;

/**
 * Xlator
 *
 */
public class Xlator 
{
    public static void main( String[] args ) throws Exception
    {
        // Creates and enters a Context. The Context stores information
        // about the execution environment of a script.
        Context cx = Context.enter();
        try {
            // Initialize the standard objects (Object, Function, etc.)
            // This must be done before scripts can be executed. Returns
            // a scope object that we use in later calls.
            Scriptable scope = cx.initStandardObjects();

            loadJSSource(cx, scope, "C:\\svn\\selenium-ide\\trunk\\src\\content\\formats\\html.js");
            loadJSSource(cx, scope, "C:\\svn\\selenium-ide\\trunk\\src\\content\\testCase.js");
            loadJSSource(cx, scope, "C:\\svn\\selenium-ide\\trunk\\src\\content\\tools.js");
            
//          add window.editor.seleniumAPI
            Scriptable seleniumAPI = (Scriptable) cx.evaluateString(scope, "window = new Object(); window.editor = new Object(); window.editor.seleniumAPI = new Object();", "<JavaEval>", 1, null);
            loadJSSource(cx, seleniumAPI, "C:\\svn\\selenium\\trunk\\code\\javascript\\core\\scripts\\selenium-api.js");            
            
            String htmlSource = loadFile("C:\\svn\\selenium\\trunk\\code\\javascript\\tests\\TestClick.html");
            
            // add log.debug
            cx.evaluateString(scope, "log = new Object(); log.debug = function(msg) { }", "<JavaEval>", 1, null);
            
            Function parse = getFunction(scope, "parse");
            Scriptable myTestCase = cx.newObject(scope);
            parse.call(cx, scope, scope, new Object[] {myTestCase, htmlSource});

            loadJSSource(cx, scope, "C:\\svn\\selenium-ide\\trunk\\src\\content\\formats\\merged-java-rc.js");
            
            Function format = getFunction(scope, "format");
            Object result = format.call(cx, scope, scope, new Object[] {myTestCase, "foo"});
            
            System.out.println(result);

        } finally {
            // Exit from the context.
            Context.exit();
        }
    }
    
    public static String loadFile(String fileName) throws IOException {
        Reader is = new FileReader(fileName);
        StringBuffer sb = new StringBuffer( );
        char[] b = new char[8192];
        int n;

        // Read a block. If it gets any chars, append them.
        while ((n = is.read(b)) > 0) {
            sb.append(b, 0, n);
        }

        // Only construct the String object once, here.
        return sb.toString( );
    }
    
    public static void loadJSSource(Context cx, Scriptable scope, String fileName) throws IOException {
        String source = loadFile(fileName);
        cx.evaluateString(scope, source, fileName, 1, null);
    }
    
    public static Function getFunction(Scriptable scope, String functionName) {
        Object fObj = scope.get(functionName, scope);
        if (!(fObj instanceof Function)) {
            throw new RuntimeException(functionName + " is undefined or not a function.");
        } else {
            return (Function) fObj;
        }
    }
}
