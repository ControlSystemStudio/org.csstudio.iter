/*******************************************************************************
 * Copyright (c) 2010-2018 ITER Organization.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.csstudio.help.generation.html;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.csstudio.help.generation.Activator;
import org.csstudio.help.generation.preferences.Preferences;
import org.eclipse.help.HelpSystem;
import org.eclipse.help.IToc;
import org.eclipse.help.ITopic;
import org.eclipse.help.UAContentFilter;
import org.eclipse.help.base.AbstractHelpScope;
import org.eclipse.help.internal.HelpPlugin;
import org.eclipse.help.internal.base.HelpEvaluationContext;
import org.eclipse.help.internal.base.scope.ScopeUtils;
import org.eclipse.help.internal.search.HTMLDocParser;
import org.eclipse.help.internal.webapp.data.UrlUtil;
import org.eclipse.help.internal.xhtml.DynamicXHTMLProcessor;
import org.eclipse.help.webapp.TocSystem;

public class GenerateHTML {

    private IToc[] tocs;
    private Set < String > imgAdded = new HashSet<>();
    private Set < String > tocLoads = new HashSet<>();
    private Set < String > refLoads = new HashSet<>();
    // images directory
    private final static String IMAGES_DIRECTORY = "images/";
    private final static String RESOURCE_DIRECTORY = "src/site/resources/";
    private final static String GENERATED_DIRECTORY = Preferences.getGeneratedDocumentationPath();
    private final static String GENERATED_DIRECTORY_SRC = GENERATED_DIRECTORY + "/src/site";
    private final static String GENERATED_DIRECTORY_XDOC = GENERATED_DIRECTORY_SRC + "/xdoc/";

    private List < Chapter >summary = new LinkedList<>();
    private Chapter current;

    public void create() throws Exception {
        loadHelp();
        writeBeginningHelp();
        launchMvnPdf();
    }

    private void launchMvnPdf() throws Exception {
        ProcessBuilder builder = new ProcessBuilder("/opt/codac/bin/switch-maven-operation", "status");
        Process process = builder.start();
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String output = stdInput.readLine();
        stdInput.close();
        boolean isOffline = output.contains("offline");
        process.waitFor();

        builder = new ProcessBuilder("sudo", "/opt/codac/bin/switch-maven-operation", "online");
        builder.start().waitFor();

        if (isOffline) {
            builder = new ProcessBuilder("sudo", "/opt/codac/bin/switch-maven-operation", "offline");
            builder.start().waitFor();
        }

        File pathToExecutable = new File("/usr/bin/mvn");
        builder = new ProcessBuilder(pathToExecutable.getAbsolutePath(), "pdf:pdf");
        builder.directory(new File(GENERATED_DIRECTORY).getAbsoluteFile());
        builder.redirectErrorStream(true);
        process = builder.start();

        stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String s = null;
        StringBuffer errorBuf = new StringBuffer();
        while ((s = stdInput.readLine()) != null) {
            errorBuf.append(s).append("\n");
        }
        Activator.getLogger().log(Level.SEVERE, errorBuf.toString());

        int result = process.waitFor();
        if (result != 0) {
            throw new Exception(errorBuf.append("Generation of the pdf documentation failed : ").toString());
        }
    }

    private void writeBeginningHelp() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/template/template_pdf.xml");
            String template = getStringFromInputStream(inputStream);
            inputStream.close();

            StringBuffer itemsContent = new StringBuffer();
            for (Chapter chapter : summary) {
                recursiveSysoutChapter(itemsContent, chapter, 0);
            }
            template = template.replaceAll("\\$item", itemsContent.toString());

            File file = new File(GENERATED_DIRECTORY_SRC + File.separator + "pdf.xml");
            file.createNewFile();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file)));
            writer.write(template);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            Activator.getLogger().log(Level.WARNING, "error", e);
        }
    }

    private static String getStringFromInputStream(InputStream is) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        String line;
        try {
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return sb.toString();

    }

    private void loadHelp() {
        try {
            deleteFolder(new File(GENERATED_DIRECTORY));
            Files.createDirectories(Paths.get(GENERATED_DIRECTORY_XDOC));
            Files.createDirectories(Paths.get(GENERATED_DIRECTORY + File.separator + IMAGES_DIRECTORY));
            Files.createDirectories(Paths.get(GENERATED_DIRECTORY + File.separator + RESOURCE_DIRECTORY));

            //create directory for maven build
            //cp pom.xml to generated dir
            copyResourceTo("/template/pom.xml", GENERATED_DIRECTORY + "/pom.xml");
            copyResourceTo("/template/pdf-config.xml", GENERATED_DIRECTORY + File.separator + RESOURCE_DIRECTORY + "/pdf-config.xml");
            copyResourceTo("/images/css.png", GENERATED_DIRECTORY + File.separator + IMAGES_DIRECTORY + "/css.png");
            copyResourceTo("/images/top_logo.png", GENERATED_DIRECTORY + File.separator + IMAGES_DIRECTORY + "/top_logo.png");

            tocs = HelpPlugin.getTocManager().getTocs(Locale.getDefault().toString());
            List < IToc > tocsTmp = new ArrayList<>();

            String[] tocFilter = Preferences.getTocFilter();
            for (int i = 0; i < tocFilter.length; i++) {
                for (int j = 0; j < tocs.length; j++) {

                    if (tocs[j].getLabel().equals(tocFilter[i].trim())) {
                        tocsTmp.add((IToc) tocs[j]);
                    }
                }
            }

            tocs = new IToc[tocsTmp.size()];
            int cpt = 0;
            for (IToc itoc : tocsTmp) {
                tocs[cpt++] = itoc;
            }

            //copy pics from help webapp
            CodeSource src = TocSystem.class.getProtectionDomain().getCodeSource();
            CopyOption[] options = new CopyOption[]{ StandardCopyOption.REPLACE_EXISTING };
            if (src != null) {
                URL jar = src.getLocation();
                ZipInputStream zip = new ZipInputStream(jar.openStream());
                ZipFile zipFile = new ZipFile(jar.getFile());

                while (true) {
                    ZipEntry e = zip.getNextEntry();
                    if (e == null)
                        break;
                    String name = e.getName();
                    if (name.startsWith("advanced/images/") && !e.isDirectory()) {
                        int index = name.lastIndexOf("/");
                        if (index > 0 && index + 1 < name.length()) {
                            name = name.substring(index + 1);
                        }
                        InputStream is = zipFile.getInputStream(e);
                        if (is != null) {
                            Files.copy(is, Paths.get(GENERATED_DIRECTORY + File.separator + IMAGES_DIRECTORY + name), options);
                            is.close();
                        }
                    }
                }
                zipFile.close();
            }

            for (int i = 0; i < tocs.length; i++) {
                String directoryToGenerate = GENERATED_DIRECTORY_XDOC + File.separator + tocs[i].getLabel();
                Files.createDirectories(Paths.get(directoryToGenerate));
                current = new Chapter(directoryToGenerate, tocs[i].getLabel(), "");
                generateBasicContentToc(i, directoryToGenerate, current);
                summary.add(current);
            }
        } catch (IOException e) {
            Activator.getLogger().log(Level.WARNING, "error", e);
        }
    }

    /**
     * @throws IOException
     */
    private void copyResourceTo(String source, String dest) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = getClass().getResourceAsStream(source);
            Files.copy(inputStream, Paths.get(dest));
        } catch (Exception e) {
            throw e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    private void recursiveSysoutChapter(StringBuffer content, Chapter chapter, int level) throws IOException {
        String ref = chapter.getPath();

        List < Chapter > subchap = chapter.getSubChapter();
        boolean xmlPathFile = ref.endsWith(".xml.vm");

        if (!xmlPathFile && subchap != null && subchap.size() > 0) {
            ref = subchap.get(0).getPath();
        }

        if (ref == null || ref.length() <= 0 || !ref.endsWith( ".xml.vm")) {
            return;
        }
        boolean refLoadContain = !refLoads.contains(ref);
        refLoads.add(ref);
        if (refLoadContain) {
            content.append("<item name=\"");
            content.append(chapter.getName());
            content.append("\" ref=\"");
            content.append(ref);
            content.append("\">");
            level++;
        }
        for (Chapter chapterTmp : chapter.getSubChapter()) {
            recursiveSysoutChapter(content, chapterTmp, level);
        }
        if (refLoadContain) {
            content.append("</item>\n");
        }
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if(files!=null) { //some JVMs return null for empty dirs
            for(File f: files) {
                if(f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    private void generateBasicContentToc(int toc, String directoryToGenerate, Chapter current) throws IOException {
        ITopic topic = tocs[toc].getTopic(null);
        generateBasicTopic(topic, directoryToGenerate, 0, current);
    }

    private void generateBasicTopic(ITopic topic, String directoryToGenerate, int level, Chapter current)
            throws IOException {

        String[] tocDisable = Preferences.getTocDisable();
        for (int k = 0; k < tocDisable.length; k++) {
            if (topic.getLabel().equals(tocDisable[k].trim())) {
                return;
            }
        }

        String href = topic.getHref();
        if (href != null) {
            int indexOfSharp = href.indexOf("#");
            if (indexOfSharp > 0) {
                href = href.substring(0, indexOfSharp);
            }
        }
        if (tocLoads.contains(href) || href == null) {
            return;
        }
        tocLoads.add(href);

        ITopic[] topics = getEnabledSubtopics(topic);
        boolean hasNodes = topics.length > 0;

        Files.createDirectories(Paths.get(directoryToGenerate));
        String fileName = directoryToGenerate + File.separator + topic.getLabel() + ".xml.vm";
        File file = new File(fileName);
        file.createNewFile();
        Activator.logInfo("Path : " + pathWithouGeneratedDirectory(fileName) + " - label : " + topic.getLabel() + " - href : " + href);
        String charset = getContentCharset(href);
        String content = getContentTopics(topic.getHref(), level);
        content = applyRegex(content);
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), charset));
        //wrap html into xml tag for pdf gen
        writer.write("<document xmlns=\"http://maven.apache.org/XDOC/2.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/XDOC/2.0 http://maven.apache.org/xsd/xdoc-2.0.xsd\"><body>");
        writer.write(content);
        writer.write("</body></document>");
        writer.flush();
        writer.close();
        Chapter temp = new Chapter(pathWithouGeneratedDirectory(fileName), topic.getLabel(), charset);
        current.getSubChapter().add(temp);

        if (hasNodes) {
            level++;
            for (int i = 0; i < topics.length; i++) {
                generateBasicTopic(topics[i], directoryToGenerate + File.separator + topics[i].getLabel(), level, temp);
            }
        }
    }

    /**
     * apply regex on wrong html to format it
     * @param content
     * @return
     */
    private String applyRegex(String content) {
        String newContent = content.replaceAll("(?i)<br>", "<br/>")
                .replaceAll("(?i)<hr>", "<hr/>")
                .replaceAll("(</?)h([0-9])", "$1H$2")
                .replaceAll("(</?)(?i)code([^>]*)(>)", "");

        //table add double quote on missing quote
        String regex = "<table([^>]*)>";
        String regexAttribute = "(\\w+)\\s*=\\s*([\\w\\d]*[^/\\s>]?)";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(newContent);
        while (matcher.find()) {
            String contentGroup = matcher.group(0);

            Pattern patternSub = Pattern.compile(regexAttribute);
            Matcher matcherSub = patternSub.matcher(contentGroup);
            while (matcherSub.find()) {
                if (matcherSub.groupCount() >= 2) {
                    String contentTmp = matcherSub.group(2);
                    if (contentTmp.trim().length() > 0) {
                        String replaceString = matcherSub.replaceAll("$1=\"$2\"").replaceAll("\"\"\"", "\"");
                        newContent = newContent.replaceAll(contentGroup, replaceString);
                    }
                }
            }
        }

        //img width and height to large
        regex = "<(img|IMG)([^>]*)>";
        regexAttribute = "(width|height)\\s*=[\"\']?(\\w+)[\"\']?";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(newContent);
        while (matcher.find()) {
            String contentGroup = matcher.group(0);
            Pattern patternSub = Pattern.compile(regexAttribute);
            Matcher matcherSub = patternSub.matcher(contentGroup);
            while (matcherSub.find()) {
                if (matcherSub.groupCount() >= 1) {
                    String contentTmp = matcherSub.group(2);
                    if (contentTmp.endsWith("%")) {
                        continue;
                    }
                    if (contentTmp.toLowerCase().endsWith("px")) {
                        contentTmp = contentTmp.substring(0, contentTmp.length() - 2);
                    }
                    if (Integer.parseInt(contentTmp) > 500) {
                        String replaceString = matcherSub.replaceAll("");
                        newContent = newContent.replaceAll(contentGroup, replaceString);
                    }
                }
            }
        }

        return newContent;
    }

    private String pathWithouGeneratedDirectory(String pathOrig) {
        if (pathOrig != null) {
            int indexOfGenDir = pathOrig.indexOf(GENERATED_DIRECTORY_XDOC);
            pathOrig = pathOrig.substring(indexOfGenDir + GENERATED_DIRECTORY_XDOC.length());
            if (pathOrig.startsWith("/")) {
                return "." + pathOrig;
            }
        }
        return pathOrig;
    }

    private String getContentTopics(String href, int level) {
        if (href == null) {
            return "";
        }
        String locale = Locale.getDefault().toString();
        //get the content

        String content = getContent(href, locale);

        href = removeAnchor(href);
        //get pict
        String newLocalFolder = IMAGES_DIRECTORY + removeAnchor(href).replaceAll("[^a-zA-Z]+","");
        String regexOr = "(?:src *= *)([\"\']([^\"\']+)|([a-zA-Z0-9.\\/\\-\\_\\\\]+[^ \\/>]))";
        Pattern pattern = Pattern.compile(regexOr);
        Matcher matcher = pattern.matcher(content);
        while (matcher.find()) {
            String src = ((matcher.group(2) != null) ? matcher.group(2).trim() : null);
            if (src == null) {
                src = ((matcher.group(3) != null) ? matcher.group(3).trim() : null);
            }
            String filename = null;
            if (src != null) {
                filename = copyImagesAndReplaceContent(href, src, newLocalFolder, content);
                if (filename != null) {
                    content = content.replaceAll(src, filename);
                }
            }
        }

        int indexBodyBegin = content.toLowerCase().indexOf("<body");
        if (indexBodyBegin >= 0) {
            int indexBodyBeginClose = content.substring(indexBodyBegin).indexOf(">");
            int indexBodyEnd = content.toLowerCase().indexOf("</body");
            if (indexBodyBeginClose >= 0 && indexBodyEnd > 0 && indexBodyEnd < content.length()) {
                content = content.substring(indexBodyBegin + indexBodyBeginClose + 1, indexBodyEnd);
            }
        }

        return content;
    }

    private String copyImagesAndReplaceContent(String href, String src, String newLocalFolder, String content) {
        String finalNameSrc = null;
        try {
            String url = href + File.separator + src;
            if ((href + "/" + src).indexOf(" ") > 0) {
                url = URLEncoder.encode(href + File.separator + src, StandardCharsets.UTF_8.name());
            }
            //avoid URLSyntaxException
            url = url.replace("\\", "/");

            InputStream in = HelpSystem.getHelpContent(url, Locale.getDefault().toString());
            finalNameSrc = "." + File.separator + newLocalFolder + File.separator + src.replaceAll("../", "");
            if (in != null) {
                if (!imgAdded.contains(finalNameSrc)) {
                    //create dir and copy pict
                    Files.createDirectories(Paths.get(GENERATED_DIRECTORY + File.separator + newLocalFolder));
                    CopyOption[] options = new CopyOption[]{ StandardCopyOption.REPLACE_EXISTING };
                    Files.copy(in, Paths.get(GENERATED_DIRECTORY + File.separator + finalNameSrc), options);
                    in.close();
                    imgAdded.add(finalNameSrc);
                }
            } else {
                finalNameSrc = null;
            }
        } catch (Exception e) {
            Activator.getLogger().log(Level.WARNING, "error", e);
        }
        return finalNameSrc;
    }

    private static String removeAnchor(String href) {
        int index = href.indexOf('#');
        if (index != -1) {
            href = href.substring(0, index);
        }
        index = href.lastIndexOf('.');
        if (index != -1) {
            int indexTmp = href.lastIndexOf('/');
            if (indexTmp != -1 && indexTmp < index) {
                href = href.substring(0, indexTmp);
            }
        }
        return href;
    }
    /**
     * Returns the string content of the referenced topic in UTF-8.
     */
    private String getContentCharset(String href) {
        String charset = null;
        InputStream in = HelpSystem.getHelpContent(href, Locale.getDefault().toString());
        if (in != null) {
            try {
                charset = getContentCharsetCustom(in);
            } catch (Exception e) {
                Activator.getLogger().log(Level.WARNING, "error on get charset", e);
            } finally {
                try {
                    in.close();
                } catch (IOException e) {
                    Activator.getLogger().log(Level.WARNING, "error on get charset", e);
                }
            }
        }
        if (charset == null) {
            charset = "UTF-8"; //$NON-NLS-1$
        }
        return charset;
    }

    private String getContentCharsetCustom(InputStream in) {
        String charset;
        charset = HTMLDocParser.getCharsetFromHTML(in);
        if (charset == null || charset.toLowerCase().equals("iso-8859-1")) {
            charset = "UTF-8"; //$NON-NLS-1$
        }
        return charset;
    }
    /*
     * Returns the string content of the referenced topic in UTF-8.
     */
    private String getContent(String href, String locale) {
        InputStream in = HelpSystem.getHelpContent(href, locale);
        StringBuffer buf = new StringBuffer();
        InputStream rawInput=null;
        if (in != null) {
            try {
                String charset = getContentCharsetCustom(in);

                rawInput = HelpSystem.getHelpContent(href, locale);
                in = DynamicXHTMLProcessor.process(href, rawInput, locale, false);

                if (in == null) {
                    in = HelpSystem.getHelpContent(href, locale);
                }

                Reader reader = new BufferedReader(new InputStreamReader(in, charset));
                char[] cbuf = new char[4096];
                int num;
                while ((num = reader.read(cbuf)) > 0) {
                    buf.append(cbuf, 0, num);
                }
            }
            catch (Exception e) {
                String msg = "Error retrieving print preview content for " + href; //$NON-NLS-1$
                Activator.getLogger().log(Level.WARNING, msg);
            } finally {
                try {
                    in.close();
                }
                catch (IOException e) {
                }
                try {
                    if (rawInput != null)
                        rawInput.close();
                }
                catch (Exception e) {}
            }
        }
        return buf.toString();
    }

    /**
     * Obtains children topics for a given navigation element. Topics from TOCs
     * not matching enabled activities are filtered out.
     *
     * @param element
     *            ITopic or IToc
     * @return ITopic[]
     */
    private ITopic[] getEnabledSubtopics(Object element) {
        List topics = getEnabledSubtopicList(element);
        return (ITopic[]) topics.toArray(new ITopic[topics.size()]);
    }


    /**
     * Obtains children topics for a given navigation element. Topics from TOCs
     * not matching enabled activities are filtered out.
     *
     * @param navigationElement
     * @return List of ITopic
     */
    private List getEnabledSubtopicList(Object element) {
        if (element instanceof IToc && !isEnabled((IToc) element))
            return Collections.EMPTY_LIST;
        List children;
        if (element instanceof IToc) {
            children = Arrays.asList(((IToc) element).getTopics());
        } else if (element instanceof ITopic) {
            children = Arrays.asList(((ITopic) element).getSubtopics());
        } else {
            // unknown element type
            return Collections.EMPTY_LIST;
        }
        List childTopics = new ArrayList(children.size());
        for (Iterator childrenIt = children.iterator(); childrenIt.hasNext();) {
            Object c = childrenIt.next();
            if ((c instanceof ITopic)) {
                // add topic only if it will not end up being an empty
                // container
                if (((((ITopic) c).getHref() != null && ((ITopic) c).getHref()
                        .length() > 0) || getEnabledSubtopicList(c).size() > 0)
                        && !UAContentFilter.isFiltered(c,
                                HelpEvaluationContext.getContext())) {
                    childTopics.add(c);
                }
            } else {
                // it is a Toc, Anchor or Link,
                // which may have children attached to it.
                childTopics.addAll(getEnabledSubtopicList(c));
            }
        }
        return childTopics;
    }

    /**
     * Check if given TOC is visible
     *
     * @param toc
     * @return true if TOC should be visible
     */
    private boolean isEnabled(IToc toc) {
        return true;
    }
}
