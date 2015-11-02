package com.example;

import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.reflections.Reflections;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by Anand.Shah on 11/2/2015.
 */
public class DocumentationGenerator {

    private static final String HTML_HEADER =
            "<html>" +
                    "<head>" +
                    "<link href=\"https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css\" rel=\"stylesheet\" integrity=\"sha256-MfvZlkHCEqatNoGiOXveE8FIwMzZg4W85qfrfIFBfYc= sha512-dTfge/zgoMYpP7QbHy4gWMEGsbsdZeCXz7irItjcC3sPUFtf0kuFbDz/ixG7ArTxmDjLXDmezHubeNikyKGVyQ==\" crossorigin=\"anonymous\">" +
                    "</head>" +
                    "<body>" +
                    "   <table class='table'>" +
                    "       <tr>" +
                    "<th>Sr.No</th> " +
                    "<th>Annotation</th> " +
                    "<th>Java Docs</th> " +
                    "</tr>";
    private static final String HTML_FOOTER =
            "   </table>" +
                    "</body>" +
                    "</html>";
    private static final String ROW_TEMPLATE =
            "<tr>" +
                    "   <th>%s</th> " +
                    "   <th>%s</th> " +
                    "   <td><div>%s</div></td> " +
                    "</tr>";

    public static void main(String[] args) throws Exception {
        Date startTime = new Date();
        Reflections reflections = new Reflections("org.springframework");
        List<Class<?>> clazzes = new ArrayList<Class<?>>(
                reflections.getTypesAnnotatedWith(Retention.class, true));
        int total = clazzes.size();
        int i = 0;
        int errorCount = 0;
        int docNotPresentCount = 0;
        int counter = 1;

        FileWriter writer = new FileWriter("annotations.html");
        writer.write(HTML_HEADER);

        for (Class<?> clazz : clazzes) {
            i++;
            try {
                System.out.println("=> Processing (" + i + "/" + total + ") - " + clazz);
                File jar = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                System.out.println("Reading from " + clazz.getCanonicalName() + " - " + jar.getAbsolutePath().replace(".jar", "-javadoc.jar"));
                String html = readZipFile(jar.getAbsolutePath().replace(".jar", "-javadoc.jar"), clazz.getCanonicalName().replace(".", "/") + ".html");

                if (html == null) {
                    System.out.println("=> No documentation found for (" + i + "/" + total + ") - " + clazz);
                    docNotPresentCount++;
                    continue;
                }

                Document document = Jsoup.parse(html);


                Elements elements = document.select("div.block");
                if (elements != null) {
                    Element first = elements.first();
                    if (first != null) {
                        writer.append(String.format(ROW_TEMPLATE, counter++, "<code>@" + clazz.getSimpleName() + "</code> (" + clazz.getCanonicalName() + ")", first.html()));
                    }
                }
            } catch (Exception e) {
                errorCount++;
                System.out.println("=> Error in processing (" + i + "/" + total + ") - " + clazz);
                e.printStackTrace();
            }
            System.out.println("=> Processed (" + i + "/" + total + ") - " + clazz);
        }

        writer.write(HTML_FOOTER);
        writer.close();

        System.out.println("*****************************************************************************");
        System.out.println("Started at: " + startTime);
        System.out.println("Ended at: " + new Date());
        System.out.println("Processed total: (" + i + "/" + total + ")");
        System.out.println("Success total: " + (total - errorCount - docNotPresentCount));
        System.out.println("Failed total: " + (errorCount));
        System.out.println("Documentation Missing total: " + (docNotPresentCount));
        System.out.println("*****************************************************************************");
    }

    public static String readZipFile(String zipFilePath, String relativeFilePath) throws IOException {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> e = zipFile.entries();

            while (e.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                // if the entry is not directory and matches relative file then extract it
                if (!entry.isDirectory() && entry.getName().equals(relativeFilePath)) {
                    BufferedInputStream bis = new BufferedInputStream(
                            zipFile.getInputStream(entry));
                    String fileContentsStr = IOUtils.toString(bis, "UTF-8");
                    bis.close();
                    return fileContentsStr;
                } else {
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
