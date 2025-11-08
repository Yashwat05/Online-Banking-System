package com.bankapp.util;

import com.bankapp.model.Transaction;
import java.io.File;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;

public class TransactionXMLExporter {

    // Export transaction list to an XML file
    public static boolean exportToXML(List<Transaction> txns, String filePath) {
        try {
            // Ensure exports directory exists
            File exportDir = new File("exports");
            if (!exportDir.exists()) exportDir.mkdirs();

            // XML Document setup
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.newDocument();

            // Root element
            Element rootElement = doc.createElement("transactions");
            doc.appendChild(rootElement);

            // Add each transaction
            for (Transaction t : txns) {
                Element txnElem = doc.createElement("transaction");

                appendChild(doc, txnElem, "userId", String.valueOf(t.getUserId()));
                appendChild(doc, txnElem, "fromAccount", t.getFromAccount());
                appendChild(doc, txnElem, "toAccount", t.getToAccount());
                appendChild(doc, txnElem, "amount", String.valueOf(t.getAmount()));
                appendChild(doc, txnElem, "txnType", t.getTxnType());
                appendChild(doc, txnElem, "txnDate", t.getTxnDate() != null ? t.getTxnDate().toString() : "");

                rootElement.appendChild(txnElem);
            }

            // Write XML to file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);

            System.out.println("Transactions exported successfully to: " + new File(filePath).getAbsolutePath());
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("XML export failed: " + e.getMessage());
            return false;
        }
    }

    // Helper method to append tag:value safely
    private static void appendChild(Document doc, Element parent, String tag, String value) {
        if (value != null && !value.isEmpty()) {
            Element elem = doc.createElement(tag);
            elem.appendChild(doc.createTextNode(value));
            parent.appendChild(elem);
        }
    }
}
